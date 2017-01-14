package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure;

/**
 * Information on general histogram
 *
 * @author Kirill Grouchnikov
 */
public class Histogram {
    private int binCount;
    private double[] bins;

    /**
     * Constructor with empty bins
     *
     * @param _binCount bin count
     */
    public Histogram(int _binCount) {
        this.binCount = _binCount;
        this.bins = new double[this.binCount];
        for (int i = 0; i < this.binCount; i++) {
            this.bins[i] = 0.0;
        }
    }

    /**
     * Constructor with given bins
     *
     * @param _binCount bin count
     * @param _bins     bin sizes
     */
    public Histogram(int _binCount, double[] _bins) {
        this.binCount = _binCount;
        this.bins = _bins;
    }

    /**
     * Combine two given histograms together to produce new histogram
     *
     * @param histo1 first histogram
     * @param histo2 second histogram
     * @return sum of histograms
     */
    public static Histogram combine(Histogram histo1, Histogram histo2) {
        assert histo1.binCount == histo2.binCount : "Histograms are of different sizes";
        double[] newBins = new double[histo1.binCount];
        for (int i = 0; i < histo1.binCount; i++) {
            newBins[i] = histo1.bins[i] + histo2.bins[i];
        }
        return new Histogram(histo1.binCount, newBins);
    }

    /**
     * Combine this histogram with another one
     *
     * @param histo2 second histogram
     */
    public void combine(Histogram histo2) {
        assert this.binCount == histo2.binCount : "Histograms are of different sizes";
        for (int i = 0; i < this.binCount; i++) {
            this.bins[i] += histo2.bins[i];
        }
    }

    /**
     * Normalize this histogram - if contains at least one entry, the sum of all
     * bins in the resulting histogram will be 1.0
     */
    public void normalize() {
        double sum = 0.0;
        for (double curr : this.bins) {
            sum += curr;
        }

        if (sum == 0.0) {
            return;
        }

        for (int i = 0; i < this.binCount; i++) {
            this.bins[i] /= sum;
        }
    }

    /**
     * Get size of single bin
     *
     * @param index bin index
     * @return bin size
     */
    public double getEntry(int index) {
        return this.bins[index];
    }

    /**
     * Set size of single bin
     *
     * @param index bin index
     * @param size  new bin size
     */
    public void setBin(int index, double size) {
        this.bins[index] = size;
    }

    /**
     * @return bin count
     */
    public int getBinCount() {
        return this.binCount;
    }

    /**
     * Compute chi-2 metric
     *
     * @param histo2 second histogram
     * @return chi-2 metric
     */
    public double computeChi2(Histogram histo2) {
        assert this.binCount == histo2.binCount : "Histograms are of different sizes";
        double chi2 = 0.0;
        for (int i = 0; i < this.binCount; i++) {
            double hi = this.bins[i];
            double hj = histo2.bins[i];
            if ((hi == 0.0) && (hj == 0.0)) {
                continue;
            }
            double toAdd = (hi - hj) * (hi - hj) / (hi + hj);
            chi2 += toAdd;
        }
        return 0.5 * chi2;
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        return "histogram (" + this.binCount + " bins " + ": " + this.bins[0] +
                ", ...";
    }
}
