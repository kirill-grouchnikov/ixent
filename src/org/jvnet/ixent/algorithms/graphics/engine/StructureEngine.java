package org.jvnet.ixent.algorithms.graphics.engine;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.ixent.algorithms.graphics.engine.linkinfo.WeightedWrapper;
import org.jvnet.ixent.algorithms.graphics.segmentation.SegmentationInfo;
import org.jvnet.ixent.algorithms.graphics.turbulence.DisplacementMatrix;
import org.jvnet.ixent.algorithms.graphics.turbulence.DisplacementVector;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.MathConstants;
import org.jvnet.ixent.math.leastsquares.WeightedFitting;
import org.jvnet.ixent.math.matrix.DenseMatrix;
import org.jvnet.ixent.util.WeightedDirection;

/**
 * The structure engine for computing image feature based characteristics. Gets
 * input image and weights of segmentation and edges and produces <b>structure
 * vicinity map</b> and <b>structure gradient map</b>
 *
 * @author Kirill Grouchnikov
 */
public class StructureEngine {
    /**
     * Helper class for sorting neighbour pixels in phase 1 of computing
     * gradient map
     */
    private static class NeighbourInfo implements Comparable<NeighbourInfo> {
        public int column;
        public int row;
        public int structureCoef;

        /**
         * @param _column        neighbour column
         * @param _row           neighbour row
         * @param _structureCoef neighbour structure coefficient
         */
        public NeighbourInfo(int _column, int _row, int _structureCoef) {
            this.column = _column;
            this.row = _row;
            this.structureCoef = _structureCoef;
        }

        /**
         * Comparator function for ordering info by decreasing structure
         * coefficient
         *
         * @param info2 second info
         * @return -1/0/1 if this node has bigger/the same/smaller structure
         *         coefficient
         */
        public int compareTo(NeighbourInfo info2) {
            if (this == info2) {
                return 0;
            }
            if (this.structureCoef < info2.structureCoef) {
                return 1;
            }
            if (this.structureCoef > info2.structureCoef) {
                return -1;
            }

            // here - compare coords
            int diffRow = this.row - info2.row;
            if (diffRow != 0) {
                return diffRow;
            }
            int diffCol = this.column - info2.column;
            return diffCol;
        }
    }

    private int imageWidth;

    private int imageHeight;

    /**
     * information on segmentation input link
     */
    private WeightedWrapper<SegmentationInfo> segmentationLinkInfo;

    /**
     * information on edge detection input link
     */
    private WeightedWrapper<IndexBitmapObject> edgeDetectionLinkInfo;

    /**
     * information on displacement input link
     */
    private WeightedWrapper<DisplacementMatrix> displacementLinkInfo;


    /**
     * structure vicinity map
     */
    private IndexBitmapObject vicinityMap;

    /**
     * structure gradient map
     */
    private IndexBitmapObject gradientMap;

    private Logger logger;

    public static final int CELL_MAX_RADIUS = 15;

    public static final double SECTOR_HALF_ANGLE = 20.0;

    /**
     * @param pImageWidth            input image width
     * @param pImageHeight           input image height
     * @param pSegmentationLinkInfo  segmentation information (non-null)
     * @param pEdgeDetectionLinkInfo edge information (non-null)
     * @param pDisplacementLinkInfo  displacement information (non-null)
     * @throws IllegalArgumentException if one of the link info objects is null
     */
    public StructureEngine(int pImageWidth, int pImageHeight,
                           WeightedWrapper<SegmentationInfo> pSegmentationLinkInfo,
                           WeightedWrapper<IndexBitmapObject> pEdgeDetectionLinkInfo,
                           WeightedWrapper<DisplacementMatrix> pDisplacementLinkInfo) {

        this.logger =
                Logger.getLogger(StructureEngine.class.getPackage().getName());

        this.imageWidth = pImageWidth;
        this.imageHeight = pImageHeight;

        // check link info
        if ((pSegmentationLinkInfo == null) ||
                (pEdgeDetectionLinkInfo == null) ||
                (pDisplacementLinkInfo == null)) {
            throw new IllegalArgumentException("Link info must be non-null");
        }

        this.segmentationLinkInfo = pSegmentationLinkInfo;
        this.edgeDetectionLinkInfo = pEdgeDetectionLinkInfo;
        this.displacementLinkInfo = pDisplacementLinkInfo;
    }

