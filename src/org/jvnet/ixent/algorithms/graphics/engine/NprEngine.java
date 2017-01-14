package org.jvnet.ixent.algorithms.graphics.engine;

import java.awt.image.BufferedImage;
import java.util.List;

import org.jvnet.ixent.algorithms.graphics.engine.linkinfo.WeightedWrapper;
import org.jvnet.ixent.algorithms.graphics.segmentation.SegmentationInfo;
import org.jvnet.ixent.algorithms.graphics.turbulence.DisplacementMatrix;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.Polygon2D;

/**
 * The base (interface) class for all NPR engines
 *
 * @author Kirill Grouchnikov
 */
public interface NprEngine {
    /**
     * Initialize this engine with all the input information
     *
     * @param pInputImage                    input image
     * @param pSegmentationLinkInfo          information on image segmentation
     * @param pEdgeDetectionLinkInfo         information on edge detection
     * @param pInputImageTesselationLinkInfo input image tesselation (list of
     *                                       polygons)
     * @param pStructureVicinityLinkInfo     structure vicinity map
     * @param pStructureGradientLinkInfo     structure gradient map
     * @param pDisplacementMapLinkInfo       displacement map
     */
    public void init(BufferedImage pInputImage,
                     WeightedWrapper<SegmentationInfo> pSegmentationLinkInfo,
                     WeightedWrapper<IndexBitmapObject> pEdgeDetectionLinkInfo,
                     WeightedWrapper<List<Polygon2D>> pInputImageTesselationLinkInfo,
                     WeightedWrapper<IndexBitmapObject> pStructureVicinityLinkInfo,
                     WeightedWrapper<IndexBitmapObject> pStructureGradientLinkInfo,
                     WeightedWrapper<DisplacementMatrix> pDisplacementMapLinkInfo);

    /**
     * Perform single step of simulation. Is relevant only for dynamic NPR
     * engines (animation)
     */
    public void step();

    /**
     * Return resulting image
     *
     * @return the resulting image
     */
    public BufferedImage getResultingImage();
}
