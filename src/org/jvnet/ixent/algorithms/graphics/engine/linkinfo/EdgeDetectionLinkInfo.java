package org.jvnet.ixent.algorithms.graphics.engine.linkinfo;

import org.jvnet.ixent.algorithms.graphics.edgedetection.EdgeDetector;

/**
 * Class for holding information on edge detection module input link.
 *
 * @author Kirill Grouchnikov
 */
public class EdgeDetectionLinkInfo {
    /**
     * fuzzyness of edge detection module
     */
    private EdgeDetector.EdgeFuzzyness fuzzyness;

    /**
     * strength of edge detection module
     */
    private EdgeDetector.EdgeStrength strength;

    /**
     * @param pFuzzyness fuzzyness of edge detection module
     * @param pStrength  strength of edge detection module
     */
    public EdgeDetectionLinkInfo(EdgeDetector.EdgeFuzzyness pFuzzyness,
                                 EdgeDetector.EdgeStrength pStrength) {
        this.fuzzyness = pFuzzyness;
        this.strength = pStrength;
    }

    /**
     * @return fuzzyness of edge detection module
     */
    public EdgeDetector.EdgeFuzzyness getFuzzyness() {
        return fuzzyness;
    }

    /**
     * @return strength of edge detection module
     */
    public EdgeDetector.EdgeStrength getStrength() {
        return strength;
    }
}
