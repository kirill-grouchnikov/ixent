package org.jvnet.ixent.math.equations;

public final class ComplexNumber {
    private double realPart;
    private double imagPart;

    public ComplexNumber(double realPart, double imagPart) {
        this.realPart = realPart;
        this.imagPart = imagPart;
    }

    public ComplexNumber(double realPart) {
        this.realPart = realPart;
        this.imagPart = 0.0;
    }

    public double getRealPart() {
        return this.realPart;
    }

    public double getImagPart() {
        return this.imagPart;
    }

    public boolean isEqual(ComplexNumber number2, double epsilon) {
        double diffReal = this.realPart - number2.realPart;
        double diffImag = this.imagPart - number2.imagPart;
        if (diffReal < 0.0) {
            diffReal = -diffReal;
        }
        if (diffImag < 0.0) {
            diffImag = -diffImag;
        }
        return ((diffReal < epsilon) && (diffImag < epsilon));
    }

    public ComplexNumber plus(ComplexNumber number2) {
        return new ComplexNumber(this.realPart + number2.realPart,
                this.imagPart + number2.imagPart);
    }

    public ComplexNumber minus(ComplexNumber number2) {
        return new ComplexNumber(this.realPart - number2.realPart,
                this.imagPart - number2.imagPart);
    }

    public ComplexNumber times(ComplexNumber number2) {
        double r1 = this.realPart, r2 = number2.realPart;
        double i1 = this.imagPart, i2 = number2.imagPart;
        return new ComplexNumber(r1 * r2 - i1 * i2, r1 * i2 + i1 * r2);
    }

    public ComplexNumber times(double coef) {
        return new ComplexNumber(coef * this.realPart, coef * this.imagPart);
    }

    public ComplexNumber divide(ComplexNumber number2) {
        double r1 = this.realPart, r2 = number2.realPart;
        double i1 = this.imagPart, i2 = number2.imagPart;
        double denom = r2 * r2 + i2 * i2;
        return new ComplexNumber((r1 * r2 + i1 * i2) / denom,
                (i1 * r2 - i2 * r1) / denom);
    }

    /**
     * Returns the <code>String</code> representation of this object
     *
     * @return a <code>String</code> representing this object
     */
    public String toString() {
        return "(" + this.realPart + ", " + this.imagPart + ")";
    }
}


