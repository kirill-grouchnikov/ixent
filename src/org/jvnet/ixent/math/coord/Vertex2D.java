package org.jvnet.ixent.math.coord;

import java.awt.geom.Point2D;

/**
 * 2-dimensional vertex. Is a decorator arount Java's Point2D. Adds ID and few
 * other functions
 *
 * @author Kirill Grouchnikov
 */
public final class Vertex2D {
    /**
     * Variable to keep track of IDs
     */
    private static int currID = 0;

    /**
     * Vertex ID
     */
    private int id;

    /**
     * The decorated point
     */
    private Point2D point;

    /**
     * @param x x coordinate
     * @param y y coordinate
     */
    public Vertex2D(int x, int y) {
        this.id = Vertex2D.currID++;
        this.point = new Point2D.Double(x, y);
    }

    /**
     * @param x x coordinate
     * @param y y coordinate
     */
    public Vertex2D(double x, double y) {
        this.id = Vertex2D.currID++;
        this.point = new Point2D.Double(x, y);
    }

    /**
     * Copy constructor
     *
     * @param vertex2 vertex to copy
     */
    public Vertex2D(Vertex2D vertex2) {
        this.id = vertex2.id;
        this.point = new Point2D.Double(vertex2.getX(), vertex2.getY());
    }

    /**
     * Return x coordinate
     *
     * @return x coordinate
     */
    public double getX() {
        return this.point.getX();
    }

    /**
     * Return y coordinate
     *
     * @return y coordinate
     */
    public double getY() {
        return this.point.getY();
    }

    /**
     * Reset ID counter
     */
    public static void resetID() {
        Vertex2D.currID = 0;
    }

    /**
     * Set new ID for this vertex
     *
     * @param newID new ID
     */
    public void setID(int newID) {
        this.id = newID;
    }

    /**
     * Get ID of this vertex
     *
     * @return vertex ID
     */
    public int getID() {
        return this.id;
    }

    /**
     * Return underlying <code>Point2D</code> object
     *
     * @return the underlying <code>Point2D</code> object
     */
    public Point2D getPoint() {
        return this.point;
    }

    /**
     * Sets the location of this vertex to the specified coordinates
     *
     * @param x new x coordinate
     * @param y new y coordinate
     */
    public void setLocation(double x, double y) {
        this.point.setLocation(x, y);
    }

    /**
     * Sets the location and ID of this vertex to the specified vertex
     *
     * @param vertex2 the second vertex
     */
    public void set(Vertex2D vertex2) {
        this.id = vertex2.id;
        this.setLocation(vertex2.getX(), vertex2.getY());
    }

    /**
     * Returns the square of the distance from this vertex to a specified
     * vertex
     *
     * @param xp x coordinate of the other vertex
     * @param yp y coordinate of the other vertex
     * @return the square of the distance from this vertex to a specified
     *         vertex
     */
    public double distanceSq(double xp, double yp) {
        return this.point.distanceSq(xp, yp);
    }

    /**
     * Returns the square of the distance from this vertex to a specified
     * vertex
     *
     * @param vertex2 the other vertex
     * @return the square of the distance from this vertex to a specified
     *         vertex
     */
    public double distanceSq(Vertex2D vertex2) {
        return this.point.distanceSq(vertex2.point);
    }

    /**
     * Returns the distance from this vertex to a specified vertex
     *
     * @param vertex2 the other vertex
     * @return the distance from this vertex to a specified vertex
     */
    public double distance(Vertex2D vertex2) {
        return this.point.distance(vertex2.point);
    }

    /**
     * Determines whether or not two vertices are equal. Two instances of
     * <code>Vertex2D</code> are equal if the values of their x and y member
     * fields, representing their position in the coordinate space, differ by no
     * more than specified value or their IDs match
     *
     * @param vertex2   the other vertex
     * @param manhattan maximal difference in either coordinate to allow
     * @return true if the vertex to be compared has the same values; false
     *         otherwise
     */
    public boolean isTheSame(Vertex2D vertex2, double manhattan) {
        if (this.id == vertex2.id) {
            return true;
        }
        return this.isTheSame(vertex2.getX(), vertex2.getY(), manhattan);
    }

    /**
     * Determines whether or not two vertices are equal. Two instances of
     * <code>Vertex2D</code> are equal if the values of their x and y member
     * fields, representing their position in the coordinate space, differ by no
     * more than specified value or their IDs match
     *
     * @param x         x coordinate of the other vertex
     * @param y         y coordinate of the other vertex
     * @param manhattan maximal difference in either coordinate to allow
     * @return true if the vertex to be compared has the same values; false
     *         otherwise
     */
    public boolean isTheSame(double x, double y, double manhattan) {
        return (this.hasSameX(x, manhattan) && this.hasSameY(y, manhattan));
    }

    /**
     * Determines whether or not two vertices have the same x coordinate (differ
     * by no more than specified value)
     *
     * @param x         x coordinate of the other vertex
     * @param manhattan maximal difference in X coordinate to allow
     * @return true if the vertex to be compared has the same X coordinate;
     *         false otherwise
     */
    public boolean hasSameX(double x, double manhattan) {
        double dx = this.getX() - x;
        double ddx = (dx > 0) ? dx : -dx;
        return (ddx < manhattan);
    }

    /**
     * Determines whether or not two vertices have the same y coordinate (differ
     * by no more than specified value)
     *
     * @param y         y coordinate of the other vertex
     * @param manhattan maximal difference in y coordinate to allow
     * @return true if the vertex to be compared has the same y coordinate;
     *         false otherwise
     */
    public boolean hasSameY(double y, double manhattan) {
        double dy = this.getY() - y;
        double ddy = (dy > 0) ? dy : -dy;
        return (ddy < manhattan);
    }

    /**
     * Create a new vertex by given offsets
     *
     * @param offsetX offset in x direction
     * @param offsetY offset in y direction
     * @return new vertex
     */
    public Vertex2D getVertexByOffsets(double offsetX, double offsetY) {
        return new Vertex2D(this.getX() + offsetX, this.getY() + offsetY);
    }

    /**
     * Offset this vertex by given offsets
     *
     * @param offsetX offset in x direction
     * @param offsetY offset in y direction
     */
    public void offset(double offsetX, double offsetY) {
        this.point.setLocation(this.point.getX() + offsetX,
                this.point.getY() + offsetY);
    }

    /**
     * Compute a vertex that lies half-way between this vertex and another
     * vertex
     *
     * @param vertex2 the other vertex
     * @return half-way vertex
     */
    public Vertex2D getMidPoint(Vertex2D vertex2) {
        return new Vertex2D(0.5 * (this.getX() + vertex2.getX()),
                0.5 * (this.getY() + vertex2.getY()));
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        return "Point " + this.id + "[" + this.getX() + ", " + this.getY() +
                "]";
    }
}

