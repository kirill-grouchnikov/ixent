package org.jvnet.ixent.math.coord;

import static org.jvnet.ixent.math.MathConstants.EPS;

import java.awt.geom.Point2D;
import java.util.List;

import org.jvnet.ixent.math.intersect.ClippingManager;

/**
 * A class representing (rotated) square
 *
 * @author Kirill Grouchnikov
 */
public final class Square2D {
    private Point2D center;
    private double side;
    private double angleInDegrees;

    private double cosa;
    private double sina;

    private Point2D[] points;

    private Segment2D[] sides;

    private double minX, minY, maxX, maxY;

    /**
     * Construct a square with given center, side and angle of rotation.
     *
     * @param pCenter         square center
     * @param pSide           square side
     * @param pAngleInDegrees the angle (in degrees) of some half-axis of this
     *                        square. Any of the four axes can be chosen (i.e.,
     *                        specifying 45<sup>o</sup> is the same as
     *                        135<sup>o</sup>, 225<sup>o</sup> or
     *                        315<sup>o</sup>.
     */
    public Square2D(Point2D pCenter, double pSide, double pAngleInDegrees) {
        this.center = pCenter;
        this.side = pSide;
        this.angleInDegrees = pAngleInDegrees;

        this.cosa = Math.cos(Math.PI * this.angleInDegrees / 180.0);
        this.sina = Math.sin(Math.PI * this.angleInDegrees / 180.0);

        double sider2 = this.side / Math.sqrt(2.0);
        double xc = this.center.getX();
        double yc = this.center.getY();
        this.points = new Point2D[4];

        // compute vertices of this square
        for (int i = 0; i < 4; i++) {
            double al = this.angleInDegrees + 45.0 + i * 90.0;
            this.points[i] = new Point2D.Double(
                    xc + sider2 * Math.cos(al * Math.PI / 180.0),
                    yc + sider2 * Math.sin(al * Math.PI / 180.0));
        }

        // compute spans of this square
        this.minX = this.points[0].getX();
        this.maxX = this.minX;
        this.minY = this.points[0].getY();
        this.maxY = this.minY;
        for (int i = 1; i < 4; i++) {
            double currX = this.points[i].getX();
            double currY = this.points[i].getY();
            if (currX < this.minX) {
                this.minX = currX;
            }
            if (currY < this.minY) {
                this.minY = currY;
            }
            if (currX > this.maxX) {
                this.maxX = currX;
            }
            if (currY > this.maxY) {
                this.maxY = currY;
            }
        }

        // compute the sides of this square
        this.sides = new Segment2D[4];
        this.sides[0] = new Segment2D(this.points[0], this.points[1]);
        this.sides[1] = new Segment2D(this.points[1], this.points[2]);
        this.sides[2] = new Segment2D(this.points[2], this.points[3]);
        this.sides[3] = new Segment2D(this.points[3], this.points[0]);
    }

    /**
     * Return the center of this square.
     *
     * @return the center of this square
     */
    public Point2D getCenter() {
        return center;
    }

    /**
     * Return the side of this square.
     *
     * @return the side of this square
     */
    public double getSide() {
        return side;
    }

    /**
     * Return the angle of rotation of this square.
     *
     * @return the angle of rotation of this square
     */
    public double getAngleInDegrees() {
        return angleInDegrees;
    }

    /**
     * Return the X coordinate of the leftmost point belonging to this square.
     *
     * @return the X coordinate of the leftmost point belonging to this square
     */
    public double getMinX() {
        return minX;
    }

    /**
     * Return the Y coordinate of the topmost point belonging to this square.
     *
     * @return the Y coordinate of the topmost point belonging to this square
     */
    public double getMinY() {
        return minY;
    }

