package org.jvnet.ixent.math.equations;

import org.jvnet.ixent.math.MathConstants;

public final class EquationManager {
    private static ComplexNumber cSqrt(ComplexNumber num) {
        double a = num.getRealPart();
        double b = num.getImagPart();
        double r = Math.sqrt(a * a + b * b);
        if (b >= 0.0) {
            return new ComplexNumber(Math.sqrt((r + a) / 2.0),
                    Math.sqrt((r - a) / 2.0));
        }
        else {
            return new ComplexNumber(Math.sqrt((r + a) / 2.0),
                    -Math.sqrt((r - a) / 2.0));
        }
    }

    private static EquationRoot[] solve1(double[] coefficients) {
        // get two coefs, a and b
        double b = coefficients[0];
        double a = coefficients[1];

        // if a is almost 0, return (no roots or infinite number)
        if (Math.abs(a) < MathConstants.EPS) {
            return null;
        }

        // otherwise, create root which is -b/a and put it into rootList
        EquationRoot root = new EquationRoot(new ComplexNumber(-b / a), 1);
        EquationRoot[] roots = new EquationRoot[1];
        roots[0] = root;
        return roots;
    }

    private static EquationRoot[] solve2(double[] coefficients) {
        // get coefs
        double c = coefficients[0];
        double b = coefficients[1];
        double a = coefficients[2];

        // if a is almost 0, this is linear equation.
        if (Math.abs(a) < MathConstants.EPS) {
            // create new coef list and fill it with b and c
            double[] newCoefficients = new double[2];
            newCoefficients[0] = c;
            newCoefficients[1] = b;
            return solve1(newCoefficients);
        }

        // otherwise
        // compute discriminant. Must use cSqrt, as b^2-4ac could be negative
        ComplexNumber disc = cSqrt(new ComplexNumber(b * b - 4.0 * a * c));

        // two cases, for zero or non-zero discriminant
        if (!disc.isEqual(new ComplexNumber(0.0, 0.0), MathConstants.EPS)) {
            // non-zero - we have two distinct (and maybe complex) roots
            ComplexNumber aux = disc.divide(new ComplexNumber(2.0 * a));

            // compute them and put into rootList
            EquationRoot root1 = new EquationRoot(
                    new ComplexNumber(-b / (2.0 * a)).plus(aux), 1);
            EquationRoot root2 = new EquationRoot(
                    new ComplexNumber(-b / (2.0 * a)).minus(aux), 1);
            EquationRoot[] roots = new EquationRoot[2];
            roots[0] = root1;
            roots[1] = root2;
            return roots;
        }
        else {
            // zero discriminant - one root with multiplicity of 2
            EquationRoot root1 = new EquationRoot(
                    new ComplexNumber(-b / (2.0 * a)), 2);
            EquationRoot[] roots = new EquationRoot[1];
            roots[0] = root1;
            return roots;
        }
    }

