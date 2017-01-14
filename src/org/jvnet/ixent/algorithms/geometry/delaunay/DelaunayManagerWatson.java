package org.jvnet.ixent.algorithms.geometry.delaunay;

import java.util.*;
import java.util.logging.Logger;

import org.jvnet.ixent.algorithms.geometry.delaunay.locator.PointLocator;
import org.jvnet.ixent.algorithms.geometry.delaunay.locator.PointLocatorFactory;
import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.graphics.objects.TriangleEdge;
import org.jvnet.ixent.math.coord.*;

/**
 * Watson implementation of Delaunay triangulation.
 *
 * @author Kirill Grouchnikov
 */
public final class DelaunayManagerWatson implements DelaunayManager {
    private Rectangle2D boundingRectangle;
    private Vertex2D[] originalVertices;

    private SortedMap<Long, Triangle> triangleTree;

    private LinkedList<Triangle> resultList;

    private PointLocator pointLocator;

    private Vertex2D pointT;
    private Vertex2D pointL;

    private Vertex2D pointR;

    private Logger logger;

    /**
     * Constructor.
     */
    public DelaunayManagerWatson() {
        this.logger =
                Logger.getLogger(
                        DelaunayManagerWatson.class.getPackage().getName());
    }

    /**
     * Initialize this manager with a list of 2-D points
     *
     * @param vertices triangulation vertices
     */
    public void init(Vertex2D[] vertices) {
        this.originalVertices = vertices;
        this.boundingRectangle = Geom2DHelper.getBoundingRectangle(vertices);

        this.triangleTree = new TreeMap<Long, Triangle>();
        // add "super triangle"
        double w = this.boundingRectangle.getPointBR().getX() -
                this.boundingRectangle.getPointTL().getX();
        double h = this.boundingRectangle.getPointBR().getY() -
                this.boundingRectangle.getPointTL().getY();
        this.pointT = new Vertex2D(
                this.boundingRectangle.getPointTL().getX() + w / 2,
                this.boundingRectangle.getPointTL().getY() - 2.5 * h);
        this.pointL = new Vertex2D(
                this.boundingRectangle.getPointTL().getX() - w,
                this.boundingRectangle.getPointBR().getY() + h);
        this.pointR = new Vertex2D(
                this.boundingRectangle.getPointBR().getX() + w,
                this.boundingRectangle.getPointBR().getY() + h);

        TriangleEdge edgeTL = new TriangleEdge(true, this.pointT, this.pointL);
        TriangleEdge edgeTR = new TriangleEdge(true, this.pointT, this.pointR);
        TriangleEdge edgeLR = new TriangleEdge(true, this.pointL, this.pointR);

        Triangle superTriangle = new Triangle(edgeTL, edgeTR, edgeLR);

        edgeTL.setTriangle1(superTriangle);
        edgeTL.setTriangle2(null);
        edgeTR.setTriangle1(superTriangle);
        edgeTR.setTriangle2(null);
        edgeLR.setTriangle1(superTriangle);
        edgeLR.setTriangle2(null);

        this.triangleTree.put(superTriangle.getID(), superTriangle);

        this.pointLocator = PointLocatorFactory.getLocator(
                this.boundingRectangle,
                this.originalVertices, superTriangle, true);
    }

    /**
     * Return all triangles that contain given vertex. The search is started
     * from given triangle and expands to all neighbours of containing triangles
     * recursively. The contain check is performed using current triangle's
     * circumscribing circle
     *
     * @param triangleTree the resulting collection of containing triangles
     * @param newTriangle  the triangle to start from
     * @param vertex       the vertex to check
     */
    private void findContainingTriangles(Map<Long, Triangle> triangleTree,
                                         Triangle newTriangle, Vertex2D vertex) {

        if (newTriangle == null) {
            return;
        }

        // check if this triangle's circumcircle contains the vertex
        if (!newTriangle.isPointInCircumcircle(vertex)) {
            return;
        }

        // check if this triangle already is in the list
        if (triangleTree.containsKey(newTriangle.getID())) {
            return;
        }

        // add to list
        triangleTree.put(newTriangle.getID(), newTriangle);

        // recursively call for all neighbours
        this.findContainingTriangles(triangleTree,
                newTriangle.getEdge1().getNeigbour(newTriangle.getID()),
                vertex);
        this.findContainingTriangles(triangleTree,
                newTriangle.getEdge2().getNeigbour(newTriangle.getID()),
                vertex);
        this.findContainingTriangles(triangleTree,
                newTriangle.getEdge3().getNeigbour(newTriangle.getID()),
                vertex);
    }

