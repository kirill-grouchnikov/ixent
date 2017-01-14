package org.jvnet.ixent.algorithms.graphics.general;

public class Hungarian {
    private int n;

    private double[][] C;

    private int[][] M;

    private int[] R_cov;

    private int[] C_cov;

    private int stepnum;

    private int Z0_r;

    private int Z0_c;

    private int[][] path;

    public class RowColumnPair {
        public int row;

        public int column;

        public RowColumnPair(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }

    private int step1() {
        double minval;
        for (int col = 0; col < n; col++) {
            minval = C[col][0];
            for (int row = 1; row < n; row++) {
                if (minval > C[col][row]) {
                    minval = C[col][row];
                }
            }

            for (int row = 0; row < n; row++) {
                C[col][row] -= minval;
            }
        }
        return 2;
    }

    private int step2() {
        for (int col = 0; col < n; col++) {
            for (int row = 0; row < n; row++) {
                if ((C[col][row] == 0) && (C_cov[row] == 0) &&
                        (R_cov[col] == 0)) {
                    M[col][row] = 1;
                    C_cov[row] = 1;
                    R_cov[col] = 1;
                }
            }
        }

        for (int col = 0; col < n; col++) {
            C_cov[col] = 0;
            R_cov[col] = 0;
        }
        return 3;
    }

    private int step3() {
        int count;
        for (int col = 0; col < n; col++) {
            for (int row = 0; row < n; row++) {
                if (M[col][row] == 1) {
                    C_cov[row] = 1;
                }
            }
        }
        count = 0;
        for (int row = 0; row < n; row++) {
            count += C_cov[row];
        }
        if (count >= n) {
            return 7;
        }
        else {
            return 4;
        }
    }

    private RowColumnPair findAZero() {
        int i, j;
        boolean done;
        int row = -1;
        int column = -1;

        i = 0;
        done = false;
        while (true) {
            j = 0;
            while (true) {
                if ((C[i][j] == 0) && (R_cov[i] == 0) && (C_cov[j] == 0)) {
                    row = i;
                    column = j;
                    done = true;
                    break;
                }
                j++;
                if (j >= n) {
                    break;
                }
            }
            if (done) {
                break;
            }
            i++;
            if (i >= n) {
                break;
            }
            if (done) {
                break;
            }
        }
        return new RowColumnPair(row, column);
    }

    private boolean starInRow(int row) {
        boolean tbool = false;
        for (int row2 = 0; row2 < n; row2++) {
            if (M[row][row2] == 1) {
                tbool = true;
            }
        }
        return tbool;
    }

    private RowColumnPair findStarInRow(int row) {
        int column = 0;
        for (int row2 = 0; row2 < n; row2++) {
            if (M[row][row2] == 1) {
                column = row2;
            }
        }
        return new RowColumnPair(row, column);
    }

    private int step4() {
        int row, col;
        RowColumnPair pair;
        boolean done = false;
        while (true) {
            pair = this.findAZero();
            row = pair.row;
            col = pair.column;
            if (row == -1) {
                return 6;
            }
            else {
                M[row][col] = 2;
                if (this.starInRow(row)) {
                    pair = this.findStarInRow(row);
                    col = pair.column;
                    R_cov[row] = 1;
                    C_cov[col] = 0;
                }
                else {
                    Z0_r = row;
                    Z0_c = col;
                    return 5;
                }
            }
        }
    }

    private int findStarInCol(int c) {
        int row = -1;
        for (int col = 0; col < n; col++) {
            if (M[col][c] == 1) {
                row = col;
            }
        }
        return row;
    }

    private int findPrimeInRow(int r) {
        int c = 0;
        for (int row = 0; row < n; row++) {
            if (M[r][row] == 2) {
                c = row;
            }
        }
        return c;
    }

