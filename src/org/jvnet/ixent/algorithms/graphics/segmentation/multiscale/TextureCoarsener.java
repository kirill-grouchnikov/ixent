package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale;

import java.util.logging.Logger;

import org.jvnet.ixent.algorithms.graphics.general.KMeans;
import org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure.*;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.PointND;
import org.jvnet.ixent.math.coord.Vertex2D;
import org.jvnet.ixent.math.filters.*;
import org.jvnet.ixent.math.matrix.SparseAccessibleMatrix;

/**
 * This class draws upon the 2003 <i>"Texture Segmentation by Multiscale
 * Aggregation of Filter Responses and Shape Elements"</i> by Meirav Galun,
 * Eitan Sharon, Ronen Basri and Achi Brandt. At deeper (>2) stages of image
 * pyramid, we use texture metrics to compute similarities between coarse nodes.
 * However, the texture metrics are computed as outlined in 2001 <i>"Contour and
 * Texture Analysis for Image Segmentation"</i> by Jitendra Malik, Serge
 * Belongie, Thomas Leung and Jianbo Shi. We use texton - grouped filter
 * responses (computed by <a href="../../general/KMeans.html">K-Means</a>
 * algorithm) histograms in order to assess texturedness of given region. These
 * histograms are compared using chi-2 statistic. <p/> This class overrides
 * specific portions of the more general algorithm implemented by <a
 * href="BrightnessCoarsener.html">BrightnessCoarsener</a> in order to reduce
 * code duplication
 * 
 * @author Kirill Grouchnikov
 */
public class TextureCoarsener extends BrightnessCoarsener {
	private Histogram[] sumHistograms;

	/**
	 * Will be used in <b>fillAdditionalInfo</b> on single pixel
	 */
	private KMeans kmeans;

	private Logger logger;

	public TextureCoarsener() {
		this.logger = Logger.getLogger(TextureCoarsener.class.getPackage()
				.getName());
	}

	/**
	 * Fill additional information on single pixel
	 * 
	 * @param col
	 * @param row
	 */
	protected void fillAdditionalInformation(int col, int row) {
		int width = this.originalImage.getWidth();
		int nodeIndex = row * width + col;

		// get index of k-mean center closest to this point
		int center = this.kmeans.getClosestCenterIndex(nodeIndex);

		// the number of bins in histogram is the same as number of centers (K)
		// in k-means
		Histogram histo = new Histogram(this.kmeans.getK());
		histo.setBin(center, 1.0);

		TextureNodeInfo tni = new TextureNodeInfo(col, row, histo);
		this.prevGraphInfo.getNode(nodeIndex).setAdditionalInfo(tni);

		// call super to setLocation brightness information
		super.fillAdditionalInformation(col, row);
	}

	/**
	 * Fill additional information (such as texton responses for example). Will
	 * be called once for the initial graph
	 */
	public void fillAdditionalInformation() {
		IndexBitmapObject ibo = IndexBitmapObject
				.getAsGreyscale(this.originalImage);

		int width = this.originalImage.getWidth();
		int height = this.originalImage.getHeight();
		Convolver convolver = new Convolver(ibo);

		long time0 = System.currentTimeMillis();
		Filter[] filters = FilterBank.getFilters(2.0, 1.0, 1, 2, 2, 2);
		PointND[][] responses = convolver.convolve(filters);
		long time1 = System.currentTimeMillis();
		this.logger.info("Creating and convolving " + filters.length
				+ " filters : " + (time1 - time0));
		PointND[] responseVector = new PointND[width * height];
		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				responseVector[row * width + col] = responses[col][row];
			}
		}

		long time2 = System.currentTimeMillis();
		this.kmeans = new KMeans(responseVector, filters.length, 12);

		// invoke k-means and ignore the resulting centers
		this.kmeans.getCenters(0.1, 50);

		long time3 = System.currentTimeMillis();
		this.logger.info("Computing " + this.kmeans.getK()
				+ " centers using K-Means : " + (time3 - time2));

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
		super.initializeArrays(newSize, capturersArray);
		this.sumHistograms = new Histogram[newSize];

		int histogramSize = ((TextureNodeInfo) this.prevGraphInfo.getNode(0)
				.getAdditionalInfo()).getTextonResponses().getBinCount();

		for (int i = 0; i < newSize; i++) {
			this.sumHistograms[i] = new Histogram(histogramSize);
		}
		int oldMatrixSize = this.prevGraphInfo.getNodeCount();
		for (int currNodeIndex = 0; currNodeIndex < oldMatrixSize; currNodeIndex++) {
			// capturer index
			int currCapturerIndex = capturersArray[currNodeIndex].getIndex();
			NodeInfo currNode = this.prevGraphInfo.getNode(currNodeIndex);
			TextureNodeInfo tni = (TextureNodeInfo) currNode
					.getAdditionalInfo();
			this.sumHistograms[currCapturerIndex].combine(tni
					.getTextonResponses());
		}
		for (int i = 0; i < newSize; i++) {
			this.sumHistograms[i].normalize();
		}

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
		// important - NOT to call super, as it will setLocation
		// BrightnessNodeInfo
		// and not TextureNodeInfo. Instead we call setBrightness here
		// explicitly

		// update all nodes
		for (NodeInfo currNewNode : newNodes) {
			int index = currNewNode.getIndex();
			TextureNodeInfo tni = new TextureNodeInfo(pointArray[index].getX(),
					pointArray[index].getY(), this.sumHistograms[index]);
			tni.setBrightness(this.sumBrightness[index]);
			currNewNode.setAdditionalInfo(tni);
		}
	}

	/**
	 * Process single edge connecting two neighbouring vertices. This edge will
	 * directly correspond to an edge in the resulting matrix. The weight on the
	 * edge will be computed according to chi2 metric of two node histograms
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

		// if (this.prevGraphInfo.getIndex() <= 2) {
		// super.processSingleEdge(weightMatrix, nodeIndex1, nodeIndex2);
		// return;
		// }
		//

		// check if weight already set
		if (weightMatrix.get(nodeIndex1, nodeIndex2) > 0.0) {
			return;
		}

		super.processSingleEdge(weightMatrix, nodeIndex1, nodeIndex2);
		double superWeight = weightMatrix.get(nodeIndex1, nodeIndex2);

		Histogram histogram1 = this.sumHistograms[nodeIndex1];
		Histogram histogram2 = this.sumHistograms[nodeIndex2];
		double chi2 = histogram1.computeChi2(histogram2);
		// compute new weight
		double newWeight = Math.exp(-chi2 * chi2);
		// if (newWeight < MathConstants.EPS) {
		// return;
		// }

		weightMatrix.set(nodeIndex1, nodeIndex2, newWeight);
		weightMatrix.set(nodeIndex2, nodeIndex1, newWeight);
	}

}