    /**
     * Update the list of affected edges (the "outer" envelope)
     *
     * @param affectedEdges the "outer" envelope of currently processed
     *                      triangles whose circumcircle contains given point
     * @param triangleId    currently processed triangle
     * @param edge          currently processed edge
     */
    private void updateAffectedEdgeList(DelaunayEdgeList affectedEdges,
                                        long triangleId, TriangleEdge edge) {
        if (affectedEdges.exists(edge.getID())) {
            // if this edge is in the list, it means that we have removed one of its
            // attached triangles. As we are about to removeByID the second triangle, we
            // removeByID this edge from the list, but only if it isn't boundary
            affectedEdges.removeNonBoundaryEdge(edge.getID());
        }
        else {
            // removeByID this triangle from the edge
            edge.removeNeigbour(triangleId);
            // if this edge still has attached triangles or is boundary edge
            // add it to the list
            if ((edge.getTriangleCount() > 0) || edge.isBoundary()) {
                affectedEdges.addFirst(edge);
            }
        }
    }

    /**
     * Create a "contourized" version of given edge list. The resulting list
     * contains edges that form closed non self-intersecting polygon
     *
     * @param edgeList input edge list
     * @return a contourized closed non self-intersecting polygon version of the
     *         input edge list
     */
    private DelaunayEdgeList contourize(DelaunayEdgeList edgeList) {
        if (edgeList == null) {
            return null;
        }
        if (edgeList.size() < 3) {
            return null;
        }
        DelaunayEdgeList result = new DelaunayEdgeList();
        TriangleEdge firstEdge = edgeList.getFirst();
        result.addFirst(firstEdge);
        // remove first element
        edgeList.remove(0);
        while (edgeList.size() > 0) {
            // find any edge connected to the last edge in 'result'
            TriangleEdge lastEdge = result.getLast();
            boolean nextContourEdgeFound = false;
            Iterator<TriangleEdge> it = edgeList.iterator();
            while (it.hasNext()) {
                TriangleEdge currEdge = it.next();
                if (lastEdge.connectsWith(currEdge)) {
                    result.addLast(currEdge);
                    it.remove();
                    nextContourEdgeFound = true;
                    break;
                }
                currEdge.flipVertices();
                if (lastEdge.connectsWith(currEdge)) {
                    result.addLast(currEdge);
                    it.remove();
                    nextContourEdgeFound = true;
                    break;
                }
            }
            if (nextContourEdgeFound == false) {
                this.logger.warning("couldn't find contour");
                return null;
            }
        }
        // check that the first and the last connect
        if (!result.getLast().connectsWith(result.getFirst())) {
            this.logger.warning("couldn't close contour");
            return null;
        }

        return result;
    }

    /**
     * Remove all triangles that have one of the initial super-triangle's
     * vertices as one of their vertices
     */
    private void removeOuterTriangles() {
        Iterator<Triangle> it = this.resultList.iterator();

        while (it.hasNext()) {
            Triangle currTriangle = it.next();
            boolean toRemoveCurr = false;

            if (currTriangle.hasPoint(this.pointT)) {
                toRemoveCurr = true;
            }
            if (currTriangle.hasPoint(this.pointL)) {
                toRemoveCurr = true;
            }
            if (currTriangle.hasPoint(this.pointR)) {
                toRemoveCurr = true;
            }

            if (!toRemoveCurr) {
                continue;
            }

            currTriangle.removeAsNeighbour();
            it.remove();
        }
    }

