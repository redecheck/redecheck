package twalsh.reporting;

import twalsh.rlg.AlignmentConstraint;
import java.util.ArrayList;
import java.util.Collections;

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

        Collections.sort(unmatched1);
        Collections.sort(unmatched2);
        String previousKey = "";
        String result = "";

        for (AlignmentConstraint ac : unmatched1) {

            // Check if this is a different edge
            if (!ac.generateKeyWithoutLabels().equals(previousKey)) {
                result += ac.node1.getXpath() + " -> " + ac.node2.getXpath();
                previousKey = ac.generateKeyWithoutLabels();

                // Print out the unmatching constraints
                result += "\n Oracle: \n";
                for (AlignmentConstraint um1 : unmatched1) {
                    if (um1.generateKeyWithoutLabels().equals(previousKey))
                        result += "\t" + um1.getMin() + " -> " + um1.getMax() + "     " + um1.generateLabelling() + "\n";
                }
                result += "\n Test: \n";
                for (AlignmentConstraint um2 : unmatched2) {
                    if (um2.generateKeyWithoutLabels().equals(previousKey))
                        result += "\t" + um2.getMin() + " -> " + um2.getMax() + "     " + um2.generateLabelling() + "\n";
                }
            }

        }
        return result;
    }
}
