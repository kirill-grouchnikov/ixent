package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale;

import java.util.List;

import org.jvnet.ixent.algorithms.geometry.delaunay.DelaunayManager;
import org.jvnet.ixent.algorithms.geometry.delaunay.DelaunayManagerFactory;
import org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure.*;
import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.math.coord.Vertex2D;
import org.jvnet.ixent.math.matrix.SparseAccessibleMatrix;
import org.jvnet.ixent.math.matrix.SparseMatrix;

/**
 * The brightness-based coarsener, as outlined in <i>"Fast Multiscale Image
 * Segmentation"</i> by Sharon, Brandt and Basri in 2000 paper. At each stage,
 * the degree of coarse node similarity is computed based only on average
 * brightness of pixels lying under the coarse nodes.
 * 
 * @author Kirill Grouchnikov
 */
public class BrightnessCoarsener extends AggregateCoarsener {
	protected double[] sumX;
	protected double[] sumY;
	protected double[] sumWeights;
	protected double[] sumBrightness;

	/**
	 * Fill additional information on single pixel
	 * 
	 * @param col
	 *            pixel column
	 * @param row
	 *            pixel row
	 */
	protected void fillAdditionalInformation(int col, int row) {
		int rgb = this.originalImage.getRGB(col, row);
		int r = (rgb & 0x00FF0000) >> 16;
		int g = (rgb & 0x0000FF00) >> 8;
		int b = (rgb & 0x000000FF);

		int luminance = (int) ((222.0 * r + 707.0 * g + 71.0 * b) / (256.0 * 1000.0));

		int width = this.originalImage.getWidth();
		int nodeIndex = row * width + col;
		BrightnessNodeInfo bni = (BrightnessNodeInfo) this.prevGraphInfo
				.getNode(nodeIndex).getAdditionalInfo();
		if (bni == null) {
			bni = new BrightnessNodeInfo(col, row, luminance);
			this.prevGraphInfo.getNode(nodeIndex).setAdditionalInfo(bni);
		} else {
			bni.setBrightness(luminance);
		}
	}

