package org.jvnet.ixent.math.matrix;

/**
 * Matrix that gives access to inner underlying structure for quicker access to
 * non-zero values
 *
 * @author Kirill Grouchnikov
 */
public class SparseAccessibleMatrix extends SparseMatrix {
    /**
     * @param colCount number of columns
     * @param rowCount number of rows
     */
    public SparseAccessibleMatrix(int colCount, int rowCount) {
        super(colCount, rowCount);
    }

    /**
     * Returns the count of non zero values in given row
     *
     * @param row row index
     * @return non-zero values count
     */
    public int getNzCountInRow(int row) {
        return this.nzCounters[row];
    }

    /**
     * Returns the column corresponding to non-zero value number <b>index</b>
     *
     * @param row   row index
     * @param index non-zero column index
     * @return column
     */
    public int getColumnByIndex(int row, int index) {
        return this.columnIndices[row][index];
    }

    /**
     * Returns the value corresponding to non-zero value number <b>index</b>
     *
     * @param row   row index
     * @param index non-zero column index
     * @return value
     */
    public double getValueByIndex(int row, int index) {
        return this.nzValues[row][index];
    }
}
