package twalsh.rlg;

import java.util.ArrayList;
/**
 * Created by thomaswalsh on 10/08/15.
 */
public class Node {
    String xpath;
    int min, max;
    String label;
    ArrayList<WidthConstraint> constraints;

    public Node(String xpath, int min, int max, String label) {
        this.xpath = xpath;
        this.min = min;
        this.max = max;
        this.label = label;
        this.constraints = new ArrayList<WidthConstraint>();
    }

    public String getXpath() {
        try {
            return this.xpath;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return "tester";
        }
    }

    public String getLabel() {
        return label;
    }

    public ArrayList<WidthConstraint> getConstraints() {
        return constraints;
    }

    public String toString() {
//		String result = "Between " + min + " and " + max;
        String result = min + " -> " + max + " : ";
        if (this.xpath != null) {
            result += this.xpath;
        }
        for (WidthConstraint c : constraints) {
            result += "\n\t" + c + " , ";
        }
        return result;
    }

    public void addConstraint(WidthConstraint wc) {
        constraints.add(wc);
    }
}
