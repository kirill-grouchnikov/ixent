package org.jvnet.ixent.algorithms.geometry.voronoi;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.jvnet.ixent.algorithms.geometry.delaunay.DelaunayManager;
import org.jvnet.ixent.algorithms.geometry.delaunay.DelaunayManagerFactory;
import org.jvnet.ixent.algorithms.geometry.dither.StochasticDisperser;
import org.jvnet.ixent.algorithms.geometry.spacefilling.SpaceFillingCurve;
import org.jvnet.ixent.algorithms.geometry.spacefilling.SpaceFillingCurveRandom;
import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.math.coord.*;

/**
 * A class implementing Voronoi tesselation. Exports exact and pixel-precision
 * computations of Voronoi diagrams.
 *
 * @author Kirill Grouchnikov
 */
public final class VoronoiManager {
    /**
     * logger object
     */
    private static Logger logger = Logger.getLogger(
            VoronoiManager.class.getPackage().getName());

    /**
     * Compute pixel-precision Voronoi diagram based on the idea from <i>"Fast
     * computation of generalized Voronoi diagrams using graphics hardware"</i>
     * by Kenneth Hoff, Tim Culver, John Keyser, Ming Lin and Dinesh
     * Manocha.<br> <p/> Here, however, the computations are performed in
     * software (in <i>O(N)</i> time where <i>N</i> is the number of pixels in
     * the image)
     *
     * @param width                         image width
     * @param height                        image height
     * @param averageDistanceBetweenCenters average distance between any pair of
     *                                      neighbouring Voronoi centers
     * @return bitmap object. Each Voronoi region (cell) is assigned a distinct
     *         color, the colors are assigned throughout the whole palette.
     */
    public static BufferedImage getVoronoiDiagram(int width,
                                                          int height,
                                                          int averageDistanceBetweenCenters) {

        long time0 = System.currentTimeMillis();

        // allocate and initialize z-buffer
        int pixelsLeft = width * height;
        int[][] zBuffer = new int[width][height];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                zBuffer[col][row] = -1;
            }
        }

        // choose center points at random in each cell
        int widthInCells = (int) (Math.ceil(
                width / averageDistanceBetweenCenters)) + 1;
        int heightInCells = (int) (Math.ceil(
                height / averageDistanceBetweenCenters)) + 1;

        Vertex2D[] centers = new Vertex2D[widthInCells * heightInCells];
        int centersCount = 0;
        for (int x = 0; x < widthInCells; x++) {
            for (int y = 0; y < heightInCells; y++) {
                int cellLeftPixel = x * averageDistanceBetweenCenters;
                int cellTopPixel = y * averageDistanceBetweenCenters;
                int offsetX = (int) (Math.random() *
                        averageDistanceBetweenCenters);
                int offsetY = (int) (Math.random() *
                        averageDistanceBetweenCenters);
                int pointX = cellLeftPixel + offsetX;
                int pointY = cellTopPixel + offsetY;
                if ((pointX < width) && (pointY < height)) {
                    centers[centersCount] = new Vertex2D(pointX, pointY);
                    zBuffer[pointX][pointY] = centersCount;
                    centersCount++;
                    pixelsLeft--;
                }
            }
        }

        // allocate colors
        int[] colors = new int[centersCount];
        int minColor = 16;
        int colorDelta = (255 - minColor) /
                ((int) Math.floor(Math.pow(centersCount, 1.0 / 3.0)));
        int currR = 255, currG = 255, currB = 255;
        for (int i = 0; i < centersCount; i++) {
            colors[i] = (255 << 24) | (currR << 16) | (currG << 8) | currB;
            currB -= colorDelta;
            if (currB < minColor) {
                currB = 255;
                currG -= colorDelta;
                if (currG < minColor) {
                    currG = 255;
                    currR -= colorDelta;
                }
            }
        }

        // one iteration of voronoi
        int prevRad = 0;
        int currRad = 1;
        while (pixelsLeft > 0) {
            int currRad2 = currRad * currRad;
            int prevRad2 = prevRad * prevRad;
            for (int currCenter = 0; currCenter < centersCount; currCenter++) {
                int centerX = (int) centers[currCenter].getX();
                int centerY = (int) centers[currCenter].getY();
                for (int dx = -currRad; dx <= currRad; dx++) {
                    for (int dy = -currRad; dy <= currRad; dy++) {
                        int d2 = dx * dx + dy * dy;
                        if (d2 <= prevRad2) {
                            // marked during previous iteration
                            continue;
                        }
                        if (d2 > currRad2) {
                            // no need to mark
                            continue;
                        }
                        // check x and y
                        int x = centerX + dx;
                        if ((x < 0) || (x >= width)) {
                            continue;
                        }
                        int y = centerY + dy;
                        if ((y < 0) || (y >= height)) {
                            continue;
                        }
                        if (zBuffer[x][y] >= 0) {
                            // already marked by another color
                            continue;
                        }
                        // mark
                        zBuffer[x][y] = currCenter;
                        pixelsLeft--;
                    }
                }
            }
            prevRad++;
            currRad++;
        }

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                if (zBuffer[col][row] < 0) {
                    result.setRGB(col, row, 0xFF000000);
                }
                else {
                	result.setRGB(col, row, colors[zBuffer[col][row]]);
                }
            }
        }
        for (int i = 0; i < centersCount; i++) {
            int centerX = (int) centers[i].getX();
            int centerY = (int) centers[i].getY();
            if ((centerX < width) && (centerY < height)) {
            	result.setRGB(centerX, centerY, 0xFF000000);
            }
        }

        long time1 = System.currentTimeMillis();
        VoronoiManager.logger.info("Voronoi: " + (time1 - time0) + " (" +
                currRad + " iterations)");

        return result;
    }

    /**
     * Compute pixel-precision Voronoi diagram having roughly given number of
     * centers (regions, cells)
     *
     * @param width               image width
     * @param height              image height
     * @param expectedCenterCount desired center count. The actual center count
     *                            will be kept as close to this figure as
     *                            possible
     * @return an object holding the information on Voronoi diagram
     * @throws IllegalStateException if some pixel is not assigned to any cell
     */
    public static VoronoiIndexDiagramInfo getVoronoiIndexDiagramByCount(
            int width, int height,
            int expectedCenterCount) {

        int averageDistanceBetweenCenters = (int) (Math.sqrt(
                width * height / expectedCenterCount));
        return VoronoiManager.getVoronoiIndexDiagramByDistance(width, height,
                averageDistanceBetweenCenters);
    }

    /**
     * Compute pixel-precision Voronoi diagram having given average distance
     * between region (cell) centers
     *
     * @param width                         image width
     * @param height                        image height
     * @param averageDistanceBetweenCenters average distance between any pair of
     *                                      neighbouring Voronoi centers
     * @return an object holding the information on Voronoi diagram
     * @throws IllegalStateException if some pixel is not assigned to any cell
     */
    public static VoronoiIndexDiagramInfo getVoronoiIndexDiagramByDistance(
            int width, int height,
            int averageDistanceBetweenCenters) {

        // choose center points at random in each cell
        int widthInCells = (int) (Math.ceil(
                width / averageDistanceBetweenCenters)) +
                1;
        int heightInCells = (int) (Math.ceil(
                height / averageDistanceBetweenCenters)) +
                1;

        Point2D[] centers = new Point2D[widthInCells * heightInCells];
        int centersCount = 0;
        for (int x = 0; x < widthInCells; x++) {
            for (int y = 0; y < heightInCells; y++) {
                int cellLeftPixel = x * averageDistanceBetweenCenters;
                int cellTopPixel = y * averageDistanceBetweenCenters;
                int offsetX = (int) (Math.random() *
                        averageDistanceBetweenCenters);
                int offsetY = (int) (Math.random() *
                        averageDistanceBetweenCenters);
                int pointX = cellLeftPixel + offsetX;
                int pointY = cellTopPixel + offsetY;
                if ((pointX < width) && (pointY < height)) {
                    centers[centersCount++] =
                            new Point2D.Double(pointX, pointY);
                }
            }
        }
        Point2D[] realCenters = new Point2D[centersCount];
        for (int i = 0; i < centersCount; i++) {
            realCenters[i] = centers[i];
        }
        return VoronoiManager.getVoronoiIndexDiagram(width, height,
                realCenters);
    }

    /**
     * Compute pixel-precision Voronoi diagram based on the idea from <i>"Fast
     * computation of generalized Voronoi diagrams using graphics hardware"</i>
     * by Kenneth Hoff, Tim Culver, John Keyser, Ming Lin and Dinesh
     * Manocha.<br> <p/> Here, however, the computations are performed in
     * software (in <i>O(N)</i> time where <i>N</i> is the number of pixels in
     * the image)
     *
     * @param width   image width
     * @param height  image height
     * @param centers Voronoi cell centers
     * @return an object holding the information on Voronoi diagram
     * @throws IllegalStateException if some pixel is not assigned to any cell
     */
    public static VoronoiIndexDiagramInfo getVoronoiIndexDiagram(int width,
                                                                 int height,
                                                                 Point2D[] centers) {

        long time0 = System.currentTimeMillis();

        // allocate and initialize z-buffer
        int pixelsLeft = width * height;
        int[][] zBuffer = new int[width][height];
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                zBuffer[col][row] = -1;
            }
        }

        int centersCount = centers.length;
        boolean[] toContinue = new boolean[centersCount];
        for (int currCenter = 0; currCenter < centersCount; currCenter++) {
            int centerX = (int) centers[currCenter].getX();
            int centerY = (int) centers[currCenter].getY();
            zBuffer[centerX][centerY] = currCenter;
            toContinue[currCenter] = true;
            pixelsLeft--;
        }


        // one iteration of voronoi
        int prevRad = 0;
        int currRad = 1;
        while (pixelsLeft > 0) {
            int currRad2 = currRad * currRad;
            int prevRad2 = prevRad * prevRad;
            for (int currCenter = 0; currCenter < centersCount; currCenter++) {
                if (!toContinue[currCenter]) {
                    continue;
                }
                int pixelsMarked = 0;
                int centerX = (int) centers[currCenter].getX();
                int centerY = (int) centers[currCenter].getY();
                for (int dx = -currRad; dx <= currRad; dx++) {
                    for (int dy = -currRad; dy <= currRad; dy++) {
                        int d2 = dx * dx + dy * dy;
                        if (d2 <= prevRad2) {
                            // marked during previous iteration
                            continue;
                        }
                        if (d2 > currRad2) {
                            // no need to mark
                            continue;
                        }
                        // check x and y
                        int x = centerX + dx;
                        if ((x < 0) || (x >= width)) {
                            continue;
                        }
                        int y = centerY + dy;
                        if ((y < 0) || (y >= height)) {
                            continue;
                        }
                        if (zBuffer[x][y] >= 0) {
                            // already marked by another color
                            continue;
                        }
                        // mark
                        zBuffer[x][y] = currCenter;
                        pixelsMarked++;
                        pixelsLeft--;
                    }
                }
                if (pixelsMarked == 0) {
                    toContinue[currCenter] = false;
                }
            }
            prevRad++;
            currRad++;
        }

        // sanity check
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                if (zBuffer[col][row] == -1) {
                    throw new IllegalStateException(
                            "Unexpected error in voronoi - unassigned pixel left");
                }
            }
        }

        long time1 = System.currentTimeMillis();
        VoronoiManager.logger.info("Voronoi: " + (time1 - time0) + " (" +
                currRad + " iterations)");

        VoronoiIndexDiagramInfo resultStruct = new VoronoiIndexDiagramInfo(
                width, height, zBuffer, centers, currRad, centersCount);
        return resultStruct;
    }

    /**
     * Compute exact Voronoi diagram over given rectangle having roughly given
     * number of centers (regions, cells)
     *
     * @param boundingRectangle   bounding rectangle that will hold the cell
     *                            centers
     * @param expectedCenterCount desired center count. The actual center count
     *                            will be kept as close to this figure as
     *                            possible
     * @return a list of polygons. Each polygon is a Voronoi cell
     */
    public static List<Polygon2D> getVoronoiPolygonsByCount(
            Rectangle2D boundingRectangle,
            int expectedCenterCount) {

        int width = (int) (boundingRectangle.getPointBR().getX() -
                boundingRectangle.getPointTL().getX());
        int height = (int) (boundingRectangle.getPointBR().getY() -
                boundingRectangle.getPointTL().getY());

        int averageDistanceBetweenCenters = (int) (Math.sqrt(
                width * height / expectedCenterCount));
        return VoronoiManager.getVoronoiPolygonsByDistance(boundingRectangle,
                averageDistanceBetweenCenters);
    }

    /**
     * Compute exact Voronoi diagram having given average distance between
     * region (cell) centers
     *
     * @param boundingRectangle             bounding rectangle that will hold
     *                                      the cell centers
     * @param averageDistanceBetweenCenters average distance between any pair of
     *                                      neighbouring Voronoi centers
     * @return a list of polygons. Each polygon is a Voronoi cell
     */
    public static List<Polygon2D> getVoronoiPolygonsByDistance(
            Rectangle2D boundingRectangle,
            int averageDistanceBetweenCenters) {

        int width = (int) (boundingRectangle.getPointBR().getX() -
                boundingRectangle.getPointTL().getX());
        int height = (int) (boundingRectangle.getPointBR().getY() -
                boundingRectangle.getPointTL().getY());
        // compute cell centers
        SpaceFillingCurve sfc = new SpaceFillingCurveRandom(
                averageDistanceBetweenCenters, false);
        sfc.init(width, height);
        Vertex2D[] centers = sfc.getCenters();
        // replicate them
        // replicate centers in the vicinity of the edges
        List<Vertex2D> replicatedCenters = new LinkedList<Vertex2D>();
        int offset = 5 + 2 * sfc.getMinDistanceBetweenCenters();
        int leftMarginX = offset;
        int rightMarginX = width - offset;
        int topMarginY = offset;
        int bottomMarginY = height - offset;

        for (int i = 0; i < centers.length; i++) {
            double x = centers[i].getX();
            double y = centers[i].getY();
            // borders
            if (x < leftMarginX) {
                replicatedCenters.add(new Vertex2D(width + x, y));
            }
            if (x > rightMarginX) {
                replicatedCenters.add(new Vertex2D(x - width, y));
            }
            if (y < topMarginY) {
                replicatedCenters.add(new Vertex2D(x, height + y));
            }
            if (y > bottomMarginY) {
                replicatedCenters.add(new Vertex2D(x, y - height));
            }
            // corners
            if ((x < leftMarginX) && (y < topMarginY)) {
                replicatedCenters.add(new Vertex2D(width + x, height + y));
            }
            if ((x > rightMarginX) && (y < topMarginY)) {
                replicatedCenters.add(new Vertex2D(x - width, height + y));
            }
            if ((x < leftMarginX) && (y > bottomMarginY)) {
                replicatedCenters.add(new Vertex2D(width + x, y - height));
            }
            if ((x > rightMarginX) && (y > bottomMarginY)) {
                replicatedCenters.add(new Vertex2D(x - width, y - height));
            }
        }
        Vertex2D[] newCenters = new Vertex2D[centers.length +
                replicatedCenters.size()];
        int prevCount = centers.length;
        for (int i = 0; i < prevCount; i++) {
            newCenters[i] = centers[i];
        }
        int count = 0;
        for (Vertex2D currVertex : replicatedCenters) {
            newCenters[prevCount + count] = currVertex;
            count++;
        }
        centers = newCenters;

        return VoronoiManager.getVoronoiPolygons(centers);
    }

    /**
     * Compute exact Voronoi diagram having given region (cell) centers
     *
     * @param centers region (cell) centers
     * @return a list of polygons. Each polygon is a Voronoi cell
     */
    public static List<Polygon2D> getVoronoiPolygons(Vertex2D[] centers) {
        long time0 = System.currentTimeMillis();
        // compute Delaunay triangulation
        DelaunayManager dm = DelaunayManagerFactory.getDelaunayManager(centers);
        List<Triangle> triangles = dm.getTriangulation();

        if (triangles == null) {
            return null;
        }

        // compute Voronoi regions using stochastic disperser with one
        // intensity level only
        StochasticDisperser sd = new StochasticDisperser(1, centers, triangles);
        sd.compute();

        // return the result
        List<Polygon2D> result = new LinkedList<Polygon2D>();
        for (int i = 0; i < centers.length; i++) {
            result.add(sd.getPointVoronoi(i));
        }

        long time1 = System.currentTimeMillis();
        VoronoiManager.logger.info("Voronoi exact: " + (time1 - time0) + " (" +
                centers.length + " centers)");
        return result;
    }

    /**
     * Compute exact Voronoi-based dither vortexes diagram having given region
     * (cell) centers
     *
     * @param centers region (cell) centers
     * @return a list of polygons. Each polygon is a Voronoi-based dither
     *         vortex
     */
    public static List<Polygon2D> getVoronoiDitherVortexes(Vertex2D[] centers) {
        long time0 = System.currentTimeMillis();
        // compute Delaunay triangulation
        DelaunayManager dm = DelaunayManagerFactory.getDelaunayManager(centers);
        List<Triangle> triangles = dm.getTriangulation();

        if (triangles == null) {
            return null;
        }

        // compute Voronoi regions using stochastic disperser with one
        // intensity level only
        StochasticDisperser sd = new StochasticDisperser(1, centers, triangles);
        sd.compute();

        // return the result
        List<Polygon2D> result = new LinkedList<Polygon2D>();
        for (int i = 0; i < centers.length; i++) {
            result.add(sd.getPointVortex(i));
        }

        long time1 = System.currentTimeMillis();
        VoronoiManager.logger.info("Voronoi exact: " + (time1 - time0) + " (" +
                centers.length + " centers)");
        return result;
    }
}

