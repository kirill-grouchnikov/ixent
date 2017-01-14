package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Logger;

import org.jvnet.ixent.algorithms.graphics.segmentation.SegmentationInfo;
import org.jvnet.ixent.algorithms.graphics.segmentation.Segmentator;
import org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure.*;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.matrix.Matrix;

/**
 * A class that implements the AMG segmentation as outlined in <i>"Fast
 * Multiscale Image Segmentation"</i> by Sharon, Brandt and Basri in 2000
 * paper.
 * 
 * @author Kirill Grouchnikov
 */
public class AMGSegmentator implements Segmentator {
	private BufferedImage inputImage;

	private int width, height;

	private Logger logger;

	private SegmentationInfo segmentationInfo;

	/**
	 * Private class used for sorting salient segments throughout the image
	 * multi-scale pyramid
	 */
	private static class NodeAndGraphInfo implements
			Comparable<NodeAndGraphInfo> {
		public NodeInfo nodeInfo;
		public GraphInfo graphInfo;

		public NodeAndGraphInfo(NodeInfo _nodeInfo, GraphInfo _graphInfo) {
			this.nodeInfo = _nodeInfo;
			this.graphInfo = _graphInfo;
		}

		public int compareTo(NodeAndGraphInfo info2) {
			if (this == info2) {
				return 0;
			}
			// compare saliency
			double diff = this.nodeInfo.getSaliency()
					- info2.nodeInfo.getSaliency();
			if (diff < 0.0) {
				return 1;
			}
			if (diff > 0.0) {
				return -1;
			}
			return 0;
		}
	}

	public AMGSegmentator() {
		this.logger = Logger.getLogger(AMGSegmentator.class.getPackage()
				.getName());
	}

	/**
	 * Initialize with true-color bitmap object
	 * 
	 * @param bitmapObject
	 *            input image
	 */
	public void init(BufferedImage bitmapObject) {
		this.inputImage = bitmapObject;
		this.width = this.inputImage.getWidth();
		this.height = this.inputImage.getHeight();
	}

	/**
	 * compute initial graph of input image
	 * 
	 * @return initial graph
	 */
	private GraphInfo getInitialGraph() {
		// convert input image into greyscale
		IndexBitmapObject ibo = IndexBitmapObject
				.getAsGreyscale(this.inputImage);
		int[][] greys = ibo.getBitmap();

		int nodeCount = this.width * this.height;
		NodeInfo[] nodes = new NodeInfo[nodeCount];
		for (int col = 0; col < this.width; col++) {
			for (int row = 0; row < this.height; row++) {
				int nodeIndex = row * this.width + col;
				NodeInfo newNode = new NodeInfo(nodeIndex, 1.0);
				nodes[nodeIndex] = newNode;
				// right
				if (col < (this.width - 1)) {
					int rightIndex = nodeIndex + 1;
					int diff = Math.abs(greys[col][row] - greys[col + 1][row]);
					EdgeInfo edge = new EdgeInfo(nodeIndex, rightIndex,
							1.0 - (double) diff / 256.0);
					newNode.addEdge(edge);
				}
				// left
				if (col > 0) {
					int leftIndex = nodeIndex - 1;
					int diff = Math.abs(greys[col][row] - greys[col - 1][row]);
					EdgeInfo edge = new EdgeInfo(nodeIndex, leftIndex,
							1.0 - (double) diff / 256.0);
					newNode.addEdge(edge);
				}
				// bottom
				if (row < (this.height - 1)) {
					int bottomIndex = nodeIndex + this.width;
					int diff = Math.abs(greys[col][row] - greys[col][row + 1]);
					EdgeInfo edge = new EdgeInfo(nodeIndex, bottomIndex,
							1.0 - (double) diff / 256.0);
					newNode.addEdge(edge);
				}
				// top
				if (row > 0) {
					int topIndex = nodeIndex - this.width;
					int diff = Math.abs(greys[col][row] - greys[col][row - 1]);
					EdgeInfo edge = new EdgeInfo(nodeIndex, topIndex,
							1.0 - (double) diff / 256.0);
					newNode.addEdge(edge);
				}
			}
		}
		GraphInfo gi = new GraphInfo(0, nodes);
		Coarsener coarsener = CoarsenerFactory
				.getCoarsener(this.inputImage, gi);
		coarsener.fillAdditionalInformation();

		return gi;
	}

