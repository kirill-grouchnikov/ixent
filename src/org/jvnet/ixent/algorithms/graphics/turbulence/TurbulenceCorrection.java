package org.jvnet.ixent.algorithms.graphics.turbulence;

import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.util.WeightedDirection;

/**
 * Correction module for turbulence generator-produced displacement map. Each
 * vector in the displacement map is corrected using a structure gradient map
 * obtained from <a href="../engine/StructureEngine.html">StructureEngine</a>
 * object.
 *
 * @author Kirill Grouchnikov
 */
public class TurbulenceCorrection {
    /**
     * Correct the given displacement map using structure gradient map. The
     * magnitude of displacement remains unchanged, the direction is adjusted
     * according to gradient at each pixel of the image. The input displacement
     * map is left unchanged
     *
     * @param initialVectors        input displacement map
     * @param structureGradientMap  structure gradient map
     * @param correctionCoefficient value in 0..1 range. The closer to 1, the
     *                              more impact the gradient has.
     * @return new displacement map
     * @throws IllegalArgumentException is thrown if one of the input parameters
     *                                  is null, dimensions of input parameters
     *                                  don't match, one of the entries in input
     *                                  displacement map is null or the
     *                                  coefficient is not in 0..1 range
     */
    public DisplacementMatrix correct(DisplacementMatrix initialVectors,
                                      IndexBitmapObject structureGradientMap,
                                      double correctionCoefficient) {
        // check input
        if ((initialVectors == null) || (structureGradientMap == null)) {
            throw new IllegalArgumentException("Can't pass null parameters");
        }

        if ((initialVectors.getWidth() != structureGradientMap.getWidth()) ||
                (initialVectors.getHeight() !=
                structureGradientMap.getHeight())) {
            throw new IllegalArgumentException("Dimensions don't match");
        }

        if ((correctionCoefficient < 0.0) || (correctionCoefficient > 1.0)) {
            throw new IllegalArgumentException("Invalid coefficient " +
                    correctionCoefficient +
                    " (must be in 0..1 range)");
        }

        int width = initialVectors.getWidth();
        int height = initialVectors.getHeight();
        DisplacementMatrix result = new DisplacementMatrix(width, height);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                DisplacementVector currVector =
                        initialVectors.getVectorAt(col, row);
                if (currVector == null) {
                    throw new IllegalArgumentException(
                            "Null entry at [" + col + ", " + row + "]");
                }
                WeightedDirection wd = new WeightedDirection();
                wd.incorporate(currVector.getDirectionInDegrees(),
                        1.0 - correctionCoefficient);
                wd.incorporate(structureGradientMap.getValue(col, row),
                        correctionCoefficient);
                double newDirection = wd.getDirection();
                result.setVectorAt(col, row, DisplacementVector.getByDirection(
                        currVector.getMagnitude(), newDirection));

            }
        }
        return result;
    }
}
