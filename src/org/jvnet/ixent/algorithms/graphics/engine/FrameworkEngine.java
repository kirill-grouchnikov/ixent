package org.jvnet.ixent.algorithms.graphics.engine;

import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;

import org.jvnet.ixent.algorithms.graphics.edgedetection.EdgeDetector;
import org.jvnet.ixent.algorithms.graphics.edgedetection.EdgeDetectorFactory;
import org.jvnet.ixent.algorithms.graphics.engine.linkinfo.*;
import org.jvnet.ixent.algorithms.graphics.segmentation.*;
import org.jvnet.ixent.algorithms.graphics.tesselation.Tesselator;
import org.jvnet.ixent.algorithms.graphics.tesselation.TesselatorFactory;
import org.jvnet.ixent.algorithms.graphics.turbulence.*;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.Polygon2D;
import org.jvnet.ixent.util.ImageCreator;

/**
 * The framework engine for producing image feature based NPR effects
 *
 * @author Kirill Grouchnikov
 */
public class FrameworkEngine {
    public enum WeightKind {
        weightSegmentationForStructure,
        /**
         * influence of the segmentator on the NPR engine
         */
        weightSegmentationForNPR,
        weightEdgeDetectionForStructure,
        /**
         * influence of the edge detector on the NPR engine
         */
        weightEdgeDetectionForNPR,
        weightDisplacementForStructure,
        weightDisplacementForCorrection,
        weightDisplacementForNPR,
        /**
         * influence of the tesselator on the NPR engine
         */
        weightTesselationForNPR,
        weightStructureVicinityForNPR,
        weightStructureGradientForNPR,
        weightStructureGradientForCorrection;
    };

    private Map<WeightKind, Double> weightMap;

    private BufferedImage inputImage;

    private SegmentationLinkInfo segmentationLinkInfo;
    private EdgeDetectionLinkInfo edgeDetectionLinkInfo;
    private DisplacementLinkInfo displacementLinkInfo;
    private TesselationLinkInfo tesselationLinkInfo;
    private NprLinkInfo nprLinkInfo;

    private NprEngine nprEngine;

    private Logger logger;

    /**
     * Constructor to initialize the framework engine
     *
     * @param image input image
     * @throws IllegalArgumentException if the input image is null
     */
    public FrameworkEngine(BufferedImage image) {
        this.inputImage = image;
        this.weightMap = new HashMap<WeightKind, Double>();
        this.logger =
                Logger.getLogger(FrameworkEngine.class.getPackage().getName());
    }

    /**
     * Set parameters of various internal components of the engine
     *
     * @param pSegmentationLinkInfo  instruction set for the segmentator
     * @param pEdgeDetectionLinkInfo instruction set for the edge detector
     * @param pDisplacementLinkInfo  instruction set for the turbulence
     *                               generator
     * @param pTesselationLinkInfo   instruction set for the tesselator
     * @param pNprLinkInfo           identification of the NPR engine instance
     * @throws IllegalArgumentException if one of the inputs is null
     */
    public void setParameters(SegmentationLinkInfo pSegmentationLinkInfo,
                              EdgeDetectionLinkInfo pEdgeDetectionLinkInfo,
                              DisplacementLinkInfo pDisplacementLinkInfo,
                              TesselationLinkInfo pTesselationLinkInfo,
                              NprLinkInfo pNprLinkInfo) {
        if ((pSegmentationLinkInfo == null) ||
                (pEdgeDetectionLinkInfo == null) ||
                (pDisplacementLinkInfo == null) ||
                (pTesselationLinkInfo == null) ||
                (pNprLinkInfo == null)) {
            throw new IllegalArgumentException("Can't pass null arguments");
        }

        this.segmentationLinkInfo = pSegmentationLinkInfo;
        this.edgeDetectionLinkInfo = pEdgeDetectionLinkInfo;
        this.displacementLinkInfo = pDisplacementLinkInfo;
        this.tesselationLinkInfo = pTesselationLinkInfo;
        this.nprLinkInfo = pNprLinkInfo;
        this.nprEngine = null;
    }

