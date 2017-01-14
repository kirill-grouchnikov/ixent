package org.jvnet.ixent.algorithms.graphics.engine;

import java.awt.image.BufferedImage;
import java.util.List;

import org.jvnet.ixent.algorithms.graphics.engine.linkinfo.WeightedWrapper;
import org.jvnet.ixent.algorithms.graphics.segmentation.SegmentationInfo;
import org.jvnet.ixent.algorithms.graphics.turbulence.DisplacementMatrix;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.Polygon2D;

/**
 * The base (abstract) class for all NPR engines. Stores all the information for
 * initializing various parts of the specific NPR engine
 *
 * @author Kirill Grouchnikov
 */
public abstract class NprEngineBase implements NprEngine {

    protected BufferedImage inputImage;
    protected int imageWidth;
    protected int imageHeight;

    protected WeightedWrapper<SegmentationInfo> segmentationLinkInfo;
    protected WeightedWrapper<IndexBitmapObject> edgeDetectionLinkInfo;
    protected WeightedWrapper<List<Polygon2D>> inputImageTesselationLinkInfo;
    protected WeightedWrapper<IndexBitmapObject> structureVicinityLinkInfo;
    protected WeightedWrapper<IndexBitmapObject> structureGradientLinkInfo;
    protected WeightedWrapper<DisplacementMatrix> displacementMapLinkInfo;

    /**
     * Default constructor
     */
    public NprEngineBase() {
    }

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
     * @throws IllegalArgumentException if one of the input parameters is null
     */
    public void init(BufferedImage pInputImage,
                     WeightedWrapper<SegmentationInfo> pSegmentationLinkInfo,
                     WeightedWrapper<IndexBitmapObject> pEdgeDetectionLinkInfo,
                     WeightedWrapper<List<Polygon2D>> pInputImageTesselationLinkInfo,
                     WeightedWrapper<IndexBitmapObject> pStructureVicinityLinkInfo,
                     WeightedWrapper<IndexBitmapObject> pStructureGradientLinkInfo,
                     WeightedWrapper<DisplacementMatrix> pDisplacementMapLinkInfo) {

        if ((pInputImage == null) || (pSegmentationLinkInfo == null) ||
                (pEdgeDetectionLinkInfo == null) ||
                (pInputImageTesselationLinkInfo == null) ||
                (pStructureVicinityLinkInfo == null) ||
                (pStructureGradientLinkInfo == null) ||
                (pDisplacementMapLinkInfo == null)) {
            throw new IllegalArgumentException("Can't pass null arguments");
        }

        this.inputImage = pInputImage;
        this.imageWidth = this.inputImage.getWidth();
        this.imageHeight = this.inputImage.getHeight();

        this.segmentationLinkInfo = pSegmentationLinkInfo;
        this.edgeDetectionLinkInfo = pEdgeDetectionLinkInfo;
        this.inputImageTesselationLinkInfo = pInputImageTesselationLinkInfo;
        this.structureVicinityLinkInfo = pStructureVicinityLinkInfo;
        this.structureGradientLinkInfo = pStructureGradientLinkInfo;
        this.displacementMapLinkInfo = pDisplacementMapLinkInfo;
    }


    /**
     * Perform single step of simulation. Is relevant only for dynamic NPR
     * engines (animation). Provides a default (empty) implementation.
     */
    public void step() {
    }
}
