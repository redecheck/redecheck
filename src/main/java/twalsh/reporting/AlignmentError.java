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
            result = "Unmatched in oracle: " + oracle + "\n";
        } else if (desc.equals("unmatched-test")) {
            result = "Unmatched in test: " + test + "\n";
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

    @Override
    public ArrayList<int[]> calculateRangeOfViewportWidths() {
        ArrayList<int[]> errorRanges = new ArrayList<>();
        switch (desc) {
            case "unmatched-oracle":
                errorRanges.add(new int[]{oracle.getMin(), oracle.getMax()});
                break;
            case "unmatched-test":
                errorRanges.add(new int[]{test.getMin(), test.getMax()});
                break;
            case "diffAttributes":
                errorRanges.add(new int[]{oracle.getMin(), oracle.getMax()});
                break;
            case "diffBounds":
                // Check the bounds to work out which ranges are erroneous
                if ((oracle.getMin() != test.getMin()) && (oracle.getMax() == test.getMax())) {
                    errorRanges.add(new int[]{Math.min(oracle.getMin(), test.getMin()), Math.max(oracle.getMin(), test.getMin()) - 1});
                } else if ((oracle.getMin() == test.getMin()) && (oracle.getMax() != test.getMax())) {
                    errorRanges.add(new int[]{Math.min(oracle.getMax(), test.getMax()) + 1, Math.max(oracle.getMax(), test.getMax())});
                } else if ((oracle.getMin() != test.getMin()) && (oracle.getMax() != test.getMax())) {
                    errorRanges.add(new int[]{Math.min(oracle.getMin(), test.getMin()), Math.max(oracle.getMin(), test.getMin()) - 1});
                    errorRanges.add(new int[]{Math.min(oracle.getMax(), test.getMax()) + 1, Math.max(oracle.getMax(), test.getMax())});
                }
                break;
        }
//        for (int[] a : errorRanges)
//        System.out.println(a[0] + " -> " + a[1]);
        return errorRanges;
    }

    public String getDesc() {
        return desc;
    }
}
