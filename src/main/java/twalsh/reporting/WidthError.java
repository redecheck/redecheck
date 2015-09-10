package twalsh.reporting;

import twalsh.rlg.AlignmentConstraint;
import twalsh.rlg.Node;
import twalsh.rlg.WidthConstraint;

import java.util.ArrayList;

/**
 * Created by thomaswalsh on 26/08/15.
 */
public class WidthError extends Error {
    // Instance variables
    Node n;
    ArrayList<WidthConstraint> unmatched1, unmatched2;

    /**
     * Constucts a width error for a particular node
     * @param node          the node for which there are unmatched constraints
     * @param um1           the set of unmatched constraints from the oracle RLG
     * @param um2           the set of unmatched constraints from the test RLG
     */
    public WidthError(Node node, ArrayList<WidthConstraint> um1, ArrayList<WidthConstraint> um2) {
        this.n = node;
        this.unmatched1 = um1;
        this.unmatched2 = um2;
    }

    /**
     * Generates a formatted string to the width constraint errors can be printed out nicely
     * @return          the formatted output string
     */
    public String toString() {
        String result= "";
        // Get the nodes to print out
        result += n.getXpath();

        // Print out the unmatching constraints
        result += "\n Oracle: \n";
        for (WidthConstraint wc : unmatched1) {
            result += "\t" + wc + "\n";
        }
        result += "Test: \n";
        for (WidthConstraint wc : unmatched2) {
            result += "\t" + wc + "\n";
        }
        return result;
    }

}
