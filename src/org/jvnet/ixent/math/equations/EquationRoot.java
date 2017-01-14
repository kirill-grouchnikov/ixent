package org.jvnet.ixent.math.equations;

public final class EquationRoot {
    private ComplexNumber value;
    private int multiplicity;

    /**
     * Constructs a root with specified (complex) value and specified
     * multiplicity
     *
     * @param value        root's (complex) value
     * @param multiplicity root's multiplicity
     */
    public EquationRoot(ComplexNumber value, int multiplicity) {
        this.value = value;
        this.multiplicity = multiplicity;
    }

    /**
     * Returns the value of this root
     *
     * @return the value of this root
     */
    public ComplexNumber getValue() {
        return this.value;
    }

    /**
     * Returns the multiplicity of this root
     *
     * @return the multiplicity of this root
     */
    public int getMultiplicity() {
        return this.multiplicity;
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        return "root : " + this.value.toString() + "[" + this.multiplicity +
                "]";
    }
}


