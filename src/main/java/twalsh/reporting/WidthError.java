package twalsh.reporting;

import twalsh.rlg.AlignmentConstraint;
import twalsh.rlg.Node;
import twalsh.rlg.WidthConstraint;

import java.util.ArrayList;

/**
 * Created by thomaswalsh on 26/08/15.
 */
public class WidthError extends Error implements Comparable<WidthError> {
    WidthConstraint oracle, test;
    String desc;

    public WidthConstraint getOracle() {
        return oracle;
    }

    public void setOracle(WidthConstraint oracle) {
        this.oracle = oracle;
    }

    public WidthConstraint getTest() {
        return test;
    }

    public void setTest(WidthConstraint test) {
        this.test = test;
    }

    public WidthError(WidthConstraint we1, WidthConstraint we2, String d) {
        this.oracle = we1;
        this.test = we2;
        this.desc = d;
    }

    public String toString() {
        String result = "";

        if (desc.equals("unmatched-oracle")) {
            result = "Unmatched in oracle: " + oracle;
        } else if (desc.equals("unmatched-test")) {
            result = "Unmatched in test: " + test;
        } else if (desc.equals("diffBounds")) {
            result = "Differing bounds for {" + oracle.generateEquationString() + "}\n"
                    + "\tOracle : " + oracle.getMin() + " -> " + oracle.getMax() + "\n"
                    + "\tTest : " + test.getMin() + " -> " + test.getMax() + "\n";
        } else if (desc.equals("diffCoefficients")) {
            result = "Differing coefficients for " + oracle.getMin() + " -> " + oracle.getMax() + "\n"
                    + "\tOracle : {" + oracle.generateEquationString() + "}\n"
                    + "\tTest : {" + test.generateEquationString() +  "}\n";
        }
        return result;
    }

    public int compareTo(WidthError we2) {
        Integer key1, key2;
        key1 = this.getOracle().getMin();
        key2 = we2.getOracle().getMin();
        return key1.compareTo(key2);
    }

//    // Instance variables
//    Node n;
//    ArrayList<WidthConstraint> unmatched1, unmatched2;
//
//    /**
//     * Constucts a width error for a particular node
//     * @param node          the node for which there are unmatched constraints
//     * @param um1           the set of unmatched constraints from the oracle RLG
//     * @param um2           the set of unmatched constraints from the test RLG
//     */
//    public WidthError(Node node, ArrayList<WidthConstraint> um1, ArrayList<WidthConstraint> um2) {
//        this.n = node;
//        this.unmatched1 = um1;
//        this.unmatched2 = um2;
//    }
//
//    /**
//     * Generates a formatted string to the width constraint errors can be printed out nicely
//     * @return          the formatted output string
//     */
//    public String toString() {
//        String result= "";
//        // Get the nodes to print out
//        result += n.getXpath();
//
//        // Print out the unmatching constraints
//        result += "\n Oracle: \n";
//        for (WidthConstraint wc : unmatched1) {
//            result += "\t" + wc + "\n";
//        }
//        result += "Test: \n";
//        for (WidthConstraint wc : unmatched2) {
//            result += "\t" + wc + "\n";
//        }
//        return result;
//    }

}
