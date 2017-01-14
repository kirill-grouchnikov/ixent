package org.jvnet.ixent.algorithms.graphics.general;

import java.util.logging.Logger;

import org.jvnet.ixent.math.coord.PointND;

/**
 * A class for performing K-Means iterative technique for finding K points that
 * best approximate a given collection of sample points
 *
 * @author Kirill Grouchnikov
 */
public final class KMeans {
    // input points
    private PointND[] samplePoints;

    // input points count
    private int sampleSize;

    // input points dimensionality
    private int sampleDimensionality;

    // desired number of centers
    private int K;

    // current centers (for current iteration)
    private PointND[] currCenters;

    // index of closest center
    private int[] closestCenters;

    private Logger logger;

    /**
     * @param samplePoints   an array of N-dimensional input points (non-null)
     * @param dimensionality the N - dimensionality of each input point
     * @param K              desired number of centers (<b>K</b> in K-Means)
     * @throws IllegalArgumentException if have null point in array or point
     *                                  with dimensionality different from
     *                                  <b>dimensionality</b> parameter
     */
    public KMeans(PointND[] samplePoints, int dimensionality, int K) {
        // check point dimensionality
        for (int i = 0; i < samplePoints.length; i++) {
            if (samplePoints[i] == null) {
                throw new IllegalArgumentException("Null point in input array");
            }
            if (samplePoints[i].getDimensionality() != dimensionality) {
                throw new IllegalArgumentException(
                        "Point of unexpected dimensionality in input array");
            }
        }
        this.samplePoints = samplePoints;
        this.sampleSize = this.samplePoints.length;
        this.sampleDimensionality = dimensionality;
        this.K = K;
        this.currCenters = new PointND[K];
        this.closestCenters = new int[this.sampleSize];
        this.logger = Logger.getLogger(KMeans.class.getPackage().getName());
    }

    /**
     * @return the number of centers
     */
    public int getK() {
        return this.K;
    }

    /**
     * Perform single iteration of the algorithm
     *
     * @return sum of squared distances between old centers and new centers. The
     *         summing is performed over the optimal matching between old and
     *         new centers. The optimum is minimizing the sum of squared
     *         distances between chosen matchings (every old center is matched
     *         to exactly one new center and vice versa)
     */
    private double singleIteration() {
        // for each point find the centroid closest to it
        for (int point = 0; point < this.sampleSize; point++) {
            PointND currPoint = this.samplePoints[point];
            double minDist2 = currPoint.dist2(this.currCenters[0]);
            int minIndex = 0;
            for (int cent = 1; cent < this.K; cent++) {
                double currDist2 = currPoint.dist2(this.currCenters[cent]);
                if (currDist2 < minDist2) {
                    minDist2 = currDist2;
                    minIndex = cent;
                }
            }
            this.closestCenters[point] = minIndex;
        }

        // holds how many points belong to each centroid
        int[] clusterSizes = new int[this.K];
        for (int i = 0; i < this.K; i++) {
            clusterSizes[i] = 0;
        }

        double[][] sumCoords = new double[this.K][this.sampleDimensionality];
        for (int i = 0; i < this.K; i++) {
            for (int j = 0; j < this.sampleDimensionality; j++) {
                sumCoords[i][j] = 0.0;
            }
        }

        for (int point = 0; point < this.sampleSize; point++) {
            PointND currPoint = this.samplePoints[point];
            int closest = this.closestCenters[point];
            clusterSizes[closest]++;
            for (int coord = 0; coord < this.sampleDimensionality; coord++) {
                sumCoords[closest][coord] += currPoint.getComponent(coord);
            }
        }

        // compute new centers as centroids of the clusters
        PointND[] newCenters = new PointND[this.K];
        for (int center = 0; center < this.K; center++) {
            if (clusterSizes[center] > 0) {
                // have points in this cluster
                for (int coord = 0;
                     coord < this.sampleDimensionality; coord++) {
                    sumCoords[center][coord] /= clusterSizes[center];
                }
                newCenters[center] = new PointND(sumCoords[center]);
            }
            else {
                newCenters[center] = this.currCenters[center];
            }
        }

        // Compute how much the centers moved. We perform assignment problem -
        // old centers are columns, new centers are rows, distance is cost
        // function.
        // The Hungarian algorithm returns the assignment that minimizes the
        // total cost
        double[][] costMatrix = new double[this.K][this.K];
        for (int newC = 0; newC < this.K; newC++) {
            for (int oldC = 0; oldC < this.K; oldC++) {
                costMatrix[newC][oldC] = newCenters[newC]
                        .dist2(this.currCenters[oldC]);
            }
        }
        Hungarian hung = new Hungarian(this.K, costMatrix);
        Hungarian.RowColumnPair[] matching = hung.solve();
        double dist2 = 0.0;
        for (int i = 0; i < matching.length; i++) {
            dist2 += costMatrix[matching[i].row][matching[i].column];
        }

        // switch centers
        this.currCenters = newCenters;
        return dist2;
    }

    /**
     * Compute centers for the input
     *
     * @param maxDeltaAllowed specifies when to stop the iterative process. Once
     *                        the sum of squared distances in optimum matching
     *                        at given stage falls below this value (or the
     *                        iterations condition holds) the process is
     *                        stopped
     * @param maxIterations   specifies when to stop the iterative process. Once
     *                        we performed this number o iterations (or the
     *                        delta condition holds) the process is stopped
     * @return an array of N-dimensional centers
     */
    public PointND[] getCenters(double maxDeltaAllowed, int maxIterations) {
        // allocate random cluster centers in the bounding hyper-cube
        double[] min = new double[this.sampleDimensionality];
        double[] max = new double[this.sampleDimensionality];
        for (int i = 0; i < this.sampleDimensionality; i++) {
            min[i] = Double.MAX_VALUE;
            max[i] = Double.MIN_VALUE;
        }
        for (int point = 0; point < this.sampleSize; point++) {
            PointND currPoint = this.samplePoints[point];
            for (int coord = 0; coord < this.sampleDimensionality; coord++) {
                double c = currPoint.getComponent(coord);
                if (c < min[coord]) {
                    min[coord] = c;
                }
                if (c > max[coord]) {
                    max[coord] = c;
                }
            }
        }

        // perform [0..1] -> [min..max] transform
        PointND[] startingCenters = new PointND[this.K];
        for (int cent = 0; cent < this.K; cent++) {
            double[] coords = new double[this.sampleDimensionality];
            for (int coo = 0; coo < this.sampleDimensionality; coo++) {
                coords[coo] = min[coo] + (max[coo] - min[coo]) * Math.random();
            }
            startingCenters[cent] = new PointND(coords);
        }

        this.currCenters = startingCenters;
        int itCount = 0;
        while (itCount < maxIterations) {
            long startTime = System.currentTimeMillis();
            double delta = this.singleIteration();
            if (delta < maxDeltaAllowed) {
                break;
            }
            long endTime = System.currentTimeMillis();
            this.logger.finest("iteration " + itCount + ", time " +
                    (endTime - startTime) +
                    ", delta " + delta);
            itCount++;
        }

        return this.currCenters;
    }

    /**
     * Return the index of the center that is closest to a given input point
     *
     * @param sampleIndex index of input point
     * @return index of a center closest to it
     */
    public int getClosestCenterIndex(int sampleIndex) {
        return this.closestCenters[sampleIndex];
    }
}

