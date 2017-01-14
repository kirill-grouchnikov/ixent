package org.jvnet.ixent.math.equations;

public final class EquationSystemManager {
    public static double[] solve(double[][] a, double[] b) {
        double a11 = a[0][0];
        double a12 = a[0][1];
        double a13 = a[0][2];
        double a21 = a[1][0];
        double a22 = a[1][1];
        double a23 = a[1][2];
        double a31 = a[2][0];
        double a32 = a[2][1];
        double a33 = a[2][2];

        double b1 = b[0];
        double b2 = b[1];
        double b3 = b[2];

        double c11 = -a12 * a21 + a11 * a22;
        double c12 = -a13 * a21 + a23 * a11;
        double c21 = -a12 * a31 + a11 * a32;
        double c22 = -a13 * a31 + a33 * a11;

        double d1 = -b1 * a21 + b2 * a11;
        double d2 = -b1 * a31 + b3 * a11;

        double[] res = new double[3];
        res[2] = (-c21 * d1 + c11 * d2) / (-c12 * c21 + c22 * c11);
        res[1] = (d1 - c12 * res[2]) / c11;
        res[0] = (b1 - a12 * res[1] - a13 * res[2]) / a11;

        return res;
    }
}


