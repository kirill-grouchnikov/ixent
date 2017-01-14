package org.jvnet.ixent.graphics.objects;

import static org.jvnet.ixent.math.MathConstants.EPS;

import java.awt.geom.Point2D;

import org.jvnet.ixent.math.coord.Vertex2D;

/**
 * Class that represents a triangle edge. Loosely follows "winged-edge" approach
 * - the edge stores links to the neighbouring triangles (maximum two) and
 * triangle stores links to exactly three edges.
 *
 * @author Kirill Grouchnikov
 */
public final class TriangleEdge {
    /**
     * Static variable for allocating IDs to edges
     */
    private static int currID = 0;

    /**
     * Edge ID
     */
    private long id;

    /**
     * Boolean specifying whether this is a boundary edge
     */
    private boolean isBoundary;

    /**
     * First vertex
     */
    private Vertex2D vertex1;

    /**
     * Second vertex
     */
    private Vertex2D vertex2;

    /**
     * First neighbouring triangle (<code>null</code> if none)
     */
    private Triangle triangle1;

    /**
     * Second neighbouring triangle (<code>null</code> if none)
     */
    private Triangle triangle2;

    /**
     * coefficients of a*x+b*y+c=0
     */
    private double a, b, c;

    /**
     * Constructs new edge given two vertices and boundary condition
     *
     * @param isBoundary <code>true</code> if this edge is a boundary edge,
     *                   <code>false</code> otherwise
     * @param p1         first vertex. Must be non-null
     * @param p2         second vertex. Must be non-null
     * @throws IllegalArgumentException if one of the vertices is null
     */
    public TriangleEdge(boolean isBoundary, Vertex2D p1, Vertex2D p2) {
        if ((p1 == null) || (p2 == null)) {
            throw new IllegalArgumentException("Null vertices not allowed");
        }

        this.id = TriangleEdge.currID++;
        this.isBoundary = isBoundary;
        this.vertex1 = new Vertex2D(p1);
        this.vertex2 = new Vertex2D(p2);

        // compute coefficients
        // compute line equation: ax+by+c=0
        double x1 = this.vertex1.getX();
        double y1 = this.vertex1.getY();
        double x2 = this.vertex2.getX();
        double y2 = this.vertex2.getY();

        double dx = x1 - x2;
        double dy = y1 - y2;
        double ddx = (dx > 0) ? dx : -dx;
        double ddy = (dy > 0) ? dy : -dy;
        if (ddx < EPS) {
            this.a = 1.0;
            this.b = 0.0;
            this.c = -x1;
        }
        else {
            if (ddy < EPS) {
                this.a = 0.0;
                this.b = 1.0;
                this.c = -y1;
            }
            else {
                // real case. Let b=1.0 -> y+ax+c=0
                this.a = (y1 - y2) / (x2 - x1);
                this.b = 1.0;
                this.c = -y1 - this.a * x1;
            }
        }
    }

    /**
     * Return ID of this edge
     *
     * @return edge ID
     */
    public long getID() {
        return id;
    }

    /**
     * Return whether this edge is boundary
     *
     * @return <code>true</code> if this edge is a boundary edge,
     *         <code>false</code> otherwise
     */
    public boolean isBoundary() {
        return isBoundary;
    }

    /**
     * Return the first vertex of this edge
     *
     * @return the first vertex of this edge
     */
    public Vertex2D getVertex1() {
        return vertex1;
    }

    /**
     * Return the second vertex of this edge
     *
     * @return the second vertex of this edge
     */
    public Vertex2D getVertex2() {
        return vertex2;
    }

    /**
     * Return the first neighbouring triangle of this edge
     *
     * @return the first neighbouring triangle of this edge
     */
    public Triangle getTriangle1() {
        return triangle1;
    }

    /**
     * Return the second neighbouring triangle of this edge
     *
     * @return the second neighbouring triangle of this edge
     */
    public Triangle getTriangle2() {
        return triangle2;
    }

