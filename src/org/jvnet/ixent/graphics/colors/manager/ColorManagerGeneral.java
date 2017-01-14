package org.jvnet.ixent.graphics.colors.manager;

import java.awt.Color;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import org.jvnet.ixent.graphics.colors.interpolator.ColorInterpolator;
import org.jvnet.ixent.graphics.colors.interpolator.ColorInterpolatorRGB;

/**
 * @author Kirill Grouchnikov
 */
public class ColorManagerGeneral implements ColorManager {
    private int[] colorBytes;
    private Color[] colors;
    private Map<ColorKind, Integer> markerIndexes;
    private Logger logger;


    private class ColorIndex implements Comparable<ColorIndex> {
        public Color color;
        public int index;

        public ColorIndex(Color pColor, int pIndex) {
            this.color = pColor;
            this.index = pIndex;
        }

        /**
         * Compares this object with the specified color index for order.
         * Returns a negative integer, zero, or a positive integer as this
         * object is less than, equal to, or greater than the specified
         * object.<p>
         *
         * @param colorIndex the ColorIndex to be compared.
         * @return a negative integer, zero, or a positive integer as this
         *         object is less than, equal to, or greater than the specified
         *         object.
         */
        public int compareTo(ColorIndex colorIndex) {
            return this.index - colorIndex.index;
        }

        public String toString() {
            return "[" + this.index + "] " + this.color.toString();
        }
    }

    public ColorManagerGeneral(Map<Color, Integer> primaryColors,
                               Map<ColorKind, Integer> pMarkerIndexes) {
        if ((primaryColors == null) || (pMarkerIndexes == null)) {
            throw new IllegalArgumentException("Can't pass null parameters");
        }

        // check markers
        for (ColorKind colorKind : ColorKind.values()) {
            if (!pMarkerIndexes.containsKey(colorKind)) {
                throw new IllegalArgumentException("No index specified for " +
                        colorKind.name());
            }
        }
        this.markerIndexes = pMarkerIndexes;

        this.logger =
                Logger.getLogger(
                        ColorManagerGeneral.class.getPackage().getName());

        this.colorBytes = new int[256];
        this.colors = new Color[256];

        // create an array of primary colors
        int primaryColorsCount = primaryColors.size();
        ColorIndex[] colorIndexes = new ColorIndex[primaryColorsCount];
        int count = 0;
        for (Color color : primaryColors.keySet()) {
            int index = primaryColors.get(color);
            colorIndexes[count++] = new ColorIndex(color, index);
        }
        // sort
        Arrays.sort(colorIndexes);

        if ((colorIndexes[0].index != 0) ||
                (colorIndexes[primaryColorsCount - 1].index != 255)) {
            throw new IllegalArgumentException(
                    "Primary colors must cover the entire 0..255 range");
        }

        for (int currPair = 0;
             currPair < (primaryColorsCount - 1); currPair++) {
            int startIndex = colorIndexes[currPair].index;
            int endIndex = colorIndexes[currPair + 1].index;
            int spanSize = endIndex - startIndex;
            this.logger.fine("Interpolating " + colorIndexes[currPair] +
                    " and " +
                    colorIndexes[currPair + 1]);
            ColorInterpolator currColorInterpolator =
                    new ColorInterpolatorRGB(colorIndexes[currPair].color,
                            colorIndexes[currPair + 1].color,
                            spanSize);
            for (int index = startIndex; index <= endIndex; index++) {
                double coef = (double) (index - startIndex) / (double) spanSize;
                this.colors[index] =
                        currColorInterpolator.getInterpolatedColor(coef);
                this.logger.fine("At index " + index + " is " + colors[index]);
                this.colorBytes[index] = (255 << 24) | colors[index].getRGB();
            }
        }
    }


    public int getHexColor(int index) {
        return this.colorBytes[index];
    }

    public Color getColor(int index) {
        return this.colors[index];
    }

    public Color getColor(ColorKind kind) {
        return this.colors[this.markerIndexes.get(kind)];
    }
}