    /**
     * Return the X coordinate of the rightmost point belonging to this square.
     *
     * @return the X coordinate of the rightmost point belonging to this square
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     * Return the Y coordinate of the bottommost point belonging to this
     * square.
     *
     * @return the Y coordinate of the bottommost point belonging to this
     *         square
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * Convert given point to the coordinate system of this square.
     *
     * @param point a point to convert
     * @return the new converted point (in the coordinate system of this
     *         square)
     */
    private Point2D convert(Point2D point) {
        double xd = point.getX() - this.center.getX();
        double yd = point.getY() - this.center.getY();

        double xn = this.cosa * xd + this.sina * yd;
        double yn = -this.sina * xd + this.cosa * yd;

        return new Point2D.Double(xn, yn);
    }

    /**
     * Checks whether a given point lies inside this square. The additional
     * parameter specifies how to treat the edges
     *
     * @param x            X coordinate of a point to check
     * @param y            Y coordinate of a point to check
     * @param edgesAreArea if <code>true</code>, egdes are treated as area (that
     *                     is for a point on the edge, the result is
     *                     <i>true</i>, if <code>false</code>, the edges are
     *                     treated as an outside
     * @return <code>true</code> if the point lies inside this square (treating
     *         the edges accordingly), <code>false</code> otherwise
     */
    public boolean contains(double x, double y, boolean edgesAreArea) {
        double xd = x - this.center.getX();
        double yd = y - this.center.getY();

        double xn = this.cosa * xd + this.sina * yd;
        double yn = -this.sina * xd + this.cosa * yd;

        double side2 = side / 2.0;
        if (edgesAreArea) {
            if ((xn < (-side2 - EPS)) || (xn > (side2 + EPS))) {
                return false;
            }
            if ((yn < (-side2 - EPS)) || (yn > (side2 + EPS))) {
                return false;
            }
            return true;
        }
        else {
            if ((xn < (-side2 + EPS)) || (xn > (side2 - EPS))) {
                return false;
            }
            if ((yn < (-side2 + EPS)) || (yn > (side2 - EPS))) {
                return false;
            }
            return true;
        }
    }

    /**
     * Checks whether a given point lies inside this square. The additional
     * parameter specifies how to treat the edges
     *
     * @param point        a point to check (not null)
     * @param edgesAreArea if <code>true</code>, egdes are treated as area (that
     *                     is for a point on the edge, the result is
     *                     <i>true</i>, if <code>false</code>, the edges are
     *                     treated as an outside
     * @return <code>true</code> if the point lies inside this square (treating
     *         the edges accordingly), <code>false</code> otherwise
     */
    public boolean contains(Point2D point, boolean edgesAreArea) {
        if (point == null) {
            throw new IllegalArgumentException("can't pass null parameter");
        }
        return this.contains(point.getX(), point.getY(), edgesAreArea);
    }

