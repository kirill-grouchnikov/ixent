package org.jvnet.ixent.graphics.colors.manager;

import java.awt.Color;

public interface ColorManager {
    public enum ColorKind {
        light, medium, dark, master
    };

    public int getHexColor(int index);

    public Color getColor(int index);

    public Color getColor(ColorKind kind);
}

