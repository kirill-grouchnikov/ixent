package org.jvnet.ixent.test;

import static org.jvnet.ixent.math.MathConstants.EPS;
import junit.framework.TestCase;

import org.jvnet.ixent.math.matrix.*;

/**
 * @author Kirill Grouchnikov
 */
public class TestMatrix extends TestCase {
    public TestMatrix(String name) {
        super(name);
    }

    public void testSingleMatrix(Matrix matrix, int n, int tries) {
        double[][] backup = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                backup[i][j] = 0.0;
            }
        }
        for (int t = 0; t < tries; t++) {
            int col = (int) (n * Math.random());
            int row = (int) (n * Math.random());
            double v = 10.0 * Math.random();
            matrix.set(col, row, v);
            backup[col][row] = v;
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double diff = backup[i][j] - matrix.get(i, j);
                if (diff > EPS) {
                    System.err.println("At position " + i + ", " + j + "" +
                            "should be " + backup[i][j] + ", have " +
                            matrix.get(i, j));
                }
                assertTrue(diff <= EPS);
            }
        }
    }

    public void testMatrices() {
        int[] ns = {5, 10, 50, 100, 500, 1000};
        for (int i = 0; i < ns.length; i++) {
            int n = ns[i];
            testSingleMatrix(new DenseMatrix(n, n), n, 4 * n);
            testSingleMatrix(new SparseMatrix(n, n), n, 4 * n);
            testSingleMatrix(new SparseColumnMatrix(n, n), n, 4 * n);
        }
    }

}
