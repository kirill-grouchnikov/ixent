package org.jvnet.ixent.graphics.objects;

import static org.jvnet.ixent.math.MathConstants.EPS;

import java.awt.geom.Point2D;

import org.jvnet.ixent.math.coord.Vertex2D;
import org.jvnet.ixent.math.intersect.ClippingManager;

/**
 * Class that represents a triangle. Loosely follows "winged-edge" approach -
 * the triangle stores links to exactly three edges and the edge stores links to
 * the neighbouring triangles (maximum two)
 *
 * @author Kirill Grouchnikov
 */
public final class Triangle {
    /**
     * Static variable for allocating IDs to triangles
     */
    private static int currID = 0;

    /**
     * Triangle ID
     */
    private long id;

    /**
     * The first edge
     */
    private TriangleEdge edge1;

    /**
     * The second edge
     */
    private TriangleEdge edge2;

    /**
     * The third edge
     */
    private TriangleEdge edge3;

    /**
     * The vertex common to the first and the second edge
     */
    private Vertex2D vertex12;

    /**
     * The vertex common to the first and the third edge
     */
    private Vertex2D vertex13;

    /**
     * The vertex common to the second and the third edge
     */
    private Vertex2D vertex23;

    /**
     * Mass center of this triangle
     */
    private Point2D massCenter;

    /**
     * Indicates whether we computed the circle that passes through this
     * triangle's vertices
     */
    private boolean circExists;

    /**
     * The center of circle that passes through this triangle's vertices
     */
    private Point2D circCenter;

    /**
     * The radius of circle that passes through this triangle's vertices
     */
    private double circRadius;

    /**
     * Minimal / maximal coordinates
     */
    private double minX, minY, maxX, maxY;

    /**
     * Helper object no. 1 for locating points during Delaunay triangulation
     */
    private Object locatorHelperObject1;

    /**
     * Helper object no. 2 for locating points during Delaunay triangulation
     */
    private Object locatorHelperObject2;

    /**
     * Construct new triangle using three edges. These edges must be non-null
     * and connecting (forming a valid triangle)
     *
     * @param pEdge1 the first edge
     * @param pEdge2 the second edge
     * @param pEdge3 the third edge
     * @throws IllegalArgumentException if one of the edges is null or there is
     *                                  a pair of edges with no common point
     */
    public Triangle(TriangleEdge pEdge1, TriangleEdge pEdge2,
                    TriangleEdge pEdge3) {
        if ((pEdge1 == null) || (pEdge2 == null) || (pEdge3 == null)) {
            throw new IllegalArgumentException("Can't pass null edges");
        }

        this.id = Triangle.currID++;
        this.edge1 = pEdge1;
        this.edge2 = pEdge2;
        this.edge3 = pEdge3;

        this.vertex12 = pEdge1.getJointPoint(pEdge2);
        if (this.vertex12 == null) {
            throw new IllegalArgumentException("Can't find common vertex of " +
                    pEdge1.toString() + " and " + pEdge2.toString());
        }
        this.vertex13 = pEdge1.getJointPoint(pEdge3);
        if (this.vertex13 == null) {
            throw new IllegalArgumentException("Can't find common vertex of " +
                    pEdge1.toString() + " and " + pEdge3.toString());
        }
        this.vertex23 = pEdge2.getJointPoint(pEdge3);
        if (this.vertex23 == null) {
            throw new IllegalArgumentException("Can't find common vertex of " +
                    pEdge2.toString() + " and " + pEdge3.toString());
        }

        this.edge1.addNeigbour(this);
        this.edge2.addNeigbour(this);
        this.edge3.addNeigbour(this);

        // min max X Y
        double xm1 = this.edge1.getMinX();
        double xm2 = this.edge2.getMinX();
        double xm3 = this.edge3.getMinX();

        double tx = (xm1 < xm2) ? xm1 : xm2;
        double ttx = (tx < xm3) ? tx : xm3;
        this.minX = ttx;

        xm1 = this.edge1.getMaxX();
        xm2 = this.edge2.getMaxX();
        xm3 = this.edge3.getMaxX();

        tx = (xm1 > xm2) ? xm1 : xm2;
        ttx = (tx > xm3) ? tx : xm3;
        this.maxX = ttx;

        double ym1 = this.edge1.getMinY();
        double ym2 = this.edge2.getMinY();
        double ym3 = this.edge3.getMinY();

        double ty = (ym1 < ym2) ? ym1 : ym2;
        double tty = (ty < ym3) ? ty : ym3;
        this.minY = tty;

        ym1 = this.edge1.getMaxY();
        ym2 = this.edge2.getMaxY();
        ym3 = this.edge3.getMaxY();

        ty = (ym1 > ym2) ? ym1 : ym2;
        tty = (ty > ym3) ? ty : ym3;
        this.maxY = tty;

        // circumference
        double x1 = vertex12.getX();
        double x2 = vertex23.getX();
        double x3 = vertex13.getX();
        double y1 = vertex12.getY();
        double y2 = vertex23.getY();
        double y3 = vertex13.getY();

        double a11 = 2.0 * (x2 - x1);
        double a12 = 2.0 * (y2 - y1);
        double b1 = x2 * x2 - x1 * x1 + y2 * y2 - y1 * y1;
        double a21 = 2.0 * (x3 - x1);
        double a22 = 2.0 * (y3 - y1);
        double b2 = x3 * x3 - x1 * x1 + y3 * y3 - y1 * y1;

        double detA = a11 * a22 - a21 * a12;

        if (Math.abs(detA) < EPS) {
            this.circExists = false;
            this.circCenter = null;
            return;
        }

        double a = (b1 * a22 - b2 * a12) / detA;
        double b = (b2 * a11 - b1 * a21) / detA;

        this.circCenter = new Point2D.Double(a, b);

        double R2 = (x1 - a) * (x1 - a) + (y1 - b) * (y1 - b);
        this.circRadius = Math.sqrt(R2);
        this.circExists = true;
    }

