package twalsh.rlg;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by thomaswalsh on 10/08/15.
 * Last modified on 08/09/15
 */
public class Node {

    // Instance variables
    String xpath;
    String label;
    ArrayList<WidthConstraint> widthConstraints;
    ArrayList<VisibilityConstraint> visibilityConstraints;
    ArrayList<AlignmentConstraint> parentConstraints;

    /**
     * Constructor for the Node object, which initializes all the required instance variables
     * @param xpath     the unique XPath of the element represented by the node
     */
    public Node(String xpath) {
        this.xpath = xpath;
        this.widthConstraints = new ArrayList<WidthConstraint>();
        this.visibilityConstraints = new ArrayList<VisibilityConstraint>();
        this.parentConstraints = new ArrayList<AlignmentConstraint>();
    }

    /**
     * Getter for the XPath variable
     * @return      the XPath of the node
     */
    public String getXpath() {
        try {
            return this.xpath;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return "tester";
        }
    }

    /**
     * Returns the width constraints currently linked to the node.
     * @return      An ArrayList of WidthConstraint objects
     */
    public ArrayList<WidthConstraint> getWidthConstraints() {
        return widthConstraints;
    }

    /**
     * Returns the visibility constraints currently linked to the node.
     * @return      An ArrayList of VisibilityConstraint objects
     */
    public ArrayList<VisibilityConstraint> getVisibilityConstraints() { return visibilityConstraints; }

    /**
     * Returns the parent-child alignment constraints for which the node is the child currently linked to the node.
     * @return      An ArrayList of AlignmentConstraint objects
     */
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

    /**
     * Generates an informative string displaying the node's XPath and width constraints, which can then be printed
     * @return      the formatted string
     */
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

    /**
     * Generates a string for printing the node's information into a GraphViz node, to illustrate the node visually.
     * @return      the formatted GraphViz string
     */
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

    /**
     * Adds a width constraint to the node
     * @param wc    the width constraint to add
     */
    public void addWidthConstraint(WidthConstraint wc) {
        widthConstraints.add(wc);
    }

    /**
     * Adds a visibility constraint to the node
     * @param vc    the visibility constraint to add
     */
    public void addVisibilityConstraint(VisibilityConstraint vc) { visibilityConstraints.add(vc); }

    /**
     * Adds an alignment constraint to the node
     * @param ac    the alignment constraint to add
     */
    public void addParentConstraint(AlignmentConstraint ac) { parentConstraints.add(ac); }
}
