package org.jvnet.ixent.algorithms.graphics.engine.npr.watercolor;

import java.util.List;

import org.jvnet.ixent.algorithms.graphics.engine.linkinfo.WeightedWrapper;
import org.jvnet.ixent.graphics.IndexBitmapObject;

/**
 * The glaze data structure as specified in <i>"Computer-Generated
 * Watercolor"</i> by Cassidy Curtis, Sean Anderson, Joshua Seims, Kurt
 * Fleischery and David Salesin in 1997 paper.<br>
 *
 * @author Kirill Grouchnikov
 * @see org.jvnet.ixent.algorithms.graphics.engine.npr.WatercolorEngine
 */
public class Glaze {
    private GlazePixelInfo[][] pixelInfo;
    private int width, height;
    private double maxVelocity;

    /**
     * Information on a single pixel in this glaze
     */
    private static class GlazePixelInfo {
        public boolean isInWetMask;
        public double distanceToWetMaskBoundary;
        public double waterVelocityX;
        public double waterVelocityY;
        public double waterPressure;
        public double paperSaturation;
        public PigmentList pigmentConcentrations;

        public GlazePixelInfo() {
            this.isInWetMask = false;
            this.distanceToWetMaskBoundary = 1.0;
            this.waterVelocityX = 0.0;
            this.waterVelocityY = 0.0;
            this.waterPressure = 1.0;
            this.paperSaturation = 0.0;
            this.pigmentConcentrations = new PigmentList();
        }

    }