    /**
     * Set the first neighbouring triangle of this edge
     *
     * @param mTriangle1 the first neighbouring triangle of this edge
     */
    public void setTriangle1(Triangle mTriangle1) {
        this.triangle1 = mTriangle1;
    }

    /**
     * Set the second neighbouring triangle of this edge
     *
     * @param mTriangle2 the second neighbouring triangle of this edge
     */
    public void setTriangle2(Triangle mTriangle2) {
        this.triangle2 = mTriangle2;
    }

    /**
     * Return a common (joint) point of this edge and another edge
     *
     * @param edge2 the other edge
     * @return <code>Vertex2D</code> that is a common point if exists,
     *         <code>null</code> otherwise
     */
    public Vertex2D getJointPoint(TriangleEdge edge2) {
        if (this.vertex1.isTheSame(edge2.vertex1, EPS)) {
            return new Vertex2D(this.vertex1);
        }
        if (this.vertex1.isTheSame(edge2.vertex2, EPS)) {
            return new Vertex2D(this.vertex1);
        }
        if (this.vertex2.isTheSame(edge2.vertex1, EPS)) {
            return new Vertex2D(this.vertex2);
        }
        if (this.vertex2.isTheSame(edge2.vertex2, EPS)) {
            return new Vertex2D(this.vertex2);
        }
        return null;
    }

    /**
     * Check that the last point of this edge is the first point of another
     * edge
     *
     * @param edge2 the other edge
     * @return <code>true</code> if the second point of this edge is the first
     *         point of the other edge, <code>false</code> otherwise
     */
    public boolean connectsWith(TriangleEdge edge2) {
        return this.vertex2.isTheSame(edge2.vertex1, EPS);
    }

    /**
     * Remove given triangle as edge's neighbour
     *
     * @param triangleID triangle ID
     */
    public void removeNeigbour(long triangleID) {
        if (this.triangle1 != null) {
            if (this.triangle1.getID() == triangleID) {
                this.triangle1 = null;
            }
        }
        if (this.triangle2 != null) {
            if (this.triangle2.getID() == triangleID) {
                this.triangle2 = null;
            }
        }
    }

    /**
     * Add given triangle as edge's neighbour. Important - this function <b>does
     * not</b> check that this triangle is already edge's neighbour
     *
     * @param triangle new neighbouring triangle
     * @throws IllegalStateException if this function is called on edge that has
     *                               already has its two neighbours set
     */
    public void addNeigbour(Triangle triangle) {
        if (this.triangle1 == null) {
            this.triangle1 = triangle;
            return;
        }
        if (this.triangle2 == null) {
            this.triangle2 = triangle;
            return;
        }
        throw new IllegalStateException(
                "Couldn't add neigbour to edge with 2 neigbours");
    }

    /**
     * Return neighbouring triangle that is different from the given triangle
     *
     * @param triangleID the first triangle ID
     * @return <code>Triangle</code> that is the second neighbouring triangle
     *         (if set), <code>null</code> otherwise
     */
    public Triangle getNeigbour(long triangleID) {
        if (this.triangle1 != null) {
            if (this.triangle1.getID() == triangleID) {
                return this.triangle2;
            }
        }
        if (this.triangle2 != null) {
            if (this.triangle2.getID() == triangleID) {
                return this.triangle1;
            }
        }
        return null;
    }

    /**
     * Return neighbouring triangle that is different from the given triangle
     *
     * @param triangle the first triangle
     * @return <code>Triangle</code> that is the second neighbouring triangle
     *         (if set), <code>null</code> otherwise
     */
    public Triangle getAnotherNeighbour(Triangle triangle) {
        if ((this.triangle1 != null) &&
                (this.triangle1.getID() != triangle.getID())) {
            return this.triangle1;
        }
        if ((this.triangle2 != null) &&
                (this.triangle2.getID() != triangle.getID())) {
            return this.triangle2;
        }
        return null;
    }

