package twalsh.rlg;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by thomaswalsh on 10/08/15.
 */
public class Node {
    String xpath;
    String label;
    ArrayList<WidthConstraint> widthConstraints;
    ArrayList<VisibilityConstraint> visibilityConstraints;
    ArrayList<AlignmentConstraint> parentConstraints;

    public Node(String xpath,  String label) {
        this.xpath = xpath;
        this.label = label;
        this.widthConstraints = new ArrayList<WidthConstraint>();
        this.visibilityConstraints = new ArrayList<VisibilityConstraint>();
        this.parentConstraints = new ArrayList<AlignmentConstraint>();
    }

    public Node(String xpath) {
        this.xpath = xpath;
        this.widthConstraints = new ArrayList<WidthConstraint>();
        this.visibilityConstraints = new ArrayList<VisibilityConstraint>();
        this.parentConstraints = new ArrayList<AlignmentConstraint>();

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

    public ArrayList<AlignmentConstraint> getParentConstraints() {
        ArrayList<AlignmentConstraint> result = new ArrayList<AlignmentConstraint>();
        TreeMap<Integer, AlignmentConstraint> ordered = new TreeMap<Integer, AlignmentConstraint>();
        for (AlignmentConstraint c : parentConstraints) {
            ordered.put(c.min,c);
        }

        for (AlignmentConstraint ac : ordered.values()) {
            result.add(ac);
        }
        return result;
    }

    public String toString() {
        String result = "";
        for (VisibilityConstraint vc : visibilityConstraints) {
            result += "\n\t" + vc;
        }
        if (this.xpath != null) {
            result += this.xpath;
        }
        for (WidthConstraint c : widthConstraints) {
            result += "\n\t" + c;
        }
        return result;
    }

    public String generateGraphVizLabel() {
        String result = "";
        for (VisibilityConstraint vc : visibilityConstraints) {
            result += "\n\t" + vc;
        }
        result += "\n";
        if (this.xpath != null) {
            result += this.xpath;
        }
        for (WidthConstraint c : widthConstraints) {
            result += "\n" + c.min + " -> " + c.max + " : " + c.percentage + "% of parent + " + c.adjustment;
        }
        return result;
    }

    public void addWidthConstraint(WidthConstraint wc) {
        widthConstraints.add(wc);
    }

    public void addVisibilityConstraint(VisibilityConstraint vc) { visibilityConstraints.add(vc); }

    public void addParentConstraint(AlignmentConstraint ac) { parentConstraints.add(ac); }
}
