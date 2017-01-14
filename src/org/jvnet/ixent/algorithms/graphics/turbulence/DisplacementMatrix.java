package org.jvnet.ixent.algorithms.graphics.turbulence;

/**
 * 2-D matrix of <a href="DisplacementVector.html">DisplacementVector</a>'s
 *
 * @author Kirill Grouchnikov
 */
public class DisplacementMatrix {
    private int width;
    private int height;
    private DisplacementVector[][] vectors;

    /**
     * Construct empty vector matrix
     *
     * @param pWidth  matrix width
     * @param pHeight matrix height
     */
    public DisplacementMatrix(int pWidth, int pHeight) {
        this.width = pWidth;
        this.height = pHeight;
        this.vectors = new DisplacementVector[this.width][this.height];
    }

    /**
     * Return matrix width
     *
     * @return matrix width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Return matrix height
     *
     * @return matrix height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Return single vector
     *
     * @param col vector column
     * @param row vector row
     * @return vector at this location
     */
    public DisplacementVector getVectorAt(int col, int row) {
        return this.vectors[col][row];
    }

    /**
     * Set single vector
     *
     * @param col    vector column
     * @param row    vector row
     * @param vector new vector at this location
     */
    public void setVectorAt(int col, int row, DisplacementVector vector) {
        this.vectors[col][row] = vector;
    }
}
