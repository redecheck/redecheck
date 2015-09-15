package twalsh.reporting;

import twalsh.rlg.AlignmentConstraint;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by thomaswalsh on 26/08/15.
 * Last modified on 15/09/15
 */

public class AlignmentError extends Error implements Comparable<AlignmentError> {
    AlignmentConstraint oracle, test;
    String desc;

    public AlignmentError(AlignmentConstraint ac1, AlignmentConstraint ac2, String d) {
        this.oracle = ac1;
        this.test = ac2;
        this.desc = d;
    }

    public AlignmentConstraint getOracle() { return oracle; }

    public AlignmentConstraint getTest() { return test; }

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
        String key1, key2;
        key1 = this.generateKey();
        key2 = ae2.generateKey();
        return key1.compareTo(key2);
    }

    public String generateKey() {
        String result ="";
        if (this.oracle != null) {
            result = this.oracle.generateKeyWithoutLabels();
        } else {
            result = this.test.generateKeyWithoutLabels();
        }
        return result;
    }
}
