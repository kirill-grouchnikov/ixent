package org.jvnet.ixent.algorithms.geometry.spacefilling;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import org.jvnet.ixent.algorithms.graphics.edgedetection.EdgeDetector;
import org.jvnet.ixent.algorithms.graphics.edgedetection.EdgeDetectorFactory;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.Vertex2D;

/**
 * Space filling curve based on structure vicinity. The distance between any
 * given pair of centers depends on how close they lie to some image feature.
 *
 * @author Kirill Grouchnikov
 */
public final class SpaceFillingCurveStructure extends SpaceFillingCurveBase {
//  public static final int CELL_MAX_RADIUS = 3;
    private int width, height;
    private int cellMaxRadius, cellMinRadius;
    private boolean toAllocateAlongEdges;

    /**
     * Construct space filling curve
     *
     * @param cellMaxRadius         minimal distance between any two centers
     *                              that lie far from image features
     * @param pToAllocateAlongEdges if <code>true</code>, additional points will
     *                              be allocated along the edges of the image
     */
    public SpaceFillingCurveStructure(int cellMaxRadius,
                                      boolean pToAllocateAlongEdges) {
        this.cellMaxRadius = cellMaxRadius;
        this.cellMinRadius = (int) (Math.ceil((double) cellMaxRadius / 2.0));
        this.minDistanceBetweenCenters = this.cellMinRadius;
        this.toAllocateAlongEdges = pToAllocateAlongEdges;
    }

    /**
     * Create coverage by centering a "disk" of given radius at given location.
     * If this location is already "taken" (covered or disk center), this
     * function returns. Otherwise it marks the central pixel as "disk center"
     * and its immediate vicinity (using regular distance metric) as "covered"
     *
     * @param xc     pixel center column
     * @param yc     pixel center row
     * @param radius distance. All pixels lying at most <b>distance</b> pixels
     *               away will be marked as "covered"
     */
    private void createDiskCoverage(int xc, int yc, int radius) {
        if (this.pixelStatus[xc][yc] == PixelStatus.diskCenter) {
            return;
        }

        if (radius < this.cellMinRadius) {
            radius = this.cellMinRadius;
        }
        int radius2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int nx = xc + dx;
                if ((nx < 0) || (nx >= this.width)) {
                    continue;
                }
                int ny = yc + dy;
                if ((ny < 0) || (ny >= this.height)) {
                    continue;
                }
                if (this.pixelStatus[nx][ny] != PixelStatus.notTaken) {
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
     * exception if the input image is null
     *
     * @param bitmapObject the input image
     * @throws IllegalArgumentException if the input image is null
     */
    public void init(BufferedImage bitmapObject) {
        if (bitmapObject == null) {
            throw new IllegalArgumentException("Can't pass null object");
        }

        this.width = bitmapObject.getWidth();
        this.height = bitmapObject.getHeight();

        IndexBitmapObject structureVicinityMap = new IndexBitmapObject(width,
                height);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                structureVicinityMap.setValue(col, row,
                        this.cellMaxRadius);
            }
        }

        // compute structure vicinity map based only on edges
        EdgeDetector edgeDetector = EdgeDetectorFactory.getEdgeDetector(
                bitmapObject);
        IndexBitmapObject edgeObject = edgeDetector.getValueMap2D(
                EdgeDetector.EdgeFuzzyness.exact,
                EdgeDetector.EdgeStrength.soft);

        // update structure vicinity map based on the edge closeness
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                if (edgeObject.getValue(col, row) > 0) {
                    // edge point
                    for (int dx = -this.cellMaxRadius;
                         dx <= this.cellMaxRadius; dx++) {
                        int nx = col + dx;
                        if ((nx < 0) || (nx >= width)) {
                            continue;
                        }
                        int ddx = (dx > 0) ? dx : -dx;
                        for (int dy = -this.cellMaxRadius;
                             dy <= this.cellMaxRadius; dy++) {
                            int ny = row + dy;
                            if ((ny < 0) || (ny >= height)) {
                                continue;
                            }
                            int ddy = (dy > 0) ? dy : -dy;
                            int dist = ddx + ddy;
                            if (structureVicinityMap.getValue(nx, ny) > dist) {
                                structureVicinityMap.setValue(nx, ny, dist);
                            }
                        }
                    }
                }
            }
        }

        // call initializer that takes structure vicinity map as a parameter
        this.init(structureVicinityMap);
    }

    /**
     * Initialize this filling curve using given true-color bitmap. Throws an
     * exception if the bitmap is null or one of the entries in it is negative
     *
     * @param structureVicinityMap the structure vicinity map. Each pixel has an
     *                             associated non-negative integer value
     *                             specifying how far does it lie from some
     *                             image feature (such as edge or segment
     *                             boundary)
     * @throws IllegalArgumentException if the map is null or one of the entries
     *                                  in the map is negative
     */
    public void init(IndexBitmapObject structureVicinityMap) {
        if (structureVicinityMap == null) {
            throw new IllegalArgumentException("Can't pass null object");
        }

        this.width = structureVicinityMap.getWidth();
        this.height = structureVicinityMap.getHeight();
        // check that there are no negative entries
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                if (structureVicinityMap.getValue(col, row) < 0) {
                    throw new IllegalArgumentException("Negative entry at [" +
                            col + ", " + row + "]: " +
                            structureVicinityMap.getValue(col, row));
                }
            }
        }


        // compute amount of points for each edge distance
        int[] edgeDistanceCount = new int[2 * this.cellMaxRadius + 1];
        for (int i = 0; i < edgeDistanceCount.length; i++) {
            edgeDistanceCount[i] = 0;
        }

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                edgeDistanceCount[structureVicinityMap.getValue(col, row)]++;
            }
        }

