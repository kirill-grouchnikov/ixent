package org.jvnet.ixent.math.matrix;

import org.jvnet.ixent.math.MathConstants;

/**
 * Class for efficient manipulation of sparse matrices. A matrix is stored as
 * non-zero only values, the columns are stored in jagged two-dimensional arrays
 * as suggested by Geir Gundersen and Trond Steihaug in "Data structures in Java
 * for Matrix computations" Each column is stored in a single-dimensional array
 * (that grows as necessary), the row indexes are stored accordingly. <p/> For
 * example, the matrix
 * <pre>
 * 0 1 0 0
 * 2 0 3 0
 * 0 0 0 0
 * 4 1 0 0
 * </pre>
 * is stored as
 * <pre>
 * [1, 3] [2, 4]
 * [0, 3] [1, 1]
 * [1] [3]
 * [] []
 * </pre>
 *
 * @author Kirill Grouchnikov
 */

public class SparseColumnMatrix extends AbstractMatrix {
    protected double[][] nzValues;

    protected int[][] rowIndices;

    protected int[] nzCounters;

    /**
     * @param colCount number of columns
     * @param rowCount number of rows
     */
    public SparseColumnMatrix(int colCount, int rowCount) {
        super(colCount, rowCount);
        this.nzValues = new double[colCount][];
        this.rowIndices = new int[colCount][];
        this.nzCounters = new int[colCount];
    }

    /**
     * Gets values at specified location
     *
     * @param column column index
     * @param row    row index
     * @return value
     */
    public double get(int column, int row) {
        if (this.rowIndices[column] == null) {
            return 0.0;
        }
        int rowIndex = binarySearch(this.rowIndices[column], 0,
                this.nzCounters[column] - 1, row);
        if (rowIndex < 0) {
            return 0.0;
        }
        return this.nzValues[column][rowIndex];
    }

    /**
     * Performs a binary search for a given value in sorted integer array. The
     * only difference from <b>Arrays.binarySearch()</b> is that this function
     * gets <i>start</i> and <i>end</i> indexes. The array <strong>must</strong>
     * be sorted prior to making this call.  If it is not sorted, the results
     * are undefined.
     *
     * @param array      array to scan
     * @param startIndex start index of sub-array
     * @param endIndex   end index of sub-array
     * @param value      key to find
     * @return index of the search key, if it is contained in the list;
     *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *         <i>insertion point</i> is defined as the point at which the key
     *         would be inserted into the list: the index of the first element
     *         greater than the key, or <tt>list.size()</tt>, if all elements in
     *         the list are less than the specified key.  Note that this
     *         guarantees that the return value will be &gt;= 0 if and only if
     *         the key is found.
     */
    private static int binarySearch(int[] array, int startIndex, int endIndex,
                                    int value) {
        if (value < array[startIndex]) {
            return (-startIndex - 1);
        }
        if (value > array[endIndex]) {
            return (-(endIndex + 1) - 1);
        }

        if (startIndex == endIndex) {
            if (array[startIndex] == value) {
                return startIndex;
            }
            else {
                return (-(startIndex + 1) - 1);
            }
        }
        int midIndex = (startIndex + endIndex) / 2;
        if (value == array[midIndex]) {
            return midIndex;
        }

        if (value < array[midIndex]) {
            return binarySearch(array, startIndex, midIndex - 1, value);
        }
        else {
            return binarySearch(array, midIndex + 1, endIndex, value);
        }
    }

    /**
     * Sets value at specified location
     *
     * @param column column index
     * @param row    row index
     * @param value  value
     */
    public void set(int column, int row, double value) {
        if (this.rowIndices[column] == null) {
            // first value in this column
            this.rowIndices[column] = new int[2];
            this.nzValues[column] = new double[2];
            this.rowIndices[column][0] = row;
            this.nzValues[column][0] = value;
            this.nzCounters[column] = 1;
            return;
        }

        // search for it
        int rowIndex = binarySearch(this.rowIndices[column], 0,
                this.nzCounters[column] - 1, row);
        if (rowIndex >= 0) {
            // already setLocation, just change
            this.nzValues[column][rowIndex] = value;
            return;
        }
        else {
            // rowIndex = (-(insertion point) - 1)
            int insertionPoint = -(rowIndex + 1);
            // allocate new arrays
            int oldLength = this.nzCounters[column];
            int newLength = oldLength + 1;
            // check if need to allocate
            if (newLength <= this.rowIndices[column].length) {
                // just copy
                if (insertionPoint != oldLength) {
                    for (int i = oldLength; i > insertionPoint; i--) {
                        this.nzValues[column][i] =
                                this.nzValues[column][i - 1];
                        this.rowIndices[column][i] =
                                this.rowIndices[column][i - 1];
                    }
                }
                this.rowIndices[column][insertionPoint] = row;
                this.nzValues[column][insertionPoint] = value;
                this.nzCounters[column]++;
                return;
            }

            int[] newRowIndices = new int[2 * oldLength];
            double[] newNzValues = new double[2 * oldLength];

            if (insertionPoint == oldLength) {
                // special case - new column is the last
                System.arraycopy(this.rowIndices[column], 0, newRowIndices,
                        0, oldLength);
                System.arraycopy(this.nzValues[column], 0, newNzValues, 0,
                        oldLength);
            }
            else {
                System.arraycopy(this.rowIndices[column], 0, newRowIndices,
                        0, insertionPoint);
                System.arraycopy(this.nzValues[column], 0, newNzValues, 0,
                        insertionPoint);
                System.arraycopy(this.rowIndices[column], insertionPoint,
                        newRowIndices, insertionPoint + 1, oldLength
                        - insertionPoint);
                System.arraycopy(this.nzValues[column], insertionPoint,
                        newNzValues, insertionPoint + 1, oldLength
                        - insertionPoint);
            }
            newRowIndices[insertionPoint] = row;
            newNzValues[insertionPoint] = value;
            this.rowIndices[column] = null;
            this.rowIndices[column] = newRowIndices;
            this.nzValues[column] = null;
            this.nzValues[column] = newNzValues;
            this.nzCounters[column]++;

        }
    }