    // to solve cubic equation
    private static EquationRoot[] solve3(double[] coefficients) {
        // get coefs
        double d = coefficients[0];
        double c = coefficients[1];
        double b = coefficients[2];
        double a = coefficients[3];

        double b1, c1, d1;

        // if a is almost 0, this is quadratic equation.
        if (Math.abs(a) < MathConstants.EPS) {
            // create new coef list and fill it with b, c and d
            double[] newCoefficients = new double[3];
            newCoefficients[0] = d;
            newCoefficients[1] = c;
            newCoefficients[2] = b;
            return solve2(newCoefficients);
        }

        // solve the equation by book formulas
        b1 = b / a;
        c1 = c / a;
        d1 = d / a;
        double adj = -b / (3.0 * a);

        double p = (c1 * 3.0 - b1 * b1) / 3.0;
        double q = (b1 * b1 * b1 * 2.0 - b1 * c1 * 9.0 + d1 * 27.0) / 27.0;
        double D = p * p * p / 27.0 + q * q / 4.0;

        if (D < 0.0) {
            double phi = Math.acos(
                    (-q / 2.0) / Math.sqrt(Math.abs(p * p * p) / 27.0));
            double aux = 2 * Math.sqrt(Math.abs(p) / 3.0);
            EquationRoot root1 = new EquationRoot(
                    new ComplexNumber(adj + aux * Math.cos(phi / 3.0)), 1);
            EquationRoot root2 = new EquationRoot(new ComplexNumber(
                    adj - aux * Math.cos((phi + Math.PI) / 3.0)),
                    1);
            EquationRoot root3 = new EquationRoot(new ComplexNumber(
                    adj - aux * Math.cos((phi - Math.PI) / 3.0)),
                    1);
            EquationRoot[] roots = new EquationRoot[3];
            roots[0] = root1;
            roots[1] = root2;
            roots[2] = root3;
            return roots;
        }

        double u1, u2, v1, v2;
        u1 = -q / 2.0 + Math.sqrt(D);
        u2 = q / 2.0 - Math.sqrt(D);
        v1 = -q / 2.0 - Math.sqrt(D);
        v2 = q / 2.0 + Math.sqrt(D);

        double u, v;
        if (u1 < 0) {
            u = -Math.pow(u2, 1.0 / 3.0);
        }
        else {
            u = Math.pow(u1, 1.0 / 3.0);
        }
        if (v1 < 0) {
            v = -Math.pow(v2, 1.0 / 3.0);
        }
        else {
            v = Math.pow(v1, 1.0 / 3.0);
        }

        if (D == 0.0) {
            if (u == 0.0) {
                EquationRoot root1 = new EquationRoot(new ComplexNumber(adj),
                        3);
                EquationRoot[] roots = new EquationRoot[1];
                roots[0] = root1;
                return roots;
            }
            else {
                EquationRoot root1 = new EquationRoot(
                        new ComplexNumber(adj + 2.0 * u), 1);
                EquationRoot root2 = new EquationRoot(
                        new ComplexNumber(adj - u), 2);
                EquationRoot[] roots = new EquationRoot[2];
                roots[0] = root1;
                roots[1] = root2;
                return roots;
            }
        }
        else {
            EquationRoot root1 = new EquationRoot(
                    new ComplexNumber(adj + u + v), 1);
            EquationRoot root2 = new EquationRoot(new ComplexNumber(adj).plus(new ComplexNumber(
                    -(u + v) / 2.0,
                    (u - v) * Math.sqrt(3.0) / 2.0)),
                    1);
            EquationRoot root3 = new EquationRoot(new ComplexNumber(adj).plus(new ComplexNumber(
                    -(u + v) / 2.0,
                    -(u - v) * Math.sqrt(3.0) / 2.0)),
                    1);
            EquationRoot[] roots = new EquationRoot[3];
            roots[0] = root1;
            roots[1] = root2;
            roots[2] = root3;
            return roots;
        }
    }

