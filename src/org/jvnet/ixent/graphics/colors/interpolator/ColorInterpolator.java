package org.jvnet.ixent.graphics.colors.interpolator;

import java.awt.Color;

public interface ColorInterpolator {
    // input: coef in [0, 1]
    public Color getInterpolatedColor(double coef);
}

