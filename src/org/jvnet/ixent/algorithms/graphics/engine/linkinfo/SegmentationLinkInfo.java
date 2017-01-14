package org.jvnet.ixent.algorithms.graphics.engine.linkinfo;

/**
 * Class for holding information on segmentation module input link
 *
 * @author Kirill Grouchnikov
 */
public class SegmentationLinkInfo {
    /**
     * maximum number of segments that the segmentator should return
     */
    private int maxSegmentsAtLastLevel;

    /**
     * @param pMaxSegmentsAtLastLevel maximum number of segments that the
     *                                segmentator should return
     */
    public SegmentationLinkInfo(int pMaxSegmentsAtLastLevel) {
        this.maxSegmentsAtLastLevel = pMaxSegmentsAtLastLevel;
    }

    /**
     * @return maximum number of segments that the segmentator should return
     */
    public int getMaxSegmentsAtLastLevel() {
        return this.maxSegmentsAtLastLevel;
    }
}
