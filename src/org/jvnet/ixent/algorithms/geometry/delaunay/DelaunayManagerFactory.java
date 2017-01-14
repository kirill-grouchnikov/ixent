package org.jvnet.ixent.algorithms.geometry.delaunay;

import org.jvnet.ixent.math.coord.Vertex2D;

/**
 * @author Kirill Grouchnikov
 */
public class DelaunayManagerFactory {
	public static DelaunayManager instance;
	
    /**
     * Factory method that returns a Delaunay triangulator
     *
     * @param vertices triangulation vertices
     * @return Delaunay triangulator initialized with these vertices
     */
    public static DelaunayManager getDelaunayManager(Vertex2D[] vertices) {
        instance.init(vertices);
        return instance;
    }
}