    /**
     * Add empty (zero) columns to this matrix
     *
     * @param columns number of columns to add
     */
    public void addEmptyColumns(int columns) {
        int oldCount = this.getColumnCount();
        int newCount = oldCount + columns;

        int[] newCounters = new int[newCount];
        System.arraycopy(this.nzCounters, 0, newCounters, 0, oldCount);
        double[][] newValues = new double[newCount][];
        for (int i = 0; i < oldCount; i++) {
            newValues[i] = this.nzValues[i];
        }
        int[][] newIndices = new int[newCount][];
        for (int i = 0; i < oldCount; i++) {
            newIndices[i] = this.rowIndices[i];
        }

        for (int i = oldCount; i < newCount; i++) {
            newCounters[i] = 0;
            newValues[i] = null;
            newIndices[i] = null;
        }

        this.nzCounters = newCounters;
        this.nzValues = newValues;
        this.rowIndices = newIndices;
    }

    /**
     * Computes convolution according to formulas for multiscale segmentation
     * process
     *
     * @param anotherMatrix weight matrix
     * @return convolution matrix, such that
     *         <pre>
     *                                                  res[k,l] =
     *         SUM(this[i,k]*w[i,j]*this[j,l])
     *                 + D(k,l)*SUM(this[i,k]*w[i,i])
     *                                              </pre>
     *         where the first SUM is over all (<b>i</b> != <b>j</b>), the
     *         second SUM is over all <b>i</b>, D(k, l) is Kronecker delta: D(i,
     *         j) = 1 if i=j and 0 otherwise, w(i, j) is element (i,j) of
     *         <i>anotherMatrix</i>
     */
    public SparseAccessibleMatrix computeConvolution(Matrix anotherMatrix) {
        // the size of the resulting matrix is the number of columns in this matrix
        int resSize = this.getColumnCount();
        SparseAccessibleMatrix result = new SparseAccessibleMatrix(resSize,
                resSize);

        long prevTime = System.currentTimeMillis();
        for (int k = 0; k < resSize; k++) {
//            System.out.println(k + " of " + resSize);
            // check if this column has non-zero values at all
            int kNZCount = this.nzCounters[k];
            if (kNZCount == 0) {
                continue;
            }

            for (int l = k + 1; l < resSize; l++) {
                int lNZCount = this.nzCounters[l];
                double firstSum = 0.0;
                // take all non-zero values of column k
                int count = 0;
                int mcount = 0;
                for (int kIndex = 0; kIndex < kNZCount; kIndex++) {
                    count++;
                    int kNZRow = this.rowIndices[k][kIndex];

                    // take all non-zero values of column l
                    for (int lIndex = 0; lIndex < lNZCount; lIndex++) {
                        count++;
                        int lNZRow = this.rowIndices[l][lIndex];

                        double toAdd = this.nzValues[k][kIndex] *
                                anotherMatrix.get(kNZRow, lNZRow) *
                                this.nzValues[l][lIndex];
                        count += 3;
                        mcount++;

                        firstSum += toAdd;
                    }
                }

                double secondSum = 0.0;
                if (k == l) {
                    // take all non-zero values of column k
                    for (int kIndex = 0; kIndex < kNZCount; kIndex++) {
                        count++;
                        int kNZRow = this.rowIndices[k][kIndex];

                        double toAdd = this.nzValues[k][kIndex] *
                                anotherMatrix.get(kNZRow, kNZRow);
                        count += 2;

                        secondSum += toAdd;
                        mcount++;
                    }
                }

                double finalWeight = firstSum + secondSum;
                if (Math.abs(finalWeight) > MathConstants.EPS_BIG) {
                    long currTime = System.currentTimeMillis();
                    result.set(k, l, firstSum + secondSum);
                    result.set(l, k, firstSum + secondSum);
//                    System.out.println((currTime-prevTime) + " to perform " +
//                            count + " (" + mcount + " = " +
//                            kNZCount + "*" + lNZCount + ") operations ");
                    prevTime = currTime;
                }
            }
        }

        return result;
    }

    public int getNzCount() {
        int allNz = 0;
        for (int i : this.nzCounters) {
            allNz += i;
        }
        return allNz;
    }

}