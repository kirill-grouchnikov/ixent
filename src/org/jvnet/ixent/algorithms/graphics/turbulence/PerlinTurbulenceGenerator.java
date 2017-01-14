package org.jvnet.ixent.algorithms.graphics.turbulence;


/**
 * A class for producing Perlin-inspired turbulence. The main idea is borrowed
 * from Ken Perlin's noise generator: for each point sum up noise functions at
 * different frequencies with different amplitudes (the higher the frequency the
 * lower the amplitude). However, due to cheap evaluation of a single noise
 * function we do not perform noise interpolation on 2D mesh. <br> <p/> A single
 * noise function is just a combination of few 2D trigonometric functions
 * combined with random numbers
 *
 * @author Kirill Grouchnikov
 */
public class PerlinTurbulenceGenerator implements TurbulenceGenerator {
    private boolean isInitialized;

    private DisplacementMatrix displacementMap;

    /**
     * Default constructor.
     */
    public PerlinTurbulenceGenerator() {
        this.isInitialized = false;
    }

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
    public void init(int width, int height, double maxStrength,
                     double directionInDegrees, double sectorInDegrees) {
        // allocate displacement map
        this.displacementMap = new DisplacementMatrix(width, height);
        // compute noise map - displacement magnitudes (in 0..1 range)
        PerlinNoiseGenerator noiseGenerator = new PerlinNoiseGenerator();
        double[][] noiseMap = noiseGenerator.getSparseNormalizedNoise(width, height);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                double currMagnitude = noiseMap[col][row] * maxStrength;
                double currDirection = directionInDegrees +
                        sectorInDegrees * Math.random() -
                        sectorInDegrees / 2.0;
                this.displacementMap.setVectorAt(col, row,
                        DisplacementVector.getByDirection(currMagnitude,
                                currDirection));
            }
        }

        this.isInitialized = true;
    }

    /**
     * Return displacement map
     *
     * @return 2-D array of displacement vectors
     * @throws IllegalStateException is thrown if <b>init()</b> wasn't called
     */
    public DisplacementMatrix getDisplacementMap() {
        if (!this.isInitialized) {
            throw new IllegalStateException(
                    "Object not initialized. Please call init() first");
        }

        return this.displacementMap;
    }
}
