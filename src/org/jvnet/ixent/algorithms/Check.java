package org.jvnet.ixent.algorithms;

import org.jvnet.ixent.algorithms.graphics.segmentation.normalizedcut.LanczosDecomposition;

public class Check {
    public void check() {
        double[][] arr = new double[16][16];
        double[] d = new double[16];
        for (int i = 0; i < 16; i++) {
            int ri = i / 4;
            int ci = i % 4;
            d[i] = 0.0;
            for (int j = 0; j < 16; j++) {
                int rj = j / 4;
                int cj = j % 4;
                arr[i][j] = 0.0;
                if ((ci < 2) && (cj >= 2)) {
                    continue;
                }
                if ((cj < 2) && (ci >= 2)) {
                    continue;
                }
                double w = 1.0 / (1.0 + Math.abs(ri - rj) + Math.abs(ci - cj));
                arr[i][j] = w;
                d[i] += w;
            }
        }
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                if (i == j) {
                    arr[i][j] = (d[i] - arr[i][j]);///d[i];
                }
                else {
                    arr[i][j] = -arr[i][j];
                }
            }
        }

        SimpleMatrix m = new SimpleMatrix();
        double[][] a = m.getInverse(arr, 16);
        double[][] mul = m.mult(arr, a, 16);
    }

    public void checkN(int n) {
        int n2 = n * n;
        int n1 = n / 2;
        double[][] arr = new double[n2][n2];
        double[] d = new double[n2];
        for (int i = 0; i < n2; i++) {
            int ri = i / n;
            int ci = i % n;
            d[i] = 0.0;
            for (int j = 0; j < n2; j++) {
                int rj = j / n;
                int cj = j % n;
                arr[i][j] = 0.0;
                if ((ci < n1) && (cj >= n1)) {
                    continue;
                }
                if ((cj < n1) && (ci >= n1)) {
                    continue;
                }
                double w = 1.0 / (1.0 + Math.abs(ri - rj) + Math.abs(ci - cj));
                arr[i][j] = w;
                d[i] += w;
            }
        }
        for (int i = 0; i < n2; i++) {
            for (int j = 0; j < n2; j++) {
                if (i == j) {
                    arr[i][j] = (d[i] - arr[i][j]);///d[i];
                }
                else {
                    arr[i][j] = -arr[i][j];
                }
            }
        }

        SimpleMatrix m = new SimpleMatrix();
        double[][] a = m.getInverse(arr, n2);
        double[][] mul = m.mult(arr, a, n2);
    }

    public void check(double[][] k, double[][] m, int n) {
        int n1 = (int) Math.sqrt(n);
        SimpleMatrix sm = new SimpleMatrix();
        // invert k
        double[][] k1 = sm.getInverse(k, n);

        // allocate
        double[] a = new double[n];
        double[] b = new double[n];
        double[][] x = new double[n][n];

        double[] x0 = new double[n];
        for (int i = 0; i < n; i++) {
            x0[i] = Math.random();
        }

        double div = Math.sqrt(sm.mult(x0, sm.mult(m, x0, n), n));

        for (int i = 0; i < n; i++) {
            x[i][0] = x0[i] / div;
        }

        double[][] k1m = sm.mult(k1, m, n);

        b[0] = 0.0;
        for (int it = 1; it <= n; it++) {
            double[] xi1 = new double[n];
            if (it == 1) {
                for (int i = 0; i < n; i++) {
                    xi1[i] = x0[i];
                }
            }
            else {
                for (int i = 0; i < n; i++) {
                    xi1[i] = x[i][it - 2];
                }
            }
            double[] xi = new double[n];
            for (int i = 0; i < n; i++) {
                xi[i] = x[i][it - 1];
            }
            double[] yi = sm.mult(k1m, xi, n);

            a[it - 1] = sm.mult(yi, sm.mult(m, xi, n), n);

            if (it == n) {
                break;
            }

            double[] zi = new double[n];
            for (int i = 0; i < n; i++) {
                zi[i] = yi[i] - a[it - 1] * xi[i] - b[it - 1] * xi1[i];
            }

            b[it] = Math.sqrt(sm.mult(yi, sm.mult(m, zi, n), n));
            for (int i = 0; i < n; i++) {
                x[i][it] = zi[i] / b[it];
            }
        }

        // here - ready for TQLI
        double[][] t = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                t[i][j] = 0.0;
            }
        }
        for (int i = 0; i < n; i++) {
            t[i][i] = a[i];
            if (i < (n - 1)) {
                t[i][i + 1] = b[i + 1];
                t[i + 1][i] = b[i + 1];
            }
        }

        double[][] z = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                z[i][j] = (i == j) ? 1.0 : 0.0;
            }
        }

        int ns = 4;
        double[] as = new double[ns];
        double[] bs = new double[ns];
        for (int i = 0; i < ns; i++) {
            as[i] = a[i];
            bs[i] = b[i];
        }

        LanczosDecomposition.tqli(a, b, n, z);

        // here - after TQLI
        // check tridiagonal
        for (int i = 0; i < n; i++) {
            System.out.println("Checking tridiag eigen " + a[i]);
            double[] ev = new double[n];
            for (int j = 0; j < n; j++) {
                ev[j] = z[j][i];
            }
            double[] left = sm.mult(t, ev, n);
            double[] right = new double[n];
            for (int j = 0; j < n; j++) {
                right[j] = a[i] * ev[j];
            }
            for (int j = 0; j < n; j++) {
                System.out.println("Difference in row " + j + " is " +
                        (left[j] - right[j]));
            }
        }

        // compute for original and check
        for (int i = 0; i < n; i++) {
            double lam = 1 / a[i];
            System.out.println("Checking orig eigen " + lam);
            double[] ev = new double[n];
            for (int j = 0; j < n; j++) {
                ev[j] = z[j][i];
            }
            double[] evo = sm.mult(x, ev, n);
            System.out.println("Original eigen vector ");
            for (int j = 0; j < n; j++) {
                if ((j % n1) == 0) {
                    System.out.println();
                }
                System.out.print(evo[j] + " ");
            }

            System.out.println();

            double[] left = sm.mult(k, evo, n);
            double[] right = sm.mult(m, evo, n);
            for (int j = 0; j < n; j++) {
                right[j] *= lam;
            }
            for (int j = 0; j < n; j++) {
                System.out.println("Difference in row " + j + " is " +
                        (left[j] - right[j]));
            }
        }

        // here - ready for TQLI small
        double[][] ts = new double[ns][ns];
        for (int i = 0; i < ns; i++) {
            for (int j = 0; j < ns; j++) {
                ts[i][j] = 0.0;
            }
        }
        for (int i = 0; i < ns; i++) {
            ts[i][i] = as[i];
            if (i < (ns - 1)) {
                t[i][i + 1] = bs[i + 1];
                t[i + 1][i] = bs[i + 1];
            }
        }

        double[][] zs = new double[ns][ns];
        for (int i = 0; i < ns; i++) {
            for (int j = 0; j < ns; j++) {
                zs[i][j] = (i == j) ? 1.0 : 0.0;
            }
        }
        LanczosDecomposition.tqli(as, bs, ns, zs);

        // here - after TQLI
        // check tridiagonal
        for (int i = 0; i < ns; i++) {
            System.out.println("Checking tridiag eigen " + as[i]);
            double[] ev = new double[ns];
            for (int j = 0; j < ns; j++) {
                ev[j] = zs[j][i];
            }
            double[] left = sm.mult(ts, ev, ns);
            double[] right = new double[ns];
            for (int j = 0; j < ns; j++) {
                right[j] = as[i] * ev[j];
            }
            for (int j = 0; j < ns; j++) {
                System.out.println("Difference in row " + j + " is " +
                        (left[j] - right[j]));
            }
        }

        // go to original
        for (int i = 0; i < ns; i++) {
            double lam = 1 / as[i];
            System.out.println("Checking approximate orig eigen " + lam);
            double[] ev = new double[n];
            for (int j = 0; j < ns; j++) {
                ev[j] = zs[j][i];
            }
            double[] evo = new double[n];
            for (int j = 0; j < n; j++) {
                evo[j] = 0.0;
                for (int l = 0; l < ns; l++) {
                    evo[j] += x[j][l] * ev[l];
                }
            }
            System.out.println("Approximate orig eigen vector ");
            for (int j = 0; j < n; j++) {
                if ((j % n1) == 0) {
                    System.out.println();
                }
                System.out.print(evo[j] + " ");
            }
            System.out.println();

            double[] left = sm.mult(k, evo, n);
            double[] right = sm.mult(m, evo, n);
            for (int j = 0; j < n; j++) {
                right[j] *= lam;
            }
            for (int j = 0; j < n; j++) {
                System.out.println("Difference in row " + j + " is " +
                        (left[j] - right[j]));
            }
        }


    }


    public void go(int n) {
        double[][] k = {{1, 1, 1}, {1, 4, 4}, {1, 4, 7}};
        double[][] m = {{4, 0, 0}, {0, 2, 0}, {0, 0, 1}};

//		check(k, m, 3);

        int n2 = n * n;
        int n1 = n / 2;
        double[][] w = new double[n2][n2];
        double[][] d = new double[n2][n2];
        for (int i = 0; i < n2; i++) {
            for (int j = 0; j < n2; j++) {
                d[i][j] = 0.0;
            }
        }

        for (int i = 0; i < n2; i++) {
            int ri = i / n;
            int ci = i % n;
            for (int j = 0; j < n2; j++) {
                int rj = j / n;
                int cj = j % n;
                w[i][j] = 0.0;
                if ((ci < n1) && (cj >= n1)) {
                    continue;
                }
                if ((cj < n1) && (ci >= n1)) {
                    continue;
                }
                double w1 = Math.exp(-Math.abs(ri - rj) - Math.abs(ci - cj));
                w[i][j] = w1;
                d[i][i] += w1;
            }
        }
        for (int i = 0; i < n2; i++) {
            for (int j = 0; j < n2; j++) {
                w[i][j] = (d[i][j] - w[i][j]);
            }
        }
        // perturb
        for (int i = 0; i < n2; i++) {
            for (int j = i + 1; j < n2; j++) {
                if (w[i][j] != 0.0) {
                    w[i][j] += 0.02 * Math.random();
                    w[j][i] = w[i][j];
                }
            }
        }

        for (int i = 0; i < n2; i++) {
            for (int j = 0; j < n2; j++) {
                w[i][j] /= d[i][i];
            }
            d[i][i] = 1.0;
        }


        SimpleMatrix sm = new SimpleMatrix();
        System.out.println("W");
        sm.dump(w, n2, n2);
        double[][] a = sm.getInverse(w, n2);
        double[][] mul = sm.mult(w, a, n2);

        double[][] mm = new double[][]{{1, 1, 1}, {1, 2, 3}, {2, 1, 2}};
        double[][] am = sm.getInverse(mm, 3);
        double[][] mulm = sm.mult(mm, am, 3);

        check(w, d, n2);
    }
}