    /**
     * Compute displaced structure map based on input image and weights for
     * edges, segmentation and displacement
     *
     * @return displaced structure map
     */
    private IndexBitmapObject computeWeightedStructureMap() {
        // Compute the structure map. The structure map is computed in three steps:
        // 1. Compute edges and segmentation.
        // 2. Combine them using the corresponding weights.
        // 3. Transform the resulting features using the displacement map

        IndexBitmapObject structureObject =
                new IndexBitmapObject(this.imageWidth, this.imageHeight);

        // compute segmentation (if necessary)
        if (this.segmentationLinkInfo.getWeight() > 0.0) {
            int[][] segmentationBitmap =
                    this.segmentationLinkInfo.getLinkObject()
                    .getBoundariesBitmap()
                    .getBitmap();
            for (int col = 0; col < this.imageWidth; col++) {
                for (int row = 0; row < this.imageHeight; row++) {
                    // here the previous value is 0
                    int newVal = (int) (this.segmentationLinkInfo.getWeight() *
                            segmentationBitmap[col][row]);
                    structureObject.setValue(col, row, newVal);
                }
            }
        }

        // compute edges (if necessary)
        if (this.edgeDetectionLinkInfo.getWeight() > 0.0) {
            int[][] edgeBitmap = this.edgeDetectionLinkInfo.getLinkObject()
                    .getBitmap();
            for (int col = 0; col < this.imageWidth; col++) {
                for (int row = 0; row < this.imageHeight; row++) {
                    // here the previous value is 0
                    int newVal = (int) (this.edgeDetectionLinkInfo.getWeight() *
                            edgeBitmap[col][row]);
                    int oldVal = structureObject.getValue(col, row);
                    structureObject.setValue(col, row, oldVal + newVal);
                }
            }
        }

        // compute displacement matrix (if necessary)
        IndexBitmapObject displacedStructureObject = null;
        if (this.displacementLinkInfo.getWeight() > 0.0) {
            DisplacementMatrix displacementMap =
                    this.displacementLinkInfo.getLinkObject();

            // transform using displacement matrix
            displacedStructureObject =
                    new IndexBitmapObject(this.imageWidth, this.imageHeight);

            double weightDisplacement = this.displacementLinkInfo.getWeight();
            for (int col = 0; col < this.imageWidth; col++) {
                for (int row = 0; row < this.imageHeight; row++) {
                    int currStructureValue = structureObject.getValue(col, row);
                    if (currStructureValue <= 0) {
                        continue;
                    }

                    DisplacementVector currDisplacement =
                            displacementMap.getVectorAt(col, row);
                    double currDisplacementX = weightDisplacement *
                            currDisplacement.getXComponent();
                    double currDisplacementY = weightDisplacement *
                            currDisplacement.getYComponent();

                    // the corresponding feature will now contribute to four pixels.
                    // The weights will be computed as fraction of pixels
                    double newX = col + currDisplacementX;
                    double newY = row - currDisplacementY;

                    // compute weights in X direction
                    int leftX = (int) Math.floor(newX);
                    int rightX = leftX + 1;

                    double diffLeft = newX - leftX;

                    double weightLeft = (diffLeft < MathConstants.EPS) ?
                            1.0 : diffLeft;
                    double weightRight = 1.0 - weightLeft;

                    // compute weights in Y direction
                    int topY = (int) Math.floor(newY);
                    int bottomY = topY + 1;

                    double diffTop = newY - topY;

                    double weightTop = (diffTop < MathConstants.EPS) ?
                            1.0 : diffTop;
                    double weightBottom = 1.0 - weightTop;

                    boolean isLeftValid = (leftX >= 0) &&
                            (leftX < this.imageWidth);
                    boolean isRightValid = (rightX >= 0) &&
                            (rightX < this.imageWidth);
                    boolean isTopValid = (topY >= 0) &&
                            (topY < this.imageHeight);
                    boolean isBottomValid = (bottomY >= 0) &&
                            (bottomY < this.imageHeight);

                    // left-top
                    if (isLeftValid && isTopValid) {
                        double weightLT = weightLeft * weightTop;
                        int oldValue = displacedStructureObject.getValue(leftX,
                                topY);
                        int newValue = oldValue +
                                (int) (weightLT * currStructureValue);
                        displacedStructureObject.setValue(leftX, topY,
                                newValue);
                    }

                    // left-bottom
                    if (isLeftValid && isBottomValid) {
                        double weightLB = weightLeft * weightBottom;
                        int oldValue = displacedStructureObject.getValue(leftX,
                                bottomY);
                        int newValue = oldValue +
                                (int) (weightLB * currStructureValue);
                        displacedStructureObject.setValue(leftX, bottomY,
                                newValue);
                    }

                    // right-top
                    if (isRightValid && isTopValid) {
                        double weightRT = weightRight * weightTop;
                        int oldValue = displacedStructureObject.getValue(
                                rightX, topY);
                        int newValue = oldValue +
                                (int) (weightRT * currStructureValue);
                        displacedStructureObject.setValue(rightX, topY,
                                newValue);
                    }

                    // left-top
                    if (isRightValid && isBottomValid) {
                        double weightRB = weightRight * weightTop;
                        int oldValue = displacedStructureObject.getValue(
                                rightX, bottomY);
                        int newValue = oldValue +
                                (int) (weightRB * currStructureValue);
                        displacedStructureObject.setValue(rightX, bottomY,
                                newValue);
                    }
                }
            }
        }
        else {
            // no need to displace - just setLocation reference to it
            displacedStructureObject = structureObject;
        }

        // check that all values in 0..255 range
        for (int col = 0; col < this.imageWidth; col++) {
            for (int row = 0; row < this.imageHeight; row++) {
                int currVal = displacedStructureObject.getValue(col, row);
                if (currVal < 0) {
                    currVal = 0;
                }
                if (currVal > 255) {
                    currVal = 255;
                }
                displacedStructureObject.setValue(col, row, currVal);
            }
        }

        if (this.logger.isLoggable(Level.FINEST)) {
            DenseMatrix distMatrix = new DenseMatrix(
                    displacedStructureObject.getBitmap(),
                    this.imageWidth, this.imageHeight);
            this.logger.finest("Structure matrix");
            distMatrix.dump(this.logger, Level.FINEST, 5, 1);
        }

        return displacedStructureObject;
    }

