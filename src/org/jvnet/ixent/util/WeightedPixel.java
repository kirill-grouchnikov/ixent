package org.jvnet.ixent.util;

/**
 * Helper class used in coloring the pixels according to area of intersection
 * with mosaic tiles
 */
public class WeightedPixel {
    private int column;
    private int row;
    private double area;
    private double additionalValue;

    public WeightedPixel(int pColumn, int pRow, double pArea) {
        this.column = pColumn;
        this.row = pRow;
        this.area = pArea;
        this.additionalValue = 0.0;
    }

    public WeightedPixel(int pColumn, int pRow, double pArea,
                         double pDistanceToBoundary) {
        this.column = pColumn;
        this.row = pRow;
        this.area = pArea;
        this.additionalValue = pDistanceToBoundary;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getAdditionalValue() {
        return additionalValue;
    }

    public void setAdditionalValue(double additionalValue) {
        this.additionalValue = additionalValue;
    }
}
