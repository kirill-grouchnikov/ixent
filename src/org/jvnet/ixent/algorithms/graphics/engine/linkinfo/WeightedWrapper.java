package org.jvnet.ixent.algorithms.graphics.engine.linkinfo;

/**
 * A generic wrapper with associated weight
 *
 * @author Kirill Grouchnikov
 */
public class WeightedWrapper <T> {
    /**
     * weight of this link
     */
    private double weight;

    private T linkObject;

    public WeightedWrapper(T pLinkObject, double pWeight) {
        this.linkObject = pLinkObject;
        this.weight = pWeight;
    }

    public double getWeight() {
        return weight;
    }

    public T getLinkObject() {
        return linkObject;
    }
}