	/**
	 * Compute a coarsened version of given graph
	 * 
	 * @param currGraph
	 *            current graph
	 * @return coarse version of this graph
	 */
	private GraphInfo getNextGraph(GraphInfo currGraph) {
		Coarsener coarsener = CoarsenerFactory.getCoarsener(this.inputImage,
				currGraph);
		return coarsener.getNextGraph(this.height * this.width);
	}

	/**
	 * Returns all salient segments throught the image segmentation pyramid
	 * 
	 * @param graphList
	 *            image segmentation pyramid as a linked list of graphs
	 * @param segmentCount
	 *            at most this number of segments will be returned
	 * @return a list of salient segments (pointer to a node and to a graph)
	 */
	private List<NodeAndGraphInfo> getAllSalientSegments(
			LinkedList<GraphInfo> graphList, int segmentCount) {
		// At this point we have a pyramid of segments. Now we have to search
		// the
		// pyramid looking for salient segments at all levels
		List<NodeAndGraphInfo> saliencyList = new ArrayList<NodeAndGraphInfo>();
		for (GraphInfo currGraph : graphList) {
			if (currGraph == graphList.getFirst()) {
				continue;
			}
			for (NodeInfo currNode : currGraph.getNodes()) {
				if (currNode.getVolume() == 0.0) {
					continue;
				}
				// check saliency
				double currSaliency = currNode.getSaliency();
				if ((currSaliency > 0.0) && (currSaliency < 0.01)) {
					saliencyList.add(new NodeAndGraphInfo(currNode, currGraph));
				}
			}
		}
		// sort saliency list so that the most salient element (with the
		// smallest
		// saliency) is last
		Collections.sort(saliencyList);
		int toLeave = segmentCount - graphList.getFirst().getNodeCount();
		if (toLeave > 0) {
			while (saliencyList.size() > toLeave) {
				saliencyList.remove(0);
			}
		}

		// add all elements from the last graph
		GraphInfo lastGraph = graphList.getFirst();
		for (NodeInfo currNode : lastGraph.getNodes()) {
			if (currNode.getVolume() == 0.0) {
				continue;
			}
			// check saliency
			saliencyList.add(new NodeAndGraphInfo(currNode, lastGraph));
		}
		Collections.sort(saliencyList);
		return saliencyList;
	}

