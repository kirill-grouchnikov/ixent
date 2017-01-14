package org.jvnet.ixent.math.leastsquares;

import java.awt.geom.Point2D;

import org.jvnet.ixent.math.MathConstants;

/**
 * The class for weighted least squares fitting. Performs linear least squares
 * fitting to a collection of 2D points. Each point has an associated weight.
 * <br> <p/> Finds <b>a</b> and <b>b</b> such that function f(x)=a+bx minimizes
 * the sum SUM[w<sub>i</sub>*(y<sub>i</sub>-f(x<sub>i</sub>))]<sup>2</sup> where
 * <b>i</b> iterates over the input points
 *
 * @author Kirill Grouchnikov
 */
public class WeightedFitting {
    private double a;
    private double b;
    private boolean hasFit;
    private boolean isFitVertical;

    /**
     * @param points  input points
     * @param weights input point weights
     */
    public WeightedFitting(Point2D[] points, double[] weights) {
        this.hasFit = false;

        if ((points == null) || (weights == null)) {
            return;
        }

        assert (points.length == weights.length) : "arrays are of different size";

        // compute sums
        double sw = 0.0;
        double swx = 0.0;
        double swy = 0.0;
        double swxy = 0.0;
        double swxx = 0.0;
        int count = points.length;
        for (int i = 0; i < count; i++) {
            double w = weights[i];
            double x = points[i].getX();
            double y = points[i].getY();
            sw += w;
            swx += (w * x);
            swy += (w * y);
            swxy += (w * x * y);
            swxx += (w * x * x);
        }
        double denom = sw * swxx - swx * swx;
        if (Math.abs(denom) < MathConstants.EPS) {
            this.isFitVertical = true;
        }
        else {
            this.a = (swy * swxx - swx * swxy) / denom;
            this.b = (sw * swxy - swx * swy) / denom;
            this.isFitVertical = false;
        }
        this.hasFit = true;
    }

    /**
     * @return free coefficient of fit
     */
    public double getA() {
        return a;
    }

    /**
     * @return linear coefficient of fit
     */
    public double getB() {
        return b;
    }

    /**
     * @return true if the fit has been computed
     */
    public boolean isHasFit() {
        return hasFit;
    }

    /**
     * Return the inclination of the fit line in degrees
     *
     * @return inclination of the fit line in degrees
     * @throws IllegalStateException is thrown if no fit is available
     */
    public double getInclinationAngleInDegrees() {
        if (!this.hasFit) {
            throw new IllegalStateException(
                    "No fit for this input setLocation");
        }

        if (this.isFitVertical) {
            return 90.0;
        }

        double dx = 1.0;
        double dy = -this.b;

        double direction = Math.atan2(dy, dx);
        // This direction is in -pi..pi range. We need to convert
        // it to 0..180 range
        direction *= (180.0 / Math.PI);
        while (direction < 0.0) {
            direction += 360.0;
        }
        while (direction > 180.0) {
            direction -= 180.0;
        }
        return direction;
    }
}
