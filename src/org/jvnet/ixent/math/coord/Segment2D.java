package org.jvnet.ixent.math.coord;

import static org.jvnet.ixent.math.MathConstants.EPS;

import java.awt.geom.Point2D;

import org.jvnet.ixent.math.MathConstants;

/**
 * This class defines a segment defined by two points in 2D space
 *
 * @author Kirill Grouchnikov
 */
public final class Segment2D {
    private Point2D point1;
    private Point2D point2;

    // line equation: ax+by+c=0
    private double a, b, c;
    private double minx, maxx, miny, maxy;

    /**
     * Constructs a new segment from the specified two end points
     *
     * @param point1 the first end point
     * @param point2 the second end point
     */
    public Segment2D(Point2D point1, Point2D point2) {
        this.point1 = point1;
        this.point2 = point2;
        double xr0 = point1.getX();
        double yr0 = point1.getY();
        double xr1 = point2.getX();
        double yr1 = point2.getY();

        // compute line equation: ax+by+c=0
        double dx = xr0 - xr1;
        double dy = yr0 - yr1;
        double ddx = (dx > 0) ? dx : -dx;
        double ddy = (dy > 0) ? dy : -dy;
        if (ddx < EPS) {
            this.a = 1.0;
            this.b = 0.0;
            this.c = -xr0;
        }
        else {
            if (ddy < EPS) {
                this.a = 0.0;
                this.b = 1.0;
                this.c = -yr0;
            }
            else {
                // real case. Let b=1.0 -> y+ax+c=0
                this.a = (yr0 - yr1) / (xr1 - xr0);
                this.b = 1.0;
                this.c = -yr0 - this.a * xr0;
            }
        }
        this.minx = Math.min(this.point1.getX(), this.point2.getX());
        this.maxx = Math.max(this.point1.getX(), this.point2.getX());

        this.miny = Math.min(this.point1.getY(), this.point2.getY());
        this.maxy = Math.max(this.point1.getY(), this.point2.getY());
    }

    /**
     * Returns the first end point of this segment
     *
     * @return the first end point
     */
    public Point2D getPoint1() {
        return this.point1;
    }

    /**
     * Returns the second end point of this segment
     *
     * @return the second end point
     */
    public Point2D getPoint2() {
        return this.point2;
    }

    /**
     * Swaps the end points of this segment
     */
    public void swap() {
        Point2D temp = this.point1;
        this.point1 = this.point2;
        this.point2 = temp;
    }

    /**
     * Determines whether or not the specified point lies on this segment
     *
     * @param point the point to check
     * @return <code>true</code> if the specified point lies on this segment,
     *         <code>false</code> otherwise
     */
    public boolean isOnSegment(Point2D point) {
        double x = point.getX();
        double y = point.getY();
        double val = this.a * x + this.b * y + this.c;
        if (val < 0.0) {
            val = -val;
        }
        if (val > EPS) {
            return false;
        }

        if ((x < (this.minx - EPS)) || (x > (this.maxx + EPS))) {
            return false;
        }
        if ((y < (this.miny - EPS)) || (y > (this.maxy + EPS))) {
            return false;
        }
        return true;
    }

    /**
     * Determines whether or not this segment intersects with another segment
     *
     * @param segment2 the segment to check
     * @return <code>true</code> if the specified segment intersects with this
     *         segment, <code>false</code> otherwise
     */
    public boolean intersects(Segment2D segment2) {
        if (segment2 == null) {
            throw new IllegalArgumentException("can't pass null parameter");
        }

        double det = this.a * segment2.b - this.b * segment2.a;
        if (Math.abs(det) < EPS) {
            if (this.isOnSegment(segment2.point1)) {
                return true;
            }
            if (this.isOnSegment(segment2.point2)) {
                return true;
            }
            if (segment2.isOnSegment(this.point1)) {
                return true;
            }
            if (segment2.isOnSegment(this.point2)) {
                return true;
            }
            return false;
        }

        double x = -(segment2.b * this.c - this.b * segment2.c) / det;
        double y = -(-segment2.a * this.c + this.a * segment2.c) / det;

        Point2D p = new Point2D.Double(x, y);
        if (!this.isOnSegment(p)) {
            return false;
        }
        if (!segment2.isOnSegment(p)) {
            return false;
        }
        return true;
    }

    /**
     * Returns distance from the specified point to a closest point on the
     * segment. In case the closest point on the segment's line does not fall
     * inside the segment the distance to a closest segment vertex is returned.
     *
     * @param point the specified point
     * @return distance from the specified point to a closest point on the
     *         segment
     */
    public double getDistanceToPoint(Point2D point) {
        double ax = point.getX() - this.point1.getX();
        double bx = this.point2.getX() - this.point1.getX();
        double ay = point.getY() - this.point1.getY();
        double by = this.point2.getY() - this.point1.getY();

        double denom = bx * bx + by * by;
        if (denom < MathConstants.EPS) {
            return this.point1.distance(point);
        }

        double t = (ax * bx + ay * by) / denom;
        if (t < 0) {
            return this.point1.distance(point);
        }
        if (t > 1) {
            return this.point2.distance(point);
        }
        double x = this.point1.getX() * (1.0 - t) + this.point2.getX() * t;
        double y = this.point1.getY() * (1.0 - t) + this.point2.getY() * t;

        double dx = x - point.getX();
        double dy = y - point.getY();

        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        return "segment : " + this.point1.toString() + "-" +
                this.point2.toString();
    }
}