    /**
     * Return ID of this triangle
     *
     * @return ID of this triangle
     */
    public long getID() {
        return id;
    }

    /**
     * Return the first edge of this triangle
     *
     * @return the first edge of this triangle
     */
    public TriangleEdge getEdge1() {
        return edge1;
    }

    /**
     * Return the second edge of this triangle
     *
     * @return the second edge of this triangle
     */
    public TriangleEdge getEdge2() {
        return edge2;
    }

    /**
     * Return the third edge of this triangle
     *
     * @return the third edge of this triangle
     */
    public TriangleEdge getEdge3() {
        return edge3;
    }

    /**
     * Return vertex common to the first and the second edge of this triangle
     *
     * @return vertex common to the first and the second edge of this triangle
     */
    public Vertex2D getVertex12() {
        return vertex12;
    }

    /**
     * Return vertex common to the first and the third edge of this triangle
     *
     * @return vertex common to the first and the third edge of this triangle
     */
    public Vertex2D getVertex13() {
        return vertex13;
    }

    /**
     * Return vertex common to the second and the third edge of this triangle
     *
     * @return vertex common to the second and the third edge of this triangle
     */
    public Vertex2D getVertex23() {
        return vertex23;
    }

    /**
     * Return mass center of this triangle
     *
     * @return mass center of this triangle
     */
    public Point2D getMassCenter() {
        return massCenter;
    }

    /**
     * Check if this triangle has circumscribing circle
     *
     * @return <code>true</code> if this triangle has circumscribing circle and
     *         <code>false</code> if not
     */
    public boolean isCircExists() {
        return circExists;
    }

    /**
     * Return the center of the  circumscribing circle
     *
     * @return the center of the  circumscribing circle
     * @throws IllegalStateException if this triangle has no such circle (that
     *                               is all its three vertices lie on the same
     *                               line)
     */
    public Point2D getCircCenter() {
        if (!this.circExists) {
            throw new IllegalStateException(
                    "This triangle has points lying on one line.");
        }
        return this.circCenter;
    }

    /**
     * Return the radius of the  circumscribing circle
     *
     * @return the radius of the  circumscribing circle
     * @throws IllegalStateException if this triangle has no such circle (that
     *                               is all its three vertices lie on the same
     *                               line)
     */
    public double getCircRadius() {
        if (!this.circExists) {
            throw new IllegalStateException(
                    "This triangle has points lying on one line.");
        }
        return this.circRadius;
    }

    /**
     * Return helper object no. 1 for locating points during Delaunay
     * triangulation
     *
     * @return helper object no. 1 for locating points during Delaunay
     *         triangulation
     */
    public Object getLocatorHelperObject1() {
        return locatorHelperObject1;
    }

