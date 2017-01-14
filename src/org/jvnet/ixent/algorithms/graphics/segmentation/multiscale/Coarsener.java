package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale;

import java.awt.image.BufferedImage;

import org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure.GraphInfo;

/**
 * The root of hierarchy of all coarseners for multi-scale segmentation using
 * AMG approach
 * 
 * @author Kirill Grouchnikov
 */
public abstract class Coarsener {
	protected BufferedImage originalImage;
	protected GraphInfo prevGraphInfo;

	public Coarsener() {
	}

	/**
	 * @param _prevGraphInfo
	 *            the graph at previous stage
	 */
	public void init(BufferedImage _originalImage, GraphInfo _prevGraphInfo) {
		this.originalImage = _originalImage;
		this.prevGraphInfo = _prevGraphInfo;
	}

	/**
	 * @return the graph at next stage
	 */
	public abstract GraphInfo getNextGraph(int origSize);

	/**
	 * Fill additional information (such as brightness for example). Will be
	 * called once for the initial graph
	 */
	public abstract void fillAdditionalInformation();
}