    /**
     * Set weight for a specific arrow in the engine flowchart
     *
     * @param kind  arrow kind
     * @param value value in 0..1 range
     * @throws IllegalArgumentException if the input weight is not in 0..1
     *                                  range
     */
    public void setWeight(WeightKind kind, double value) {
        if ((value < 0.0) || (value > 1.0)) {
            throw new IllegalArgumentException("Invalid value for '" +
                    kind.name() +
                    "' : " +
                    value +
                    " (must be in 0..1 range)");
        }
        this.weightMap.put(kind, value);
    }

    /**
     * Return weight for a specific arrow in the engine flowchart
     *
     * @param kind arrow kind
     * @return its weight
     */
    public double getWeight(WeightKind kind) {
        if (this.weightMap.containsKey(kind)) {
            return this.weightMap.get(kind);
        }
        else {
            return 0.0;
        }
    }

    /**
     * Checks if a specific arrow in the engine flowchart has an associated
     * positive weight
     *
     * @param kind arrow kind
     * @return <code>true</code> if it has an associated weight and it is
     *         positive, <code>false</code> otherwise
     */
    public boolean hasPositiveWeight(WeightKind kind) {
        if (!this.weightMap.containsKey(kind)) {
            return false;
        }
        return (this.weightMap.get(kind) > 0.0);
    }

