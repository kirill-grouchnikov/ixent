package org.jvnet.ixent.math.coord;

import java.awt.geom.Point2D;

public final class Vector2D {
    private Point2D p1;
    private Point2D p2;
    private double x;
    private double y;

    private Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Point2D p1, Point2D p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.x = p2.getX() - p1.getX();
        this.y = p2.getY() - p1.getY();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector2D getNormalizedVector() {
        double norm = this.getNorm();
        if (norm > 0.0) {
            return new Vector2D(this.x / norm, this.y / norm);
        }
        else {
            return this;
        }
    }

    public double getNorm() {
        double norm = Math.sqrt(this.x * this.x + this.y * this.y);
        return norm;
    }

    public double dot(Vector2D vector2) {
        return (this.x * vector2.x + this.y * vector2.y);
    }

    public double getAngleInDegrees(Vector2D vector2) {
        double cosa = this.dot(vector2) / (this.getNorm() * vector2.getNorm());
        return 180.0 * Math.acos(cosa) / Math.PI;
    }
}

