package org.jvnet.ixent.algorithms.graphics.edgedetection;

import java.awt.image.BufferedImage;

/**
 * Factory for edge detectors
 * 
 * @author Kirill Grouchnikov
 */
public class EdgeDetectorFactory {
	private static EdgeDetector instance;

	public static void setInstance(EdgeDetector instance) {
		EdgeDetectorFactory.instance = instance;
	}

	/**
	 * Method for retrieving edge detector
	 * 
	 * @param inputImage
	 *            input image
	 * @return edge detector
	 */
	public static EdgeDetector getEdgeDetector(BufferedImage inputImage) {
		instance.init(inputImage);
		return instance;
	}
}
