package org.jvnet.ixent.algorithms.geometry.delaunay;

import java.util.List;

import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.math.coord.Vertex2D;

/**
 * Interface class for Delaunay triangulations
 *
 * @author Kirill Grouchnikov
 */
public interface DelaunayManager {
    /**
     * Initialize this manager with a list of 2-D points
     *
     * @param vertices triangulation vertices
     */
    public void init(Vertex2D[] vertices);

    /**
     * Get a list of all triangles in this triangulation
     *
     * @return triangle list
     */
    public List<Triangle> getTriangulation();
}