    /**
     * Return helper object no. 2 for locating points during Delaunay
     * triangulation
     *
     * @return helper object no. 2 for locating points during Delaunay
     *         triangulation
     */
    public Object getLocatorHelperObject2() {
        return locatorHelperObject2;
    }

    /**
     * Set  helper object no. 1 for locating points during Delaunay
     * triangulation
     *
     * @param locatorHelperObject1 helper object no. 1 for locating points
     *                             during Delaunay triangulation
     */
    public void setLocatorHelperObject1(Object locatorHelperObject1) {
        this.locatorHelperObject1 = locatorHelperObject1;
    }

    /**
     * Set helper object no. 2 for locating points during Delaunay
     * triangulation
     *
     * @param locatorHelperObject2 helper object no. 2 for locating points
     *                             during Delaunay triangulation
     */
    public void setLocatorHelperObject2(Object locatorHelperObject2) {
        this.locatorHelperObject2 = locatorHelperObject2;
    }

    /**
     * Check if given point lies inside the circumscribing circle
     *
     * @param point point to check
     * @return <code>true</code> if it lies inside the circumscribing circle,
     *         <code>false</code> if it does not
     * @throws IllegalStateException if this triangle has no such circle (that
     *                               is all its three vertices lie on the same
     *                               line)
     */
    public boolean isPointInCircumcircle(Vertex2D point) {
        if (!this.circExists) {
            throw new IllegalStateException(
                    "This triangle has points lying on one line.");
        }

        // check distance to center
        double dx = point.getX() - this.circCenter.getX();
        double dy = point.getY() - this.circCenter.getY();
        return !((dx * dx + dy * dy) > this.circRadius * this.circRadius);
    }

    /**
     * Check if given point lies inside this triangle
     *
     * @param point point to check
     * @return <code>true</code> if it lies inside this triangle,
     *         <code>false</code> if it does not
     */
    public boolean isPointInside(Vertex2D point) {
        if (this.circExists) {
            // check distance to center
            double dx = point.getX() - this.circCenter.getX();
            double dy = point.getY() - this.circCenter.getY();
            if ((dx * dx + dy * dy) > this.circRadius * this.circRadius) {
                return false;
            }
        }
        if (!this.edge1.isOnTheSameSide(point, this.vertex23)) {
            return false;
        }
        if (!this.edge2.isOnTheSameSide(point, this.vertex13)) {
            return false;
        }
        if (!this.edge3.isOnTheSameSide(point, this.vertex12)) {
            return false;
        }
        return true;
    }

    /**
     * Get minimal X coordinate of this triangle's vertices
     *
     * @return minimal X coordinate of this triangle's vertices
     */
    public double getMinX() {
        return this.minX;
    }

    /**
     * Get maximal X coordinate of this triangle's vertices
     *
     * @return maximal X coordinate of this triangle's vertices
     */
    public double getMaxX() {
        return this.maxX;
    }

    /**
     * Get minimal Y coordinate of this triangle's vertices
     *
     * @return minimal Y coordinate of this triangle's vertices
     */
    public double getMinY() {
        return this.minY;
    }

    /**
     * Get maximal Y coordinate of this triangle's vertices
     *
     * @return maximal Y coordinate of this triangle's vertices
     */
    public double getMaxY() {
        return this.maxY;
    }

    /**
     * Compute area of intersection of this triangle and 1*1 square with
     * specified coordinates
     *
     * @param x X coordinate of left bottom corner of 1*1 square
     * @param y Y coordinate of left bottom corner of 1*1 square
     * @return area of intersection of this triangle and 1*1 square with
     *         specified coordinates
     */
    public double intersectionArea(int x, int y) {
        Point2D[] polygon = new Point2D[3];
        polygon[0] = this.vertex12.getPoint();
        polygon[1] = this.vertex13.getPoint();
        polygon[2] = this.vertex23.getPoint();

        return ClippingManager.intersectionArea(polygon,
                new Point2D.Double(x, y), new Point2D.Double(x + 1, y + 1));
    }

