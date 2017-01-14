package org.jvnet.ixent.algorithms.geometry.dither;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.logging.Logger;

import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.graphics.objects.TriangleEdge;
import org.jvnet.ixent.math.coord.Polygon2D;
import org.jvnet.ixent.math.coord.Vertex2D;

/**
 * A stochastic disperser as described in <i>"Stochastic clustered-dot
 * dithering"</i> paper by Victor Ostromoukhov and Roger D. Hersch
 *
 * @author Kirill Grouchnikov
 */
public final class StochasticDisperser {
    private int intensityLevelsCount;
    private Vertex2D[] vertices;
    private int verticeCount;
    private List<Triangle> triangles;

    private LinkedList<Triangle>[] triangleVortexes;
    private LinkedList<Point2D>[] pointVortexes;
    private LinkedList<Point2D>[] pointVoronoi;

    // ISO-intensity polygons
    private Polygon2D[][] isoIntensityLines;

    private Logger logger;

    /**
     * Construct stochastic disperser
     *
     * @param intensityLevelsCount desired number of intensity levels
     * @param vertices             input vertices
     * @param triangles            triangulation of the input vertices
     */
    public StochasticDisperser(int intensityLevelsCount, Vertex2D[] vertices,
                               List<Triangle> triangles) {

        this.logger = Logger.getLogger(
                StochasticDisperser.class.getPackage().getName());

        this.intensityLevelsCount = intensityLevelsCount;
        this.vertices = vertices;
        this.verticeCount = this.vertices.length;
        this.triangles = triangles;

        this.triangleVortexes = new LinkedList[this.verticeCount];
        for (int i = 0; i < this.verticeCount; i++) {
            this.triangleVortexes[i] = new LinkedList<Triangle>();
        }
        this.pointVortexes = new LinkedList[this.verticeCount];
        for (int i = 0; i < this.verticeCount; i++) {
            this.pointVortexes[i] = new LinkedList<Point2D>();
        }
        this.pointVoronoi = new LinkedList[this.verticeCount];
        for (int i = 0; i < this.verticeCount; i++) {
            this.pointVoronoi[i] = new LinkedList<Point2D>();
        }

        this.isoIntensityLines =
                new Polygon2D[this.verticeCount][this.intensityLevelsCount];

        for (Triangle currTriangle : this.triangles) {
            currTriangle.computeMassCenter();
        }
    }

