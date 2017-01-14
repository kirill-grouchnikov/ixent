package org.jvnet.ixent.algorithms.graphics.edgedetection;

import java.awt.image.BufferedImage;

import org.jvnet.ixent.graphics.IndexBitmapObject;

/**
 * An interface for edge detectors
 * 
 * @author Kirill Grouchnikov
 */
public interface EdgeDetector {
	/**
	 * Enum for edge fuzzyness
	 */
	public enum EdgeFuzzyness {
		fuzzy, exact
	}

	/**
	 * Enum for edge strength
	 */
	public enum EdgeStrength {
		soft, medium, strong, veryStrong
	}

	/**
	 * Initialize with Java image object
	 * 
	 * @param image
	 *            input image
	 */
	public void init(BufferedImage image);

	/**
	 * Initialize with indexed bitmap object
	 * 
	 * @param indexObject
	 *            input image
	 */
	public void init(IndexBitmapObject indexObject);

	/**
	 * Return edge map.
	 * 
	 * @param fuzzyness
	 *            edge fuzzyness (soft or exact)
	 * @param strength
	 *            edge strength (soft - many edges, very string - few edges)
	 * @return 2-D array of values. The value at each pixel specifies the
	 *         probability of an edge passing through this point
	 */
	public IndexBitmapObject getValueMap2D(EdgeFuzzyness fuzzyness,
			EdgeStrength strength);
}
