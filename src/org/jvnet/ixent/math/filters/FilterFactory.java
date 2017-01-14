package org.jvnet.ixent.math.filters;

public class FilterFactory {
    public static final double C = 3;

    // make all calls be static
    private FilterFactory() {
    }

    private static int getSize(double sigma) {
        return (int) (1 + Math.ceil(FilterFactory.C * sigma));
    }

    public static Filter getGaussianRegular(double sigma) {
        int size = FilterFactory.getSize(sigma);
        Filter result = new Filter(size);
        // compute positive X - positive Y quadrant and then mirror
        double coef = 1.0 / (2.0 * Math.PI * sigma * sigma);
        for (int i = 0; i <= size; i++) {
            double x = i / sigma;
            for (int j = 0; j <= size; j++) {
                double y = j / sigma;
                double value = coef * Math.exp(-(x * x + y * y));
                result.set(i, j, value);
            }
        }
        for (int i = 0; i <= size; i++) {
            for (int j = 0; j <= size; j++) {
                double value = result.get(i, j);
                result.set(-i, j, value);
                result.set(i, -j, value);
                result.set(-i, -j, value);
            }
        }
        return result;
    }

    public static Filter getDOOGFilter(double sigma, double factor) {
        if (factor < 0.0) {
            factor = -factor;
        }
        if (factor < 1.0) {
            factor = 1.0 / factor;
        }
        Filter gaussianBig = FilterFactory.getGaussianRegular(sigma * factor);
        Filter gaussianSmall = FilterFactory.getGaussianRegular(sigma);
        int size = gaussianBig.getSize();
        Filter result = new Filter(size);
        for (int i = -size; i <= size; i++) {
            for (int j = -size; j <= size; j++) {
                result.set(i, j, gaussianBig.get(i, j)
                        - gaussianSmall.get(i, j));
            }
        }
        return result;
    }

    public static double G2(double x, double y, double sigma, double factor) {
        double xp = x / (sigma);
        double yp = y / (factor * sigma);
        double ep = Math.exp(-(xp * xp + yp * yp));
        return 0.9213 * (2 * x * x - 1.0) * ep;
    }

    public static double H2(double x, double y, double sigma, double factor) {
        double xp = x / (sigma);
        double yp = y / (factor * sigma);
        double ep = Math.exp(-(xp * xp + yp * yp));
        return (-2.205 * x + 0.9780 * x * x * x) * ep;
    }

    public static double G1(double x, double y, double sigma, double factor) {
        double xp = x / (sigma);
        double yp = y / (factor * sigma);
        double ep = Math.exp(-(xp * xp + yp * yp));
        return 2 * x * ep;
    }

    public static Filter getG1Filter(double sigma, double factor, double theta) {
        // normalize factor
        if (factor < 0.0) {
            factor = -factor;
        }
        if (factor < 1.0) {
            factor = 1.0 / factor;
        }
        // normalize theta
        while (theta < 0.0) {
            theta += 360.0;
        }
        while (theta >= 360.0) {
            theta -= 360.0;
        }
        int size = FilterFactory.getSize(sigma * factor);
        Filter result = new Filter(size);
        // compute positive X - positive Y quadrant and then mirror
        double coef = 1.0 / (2.0 * Math.PI * sigma * sigma * factor);

        double ct = Math.cos(theta * Math.PI / 180.0);
        double st = Math.sin(theta * Math.PI / 180.0);

        for (int x = -size; x <= size; x++) {
            for (int y = -size; y <= size; y++) {
                double xt = x * ct + y * st;
                double yt = -x * st + y * ct;
                double value = coef * G1(xt, yt, sigma, factor);
                if (value < 0.0) {
                    value = 0.0;
                }
                result.set(x, y, value);
            }
        }
        result.normalize();
        return result;
    }

    public static Filter getG2Filter(double sigma, double factor, double theta) {
        // normalize factor
        if (factor < 0.0) {
            factor = -factor;
        }
        if (factor < 1.0) {
            factor = 1.0 / factor;
        }
        // normalize theta
        while (theta < 0.0) {
            theta += 360.0;
        }
        while (theta >= 360.0) {
            theta -= 360.0;
        }
        int size = FilterFactory.getSize(sigma * factor);
        Filter result = new Filter(size);
        // compute positive X - positive Y quadrant and then mirror
        double coef = 1.0 / (2.0 * Math.PI * sigma * sigma * factor);

        double ct = Math.cos(theta * Math.PI / 180.0);
        double st = Math.sin(theta * Math.PI / 180.0);

        for (int x = -size; x <= size; x++) {
            for (int y = -size; y <= size; y++) {
                double xt = x * ct + y * st;
                double yt = -x * st + y * ct;
                double value = coef * G2(xt, yt, sigma, factor);
                result.set(x, y, value);
            }
        }
        result.normalize();
        return result;
    }

    public static Filter getH2Filter(double sigma, double factor, double theta) {
        // normalize factor
        if (factor < 0.0) {
            factor = -factor;
        }
        if (factor < 1.0) {
            factor = 1.0 / factor;
        }
        // normalize theta
        while (theta < 0.0) {
            theta += 360.0;
        }
        while (theta >= 360.0) {
            theta -= 360.0;
        }
        int size = FilterFactory.getSize(sigma * factor);
        Filter result = new Filter(size);
        // compute positive X - positive Y quadrant and then mirror
        double coef = 1.0 / (2.0 * Math.PI * sigma * sigma * factor);

        double ct = Math.cos(theta * Math.PI / 180.0);
        double st = Math.sin(theta * Math.PI / 180.0);

        for (int x = -size; x <= size; x++) {
            for (int y = -size; y <= size; y++) {
                double xt = x * ct + y * st;
                double yt = -x * st + y * ct;
                double value = coef * H2(xt, yt, sigma, factor);
                if (value < 0.0) {
                    value = 0.0;
                }
                result.set(x, y, value);
            }
        }
        result.normalize();
        return result;
    }
}