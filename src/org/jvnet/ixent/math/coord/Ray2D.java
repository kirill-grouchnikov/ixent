package org.jvnet.ixent.math.coord;

import java.awt.geom.Point2D;

import org.jvnet.ixent.math.MathConstants;

public final class Ray2D {
    private Vertex2D origin;
    private Vertex2D pointOnRay;

    // for intersections
    private double k;
    private double dxr, dyr;
    // line equation: ax+by+c=0
    private double a, b, c;

    private boolean isRightwards;
    private boolean isUpwards;

    public Ray2D(Vertex2D origin, Vertex2D pointOnRay) {
        this.origin = origin;
        this.pointOnRay = pointOnRay;
        double xr0 = origin.getX();
        double yr0 = origin.getY();
        double xr1 = pointOnRay.getX();
        double yr1 = pointOnRay.getY();

        this.isRightwards = (xr1 >= xr0);
        this.isUpwards = (yr1 >= yr0);

        // for intersections
        this.k = (yr1 - yr0) / (xr1 - xr0);
        // compute line equation: ax+by+c=0
        double dx = xr0 - xr1;
        double dy = yr0 - yr1;
        double ddx = (dx > 0) ? dx : -dx;
        double ddy = (dy > 0) ? dy : -dy;
        if (ddx < MathConstants.EPS) {
            this.a = 1.0;
            this.b = 0.0;
            this.c = -xr0;
        }
        else {
            if (ddy < MathConstants.EPS) {
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

        this.dxr = xr1 - xr0;
        this.dyr = yr1 - yr0;
    }

    public Vertex2D getOrigin() {
        return this.origin;
    }

    public Vertex2D getPointOnRay() {
        return this.pointOnRay;
    }

    public double getK() {
        return this.k;
    }

    public boolean onPositiveSide(double xp, double yp) {
        return ((this.a * xp + this.b * yp + this.c) >= 0.0);
    }

    public boolean onPositiveSide(Point2D point) {
        return this.onPositiveSide(point.getX(), point.getY());
    }

    public boolean isPointOnRay(double xp, double yp) {
        double val = this.a * xp + this.b * yp + this.c;
        if (val < 0.0) {
            val = -val;
        }
        return val < MathConstants.EPS;
    }

    public boolean isPointOnRay(Point2D point) {
        return this.isPointOnRay(point.getX(), point.getY());
    }

    public boolean isGoingRightwards() {
        return this.isRightwards;
    }

    public boolean intersectsWithSegment(Point2D p1, Point2D p2) {
        // both on same side?
        boolean side1 = this.onPositiveSide(p1);
        boolean side2 = this.onPositiveSide(p2);
        if ((side1 && side2) || ((!side1) && (!side2))) {
            return false;
        }

        double xr1 = this.origin.getX();
        double yr1 = this.origin.getY();

        if (this.isRightwards && this.isUpwards) {
            // both to the left of the origin
            if ((p1.getX() < xr1) && (p2.getX() < xr1)) {
                return false;
            }
            // both to the bottom of the origin
            if ((p1.getY() < yr1) && (p2.getY() < yr1)) {
                return false;
            }
        }

        double xp1 = p1.getX();
        double yp1 = p1.getY();
        double xp2 = p2.getX();
        double yp2 = p2.getY();

        double dxp = xp2 - xp1;
        double dyp = yp2 - yp1;

        double A = dyp - this.dyr * dxp / this.dxr;
        if ((A > -MathConstants.EPS) && (A < MathConstants.EPS)) {
            // parallel
            return false;
        }
        double B = yr1 - yp1 + this.dyr * (xp1 - xr1) / this.dxr;
        // on segment
        double t = B / A;
        // on ray
        double tau = (dxp * t + (xp1 - xr1)) / this.dxr;

        // 0..1 on segment
        if ((t < 0.0) || (t > 1.0)) {
            return false;
        }

        // 0.. on ray
        if (tau < 0.0) {
            return false;
        }

        return true;
    }

    public boolean intersectsWithSegment(Segment2D segment) {
        return this.intersectsWithSegment(segment.getPoint1(),
                segment.getPoint2());
    }
}

