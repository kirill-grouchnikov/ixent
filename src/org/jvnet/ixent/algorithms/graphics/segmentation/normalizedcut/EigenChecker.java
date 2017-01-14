package org.jvnet.ixent.algorithms.graphics.segmentation.normalizedcut;

public interface EigenChecker {
    public boolean isAcceptable(int iteration, double value, double[] vector);
}
