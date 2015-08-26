package twalsh.reporting;

import twalsh.rlg.AlignmentConstraint;
import java.util.ArrayList;

/**
 * Created by thomaswalsh on 26/08/15.
 */
public class AlignmentError extends Error {
    ArrayList<AlignmentConstraint> unmatched1, unmatched2;

    public AlignmentError(ArrayList<AlignmentConstraint> um1, ArrayList<AlignmentConstraint> um2) {
        this.unmatched1 = um1;
        this.unmatched2 = um2;
    }

    public String toString() {
        String result = "";
        // Get the nodes to print out
        if (unmatched1.size() > 0) {
            result += unmatched1.get(0).node1.getXpath() + " -> " + unmatched1.get(0).node2.getXpath();
        } else {
            result += unmatched2.get(0).node1.getXpath() + " -> " + unmatched2.get(0).node2.getXpath();
        }

        // Print out the unmatching constraints
        result += "\n Oracle: \n";
        for (AlignmentConstraint ac : unmatched1) {
            result += "\t" + ac.getMin() + " -> " + ac.getMax() + "     " + ac.generateLabelling() + "\n";
        }
        result += "\n Test: \n";
        for (AlignmentConstraint ac : unmatched2) {
            result += "\t" + ac.getMin() + " -> " + ac.getMax() + "     " + ac.generateLabelling() + "\n";
        }
        return result;
    }
}
