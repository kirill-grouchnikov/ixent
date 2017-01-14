package org.jvnet.ixent.math.coord;

import java.awt.geom.Point2D;

public final class Rectangle2D {
    private Point2D pointTL;
    private Point2D pointBR;

    private double xl, xr, yt, yb;

    public Rectangle2D(Point2D pointTL, Point2D pointBR) {
        this.pointTL = new Point2D.Double(pointTL.getX(), pointTL.getY());
        this.pointBR = new Point2D.Double(pointBR.getX(), pointBR.getY());

        this.xl = this.pointTL.getX();
        this.xr = this.pointBR.getX();
        this.yt = this.pointTL.getY();
        this.yb = this.pointBR.getY();
    }

    public Rectangle2D(int x, int y, int width, int height) {
        this.xl = x;
        this.yt = y;
        this.xr = x + width;
        this.yb = y + height;

        this.pointTL = new Point2D.Double(this.xl, this.yt);
        this.pointBR = new Point2D.Double(this.xr, this.yb);
    }

    public Object clone() {
        return new Rectangle2D(this.pointTL, this.pointBR);
    }

    public Point2D getPointTL() {
        return this.pointTL;
    }

    public Point2D getPointBR() {
        return this.pointBR;
    }

    public double area() {
        return ((this.xr - this.xl) * (this.yb - this.yt));
    }

    public boolean contains(double x, double y) {
        return ((x >= this.xl) && (x <= this.xr) &&
                (y >= this.yt) && (y <= this.yb));
    }

    public double getX() {
        return this.xl;
    }

    public double getWidth() {
        return (this.xr - this.xl);
    }

    public double getY() {
        return this.yt;
    }

    public double getHeight() {
        return (this.yb - this.yt);
    }
}

