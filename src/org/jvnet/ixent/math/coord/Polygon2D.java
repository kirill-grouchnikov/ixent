package org.jvnet.ixent.math.coord;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.jvnet.ixent.math.intersect.ClippingManager;

public final class Polygon2D {
    private static int currID = 0;

    public int id;
    private Point2D[] points;
    private Segment2D[] segments;

    public Polygon2D(Point2D[] points) {
        this.id = Polygon2D.currID++;
        this.points = points;
        this.segments = null;
    }

    public Polygon2D(List<Point2D> pointList) {
        this.id = Polygon2D.currID++;
        this.points = new Point2D[pointList.size()];
        int curr = 0;
        for (Point2D currPoint : pointList) {
            this.points[curr++] = currPoint;
        }
        this.segments = null;
    }

    public Polygon2D(Square2D square) {
        this.id = Polygon2D.currID++;
        this.points = new Point2D[4];
        this.points[0] = square.getPoint(0);
        this.points[1] = square.getPoint(1);
        this.points[2] = square.getPoint(2);
        this.points[3] = square.getPoint(3);
        this.segments = null;
    }

    public Point2D[] getPoints() {
        return this.points;
    }

    public int getPointsCount() {
        if (this.points == null) {
            return 0;
        }
        return this.points.length;
    }

    public Polygon2D simplify(double minDistanceBetweenVertices,
                              int maxVertexJump) {
        double d2 = minDistanceBetweenVertices * minDistanceBetweenVertices;
        LinkedList<Point2D> simplifiedPoints = new LinkedList<Point2D>();
        simplifiedPoints.addLast(this.points[0]);
        Point2D prevPoint = this.points[0];
        int currJump = 0;
        for (int i = 1; i < this.points.length; i++) {
            Point2D currPoint = this.points[i];
            // current point is added if one of the following is true:
            // 1. current jump (vertices disposed of) is too big
            // 2. distance from the last point taken is too large
            boolean toTakeCurrent = false;
            if (currJump >= maxVertexJump) {
                toTakeCurrent = true;
            }
            else {
                // check distance
                double dist2 = prevPoint.distanceSq(currPoint);
                if (dist2 >= d2) {
                    toTakeCurrent = true;
                }
//        System.out.println(prevPoint.distance(currPoint));
            }
            if (toTakeCurrent) {
                simplifiedPoints.addLast(currPoint);
                prevPoint = currPoint;
                currJump = 0;
            }
            else {
                currJump++;
            }
        }
//    System.out.println(this.getPointsCount() + "-" + simplifiedPointArray.length+".");
        return new Polygon2D(simplifiedPoints);
    }

    public double area() {
        return ClippingManager.generalPolygonArea(this.points);
    }

    public int getFillWinding(double xp, double yp) {
        int rayCount = 0;
        while (true) {
            if (rayCount++ > 50) {
                // unable to find winding
                return 0;
            }
            double xr = xp + 1.0 + Math.random();
            double yr = yp + (xr - xp) + Math.random();
            Ray2D ray = new Ray2D(new Vertex2D(xp, yp), new Vertex2D(xr, yr));
            // intersect with each side
            int windingCounter = 0;
            boolean fireAnotherRay = false;
            int pointCount = this.points.length;
            for (int i1 = 0; i1 < pointCount; i1++) {
                int i2 = (i1 != (pointCount - 1)) ? (i1 + 1) : 0;
                Point2D p1 = this.points[i1];
                Point2D p2 = this.points[i2];
                // enough to check start point (end point is start of the next segment)
                if (ray.isPointOnRay(p1)) {
                    fireAnotherRay = true;
                    break;
                }
                if (ray.intersectsWithSegment(p1, p2)) {
                    if (ray.onPositiveSide(p1)) {
                        windingCounter++;
                    }
                    else {
                        windingCounter--;
                    }
                }
            }
            if (fireAnotherRay) {
                continue;
            }
            return windingCounter;
        }
    }

    public double getMinX() {
        if (this.points == null) {
            return 0.0;
        }
        double res = this.points[0].getX();
        for (int i = 1; i < this.points.length; i++) {
            if (res > this.points[i].getX()) {
                res = this.points[i].getX();
            }
        }
        return res;
    }

    public double getMaxX() {
        if (this.points == null) {
            return 0.0;
        }
        double res = this.points[0].getX();
        for (int i = 1; i < this.points.length; i++) {
            if (res < this.points[i].getX()) {
                res = this.points[i].getX();
            }
        }
        return res;
    }

    public double getMinY() {
        if (this.points == null) {
            return 0.0;
        }
        double res = this.points[0].getY();
        for (int i = 1; i < this.points.length; i++) {
            if (res > this.points[i].getY()) {
                res = this.points[i].getY();
            }
        }
        return res;
    }

    public double getMaxY() {
        if (this.points == null) {
            return 0.0;
        }
        double res = this.points[0].getY();
        for (int i = 1; i < this.points.length; i++) {
            if (res < this.points[i].getY()) {
                res = this.points[i].getY();
            }
        }
        return res;
    }

    public double getDistanceToPoint(Point2D point) {
        if (this.points == null) {
            return 0.0;
        }

        int n = this.points.length;
        if (n == 0) {
            return 0.0;
        }
        if (n == 1) {
            return this.points[0].distance(point);
        }

        if (this.segments == null) {
            // compute all the segments (once)
            this.segments = new Segment2D[n];
            for (int i = 0; i < (n - 1); i++) {
                this.segments[i] =
                        new Segment2D(this.points[i], this.points[i + 1]);
            }
            this.segments[n - 1] =
                    new Segment2D(this.points[n - 1], this.points[0]);
        }
        double minDistance = this.points[0].distance(point);
        for (Segment2D currSegm : this.segments) {
            double dist = currSegm.getDistanceToPoint(point);
            minDistance = Math.min(minDistance, dist);
        }
        return minDistance;
    }

    public double getMinimalHalfDimension() {
        return 0.5 * Math.min(this.getMaxX() - this.getMinX(),
                this.getMaxY() - this.getMinY());
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("polygon (");
        sb.append((this.points == null) ? 0 : this.points.length);
        sb.append(" points):");
        if (this.points != null) {
            for (int i = 0; i < this.points.length; i++) {
                sb.append(points[i].toString() + "\n");
            }
        }
        return sb.toString();
    }
}

