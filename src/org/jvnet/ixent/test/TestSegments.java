package org.jvnet.ixent.test;

import java.awt.geom.Point2D;

import junit.framework.TestCase;

import org.jvnet.ixent.math.MathConstants;
import org.jvnet.ixent.math.coord.Segment2D;

/**
 * @author Kirill Grouchnikov
 */
public class TestSegments extends TestCase {
    int n = 9;
    double[][] x = new double[][]{
        {-1.0, 2.0},
        {-1.0, 1.0},
        {-1.0, 2.0},
        {1.0, 1.0},
        {-2.0, -2.0},
        {-3.0, -1.0},
        {-3.0, -2.0},
        {-3.0, -1.0},
        {-1.0, 3.0}
    };
    double[][] y = new double[][]{
        {1.0, 1.0},
        {-3.0, 2.0},
        {-2.0, -1.0},
        {-2.0, 0.0},
        {-1.0, 1.0},
        {-1.0, 0.0},
        {4.0, 2.0},
        {3.0, 3.0},
        {2.0, -2.0}
    };

    Point2D p = new Point2D.Double(1.0, -3.0);

    boolean[][] intersects = new boolean[][]{
        {true, true, false, false, false, false, false, false, true},
        {true, true, true, false, false, false, false, false, true},
        {false, true, true, true, false, false, false, false, true},
        {false, false, true, true, false, false, false, false, true},
        {false, false, false, false, true, true, false, false, false},
        {false, false, false, false, true, true, false, false, false},
        {false, false, false, false, false, false, true, true, false},
        {false, false, false, false, false, false, true, true, false},
        {true, true, true, true, false, false, false, false, true}
    };

    double[] distances = new double[]{
        4.0, 10.0 / Math.sqrt(29.0), Math.sqrt(2.5), 1.0,
        Math.sqrt(13.0), Math.sqrt(13.0), Math.sqrt(34.0), Math.sqrt(40.0),
        Math.sqrt(4.5)
    };

    public TestSegments(String name) {
        super(name);
    }

    private void testSegmentsIntersect(boolean toSwap1, boolean toSwap2) {
        for (int i = 0; i < n; i++) {
            Segment2D segm1 = new Segment2D(
                    new Point2D.Double(x[i][0], y[i][0]),
                    new Point2D.Double(x[i][1], y[i][1]));
            if (toSwap1) {
                segm1.swap();
            }
            for (int j = 0; j < n; j++) {
                Segment2D segm2 = new Segment2D(
                        new Point2D.Double(x[j][0], y[j][0]),
                        new Point2D.Double(x[j][1], y[j][1]));
                if (toSwap2) {
                    segm2.swap();
                }
                boolean result = segm1.intersects(segm2);
                if (result != intersects[i][j]) {
                    System.out.println("1: " + segm1 + ", 2: " + segm2 +
                            " should intersect: " +
                            intersects[i][j]);
                }
                assertTrue(segm1.intersects(segm2) == intersects[i][j]);
            }
        }
    }

    public void testSegmentsIntersect() {
        testSegmentsIntersect(false, false);
        testSegmentsIntersect(false, true);
        testSegmentsIntersect(true, false);
        testSegmentsIntersect(true, true);
    }

    public void testDistances() {
        for (int i = 0; i < n; i++) {
            Segment2D segm = new Segment2D(
                    new Point2D.Double(x[i][0], y[i][0]),
                    new Point2D.Double(x[i][1], y[i][1]));
            double dist = segm.getDistanceToPoint(p);
            double diff = Math.abs(dist - distances[i]);
            if (diff > MathConstants.EPS) {
                System.out.println(segm + " and " + p +
                        " must lie at distance " +
                        distances[i]);
            }
            assertTrue(diff <= MathConstants.EPS);
        }
    }
}
