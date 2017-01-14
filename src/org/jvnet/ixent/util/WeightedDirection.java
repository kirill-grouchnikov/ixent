package org.jvnet.ixent.util;

import org.jvnet.ixent.math.MathConstants;

/**
 * @author Kirill Grouchnikov
 */
public class WeightedDirection {
    private double direction;
    private double weight;
    private boolean isWeightSet;

    public WeightedDirection() {
        this.weight = 0.0;
        this.direction = 0.0;
        this.isWeightSet = false;
    }

    /**
     * @return weighted average direction
     */
    public double getDirection() {
        return this.direction;
    }

    /**
     * Check if this weighted direction has any significant weight
     *
     * @return <code>true</code> if this weighted direction has any significant
     *         weight and <code>false</code> otherwise
     */
    public boolean hasWeight() {
        return (this.isWeightSet && (this.weight > 0.0));
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        return "direction " + this.direction + " (weight " + this.weight +
                ")";
    }

    /**
     * Get new average direction with the corresponding one
     *
     * @param newDirection new direction
     * @param newWeight    new direction weight
     * @return resulting average in 0..180 range
     */
    private double getNewAverage(double newDirection, double newWeight) {
        // get average
        double result = (this.weight * this.direction +
                newWeight * newDirection) /
                (this.weight + newWeight);

        // convert to 0..180 range
        while (result < 0.0) {
            result += 360.0;
        }
        while (result >= 180.0) {
            result -= 180.0;
        }
        return result;
    }

    /**
     * Gets angular distance between two angles in 0..180 range
     *
     * @param angle1 the first angle
     * @param angle2 the second angle
     * @return distance minimal distance between two-directed rays that "host"
     *         these angles. For example, for 10<sup>o</sup> and 170<sup>o</sup>
     *         the distance will be 20<sup>o</sup> and not 160<sup>o</sup>
     */
    private double getDistance(double angle1, double angle2) {
        double dist1 = Math.abs(angle1 - angle2);
        double dist2 = 180.0 - dist1;
        return Math.min(dist1, dist2);
    }

    /**
     * Incorporate new weighted direction into this object
     *
     * @param newDirection new direction
     * @param newWeight    new direction weight
     */
    public void incorporate(double newDirection, double newWeight) {
        // check for the initial case
        if (Math.abs(this.weight) < MathConstants.EPS) {
            this.direction = newDirection;
            this.weight = newWeight;
            this.isWeightSet = true;
            return;
        }

        // check three cases, for direction, for direction+180, for
        // direction-180
        double[] candidates = new double[]{
            this.getNewAverage(newDirection, newWeight),
            this.getNewAverage(newDirection + 180.0, newWeight),
            this.getNewAverage(newDirection - 180.0, newWeight)
        };
        double[] distances = new double[candidates.length];
        int count = 0;
        for (double candidate : candidates) {
            distances[count++] = getDistance(this.direction, candidate);
        }

        // find minimal distance
        int minIndex = 0;
        double minDistance = distances[0];
        for (int i = 1; i < distances.length; i++) {
            if (distances[i] < minDistance) {
                minDistance = distances[i];
                minIndex = i;
            }
        }

        this.direction = candidates[minIndex];
        this.weight += newWeight;
        this.isWeightSet = true;
    }
}