    /**
     * Compute the "contour" of given vertex. The contour is defined as closed
     * polygon that is a union of all triangles that have given vertex as one of
     * their vertices. These triangles are ordered so that the resulting polygon
     * is closed and non self-crossing
     *
     * @param verticeIndex index of vertex
     */
    private void contourize(int verticeIndex) {
        LinkedList<Triangle> origTrianglesList = this.triangleVortexes[verticeIndex];
        if ((origTrianglesList == null) || (origTrianglesList.size() < 1)) {
            return;
        }

        Vertex2D sinkVertex = this.vertices[verticeIndex];

        LinkedList<Triangle> newTrianglesList = new LinkedList<Triangle>();
        // start from the first triangle
        newTrianglesList.addFirst(origTrianglesList.getFirst());
        origTrianglesList.remove(0);

        // connect in one direction
        TriangleEdge edgeToConnectTo = newTrianglesList.getFirst()
                .getEdgeByVertex1(sinkVertex.getID());
        this.pointVortexes[verticeIndex].addFirst(
                newTrianglesList.getFirst().getMassCenter());
        this.pointVortexes[verticeIndex].addLast(edgeToConnectTo.getMidPoint());
        this.pointVoronoi[verticeIndex].addFirst(
                newTrianglesList.getFirst().getCircCenter());
        while (origTrianglesList.size() > 0) {
            // search the original list for the 'edgeToConnectTo'
            boolean nextEdgeFound = false;
            Iterator<Triangle> it = origTrianglesList.iterator();
            while (it.hasNext()) {
                Triangle currTriangle = it.next();
                TriangleEdge adjacentEdge = currTriangle.getAdjacentEdgeByVertex(
                        sinkVertex.getID(), edgeToConnectTo.getID());
                if (adjacentEdge != null) {
                    // found
                    newTrianglesList.addLast(currTriangle);
                    this.pointVortexes[verticeIndex].addLast(
                            currTriangle.getMassCenter());
                    this.pointVortexes[verticeIndex].addLast(
                            adjacentEdge.getMidPoint());
                    this.pointVoronoi[verticeIndex].addLast(
                            currTriangle.getCircCenter());
                    it.remove();
                    edgeToConnectTo = adjacentEdge;
                    nextEdgeFound = true;
                    break;
                }
            }
            if (!nextEdgeFound) {
                break;
            }
        }

        edgeToConnectTo =
                newTrianglesList.getFirst().getEdgeByVertex2(
                        sinkVertex.getID());
        this.pointVortexes[verticeIndex].addFirst(
                edgeToConnectTo.getMidPoint());

        // connect in the other direction
        while (origTrianglesList.size() > 0) {
            // search the original list for the 'edgeToConnectTo'
            boolean nextEdgeFound = false;
            Iterator<Triangle> it = origTrianglesList.iterator();
            while (it.hasNext()) {
                Triangle currTriangle = it.next();
                TriangleEdge adjacentEdge = currTriangle.getAdjacentEdgeByVertex(
                        sinkVertex.getID(), edgeToConnectTo.getID());
                if (adjacentEdge != null) {
                    // found
                    newTrianglesList.addFirst(currTriangle);
                    this.pointVortexes[verticeIndex].addFirst(
                            currTriangle.getMassCenter());
                    this.pointVortexes[verticeIndex].addFirst(
                            adjacentEdge.getMidPoint());
                    this.pointVoronoi[verticeIndex].addFirst(
                            currTriangle.getCircCenter());
//                    origTrianglesList.removeByID(currElement);
                    it.remove();
                    edgeToConnectTo = adjacentEdge;
                    nextEdgeFound = true;
                    break;
                }
            }
            if (!nextEdgeFound) {
                break;
            }
        }

        assert origTrianglesList.size() == 0 : "Failed in contourizing triangle vortex";

        this.triangleVortexes[verticeIndex] = newTrianglesList;
    }

    /**
     * Create ISO-intensity lines for a single vertex. The distance between each
     * pair of consecutive intensity lines is based on an input array
     *
     * @param vertexIndex     index of vertex
     * @param proportionArray a proportion array. Each value is in 0..1 range,
     *                        the values are monotonical. The distance between
     *                        each pair of consecutive lines is proportional to
     *                        the difference of the corresponding entries in
     *                        this array
     */
    private void createIsoIntensityLines(int vertexIndex,
                                         double[] proportionArray) {
        Vertex2D vertexSink = this.vertices[vertexIndex];

        for (int i = 0; i < this.intensityLevelsCount; i++) {
            // create intensity line for this vertex at given intensity level
            int pointCount = this.pointVortexes[vertexIndex].size();
            Point2D[] newPoints = new Point2D[pointCount];
            int curr = 0;
            for (Point2D currPoint : this.pointVortexes[vertexIndex]) {
                double dx = currPoint.getX() - vertexSink.getX();
                double dy = currPoint.getY() - vertexSink.getY();
                newPoints[curr++] = new Point2D.Double(
                        vertexSink.getX() + dx * proportionArray[i],
                        vertexSink.getY() + dy * proportionArray[i]);
            }
            this.isoIntensityLines[vertexIndex][i] = new Polygon2D(newPoints);//.simplify(0.5, 4);
        }
    }

