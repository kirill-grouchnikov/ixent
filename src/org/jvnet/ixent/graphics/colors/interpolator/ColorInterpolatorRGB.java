package org.jvnet.ixent.graphics.colors.interpolator;

import java.awt.Color;

public class ColorInterpolatorRGB implements ColorInterpolator {
    private Color[] colors;
    private Color color1;
    private Color color2;
    private int count;

    public ColorInterpolatorRGB(Color color1, Color color2, int count) {
        this.color1 = color1;
        this.color2 = color2;
        this.count = count;

        this.colors = new Color[this.count + 1];
        double r1 = this.color1.getRed();
        double g1 = this.color1.getGreen();
        double b1 = this.color1.getBlue();
        double r2 = this.color2.getRed();
        double g2 = this.color2.getGreen();
        double b2 = this.color2.getBlue();
        for (int i = 0; i <= this.count; i++) {
            double r = r1 + (double) i * (r2 - r1) / (double) this.count;
            double g = g1 + (double) i * (g2 - g1) / (double) this.count;
            double b = b1 + (double) i * (b2 - b1) / (double) this.count;
            this.colors[i] = new Color((int) r, (int) g, (int) b);
//      System.out.println(i + ": " + r + ", " + g + ", " + b);
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
        //   System.out.println(index + " - " + this.colors[index]);

        return this.colors[index];
    }
}

