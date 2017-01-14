package org.jvnet.ixent.algorithms.graphics.colorreduction;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.jvnet.ixent.graphics.IndexBitmapObject;

/**
 * An interface for color reductors (quantizers)
 *
 * @author Kirill Grouchnikov
 */
public interface ColorReductor {
    /**
     * Initialize with Java image object
     *
     * @param image input image
     */
    public void init(BufferedImage image);

    /**
     * Process this image using the specified number of quantization colors
     *
     * @param numberOfQuantizationColors number of quantization colors in the
     *                                   resulting image
     */
    public void process(int numberOfQuantizationColors);

    /**
     * Return index 2-D array of quantized color indexes. Each value points to a
     * quantized color that best matches the original color of the pixel
     *
     * @return 2-D array of quantized color indexes. Each value points to a
     *         quantized color that best matches the original color of the
     *         pixel
     */
    public IndexBitmapObject getValueMap2D();

    /**
     * Return array of the resulting quantization colors
     *
     * @return array of the resulting quantization colors
     */
    public Color[] getQuantizationColors();
}
