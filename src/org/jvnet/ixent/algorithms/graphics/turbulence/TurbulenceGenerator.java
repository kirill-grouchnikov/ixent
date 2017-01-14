package org.jvnet.ixent.algorithms.graphics.turbulence;


/**
 * An interface for turbulence generators
 *
 * @author Kirill Grouchnikov
 */

public interface TurbulenceGenerator {
    /**
     * Initialize the turbulence generator with direction of wind in degrees (as
     * in primary school)
     *
     * @param width              image width
     * @param height             image height
     * @param maxStrength        maximum displacement
     * @param directionInDegrees displacement direction
     * @param sectorInDegrees    direction "fuzzyness" - sector that will hold
     *                           all vectors. For example, specifying 20 means
     *                           that all the vectors will lie around the main
     *                           direction in -10<sup>0</sup>..10<sup>0</sup>
     *                           sector
     */
    public void init(int width, int height,
                     double maxStrength, double directionInDegrees,
                     double sectorInDegrees);

    /**
     * Return displacement map
     *
     * @return 2-D array of displacement vectors
     */
    public DisplacementMatrix getDisplacementMap();
}
