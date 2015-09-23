package twalsh.reporting;

import twalsh.rlg.Node;
import twalsh.rlg.VisibilityConstraint;

import java.util.ArrayList;

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

    @Override
    ArrayList<int[]> calculateRangeOfViewportWidths() {
        ArrayList<int[]> errorRanges = new ArrayList<>();
        VisibilityConstraint vco = oracle.getVisibilityConstraints().get(0);
        VisibilityConstraint vct = test.getVisibilityConstraints().get(0);

        if ((vco.getAppear()!=vct.getAppear()) && (vco.getDisappear()==vct.getDisappear())) {
            errorRanges.add(new int[] {Math.min(vco.getAppear(), vct.getAppear()), Math.max(vco.getAppear(),vct.getAppear())-1});
        } else if ((vco.getAppear()==vct.getAppear()) && (vco.getDisappear()!=vct.getDisappear())) {
            errorRanges.add(new int[] {Math.min(vco.getDisappear(), vct.getDisappear())+1, Math.max(vco.getDisappear(), vct.getDisappear())});
        } else {
            errorRanges.add(new int[] {Math.min(vco.getAppear(), vct.getAppear()), Math.max(vco.getAppear(),vct.getAppear())-1});
            errorRanges.add(new int[] {Math.min(vco.getDisappear(), vct.getDisappear())+1, Math.max(vco.getDisappear(), vct.getDisappear())});
        }
        return errorRanges;
    }
}