    /**
     * Compute the ISO-intensity lines for all the vertices. The outermost line
     * for each vertex is an outline of the corresponding Voronoi cell
     */
    public void compute() {
        long time0 = System.currentTimeMillis();

        // create a binary tree to hold the relation 'vertex id' -> 'vertex index'
        SortedMap<Integer, Integer> vertexManager = new TreeMap<Integer, Integer>();
        for (int i = 0; i < this.verticeCount; i++) {
            vertexManager.put(this.vertices[i].getID(), i);
        }

        // compute triangle vortex
        for (Triangle currTriangle : this.triangles) {
            Vertex2D p1 = currTriangle.getVertex12();
            Object val1 = vertexManager.get(p1.getID());
            this.triangleVortexes[((Integer) val1).intValue()].addLast(
                    currTriangle);
            Vertex2D p2 = currTriangle.getVertex13();
            Object val2 = vertexManager.get(p2.getID());
            this.triangleVortexes[((Integer) val2).intValue()].addLast(
                    currTriangle);
            Vertex2D p3 = currTriangle.getVertex23();
            Object val3 = vertexManager.get(p3.getID());
            this.triangleVortexes[((Integer) val3).intValue()].addLast(
                    currTriangle);
        }

        // contourize each vortex
        for (int i = 0; i < this.verticeCount; i++) {
            this.contourize(i);
        }

        // create iso-intensity lines
        double[] proportionArray = new double[this.intensityLevelsCount];
        for (int i = 0; i < this.intensityLevelsCount; i++) {
            proportionArray[i] =
                    Math.sqrt((double) (i + 1) /
                    (double) this.intensityLevelsCount);
        }
        for (int i = 0; i < this.verticeCount; i++) {
            this.createIsoIntensityLines(i, proportionArray);
        }

        long time1 = System.currentTimeMillis();

        this.logger.info("Dither: " + (time1 - time0));
    }

    /**
     * Return the outline of a dither dot situated at given vertice. The size of
     * the dither dot depends on given intensity level
     *
     * @param verticeIndex index of vertice
     * @param intensity    intensity level (must be in 0..255 range). The closer
     *                     to zero, the smaller the resulting dither dot, the
     *                     closer to 255, the closer the resulting dither dot to
     *                     the corresponding point vortex (which approximates
     *                     Voronoi region but is not equal to it)
     * @return polygon outline of the corresponding dither dot
     * @throws IllegalArgumentException if intensity level is not in 0..255
     *                                  range
     */
    public Polygon2D getDitherDot(int verticeIndex, int intensity) {
        if ((intensity < 0) || (intensity > 255)) {
            throw new IllegalArgumentException(
                    "Intensity must be in 0..255 range");
        }

        int intensityLevel = this.intensityLevelsCount * intensity / 255;
        if (intensityLevel < 0) {
            intensityLevel = 0;
        }
        if (intensityLevel >= this.intensityLevelsCount) {
            intensityLevel = this.intensityLevelsCount - 1;
        }
        return this.isoIntensityLines[verticeIndex][intensityLevel];
    }

    /**
     * Return the vortex of given vertice. Each vertex of the corresponding
     * Voronoi cell is vertex of the resulting vortex. Furthermore, between each
     * pair of consecutive Voronoi cell vertices lies an additional vertex
     *
     * @param verticeIndex index of vertice
     * @return its vortex
     */
    public Polygon2D getPointVortex(int verticeIndex) {
        return new Polygon2D(this.pointVortexes[verticeIndex]);
    }

    /**
     * Return the Voronoi region (cell) of given vertice
     *
     * @param verticeIndex index of vertice
     * @return its Voronoi region (cell)
     */
    public Polygon2D getPointVoronoi(int verticeIndex) {
        return new Polygon2D(this.pointVoronoi[verticeIndex]);
    }

    /**
     * Return the vortex of given vertice as a collection of triangles. Each
     * vertex of the corresponding Voronoi cell is vertex of the resulting
     * vortex. Furthermore, between each pair of consecutive Voronoi cell
     * vertices lies an additional vertex. Each triangle of the vortex has the
     * input vertice as one of its vertices.
     *
     * @param verticeIndex index of vertice
     * @return its vortex as a list of triangles
     */
    public LinkedList<Triangle> getTriangleVortex(int verticeIndex) {
        return this.triangleVortexes[verticeIndex];
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("Stochastic disperser: \n");
        result.append(this.verticeCount + " vertices\n");
        result.append(this.triangles.size() + " triangles");
        return result.toString();
    }
}

