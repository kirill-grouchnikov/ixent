package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure;

/**
 * Information holder on brightness
 *
 * @author Kirill Grouchnikov
 */
public class BrightnessNodeInfo extends AdditionalNodeInfo {
    private double brightness;

    public BrightnessNodeInfo(double _x, double _y, double _brightness) {
        super(_x, _y);
        this.brightness = _brightness;
    }

    /**
     * @return node brightness
     */
    public double getBrightness() {
        return brightness;
    }

    /**
     * @param brightness new node brightness
     */
    public void setBrightness(double brightness) {
        this.brightness = brightness;
    }
}
