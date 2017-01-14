package org.jvnet.ixent.algorithms.graphics.turbulence;

/**
 * A class for producing Perlin-inspired noise. The main idea is borrowed from
 * Ken Perlin's noise generator: for each point sum up noise functions at
 * different frequencies with different amplitudes (the higher the frequency the
 * lower the amplitude). However, due to cheap evaluation of a single noise
 * function we do not perform noise interpolation on 2D mesh. <br> <p/> A single
 * noise function is just a combination of few 2D trigonometric functions
 * combined with random numbers
 *
 * @author Kirill Grouchnikov
 */
public class PerlinNoiseGenerator {
    /**
     * Compute single-octave noise at given point.
     *
     * @param random random number (preferably in 0..1 range)
     * @param freq   frequency of this octave
     * @param x      x coordinate (preferably in 0..1 range to obtain smooth
     *               variations)
     * @param y      y coordinate (preferably in 0..1 range to obtain smooth
     *               variations)
     * @return noise at this point
     */
    private double noise(double random, int freq, double x, double y) {
        double r1 = Math.sin(freq * (x - random) + y + random);
        double r2 = Math.cos(freq * (y + random) + x - random);
        double r3 = Math.cos(x + freq * y + random / 2.0);
        double r4 = Math.sin(y + freq * x - random / 2.0);
        return (r1 + r2 + r3 + r4) / 4.0;
    }

    /**
     * Compute perlin noise at given point
     *
     * @param octaves number of octaves. The higher this number the less smooth
     *                the resulting noise will be. Must be non-negative to
     *                produce meaningful noise (that is a noise that isn't zero
     *                at all points)
     * @param x       x coordinate (preferably in 0..1 range to obtain smooth
     *                variations)
     * @param y       y coordinate (preferably in 0..1 range to obtain smooth
     *                variations)
     * @return noise at this point
     */
    private double perlinNoise2D(int octaves, double x, double y) {
        double total = 0;
        int n = octaves;

        int frequency = 1;
        for (int i = 0; i < n; i++) {
            total = total + this.noise(Math.random(), frequency, x, y);
            frequency *= 2.0;
        }
        return total;
    }

    /**
     * Compute normalized noise map for given dimensions (width and height)
     *
     * @param width  noise map width
     * @param height noise map height
     * @return noise map. Each entry is in 0..1 range
     */
    public double[][] getSparseNormalizedNoise(int width, int height) {
        double[][] res = new double[width][height];
        double r = Math.random();
        double minNoise = 0.0, maxNoise = 0.0;
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                double val = this.perlinNoise2D(4, r +
                        (double) col / (double) width,
                        r + (double) row / (double) height);
                if ((col == 0) && (row == 0)) {
                    minNoise = val;
                    maxNoise = val;
                }
                else {
                    if (val < minNoise) {
                        minNoise = val;
                    }
                    if (val > maxNoise) {
                        maxNoise = val;
                    }
                }
                res[col][row] = val;
            }
        }

        // normalize to 0..1 range
        double denom = 1.0 / (maxNoise - minNoise);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                res[i][j] = (res[i][j] - minNoise) * denom;
            }
        }

        return res;
    }

    /**
     * Compute normalized noise map for given dimensions (width and height)
     *
     * @param width  noise map width
     * @param height noise map height
     * @return noise map. Each entry is in 0..1 range
     */
    public double[][] getDenseNormalizedNoise(int width, int height) {
        double[][] res = new double[width][height];
        double r = Math.random()*0.4+0.4;
        double minNoise = 0.0, maxNoise = 0.0;
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                double val = this.perlinNoise2D(12, r*(col+r*row), r*(r*col-row));
                if ((col == 0) && (row == 0)) {
                    minNoise = val;
                    maxNoise = val;
                }
                else {
                    if (val < minNoise) {
                        minNoise = val;
                    }
                    if (val > maxNoise) {
                        maxNoise = val;
                    }
                }
                res[col][row] = val;
            }
        }

        // normalize to 0..1 range
        double denom = 1.0 / (maxNoise - minNoise);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                res[i][j] = (res[i][j] - minNoise) * denom;
            }
        }

        return res;
    }


}
