package org.jvnet.ixent.algorithms.geometry.spacefilling;

import org.jvnet.ixent.math.coord.Vertex2D;

public abstract class SpaceFillingCurveBase implements SpaceFillingCurve {
    protected enum PixelStatus {
        notTaken, covered, diskCenter
    };

    protected PixelStatus[][] pixelStatus;
    protected int centerCount;
    protected Vertex2D[] centers;

    protected int minDistanceBetweenCenters;

    public Vertex2D[] getCenters() {
        return this.centers;
    }

    public int getMinDistanceBetweenCenters() {
        return this.minDistanceBetweenCenters;
    }
}

