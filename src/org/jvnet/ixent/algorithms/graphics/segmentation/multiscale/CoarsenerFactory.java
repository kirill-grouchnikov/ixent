package org.jvnet.ixent.algorithms.graphics.segmentation.multiscale;

import java.awt.image.BufferedImage;

import org.jvnet.ixent.algorithms.graphics.segmentation.multiscale.structure.GraphInfo;

/**
 * A factory for coarsener creation
 *
 * @author Kirill Grouchnikov
 */
public class CoarsenerFactory {
	public static Coarsener instance;
    /**
     * Getter of factory
     *
     * @param graph input graph
     * @return coarsener
     */
    public static Coarsener getCoarsener(BufferedImage originalImage,
                                         GraphInfo graph) {
//        return new AggregateCoarsener(originalImage, graph);
//        return new BrightnessCoarsener(originalImage, graph);
    	instance.init(originalImage, graph);
    	return instance;
    }
}
