package twalsh.reporting;

import twalsh.rlg.Node;
import twalsh.rlg.VisibilityConstraint;

/**
 * Created by thomaswalsh on 26/08/15.
 */
public class VisibilityError extends Error {
    Node oracle;
    Node test;

    public VisibilityError(Node node1, Node node2) {
        this.oracle = node1;
        this.test = node2;
    }

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
