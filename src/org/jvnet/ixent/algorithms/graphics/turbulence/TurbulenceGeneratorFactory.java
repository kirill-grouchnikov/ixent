package org.jvnet.ixent.algorithms.graphics.turbulence;


/**
 * Factory for turbulence generators
 *
 * @author Kirill Grouchnikov
 */
public class TurbulenceGeneratorFactory {
	public static TurbulenceGenerator instance;
    /**
     * Return turbulence generator
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
     * @return turbulence generator
     */
    public static TurbulenceGenerator getDisplacer(int width, int height,
                                                   double maxStrength, double directionInDegrees,
                                                   double sectorInDegrees) {

        // create Perlin turbulenceGenerator
        // initialize it
        instance.init(width, height, maxStrength, directionInDegrees,
                sectorInDegrees);
        return instance;
    }
}
