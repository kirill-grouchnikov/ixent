package org.jvnet.ixent.algorithms.graphics.segmentation;

import org.jvnet.ixent.graphics.IndexBitmapObject;

/**
 * @author Kirill Grouchnikov
 */
public class SegmentationInfo {
    private int numberOfSegments;

    private IndexBitmapObject areasBitmap;

    private IndexBitmapObject boundariesBitmap;

    public SegmentationInfo(int pNumberOfSegments,
                            IndexBitmapObject pSegmentationObject,
                            IndexBitmapObject pSegmentationBoundariesObject) {
        this.numberOfSegments = pNumberOfSegments;
        this.areasBitmap = pSegmentationObject;
        this.boundariesBitmap = pSegmentationBoundariesObject;
    }

    public int getNumberOfSegments() {
        return numberOfSegments;
    }

    public IndexBitmapObject getAreasBitmap() {
        return areasBitmap;
    }

    public IndexBitmapObject getBoundariesBitmap() {
        return boundariesBitmap;
    }
}