    /**
     * Perform the initialization of all blocks of the framework
     */
    public void init() {
        // compute all necessary image-based information
        IndexBitmapObject edgeDetectionObject = null;
        if (this.hasPositiveWeight(WeightKind.weightEdgeDetectionForStructure) ||
                this.hasPositiveWeight(WeightKind.weightEdgeDetectionForNPR)) {
            this.logger.info("Computing edges");
            EdgeDetector edgeDetector = EdgeDetectorFactory.getEdgeDetector(
                    this.inputImage);
            edgeDetectionObject = edgeDetector.getValueMap2D(
                    this.edgeDetectionLinkInfo.getFuzzyness(),
                    this.edgeDetectionLinkInfo.getStrength());

            ImageCreator.createWithEdges(this.inputImage, edgeDetectionObject);
        }

        SegmentationInfo segmentationObject = null;
        if (this.hasPositiveWeight(WeightKind.weightSegmentationForStructure) ||
                this.hasPositiveWeight(WeightKind.weightSegmentationForNPR)) {
            this.logger.info("Computing segmentation");
            Segmentator segmentator = SegmentatorFactory.getSegmentator(
                    this.inputImage);
            segmentator.process(
                    this.segmentationLinkInfo.getMaxSegmentsAtLastLevel());
            segmentationObject = segmentator.getSegmentationInfo();
            assert segmentationObject.getNumberOfSegments() <=
                    this.segmentationLinkInfo.getMaxSegmentsAtLastLevel() :
                    "Too many segments computed (" +
                    segmentationObject.getNumberOfSegments() + " instead of " +
                    this.segmentationLinkInfo.getMaxSegmentsAtLastLevel() +
                    ")";

            ImageCreator.createWithSegments(this.inputImage,
                    segmentationObject.getBoundariesBitmap());
//            if (true) {
//                return ImageCreator.createWithSegments(this.inputImage,
//                        segmentationObject);
//            }
        }

        DisplacementMatrix displacementMatrix = null;
        if (this.hasPositiveWeight(WeightKind.weightDisplacementForStructure) ||
                this.hasPositiveWeight(WeightKind.weightDisplacementForNPR)) {
            this.logger.info("Computing displacements");
            TurbulenceGenerator turbulenceGenerator =
                    TurbulenceGeneratorFactory.getDisplacer(
                            this.inputImage.getWidth(),
                            this.inputImage.getHeight(),
                            this.displacementLinkInfo.getMaximalDisplacement(),
                            this.displacementLinkInfo.getDirectionInDegrees(),
                            this.displacementLinkInfo.getSectorInDegrees());
            displacementMatrix = turbulenceGenerator.getDisplacementMap();
        }

        this.logger.info("Invoking structure engine");
        StructureEngine structureEngine = new StructureEngine(
                this.inputImage.getWidth(), this.inputImage.getHeight(),
                new WeightedWrapper<SegmentationInfo>(segmentationObject,
                        this.getWeight(
                                WeightKind.weightSegmentationForStructure)),
                new WeightedWrapper<IndexBitmapObject>(edgeDetectionObject,
                        this.getWeight(
                                WeightKind.weightEdgeDetectionForStructure)),
                new WeightedWrapper<DisplacementMatrix>(displacementMatrix,
                        this.getWeight(
                                WeightKind.weightDisplacementForStructure)));
        structureEngine.process();

        IndexBitmapObject structureVicinityMap = structureEngine.getVicinityMap();
        IndexBitmapObject structureGradientMap = structureEngine.getGradientMap();
        
        ImageCreator.createWithGradient(this.inputImage, structureGradientMap);

        this.logger.info("Computing tesselation");
        Tesselator tesselator = TesselatorFactory.getTesselator();
        List<Polygon2D> tesselationPolygons = tesselator.getTesselation(
                this.tesselationLinkInfo.getCellRadius(),
                structureVicinityMap);

        ImageCreator.createWithPolygons(tesselationPolygons);

        DisplacementMatrix displacementForNPR = null;
        if (this.hasPositiveWeight(WeightKind.weightDisplacementForCorrection)) {
            this.logger.info("Computing corrected displacements");

            TurbulenceCorrection displacementCorrector = new TurbulenceCorrection();
            displacementForNPR =
                    displacementCorrector.correct(displacementMatrix,
                            structureGradientMap,
                            this.getWeight(
                                    WeightKind.weightDisplacementForCorrection));
        }
        else {
            displacementForNPR = displacementMatrix;
        }

        Class nprEngineClass = this.nprLinkInfo.getNprEngineClass();
        this.logger.info(
                "Creating NPR engine (" + nprEngineClass.getName() + ")");
        Constructor nprEngineConstructor = null;
        try {
            nprEngineConstructor = nprEngineClass.getConstructor();
        }
        catch (NoSuchMethodException nsme) {
            this.logger.warning("Exception in fetching the constructor");
            nsme.printStackTrace();
            return;
        }

        try {
            this.nprEngine = (NprEngine) nprEngineConstructor.newInstance();
        }
        catch (InstantiationException ie) {
            this.logger.warning("Exception in calling the constructor");
            ie.printStackTrace();
            return;
        }
        catch (IllegalAccessException iae) {
            this.logger.warning("Exception in calling the constructor");
            iae.printStackTrace();
            return;
        }
        catch (InvocationTargetException ite) {
            this.logger.warning("Exception in calling the constructor");
            ite.printStackTrace();
            return;
        }

        this.logger.info("Invoking NPR engine");
        this.nprEngine.init(this.inputImage,
                new WeightedWrapper<SegmentationInfo>(segmentationObject,
                        this.getWeight(WeightKind.weightSegmentationForNPR)),
                new WeightedWrapper<IndexBitmapObject>(edgeDetectionObject,
                        this.getWeight(WeightKind.weightEdgeDetectionForNPR)),
                new WeightedWrapper<List<Polygon2D>>(tesselationPolygons,
                        this.getWeight(WeightKind.weightTesselationForNPR)),
                new WeightedWrapper<IndexBitmapObject>(structureVicinityMap,
                        this.getWeight(
                                WeightKind.weightStructureVicinityForNPR)),
                new WeightedWrapper<IndexBitmapObject>(structureGradientMap,
                        this.getWeight(
                                WeightKind.weightStructureGradientForNPR)),
                new WeightedWrapper<DisplacementMatrix>(displacementForNPR,
                        this.getWeight(WeightKind.weightDisplacementForNPR)));
    }

    /**
     * Run the flowchart and produce a resulting image
     *
     * @return the resulting image
     */
    public BufferedImage process() {
        if (this.nprEngine == null) {
            return null;
        }

        this.nprEngine.step();
        BufferedImage result = this.nprEngine.getResultingImage();
        ImageCreator.paintProgress(result);
        return result;
    }
}
