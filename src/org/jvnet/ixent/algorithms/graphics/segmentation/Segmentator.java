package org.jvnet.ixent.algorithms.graphics.segmentation;

import java.awt.image.BufferedImage;

import org.jvnet.ixent.graphics.IndexBitmapObject;

/**
 * @author Kirill Grouchnikov
 */
public interface Segmentator {
    /**
     * Initialize with Java image object
     *
     * @param image input image
     */
    public void init(BufferedImage image);

    /**
     * Computes image segmentation
     *
     * @param segmentsAtLastLevel at most this number of segments will be
     *                            computed at the last level of the algorithm
     */
    public void process(int segmentsAtLastLevel);

    /**
     * Returns all available information about image segmentation.
     *
     * @return all available information about image segmentation
     */
    public SegmentationInfo getSegmentationInfo();

    /**
     * Returns image segmentation as a 2-D array of indexes. Each pixel is
     * assigned a unique ID of its segment
     *
     * @return two-dimensional array. Each pixel is assigned an index. Pixels
     *         belonging to the same segment have the same indexes.
     */
    public IndexBitmapObject getSegmentationAreas();

    /**
     * Returns image segmentation as a 2-D array of indexes. Each pixel is
     * assigned a probability of belonging to segment boundary
     *
     * @return Each entry will contain a value from 0 to 255. 0 - no segment
     *         boundary at this pixel, 255 - certain segment boundary at this
     *         pixel
     */
    public IndexBitmapObject getSegmentationBoundaries();
}
