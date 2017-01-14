package org.jvnet.ixent.math.matrix;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The interface class for general matrix (both dense and sparse).
 *
 * @author Kirill Grouchnikov
 */

public interface Matrix {
    /**
     * @return number of columns
     */
    public abstract int getColumnCount();

    /**
     * @return number of rows
     */
    public abstract int getRowCount();

    /**
     * Gets values at specified location
     *
     * @param column column index
     * @param row    row index
     * @return value
     */
    public abstract double get(int column, int row);

    /**
     * Sets value at specified location
     *
     * @param column column index
     * @param row    row index
     * @param value  value
     */
    public abstract void set(int column, int row, double value);

    /**
     * Dump to logger
     */
    public abstract void dump(Logger logger, Level level,
                              int totalPlaces, int decimalPlaces);

    /**
     * Multiply this matrix by the specified column of another matrix
     *
     * @param matrix the second matrix
     * @param column column index in the second matrix
     * @return vector result
     */
    public abstract double[] multiply(Matrix matrix, int column);

    /**
     * Multiply this matrix by the specified vector
     *
     * @param vector vector
     * @return vector result
     */
    public abstract double[] multiply(double[] vector);

    /**
     * Add empty (zero) columns to this matrix
     *
     * @param columns number of columns to add
     */
    public abstract void addEmptyColumns(int columns);

    /**
     * Compute the sum of all elements in given row
     *
     * @param row row index
     * @return sum of all elements in this row
     */
    public abstract double getSum(int row);

    /**
     * Compute the sum of all elements in given row. Only entries with 'true' in
     * the corresponding position of <b>toConsider</b> array are summed up.
     *
     * @param row        row index
     * @param toConsider boolean array
     * @return sum of all elements in this row
     */
    public abstract double getSum(int row, boolean[] toConsider);

    public abstract int getNzCount();
}