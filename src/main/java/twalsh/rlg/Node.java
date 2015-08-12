package twalsh.rlg;

import java.util.ArrayList;
/**
 * Created by thomaswalsh on 10/08/15.
 */
public class Node {
    String xpath;
    int min, max;
    String label;
    ArrayList<WidthConstraint> widthConstraints;
    ArrayList<VisibilityConstraint> visibilityConstraints;

    public Node(String xpath, int min, int max, String label) {
        this.xpath = xpath;
        this.min = min;
        this.max = max;
        this.label = label;
        this.widthConstraints = new ArrayList<WidthConstraint>();
        this.visibilityConstraints = new ArrayList<VisibilityConstraint>();
    }

    public Node(String xpath) {
        this.xpath = xpath;
        this.widthConstraints = new ArrayList<WidthConstraint>();
        this.visibilityConstraints = new ArrayList<VisibilityConstraint>();
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

    public ArrayList<WidthConstraint> getWidthConstraints() {
        return widthConstraints;
    }

    public ArrayList<VisibilityConstraint> getVisibilityConstraints() { return visibilityConstraints; }

    public String toString() {
//		String result = "Between " + min + " and " + max;
        String result = min + " -> " + max + " : ";
        if (this.xpath != null) {
            result += this.xpath;
        }
        for (WidthConstraint c : widthConstraints) {
            result += "\n\t" + c + " , ";
        }
        return result;
    }

    public void addWidthConstraint(WidthConstraint wc) {
        widthConstraints.add(wc);
    }

    public void addVisibilityConstraint(VisibilityConstraint vc) { visibilityConstraints.add(vc); }
}
