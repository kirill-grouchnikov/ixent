package org.jvnet.ixent.math.filters;

import org.jvnet.ixent.math.MathConstants;

public class Filter {
    // The filter values are centered around (0,0)
    // The matrix that holds the values has its [0][0] component as left-top
    // corner of the filter

    private double[][] filterValues;

    // the matrix is N*N where N=2*filterSize+1 and the value at [fs][fs]
    // is the central value (0,0) of the filter
    private int filterSize;

    private int filterSpan;

    private int minX, maxX, minY, maxY;

    public Filter(int filterSize) {
        this.filterSize = filterSize;
        this.filterSpan = 2 * this.filterSize + 1;
        this.filterValues = new double[this.filterSpan][this.filterSpan];
        for (int i = 0; i < this.filterSpan; i++) {
            for (int j = 0; j < this.filterSpan; j++) {
                this.filterValues[i][j] = 0;
            }
        }
    }

    public int getSize() {
        return this.filterSize;
    }

    public int getBoundL() {
        return this.minX;
    }

    public int getBoundR() {
        return this.maxX;
    }

    public int getBoundT() {
        return this.minY;
    }

    public int getBoundB() {
        return this.maxY;
    }

    private boolean isValidIndex(int index) {
        return ((index >= -this.filterSize) && (index <= this.filterSize));
    }

    public void set(int indexX, int indexY, double value) {
        if ((!this.isValidIndex(indexX)) || (!this.isValidIndex(indexY))) {
            return;
        }
        this.filterValues[indexX + this.filterSize][indexY + this.filterSize] =
                value;
    }

    public double get(int indexX, int indexY) {
        if ((!this.isValidIndex(indexX)) || (!this.isValidIndex(indexY))) {
            return 0.0;
        }
        return this.filterValues[indexX + this.filterSize][indexY
                + this.filterSize];
    }

    public double getUnchecked(int indexX, int indexY) {
        return this.filterValues[indexX + this.filterSize][indexY
                + this.filterSize];
    }

    public double getMax() {
        double maxv = Double.MIN_VALUE;
        for (int i = 0; i < this.filterSpan; i++) {
            for (int j = 0; j < this.filterSpan; j++) {
                if (this.filterValues[i][j] > maxv) {
                    maxv = this.filterValues[i][j];
                }
            }
        }
        return maxv;
    }

    public void normalize() {
        // sum up
        double sum = 0.0;
        for (int i = 0; i < this.filterSpan; i++) {
            for (int j = 0; j < this.filterSpan; j++) {
                sum += this.filterValues[i][j];
            }
        }
        // normalize
        for (int i = 0; i < this.filterSpan; i++) {
            for (int j = 0; j < this.filterSpan; j++) {
                this.filterValues[i][j] /= sum;
            }
        }

        // scan for bounding rectangle of non-zero values
        double eps = MathConstants.EPS_BIG;
        this.minX = -this.filterSize;
        this.maxX = this.filterSize;
        this.minY = -this.filterSize;
        this.maxY = this.filterSize;
        // scan columns
        boolean[] isColumnZero = new boolean[this.filterSpan];
        for (int col = 0; col < this.filterSpan; col++) {
            for (int row = 0; row < this.filterSpan; row++) {
                if (this.filterValues[col][row] > eps) {
                    isColumnZero[col] = true;
                    break;
                }
            }
        }
        // scan from left
        for (int col = 0; col < this.filterSpan; col++) {
            if (isColumnZero[col] == false) {
                this.minX++;
            }
            else {
                break;
            }
        }
        // scan from right
        for (int col = this.filterSpan - 1; col >= 0; col--) {
            if (isColumnZero[col] == false) {
                this.maxX--;
            }
            else {
                break;
            }
        }
        // scan rows
        boolean[] isRowZero = new boolean[this.filterSpan];
        for (int row = 0; row < this.filterSpan; row++) {
            for (int col = 0; col < this.filterSpan; col++) {
                if (this.filterValues[col][row] > eps) {
                    isRowZero[row] = true;
                    break;
                }
            }
        }
        // scan from top
        for (int row = 0; row < this.filterSpan; row++) {
            if (isRowZero[row] == false) {
                this.minY++;
            }
            else {
                break;
            }
        }
        // scan from bottom
        for (int row = this.filterSpan - 1; row >= 0; row--) {
            if (isColumnZero[row] == false) {
                this.maxY--;
            }
            else {
                break;
            }
        }
    }

}