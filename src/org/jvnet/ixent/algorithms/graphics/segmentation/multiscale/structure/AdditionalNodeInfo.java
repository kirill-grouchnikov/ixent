package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure;

/**
 * Abstract class - base class for additional information holders
 *
 * @author Kirill Grouchnikov
 */
public abstract class AdditionalNodeInfo {
    private double x;
    private double y;

    public AdditionalNodeInfo(double _x, double _y) {
        this.x = _x;
        this.y = _y;
    }

    /**
     * @return x coordinate of node
     */
    public double getX() {
        return x;
    }

    /**
     * @param x new x coordinate of node
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return y coordinate of node
     */
    public double getY() {
        return y;
    }

    /**
     * @param y new y coordinate of node
     */
    public void setY(double y) {
        this.y = y;
    }
}
