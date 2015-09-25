package twalsh.reporting;

import twalsh.rlg.AlignmentConstraint;
import twalsh.rlg.Node;
import twalsh.rlg.WidthConstraint;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by thomaswalsh on 26/08/15.
 */
public class WidthError extends Error implements Comparable<WidthError> {
    WidthConstraint oracle, test;
    String desc, xpath;

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

    public WidthError(WidthConstraint we1, WidthConstraint we2, String d, String xp) {
        this.oracle = we1;
        this.test = we2;
        this.desc = d;
        this.xpath = xp;
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
        String key1, key2;
        key1 = this.xpath;
        key2 = we2.xpath;
        return key1.compareTo(key2);
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
            case "diffCoefficients":
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
        return errorRanges;
    }


    public String getXPath() {
        return xpath;
    }

    public String getDesc() {
        return desc;
    }
}
