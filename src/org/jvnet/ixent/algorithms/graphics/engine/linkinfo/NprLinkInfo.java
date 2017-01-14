package org.jvnet.ixent.algorithms.graphics.engine.linkinfo;

/**
 * Class for holding information on segmentation module input link
 *
 * @author Kirill Grouchnikov
 */
public class NprLinkInfo {
    /**
     * the Class object of NPR engine
     */
    private Class nprEngineClass;

    /**
     * @param pNprEngineClass NPR engine class
     */
    public NprLinkInfo(Class pNprEngineClass) {
        this.nprEngineClass = pNprEngineClass;
    }

    /**
     * @return the Class object of NPR engine
     */
    public Class getNprEngineClass() {
        return this.nprEngineClass;
    }
}
