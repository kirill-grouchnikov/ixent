package org.jvnet.ixent.algorithms.geometry.voronoi;

import java.awt.geom.Point2D;

public final class VoronoiIndexDiagramInfo {
    private int width;
    private int height;
    private int[][] diagramIndex;
    private Point2D[] centers;
    private int maxRadius;
    private int maxIndex;

    public VoronoiIndexDiagramInfo(int width, int height, int[][] diagramIndex,
                                   Point2D[] centers, int maxRadius,
                                   int maxIndex) {

        this.width = width;
        this.height = height;
        this.diagramIndex = diagramIndex;
        this.centers = centers;
        this.maxIndex = maxIndex;
        this.maxRadius = maxRadius;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int[][] getDiagramIndex() {
        return this.diagramIndex;
    }

    public Point2D[] getCenters() {
        return this.centers;
    }

    public int getMaxIndex() {
        return this.maxIndex;
    }

    public int getMaxRadius() {
        return this.maxRadius;
    }
}

