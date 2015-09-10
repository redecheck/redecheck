package twalsh.reporting;

import twalsh.rlg.Node;
import twalsh.rlg.VisibilityConstraint;

/**
 * Created by thomaswalsh on 26/08/15.
 */
public class VisibilityError extends Error {
    // Instance variables
    Node oracle;
    Node test;

    /**
     * Constructs a visibility error
     * @param node1     the node from the oracle
     * @param node2     the node from the test version
     */
    public VisibilityError(Node node1, Node node2) {
        this.oracle = node1;
        this.test = node2;
    }

    /**
     * Generates a formatted string to the error can be printed in an easy to understand way
     * @return      the formatted string
     */
    public String toString() {
        String result = "";
        result += oracle.getXpath();
        result += "\n Oracle: ";
        for (VisibilityConstraint vc : oracle.getVisibilityConstraints()) {
            result += vc + "   ";
        }
        result += "\n Test: ";
        for (VisibilityConstraint vc : test.getVisibilityConstraints()) {
            result += vc + "   ";
        }
        return result;
    }
}
