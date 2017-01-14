package org.jvnet.ixent.graphics.colors.interpolator;

import java.awt.Color;

import org.jvnet.ixent.graphics.colors.ColorConverter;

public class ColorInterpolatorHSV implements ColorInterpolator {
    private Color[] colors;
    private Color color1;
    private Color color2;
    private int count;

    public ColorInterpolatorHSV(Color color1, Color color2, int count) {
        this.color1 = color1;
        this.color2 = color2;
        this.count = count;

        this.colors = new Color[this.count + 1];
        double r1 = this.color1.getRed();
        double g1 = this.color1.getGreen();
        double b1 = this.color1.getBlue();
        ColorConverter.TripletHSV tripletHSV1 = ColorConverter.RGB_To_HSV(new ColorConverter.TripletRGB(
                r1 / 256.0, g1 / 256.0,
                b1 / 256.0));
        double h1 = tripletHSV1.h;
        double s1 = tripletHSV1.s;
        double v1 = tripletHSV1.v;
        double r2 = this.color2.getRed();
        double g2 = this.color2.getGreen();
        double b2 = this.color2.getBlue();
        ColorConverter.TripletHSV tripletHSV2 = ColorConverter.RGB_To_HSV(new ColorConverter.TripletRGB(
                r2 / 256.0, g2 / 256.0,
                b2 / 256.0));
        double h2 = tripletHSV2.h;
        double s2 = tripletHSV2.s;
        double v2 = tripletHSV2.v;
        boolean haveH = (tripletHSV1.h_defined && tripletHSV2.h_defined);
        for (int i = 0; i <= this.count; i++) {
            double h = h1 + (double) i * (h2 - h1) / (double) this.count;
            double s = s1 + (double) i * (s2 - s1) / (double) this.count;
            double v = v1 + (double) i * (v2 - v1) / (double) this.count;

            ColorConverter.TripletRGB tripletRGB = null;
            if (h>0) {
                tripletRGB = ColorConverter.HSV_To_RGB(
                    new ColorConverter.TripletHSV(h, s, v));
            }
            else {
                tripletRGB = ColorConverter.HSV_To_RGB(
                    new ColorConverter.TripletHSV(s, v));
            }

            this.colors[i] =
                    new Color((int) (256.0 * tripletRGB.r),
                            (int) (256.0 * tripletRGB.g),
                            (int) (256.0 * tripletRGB.b));
        }
    }

    // input: coef in [0, 1]
    public Color getInterpolatedColor(double coef) {
        int index = (int) (coef * this.count);
        if (index < 0) {
            index = 0;
        }
        if (index > this.count) {
            index = this.count;
        }

        return this.colors[index];
    }
}