    /**
     * Compute mass center of this triangle. Does nothing if this function was
     * already called
     */
    public void computeMassCenter() {
        if (this.massCenter != null) {
            return;
        }

        double S = ClippingManager.generalPolygonArea(new Point2D[]{
            this.vertex12.getPoint(),
            this.vertex13.getPoint(),
            this.vertex23.getPoint()});

        // **** compute x

        // sort by x
        Point2D[] points = new Point2D[3];
        points[0] = this.vertex12.getPoint();
        points[1] = this.vertex13.getPoint();
        points[2] = this.vertex23.getPoint();
        for (int i = 0; i <= 2; i++) {
            for (int j = i + 1; j <= 2; j++) {
                if (points[i].getX() > points[j].getX()) {
                    Point2D tmp = points[i];
                    points[i] = points[j];
                    points[j] = tmp;
                }
            }
        }

        // compute
        double x1 = points[0].getX();
        double x2 = points[1].getX();
        double x3 = points[2].getX();

        double xc = x1;

        if (Math.abs(x1 - x2) < EPS) {
            xc = x3 - (x3 - x1) / Math.sqrt(2.0);
        }
        else {
            if (Math.abs(x2 - x3) < EPS) {
                xc = x1 + (x2 - x1) / Math.sqrt(2.0);
            }
            else {
                // general case
                double y1 = points[0].getY();
                double y3 = points[2].getY();
                double y4 = y1 + (y3 - y1) * (x2 - x1) / (x3 - x1);
                double SL = ClippingManager.generalPolygonArea(new Point2D[]{
                    points[0], points[1],
                    new Point2D.Double(x2, y4)});
                double SR = S - SL;

                if (SL >= SR) {
                    xc = x1 + (x2 - x1) * Math.sqrt(S / (2.0 * SL));
                }
                else {
                    xc = x3 - (x3 - x2) * Math.sqrt(S / (2.0 * SR));
                }
            }
        }

        // **** compute y

        // sort by y
        points = new Point2D[3];
        points[0] = this.vertex12.getPoint();
        points[1] = this.vertex13.getPoint();
        points[2] = this.vertex23.getPoint();
        for (int i = 0; i <= 2; i++) {
            for (int j = i + 1; j <= 2; j++) {
                if (points[i].getY() > points[j].getY()) {
                    Point2D tmp = points[i];
                    points[i] = points[j];
                    points[j] = tmp;
                }
            }
        }

        // compute
        double y1 = points[0].getY();
        double y2 = points[1].getY();
        double y3 = points[2].getY();

        double yc = y1;

        if (Math.abs(y1 - y2) < EPS) {
            yc = y3 - (y3 - y1) / Math.sqrt(2.0);
        }
        else {
            if (Math.abs(y2 - y3) < EPS) {
                yc = y1 + (y2 - y1) / Math.sqrt(2.0);
            }
            else {
                // general case
                x1 = points[0].getX();
                x3 = points[2].getX();
                double x4 = x1 + (x3 - x1) * (y2 - y1) / (y3 - y1);
                double ST = ClippingManager.generalPolygonArea(new Point2D[]{
                    points[0], points[1],
                    new Point2D.Double(x4, y2)});
                double SB = S - ST;

                if (ST >= SB) {
                    yc = y1 + (y2 - y1) * Math.sqrt(S / (2.0 * ST));
                }
                else {
                    yc = y3 - (y3 - y2) * Math.sqrt(S / (2.0 * SB));
                }
            }
        }

        this.massCenter = new Point2D.Double(xc, yc);
    }

    /**
     * Given a vertex and an edge, return another edge of this triangle that has
     * this vertex as one of the vertices but is different from the given edge
     *
     * @param vertexID ID of vertex
     * @param edgeID   ID of edge
     * @return another edge of this triangle that has this vertex as one of the
     *         vertices but is different from the given edge
     */
    public TriangleEdge getAdjacentEdgeByVertex(long vertexID, long edgeID) {
        if (vertexID == this.vertex12.getID()) {
            if (this.edge1.getID() == edgeID) {
                return this.edge2;
            }
            if (this.edge2.getID() == edgeID) {
                return this.edge1;
            }
        }
        if (vertexID == this.vertex13.getID()) {
            if (this.edge1.getID() == edgeID) {
                return this.edge3;
            }
            if (this.edge3.getID() == edgeID) {
                return this.edge1;
            }
        }
        if (vertexID == this.vertex23.getID()) {
            if (this.edge2.getID() == edgeID) {
                return this.edge3;
            }
            if (this.edge3.getID() == edgeID) {
                return this.edge2;
            }
        }
        return null;
    }