    // to solve quadric equation
    private static EquationRoot[] solve4(double[] coefficients) {
        // get coefs
        double first = coefficients[4];

        // if first is almost 0, this is cubic equation.
        if (Math.abs(first) < MathConstants.EPS) {
            // create new coef list and fill it with a, b, c and d
            double[] newCoefficients = new double[4];
            newCoefficients[0] = coefficients[0];
            newCoefficients[1] = coefficients[1];
            newCoefficients[2] = coefficients[2];
            newCoefficients[3] = coefficients[3];
            return solve3(newCoefficients);
        }

        // solve the equation by book formulas

        double d = coefficients[0] / first;
        double c = coefficients[1] / first;
        double b = coefficients[2] / first;
        double a = coefficients[3] / first;

        double[] cList = new double[4];
        cList[0] = -a * a * d + 4.0 * b * d - c * c;
        cList[1] = a * c - 4.0 * d;
        cList[2] = -b;
        cList[3] = 1;

        EquationRoot[] rList = solve(cList);

        ComplexNumber y = rList[0].getValue();
        ComplexNumber temp = new ComplexNumber(a * a / 4.0 - b).plus(y);
        ComplexNumber R;
        if (temp.isEqual(new ComplexNumber(0.0, 0.0), MathConstants.EPS)) {
            R = new ComplexNumber(0.0, 0.0);
        }
        else {
            R = cSqrt(temp);
        }

        ComplexNumber D, E;
        if (R.isEqual(new ComplexNumber(0.0, 0.0), MathConstants.EPS)) {
            ComplexNumber aux1 = new ComplexNumber(3.0 * a * a / 4.0 - 2.0 * b);
            ComplexNumber aux2 = cSqrt(
                    y.times(y).minus(new ComplexNumber(4.0 * d)));
            D = cSqrt(aux1.plus(aux2.times(2.0)));
            E = cSqrt(aux1.minus(aux2.times(2.0)));
        }
        else {
            ComplexNumber aux1 = new ComplexNumber(3.0 * a * a / 4.0 - 2.0 * b).minus(
                    R.times(R));
            double temp2 = (4.0 * a * b - 8.0 * c - a * a * a) / 4.0;
            ComplexNumber aux2;
            if (Math.abs(temp2) < MathConstants.EPS) {
                aux2 = new ComplexNumber(0.0, 0.0);
            }
            else {
                aux2 = new ComplexNumber(temp2).divide(R);
            }
            D = cSqrt(aux1.plus(aux2));
            E = cSqrt(aux1.minus(aux2));
        }

        ComplexNumber A4 = new ComplexNumber(-a / 4.0);
        ComplexNumber R2 = R.times(0.5);
        ComplexNumber D2 = D.times(0.5);
        ComplexNumber E2 = E.times(0.5);

        EquationRoot root1 = new EquationRoot(A4.plus(R2).plus(D2), 1);
        EquationRoot root2 = new EquationRoot(A4.plus(R2).minus(D2), 1);
        EquationRoot root3 = new EquationRoot(A4.minus(R2).plus(E2), 1);
        EquationRoot root4 = new EquationRoot(A4.minus(R2).minus(E2), 1);
        EquationRoot[] roots = new EquationRoot[4];
        roots[0] = root1;
        roots[1] = root2;
        roots[2] = root3;
        roots[3] = root4;
        return roots;
    }

    // removeByID duplicate roots (adjust multiplicity)
    private static EquationRoot[] unifyRoots(EquationRoot[] roots) {
        if (roots == null) {
            return null;
        }
        int count = roots.length;
        if (count == 1) {
            return roots;
        }

        // start going over all roots. if multiple found - adjust and recurse
        for (int i = 0; i < count; i++) {
            for (int j = i + 1; j < count; j++) {
                if (roots[i].getValue().isEqual(roots[j].getValue(),
                        MathConstants.EPS)) {
                    EquationRoot[] newRoots = new EquationRoot[count - 1];
                    // copy all until i
                    for (int k = 0; k < i; k++) {
                        newRoots[k] = roots[k];
                    }
                    // i-th
                    newRoots[i] =
                            new EquationRoot(roots[i].getValue(),
                                    roots[i].getMultiplicity() +
                            roots[j].getMultiplicity());
                    // copy until j
                    for (int k = i + 1; k < j; k++) {
                        newRoots[k] = roots[k];
                    }
                    // copy after j
                    for (int k = j + 1; k < count; k++) {
                        newRoots[k - 1] = roots[k];
                    }
                    return unifyRoots(newRoots);
                }
            }
        }
        return roots;
    }

    // Function to solve the equation
    public static EquationRoot[] solve(double[] coefficients) {
        switch (coefficients.length) {
            case 1:
                return null;
            case 2:
                return solve1(coefficients);
            case 3:
                return solve2(coefficients);
            case 4:
                return solve3(coefficients);
                // for quadric equation - solve and unify roots (only here we might
                // get multiple occurences of the same root)
            case 5:
                return unifyRoots(solve4(coefficients));
                // if more than 5 coefs, return - no roots are found
            default:
                return null;
        }
    }
}