//    for (int i=0; i<edgeDistanceCount.length; i++)
//      System.out.println(edgeDistanceCount[i] + " points at distance " + i);

        // allocate permutation array
        int total = this.width * this.height;
        int[] visitOrder = new int[total];
        for (int i = 0; i < total; i++) {
            visitOrder[i] = -1;
        }

        int[] countBeforeEdgeDistance = new int[2 * this.cellMaxRadius + 1];
        countBeforeEdgeDistance[0] = 0;
        for (int i = 0; i < (countBeforeEdgeDistance.length - 1); i++) {
            countBeforeEdgeDistance[i + 1] = countBeforeEdgeDistance[i] +
                    edgeDistanceCount[i];
        }

//    for (int i=0; i<countBeforeEdgeDistance.length; i++)
//      System.out.println(countBeforeEdgeDistance[i] + " points at distance < " + i);

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                // get edge distance
                int edgeDistance = structureVicinityMap.getValue(col, row);
                // setLocation visiting order
                visitOrder[countBeforeEdgeDistance[edgeDistance]] = row *
                        width +
                        col;
                // update count
                countBeforeEdgeDistance[edgeDistance]++;
            }
        }

        // create random permutation among each group
        for (int i = 0; i < edgeDistanceCount.length; i++) {
            int startIndex = (i == 0) ? 0 : countBeforeEdgeDistance[i - 1];
            int endIndex = countBeforeEdgeDistance[i] - 1;
//      System.out.println("permuting in [" + startIndex + ", " + endIndex + "]");
            for (int j = startIndex; j <= endIndex; j++) {
                int newPos = startIndex +
                        (int) ((endIndex - startIndex) * Math.random());
                int tmp = visitOrder[j];
                visitOrder[j] = visitOrder[newPos];
                visitOrder[newPos] = tmp;
            }
        }

        // create centers array and allocate along edges
        this.pixelStatus = new PixelStatus[width][height];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                pixelStatus[col][row] = PixelStatus.notTaken;
            }
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
            this.createDiskCoverage(centerX, centerY,
                    structureVicinityMap.getValue(centerX, centerY));
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

        if (this.toAllocateAlongEdges) {
            // replicate centers in the vicinity of the edges
            List<Vertex2D> replicatedCenters = new LinkedList<Vertex2D>();
            int offset = 5 + 2 * this.getMinDistanceBetweenCenters();
            int leftMarginX = offset;
            int rightMarginX = this.width - offset;
            int topMarginY = offset;
            int bottomMarginY = this.height - offset;

            for (int i = 0; i < this.centers.length; i++) {
                double x = this.centers[i].getX();
                double y = this.centers[i].getY();
                // borders
                if (x < leftMarginX) {
                    replicatedCenters.add(new Vertex2D(this.width + x, y));
                }
                if (x > rightMarginX) {
                    replicatedCenters.add(new Vertex2D(x - this.width, y));
                }
                if (y < topMarginY) {
                    replicatedCenters.add(new Vertex2D(x, this.height + y));
                }
                if (y > bottomMarginY) {
                    replicatedCenters.add(new Vertex2D(x, y - this.height));
                }
                // corners
                if ((x < leftMarginX) && (y < topMarginY)) {
                    replicatedCenters.add(
                            new Vertex2D(this.width + x, this.height + y));
                }
                if ((x > rightMarginX) && (y < topMarginY)) {
                    replicatedCenters.add(
                            new Vertex2D(x - this.width, this.height + y));
                }
                if ((x < leftMarginX) && (y > bottomMarginY)) {
                    replicatedCenters.add(
                            new Vertex2D(this.width + x, y - this.height));
                }
                if ((x > rightMarginX) && (y > bottomMarginY)) {
                    replicatedCenters.add(
                            new Vertex2D(x - this.width, y - this.height));
                }
            }
            Vertex2D[] newCenters = new Vertex2D[this.centers.length +
                    replicatedCenters.size()];
            int prevCount = this.centers.length;
            for (int i = 0; i < prevCount; i++) {
                newCenters[i] = this.centers[i];
            }
            for (Vertex2D currVertex : replicatedCenters) {
                newCenters[prevCount++] = currVertex;
            }
            this.centers = newCenters;
        }
    }

    /**
     * Initialize this filling curve using only width and height. Throws an
     * exception as this operation is not supported by this class
     *
     * @param width  image width
     * @param height image height
     * @throws UnsupportedOperationException
     */
    public void init(int width, int height) {
        throw new UnsupportedOperationException();
    }
}

