package org.jvnet.ixent.algorithms.graphics.colorreduction;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.ixent.graphics.IndexBitmapObject;

/**
 * Canny edge detector
 */
public class MedianCutColorReductor implements ColorReductor {
    /**
     * Input image width
     */
    private int width;

    /**
     * Input image height
     */
    private int height;

    private int[][] imPixels;

    private IndexBitmapObject quantizedBitmap;

    private Color[] quantizationColors;

    private Logger logger;

    /**
     * Constructor.
     */
    public MedianCutColorReductor() {
        this.logger =
                Logger.getLogger(
                        MedianCutColorReductor.class.getPackage().getName());
    }

    /**
     * @param bitmapObject input image as bitmap object
     */
    public void init(BufferedImage bitmapObject) {
        this.width = bitmapObject.getWidth();
        this.height = bitmapObject.getHeight();

        this.imPixels = new int[width][height];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                this.imPixels[col][row] = bitmapObject.getRGB(col, row);
            }
        }
    }

    /**
     * Return index 2-D array of quantized color indexes. Each value points to a
     * quantized color that best matches the original color of the pixel
     *
     * @return 2-D array of quantized color indexes. Each value points to a
     *         quantized color that best matches the original color of the
     *         pixel
     */
    public IndexBitmapObject getValueMap2D() {
        return this.quantizedBitmap;
    }

    /**
     * Return array of the resulting quantization colors
     *
     * @return array of the resulting quantization colors
     */
    public Color[] getQuantizationColors() {
        return this.quantizationColors;
    }

    private void updatePixels(int colorBoxIndexToCheck, ColorBox newColorBox) {
        int newColorBoxIndex = newColorBox.getId();
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                int prevIndex = this.quantizedBitmap.getValue(col, row);
                if (prevIndex == colorBoxIndexToCheck) {
                    if (newColorBox.isColorInside(this.imPixels[col][row])) {
                        this.quantizedBitmap.setValue(col, row,
                                newColorBoxIndex);
                        newColorBox.incrementPixelCount();
                    }
                }
            }
        }
    }

    /**
     * Process this image using the specified number of quantization colors
     *
     * @param numberOfQuantizationColors number of quantization colors in the
     *                                   resulting image
     */
    public void process(int numberOfQuantizationColors) {
        long time0 = System.currentTimeMillis();
        this.quantizedBitmap =
                new IndexBitmapObject(this.width, this.height, false, 0);
        // create global color box
        int minR = 255, maxR = 0;
        int minG = 255, maxG = 0;
        int minB = 255, maxB = 0;
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                int currRGB = this.imPixels[col][row];
                int red = currRGB & 0x00FF0000;
                red >>>= 16;
                minR = Math.min(minR, red);
                maxR = Math.max(maxR, red);
                int green = currRGB & 0x0000FF00;
                green >>>= 8;
                minG = Math.min(minG, green);
                maxG = Math.max(maxG, green);
                int blue = currRGB & 0x000000FF;
                minB = Math.min(minB, blue);
                maxB = Math.max(maxB, blue);
            }
        }
        SortedSet<ColorBox> allColorBoxes = new TreeSet<ColorBox>();
        ColorBox bigBox = new ColorBox(minR, maxR, minG, maxG, minB, maxB);
        bigBox.setPixelCount(this.width * this.height);
        this.quantizedBitmap.reset(bigBox.getId());
        allColorBoxes.add(bigBox);
        this.logger.finer("Created initial box " + bigBox.toString());

        while (allColorBoxes.size() < numberOfQuantizationColors) {
            // get a box with biggest volume
            ColorBox biggestColorBox = allColorBoxes.first();
            allColorBoxes.remove(biggestColorBox);
            // split it in two by biggest axis
            minR = biggestColorBox.getRedMin();
            maxR = biggestColorBox.getRedMax();
            minG = biggestColorBox.getGreenMin();
            maxG = biggestColorBox.getGreenMax();
            minB = biggestColorBox.getBlueMin();
            maxB = biggestColorBox.getBlueMax();
            int diffR = maxR - minR;
            int diffG = maxG - minG;
            int diffB = maxB - minB;
            ColorBox newColorBox1 = null;
            ColorBox newColorBox2 = null;
            if ((diffR >= diffG) && (diffR >= diffB)) {
                // split by red
                int midR = (minR + maxR) / 2;
                newColorBox1 =
                        new ColorBox(minR, midR, minG, maxG, minB, maxB);
                newColorBox2 =
                        new ColorBox(midR + 1, maxR, minG, maxG, minB, maxB);
            }
            else {
                if ((diffG >= diffR) && (diffG >= diffB)) {
                    // split by green
                    int midG = (minG + maxG) / 2;
                    newColorBox1 =
                            new ColorBox(minR, maxR, minG, midG, minB, maxB);
                    newColorBox2 =
                            new ColorBox(minR, maxR, midG + 1, maxG, minB, maxB);
                }
                else {
                    // split by blue
                    int midB = (minB + maxB) / 2;
                    newColorBox1 =
                            new ColorBox(minR, maxR, minG, maxG, minB, midB);
                    newColorBox2 =
                            new ColorBox(minR, maxR, minG, maxG, midB + 1, maxB);
                }
            }
            int oldColorBoxId = biggestColorBox.getId();
            this.updatePixels(oldColorBoxId, newColorBox1);
            this.updatePixels(oldColorBoxId, newColorBox2);
            allColorBoxes.add(newColorBox1);
            allColorBoxes.add(newColorBox2);
            for (int col = 0; col < this.width; col++) {
                for (int row = 0; row < this.height; row++) {
                    assert this.quantizedBitmap.getValue(col, row) !=
                            oldColorBoxId : "Pixel [" + col + ", " + row +
                            "] still associated with the old color box";
                }
            }
            if (this.logger.isLoggable(Level.FINEST)) {
                this.logger.finest(
                        "Replaced box " + biggestColorBox.toString());
                this.logger.finest(" 1 : " + newColorBox1.toString());
                this.logger.finest(" 2 : " + newColorBox2.toString());
            }
            assert biggestColorBox.getPixelCount() == (newColorBox1.getPixelCount() +
                    newColorBox2.getPixelCount()) :
                    "Pixels not associated correctly with new color boxes";
        }

        // here we have exactly the desired number of color boxes
        int index = 0;
        this.quantizationColors = new Color[numberOfQuantizationColors];
        Map<Integer, Integer> colorBoxIdMapping =
                new HashMap<Integer, Integer>();
        for (ColorBox currColorBox : allColorBoxes) {
            // compute average color
            int midR = (currColorBox.getRedMin() + currColorBox.getRedMax()) /
                    2;
            int midG = (currColorBox.getGreenMin() +
                    currColorBox.getGreenMax()) / 2;
            int midB = (currColorBox.getBlueMin() + currColorBox.getBlueMax()) /
                    2;
            this.quantizationColors[index] = new Color(midR, midG, midB);
            colorBoxIdMapping.put(currColorBox.getId(), index);
            index++;
        }
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                this.quantizedBitmap.setValue(col, row,
                        colorBoxIdMapping.get(
                                this.quantizedBitmap.getValue(col, row)));
            }
        }
        long time1 = System.currentTimeMillis();
        this.logger.info("Median cut quantization : " + (time1 - time0));
    }
}