	/**
	 * Computes image segmentation
	 * 
	 * @param segmentsAtLastLevel
	 *            at most this number of segments will be computed at the last
	 *            level of the algorithm
	 */
	public void process(int segmentsAtLastLevel) {
		long time0 = System.currentTimeMillis();
		LinkedList<GraphInfo> graphList = new LinkedList<GraphInfo>();
		// create initial graph
		GraphInfo initialGraph = this.getInitialGraph();
		graphList.addFirst(initialGraph);

		GraphInfo prevGraph = initialGraph;
		int prevGraphSize = prevGraph.getNodeCount();
		int countAtSameStep = 0;
		while (true) {
			this.logger.finer("Creating next graph...");
			GraphInfo nextGraph = this.getNextGraph(prevGraph);
			if (nextGraph.getNodeCount() < 2) {
				break;
			}
			graphList.addFirst(nextGraph);
			// compute how many nodes there are that have non-zero volume
			int graphSize = nextGraph.getNodeCount();

			if (graphSize == prevGraphSize) {
				countAtSameStep++;
				if (countAtSameStep >= 10) {
					// no coarsening is possible. Break out
					this.logger.warning("No further segmentation possible");
					break;
				} else {
					this.logger.info("Trying to coarsen once again");
					graphList.remove(0);
				}
			} else {
				countAtSameStep = 0;
				int nzNodes = 0;
				for (int i = 0; i < graphSize; i++) {
					if (nextGraph.getNode(i).getVolume() > 0.0) {
						nzNodes++;
					}
				}
				this.logger.finer("Have " + nzNodes
						+ " non empty nodes for this graph");

				if (nzNodes <= segmentsAtLastLevel) {
					break;
				}
				prevGraph = nextGraph;
				prevGraphSize = graphSize;
			}
		}
		long time1 = System.currentTimeMillis();

		this.logger.info("Creating multi-scale image pyramid : "
				+ (time1 - time0));

		// Allocate and initialize the array for segments
		int[][] segmentIndexMap = new int[this.width][this.height];
		double[][] bestStateVector = new double[this.width][this.height];
		for (int col = 0; col < this.width; col++) {
			for (int row = 0; row < this.height; row++) {
				segmentIndexMap[col][row] = -1;
				bestStateVector[col][row] = -1.0;
			}
		}

		List<NodeAndGraphInfo> saliencyList = getAllSalientSegments(graphList,
				segmentsAtLastLevel);

		int segmentNumber = 0;
		// go over all salient segments
		for (NodeAndGraphInfo currSaliencyNode : saliencyList) {
			NodeInfo currNode = currSaliencyNode.nodeInfo;
			GraphInfo currGraph = currSaliencyNode.graphInfo;
			this.logger.finest("Computing support for " + currNode.getIndex()
					+ " with saliency " + currNode.getSaliency() + " at level "
					+ currGraph.getIndex());
			int graphSize = currGraph.getNodeCount();
			double[] prevU = new double[graphSize];
			for (int i = 0; i < graphSize; i++) {
				prevU[i] = 0.0;
			}
			prevU[currNode.getIndex()] = 1.0;

			int graphPositionInList = graphList.indexOf(currGraph);
			ListIterator<GraphInfo> iterator = graphList
					.listIterator(graphPositionInList);
			while (iterator.hasNext()) {
				GraphInfo graph = iterator.next();
				// get interpolation matrix
				Matrix currInterpolationMatrix = graph.getInterpolationMatrix();
				if (currInterpolationMatrix == null) {
					break;
				}
				double[] nextU = currInterpolationMatrix.multiply(prevU);
				prevU = nextU;
			}
			// at this point the 'prevU' vector holds values that will allow
			// to associate pixels with this segment
			for (int col = 0; col < this.width; col++) {
				for (int row = 0; row < this.height; row++) {
					int pos = row * this.width + col;
					// compare previous best with this
					if (bestStateVector[col][row] < prevU[pos]) {
						bestStateVector[col][row] = prevU[pos];
						segmentIndexMap[col][row] = segmentNumber;
					}
				}
			}
			segmentNumber++;
		}
		long time2 = System.currentTimeMillis();

		this.logger
				.info("Computing image segment support : " + (time2 - time1));

		// compute "thick boundary" map. A pixel has value 0 if all of its
		// neighbours
		// have the same color value. Otherwise it has value 255
		int[][] thickBoundaries = new int[this.width][this.height];
		for (int col = 0; col < this.width; col++) {
			for (int row = 0; row < this.height; row++) {
				int different = 0;
				for (int dc = 0; dc <= 1; dc++) {
					int newCol = col + dc;
					if ((newCol < 0) || (newCol == this.width)) {
						continue;
					}
					for (int dr = 0; dr <= 1; dr++) {
						int newRow = row + dr;
						if ((newRow < 0) || (newRow == this.height)) {
							continue;
						}
						if (segmentIndexMap[col][row] != segmentIndexMap[newCol][newRow]) {
							different++;
						}
					}
				}
				int finalValue = 255 * different / 3;
				if (finalValue > 255) {
					finalValue = 255;
				}
				thickBoundaries[col][row] = finalValue;
			}
		}

		IndexBitmapObject segmentationAreasObject = new IndexBitmapObject(
				segmentIndexMap, this.width, this.height);
		IndexBitmapObject segmentationBoundariesObject = new IndexBitmapObject(
				thickBoundaries, this.width, this.height);
		this.segmentationInfo = new SegmentationInfo(segmentNumber,
				segmentationAreasObject, segmentationBoundariesObject);
		graphList.clear();
	}

	/**
	 * Returns all available information about image segmentation.
	 * 
	 * @return all available information about image segmentation
	 */
	public SegmentationInfo getSegmentationInfo() {
		return this.segmentationInfo;
	}

	/**
	 * Returns image segmentation as a 2-D array of indexes. Each pixel is
	 * assigned a unique ID of its segment
	 * 
	 * @return two-dimensional array. Each pixel is assigned an index. Pixels
	 *         belonging to the same segment have the same indexes.
	 */
	public IndexBitmapObject getSegmentationAreas() {
		return this.segmentationInfo.getAreasBitmap();
	}

	/**
	 * Returns image segmentation as a 2-D array of indexes. Each pixel is
	 * assigned a probability of belonging to segment boundary
	 * 
	 * @return Each entry will contain a value from 0 to 255. 0 - no segment
	 *         boundary at this pixel, 255 - certain segment boundary at this
	 *         pixel
	 */
	public IndexBitmapObject getSegmentationBoundaries() {
		return this.segmentationInfo.getBoundariesBitmap();
	}

}
