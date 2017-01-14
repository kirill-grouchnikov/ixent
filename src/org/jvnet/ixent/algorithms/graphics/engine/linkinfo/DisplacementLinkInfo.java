package org.jvnet.ixent.algorithms.graphics.engine.linkinfo;

/**
 * Class for holding information on displacement module input link.
 *
 * @author Kirill Grouchnikov
 */
public class DisplacementLinkInfo {
    /**
     * maximal magnitude of displacement
     */
    private double maximalDisplacement;

    /**
     * main direction of displacement
     */
    private double directionInDegrees;

    /**
     * specifies the sector in which all vectors will lie (half to the left and
     * half to the right on average). For example, specifying 20 means that all
     * the vectors will lie around the main direction in
     * -10<sup>0</sup>..10<sup>0</sup> sector
     */
    private double sectorInDegrees;

    /**
     * @param pMaximalDisplacement maximal magnitude of displacement
     * @param pDirectionInDegrees  main direction of displacement
     * @param pSectorInDegrees     the sector in which all vectors will lie
     *                             (half to the left and half to the right on
     *                             average). For example, specifying 20 means
     *                             that all the vectors will lie around the main
     *                             direction in -10<sup>0</sup>..10<sup>0</sup>
     *                             sector
     */
    public DisplacementLinkInfo(double pMaximalDisplacement,
                                double pDirectionInDegrees,
                                double pSectorInDegrees) {
        this.maximalDisplacement = pMaximalDisplacement;
        this.directionInDegrees = pDirectionInDegrees;
        this.sectorInDegrees = pSectorInDegrees;
    }

    /**
     * @return maximal magnitude of displacement
     */
    public double getMaximalDisplacement() {
        return maximalDisplacement;
    }

    /**
     * @return main direction of displacement
     */
    public double getDirectionInDegrees() {
        return directionInDegrees;
    }

    /**
     * @return sector in which all vectors will lie
     */
    public double getSectorInDegrees() {
        return sectorInDegrees;
    }
}
