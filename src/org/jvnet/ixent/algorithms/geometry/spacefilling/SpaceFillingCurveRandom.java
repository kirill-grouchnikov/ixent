package org.jvnet.ixent.algorithms.geometry.spacefilling;

import java.awt.image.BufferedImage;

import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.Vertex2D;

/**
 * Space filling curve based on random behaviour. The distance between any given
 * pair of centers is guaranteed to be at least value specified in the
 * constructor of this class's object
 *
 * @author Kirill Grouchnikov
 */
public final class SpaceFillingCurveRandom extends SpaceFillingCurveBase {
    private int width, height;
    private int cellRadius;
    private boolean toAllocateAlongEdges;

    /**
     * Construct space filling curve
     *
     * @param cellRadius            minimal distance between any two centers
     * @param pToAllocateAlongEdges if <code>true</code>, additional points will
     *                              be allocated along the edges of the image
     */
    public SpaceFillingCurveRandom(int cellRadius,
                                   boolean pToAllocateAlongEdges) {
        this.cellRadius = cellRadius;
        this.minDistanceBetweenCenters = cellRadius;
        this.toAllocateAlongEdges = pToAllocateAlongEdges;
    }

    /**
     * Create coverage by centering a "disk" of given radius at given location.
     * If this location is already "taken" (covered or disk center), this
     * function returns. Otherwise it marks the central pixel as "disk center"
     * and its immediate vicinity (using regular distance metric) as "covered"
     *
     * @param xc       pixel center column
     * @param yc       pixel center row
     * @param distance distance. All pixels lying at most <b>distance</b> pixels
     *                 away will be marked as "covered"
     */
    private void createDiskCoverage(int xc, int yc, int distance) {
        if (this.pixelStatus[xc][yc] != PixelStatus.notTaken) {
            return;
        }

        int radius2 = distance * distance;
        for (int dx = -distance; dx <= distance; dx++) {
            for (int dy = -distance; dy <= distance; dy++) {
                int nx = xc + dx;
                if ((nx < 0) || (nx >= this.width)) {
                    continue;
                }
                int ny = yc + dy;
                if ((ny < 0) || (ny >= this.height)) {
                    continue;
                }
                if (this.pixelStatus[nx][ny] == PixelStatus.diskCenter) {
                    continue;
                }
                if ((dx * dx + dy * dy) > radius2) {
                    continue;
                }
                this.pixelStatus[nx][ny] = PixelStatus.covered;
            }
        }
        this.pixelStatus[xc][yc] = PixelStatus.diskCenter;
        this.centerCount++;
    }

    /**
     * Initialize this filling curve using given true-color bitmap. Throws an
     * exception ad this operation is not supported by this class
     *
     * @param bitmapObject the input image
     * @throws UnsupportedOperationException
     */
    public void init(BufferedImage bitmapObject) {
        throw new UnsupportedOperationException();
    }

    /**
     * Initialize this filling curve using given true-color bitmap. Throws an
     * exception as this operation is not supported by this class
     *
     * @param structureVicinityMap the structure vicinity map. Each pixel has an
     *                             associated non-negative integer value
     *                             specifying how far does it lie from some
     *                             image feature (such as edge or segment
     *                             boundary)
     * @throws UnsupportedOperationException
     */
    public void init(IndexBitmapObject structureVicinityMap) {
        throw new UnsupportedOperationException();
    }

    /**
     * Initialize this filling curve using only width and height.
     *
     * @param width  image width
     * @param height image height
     */
    public void init(int width, int height) {
        this.width = width;
        this.height = height;

        int total = this.width * this.height;

        this.centerCount = 0;

        // allocate permutation array
        int[] visitOrder = new int[total];
        for (int i = 0; i < total; i++) {
            visitOrder[i] = i;
        }

        // create random space filling visiting curve
        for (int i = 0; i < total; i++) {
            int newPos = (int) (total * Math.random());
            int tmp = visitOrder[i];
            visitOrder[i] = visitOrder[newPos];
            visitOrder[newPos] = tmp;
        }

        // allocate center array
        this.pixelStatus = new PixelStatus[this.width][this.height];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                pixelStatus[col][row] = PixelStatus.notTaken;
            }
        }

        // create centers array and allocate along edges
        if (this.toAllocateAlongEdges) {
            this.computeCentersAlongEdges();
        }

        // visit
        for (int visit = 0; visit < total; visit++) {
            int centerIndex = visitOrder[visit];
            int centerX = centerIndex % this.width;
            int centerY = centerIndex / this.width;
            // check if already visited
            if (pixelStatus[centerX][centerY] != PixelStatus.notTaken) {
                continue;
            }
            this.createDiskCoverage(centerX, centerY, this.cellRadius);
        }

        this.centers = new Vertex2D[this.centerCount];
        int count = 0;
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                if (pixelStatus[col][row] == PixelStatus.diskCenter) {
                    this.centers[count++] = new Vertex2D(col, row);
                }
            }
        }
    }

    /**
     * Creates random centers along image's edges
     */
    private void computeCentersAlongEdges() {
        this.centerCount = 0;

        // allocate center array
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                pixelStatus[col][row] = PixelStatus.notTaken;
            }
        }

        // create points on borders. Very important for replication -
        // symmetry on T/B and L/R borders
        int currL = 0, currR = this.width - 1;
        while ((currR - currL) > this.cellRadius) {
//      System.out.println("setting at " + currL + "*" + yt);
            this.createDiskCoverage(currL, 0, this.cellRadius);
//      System.out.println("setting at " + currL + "*" + (yb-1));
            this.createDiskCoverage(currL, this.height - 1, this.cellRadius);
//      System.out.println("setting at " + currR + "*" + yt);
            this.createDiskCoverage(currR, 0, this.cellRadius);
//      System.out.println("setting at " + currR + "*" + (yb-1));
            this.createDiskCoverage(currR, this.height - 1, this.cellRadius);
            int sL = (int) (this.cellRadius * (0.5 + Math.random()));
            int sR = (int) (this.cellRadius * (0.5 + Math.random()));
            currL += sL;
            currR -= sR;
        }

        int currT = 0, currB = this.height - 1;
        while ((currB - currT) > this.cellRadius) {
//      System.out.println("setting at " + xl + "*" + currT);
            this.createDiskCoverage(0, currT, this.cellRadius);
//      System.out.println("setting at " + (xr-1) + "*" + currT);
            this.createDiskCoverage(this.width - 1, currT, this.cellRadius);
//      System.out.println("setting at " + xl + "*" + currB);
            this.createDiskCoverage(0, currB, this.cellRadius);
//      System.out.println("setting at " + (xr-1) + "*" + currB);
            this.createDiskCoverage(this.width - 1, currB, this.cellRadius);
            int sT = (int) (this.cellRadius * (1.0 + Math.random()));
            int sB = (int) (this.cellRadius * (1.0 + Math.random()));
            currT += sT;
            currB -= sB;
        }
    }
}

