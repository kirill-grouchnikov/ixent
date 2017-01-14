package org.jvnet.ixent.algorithms.graphics.turbulence;

import org.jvnet.ixent.math.MathConstants;

/**
 * Class holding information on displacement vector for a single point
 *
 * @author Kirill Grouchnikov
 */
public class DisplacementVector {
    /**
     * displacement value in X direction
     */
    private double dx;

    /**
     * displacement value in Y direction
     */
    private double dy;

    /**
     * displacement vector length
     */
    private double magnitude;

    /**
     * displacement vector direction in degrees
     */
    private double directionInDegrees;

    /**
     * Private constructor that enables access only through factory methods
     *
     * @param _dx                 displacement value in X direction
     * @param _dy                 displacement value in Y direction
     * @param _magnitude          displacement vector length
     * @param _directionInDegrees displacement vector direction in degrees
     */
    private DisplacementVector(double _dx, double _dy, double _magnitude,
                               double _directionInDegrees) {
        this.dx = _dx;
        this.dy = _dy;
        this.magnitude = _magnitude;
        this.directionInDegrees = _directionInDegrees;
    }

    /**
     * Static factory method. Creates displacement vector by its X and Y
     * components
     *
     * @param dx displacement value in X direction
     * @param dy displacement value in Y direction
     * @return new displacement vector
     */
    public static DisplacementVector getByCoordinates(double dx, double dy) {
        // compute polar coordinates
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        double directionInDegrees = 0.0;
        if (magnitude >= MathConstants.EPS) {
            double angle = Math.acos(dx / magnitude);
            if (dy < 0.0) {
                angle = 2.0 * Math.PI - angle;
            }

            directionInDegrees = 180.0 * angle / Math.PI;
        }
        return new DisplacementVector(dx, dy, magnitude, directionInDegrees);

    }

    /**
     * Static factory method. Creates displacement vector by its polar
     * components (length and direction in degrees)
     *
     * @param magnitude          displacement vector length
     * @param directionInDegrees displacement vector direction in degrees
     * @return new displacement vector
     */
    public static DisplacementVector getByDirection(double magnitude,
                                                    double directionInDegrees) {
        double angle = directionInDegrees * Math.PI / 180.0;
        double dx = magnitude * Math.cos(angle);
        double dy = magnitude * Math.sin(angle);

        return new DisplacementVector(dx, dy, magnitude, directionInDegrees);
    }

    /**
     * @return displacement value in X direction
     */
    public double getXComponent() {
        return dx;
    }

    /**
     * @return displacement value in Y direction
     */
    public double getYComponent() {
        return dy;
    }

    /**
     * @return displacement vector length
     */
    public double getMagnitude() {
        return magnitude;
    }

    /**
     * @return displacement vector direction in degrees
     */
    public double getDirectionInDegrees() {
        return directionInDegrees;
    }
}
