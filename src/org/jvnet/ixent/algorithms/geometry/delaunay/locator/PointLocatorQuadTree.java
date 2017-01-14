package org.jvnet.ixent.algorithms.geometry.delaunay.locator;

import java.util.List;

import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.math.coord.Rectangle2D;
import org.jvnet.ixent.math.coord.Vertex2D;
import org.jvnet.ixent.util.QuadTree;

public final class PointLocatorQuadTree implements PointLocator {
    private Rectangle2D boundingRectangle;
    private Vertex2D[] originalVertices;
    private QuadTree quadTree;

    public PointLocatorQuadTree() {
    }

    // locatorHelperObject1 points to a list of all triangles of the quad tree node
    // locatorHelperObject2 points to a specific node in that list

    // isExact:
    //   true - locate triangle that contains the point
    //   false - locate any triangle whose circumcircle contains the point
    public void init(Rectangle2D boundingRectangle,
                     Vertex2D[] originalVertices, Triangle superTriangle,
                     boolean isExact) {

        this.boundingRectangle = boundingRectangle;
        this.originalVertices = originalVertices;
        int levels = (int) (Math.log(originalVertices.length) /
                (2 * Math.log(2.0)));
        if (levels < 1) {
            levels = 1;
        }
        this.quadTree = new QuadTree(boundingRectangle, levels, isExact);
        this.quadTree.insert(superTriangle);
    }

    public Triangle locate(int vertexIndex) {
//    System.out.println("quad looking for: " + this.originalVertices[vertexIndex]);
        return this.quadTree.locate(this.originalVertices[vertexIndex]);
    }

    public void onDeleteTriangles(List<Triangle> triangles) {
        for (Triangle currTriangle : triangles) {
            this.quadTree.delete(currTriangle);
        }
    }

    public void onInsertTriangle(Triangle triangle) {
        this.quadTree.insert(triangle);
    }

    public void onReplaceTriangles(List<Triangle> oldTriangles,
                                   List<Triangle> newTriangles) {
        this.onDeleteTriangles(oldTriangles);
        for (Triangle currTriangle : newTriangles) {
            this.onInsertTriangle(currTriangle);
        }
    }

    public void dumpInfo() {
        this.quadTree.dumpInfo();
    }
}