    /**
     * Flip vertices of this edge (swap the first and the second vertices)
     */
    public void flipVertices() {
        Vertex2D tempPoint = new Vertex2D(this.vertex1);
        this.vertex1.set(vertex2);
        this.vertex2.set(tempPoint);
    }

    /**
     * Return the number of neighbouring triangles
     *
     * @return the number of neighbouring triangles
     */
    public int getTriangleCount() {
        int result = 0;
        if (this.triangle1 != null) {
            result++;
        }
        if (this.triangle2 != null) {
            result++;
        }
        return result;
    }

    /**
     * Return minimal X of this edge
     *
     * @return minimal X of this edge
     */
    public double getMinX() {
        double x1 = this.vertex1.getX();
        double x2 = this.vertex2.getX();
        if (x1 < x2) {
            return x1;
        }
        else {
            return x2;
        }
    }

    /**
     * Return maximal X of this edge
     *
     * @return maximal X of this edge
     */
    public double getMaxX() {
        double x1 = this.vertex1.getX();
        double x2 = this.vertex2.getX();
        if (x1 > x2) {
            return x1;
        }
        else {
            return x2;
        }
    }

    /**
     * Return minimal Y of this edge
     *
     * @return minimal Y of this edge
     */
    public double getMinY() {
        double y1 = this.vertex1.getY();
        double y2 = this.vertex2.getY();
        if (y1 < y2) {
            return y1;
        }
        else {
            return y2;
        }
    }

    /**
     * Return maximal Y of this edge
     *
     * @return maximal Y of this edge
     */
    public double getMaxY() {
        double y1 = this.vertex1.getY();
        double y2 = this.vertex2.getY();
        if (y1 > y2) {
            return y1;
        }
        else {
            return y2;
        }
    }

    /**
     * Return mid point of this edge
     *
     * @return mid point of this edge
     */
    public Point2D getMidPoint() {
        return new Point2D.Double(
                (this.vertex1.getX() + this.vertex2.getX()) / 2.0,
                (this.vertex1.getY() + this.vertex2.getY()) / 2.0);
    }

    public boolean isMatchPoints(Vertex2D point1, Vertex2D point2) {
        if (this.vertex1.isTheSame(point1, EPS) &&
                this.vertex2.isTheSame(point2, EPS)) {
            return true;
        }
        if (this.vertex1.isTheSame(point2, EPS) &&
                this.vertex2.isTheSame(point1, EPS)) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two given vertices lie on the same side of this edge
     *
     * @param vertex1 the first vertex
     * @param vertex2 the second vertex
     * @return <code>true</code> if they lie on the same side of the edge,
     *         <code>false</code> otherwise
     */
    public boolean isOnTheSameSide(Vertex2D vertex1, Vertex2D vertex2) {
        // check signs
        double val1 = this.a * vertex1.getX() + this.b * vertex1.getY() +
                this.c;
        double val2 = this.a * vertex2.getX() + this.b * vertex2.getY() +
                this.c;

        if ((val1 >= 0.0) && (val2 >= 0.0)) {
            return true;
        }
        if ((val1 <= 0.0) && (val2 <= 0.0)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if given point lies on the line containing the edge
     *
     * @param point point to check
     * @return <code>true</code> if the point lies on this edge,
     *         <code>false</code> otherwise
     */
    public boolean isOnEdge(Vertex2D point) {
        double val = this.a * point.getX() + this.b * point.getY() + this.c;
        if (val < 0.0) {
            val = -val;
        }
        return val < EPS;
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        return ("Triangle edge " + this.id + " [" +
                this.vertex1.toString() + "-" + this.vertex2.toString() +
                "], [" +
                ((this.triangle1 == null) ? -1 : this.triangle1.getID()) +
                ", " +
                ((this.triangle2 == null) ? -1 : this.triangle2.getID()) + "]");
    }
}

