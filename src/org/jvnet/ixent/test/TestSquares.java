package org.jvnet.ixent.test;

import static org.jvnet.ixent.math.MathConstants.EPS;

import java.awt.geom.Point2D;

import junit.framework.TestCase;

import org.jvnet.ixent.math.coord.Square2D;

/**
 * @author Kirill Grouchnikov
 */
public class TestSquares extends TestCase {
    private class Range {
        public double x;
        public double y1;
        public double y2;

        public Range(double px, double py1, double py2) {
            this.x = px;
            this.y1 = Math.min(py1, py2);
            this.y2 = Math.max(py1, py2);
        }

        public boolean matches(double x) {
            return (Math.abs(x - this.x) < EPS);
        }

        public boolean isInside(double y) {
            if (y < (this.y1 - EPS)) {
                return false;
            }
            if (y > (this.y2 + EPS)) {
                return false;
            }
            return true;
        }

        /**
         * Returns the <code>String</code> representation of this object
         *
         * @return a <code>String</code> representing this object
         */
        public String toString() {
            return x + ":" + y1 + "-" + y2;
        }
    }

    int n = 7;
    double[] x = new double[]{2.5, -2.0, -1.0, 0.5, 0.5, -2.0, 0.5};
    double[] y = new double[]{0.5, 0.0, -2.0, 1.5, 1.5, 0.0, -3.5};
    double[] side = new double[]{Math.sqrt(5.0), Math.sqrt(8.0), 2.0, 3.0, 1.0,
                                 Math.sqrt(2.0), Math.sqrt(5.0)};
    double[] angle = new double[]{
        180.0 * Math.acos(2.0 / Math.sqrt(5.0)) / Math.PI,
        45.0, 0.0, 0.0, 0.0, 45.0,
        180.0 * Math.acos(-2.0 / Math.sqrt(5.0)) / Math.PI};

    double[][] cx = new double[][]{
        {4., 3, 1, 2}, {0., -2, -4, -2}, {0., 0, -2, -2}, {2., 2, -1, -1},
        {1., 1, 0, 0}, {-1., -2, -3, -2}, {2., 0, -1, 1}};

    double[][] cy = new double[][]{
        {0., 2, 1, -1}, {0., 2, 0, -2}, {-3., -1, -1, -3}, {0., 3, 3, 0},
        {1., 2, 2, 1}, {0., 1, 0, -1}, {-3., -2, -4, -5}};

    boolean[][] intersects = new boolean[][]{
        {true, false, false, true, true, false, false},
        {false, true, true, true, false, true, false},
        {false, true, true, false, false, true, true},
        {true, true, false, true, true, true, false},
        {true, false, false, true, true, false, false},
        {false, true, true, true, false, true, false},
        {false, false, true, false, false, false, true}
    };

    Range[][] contains = new Range[][]{
        {new Range(1, 1, 1), new Range(1.5, 0, 1.25), new Range(2, -1, 1.5),
         new Range(2.5, -0.75, 1.75), new Range(3, -.5, 2),
         new Range(3.5, -.25, 1), new Range(4, 0, 0)},
        {new Range(-4, 0, 0), new Range(-3.5, -.5, .5), new Range(-3, -1, 1),
         new Range(-2.5, -1.5, 1.5), new Range(-2, -2, 2),
         new Range(-1.5, -1.5, 1.5), new Range(-1, -1, 1),
         new Range(-.5, -.5, .5), new Range(0, 0, 0)},
        {new Range(-2, -3, -1), new Range(-1.5, -3, -1), new Range(-1, -3, -1),
         new Range(-.5, -3, -1), new Range(0, -3, -1)},
        {new Range(-1, 0, 3), new Range(-.5, 0, 3), new Range(0, 0, 3),
         new Range(.5, 0, 3), new Range(1, 0, 3), new Range(1.5, 0, 3),
         new Range(2, 0, 3)},
        {new Range(0, 1, 2), new Range(0.5, 1, 2), new Range(1, 1, 2)},
        {new Range(-3, 0, 0), new Range(-2.5, -.5, .5), new Range(-2, -1, 1),
         new Range(-1.5, -.5, .5), new Range(-1, 0, 0)},
        {new Range(-1, -4, -4), new Range(-.5, -4.25, -3),
         new Range(0, -4.5, -2), new Range(.5, -4.75, -2.25),
         new Range(1, -5, -2.5), new Range(1.5, -4, -2.75),
         new Range(2, -3, -3)},
    };


    public TestSquares(String name) {
        super(name);
    }

    public void testSquaresPoints() {
        for (int i = 0; i < n; i++) {
            Square2D sq = new Square2D(new Point2D.Double(x[i], y[i]),
                    side[i], angle[i]);
            for (int j = 0; j < 4; j++) {
                Point2D currVert = sq.getPoint(j);
                boolean found = false;
                for (int k = 0; k < 4; k++) {
                    Point2D currToCheck = new Point2D.Double(cx[i][k],
                            cy[i][k]);
                    if (currToCheck.distanceSq(currVert) < EPS) {
                        found = true;
                    }
                }
                if (found == false) {
                    System.err.println(i + ": " + sq + ", " + j + " point " +
                            currVert + " not found ");
                }
                assertTrue(found);
            }
        }
    }

    public void testSquaresIntersect(double deltaAngle1, double deltaAngle2) {
        for (int i = 0; i < n; i++) {
            Square2D sq1 = new Square2D(new Point2D.Double(x[i], y[i]),
                    side[i], angle[i] + deltaAngle1);
            for (int j = 0; j < n; j++) {
                Square2D sq2 = new Square2D(new Point2D.Double(x[j], y[j]),
                        side[j], angle[j] + deltaAngle2);
                boolean result = sq1.intersects(sq2, true);
                if (result != intersects[i][j]) {
                    System.err.println(i + ": " + sq1 + ", " + j + ": " + sq2 +
                            " should intersect: " +
                            intersects[i][j]);
                }
                assertTrue(sq1.intersects(sq2, true) == intersects[i][j]);
            }
        }
    }

    public void testSquaresIntersect() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                testSquaresIntersect(i * 90.0, j * 90.0);
            }
        }
    }

    public boolean shouldBeInside(int sqIndex, double x, double y) {
        Range[] ranges = contains[sqIndex];
        for (int i = 0; i < ranges.length; i++) {
            Range currRange = ranges[i];
            if (currRange.matches(x)) {
                return currRange.isInside(y);
            }
        }
        return false;
    }

    public void testSquaresContainsGrid() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 4; j++) {
                Square2D sq = new Square2D(new Point2D.Double(x[i], y[i]),
                        side[i], angle[i] + j * 90.0);
                for (int x2 = -15; x2 <= 15; x2++) {
                    double cx = 0.5 * (double) x2;
                    for (int y2 = -15; y2 <= 15; y2++) {
                        double cy = .5 * (double) y2;
                        Point2D p = new Point2D.Double(cx, cy);
                        boolean res = sq.contains(p, true);
                        boolean exp = shouldBeInside(i, cx, cy);
                        if (res != exp) {
                            System.err.println(i + ": " + sq + ", " +
                                    p.toString() +
                                    " should contain: " + exp);
                        }
                        assertTrue(res == exp);
                    }
                }
            }
        }
    }
}
