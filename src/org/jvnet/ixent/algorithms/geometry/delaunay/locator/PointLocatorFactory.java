package org.jvnet.ixent.algorithms.geometry.delaunay.locator;

import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.math.coord.Rectangle2D;
import org.jvnet.ixent.math.coord.Vertex2D;

/**
 * Factory class for point locators
 *
 * @author Kirill Grouchnikov
 */
public class PointLocatorFactory {
	public static PointLocator instance;
    /**
     * Get single locator
     *
     * @param boundingRectangle bouding rectangle of input points
     * @param originalVertices  input points
     * @param superTriangle     encompassing triangle
     * @param isExact           whether to perform exact computations
     * @return point locator
     */
    public static PointLocator getLocator(Rectangle2D boundingRectangle,
                                          Vertex2D[] originalVertices, Triangle superTriangle,
                                          boolean isExact) {
//        PointLocator locator = new PointLocatorQuadTree();
        instance.init(boundingRectangle, originalVertices, superTriangle,
                isExact);
        return instance;
    }
}
