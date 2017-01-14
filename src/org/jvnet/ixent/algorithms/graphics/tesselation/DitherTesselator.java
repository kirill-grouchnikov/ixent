package org.jvnet.ixent.algorithms.graphics.tesselation;

import java.util.List;

import org.jvnet.ixent.algorithms.geometry.spacefilling.SpaceFillingCurve;
import org.jvnet.ixent.algorithms.geometry.spacefilling.SpaceFillingCurveStructure;
import org.jvnet.ixent.algorithms.geometry.voronoi.VoronoiManager;
import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.Polygon2D;
import org.jvnet.ixent.math.coord.Vertex2D;

/**
 * A tesselator based on stochastic disperser as described in <i>"Stochastic
 * clustered-dot dithering"</i> paper by Victor Ostromoukhov and Roger D Hersch
 *
 * @author Kirill Grouchnikov
 */
public class DitherTesselator implements Tesselator {
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
        // compute Voronoi-based dither-dot tesselation
        List<Polygon2D> result = VoronoiManager.getVoronoiDitherVortexes(
                centers);

        return result;
    }
}
