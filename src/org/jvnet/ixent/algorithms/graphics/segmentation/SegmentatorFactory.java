package org.jvnet.ixent.algorithms.graphics.segmentation;

import java.awt.image.BufferedImage;

/**
 * @author Kirill Grouchnikov
 */
public class SegmentatorFactory {
	public static Segmentator instance;
	
	/**
	 * Return segmentator for true color bitmap
	 * 
	 * @param bitmapObject
	 *            input bitmap
	 * @return segmentator segmentator
	 */
	public static Segmentator getSegmentator(BufferedImage bitmapObject) {
		instance.init(bitmapObject);
		return instance;
	}

}
