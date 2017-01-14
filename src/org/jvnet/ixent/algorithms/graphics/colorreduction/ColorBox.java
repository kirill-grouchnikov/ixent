package org.jvnet.ixent.algorithms.graphics.colorreduction;


/**
 * @author Kirill Grouchnikov
 */
public class ColorBox implements Comparable<ColorBox> {
    private static int globalID = 0;

    private int id;
    private int redMin, redMax;
    private int greenMin, greenMax;
    private int blueMin, blueMax;

    private int pixelCount;

    public ColorBox(int pRedMin, int pRedMax, int pGreenMin, int pGreenMax,
                    int pBlueMin, int pBlueMax) {
        this.redMin = pRedMin;
        this.redMax = pRedMax;
        this.greenMin = pGreenMin;
        this.greenMax = pGreenMax;
        this.blueMin = pBlueMin;
        this.blueMax = pBlueMax;
        this.pixelCount = 0;
        this.id = ColorBox.globalID++;
    }

    public int getId() {
        return id;
    }

    public int getRedMin() {
        return redMin;
    }

    public int getRedMax() {
        return redMax;
    }

    public int getGreenMin() {
        return greenMin;
    }

    public int getGreenMax() {
        return greenMax;
    }

    public int getBlueMin() {
        return blueMin;
    }

    public int getBlueMax() {
        return blueMax;
    }

    public int getPixelCount() {
        return pixelCount;
    }

    public void incrementPixelCount() {
        this.pixelCount++;
    }

    public void setPixelCount(int pixelCount) {
        this.pixelCount = pixelCount;
    }

    public int getVolume() {
        return (this.redMax - this.redMin) * (this.greenMax - this.greenMin) *
                (this.blueMax - this.blueMin);
    }

    public boolean isColorInside(int rgb) {
        int red = rgb & 0x00FF0000;
        red >>>= 16;
        if ((red < this.redMin) || (red > this.redMax)) {
            return false;
        }
        int green = rgb & 0x0000FF00;
        green >>>= 8;
        if ((green < this.greenMin) || (green > this.greenMax)) {
            return false;
        }
        int blue = rgb & 0x000000FF;
        if ((blue < this.blueMin) || (blue > this.blueMax)) {
            return false;
        }
        return true;
    }

    /**
     * Compares this color box with the specified color box for order.  Returns
     * a negative integer, zero, or a positive integer as this color box is less
     * than, equal to, or greater than the specified object.<p>
     *
     * @param colorBox the color box to be compared.
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     */
    public int compareTo(ColorBox colorBox) {
        if (this == colorBox) {
            return 0;
        }
//        int volumeDiff = colorBox.getVolume() - this.getVolume();
//        if (volumeDiff != 0)
//            return volumeDiff;
        int countDiff = colorBox.pixelCount - this.pixelCount;
        if (countDiff != 0) {
            return countDiff;
        }
        return this.id - colorBox.id;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append("Color box (id ");
        res.append(this.id);
        res.append(", ");
        res.append(this.pixelCount);
        res.append(" pixels) : r [");
        res.append(this.redMin);
        res.append("-");
        res.append(this.redMax);
        res.append("], g [");
        res.append(this.greenMin);
        res.append("-");
        res.append(this.greenMax);
        res.append("], b [");
        res.append(this.blueMin);
        res.append("-");
        res.append(this.blueMax);
        res.append("]");
        return res.toString();
    }
}
