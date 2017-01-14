package org.jvnet.ixent.graphics.colors.manager;

import java.awt.Color;

import org.jvnet.ixent.graphics.colors.interpolator.ColorInterpolator;
import org.jvnet.ixent.graphics.colors.interpolator.ColorInterpolatorHSV;

public class ColorManager3ColorScheme implements ColorManager {
    private int[] colorBytes;
    private Color[] colors;
    private Color masterColor;

    public ColorManager3ColorScheme(Color masterColor, Color colorLight,
                                    Color colorMid, Color colorDark) {

        this.masterColor = masterColor;
        ColorInterpolator colorInterpolatorLight =
                new ColorInterpolatorHSV(colorMid, colorLight, 256);
        ColorInterpolator colorInterpolatorDark =
                new ColorInterpolatorHSV(colorDark, colorMid, 256);

        colorBytes = new int[256];
        colors = new Color[256];
        // first 128 - dark
        // last 128 - light
        for (int i = 0; i < 128; i++) {
            double coef = (double) (i) / 128.0;
            colors[i] = colorInterpolatorDark.getInterpolatedColor(coef);
//      System.out.println(i + ": " + colors[i]);
            colorBytes[i] = (255 << 24) | colors[i].getRGB();
        }
        for (int i = 0; i < 128; i++) {
            double coef = (double) (i) / 128.0;
            colors[128 + i] =
                    colorInterpolatorLight.getInterpolatedColor(coef);
//      System.out.println((128+i) + ": " + colors[128+i]);
            colorBytes[128 + i] = (255 << 24) | colors[128 + i].getRGB();
        }
    }

    public int getHexColor(int index) {
        return colorBytes[index];
    }

    public Color getColor(int index) {
        return colors[index];
    }

    public Color getColor(ColorKind kind) {
        switch (kind) {
            case master:
                return this.masterColor;
            case dark:
                return colors[10];
            case medium:
                return colors[128];
            case light:
                return colors[250];
        }
        return null;
    }
}

