package org.jvnet.ixent.math.coord;

import java.awt.geom.Point2D;

public final class Geom2DHelper {
    public static Rectangle2D getBoundingRectangle(Point2D[] points,
                                                   double inflate) {
        if (points == null) {
            return null;
        }
        int count = points.length;
        if (count == 0) {
            return null;
        }

        double minX = points[0].getX();
        double maxX = points[0].getX();
        double minY = points[0].getY();
        double maxY = points[0].getY();
        for (int i = 1; i < count; i++) {
            double x = points[i].getX();
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            double y = points[i].getY();
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }
        return new Rectangle2D(
                new Point2D.Double(minX - inflate, minY - inflate),
                new Point2D.Double(maxX + inflate, maxY + inflate));
    }

    public static Rectangle2D getBoundingRectangle(Point2D[] points) {
        return Geom2DHelper.getBoundingRectangle(points, 0.0);
    }

    public static Rectangle2D getBoundingRectangle(Vertex2D[] points,
                                                   double inflate) {
        if (points == null) {
            return null;
        }
        int count = points.length;
        if (count == 0) {
            return null;
        }

        double minX = points[0].getX();
        double maxX = points[0].getX();
        double minY = points[0].getY();
        double maxY = points[0].getY();
        for (int i = 1; i < count; i++) {
            double x = points[i].getX();
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            double y = points[i].getY();
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }
        return new Rectangle2D(
                new Point2D.Double(minX - inflate, minY - inflate),
                new Point2D.Double(maxX + inflate, maxY + inflate));
    }

    public static Rectangle2D getBoundingRectangle(Vertex2D[] points) {
        return Geom2DHelper.getBoundingRectangle(points, 0.0);
    }

    public static Rectangle2D getBoundingRectangle(Polygon2D polygon) {
        if (polygon == null) {
            return null;
        }
        return Geom2DHelper.getBoundingRectangle(polygon.getPoints());
    }

    public static Point2D getMidPoint(Point2D point1, Point2D point2) {
        return new Point2D.Double(0.5 * (point1.getX() + point2.getX()),
                0.5 * (point1.getY() + point2.getY()));
    }
}

