package org.jvnet.ixent.algorithms.graphics.colorreduction;

import java.awt.image.BufferedImage;

/**
 * Factory for color reductors
 *
 * @author Kirill Grouchnikov
 */
public class ColorReductorFactory {
	public static ColorReductor instance; 
    /**
     * Method for retrieving color reductor
     *
     * @param inputObject input image
     * @return color reductor
     */
    public static ColorReductor getColorReductor(
    		BufferedImage inputObject) {
        instance.init(inputObject);
        return instance;
    }
}