	/**
	 * Fill additional information (such as brightness for example). Will be
	 * called once for the initial graph
	 */
	public void fillAdditionalInformation() {
		int width = this.originalImage.getWidth();
		int height = this.originalImage.getHeight();
		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				this.fillAdditionalInformation(col, row);
			}
		}
	}

	/**
	 * Allocate and intialize arrays for info on new nodes
	 * 
	 * @param newSize
	 *            new graph size
	 * @param capturersArray
	 *            info on node capturers
	 */
	protected void initializeArrays(int newSize, NodeInfo[] capturersArray) {
		this.sumX = new double[newSize];
		this.sumY = new double[newSize];
		this.sumWeights = new double[newSize];
		this.sumBrightness = new double[newSize];
		for (int i = 0; i < newSize; i++) {
			this.sumX[i] = 0.0;
			this.sumY[i] = 0.0;
			this.sumWeights[i] = 0.0;
			this.sumBrightness[i] = 0.0;
		}

		// compute various statistics
		int oldMatrixSize = this.prevGraphInfo.getNodeCount();
		for (int currNodeIndex = 0; currNodeIndex < oldMatrixSize; currNodeIndex++) {
			// capturer index
			int currCapturerIndex = capturersArray[currNodeIndex].getIndex();
			NodeInfo currNode = this.prevGraphInfo.getNode(currNodeIndex);

			BrightnessNodeInfo bni = (BrightnessNodeInfo) currNode
					.getAdditionalInfo();
			this.sumX[currCapturerIndex] += (bni.getX() * currNode.getVolume());
			this.sumY[currCapturerIndex] += (bni.getY() * currNode.getVolume());
			this.sumBrightness[currCapturerIndex] += (bni.getBrightness() * currNode
					.getVolume());
			this.sumWeights[currCapturerIndex] += currNode.getVolume();
		}

		// normalize brightness
		for (int i = 0; i < newSize; i++) {
			this.sumBrightness[i] /= this.sumWeights[i];
		}

	}

	/**
	 * Create weight matrix for the new graph based on 'capturer' info (which
	 * node captured which node) and interpolation matrix
	 * 
	 * @param origSize
	 *            the size of the original graph
	 * @param selectedNodes
	 *            capturers
	 * @param capturersArray
	 *            capturer info
	 * @param interpolationMatrix
	 *            interpolation matrix
	 * @return new weight matrix
	 */
	protected GraphInfo createNewGraph(int origSize,
			List<NodeInfo> selectedNodes, NodeInfo[] capturersArray,
			SparseMatrix interpolationMatrix) {

		int newMatrixSize = selectedNodes.size();

		// for each capturer compute the weight mass of all the nodes that it
		// has captured
		this.initializeArrays(newMatrixSize, capturersArray);

		// create an array of 2D points - each point is a coarsened node
		Vertex2D[] pointArray = new Vertex2D[newMatrixSize];
		Vertex2D.resetID();
		for (int i = 0; i < newMatrixSize; i++) {
			pointArray[i] = new Vertex2D(this.sumX[i] / this.sumWeights[i],
					this.sumY[i] / this.sumWeights[i]);
		}

		// create resulting matrix
		SparseAccessibleMatrix newWeightMatrix = new SparseAccessibleMatrix(
				newMatrixSize, newMatrixSize);

		if (newMatrixSize < 1000) {
			// perform Delaunay triangulation on this point array
			DelaunayManager dm = DelaunayManagerFactory
					.getDelaunayManager(pointArray);
			List<Triangle> delaunayTriangles = dm.getTriangulation();

			// go over all triangles
			for (Triangle currTriangle : delaunayTriangles) {
				// each triangle has three edges
				processSingleEdge(newWeightMatrix, currTriangle.getVertex12()
						.getID(), currTriangle.getVertex13().getID());
				processSingleEdge(newWeightMatrix, currTriangle.getVertex13()
						.getID(), currTriangle.getVertex23().getID());
				processSingleEdge(newWeightMatrix, currTriangle.getVertex12()
						.getID(), currTriangle.getVertex23().getID());
			}
		} else {
			// for big graphs - there's an edge in coarsened graph between
			// each capturer of a node and the capturers of its neighbours
			for (NodeInfo currNode : this.prevGraphInfo.getNodes()) {
				int currNodeIndex = currNode.getIndex();
				int currCapturerIndex = capturersArray[currNodeIndex]
						.getIndex();
				for (EdgeInfo currEdge : currNode.getEdges()) {
					int neighbourIndex = currEdge
							.getOtherNodeIndex(currNodeIndex);
					int currNeighbourCapturerIndex = capturersArray[neighbourIndex]
							.getIndex();
					if (currCapturerIndex != currNeighbourCapturerIndex) {
						processSingleEdge(newWeightMatrix, currCapturerIndex,
								currNeighbourCapturerIndex);
					}
				}
			}
		}

		// compute new node array - copy from selected nodes
		NodeInfo[] newNodes = new NodeInfo[selectedNodes.size()];
		int count = 0;
		for (NodeInfo node : selectedNodes) {
			newNodes[count++] = node;
		}

		// compute all edges. Each edge corresponds to a non-zero value
		// in the weight matrix
		for (int row = 0; row < newMatrixSize; row++) {
			// compute how many non-zero values there are in the corresponding
			// column
			int nzCount = newWeightMatrix.getNzCountInRow(row);
			if (nzCount == 0) {
				continue;
			}

			for (int colIndex = 0; colIndex < nzCount; colIndex++) {
				int column = newWeightMatrix.getColumnByIndex(row, colIndex);
				double value = newWeightMatrix.getValueByIndex(row, colIndex);

				// this edge connects node number 'row' and node number 'column'
				EdgeInfo newEdge = new EdgeInfo(row, column, value);
				newNodes[row].addEdge(newEdge);
			}
		}

		// compute volume for each new node
		double[] newVolumes = new double[newMatrixSize];
		for (int i = 0; i < newMatrixSize; i++) {
			newVolumes[i] = newNodes[i].getVolume();
		}

		int nodeCount = this.prevGraphInfo.getNodeCount();
		for (int nodeIndex = 0; nodeIndex < nodeCount; nodeIndex++) {
			// who's the capturer?
			NodeInfo capturer = capturersArray[nodeIndex];
			int capturerIndex = capturer.getIndex();

			// go over all neighbours of the current node and sum up the
			// weights on edges that connect this node and nodes that were
			// captures by the same capturer
			NodeInfo currNode = this.prevGraphInfo.getNode(nodeIndex);
			double weightToCapturer = 0.0;
			for (EdgeInfo currNeighbourEdge : currNode.getEdges()) {
				int currNeighbourIndex = currNeighbourEdge
						.getOtherNodeIndex(nodeIndex);

				// check if this neighbour is captured
				assert capturersArray[currNeighbourIndex] != null : "No capturer for the current neighbour";

				if (capturersArray[currNeighbourIndex].getIndex() == capturerIndex) {
					weightToCapturer += currNeighbourEdge.getWeight();
				}
			}
			newVolumes[capturerIndex] += currNode.getVolume()
					* weightToCapturer;
		}

		// compute saliency for each new node
		double factor = (double) origSize / newMatrixSize;
		double[] newSaliencies = new double[newMatrixSize];
		for (NodeInfo currNewNode : newNodes) {
			int index = currNewNode.getIndex();
			double totalConnectivity = currNewNode.getTotalConnectivityWeight();
			newSaliencies[index] = factor * totalConnectivity
					/ newVolumes[index];
		}

		// update all nodes
		for (NodeInfo currNewNode : newNodes) {
			int index = currNewNode.getIndex();
			currNewNode.setVolume(newVolumes[index]);
			currNewNode.setSaliency(newSaliencies[index]);
		}

		// setLocation additional info
		this.updateNodeOfNewGraph(newNodes, pointArray);

		// // check that every node has at least one neighbour
		// if (newMatrixSize > 1) {
		// for (NodeInfo currNode: newNodes)
		// assert currNode.getNeighbourCount() > 0:
		// "No neighbours for node " + currNode.toString();
		// }

		GraphInfo result = new GraphInfo(this.prevGraphInfo.getIndex() + 1,
				newNodes, newWeightMatrix, interpolationMatrix);

		return result;

	}

	/**
	 * Set additional information (brightness for instance) of the new node
	 * 
	 * @param newNodes
	 *            new node array
	 * @param pointArray
	 *            new points
	 */
	protected void updateNodeOfNewGraph(NodeInfo[] newNodes,
			Vertex2D[] pointArray) {
		// update all nodes
		for (NodeInfo currNewNode : newNodes) {
			int index = currNewNode.getIndex();
			BrightnessNodeInfo bni = new BrightnessNodeInfo(pointArray[index]
					.getX(), pointArray[index].getY(),
					this.sumBrightness[index]);
			currNewNode.setAdditionalInfo(bni);
		}
	}

	/**
	 * Process single edge connecting two neighbouring vertices. This edge will
	 * directly correspond to an edge in the resulting matrix. The weight on the
	 * edge will be computed according to average brightness in two new clusters
	 * 
	 * @param weightMatrix
	 *            weight matrix for new graph
	 * @param nodeIndex1
	 *            index of first node
	 * @param nodeIndex2
	 *            index of second node
	 */
	protected void processSingleEdge(SparseAccessibleMatrix weightMatrix,
			int nodeIndex1, int nodeIndex2) {
		// check if weight already setLocation
		if (weightMatrix.get(nodeIndex1, nodeIndex2) > 0.0) {
			return;
		}

		// compute new weight
		double brightnessDifference = this.sumBrightness[nodeIndex1]
				- this.sumBrightness[nodeIndex2];
		double newWeight = 1.0 - Math.abs(brightnessDifference);

		weightMatrix.set(nodeIndex1, nodeIndex2, newWeight);
		weightMatrix.set(nodeIndex2, nodeIndex1, newWeight);
	}
}
