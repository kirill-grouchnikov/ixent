package org.jvnet.ixent.math.matrix;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for manipulation of dense matrices. A matrix is stored as a regular
 * two-dimensional array
 *
 * @author Kirill Grouchnikov
 */
public class DenseMatrix extends AbstractMatrix {
    private double[][] values;

    /**
     * @param _colCount number of columns
     * @param _rowCount number of rows
     */
    public DenseMatrix(int _colCount, int _rowCount) {
        super(_colCount, _rowCount);
        this.values = new double[_colCount][_rowCount];
    }

    /**
     * @param _values   2D array of initial values
     * @param _colCount number of columns
     * @param _rowCount number of rows
     */
    public DenseMatrix(int[][] _values, int _colCount, int _rowCount) {
        super(_colCount, _rowCount);
        this.values = new double[_colCount][_rowCount];
        for (int col = 0; col < this.colCount; col++) {
            for (int row = 0; row < this.rowCount; row++) {
                this.values[col][row] = _values[col][row];
            }
        }
    }

    /**
     * @param _values   2D array of initial _values
     * @param _colCount number of columns
     * @param _rowCount number of rows
     */
    public DenseMatrix(double[][] _values, int _colCount, int _rowCount) {
        super(_colCount, _rowCount);
        this.values = _values;
    }

    /**
     * Gets values at specified location
     *
     * @param column column index
     * @param row    row index
     * @return value
     */
    public double get(int column, int row) {
        return this.values[column][row];
    }

    /**
     * Sets value at specified location
     *
     * @param column column index
     * @param row    row index
     * @param value  value
     */
    public void set(int column, int row, double value) {
        this.values[column][row] = value;
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
                String s = nformat.format(this.values[col][row]);
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
     * Add empty (zero) columns to this matrix
     *
     * @param columns number of columns to add
     */
    public void addEmptyColumns(int columns) {
        int newColumnCount = this.colCount + columns;
        double[][] newValues = new double[newColumnCount][this.rowCount];
        for (int col = 0; col < this.colCount; col++) {
            for (int row = 0; row < this.rowCount; row++) {
                newValues[col][row] = this.values[col][row];
            }
        }
        for (int col = this.colCount; col < newColumnCount; col++) {
            for (int row = 0; row < this.rowCount; row++) {
                newValues[col][row] = 0.0;
            }
        }
        this.colCount = newColumnCount;
        this.values = null;
        this.values = newValues;
    }

    /**
     * Multiply this matrix by the specified column of another matrix
     *
     * @param matrix the second matrix
     * @param column column index in the second matrix
     * @return vector result
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
                sum += (this.values[i][row] * matrix.get(column, i));
            }
            result[row] = sum;
        }
        return result;
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
            sum += this.values[col][row];
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
                sum += this.values[col][row];
            }
        }
        return sum;
    }

    public int getNzCount() {
        return 0;
    }
}