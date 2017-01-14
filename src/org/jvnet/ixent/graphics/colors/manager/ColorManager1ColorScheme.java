package org.jvnet.ixent.graphics.colors.manager;

import java.awt.Color;

public class ColorManager1ColorScheme implements ColorManager {
    private int[] colorBytes;
    private Color[] colors;

    private Color masterColor;

    public ColorManager1ColorScheme(Color masterColor) {
        colorBytes = new int[256];
        colors = new Color[256];
        this.masterColor = masterColor;

        int masterR = masterColor.getRed();
        int masterG = masterColor.getGreen();
        int masterB = masterColor.getBlue();

        // first 240 - black-master
        // last 16 - master-white
        for (int i = 0; i < 240; i++) {
            int redComp = (int) ((double) (i) * masterR / 240);
            int greenComp = (int) ((double) (i) * masterG / 240);
            int blueComp = (int) ((double) (i) * masterB / 240);
            colorBytes[i] = (255 << 24) | (redComp << 16) | (greenComp << 8) |
                    blueComp;
            colors[i] = new Color(colorBytes[i]);
        }
        for (int i = 0; i < 16; i++) {
            int redComp = masterR + (255 - masterR) * i / 15;
            int greenComp = masterG + (255 - masterG) * i / 15;
            int blueComp = masterB + (255 - masterB) * i / 15;
            colorBytes[240 + i] = (255 << 24) | (redComp << 16) |
                    (greenComp << 8) |
                    blueComp;
            colors[240 + i] = new Color(colorBytes[240 + i]);
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
                return colors[20];
            case medium:
                return colors[200];
            case light:
                return colors[250];
        }
        return null;
    }
}