    /**
     * Checks whether a given square lies completely inside this square. The
     * additional parameter specifies how to treat the edges
     *
     * @param square2      a square to check (not null)
     * @param edgesAreArea if <code>true</code>, egdes are treated as area (that
     *                     is for a square touching the edge, the result is
     *                     <i>true</i>, if <code>false</code>, the edges are
     *                     treated as an outside
     * @return <code>true</code> if the second square lies completely inside
     *         this square (treating the edges accordingly), <code>false</code>
     *         otherwise
     */
    public boolean contains(Square2D square2, boolean edgesAreArea) {
        if (square2 == null) {
            throw new IllegalArgumentException("can't pass null parameter");
        }
        for (Point2D currVertex : square2.points) {
            if (!this.contains(currVertex, edgesAreArea)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether a given square intersects with this square. The additional
     * parameter specifies how to treat the edges
     *
     * @param square2      a square to check (not null)
     * @param edgesAreArea if <code>true</code>, egdes are treated as area (that
     *                     is for a square touching the edge, the result is
     *                     <i>true</i>, if <code>false</code>, the edges are
     *                     treated as an outside
     * @return <code>true</code> if the second square intersects with this
     *         square (treating the edges accordingly), <code>false</code>
     *         otherwise
     */
    public boolean intersects(Square2D square2, boolean edgesAreArea) {
        if (square2 == null) {
            throw new IllegalArgumentException("can't pass null parameter");
        }
        for (int i = 0; i < 4; i++) {
            if (this.contains(square2.points[i], edgesAreArea)) {
                return true;
            }
        }

        for (int i = 0; i < 4; i++) {
            if (square2.contains(this.points[i], edgesAreArea)) {
                return true;
            }
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (this.sides[i].intersects(square2.sides[j])) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks whether a given square intersects with any square in a given list.
     * The additional parameter specifies how to treat the edges
     *
     * @param squares      a list of squares to check
     * @param edgesAreArea if <code>true</code>, egdes are treated as area (that
     *                     is for a square touching the edge, the result is
     *                     <i>true</i>, if <code>false</code>, the edges are
     *                     treated as an outside
     * @return <code>true</code> if there is any square in the list that
     *         intersects with this square (treating the edges accordingly),
     *         <code>false</code> otherwise
     */
    public boolean intersects(List<Square2D> squares, boolean edgesAreArea) {
        if (squares == null) {
            return false;
        }
        for (Square2D currSquare : squares) {
            if (this.intersects(currSquare, edgesAreArea)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the area of overlapping of a given square with any square
     * in a given list doesn't exceed a specified margin (in percents).
     *
     * @param squares                    a list of squares to check
     * @param maxAllowedPercentageOfArea the intersection area is computed for
     *                                   this square and all the squares in the
     *                                   list (one by one). The resulting area
     *                                   is checked not to exceed the specified
     *                                   percentage of both squares' area. Must
     *                                   be in 0..1 range
     * @return <code>true</code> if there is any square in the list so that the
     *         area of its intersection with this square is more than specified
     *         portion of any of these two squares' area, <code>false</code>
     *         otherwise
     * @throws IllegalArgumentException if the overlapping area is not in 0..1
     *                                  range
     */
    public boolean partlyOverlaps(List<Square2D> squares,
                                  double maxAllowedPercentageOfArea) {
        if (squares == null) {
            return false;
        }
        if ((maxAllowedPercentageOfArea < 0.0) ||
                (maxAllowedPercentageOfArea > 1.0)) {
            throw new IllegalArgumentException(
                    "Parameter must be in 0..1 range");
        }
        double maxAllowedArea = maxAllowedPercentageOfArea * this.side *
                this.side;
        for (Square2D currSquare : squares) {
            double areaOfIntersection = this.areaOfIntersection(currSquare);
            if (areaOfIntersection > maxAllowedArea) {
                return true;
            }
            if (areaOfIntersection >
                    (maxAllowedPercentageOfArea * currSquare.side *
                    currSquare.side)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return specified vertex of this square.
     *
     * @param index vertex index (in 0..3 range)
     * @return the corresponding vertex
     * @throws IllegalArgumentException is vertex index is not in 0..3 range
     */
    public Point2D getPoint(int index) {
        if ((index < 0) || (index >= 4)) {
            throw new IllegalArgumentException("Only valid in 0..3 range");
        }

        return new Point2D.Double(this.points[index].getX(),
                this.points[index].getY());
    }

    /**
     * Computes the area of intersection of this square with another square.
     *
     * @param square2 another square (not null)
     * @return the area of intersection of this square with a given square
     * @throws IllegalArgumentException if the second square is null
     */
    public double areaOfIntersection(Square2D square2) {
        if (square2 == null) {
            throw new IllegalArgumentException("can't pass null parameter");
        }
        Point2D pConverted1 = this.convert(square2.points[0]);
        Point2D pConverted2 = this.convert(square2.points[1]);
        Point2D pConverted3 = this.convert(square2.points[2]);
        Point2D pConverted4 = this.convert(square2.points[3]);
        Polygon2D convertedPolygon = new Polygon2D(new Point2D[]{pConverted1, pConverted2, pConverted3,
                                                                 pConverted4});
        double side2 = 0.5 * this.side;
        return ClippingManager.intersectionArea(convertedPolygon,
                new Point2D.Double(-side2, -side2),
                new Point2D.Double(side2, side2));
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        return "square: " + this.center.toString() + ", side " + this.side +
                ", angle " + this.angleInDegrees;
    }
}

