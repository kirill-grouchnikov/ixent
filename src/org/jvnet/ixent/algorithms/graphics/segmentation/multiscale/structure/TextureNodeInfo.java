package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure;

/**
 * Information holder on brightness
 *
 * @author Kirill Grouchnikov
 */
public class TextureNodeInfo extends BrightnessNodeInfo {
    private Histogram textonResponses;

    public TextureNodeInfo(double _x, double _y, Histogram _textonResponses) {
        // setLocation brightness equal to 0.0
        super(_x, _y, 0.0);
        this.textonResponses = _textonResponses;
    }

    /**
     * @return texton responses histogram
     */
    public Histogram getTextonResponses() {
        return textonResponses;
    }

    /**
     * @param textonResponses new texton responses histogram
     */
    public void setTextonResponses(Histogram textonResponses) {
        this.textonResponses = textonResponses;
    }
}
