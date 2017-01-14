package org.jvnet.ixent.math.matrix;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class implementing a few functions for all kinds of matrices. Some
 * functions may be overriden in the concrete classes
 *
 * @author Kirill Grouchnikov
 */

public abstract class AbstractMatrix implements Matrix {
    protected int rowCount;

    protected int colCount;

    /**
     * @param colCount number of columns
     * @param rowCount number of rows
     */
    public AbstractMatrix(int colCount, int rowCount) {
        this.colCount = colCount;
        this.rowCount = rowCount;
    }

    public int getColumnCount() {
        return this.colCount;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    /**
     * General implementation. May be overriden in concrete subclasses
     */
    public double[] multiply(Matrix matrix, int column) {
        if (this.getColumnCount() != matrix.getRowCount()) {
            return null;
        }
        int n = this.getRowCount();
        double[] result = new double[n];
        for (int row = 0; row < n; row++) {
            double sum = 0.0;
            for (int i = 0; i < this.getColumnCount(); i++) {
                sum += (this.get(i, row) * matrix.get(column, i));
            }
            result[row] = sum;
        }
        return result;
    }

    /**
     * General implementation. May be overriden in concrete subclasses
     *
     * @param vector vector
     * @return vector result
     */
    public double[] multiply(double[] vector) {
        if (this.getColumnCount() != vector.length) {
            return null;
        }
        int n = this.getRowCount();
        double[] result = new double[n];
        for (int row = 0; row < n; row++) {
            double sum = 0.0;
            for (int col = 0; col < this.getColumnCount(); col++) {
                sum += (this.get(col, row) * vector[col]);
            }
            result[row] = sum;
        }
        return result;
    }

    /**
     * Dump to standard output
     */
    public void dump(Logger logger, Level level,
                     int totalPlaces, int decimalPlaces) {

        StringBuffer sbuffer = new StringBuffer();
        sbuffer.append(
                "\nMATRIX " + this.rowCount + "*" + this.colCount + '\n');

        NumberFormat nformat = new DecimalFormat();
        nformat.setMinimumIntegerDigits(1);
        nformat.setMaximumIntegerDigits(totalPlaces - decimalPlaces - 1);
        nformat.setMinimumFractionDigits(decimalPlaces);
        nformat.setMaximumFractionDigits(decimalPlaces);
        nformat.setGroupingUsed(false);

        for (int row = 0; row < this.rowCount; row++) {
            for (int col = 0; col < this.colCount; col++) {
                String s = nformat.format(this.get(col, row));
                int padding = Math.max(1, totalPlaces + 1 - s.length());
                for (int k = 0; k < padding; k++) {
                    sbuffer.append(' ');
                }
                sbuffer.append(s);
            }
            sbuffer.append('\n');
        }
        logger.log(level, sbuffer.toString());
    }

    /**
     * Compute the sum of all elements in given row
     *
     * @param row row index
     * @return sum of all elements in this row
     */
    public double getSum(int row) {
        double sum = 0.0;
        int ncol = this.getColumnCount();
        for (int col = 0; col < ncol; col++) {
            sum += this.get(col, row);
        }
        return sum;
    }

    /**
     * Compute the sum of all elements in given row. Only entries with 'true' in
     * the corresponding position of <b>toConsider</b> array are summed up.
     *
     * @param row        row index
     * @param toConsider boolean array
     * @return sum of all elements in this row
     */
    public double getSum(int row, boolean[] toConsider) {
        double sum = 0.0;
        int ncol = this.getColumnCount();
        for (int col = 0; col < ncol; col++) {
            if (toConsider[col]) {
                sum += this.get(col, row);
            }
        }
        return sum;
    }
}