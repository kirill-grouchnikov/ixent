package org.jvnet.ixent.algorithms.geometry.delaunay;

import java.util.*;
import java.util.logging.Logger;

import org.jvnet.ixent.algorithms.geometry.delaunay.locator.PointLocator;
import org.jvnet.ixent.algorithms.geometry.delaunay.locator.PointLocatorFactory;
import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.graphics.objects.TriangleEdge;
import org.jvnet.ixent.math.coord.*;

/**
 * Egde-flip implementation of Delaunay triangulation.
 *
 * @author Kirill Grouchnikov
 */
public final class DelaunayManagerEdgeFlip implements DelaunayManager {
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
     * Inner class that holds information on a single edge and its contributing
     * triangle in a star link
     */
    private static class StarLinkInfo {
        public Triangle starTriangle;
        public TriangleEdge linkEdge;

        public StarLinkInfo(Triangle starTriangle, TriangleEdge linkEdge) {
            this.starTriangle = starTriangle;
            this.linkEdge = linkEdge;
        }
    }

    /**
     * Constructor.
     */
    public DelaunayManagerEdgeFlip() {
        this.logger =
                Logger.getLogger(
                        DelaunayManagerEdgeFlip.class.getPackage().getName());
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
            this.logger.warning(
                    "No triangle circumscribing vertex in Delaunay");
            return false;
        }

        // Here we have several cases:
        //  1. new vertex is a vertex of existing node - ignore it
        //  2. new vertex lies on boundary edge - split triangle in two
        //  3. new vertex lies on inner edge - split two triangles in four
        //  4. new vertex lies inside the triangle - split triangle in three

        // *** case 1
        if (startTriangle.hasPoint(vertex)) {
            return true;
        }

        // allocate growing star (star and link of the new vertex)
        Stack<StarLinkInfo> growingStar = new Stack<StarLinkInfo>();
        // *** cases 2 and 3
        if (startTriangle.hasPointOnEdge(vertex)) {
            TriangleEdge edgeWithPoint = startTriangle.getEdgeContainingPoint(
                    vertex);
            if (edgeWithPoint == null) {
                this.logger.warning("vertex on edge but didn't get edge");
                return false;
            }
            if (edgeWithPoint.isBoundary()) {
                // *** case 2

                // replace the old triangle with two new ones
                this.triangleTree.remove(startTriangle.getID());

                TriangleEdge eOut1 = startTriangle.getAdjacentEdgeByVertex(
                        edgeWithPoint.getVertex1().getID(),
                        edgeWithPoint.getID());
                TriangleEdge eOut2 = startTriangle.getAdjacentEdgeByVertex(
                        edgeWithPoint.getVertex2().getID(),
                        edgeWithPoint.getID());

                startTriangle.removeAsNeighbour();

                TriangleEdge eInMid = new TriangleEdge(false,
                        startTriangle.getOpposingVertex(edgeWithPoint), vertex);
                TriangleEdge eIn3_1 = new TriangleEdge(true,
                        edgeWithPoint.getVertex1(), vertex);
                TriangleEdge eIn3_2 = new TriangleEdge(true,
                        edgeWithPoint.getVertex2(), vertex);

                Triangle tIn1 = new Triangle(eOut1, eInMid, eIn3_1);
                Triangle tIn2 = new Triangle(eOut2, eInMid, eIn3_2);

                List<Triangle> addList = new LinkedList<Triangle>();
                addList.add(tIn1);
                addList.add(tIn2);
                List<Triangle> delList = new LinkedList<Triangle>();
                delList.add(startTriangle);
                this.pointLocator.onReplaceTriangles(delList, addList);

                growingStar.push(new StarLinkInfo(tIn1, eOut1));
                growingStar.push(new StarLinkInfo(tIn2, eOut2));
            }
            else {
                // *** case 3
                Triangle opposingTriangle = edgeWithPoint.getAnotherNeighbour(
                        startTriangle);
                if (opposingTriangle == null) {
                    this.logger.warning(
                            "opposing triangle null in 2->4 in Delaunay");
                    return false;
                }

                // replace the old triangles with two four ones
                this.triangleTree.remove(startTriangle.getID());
                this.triangleTree.remove(opposingTriangle.getID());

                TriangleEdge eOut_st1 = startTriangle.getAdjacentEdgeByVertex(
                        edgeWithPoint.getVertex1().getID(),
                        edgeWithPoint.getID());
                TriangleEdge eOut_st2 = startTriangle.getAdjacentEdgeByVertex(
                        edgeWithPoint.getVertex2().getID(),
                        edgeWithPoint.getID());
                TriangleEdge eOut_opp1 = opposingTriangle.getAdjacentEdgeByVertex(
                        edgeWithPoint.getVertex1().getID(),
                        edgeWithPoint.getID());
                TriangleEdge eOut_opp2 = opposingTriangle.getAdjacentEdgeByVertex(
                        edgeWithPoint.getVertex2().getID(),
                        edgeWithPoint.getID());

                startTriangle.removeAsNeighbour();
                opposingTriangle.removeAsNeighbour();

                TriangleEdge eInSt = new TriangleEdge(false,
                        startTriangle.getOpposingVertex(edgeWithPoint), vertex);
                TriangleEdge eInOpp = new TriangleEdge(false,
                        opposingTriangle.getOpposingVertex(edgeWithPoint),
                        vertex);

                TriangleEdge eInMid1 = new TriangleEdge(false,
                        edgeWithPoint.getVertex1(), vertex);
                TriangleEdge eInMid2 = new TriangleEdge(false,
                        edgeWithPoint.getVertex2(), vertex);

                Triangle tIn1 = new Triangle(eOut_opp1, eInOpp, eInMid1);
                Triangle tIn2 = new Triangle(eOut_opp2, eInOpp, eInMid2);
                Triangle tIn3 = new Triangle(eOut_st1, eInSt, eInMid1);
                Triangle tIn4 = new Triangle(eOut_st2, eInSt, eInMid2);

                List<Triangle> addList1 = new LinkedList<Triangle>();
                addList1.add(tIn1);
                addList1.add(tIn2);
                List<Triangle> delList1 = new LinkedList<Triangle>();
                delList1.add(opposingTriangle);
                this.pointLocator.onReplaceTriangles(delList1, addList1);

                List<Triangle> addList2 = new LinkedList<Triangle>();
                addList2.add(tIn3);
                addList2.add(tIn4);
                List<Triangle> delList2 = new LinkedList<Triangle>();
                delList2.add(startTriangle);
                this.pointLocator.onReplaceTriangles(delList2, addList2);

                growingStar.push(new StarLinkInfo(tIn1, eOut_opp1));
                growingStar.push(new StarLinkInfo(tIn2, eOut_opp2));
                growingStar.push(new StarLinkInfo(tIn3, eOut_st1));
                growingStar.push(new StarLinkInfo(tIn4, eOut_st2));
            }
        }
        else {
            // *** case 4

            // replace the old triangle with three new ones
            this.triangleTree.remove(startTriangle.getID());

            TriangleEdge eOut1 = startTriangle.getEdge1();
            TriangleEdge eOut2 = startTriangle.getEdge2();
            TriangleEdge eOut3 = startTriangle.getEdge3();

            startTriangle.removeAsNeighbour();

            TriangleEdge eIn12 = new TriangleEdge(false,
                    startTriangle.getVertex12(), vertex);
            TriangleEdge eIn13 = new TriangleEdge(false,
                    startTriangle.getVertex13(), vertex);
            TriangleEdge eIn23 = new TriangleEdge(false,
                    startTriangle.getVertex23(), vertex);

            Triangle tIn1 = new Triangle(eOut1, eIn12, eIn13);
            Triangle tIn2 = new Triangle(eOut2, eIn12, eIn23);
            Triangle tIn3 = new Triangle(eOut3, eIn13, eIn23);

            List<Triangle> addList = new LinkedList<Triangle>();
            addList.add(tIn1);
            addList.add(tIn2);
            addList.add(tIn3);
            List<Triangle> delList = new LinkedList<Triangle>();
            delList.add(startTriangle);
            this.pointLocator.onReplaceTriangles(delList, addList);

            growingStar.push(new StarLinkInfo(tIn1, eOut1));
            growingStar.push(new StarLinkInfo(tIn2, eOut2));
            growingStar.push(new StarLinkInfo(tIn3, eOut3));
        }