    /**
     * Constructs a single glaze for the whole image.
     *
     * @param quantizedColorIndexes 2D array of indexes of quantization colors.
     *                              Entries with the same value are approximated
     *                              by the same discrete quantizing color
     * @param quantizationColorsApproximation
     *                              1D array of pigment approximation of each
     *                              discrete quantizing color
     * @param pMaxVelocity          maximal velocity of water in horizontal and
     *                              vertical directions
     * @throws IllegalArgumentException if some parameter is null, no pigment
     *                                  approximation is given for some
     *                                  quantizing color, or there is some entry
     *                                  in quantization color 2D array that has
     *                                  no mapping in pigment approximation
     *                                  array
     */
    public Glaze(IndexBitmapObject quantizedColorIndexes,
                 List<WeightedWrapper<Pigment>>[] quantizationColorsApproximation,
                 double pMaxVelocity) {

        if ((quantizedColorIndexes == null) ||
                (quantizationColorsApproximation == null)) {
            throw new IllegalArgumentException("Can't pass null parameters");
        }

        for (List<WeightedWrapper<Pigment>> currApproximation :
                quantizationColorsApproximation) {
            if ((currApproximation == null) || (currApproximation.size() == 0)) {
                throw new IllegalArgumentException(
                        "Pigment approximation for must be non-null and non-empty");
            }
        }

        int approximationSize = quantizationColorsApproximation.length;

        this.width = quantizedColorIndexes.getWidth();
        this.height = quantizedColorIndexes.getHeight();

        // check that have approximation for each quantizing color
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                int quantizingColorIndex = quantizedColorIndexes.getValue(col,
                        row);
                if ((quantizingColorIndex < 0) ||
                        (quantizingColorIndex >= approximationSize)) {
                    throw new IllegalArgumentException(
                            "No approximation for [" + col + ", " + row + "]");
                }
            }
        }

        this.maxVelocity = pMaxVelocity;
        this.pixelInfo = new GlazePixelInfo[this.width][this.height];
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                this.pixelInfo[col][row] = new GlazePixelInfo();
                // set wet mask flag
                this.pixelInfo[col][row].isInWetMask = true;
                // set initial pigment concentrations
                int colorIndex = quantizedColorIndexes.getValue(col, row);
                List<WeightedWrapper<Pigment>> pigmentList =
                        quantizationColorsApproximation[colorIndex];
                for (WeightedWrapper<Pigment> currPigment : pigmentList) {
                    this.pixelInfo[col][row].pigmentConcentrations.setPigment(
                            currPigment.getLinkObject(),
                            currPigment.getWeight());
                }
            }
        }
    }

    /**
     * Constructs a glaze for one segment of the image.
     *
     * @param segmentationAreas     2D array of image segments. Entries with the
     *                              same value belong to the same image segment
     * @param areaIndex             Image segment index to create a glaze for.
     * @param quantizedColorIndexes 2D array of indexes of quantization colors.
     *                              Entries with the same value are approximated
     *                              by the same discrete quantizing color
     * @param quantizationColorsApproximation
     *                              1D array of pigment approximation of each
     *                              discrete quantizing color
     * @param pMaxVelocity          maximal velocity of water in horizontal and
     *                              vertical directions
     * @throws IllegalArgumentException if some parameter is null, no pigment
     *                                  approximation is given for some
     *                                  quantizing color, or there is some entry
     *                                  in quantization color 2D array that has
     *                                  no mapping in pigment approximation
     *                                  array
     */
    public Glaze(IndexBitmapObject segmentationAreas, int areaIndex,
                 IndexBitmapObject quantizedColorIndexes,
                 List<WeightedWrapper<Pigment>>[] quantizationColorsApproximation,
                 double pMaxVelocity) {
        if ((quantizedColorIndexes == null) ||
                (quantizationColorsApproximation == null)) {
            throw new IllegalArgumentException("Can't pass null parameters");
        }

        for (List<WeightedWrapper<Pigment>> currApproximation :
                quantizationColorsApproximation) {
            if ((currApproximation == null) || (currApproximation.size() == 0)) {
                throw new IllegalArgumentException(
                        "Pigment approximation for must be non-null and non-empty");
            }
        }

        int approximationSize = quantizationColorsApproximation.length;

        this.width = quantizedColorIndexes.getWidth();
        this.height = quantizedColorIndexes.getHeight();

        // check that have approximation for each quantizing color
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                int quantizingColorIndex = quantizedColorIndexes.getValue(col,
                        row);
                if ((quantizingColorIndex < 0) ||
                        (quantizingColorIndex >= approximationSize)) {
                    throw new IllegalArgumentException(
                            "No approximation for [" + col + ", " + row + "]");
                }
            }
        }

        this.maxVelocity = pMaxVelocity;
        this.pixelInfo = new GlazePixelInfo[this.width][this.height];
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                this.pixelInfo[col][row] = new GlazePixelInfo();
                if (segmentationAreas.getValue(col, row) == areaIndex) {
                    // set wet mask flag
                    this.pixelInfo[col][row].isInWetMask = true;
                    // set initial pigment concentrations
                    int colorIndex = quantizedColorIndexes.getValue(col, row);
                    List<WeightedWrapper<Pigment>> pigmentList =
                            quantizationColorsApproximation[colorIndex];
                    for (WeightedWrapper<Pigment> currPigment : pigmentList) {
                        this.pixelInfo[col][row].pigmentConcentrations.setPigment(
                                currPigment.getLinkObject(),
                                currPigment.getWeight());
                    }
                }
            }
        }
    }

    /**
     * Returns the width of the embedding image
     *
     * @return the width of the embedding image
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the embedding image
     *
     * @return the height of the embedding image
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the water horizontal velocity at specified location
     *
     * @param column column of interest
     * @param row    row of interest
     * @return the water horizontal velocity at this location
     */
    public double getWaterVelocityX(int column, int row) {
        if ((column < 0) || (column >= this.width)) {
            return 0.0;
        }
        if ((row < 0) || (row >= this.height)) {
            return 0.0;
        }
        return this.pixelInfo[column][row].waterVelocityX;
    }

    /**
     * Returns the water vertical velocity at specified location
     *
     * @param column column of interest
     * @param row    row of interest
     * @return the water vertical velocity at this location
     */
    public double getWaterVelocityY(int column, int row) {
        if ((column < 0) || (column >= this.width)) {
            return 0.0;
        }
        if ((row < 0) || (row >= this.height)) {
            return 0.0;
        }
        return this.pixelInfo[column][row].waterVelocityY;
    }

    /**
     * Sets the water horizontal velocity at specified location
     *
     * @param column   column of interest
     * @param row      row of interest
     * @param newValue new horizontal velocity at this location
     */
    public void setWaterVelocityX(int column, int row, double newValue) {
        if (newValue > this.maxVelocity) {
            newValue = this.maxVelocity;
        }
        if (newValue < -this.maxVelocity) {
            newValue = -this.maxVelocity;
        }
        this.pixelInfo[column][row].waterVelocityX = newValue;
    }

    /**
     * Sets the water vertical velocity at specified location
     *
     * @param column   column of interest
     * @param row      row of interest
     * @param newValue new vertical velocity at this location
     */
    public void setWaterVelocityY(int column, int row, double newValue) {
        if (newValue > this.maxVelocity) {
            newValue = this.maxVelocity;
        }
        if (newValue < -this.maxVelocity) {
            newValue = -this.maxVelocity;
        }
        this.pixelInfo[column][row].waterVelocityY = newValue;
    }

    /**
     * Returns the water pressure at specified location
     *
     * @param column column of interest
     * @param row    row of interest
     * @return the water pressure at this location
     */
    public double getWaterPressure(int column, int row) {
        if ((column < 0) || (column >= this.width)) {
            return 0.0;
        }
        if ((row < 0) || (row >= this.height)) {
            return 0.0;
        }
        return this.pixelInfo[column][row].waterPressure;
    }

    /**
     * Sets the water pressure at specified location
     *
     * @param column   column of interest
     * @param row      row of interest
     * @param newValue new pressure at this location
     */
    public void setWaterPressure(int column, int row, double newValue) {
        this.pixelInfo[column][row].waterPressure = newValue;
    }

    /**
     * Returns the paper saturation at specified location
     *
     * @param column column of interest
     * @param row    row of interest
     * @return the paper saturation at this location
     */
    public double getPaperSaturation(int column, int row) {
        if ((column < 0) || (column >= this.width)) {
            return 0.0;
        }
        if ((row < 0) || (row >= this.height)) {
            return 0.0;
        }
        return this.pixelInfo[column][row].paperSaturation;
    }

    /**
     * Sets the paper saturation at specified location
     *
     * @param column   column of interest
     * @param row      row of interest
     * @param newValue paper saturation at this location
     */
    public void setPaperSaturation(int column, int row, double newValue) {
        this.pixelInfo[column][row].paperSaturation = newValue;
    }

    /**
     * Returns the distance to wet mask boundary at specified location
     *
     * @param column column of interest
     * @param row    row of interest
     * @return the distance to wet mask boundary at this location
     */
    public double getDistanceToWetMaskBoundary(int column, int row) {
        return this.pixelInfo[column][row].distanceToWetMaskBoundary;
    }

    /**
     * Returns whether the specified location is in this glaze's wet mask
     *
     * @param column column of interest
     * @param row    row of interest
     * @return <code>true</code> if the specified location is in this glaze's
     *         wet mask and <code>false</code> otherwise
     */
    public boolean isInWetMask(int column, int row) {
        return this.pixelInfo[column][row].isInWetMask;
    }

    /**
     * Changes the property of belonging to this glaze's wet mask for specified
     * location
     *
     * @param column   column of interest
     * @param row      row of interest
     * @param newValue property of belonging to this glaze's wet mask for this
     *                 location
     */
    public void setInWetMask(int column, int row, boolean newValue) {
        this.pixelInfo[column][row].isInWetMask = newValue;
    }

    /**
     * Returns the concentration of the specified pigment at specified location
     *
     * @param column  column of interest
     * @param row     row of interest
     * @param pigment pigment of interest
     * @return the water the concentration of this pigment at this location
     */
    public double getPigmentConcentration(int column, int row, Pigment pigment) {
        return this.pixelInfo[column][row].pigmentConcentrations.getPigmentConcentration(
                pigment);
    }

    /**
     * Sets the concentration of the specified pigment at specified location
     *
     * @param column        column of interest
     * @param row           row of interest
     * @param pigment       pigment of interest
     * @param concentration new concentration of this pigment at this location
     */
    public void setPigmentConcentration(int column, int row, Pigment pigment,
                                        double concentration) {
        this.pixelInfo[column][row].pigmentConcentrations.setPigment(pigment,
                concentration);
    }

    /**
     * Returns a collection of all pigment concentrations at specified location
     *
     * @param column column of interest
     * @param row    row of interest
     * @return collection of all pigment concentrations at this location
     */
    public PigmentList getAllPigmentConcentrations(int column, int row) {
        return this.pixelInfo[column][row].pigmentConcentrations;
    }

    /**
     * Enforces boundary conditions for this glaze (section 4.3.1)
     */
    public void enforceBoundaryConditions() {
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                GlazePixelInfo currPixel = this.pixelInfo[col][row];
                if (!currPixel.isInWetMask) {
                    currPixel.waterVelocityX = 0.0;
                    currPixel.waterVelocityY = 0.0;
                }
            }
        }
    }

    /**
     * Computes distance to wet-mask boundary for all the pixels (not only in
     * this glaze, but in the whole embedding image). The distance is computed
     * to the nearest pixel lying on wet-mask boundary (Eucledian metric).
     *
     * @param maxDistance for all pixels lying farther than this value, this
     *                    value will be set as the distance to wet-mask
     *                    boundary
     */
    public void computeDistancesToWetMaskBoundary(int maxDistance) {
        //   The vicinity map assigns a double value to each pixel. This
        // value signifies how close is this pixel to some image feature. To
        // compute this we go over all pixels that lie on a boundary of wet-mask
        // and update all its neighbours: each neighbour pixel holds the
        // (up to now) minimal distance to some wet-mask boundary.
        // Each time we have wet-mask boundary that lies closer, we
        // update the corresponding pixels.

        // compute wet-mask boundary
        // compute "thick boundary" map. A pixel has value 0 if all of its neighbours
        // have the same color value. Otherwise it has value 255
        boolean[][] thickBoundaries = new boolean[this.width][this.height];
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                thickBoundaries[col][row] = false;
                for (int dc = -1; dc <= 1; dc++) {
                    int newCol = col + dc;
                    if ((newCol < 0) || (newCol == this.width)) {
                        continue;
                    }
                    for (int dr = -1; dr <= 1; dr++) {
                        int newRow = row + dr;
                        if ((newRow < 0) || (newRow == this.height)) {
                            continue;
                        }
                        if (this.pixelInfo[col][row].isInWetMask !=
                                this.pixelInfo[newCol][newRow].isInWetMask) {
                            thickBoundaries[col][row] = true;
                            break;
                        }
                    }
                }
            }
        }


        // update boundary distance array
        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                this.pixelInfo[col][row].distanceToWetMaskBoundary = 1.0;
            }
        }

        for (int col = 0; col < this.width; col++) {
            for (int row = 0; row < this.height; row++) {
                if (!thickBoundaries[col][row]) {
                    continue;
                }
                for (int dx = -maxDistance; dx <= maxDistance; dx++) {
                    int nx = col + dx;
                    if ((nx < 0) || (nx >= this.width)) {
                        continue;
                    }
                    for (int dy = -maxDistance; dy <= maxDistance; dy++) {
                        int ny = row + dy;
                        if ((ny < 0) || (ny >= this.height)) {
                            continue;
                        }
                        this.pixelInfo[nx][ny].distanceToWetMaskBoundary =
                                Math.min(1.0,
                                        Math.sqrt(dx * dx + dy * dy) /
                                maxDistance);
                    }
                }
            }
        }
    }
}
