package org.jvnet.ixent.algorithms;

public class SimpleMatrix {
    double eps = 1e-07;

    public double[][] getInverse(double[][] matrix, int n) {
        // create
        int n2 = 2 * n;
        double[][] a = new double[n][n2];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                a[row][col] = matrix[row][col];
            }
            for (int col = n; col < n2; col++) {
                a[row][col] = ((col - row) == n) ? 1 : 0;
            }
        }
        dump(a, n, n2);
        double f = 1.0 / a[0][0];
        for (int col = 0; col < n2; col++) {
            a[0][col] *= f;
        }
        for (int col = 0; col < (n - 1); col++) {
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(a[row][col]) < eps) {
                    continue;
                }
                double w = 1.0 / a[row][col];
                for (int col2 = col; col2 < n2; col2++) {
                    a[row][col2] = w * a[row][col2] - a[col][col2];
                }
            }
            if (Math.abs(a[col + 1][col + 1]) < eps) {
                continue;
            }
            double f2 = 1.0 / a[col + 1][col + 1];
            for (int col2 = col + 1; col2 < n2; col2++) {
                a[col + 1][col2] *= f2;
            }

            // zero and one
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    if (Math.abs(a[r][c]) < eps) {
                        a[r][c] = 0.0;
                    }
                    if (Math.abs(1.0 - a[r][c]) < eps) {
                        a[r][c] = 1.0;
                    }
                }
            }
//			System.out.println("\n After column " + col);
//			dump(a, n, n2);
        }

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (Math.abs(a[row][col]) < eps) {
                    a[row][col] = 0.0;
                }
                if (Math.abs(1.0 - a[row][col]) < eps) {
                    a[row][col] = 1.0;
                }
            }
        }

        dump(a, n, n2);

        for (int col = n - 1; col > 0; col--) {
            if (Math.abs(a[col][col]) < eps) {
                continue;
            }
            for (int row = 0; row < col; row++) {
                double w = a[row][col];
                for (int col2 = col; col2 < n2; col2++) {
                    a[row][col2] -= w * a[col][col2];
                    if (Math.abs(a[row][col2]) < eps) {
                        a[row][col2] = 0.0;
                    }
                    if (Math.abs(1.0 - a[row][col2]) < eps) {
                        a[row][col2] = 1.0;
                    }
                }
            }
        }
        dump(a, n, n2);

        double[][] res = new double[n][n];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                res[row][col] = a[row][n + col];
            }
        }

        dump(res, n, n);
        return res;
    }

    public double[][] mult(double[][] a, double[][] b, int n) {
        double[][] res = new double[n][n];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                res[row][col] = 0.0;
            }
        }
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                for (int i = 0; i < n; i++) {
                    res[row][col] += a[row][i] * b[i][col];
                }
            }
        }
        dump(res, n, n);
        return res;
    }

    public double[] mult(double[][] a, double[] b, int n) {
        double[] res = new double[n];
        for (int row = 0; row < n; row++) {
            res[row] = 0.0;
        }
        for (int row = 0; row < n; row++) {
            for (int i = 0; i < n; i++) {
                res[row] += a[row][i] * b[i];
            }
        }
        return res;
    }

    public double mult(double[] a, double[] b, int n) {
        double res = 0.0;
        for (int i = 0; i < n; i++) {
            res += a[i] * b[i];
        }
        return res;
    }

    protected void dump(double[][] matrix, int nr, int nc) {
        for (int row = 0; row < nr; row++) {
            for (int col = 0; col < nc; col++) {
                System.out.print(matrix[row][col] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

}
