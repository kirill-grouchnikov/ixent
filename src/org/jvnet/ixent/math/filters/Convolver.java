package org.jvnet.ixent.math.filters;

import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.PointND;

public class Convolver {
    // greyscale bitmap
    private IndexBitmapObject gsImage;
    private int[][] gsBitmap;

    private int bitmapWidth, bitmapHeight;

    public Convolver(IndexBitmapObject gsImage) {
        this.bitmapHeight = gsImage.getHeight();
        this.bitmapWidth = gsImage.getWidth();
        this.gsImage = gsImage;
        this.gsBitmap = this.gsImage.getBitmap();
    }

    // convolves a single pixel using a filter
    public double convolve(int column, int row, Filter filter) {
        int fSize = filter.getSize();
        int startCol = column + filter.getBoundL();
        if (startCol < 0) {
            startCol = 0;
        }
        int endCol = column + filter.getBoundR();
        if (endCol >= this.bitmapWidth) {
            endCol = this.bitmapWidth - 1;
        }
        int startRow = row + filter.getBoundT();
        if (startRow < 0) {
            startRow = 0;
        }
        int endRow = row + filter.getBoundB();
        if (endRow >= this.bitmapHeight) {
            endRow = this.bitmapHeight - 1;
        }

        double result = 0.0;
        for (int c = startCol; c <= endCol; c++) {
            for (int r = startRow; r <= endRow; r++) {
                result += (this.gsBitmap[c][r] * filter.getUnchecked(c
                        - column, r - row));
            }
        }
        return result;
    }

    // convolves a single pixel using the filter bank
    public double[] convolve(int column, int row, Filter[] filters) {
        int size = filters.length;
        double[] result = new double[size];
        for (int f = 0; f < size; f++) {
            result[f] = this.convolve(column, row, filters[f]);
        }
        return result;
    }

    // convolves the whole image using the filter bank
    public PointND[][] convolve(Filter[] filters) {
        PointND[][] result = new PointND[this.bitmapWidth][this.bitmapHeight];
        for (int col = 0; col < this.bitmapWidth; col++) {
            for (int row = 0; row < this.bitmapHeight; row++) {
                result[col][row] =
                        new PointND(this.convolve(col, row, filters));
            }
        }
        return result;
    }
}