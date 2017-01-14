package org.jvnet.ixent.algorithms.graphics.edgedetection;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Stack;

import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.Vertex2D;
import org.jvnet.ixent.math.filters.GaussConvolution;

/**
 * Canny edge detector
 */
public class CannyEdgeDetector implements EdgeDetector {
    /**
     * Input image width
     */
    private int width;

    /**
     * Input image height
     */
    private int height;

    /**
     * Input image pixels in black and white (brightness) scale
     */
    private int[][] bwImPixels;
    private int[][] redImPixels;
    private int[][] greenImPixels;
    private int[][] blueImPixels;

    private int[][] magnitudes;
    private double[][] theta;


    /**
     * Constructor. 
     */
    public CannyEdgeDetector() {
    }

    /**
     * @param im input image
     */
    public void init(BufferedImage im) {
        this.width = im.getWidth();
        this.height = im.getHeight();

        this.bwImPixels = new int[width][height];
        this.redImPixels = new int[width][height];
        this.greenImPixels = new int[width][height];
        this.blueImPixels = new int[width][height];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int currRGB = im.getRGB(col, row);
                int red = currRGB & 0x00FF0000;
                red >>>= 16;
                int green = currRGB & 0x0000FF00;
                green >>>= 8;
                int blue = currRGB & 0x000000FF;

                this.redImPixels[col][row] = (int) red;
                this.greenImPixels[col][row] = (int) green;
                this.blueImPixels[col][row] = (int) blue;

                // compute perceived brightness of the pixel
                int bw = (222 * red + 707 * green + 71 * blue) / 1000;
                this.bwImPixels[col][row] = (int) bw;
            }
        }
    }

    /**
     * @param indexObject input image as index bitmap object
     */
    public void init(IndexBitmapObject indexObject) {
        this.width = indexObject.getWidth();
        this.height = indexObject.getHeight();

        this.bwImPixels = new int[width][height];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                this.bwImPixels[col][row] =
                        (int) indexObject.getValue(col, row);
            }
        }
    }

    /**
     * Compute non-maxima suppression sector of given angle
     *
     * @param theta angle
     * @return its sector
     */
    private int nmsSector(double theta) {
        double thetaD = theta * 180.0 / Math.PI;
        thetaD += 270;
        thetaD = thetaD % 360;
        if ((thetaD >= 337.5) || (thetaD < 22.5)
                || ((thetaD >= 157.5) && (thetaD < 202.5))) {
            return 0;
        }
        if (((thetaD >= 22.5) && (thetaD < 67.5))
                || ((thetaD >= 202.5) && (thetaD < 247.5))) {
            return 1;
        }
        if (((thetaD >= 67.5) && (thetaD < 112.5))
                || ((thetaD >= 247.5) && (thetaD < 292.5))) {
            return 2;
        }
        if (((thetaD >= 112.5) && (thetaD < 157.5))
                || ((thetaD >= 292.5) && (thetaD < 337.5))) {
            return 3;
        }
        return 0;
    }

    /**
     * Perform non-maxima suppression for a single pixel
     *
     * @param magnitudes values after Gaussian smoothing
     * @param sector     sector
     * @param col        pixel column
     * @param row        pixel row
     * @param lowThresh  lower threshold
     * @return value after suppression
     */
    private int suppress(int[][] magnitudes, int sector, int col, int row,
                         int lowThresh) {
        int curr = magnitudes[col][row];
        if (curr < lowThresh) {
            return 0;
        }
        switch (sector) {
            case 0:
                if ((magnitudes[col + 1][row] >= curr) ||
                        (magnitudes[col - 1][row] > curr)) {
                    return 0;
                }
                return curr;
            case 1:
                if ((magnitudes[col + 1][row + 1] >= curr)
                        || (magnitudes[col - 1][row - 1] > curr)) {
                    return 0;
                }
                return curr;
            case 2:
                if ((magnitudes[col][row + 1] >= curr) ||
                        (magnitudes[col][row - 1] > curr)) {
                    return 0;
                }
                return curr;
            case 3:
                if ((magnitudes[col + 1][row - 1] >= curr)
                        || (magnitudes[col - 1][row + 1] > curr)) {
                    return 0;
                }
                return curr;
        }
        return 0;
    }

    /**
     * Perform non-maxima suppression over the whole image
     *
     * @param magnitudes   values after Gaussian smoothing
     * @param orientations gradient orientations at each pixel
     * @param lowThresh    lower threshold
     */
    private void suppressNonMaxima(int[][] magnitudes, double[][] orientations,
                                   int lowThresh) {
        // setLocation first/last column
        for (int row = 0; row < this.height; row++) {
            magnitudes[this.width - 1][row] = 0;
            magnitudes[0][row] = 0;
        }
        // setLocation first/last row
        for (int col = 0; col < this.width; col++) {
            magnitudes[col][this.height - 1] = 0;
            magnitudes[col][0] = 0;
        }

        // others
        for (int col = 1; col < (this.width - 1); col++) {
            for (int row = 1; row < (this.height - 1); row++) {
                magnitudes[col][row] =
                        suppress(magnitudes,
                                nmsSector(orientations[col][row]), col, row,
                                lowThresh);
            }
        }
    }

    /**
     * Track an edge from a given point
     *
     * @param magnitudes suppressed magnitudes
     * @param tracked    helper array (true for each tracked pixel)
     * @param lowThresh  lower threshold
     * @param x          start pixel column
     * @param y          start pixel row
     */
    private void trackFromSinglePoint(int[][] magnitudes,
                                      boolean[][] tracked, int lowThresh,
                                      int x, int y) {

        // implement BFS with stack of points
        Stack<Vertex2D> to_track = new Stack<Vertex2D>();
        Vertex2D curr = new Vertex2D(x, y);
        // push the starting point
        to_track.push(curr);
        while (!to_track.empty()) {
            curr = (Vertex2D) to_track.pop();
            x = (int) curr.getX();
            y = (int) curr.getY();

            // check if already tracked
            if (tracked[x][y]) {
                continue;
            }

            // go over all neighbours
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    // check if the same point
                    if ((dx == 0) && (dy == 0)) {
                        continue;
                    }
                    // check if valid coordinates
                    int newX = x + dx;
                    if ((newX < 0) || (newX >= this.width)) {
                        continue;
                    }
                    int newY = y + dy;
                    if ((newY < 0) || (newY >= this.height)) {
                        continue;
                    }

                    if (magnitudes[newX][newY] > lowThresh) {
                        to_track.push(new Vertex2D(newX, newY));
                    }
                }
            }
            tracked[x][y] = true;
        }
    }

    private void computeMagnitudeAndTheta(int[][] inputChannelMap) {
        GaussConvolution gaussConvolution = new GaussConvolution(1.0, 2);
        int[][] smoothedPixels = gaussConvolution.getSmoothedBitmap(
                inputChannelMap, this.width, this.height);

        //    long time1 = System.currentTimeMillis();
        // 2. Compute gradient magnitude and orientation
        int maxP = 0, maxQ = 0;
        for (int col = 0; col < (this.width - 1); col++) {
            for (int row = 0; row < (this.height - 1); row++) {
                int p = (smoothedPixels[col][row + 1] -
                        smoothedPixels[col][row]
                        + smoothedPixels[col + 1][row + 1] -
                        smoothedPixels[col + 1][row]);
                if (p < 0) {
                    p = -p;
                }
                if (maxP < p) {
                    maxP = p;
                }
                int q = (smoothedPixels[col][row] -
                        smoothedPixels[col + 1][row]
                        + smoothedPixels[col][row + 1] -
                        smoothedPixels[col + 1][row + 1]);
                if (q < 0) {
                    q = -q;
                }
                if (maxQ < q) {
                    maxQ = q;
                }
            }
        }

        int N = (maxP > maxQ) ? maxP : maxQ;
        int N2 = 2 * N + 1;
        double[][] atans = new double[N2][N2];
        int[][] sqrs = new int[N2][N2];
        for (int i = 0; i < N2; i++) {
            for (int j = 0; j < N2; j++) {
                double p = (double) (i - N) / 2.0;
                double q = (double) (j - N) / 2.0;
                sqrs[i][j] = (int) Math.sqrt(p * p + q * q);
                atans[i][j] = Math.atan2(q, p);
            }
        }

        this.magnitudes = new int[this.width][this.height];
        this.theta = new double[this.width][this.height];
        for (int col = 0; col < (this.width - 1); col++) {
            for (int row = 0; row < (this.height - 1); row++) {
                int p2 = (smoothedPixels[col][row + 1] -
                        smoothedPixels[col][row]
                        + smoothedPixels[col + 1][row + 1] -
                        smoothedPixels[col + 1][row]);
                int q2 = (smoothedPixels[col][row] -
                        smoothedPixels[col + 1][row]
                        + smoothedPixels[col][row + 1] -
                        smoothedPixels[col + 1][row + 1]);
                magnitudes[col][row] = sqrs[p2 + N][q2 + N];
                theta[col][row] = atans[p2 + N][q2 + N];
            }
        }
    }

    /**
     * Apply Canny edge detection algorithm to the whole image
     *
     * @param fuzzyness  fuzzyness type (soft or exact)
     * @param highThresh higher threshold
     * @param lowThresh  lower threshold
     * @return 1-D array of values. The value at each pixel specifies the
     *         probability of an edge passing through this point
     */
    private int[][] applyCannyAlgorithm(int[][] inputChannelMap,
                                        EdgeFuzzyness fuzzyness, int highThresh,
                                        int lowThresh) {
        // resulting value-map
        int[][] valueMap = new int[this.width][this.height];
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                valueMap[col][row] = 0;
            }
        }

        this.computeMagnitudeAndTheta(inputChannelMap);

        // Non maxima suppression
        suppressNonMaxima(magnitudes, theta, lowThresh);

        if (fuzzyness == EdgeFuzzyness.exact) {
            // Thresholding
            boolean tracked[][] = new boolean[this.width][this.height];
            for (int col = 0; col < this.width; col++) {
                for (int row = 0; row < this.height; row++) {
                    tracked[col][row] = false;
                    if (magnitudes[col][row] < lowThresh) {
                        magnitudes[col][row] = 0;
                    }
                }
            }
            // track from all points which are above the high threshold
            for (int col = 0; col < this.width; col++) {
                for (int row = 0; row < this.height; row++) {
                    if ((magnitudes[col][row] >= highThresh) &&
                            (!tracked[col][row])) {
                        trackFromSinglePoint(magnitudes, tracked, lowThresh, col,
                                row);
                    }
                }
            }
            // nullify all unmarked points
            for (int col = 0; col < this.width; col++) {
                for (int row = 0; row < this.height; row++) {
                    if (!tracked[col][row]) {
                        magnitudes[col][row] = 0;
                    }
                }
            }
        }

        // resulting value-map
        for (int col = 0; col < this.width - 1; col++) {
            for (int row = 0; row < this.height - 1; row++) {
                int value = magnitudes[col][row];
                if (value > 0) {
                    value = Math.min(255, (int) (100 + 3 * value));
                }
                valueMap[col][row] = value;
            }
        }

        return valueMap;
    }

    /**
     * Apply Canny edge detection algorithm to the whole image
     *
     * @param fuzzyness  fuzzyness type (soft or exact)
     * @param highThresh higher threshold
     * @param lowThresh  lower threshold
     * @return 1-D array of values. The value at each pixel specifies the
     *         probability of an edge passing through this point
     */
    private int[][] applyCannyAlgorithmMultichannel(int[][] mainInputChannel,
                                                    List<int[][]> secondaryChannels,
                                                    EdgeFuzzyness fuzzyness, int highThresh,
                                                    int lowThresh) {

        // resulting value-map
        int[][] valueMap = this.applyCannyAlgorithm(mainInputChannel, fuzzyness,
                highThresh, lowThresh);
        for (int[][] currChannel : secondaryChannels) {
            int[][] currMap = this.applyCannyAlgorithm(currChannel, fuzzyness,
                    highThresh, lowThresh);
            for (int col = 0; col < this.width; col++) {
                for (int row = 0; row < this.height; row++) {
                    valueMap[col][row] =
                            Math.max(valueMap[col][row], currMap[col][row]);
                }
            }
        }

        this.computeMagnitudeAndTheta(valueMap);

        // Non maxima suppression
        suppressNonMaxima(valueMap, theta, lowThresh);

        if (fuzzyness == EdgeFuzzyness.exact) {
            // Thresholding
            boolean tracked[][] = new boolean[this.width][this.height];
            for (int col = 0; col < this.width; col++) {
                for (int row = 0; row < this.height; row++) {
                    tracked[col][row] = false;
                    if (valueMap[col][row] < lowThresh) {
                        valueMap[col][row] = 0;
                    }
                }
            }
            // track from all points which are above the high threshold
            for (int col = 0; col < this.width; col++) {
                for (int row = 0; row < this.height; row++) {
                    if ((valueMap[col][row] >= highThresh) &&
                            (!tracked[col][row])) {
                        trackFromSinglePoint(valueMap, tracked, lowThresh, col,
                                row);
                    }
                }
            }
            // nullify all unmarked points
            for (int col = 0; col < this.width; col++) {
                for (int row = 0; row < this.height; row++) {
                    if (!tracked[col][row]) {
                        valueMap[col][row] = 0;
                    }
                }
            }
        }

        // resulting value-map
        for (int col = 0; col < this.width - 1; col++) {
            for (int row = 0; row < this.height - 1; row++) {
                int value = valueMap[col][row];
                if (value > 0) {
                    value = Math.min(255, (int) (100 + 3 * value));
                }
                valueMap[col][row] = value;
            }
        }

        return valueMap;
    }

    /**
     * Return edge map.
     *
     * @param fuzzyness edge fuzzyness (soft or exact)
     * @param strength  edge strength (soft - many edges, very string - few
     *                  edges)
     * @return 1-D array of values. The value at each pixel specifies the
     *         probability of an edge passing through this point
     */
    private int[][] getValueMap(EdgeFuzzyness fuzzyness, EdgeStrength strength) {
        switch (strength) {
            case veryStrong:
                return applyCannyAlgorithm(this.bwImPixels, fuzzyness, 80, 40);
            case strong:
                return applyCannyAlgorithm(this.bwImPixels, fuzzyness, 60, 30);
            case medium:
                return applyCannyAlgorithm(this.bwImPixels, fuzzyness, 50, 25);
            case soft:
//                List<int[][]> channels = new LinkedList<int[][]>();
//                channels.add(this.redImPixels);
//                channels.add(this.greenImPixels);
//                channels.add(this.blueImPixels);
//                return this.applyCannyAlgorithmMultichannel(this.bwImPixels,
//                        channels, fuzzyness, 40, 20);
                return applyCannyAlgorithm(this.bwImPixels, fuzzyness, 30, 15);
            default:
                return null;
        }
    }

    /**
     * Return edge map.
     *
     * @param fuzzyness edge fuzzyness (soft or exact)
     * @param strength  edge strength (soft - many edges, very string - few
     *                  edges)
     * @return 2-D array of values. The value at each pixel specifies the
     *         probability of an edge passing through this point
     */
    public IndexBitmapObject getValueMap2D(EdgeFuzzyness fuzzyness,
                                           EdgeStrength strength) {
        return new IndexBitmapObject(getValueMap(fuzzyness, strength),
                this.width, this.height);
    }
}