        while (!growingStar.isEmpty()) {
            try {
                StarLinkInfo currStarLink = (StarLinkInfo) growingStar.pop();
                Triangle currStarTriangle = currStarLink.starTriangle;
                TriangleEdge currLinkEdge = currStarLink.linkEdge;

                if (currLinkEdge.isBoundary()) {
                    // nothing to check
                    this.triangleTree.put(currStarTriangle.getID(),
                            currStarTriangle);
                    continue;
                }

                Triangle adjacentTriangle = currLinkEdge.getAnotherNeighbour(
                        currStarTriangle);
                Vertex2D opposingVertex = adjacentTriangle.getOpposingVertex(
                        currLinkEdge);

                // check if this edge is locally Delaunay
                if (currStarTriangle.isPointInCircumcircle(opposingVertex)) {
                    // *** flip edge

                    currStarTriangle.removeAsNeighbour();
                    adjacentTriangle.removeAsNeighbour();

                    this.triangleTree.remove(currStarTriangle.getID());
                    this.triangleTree.remove(adjacentTriangle.getID());

                    // create two new ones
                    TriangleEdge edge11 = currStarTriangle.getAdjacentEdgeByVertex(
                            currLinkEdge.getVertex1().getID(),
                            currLinkEdge.getID());
                    TriangleEdge edge12 = currStarTriangle.getAdjacentEdgeByVertex(
                            currLinkEdge.getVertex2().getID(),
                            currLinkEdge.getID());

                    TriangleEdge edge21 = adjacentTriangle.getAdjacentEdgeByVertex(
                            currLinkEdge.getVertex1().getID(),
                            currLinkEdge.getID());
                    TriangleEdge edge22 = adjacentTriangle.getAdjacentEdgeByVertex(
                            currLinkEdge.getVertex2().getID(),
                            currLinkEdge.getID());

                    TriangleEdge newEdge = new TriangleEdge(false, vertex,
                            opposingVertex);
                    Triangle newTriangle1 = new Triangle(edge11, edge21,
                            newEdge);
                    Triangle newTriangle2 = new Triangle(edge12, edge22,
                            newEdge);
                    growingStar.push(new StarLinkInfo(newTriangle1, edge21));
                    growingStar.push(new StarLinkInfo(newTriangle2, edge22));

                    List<Triangle> addList = new LinkedList<Triangle>();
                    addList.add(newTriangle1);
                    addList.add(newTriangle2);
                    List<Triangle> delList = new LinkedList<Triangle>();
                    delList.add(currStarTriangle);
                    delList.add(adjacentTriangle);
                    this.pointLocator.onReplaceTriangles(delList, addList);
                }
                else {
                    this.triangleTree.put(currStarTriangle.getID(),
                            currStarTriangle);
                }
            }
            catch (EmptyStackException ese) {
                this.logger.warning("Stack empty in growing star");
                return false;
            }
        }
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

