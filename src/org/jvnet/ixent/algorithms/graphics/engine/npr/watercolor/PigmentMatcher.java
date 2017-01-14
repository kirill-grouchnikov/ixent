package org.jvnet.ixent.algorithms.graphics.engine.npr.watercolor;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import org.jvnet.ixent.algorithms.graphics.engine.linkinfo.WeightedWrapper;

/**
 * A class for matching given color with one or more (mix) pigments. The
 * pigments are matched based on the assumption that the paper medium is
 * perfectly reflectant white paper.
 *
 * @author Kirill Grouchnikov
 */
public class PigmentMatcher {

    private static final int STEPS = 15;

    /**
     * Computes the matching coefficient of a single pigment (on perfectly
     * reflectant white paper) of specified concentration to a specified color.
     * The smaller the resulting value, the better the match.
     *
     * @param r             red value of the color to match
     * @param g             green value of the color to match
     * @param b             blue value of the color to match
     * @param pigment       a pigment to check
     * @param concentration pigment's concentration
     * @return the matching coefficient of the specified pigment and the
     *         specified color. The smaller the value, the better the match.
     */
    private double getMatchingCoef(int r, int g, int b, Pigment pigment,
                                   double concentration) {
        double deltaR = concentration *
                pigment.getReflectance(Pigment.Component.red, 1.0) *
                pigment.getComponent(Pigment.Component.red) +
                (1.0 - concentration) * 255.0 - r;
        double deltaG = concentration *
                pigment.getReflectance(Pigment.Component.green, 1.0) *
                pigment.getComponent(Pigment.Component.green) +
                (1.0 - concentration) * 255.0 - g;
        double deltaB = concentration *
                pigment.getReflectance(Pigment.Component.blue, 1.0) *
                pigment.getComponent(Pigment.Component.blue) +
                (1.0 - concentration) * 255.0 - b;
        return (deltaR * deltaR + deltaG * deltaG + deltaB * deltaB);
    }

    /**
     * Computes the matching coefficient of two pigment (on perfectly reflectant
     * white paper) of specified concentrations to a specified color. The
     * smaller the resulting value, the better the match.
     *
     * @param r              red value of the color to match
     * @param g              green value of the color to match
     * @param b              blue value of the color to match
     * @param pigment1       the first pigment to check
     * @param concentration1 the first pigment's concentration
     * @param pigment2       the second pigment to check
     * @param concentration2 the second pigment's concentration
     * @return the matching coefficient of the specified pigments and the
     *         specified color. The smaller the value, the better the match.
     */
    private double getMatchingCoef(int r, int g, int b, Pigment pigment1,
                                   double concentration1, Pigment pigment2,
                                   double concentration2) {
        double deltaR = concentration1 *
                pigment1.getReflectance(Pigment.Component.red, 1.0) *
                pigment1.getComponent(Pigment.Component.red) +
                concentration2 *
                pigment2.getReflectance(Pigment.Component.red, 1.0) *
                pigment2.getComponent(Pigment.Component.red) +
                (1.0 - concentration1 - concentration2) * 255.0 - r;
        double deltaG = concentration1 *
                pigment1.getReflectance(Pigment.Component.green,
                        1.0) *
                pigment1.getComponent(Pigment.Component.green) +
                concentration2 *
                pigment2.getReflectance(Pigment.Component.green,
                        1.0) *
                pigment2.getComponent(Pigment.Component.green) +
                (1.0 - concentration1 - concentration2) * 255.0 - g;
        double deltaB = concentration1 *
                pigment1.getReflectance(Pigment.Component.blue, 1.0) *
                pigment1.getComponent(Pigment.Component.blue) +
                concentration2 *
                pigment2.getReflectance(Pigment.Component.blue, 1.0) *
                pigment2.getComponent(Pigment.Component.blue) +
                (1.0 - concentration1 - concentration2) * 255.0 - b;
        return (deltaR * deltaR + deltaG * deltaG + deltaB * deltaB);
    }


    /**
     * Returns a list (mix) of one or more pigments that best match given color.
     * Each pigment in the list has an associated weight (concentration).
     *
     * @param r the red component of the color to match
     * @param g the green component of the color to match
     * @param b the blue component of the color to match
     * @return a list (mix) of one or more pigments that best match given color.
     *         Each pigment in the list has an associated weight
     *         (concentration).
     */
    public List<WeightedWrapper<Pigment>> matchPigments(int r, int g, int b) {
        // for now look only for singles and pairs
        double bestMatchingCoef = 4 * 255 * 255;
        List<WeightedWrapper<Pigment>> bestMatching =
                new LinkedList<WeightedWrapper<Pigment>>();

        // check singles
        for (Pigment currPigment : Pigment.values()) {
            for (int step = 0; step <= STEPS; step++) {
                double concentration = (double) step / (double) STEPS;
                double currMatchingCoef = this.getMatchingCoef(r, g, b,
                        currPigment, concentration);
                if (currMatchingCoef < bestMatchingCoef) {
                    bestMatchingCoef = currMatchingCoef;
                    bestMatching.clear();
                    bestMatching.add(new WeightedWrapper<Pigment>(currPigment,
                            concentration));
                }
            }
        }

        // check doubles
        for (Pigment currPigment1 : Pigment.values()) {
            for (int step1 = 1; step1 <= STEPS; step1++) {
                double concentration1 = (double) step1 / (double) STEPS;
                for (Pigment currPigment2 : Pigment.values()) {
                    if (currPigment1 == currPigment2) {
                        continue;
                    }
                    for (int step2 = 1; step2 <= (STEPS - step1); step2++) {
                        double concentration2 = (double) step2 /
                                (double) STEPS;
                        double currMatchingCoef = this.getMatchingCoef(r, g, b,
                                currPigment1, concentration1, currPigment2,
                                concentration2);
                        if (currMatchingCoef < bestMatchingCoef) {
                            bestMatchingCoef = currMatchingCoef;
                            bestMatching.clear();
                            bestMatching.add(new WeightedWrapper<Pigment>(
                                    currPigment1,
                                    concentration1));
                            bestMatching.add(new WeightedWrapper<Pigment>(
                                    currPigment2,
                                    concentration2));
                        }
                    }
                }
            }
        }

        return bestMatching;
    }

    /**
     * Returns a list (mix) of one or more pigments that best match given color.
     * Each pigment in the list has an associated weight (concentration).
     *
     * @param color the color to match
     * @return a list (mix) of one or more pigments that best match given color.
     *         Each pigment in the list has an associated weight
     *         (concentration).
     */
    public List<WeightedWrapper<Pigment>> matchPigments(Color color) {
        return this.matchPigments(color.getRed(), color.getGreen(),
                color.getBlue());
    }

}