    private void convertPath(int count) {
        for (int col = 0; col <= count; col++) {
            if (M[path[col][0]][path[col][1]] == 1) {
                M[path[col][0]][path[col][1]] = 0;
            }
            else {
                M[path[col][0]][path[col][1]] = 1;
            }
        }
    }

    private void clearCovers() {
        for (int i = 0; i < n; i++) {
            R_cov[i] = 0;
            C_cov[i] = 0;
        }
    }

    private void erasePrimes() {
        for (int col = 0; col < n; col++) {
            for (int row = 0; row < n; row++) {
                if (M[col][row] == 2) {
                    M[col][row] = 0;
                }
            }
        }
    }

    private int step5() {
        int count;
        int r, c;
        count = 0;
        path[count][0] = this.Z0_r;
        path[count][1] = this.Z0_c;
        while (true) {
            r = this.findStarInCol(path[count][1]);
            if (r >= 0) {
                count++;
                path[count][0] = r;
                path[count][1] = path[count - 1][1];
            }
            else {
                break;
            }
            c = this.findPrimeInRow(path[count][0]);
            count++;
            path[count][0] = path[count - 1][0];
            path[count][1] = c;
        }
        this.convertPath(count);
        this.clearCovers();
        this.erasePrimes();
        return 3;
    }

    private double findSmallest() {
        double minval = Double.MAX_VALUE;
        for (int col = 0; col < n; col++) {
            for (int row = 0; row < n; row++) {
                if ((R_cov[col] == 0) && (C_cov[row] == 0)) {
                    if (minval > C[col][row]) {
                        minval = C[col][row];
                    }
                }
            }
        }
        return minval;
    }

    private int step6() {
        double minval = this.findSmallest();
        for (int col = 0; col < n; col++) {
            for (int row = 0; row < n; row++) {
                if (R_cov[col] == 1) {
                    C[col][row] += minval;
                }
                if (C_cov[row] == 0) {
                    C[col][row] -= minval;
                }
            }
        }
        return 4;
    }

    public Hungarian(int size, double[][] costMatrix) {
        this.n = size;
        // as algorithm is destuctive, we have to copy the matrix
        this.C = new double[size][size];
        for (int col = 0; col < size; col++) {
            for (int row = 0; row < size; row++) {
                this.C[col][row] = costMatrix[col][row];
            }
        }
        this.M = new int[n][n];
        this.R_cov = new int[n];
        this.C_cov = new int[n];
        this.path = new int[n * n][2];
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Hungarian, step " + stepnum + "\n");
        for (int j = 0; j < n; j++) {
            if (this.C_cov[j] > 0) {
                sb.append("   | ");
            }
            else {
                sb.append("     ");
            }
        }
        sb.append("\n");
        for (int col = 0; col < n; col++) {
            if (this.R_cov[col] > 0) {
                sb.append("- ");
            }
            else {
                sb.append("  ");
            }
            for (int row = 0; row < n; row++) {
                sb.append(C[col][row]);
                switch (M[col][row]) {
                    case 0:
                        sb.append("  ");
                        break;
                    case 1:
                        sb.append("* ");
                        break;
                    case 2:
                        sb.append("' ");
                        break;
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public RowColumnPair[] solve() {
        int stepnum = 1;
        boolean done = false;
        while (!done) {
            switch (stepnum) {
                case 1:
                    stepnum = step1();
                    break;
                case 2:
                    stepnum = step2();
                    break;
                case 3:
                    stepnum = step3();
                    break;
                case 4:
                    stepnum = step4();
                    break;
                case 5:
                    stepnum = step5();
                    break;
                case 6:
                    stepnum = step6();
                    break;
                default:
                    done = true;
            }
        }

        RowColumnPair[] result = new RowColumnPair[this.n];
        for (int col = 0; col < n; col++) {
            for (int row = 0; row < n; row++) {
                if (this.M[col][row] == 1) {
                    result[col] = new RowColumnPair(col, row);
                    break;
                }
            }
        }
        return result;
    }

}