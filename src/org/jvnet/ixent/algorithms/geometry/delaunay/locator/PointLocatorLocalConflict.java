package org.jvnet.ixent.algorithms.geometry.delaunay.locator;

import java.util.*;

import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.math.coord.Rectangle2D;
import org.jvnet.ixent.math.coord.Vertex2D;

public final class PointLocatorLocalConflict implements PointLocator {
    private Vertex2D[] originalVertices;
    private boolean isExact;

    // for each point stores the containing triangle (conflict)
    private Triangle[] localConflictGraph;

    // stores all points that were in conflict with deleted triangles
    private List<Integer> affectedPoints;

    public PointLocatorLocalConflict() {
    }

    // locatorHelperObject1 points to a list of IDs of all uninserted points that
    // are in conflict with this triangle

    public void init(Rectangle2D boundingRectangle,
                     Vertex2D[] originalVertices, Triangle superTriangle,
                     boolean isExact) {

        this.originalVertices = originalVertices;
        this.isExact = isExact;

        this.localConflictGraph = new Triangle[this.originalVertices.length];
        for (int i = 0; i < this.originalVertices.length; i++) {
            this.localConflictGraph[i] = superTriangle;
        }
        List<Integer> containedPoints = new LinkedList<Integer>();
        for (int i = 0; i < this.originalVertices.length; i++) {
            containedPoints.add(i);
        }
        superTriangle.setLocatorHelperObject1(containedPoints);

        this.affectedPoints = new LinkedList<Integer>();
    }

    public Triangle locate(int vertexIndex) {
        return this.localConflictGraph[vertexIndex];
    }

    public void onDeleteTriangles(List<Triangle> triangles) {
        for (Triangle currTriangle : triangles) {
            this.affectedPoints.addAll(
                    (List<Integer>) currTriangle.getLocatorHelperObject1());
        }
    }

    public void onInsertTriangle(Triangle triangle) {
        // update local conflicts graph
//        ObjectList.ObjectElement currElement = this.affectedPoints.head;
//        ObjectList.ObjectElement nextElement = null;
        Iterator<Integer> it = this.affectedPoints.iterator();
        while (it.hasNext()) {
//        while (currElement != null) {
//            nextElement = currElement.next;
            int originalIndex = it.next();
            this.localConflictGraph[originalIndex] = null;
            if (triangle.isPointInside(this.originalVertices[originalIndex])) {
                this.localConflictGraph[originalIndex] = triangle;
                it.remove();
//                this.affectedPoints.removeObject(currElement);
                List<Integer> conflictPoints = (List<Integer>) triangle.getLocatorHelperObject1();
                if (conflictPoints == null) {
                    conflictPoints = new LinkedList<Integer>();
                    triangle.setLocatorHelperObject1(conflictPoints);
                }
                conflictPoints.add(0, originalIndex);
            }
        }
    }

    public void onReplaceTriangles(List<Triangle> oldTriangles,
                                   List<Triangle> newTriangles) {
        this.onDeleteTriangles(oldTriangles);
        for (Triangle currTriangle : newTriangles) {
            this.onInsertTriangle(currTriangle);
        }
    }

    public void dumpInfo() {
    }
}

