package org.jvnet.ixent.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Kirill Grouchnikov
 */
public class IxentTestSuite {
    public static Test suite() {
        TestSuite ts = new TestSuite();
        ts.addTestSuite(TestSegments.class);
        ts.addTestSuite(TestSquares.class);
        ts.addTestSuite(TestMatrix.class);
        return ts;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
