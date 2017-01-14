package org.jvnet.ixent.algorithms.graphics.engine.linkinfo;

/**
 * Class for holding information on displacement module input link.
 *
 * @author Kirill Grouchnikov
 */
public class TesselationLinkInfo {
    /**
     * tesselator cell radius
     */
    private int cellRadius;

    /**
     * @param pCellRadius tesselator cell radius
     */
    public TesselationLinkInfo(int pCellRadius) {
        this.cellRadius = pCellRadius;
    }

    /**
     * @return tesselator cell radius
     */
    public int getCellRadius() {
        return cellRadius;
    }
}
