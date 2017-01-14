package org.jvnet.ixent.algorithms.geometry.delaunay;

import java.util.Iterator;
import java.util.LinkedList;

import org.jvnet.ixent.graphics.objects.TriangleEdge;

public final class DelaunayEdgeList extends LinkedList<TriangleEdge> {
    public DelaunayEdgeList() {
        super();
    }

    public void addFirst(TriangleEdge edge) {
        super.addFirst(edge);
    }

    public void addLast(TriangleEdge edge) {
        super.addLast(edge);
    }

    public void removeNonBoundaryEdge(long id) {
        // search for it
        Iterator<TriangleEdge> it = this.iterator();
        while (it.hasNext()) {
            TriangleEdge currEdge = it.next();
            if (currEdge.getID() == id) {
                if (!currEdge.isBoundary()) {
                    it.remove();
                }
                return;
            }
        }
    }

    public boolean exists(long id) {
        // search for it
        Iterator<TriangleEdge> it = this.iterator();
        while (it.hasNext()) {
            TriangleEdge currEdge = it.next();
            if (currEdge.getID() == id) {
                return true;
            }
        }
        return false;
    }
}