    /**
     * Given a vertex return triangle's edge that has this vertex as its first
     * vertex
     *
     * @param vertexID vertex ID
     * @return triangle's edge that has this vertex as its first vertex
     */
    public TriangleEdge getEdgeByVertex1(long vertexID) {
        if (vertexID == this.vertex12.getID()) {
            return this.edge1;
        }
        if (vertexID == this.vertex23.getID()) {
            return this.edge2;
        }
        if (vertexID == this.vertex13.getID()) {
            return this.edge3;
        }
        return null;
    }

    /**
     * Given a vertex return triangle's edge that has this vertex as its second
     * vertex
     *
     * @param vertexID vertex ID
     * @return triangle's edge that has this vertex as its second vertex
     */
    public TriangleEdge getEdgeByVertex2(long vertexID) {
        if (vertexID == this.vertex12.getID()) {
            return this.edge2;
        }
        if (vertexID == this.vertex23.getID()) {
            return this.edge3;
        }
        if (vertexID == this.vertex13.getID()) {
            return this.edge1;
        }
        return null;
    }

    /**
     * Check if given vertex is one of this triangle's vertices
     *
     * @param vertex vertex to check
     * @return <code>true</code> if given vertex is one of this triangle's
     *         vertices and <code>false</code> otherwise
     */
    public boolean hasPoint(Vertex2D vertex) {
        if (this.vertex12.isTheSame(vertex, EPS)) {
            return true;
        }
        if (this.vertex13.isTheSame(vertex, EPS)) {
            return true;
        }
        if (this.vertex23.isTheSame(vertex, EPS)) {
            return true;
        }
        return false;
    }

    /**
     * Remove this triangle as neighbour from all of its edges
     */
    public void removeAsNeighbour() {
        this.edge1.removeNeigbour(this.id);
        this.edge2.removeNeigbour(this.id);
        this.edge3.removeNeigbour(this.id);
    }

    /**
     * Return the vertice that lies opposite given edge
     *
     * @param edge edge to check
     * @return the vertice that lies opposite given edge
     * @throws IllegalArgumentException if the specified edge doesn't belong to
     *                                  this triangle
     */
    public Vertex2D getOpposingVertex(TriangleEdge edge) {
        if (this.edge1.getID() == edge.getID()) {
            return this.vertex23;
        }
        if (this.edge2.getID() == edge.getID()) {
            return this.vertex13;
        }
        if (this.edge3.getID() == edge.getID()) {
            return this.vertex12;
        }
        throw new IllegalArgumentException("Edge not part of triangle");
    }

    /**
     * Check if given point lies on one of this triangle's edge
     *
     * @param point point to check
     * @return <code>true</code> if given point lies on one of this triangle's
     *         edge and <code>false</code> otherwise
     */
    public boolean hasPointOnEdge(Vertex2D point) {
        if (this.edge1.isOnEdge(point)) {
            return true;
        }
        if (this.edge2.isOnEdge(point)) {
            return true;
        }
        if (this.edge3.isOnEdge(point)) {
            return true;
        }
        return false;
    }

    /**
     * Return this triangle's edge that contains given point
     *
     * @param point point to check
     * @return <code>TriangleEdge</code> that contains given point if such edge
     *         exists or <code>null</code> otherwise
     */
    public TriangleEdge getEdgeContainingPoint(Vertex2D point) {
        if (this.edge1.isOnEdge(point)) {
            return this.edge1;
        }
        if (this.edge2.isOnEdge(point)) {
            return this.edge2;
        }
        if (this.edge3.isOnEdge(point)) {
            return this.edge3;
        }
        return null;
    }

    /**
     * Get this triangle's vertices as array of points
     *
     * @return this triangle's vertices as array of points
     */
    public Point2D[] getPoints() {
        return new Point2D[]{this.vertex12.getPoint(), this.vertex13.getPoint(),
                             this.vertex23.getPoint()};
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Triangle no. " + this.id + ", edges " + edge1.getID() +
                ", " +
                edge2.getID() + ", " + edge3.getID());
        sb.append('\n');
        sb.append(" edge1: " + this.edge1.toString());
        sb.append('\n');
        sb.append(" edge2: " + this.edge2.toString());
        sb.append('\n');
        sb.append(" edge3: " + this.edge3.toString());
        sb.append('\n');
        sb.append(" circ: " + this.circCenter.toString() + ", R=" +
                this.circRadius);
        sb.append('\n');
        return sb.toString();
    }
}

