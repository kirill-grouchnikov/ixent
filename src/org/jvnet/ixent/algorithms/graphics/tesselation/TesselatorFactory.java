package org.jvnet.ixent.algorithms.graphics.tesselation;

/**
 * Factory for tesselators
 *
 * @author Kirill Grouchnikov
 */
public class TesselatorFactory {
	public static Tesselator instance;
    /**
     * Return a new instance of tesselator
     *
     * @return new instance of tesselator
     */
    public static Tesselator getTesselator() {
//        return new DelaunayTesselator();
        return instance;
    }
}