    /**
     * Add one vertex to the data structures. Guaranteed to perform in
     * <i>O(N*log(N))</i> time where <i>N</i> is the total number of vertices in
     * the final triangulation
     *
     * @param vertex new vertex
     * @param index  its index
     * @return <code>true</code> if succeeded, <code>false</code> if failed
     */
    private boolean addSingleVertex(Vertex2D vertex, int index) {
        // find the triangle that contains this vertex
        Triangle startTriangle = this.pointLocator.locate(index);
        if (startTriangle == null) {
            this.logger.warning("No triangle circumscribing vertex " + index +
                    " " + vertex.toString() + " in Delaunay");
            return false;
        }

        // Recursively find all the triangles whose circumcircle contains this vertex.
        // Start with the triangle that contains it
        Map<Long, Triangle> contTrianglesTree = new TreeMap<Long, Triangle>();
        this.findContainingTriangles(contTrianglesTree, startTriangle, vertex);
        List<Triangle> contTriangles = new LinkedList<Triangle>();

        for (Triangle currTriangle : contTrianglesTree.values()) {
            contTriangles.add(currTriangle);
        }

        // Go over these triangles and create a list of affected edges
        DelaunayEdgeList affectedEdges = new DelaunayEdgeList();
        for (Triangle currTriangle : contTriangles) {
            this.updateAffectedEdgeList(affectedEdges, currTriangle.getID(),
                    currTriangle.getEdge1());
            this.updateAffectedEdgeList(affectedEdges, currTriangle.getID(),
                    currTriangle.getEdge2());
            this.updateAffectedEdgeList(affectedEdges, currTriangle.getID(),
                    currTriangle.getEdge3());
        }

        // Throw away all the triangles in this list
        for (Triangle currTriangle : contTriangles) {
            this.triangleTree.remove(currTriangle.getID());
        }

        // Create the "contour" of the hole
        DelaunayEdgeList contourList = this.contourize(affectedEdges);
        if (contourList == null) {
            return false;
        }

        // Create triangles
        TriangleEdge firstEdgeFromContourInside = null;
        TriangleEdge prevEdgeFromContourInside = null;
        Triangle currTriangle = null;
        List<Triangle> newTriangles = new LinkedList<Triangle>();
        TriangleEdge lastContourEdge = contourList.getLast();
        for (TriangleEdge currContourEdge : contourList) {
            if (prevEdgeFromContourInside == null) {
                // this is the first contour edge
                firstEdgeFromContourInside = new TriangleEdge(false,
                        currContourEdge.getVertex1(), vertex);
                prevEdgeFromContourInside = firstEdgeFromContourInside;
            }

            TriangleEdge nextEdgeFromContourInside = null;
            if (currContourEdge == lastContourEdge) {
                // take the first edge created
                nextEdgeFromContourInside = firstEdgeFromContourInside;
            }
            else {
                // create a new one
                nextEdgeFromContourInside = new TriangleEdge(false,
                        currContourEdge.getVertex2(), vertex);
            }

            // create triangle
            currTriangle =
                    new Triangle(currContourEdge, prevEdgeFromContourInside,
                            nextEdgeFromContourInside);

            // add this triangle
            this.triangleTree.put(currTriangle.getID(), currTriangle);

            newTriangles.add(currTriangle);

            prevEdgeFromContourInside = nextEdgeFromContourInside;
        }
        // update vertex locator
        this.pointLocator.onReplaceTriangles(contTriangles, newTriangles);
        return true;
    }

    /**
     * Get a list of all triangles in this triangulation
     *
     * @return triangle list
     */
    public List<Triangle> getTriangulation() {
        long time0 = System.currentTimeMillis();
        if (this.originalVertices != null) {
            for (int i = 0; i < this.originalVertices.length; i++) {
                if (!this.addSingleVertex(this.originalVertices[i], i)) {
                    return null;
                }
            }
        }

        this.resultList = new LinkedList<Triangle>();
        for (Triangle currTriangle : this.triangleTree.values()) {
            this.resultList.add(currTriangle);
        }
        this.removeOuterTriangles();
        long time1 = System.currentTimeMillis();
        this.logger.fine("Delaunay triangulation (" +
                this.originalVertices.length +
                " vertices) : " + (time1 - time0));
        return this.resultList;
    }
}

