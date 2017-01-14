package org.jvnet.ixent.algorithms.graphics.tesselation;

import java.util.LinkedList;
import java.util.List;

import org.jvnet.ixent.algorithms.geometry.delaunay.DelaunayManager;
import org.jvnet.ixent.algorithms.geometry.delaunay.DelaunayManagerFactory;
import org.jvnet.ixent.algorithms.geometry.spacefilling.SpaceFillingCurve;
import org.jvnet.ixent.algorithms.geometry.spacefilling.SpaceFillingCurveStructure;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.graphics.objects.Triangle;
import org.jvnet.ixent.math.coord.Polygon2D;
import org.jvnet.ixent.math.coord.Vertex2D;

/**
 * A tesselator based on Delaunay triangulation
 *
 * @author Kirill Grouchnikov
 */
public class DelaunayTesselator implements Tesselator {

    /**
     * Return the tesselation (a list of polygons) given the structure vicinity
     * map
     *
     * @param cellRadius           radius of a primitive cell used to create the
     *                             tesselation
     * @param structureVicinityMap value for each pixel specifies how close it
     *                             lies to some image feature (edge or segment
     *                             boudary for example)
     * @return the tesselation (a list of polygons) given the structure vicinity
     *         map
     */
    public List<Polygon2D> getTesselation(int cellRadius,
                                          IndexBitmapObject structureVicinityMap) {

        // create space filling curve that takes structure vicinity into account
        SpaceFillingCurve sfc = new SpaceFillingCurveStructure(cellRadius,
                true);
        // initialize it with structure vicinity map
        sfc.init(structureVicinityMap);
        // get centers of the space filling curve
        Vertex2D[] centers = sfc.getCenters();
        // compute Delaunay triangulation
        DelaunayManager dm = DelaunayManagerFactory.getDelaunayManager(centers);
        List<Triangle> triangles = dm.getTriangulation();
        // allocate resulting list
        List<Polygon2D> result = new LinkedList<Polygon2D>();
        // convert each triangle to polygon
        for (Triangle currTriangle : triangles) {
            result.add(new Polygon2D(currTriangle.getPoints()));
        }

        return result;
    }
}