    /**
     * Compute vicinity map based on displaced structure map
     *
     * @param displacedStructureObject displaced structure map
     */
    private void computeVicinityMap(IndexBitmapObject displacedStructureObject) {
        //   The vicinity map assigns an integer value to each pixel. This
        // value signifies how close is this pixel to some image feature. To
        // compute this we go over all pixels that have non-zero value in the
        // 'displacedStructureObject' and update all its neighbours: each
        // neighbour pixel holds the (up to now) minimal distance to some
        // image feature. Each time we have image feature that lies closer, we
        // update the corresponding pixels.

        // update edge distance array
        int[][] distanceFromStructure = new int[this.imageWidth][this.imageHeight];
        for (int col = 0; col < this.imageWidth; col++) {
            for (int row = 0; row < this.imageHeight; row++) {
                distanceFromStructure[col][row] = CELL_MAX_RADIUS;
            }
        }

        for (int col = 0; col < this.imageWidth; col++) {
            for (int row = 0; row < this.imageHeight; row++) {
                int structurePresence = displacedStructureObject.getValue(col,
                        row);
                if (structurePresence > 0) {
                    for (int dx = -CELL_MAX_RADIUS;
                         dx <= CELL_MAX_RADIUS; dx++) {
                        int nx = col + dx;
                        if ((nx < 0) || (nx >= this.imageWidth)) {
                            continue;
                        }
                        int ddx = (dx > 0) ? dx : -dx;
                        for (int dy = -CELL_MAX_RADIUS;
                             dy <= CELL_MAX_RADIUS; dy++) {
                            int ny = row + dy;
                            if ((ny < 0) || (ny >= this.imageHeight)) {
                                continue;
                            }
                            int ddy = (dy > 0) ? dy : -dy;

                            // if we have high confidence of structure nearby (value of
                            // 'structurePresence' is close to 255 - the distance is not
                            // influenced. If, however, the presence of image feature is
                            // questionable (value close to 0, we should reflect it in the
                            // distance). The chosen metric is:
                            //    255 -> dist = dist
                            //      0 -> dist = 3*dist
                            double coef = 3.0 -
                                    2.0 * structurePresence / 255.0;
                            int dist = (int) ((ddx + ddy) * coef);
                            if (distanceFromStructure[nx][ny] > dist) {
                                distanceFromStructure[nx][ny] = dist;
                            }
                        }
                    }
                }
            }
        }

        if (this.logger.isLoggable(Level.FINEST)) {
            DenseMatrix distMatrix = new DenseMatrix(distanceFromStructure,
                    this.imageWidth, this.imageHeight);
            this.logger.finest("Distance from structure matrix");
            distMatrix.dump(this.logger, Level.FINEST, 4, 1);
        }

        this.vicinityMap = new IndexBitmapObject(distanceFromStructure,
                this.imageWidth, this.imageHeight);
    }

