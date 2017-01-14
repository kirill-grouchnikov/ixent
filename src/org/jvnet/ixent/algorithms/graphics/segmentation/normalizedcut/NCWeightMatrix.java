package org.jvnet.ixent.algorithms.graphics.segmentation.normalizedcut;

import org.jvnet.ixent.math.matrix.Matrix;
import org.jvnet.ixent.math.matrix.SparseMatrix;

public class NCWeightMatrix extends SparseMatrix {
    public NCWeightMatrix(int colCount, int rowCount) {
        super(colCount, rowCount);
    }

    public Matrix createMatrixForEigen() {
        NCWeightMatrix result = new NCWeightMatrix(this.colCount,
                this.rowCount);
        for (int row = 0; row < this.rowCount; row++) {
            // compute sum of all values
            double d = this.getSum(row);
            // compute the values
            int nzCount = this.nzCounters[row];
            double[] newValues = new double[nzCount];
            for (int i = 0; i < nzCount; i++) {
                int column = this.columnIndices[row][i];
                newValues[i] = -this.nzValues[row][i] / d;
            }
            // copy the indices
            int[] newIndices = new int[nzCount];
            System.arraycopy(this.columnIndices[row], 0, newIndices, 0,
                    nzCount);

            // create new row entry
            result.nzValues[row] = newValues;
            result.columnIndices[row] = newIndices;
            result.nzCounters[row] = nzCount;

            double diagValue = result.get(row, row);
            result.set(row, row, (d - diagValue) / d);
        }

        return result;
    }
}
