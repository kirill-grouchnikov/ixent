package org.jvnet.ixent.math.coord;

/**
 * N-dimensional point (a point in R<sup>N</sup>)
 *
 * @author Kirill Grouchnikov
 */
public final class PointND {
    private double[] comps;

    private int dimensionality;

    /**
     * @param mComps component array (non-null and not empty)
     * @throws IllegalArgumentException if initializer array is null or empty
     */
    public PointND(double[] mComps) {
        if (mComps == null) {
            throw new IllegalArgumentException("Null initializer array");
        }
        if (mComps.length == 0) {
            throw new IllegalArgumentException("Empty initializer array");
        }
        this.comps = mComps;
        this.dimensionality = this.comps.length;
    }

    /**
     * @return point dimensionality
     */
    public int getDimensionality() {
        return this.dimensionality;
    }

    /**
     * Set n'th component of point to specified value
     *
     * @param index component index
     * @param value new value
     */
    public void setComponent(int index, double value) {
        this.comps[index] = value;
    }

    /**
     * Get n'th component of point
     *
     * @param index component index
     * @return corresponding value
     */
    public double getComponent(int index) {
        return this.comps[index];
    }

    /**
     * Computes squared distance to another point
     *
     * @param point2 the second point
     * @return squared distance between two points
     * @throws IllegalArgumentException if the two points are of different
     *                                  dimensionalities
     */
    public double dist2(PointND point2) {
        if (this.dimensionality != point2.dimensionality) {
            throw new IllegalArgumentException(
                    "Points are of different dimensionalities");
        }

        double sum2 = 0.0;
        for (int i = 0; i < this.dimensionality; i++) {
            double diff = this.comps[i] - point2.comps[i];
            sum2 += (diff * diff);
        }

        return sum2;
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        String res = "[";
        for (int i = 0; i < this.dimensionality; i++) {
            res += this.comps[i];
            if (i != (this.dimensionality - 1)) {
                res += ", ";
            }
        }
        res += "]";
        return res;
    }
}