    /**
     * Compute structure gradient map. At each pixel we'll have a value in
     * 0..179 range that will approximate the image gradient at this pixel. The
     * image gradient is not computed according to image brightness. It rather
     * depends on nearby image features (such as edges and segment boundaries)
     *
     * @param displacedStructureObject displaced structure map
     */
    private void computeGradientMap(IndexBitmapObject displacedStructureObject) {
        // The algorithm consists of the following steps:
        // 1. compute gradient direction at each pixel that is part of image feature.
        // 2. compute gradiend direction at each pixel that lies in two-way
        //      sectors focused in image feature pixels
        // 3. interpolate every pixel row-wise and column-wise
        // 4. repeat step 3 once

        // 1. compute gradient direction at each pixel that is part of image feature:
        //  For every pixel that is part of image feature (has non-zero value in
        //  the 'displacedStructureObject' we go over its neighbours that also have
        //  non-zero value in that map. Two such strongest neighbours (the pixel itself
        //  is eligible for this process) are selected. A line connecting centers
        //  of these pixels is taken to be the approximation to image feature coming
        //  through this pixel
        double[][] featureOrientation = new double[this.imageWidth][this.imageHeight];
        for (int col = 0; col < this.imageWidth; col++) {
            for (int row = 0; row < this.imageHeight; row++) {
                featureOrientation[col][row] = -1.0;
            }
        }
        // initialize gradients for border pixels - top row and bottom row to 0,
        // left column and right column to 90, TL and BR pixels to 135,
        // TR and BL pixels to 45
        for (int col = 0; col < this.imageWidth; col++) {
            featureOrientation[col][0] = 0;
            featureOrientation[col][this.imageHeight - 1] = 0;
        }
        for (int row = 0; row < this.imageHeight; row++) {
            featureOrientation[0][row] = 90;
            featureOrientation[this.imageWidth - 1][row] = 90;
        }
        featureOrientation[0][0] = 45.0;
        featureOrientation[0][this.imageHeight - 1] = 135.0;
        featureOrientation[this.imageWidth - 1][0] = 135.0;
        featureOrientation[this.imageWidth - 1][this.imageHeight - 1] = 45.0;

        for (int col = 0; col < this.imageWidth; col++) {
            for (int row = 0; row < this.imageHeight; row++) {
                int currValue = displacedStructureObject.getValue(col, row);
                if (currValue == 0) {
                    continue;
                }
                // go over all neighbours and put them in a sorted setLocation
                SortedSet<NeighbourInfo> neighbours = new TreeSet<NeighbourInfo>();
                for (int dcol = -1; dcol <= 1; dcol++) {
                    int neighbourCol = col + dcol;
                    if ((neighbourCol < 0) ||
                            (neighbourCol >= this.imageWidth)) {
                        continue;
                    }
                    for (int drow = -1; drow <= 1; drow++) {
                        int neighbourRow = row + drow;
                        if ((neighbourRow < 0) ||
                                (neighbourRow >= this.imageHeight)) {
                            continue;
                        }
                        int neighbourValue = displacedStructureObject.getValue(
                                neighbourCol, neighbourRow);
                        if (neighbourValue == 0) {
                            continue;
                        }
                        neighbours.add(new NeighbourInfo(neighbourCol,
                                neighbourRow, neighbourValue));
                    }
                }
                // if have less than 2 values in this map - no way to compute the direction
                if (neighbours.size() < 2) {
                    continue;
                }

                // perform least squares fitting with weights - find
                // an optimal straight line that approximates this pixel's
                // neighbours
                Point2D[] neighbourCenters = new Point2D[neighbours.size()];
                double[] neighbourWeights = new double[neighbours.size()];
                int count = 0;
                for (NeighbourInfo currNeighbour : neighbours) {
                    neighbourCenters[count] =
                            new Point2D.Double(currNeighbour.column,
                                    currNeighbour.row);
                    neighbourWeights[count] = currNeighbour.structureCoef;
                    count++;
                }

                WeightedFitting fitting = new WeightedFitting(neighbourCenters,
                        neighbourWeights);
                if (!fitting.isHasFit()) {
                    continue;
                }

                featureOrientation[col][row] =
                        fitting.getInclinationAngleInDegrees();
            }
        }


        if (this.logger.isLoggable(Level.FINEST)) {
            DenseMatrix distMatrix = new DenseMatrix(featureOrientation,
                    this.imageWidth, this.imageHeight);
            this.logger.finest("Feature orientation after phase 1");
            distMatrix.dump(this.logger, Level.FINEST, 5, 1);
        }

        // 2. compute gradiend direction at each pixel that lies in two-way
        // sectors focused in image feature pixels: each pixel that has non-zero value
        //  in 'featureOrientation' array is a source of a two-way sector. Each one-way
        //  half spans from this pixel in a direction perpendicular to its orientation.
        //  We go over all pixels in this sector (column-wise or row-wise, depending
        //  on sector's center ray direction) and update two arrays:
        //    - sum(coef)
        //    - sum(coef*direction)
        //  Where coef equals to structureCoef/distanceToPixel and direction must
        //  be properly tweaked to account for cases where the directions of two nearby
        //  edges are close to 0 (e.g. 1 and 179 must produce 0 and not 90). The
        //  coefficient is proportional to the probability of image feature in the
        //  sector origin and inversely proportional to the distance between the sector
        //  origin and the currently sweeped pixel.
        WeightedDirection[][] weightedDirections =
                new WeightedDirection[this.imageWidth][this.imageHeight];
        for (int col = 0; col < this.imageWidth; col++) {
            for (int row = 0; row < this.imageHeight; row++) {
                weightedDirections[col][row] = new WeightedDirection();
            }
        }

        int maxDistance = 50;
        for (int col = 0; col < this.imageWidth; col++) {
            for (int row = 0; row < this.imageHeight; row++) {
                // check if has image feature
                if (featureOrientation[col][row] < 0.0) {
                    continue;
                }

                // orientation of image feature
                double alpha = featureOrientation[col][row];

                // orientation of sector center ray
                double betha = alpha - 90.0;
                if (betha < 0.0) {
                    betha += 360.0;
                }

                // decide if need to scan by columns or by rows:
                // 45-135 and 225-315 ranges - scan rows
                // otherwise - scan columns
                boolean toScanRows = (((betha >= 45.0) && (betha <= 135.0))
                        || ((betha >= 225.0) && (betha <= 315.0)));

                // compute sector ray angles
                double bethaMinusGamma = (betha - SECTOR_HALF_ANGLE) * Math.PI /
                        180.0;
                double bethaPlusGamma = (betha + SECTOR_HALF_ANGLE) * Math.PI /
                        180.0;

                double cosBMG = Math.cos(bethaMinusGamma);
                double sinBMG = Math.sin(bethaMinusGamma);
                double cosBPG = Math.cos(bethaPlusGamma);
                double sinBPG = Math.sin(bethaPlusGamma);

                if (toScanRows) {
                    // go over all rows from top to bottom
                    for (int yc = 0; yc < this.imageHeight; yc++) {
                        // compute start and end X of the sector in this row
                        double x1 = (yc * cosBMG + col * sinBMG - row * cosBMG) /
                                sinBMG;
                        double x2 = (yc * cosBPG + col * sinBPG - row * cosBPG) /
                                sinBPG;

                        int xStart = (int) ((x1 < x2) ? x1 : x2);
                        int xEnd = (int) ((x1 < x2) ? x2 : x1);
                        if (xStart < 0) {
                            xStart = 0;
                        }
                        if (xEnd >= this.imageWidth) {
                            xEnd = this.imageWidth - 1;
                        }

                        double ycp = Math.abs(yc - row);
                        for (int currCol = xStart; currCol <= xEnd; currCol++) {
                            double distance = 1.0 + Math.abs(currCol - col) +
                                    ycp;
                            if (distance >= maxDistance) {
                                continue;
                            }
                            // update entry (currCol, yc)
                            double currCoef = displacedStructureObject.getValue(
                                    col, row) /
                                    (distance * distance);
                            weightedDirections[currCol][yc].incorporate(alpha,
                                    currCoef);
                        }
                    }
                }
                else {
                    // go over all columns from left to right
                    for (int xc = 0; xc < this.imageWidth; xc++) {
                        // compute start and end Y of the sector in this row
                        double y1 = (xc * sinBMG - col * sinBMG + row * cosBMG) /
                                cosBMG;
                        double y2 = (xc * sinBPG - col * sinBPG + row * cosBPG) /
                                cosBPG;

                        int yStart = (int) ((y1 < y2) ? y1 : y2);
                        int yEnd = (int) ((y1 < y2) ? y2 : y1);
                        if (yStart < 0) {
                            yStart = 0;
                        }
                        if (yEnd >= this.imageHeight) {
                            yEnd = this.imageHeight - 1;
                        }

                        double xcp = Math.abs(xc - col);
                        for (int currRow = yStart; currRow <= yEnd; currRow++) {
                            double distance = 1.0 + xcp +
                                    Math.abs(currRow - row);
                            if (distance >= maxDistance) {
                                continue;
                            }
                            // update entry (xc, currRow)
                            double currCoef = displacedStructureObject.getValue(
                                    col, row) /
                                    (distance * distance);
                            weightedDirections[xc][currRow].incorporate(alpha,
                                    currCoef);
                        }
                    }
                }
            }
        }

        // compute interpolated directions for pixels in sectors
        for (int col = 0; col < this.imageWidth; col++) {
            for (int row = 0; row < this.imageHeight; row++) {
                // check if has image feature
                if (featureOrientation[col][row] >= 0.0) {
                    continue;
                }

                if (!weightedDirections[col][row].hasWeight()) {
                    continue;
                }

                featureOrientation[col][row] =
                        weightedDirections[col][row].getDirection();
            }
        }

        if (this.logger.isLoggable(Level.FINEST)) {
            DenseMatrix distMatrix = new DenseMatrix(featureOrientation,
                    this.imageWidth, this.imageHeight);
            this.logger.finest("Feature orientation after phase 2");
            distMatrix.dump(this.logger, Level.FINEST, 5, 1);
        }

        // 3. interpolate every pixel row-wise and column-wise
        double[][] interpolatedByRow = new double[this.imageWidth][this.imageHeight];
        double[][] interpolatedByCol = new double[this.imageWidth][this.imageHeight];
        for (int count = 0; count < 2; count++) {
            for (int col = 0; col < this.imageWidth; col++) {
                for (int row = 0; row < this.imageHeight; row++) {
                    interpolatedByRow[col][row] = -1.0;
                    interpolatedByCol[col][row] = -1.0;
                }
            }
            // by row
            for (int row = 0; row < this.imageHeight; row++) {
                // create a list of all pixels in this row that have directions
                LinkedList<Integer> directedEntries = new LinkedList<Integer>();
                for (int col = 0; col < this.imageWidth; col++) {
                    if (featureOrientation[col][row] >= 0.0) {
                        directedEntries.addLast(col);
                    }
                }
                if (directedEntries.size() == 0) {
                    // no pixels in this row that have direction - go to the next row
                    continue;
                }
                int[] entries = new int[directedEntries.size()];
                int index = 0;
                for (int entry : directedEntries) {
                    entries[index++] = entry;
                }

                // setLocation all values before the first as the first
                for (int col = 0; col < entries[0]; col++) {
                    interpolatedByRow[col][row] =
                            featureOrientation[entries[0]][row];
                }
                // interpolate between each pair of consecutive entries
                for (int entryIndex = 0;
                     entryIndex < entries.length - 1; entryIndex++) {
                    int colStart = entries[entryIndex];
                    int colEnd = entries[entryIndex + 1];

                    double valueStart = featureOrientation[colStart][row];
                    double valueEnd = featureOrientation[colEnd][row];
                    for (int col = colStart + 1; col < colEnd; col++) {
                        double newValue = valueStart + (valueEnd - valueStart) *
                                (col - colStart) / (colEnd - colStart);
                        interpolatedByRow[col][row] = newValue;
                    }
                }
                // setLocation all values after the last as the last
                int colLast = entries[entries.length - 1];
                for (int col = colLast; col < this.imageWidth; col++) {
                    interpolatedByRow[col][row] =
                            featureOrientation[colLast][row];
                }
            }
            // by column
            for (int col = 0; col < this.imageWidth; col++) {
                // create a list of all pixels in this column that have directions
                LinkedList<Integer> directedEntries = new LinkedList<Integer>();
                for (int row = 0; row < this.imageHeight; row++) {
                    if (featureOrientation[col][row] >= 0.0) {
                        directedEntries.addLast(row);
                    }
                }
                if (directedEntries.size() == 0) {
                    // no pixels in this column that have direction - go to the next
                    // column
                    continue;
                }
                int[] entries = new int[directedEntries.size()];
                int index = 0;
                for (int entry : directedEntries) {
                    entries[index++] = entry;
                }

                // setLocation all values before the first as the first
                for (int row = 0; row < entries[0]; row++) {
                    interpolatedByRow[col][row] =
                            featureOrientation[col][entries[0]];
                }
                // interpolate between each pair of consecutive entries
                for (int entryIndex = 0;
                     entryIndex < entries.length - 1; entryIndex++) {
                    int rowStart = entries[entryIndex];
                    int rowEnd = entries[entryIndex + 1];

                    double valueStart = featureOrientation[col][rowStart];
                    double valueEnd = featureOrientation[col][rowEnd];
                    for (int row = rowStart + 1; row < rowEnd; row++) {
                        double newValue = valueStart + (valueEnd - valueStart) *
                                (row - rowStart) / (rowEnd - rowStart);
                        interpolatedByCol[col][row] = newValue;
                    }
                }
                // setLocation all values after the last as the last
                int colLast = entries[entries.length - 1];
                for (int row = colLast; row < this.imageHeight; row++) {
                    interpolatedByCol[col][row] =
                            featureOrientation[col][colLast];
                }
            }

            for (int col = 0; col < this.imageWidth; col++) {
                for (int row = 0; row < this.imageHeight; row++) {
                    // if already has orientation - skip this pixel
                    if (featureOrientation[col][row] >= 0.0) {
                        continue;
                    }

                    boolean hasInterpolationByRow = (interpolatedByRow[col][row] >=
                            0.0);
                    boolean hasInterpolationByCol = (interpolatedByCol[col][row] >=
                            0.0);

                    if (hasInterpolationByCol) {
                        if (hasInterpolationByRow) {
                            // has both interpolations - use them
                            WeightedDirection wd = new WeightedDirection();
                            wd.incorporate(interpolatedByCol[col][row], 1.0);
                            wd.incorporate(interpolatedByRow[col][row], 1.0);
                            featureOrientation[col][row] = wd.getDirection();
                        }
                        else {
                            // has only one - by column
                            featureOrientation[col][row] =
                                    interpolatedByCol[col][row];
                        }
                    }
                    else {
                        if (hasInterpolationByRow) {
                            // has only one - by row
                            featureOrientation[col][row] =
                                    interpolatedByRow[col][row];
                        }
                    }
                }
            }
            if (this.logger.isLoggable(Level.FINEST)) {
                DenseMatrix distMatrix = new DenseMatrix(featureOrientation,
                        this.imageWidth, this.imageHeight);
                this.logger.finest(
                        "Feature orientation after phase " + (3 + count));
                distMatrix.dump(this.logger, Level.FINEST, 5, 1);
            }

        }

        int[][] finalOrientation = new int[this.imageWidth][this.imageHeight];
        for (int col = 0; col < this.imageWidth; col++) {
            for (int row = 0; row < this.imageHeight; row++) {
                int finalValue = (int) featureOrientation[col][row];
                if (finalValue < 0) {
                    finalValue = 0;
                }
                if (finalValue >= 180) {
                    finalValue -= 180;
                }
                finalOrientation[col][row] = finalValue;
            }
        }

        if (this.logger.isLoggable(Level.FINEST)) {
            DenseMatrix distMatrix = new DenseMatrix(finalOrientation,
                    this.imageWidth, this.imageHeight);
            this.logger.finest("Structure gradient matrix");
            distMatrix.dump(this.logger, Level.FINEST, 5, 1);
        }

        this.gradientMap = new IndexBitmapObject(finalOrientation,
                this.imageWidth, this.imageHeight);
    }

    /**
     * Run all the necessary computations to produce vicinity map and gradient
     * map
     */
    public void process() {
        // compute displaced structure map. Each pixel has a combination (weighted
        // appropiately) of edges and segments passing through it, displaced in
        // a certain direction
        IndexBitmapObject displacedStructureObject = this.computeWeightedStructureMap();

        //   At this point the 'displacedStructureObject' holds value in 0..255 range for
        // each original pixel. This value signifies the probability of image feature
        // passing through this pixel. Now we can compute the vicinity map.
        this.computeVicinityMap(displacedStructureObject);

        // Now we can compute the gradient map
        this.computeGradientMap(displacedStructureObject);
    }

    /**
     * Get structure vicinity map computed by this object
     *
     * @return structure vicinity map
     */
    public IndexBitmapObject getVicinityMap() {
        return this.vicinityMap;
    }

    /**
     * Get structure gradient map computed by this object
     *
     * @return structure gradient map
     */
    public IndexBitmapObject getGradientMap() {
        return this.gradientMap;
    }
}
