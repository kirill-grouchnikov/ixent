package org.jvnet.ixent.graphics.colors;

public final class ColorConverter {
    public static final class TripletHSV {
        public double h;
        public boolean h_defined;
        public double s;
        public double v;

        public TripletHSV(double h, double s, double v) {
            this.h = h;
            this.s = s;
            this.v = v;
            this.h_defined = true;
        }

        public TripletHSV(double s, double v) {
            this.s = s;
            this.v = v;
            this.h_defined = false;
        }
    }

    public static final class TripletRGB {
        public double r;
        public double g;
        public double b;

        public TripletRGB(double r, double g, double b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    public static final class TripletLAB {
        public double l;
        public double a;
        public double b;

        public TripletLAB(double l, double a, double b) {
            this.l = l;
            this.a = a;
            this.b = b;
        }
    }

    // input: h in [0, 360) or UNDEFINED, s and v in [0,1]
    // output : r, g, b each in [0,1]
    public static TripletRGB HSV_To_RGB(TripletHSV tripletHSV) {
        double v = tripletHSV.v;

        if (!tripletHSV.h_defined) {
            return new TripletRGB(v, v, v);
        }

        double s = tripletHSV.s;
        if (s == 0.0) {
            return null;
        }

        double h = tripletHSV.h;

        if (h == 360.0) {
            h = 0.0;
        }
        h /= 60.0;
        int i = (int) Math.floor(h);
        double f = h - i;
        double p = v * (1.0 - s);
        double q = v * (1 - (s * f));
        double t = v * (1 - s * (1 - f));
        switch (i) {
            case 0:
                return new TripletRGB(v, t, p);
            case 1:
                return new TripletRGB(q, v, p);
            case 2:
                return new TripletRGB(p, v, t);
            case 3:
                return new TripletRGB(p, q, v);
            case 4:
                return new TripletRGB(t, p, v);
            case 5:
                return new TripletRGB(v, p, q);
        }
        return null;
    }

    // input : r, g, b each in [0,1]
    // output: h in [0, 360), s and v in [0,1] except if s=0 then h=UNDEFINED
    public static TripletHSV RGB_To_HSV(TripletRGB tripletRGB) {
        double r = tripletRGB.r;
        double g = tripletRGB.g;
        double b = tripletRGB.b;

        double maxVal = r;
        double minVal = r;
        if (g > maxVal) {
            maxVal = g;
        }
        if (g < minVal) {
            minVal = g;
        }
        if (b > maxVal) {
            maxVal = b;
        }
        if (b < minVal) {
            minVal = b;
        }

        double v = maxVal;
        double s;
        if (maxVal != 0) {
            s = (maxVal - minVal) / maxVal;
        }
        else {
            s = 0.0;
        }

        if (s == 0.0) {
            return new TripletHSV(s, v);
        }

        // chromatic case
        double delta = maxVal - minVal;
        double h;
        if (r == maxVal) {
            h = (g - b) / delta;
        }
        else {
            if (g == maxVal) {
                h = 2.0 + (b - r) / delta;
            }
            else {
                h = 4.0 + (r - g) / delta;
            }
        }
        h *= 60.0;
        if (h < 0.0) {
            h += 360.0;
        }
        return new TripletHSV(h, s, v);
    }

    public static TripletRGB LAB_To_RGB(TripletLAB tripletLAB) {
        double X, Y, Z, fX, fY, fZ;
        int RR, GG, BB;

        fY = Math.pow((tripletLAB.l + 16.0) / 116.0, 3.0);
        if (fY < 0.008856) {
            fY = tripletLAB.l / 903.3;
        }
        Y = fY;

        if (fY > 0.008856) {
            fY = Math.pow(fY, 1.0 / 3.0);
        }
        else {
            fY = 7.787 * fY + 16.0 / 116.0;
        }

        fX = tripletLAB.a / 500.0 + fY;
        if (fX > 0.206893) {
            X = Math.pow(fX, 3.0);
        }
        else {
            X = (fX - 16.0 / 116.0) / 7.787;
        }

        fZ = fY - tripletLAB.b / 200.0;
        if (fZ > 0.206893) {
            Z = Math.pow(fZ, 3.0);
        }
        else {
            Z = (fZ - 16.0 / 116.0) / 7.787;
        }

        X *= (0.950456 * 255);
        Y *= 255;
        Z *= (1.088754 * 255);

        RR = (int) (3.240479 * X - 1.537150 * Y - 0.498535 * Z + 0.5);
        GG = (int) (-0.969256 * X + 1.875992 * Y + 0.041556 * Z + 0.5);
        BB = (int) (0.055648 * X - 0.204043 * Y + 1.057311 * Z + 0.5);

        int R = RR < 0 ? 0 : RR > 255 ? 255 : RR;
        int G = GG < 0 ? 0 : GG > 255 ? 255 : GG;
        int B = BB < 0 ? 0 : BB > 255 ? 255 : BB;
        return new TripletRGB(R, G, B);
    }


    public static TripletLAB RGB_To_LAB(TripletRGB tripletRGB) {
        double R = tripletRGB.r;
        double G = tripletRGB.g;
        double B = tripletRGB.b;

        double X, Y, Z, fX, fY, fZ;
        double L, a, b;

        X = 0.412453 * R + 0.357580 * G + 0.180423 * B;
        Y = 0.212671 * R + 0.715160 * G + 0.072169 * B;
        Z = 0.019334 * R + 0.119193 * G + 0.950227 * B;

        X /= (255 * 0.950456);
        Y /= 255;
        Z /= (255 * 1.088754);

        if (Y > 0.008856) {
            fY = Math.pow(Y, 1.0 / 3.0);
            L = 116.0 * fY - 16.0 + 0.5;
        }
        else {
            fY = 7.787 * Y + 16.0 / 116.0;
            L = 903.3 * Y + 0.5;
        }

        if (X > 0.008856) {
            fX = Math.pow(X, 1.0 / 3.0);
        }
        else {
            fX = 7.787 * X + 16.0 / 116.0;
        }

        if (Z > 0.008856) {
            fZ = Math.pow(Z, 1.0 / 3.0);
        }
        else {
            fZ = 7.787 * Z + 16.0 / 116.0;
        }

        a = 500.0 * (fX - fY) + 0.5;
        b = 200.0 * (fY - fZ) + 0.5;

        return new TripletLAB(L, a, b);
    }
}


