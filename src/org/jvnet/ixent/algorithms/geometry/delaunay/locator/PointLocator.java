package org.jvnet.ixent.algorithms.geometry.delaunay.locator;

import java.util.List;

import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.math.coord.Rectangle2D;
import org.jvnet.ixent.math.coord.Vertex2D;

public interface PointLocator {
    // isExact:
    //   true - locate triangle that contains the point
    //   false - locate any triangle whose circumcircle contains the point
    public void init(Rectangle2D boundingRectangle,
                     Vertex2D[] originalVertices, Triangle superTriangle,
                     boolean isExact);

    public Triangle locate(int vertexIndex);

    public void onDeleteTriangles(List<Triangle> triangles);

    public void onInsertTriangle(Triangle triangle);

    public void onReplaceTriangles(List<Triangle> oldTriangles,
                                   List<Triangle> newTriangles);

    public void dumpInfo();
}

