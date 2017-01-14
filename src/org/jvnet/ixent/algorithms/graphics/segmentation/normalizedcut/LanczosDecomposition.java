package org.jvnet.ixent.algorithms.graphics.segmentation.normalizedcut;

import org.jvnet.ixent.math.matrix.DenseMatrix;
import org.jvnet.ixent.math.matrix.Matrix;

public class LanczosDecomposition {

    private static final double EPSILON = 1E-6;

    private double[] m_eigenvalues;

    private Matrix m_eigenvectors;

    private EigenChecker checker;

    public LanczosDecomposition(EigenChecker checker, Matrix matrix,
                                int sought, int maxIterations) {
        this.checker = checker;
        if (matrix != null) {
            lanczos(matrix, sought, maxIterations);
        }
    }

    /**
     * Returns the eigenvalues.
     */
    public double[] getRealEigenvalues() {
        return this.m_eigenvalues;
    }

    /**
     * Returns the eigenvector matrix.
     */

    public Matrix getEigenvectors() {
        return m_eigenvectors;
    }

    /**
     * Iterative Lanczos algorithm for finding the approximate eigenvalues and
     * eigenvectors of a matrix.
     *
     * @param input  The matrix whose eigenvalues we wish to compute.
     * @param sought The number of eigenvalues we wish to compute for the input
     *               matrix. This number cannot exceed the number of columns for
     *               the matrix.
     */
    private void lanczos(Matrix input, int sought, int maxIterations) {
        int n = input.getColumnCount();

        if (sought > n) {
            throw new IllegalArgumentException("Number of eigensolutions sought cannot "
                    + "exceed size of the matrix: " + sought + " > "
                    + n);
        }

        if (maxIterations > n) {
            throw new IllegalArgumentException("Number of iterations sought cannot "
                    + "exceed size of the matrix: " + maxIterations + " > "
                    + n);
        }

        // random vector
        double[] r = new double[n];
        int ns = (int) Math.sqrt(n);
        for (int i = 0; i < n; i++) {
            r[i] = Math.random();//input.get(n%ns, n/ns);
        }

        // initial size for algorithm data structures
        // this may grow after a number of iterations
        int size = sought;

        // diagonal elements of T
        double[] a = new double[size];

        // off-diagonal elements of T
        double[] b = new double[size];

        boolean[] conv = new boolean[size];
        int converged = 0;

        // basis vectors for the Krylov subspace
        Matrix v = new DenseMatrix(size, n);

        // arrays used in the QL decomposition
        double[] d = new double[size];
        double[] e = new double[size];
        double[][] z = new double[size][size];

        // algorithm iterations
        b[0] = norm(r);
        // fill the 0'th column of V
        int i = 0;
        while (true) {
            System.out.println("Iteration " + i + " of " + maxIterations);
            // fill i-th column of matrix V
            if (i == 0) {
                for (int row = 0; row < n; row++) {
                    v.set(0, row, r[row] / b[0]);
                }
            }
            else {
                // v(i) = r / b(i-1)
                for (int row = 0; row < n; row++) {
                    v.set(i, row, r[row] / b[i - 1]);
                }
            }

            // r = A * v(i)
            r = input.multiply(v, i);

            if (i > 0) {
                // r = r - b(i-1) * v(i-1)
                for (int row = 0; row < n; row++) {
                    r[row] -= b[i - 1] * v.get(i - 1, row);
                }
            }

            // compute dot product
            // a(i) = v(i)' * r
            a[i] = 0.0;
            for (int row = 0; row < n; row++) {
                a[i] += (v.get(i, row) * r[i]);
            }

            // r = r - a(i)*v(i)
            for (int row = 0; row < n; row++) {
                r[row] -= (a[i] * v.get(i, row));
            }

            // b(i) = norm(r)
            b[i] = norm(r);

            // prepare to compute the eigenvalues and eigenvectors
            // of the tridiagonal matrix defined by a and b
            System.arraycopy(a, 0, d, 0, i + 1);
            System.arraycopy(b, 0, e, 1, i);
            // create identity matrix
            for (int p = 0; p < i; p++) {
                for (int q = 0; q < i; q++) {
                    z[p][q] = (p == q) ? 1 : 0;
                }
            }

            if (i > 0) {
                // compute the eigenvalues and eigenvectors of the
                // tridiagonal matrix
                tqli(d, e, i, z);

                // check how many converged
                converged = 0;
                for (int col = 0; col < i; col++) {
                    double residual = Math.abs(b[col] * z[i - 1][col]);
                    if ((residual < 0.1) && (Math.abs(d[col]) < 0.3)) {
                        ///					System.out.println(col + " converged");
                        converged++;
                        conv[col] = true;
                        double[] ritz = new double[size];
                        for (int j = 0; j < size; j++) {
                            ritz[j] = z[j][col];
                        }
                        double[] evector = v.multiply(ritz);
                        if (this.checker.isAcceptable(i, d[col], evector)) {
                            return;
                        }
                    }
                    else {
                        conv[col] = false;
                    }
                }
                System.out.println("At iteration " + i + " have " + converged
                        + " converged eigenvalues");

                if (converged >= sought) {
                    break;
                }
            }


            i++;

            if (i >= maxIterations) {
                break;
            }

            // check if need to allocate more space
            if (i >= size) {
                System.out.println("Growing...");
                int deltaSize = size;
                size *= 2;
                a = grow(a, size);
                b = grow(b, size);
                v.addEmptyColumns(deltaSize);
                d = grow(d, size + 1);
                e = grow(e, size + 1);
                z = grow(z, size + 1);
                conv = grow(conv, size + 1);
            }

        }

        this.m_eigenvalues = new double[converged];
        this.m_eigenvectors = new DenseMatrix(converged, n);
        Matrix ritzvectors = new DenseMatrix(size, size);
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                ritzvectors.set(col, row, z[row][col]);
            }
        }

        int index = 0;
        for (int col = 0; col < i; col++) {
            if (!conv[col]) {
                continue;
            }

            m_eigenvalues[index] = d[col];
            double[] column = v.multiply(ritzvectors, col);
            for (int row = 0; row < n; row++) {
                m_eigenvectors.set(index, row, column[row]);
            }
            index++;
        }

    }

    private static double norm(double[] arr) {
        double sum2 = 0.0;
        for (int i = 0; i < arr.length; i++) {
            sum2 += (arr[i] * arr[i]);
        }
        return Math.sqrt(sum2);
    }

    private boolean[] grow(boolean[] array, int length) {
        boolean[] result = new boolean[length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    private double[] grow(double[] array, int length) {
        double[] result = new double[length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    private double[][] grow(double[][] array, int length) {
        int prevLen = array.length;
        double[][] result = new double[length][length];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                result[i][j] = 0.0;
            }
        }
        for (int i = 0; i < prevLen; i++) {
            for (int j = 0; j < prevLen; j++) {
                result[i][j] = array[i][j];
            }
        }
        return result;
    }

    /**
     * Return the absolute value of a with the same sign as b.
     */
    private static double sign(double a, double b) {
        return (b >= 0.0 ? Math.abs(a) : -Math.abs(a));
    }

    /**
     * Returns sqrt(a^2 + b^2) without under/overflow.
     */

    private static double pythag(double a, double b) {
        double r;
        if (Math.abs(a) > Math.abs(b)) {
            r = b / a;
            r = Math.abs(a) * Math.sqrt(1 + r * r);
        }
        else {
            if (b != 0) {
                r = a / b;
                r = Math.abs(b) * Math.sqrt(1 + r * r);
            }
            else {
                r = 0.0;
            }
        }
        return r;
    }

    /**
     * "Tridiagonal QL Implicit" routine for computing eigenvalues and
     * eigenvectors of a symmetric, real, tridiagonal matrix. <p/> The routine
     * works extremely well in practice. The number of iterations for the first
     * few eigenvalues might be 4 or 5, say, but meanwhile the off-diagonal
     * elements in the lower right-hand corner have been reduced too. The later
     * eigenvalues are liberated with very little work. The average number of
     * iterations per eigenvalue is typically 1.3 - 1.6. The operation count per
     * iteration is O(n), with a fairly large effective coefficient, say, ~20n.
     * The total operation count for the diagonalization is then ~20n * (1.3 -
     * 1.6)n = ~30n^2. If the eigenvectors are required, there is an additional,
     * much larger, workload of about 3n^3 operations.
     *
     * @param d [0..n-1] array. On input, it contains the diagonal elements of
     *          the tridiagonal matrix. <p/> On output, it contains the
     *          eigenvalues.
     * @param e [0..n-1] array. On input, it contains the subdiagonal elements
     *          of the tridiagonal <p/> matrix, with e[0] arbitrary. On output,
     *          its contents are destroyed.
     * @param n The size of all parameter arrays.
     * @param z [0..n-1][0..n-1] array. On input, it contains the identity
     *          matrix. On output, the kth column <p/> of z returns the
     *          normalized eigenvector corresponding to d[k].
     */

    public static void tqli(double d[], double e[], int n, double[][] z) {
        int i;
        // Convenient to renumber the elements of e.
        for (i = 1; i < n; i++) {
            e[i - 1] = e[i];
        }
        e[n - 1] = 0.0;
        for (int l = 0; l < n; l++) {
//			System.out.println("Iteration " + l + " of " + n);
            int iter = 0;
            int m;
            do {
                // Look for a single small subdiagonal element to split the
                // matrix.
                for (m = l; m < n - 1; m++) {
                    double dd = Math.abs(d[m]) + Math.abs(d[m + 1]);
                    //					if (Math.abs(e[m]) + dd == dd) {
                    if (Math.abs(e[m]) < 0.0001) {
                        break;
                    }
                }
                if (m != l) {
                    iter = iter + 1;
                    if (iter == 30) {
                        throw new RuntimeException("Too many iterations");
                    }

                    // Form shift.
                    double g = (d[l + 1] - d[l]) / (2.0 * e[l]);
                    double r = pythag(g, 1.0);
                    // This is dm / ks.
                    g = d[m] - d[l] + e[l] / (g + sign(r, g));
                    double s, c;
                    s = c = 1.0;
                    double p = 0.0;

                    // A plane rotation as in the original QL, followed by
                    // Givens rotations to restore tridiagonal form.
                    for (i = m - 1; i >= l; i--) {
                        double f = s * e[i];
                        double b = c * e[i];
                        e[i + 1] = (r = pythag(f, g));
                        // Recover from underflow.
                        if (r == 0.0) {
                            d[i + 1] -= p;
                            e[m] = 0.0;
                            break;
                        }

                        s = f / r;
                        c = g / r;
                        g = d[i + 1] - p;
                        r = (d[i] - g) * s + 2.0 * c * b;
                        d[i + 1] = g + (p = s * r);
                        g = c * r - b;

                        // Form eigenvectors (optional).
                        for (int k = 0; k < n; k++) {
                            f = z[k][i + 1];
                            z[k][i + 1] = s * z[k][i] + c * f;
                            z[k][i] = c * z[k][i] - s * f;
                        }
                    }

                    if (r == 0.0 && i >= l) {
                        continue;
                    }

                    d[l] -= p;
                    e[l] = g;
                    e[m] = 0.0;
                }
            }
            while (m != l);
        }
    }

    public void tqli2(double d[], double e[], int n, double[][] z) {
        int i;
        // Convenient to renumber the elements of e.
        for (i = 2; i <= n; i++) {
            e[i - 1] = e[i];
        }
        e[n] = 0.0;
        for (int l = 1; l <= n; l++) {
            int iter = 0;
            int m;
            do {
                // Look for a single small subdiagonal element to split the
                // matrix.
                for (m = l; m <= n - 1; m++) {
                    double dd = Math.abs(d[m]) + Math.abs(d[m + 1]);
                    if (Math.abs(e[m]) + dd == dd) {
                        break;
                    }
                }
                if (m != l) {
                    iter = iter + 1;
                    if (iter == 30) {
                        throw new RuntimeException("Too many iterations");
                    }

                    // Form shift.
                    double g = (d[l + 1] - d[l]) / (2.0 * e[l]);
                    double r = pythag(g, 1.0);
                    // This is dm / ks.
                    g = d[m] - d[l] + e[l] / (g + sign(r, g));
                    double s, c;
                    s = c = 1.0;
                    double p = 0.0;

                    // A plane rotation as in the original QL, followed by
                    // Givens rotations to restore tridiagonal form.
                    for (i = m - 1; i >= l; i--) {
                        double f = s * e[i];
                        double b = c * e[i];
                        e[i + 1] = (r = pythag(f, g));
                        // Recover from underflow.
                        if (r == 0.0) {
                            d[i + 1] -= p;
                            e[m] = 0.0;
                            break;
                        }

                        s = f / r;
                        c = g / r;
                        g = d[i + 1] - p;
                        r = (d[i] - g) * s + 2.0 * c * b;
                        d[i + 1] = g + (p = s * r);
                        g = c * r - b;

                        // Form eigenvectors (optional).
                        for (int k = 1; k <= n; k++) {
                            f = z[k][i + 1];
                            z[k][i + 1] = s * z[k][i] + c * f;
                            z[k][i] = c * z[k][i] - s * f;
                        }
                    }

                    if (r == 0.0 && i >= l) {
                        continue;
                    }

                    d[l] -= p;
                    e[l] = g;
                    e[m] = 0.0;
                }
            }
            while (m != l);
        }
    }
}