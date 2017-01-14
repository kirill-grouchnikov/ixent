package org.jvnet.ixent.algorithms.graphics.tesselation;

import java.util.List;

import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.Polygon2D;

/**
 * Interface for tesselators
 *
 * @author Kirill Grouchnikov
 */
public interface Tesselator {
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
                                          IndexBitmapObject structureVicinityMap);
}
