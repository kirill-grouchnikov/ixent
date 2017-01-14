package org.jvnet.ixent.util;

/**
 * A simple holder for two values of the same type
 *
 * @author Kirill Grouchnikov
 */
public class DoubleHolder <T> {
    private T value1;
    private T value2;

    /**
     * @param pValue1 first value
     * @param pValue2 second value
     */
    public DoubleHolder(T pValue1, T pValue2) {
        this.value1 = pValue1;
        this.value2 = pValue2;
    }

    /**
     * @return first value
     */
    public T getValue1() {
        return this.value1;
    }

    /**
     * @return second value
     */
    public T getValue2() {
        return this.value2;
    }
}
