package twalsh.reporting;

import twalsh.rlg.AlignmentConstraint;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by thomaswalsh on 26/08/15.
 * Last modified on 10/09/15
 */
//public class AlignmentError extends Error {
//    // Instance variables
//    ArrayList<AlignmentConstraint> unmatched1, unmatched2;
//
//    /**
//     * Constructs an alignment error for a particular node
//     * @param um1       the set of alignments unmatched from the oracle RLG
//     * @param um2       the set of alignments unmatched from the test/dev RLG
//     */
//    public AlignmentError(ArrayList<AlignmentConstraint> um1, ArrayList<AlignmentConstraint> um2) {
//        this.unmatched1 = um1;
//        this.unmatched2 = um2;
//    }
//
//    /**
//     * Creates a string to print out the differences in alignment so they are easy to read and understand
//     * @return      the formatted result string
//     */
//    public String toString() {
//        Collections.sort(unmatched1);
//        Collections.sort(unmatched2);
//        String previousKey = "";
//        String result = "";
//
//        for (AlignmentConstraint ac : unmatched1) {
//
//            // Check if this is a different edge
//            if (!ac.generateKeyWithoutLabels().equals(previousKey)) {
//                result += ac.node1.getXpath() + " -> " + ac.node2.getXpath();
//                previousKey = ac.generateKeyWithoutLabels();
//
//                // Print out the unmatching constraints
//                result += "\n Oracle: \n";
//                for (AlignmentConstraint um1 : unmatched1) {
//                    if (um1.generateKeyWithoutLabels().equals(previousKey))
//                        result += "\t" + um1.getMin() + " -> " + um1.getMax() + "     " + um1.generateLabelling() + "\n";
//                }
//                result += "\n Test: \n";
//                for (AlignmentConstraint um2 : unmatched2) {
//                    if (um2.generateKeyWithoutLabels().equals(previousKey))
//                        result += "\t" + um2.getMin() + " -> " + um2.getMax() + "     " + um2.generateLabelling() + "\n";
//                }
//            }
//
//        }
//        return result;
//    }
//}

public class AlignmentError extends Error implements Comparable<AlignmentError> {
    AlignmentConstraint oracle, test;
    String desc;

    public AlignmentError(AlignmentConstraint ac1, AlignmentConstraint ac2, String d) {
        this.oracle = ac1;
        this.test = ac2;
        this.desc = d;
    }

    public AlignmentConstraint getOracle() { return oracle; }

    public String toString() {
        String result = "";

        if (desc.equals("unmatched-oracle")) {
            result = "Unmatched in oracle: " + oracle;
        } else if (desc.equals("unmatched-test")) {
            result = "Unmatched in test: " + test;
        } else if (desc.equals("diffBounds")) {
            result = "Differing bounds for {" + oracle.generateLabelling() + "}\n"
                    + "\tOracle : " + oracle.getMin() + " -> " + oracle.getMax() + "\n"
                    + "\tTest : " + test.getMin() + " -> " + test.getMax() + "\n";
        } else if (desc.equals("diffAttributes")) {
            result = "Differing attributes for " + oracle.getMin() + " -> " + oracle.getMax() + "\n"
                    + "\tOracle : {" + oracle.generateLabelling() + "}\n"
                    + "\tTest : {" + test.generateLabelling() +  "}\n";
        }
        return result;
    }

    public int compareTo(AlignmentError ae2) {
        String key1 = this.oracle.generateKeyWithoutLabels();
        String key2 = ae2.oracle.generateKeyWithoutLabels();
        return key1.compareTo(key2);
    }
}
