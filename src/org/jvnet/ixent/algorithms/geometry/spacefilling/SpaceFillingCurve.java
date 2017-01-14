package org.jvnet.ixent.algorithms.geometry.spacefilling;

import java.awt.image.BufferedImage;

import org.jvnet.ixent.graphics.IndexBitmapObject;
import org.jvnet.ixent.math.coord.Vertex2D;

/**
 * Interface for space filling curves. A space filling curve takes as an input
 * an image or a structure vicinity map and produces a set of "centers". The
 * centers are chosen according to a specific implementation
 */
public interface SpaceFillingCurve {
    /**
     * Initialize this filling curve using only width and height. Might throw an
     * exception if this operation is not supported by the given object
     *
     * @param width  image width
     * @param height image height
     * @throws UnsupportedOperationException if the implementor class doesn't
     *                                       logically support this operation
     *                                       (for example, <a href="SpaceFillingCurveStructure.html">
     *                                       SpaceFillingCurveStructure</a>
     *                                       doesn't support initializing with
     *                                       only width and height
     */
    public void init(int width, int height);

    /**
     * Initialize this filling curve using given image. Might throw an exception
     * if this operation is not supported by the given object
     *
     * @param image the input image
     * @throws UnsupportedOperationException if the implementor class doesn't
     *                                       logically support this operation
     *                                       (for example, <a href="SpaceFillingCurveRandom.html">
     *                                       SpaceFillingCurveRandom</a>
     *                                       supports initializing with only
     *                                       width and height
     * @throws IllegalArgumentException      if the image is null
     */
    public void init(BufferedImage image);

    /**
     * Initialize this filling curve using given true-color bitmap. Might throw
     * an exception if this operation is not supported by the given object
     *
     * @param structureVicinityMap the structure vicinity map. Each pixel has an
     *                             associated non-negative integer value
     *                             specifying how far does it lie from some
     *                             image feature (such as edge or segment
     *                             boundary)
     * @throws UnsupportedOperationException if the implementor class doesn't
     *                                       logically support this operation
     *                                       (for example, <a href="SpaceFillingCurveRandom.html">
     *                                       SpaceFillingCurveRandom</a>
     *                                       supports initializing with only
     *                                       width and height
     * @throws IllegalArgumentException      if the map is null or one of the
     *                                       entries in the map is negative
     */
    public void init(IndexBitmapObject structureVicinityMap);

    /**
     * Get centers of this object
     *
     * @return centers of this object
     */
    public Vertex2D[] getCenters();

    /**
     * Get minimal distance between any pair of centers
     *
     * @return minimal distance between any pair of centers
     */
    public int getMinDistanceBetweenCenters();
}

