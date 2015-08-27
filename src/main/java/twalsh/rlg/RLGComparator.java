package twalsh.rlg;
import com.rits.cloning.Cloner;
import twalsh.reporting.*;
import twalsh.reporting.Error;
import xpert.ag.*;
import xpert.ag.Sibling;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.PrintWriter;
import org.apache.commons.io.FileUtils;
import java.io.File;

/**
 * Created by thomaswalsh on 20/08/15.
 */
public class RLGComparator {
    ResponsiveLayoutGraph rlg1,rlg2;
    public HashMap<Node, Node> matchedNodes;
    Cloner cloner;
    public ArrayList<String> issues;
    public static ArrayList<Error> errors;

    public RLGComparator(ResponsiveLayoutGraph r1, ResponsiveLayoutGraph r2) {
        rlg1 = r1;
        rlg2 = r2;
        issues = new ArrayList<String>();
        errors = new ArrayList<Error>();
    }

    public void compare() {
        matchedNodes = new HashMap<Node, Node>();
        cloner = new Cloner();
        matchNodes();
    }

    public ArrayList<String> compareMatchedNodes() {
        for (Node n : matchedNodes.keySet()) {
            Node m = matchedNodes.get(n);
            compareVisibilityConstraints(n, m, issues);
            compareAlignmentConstraints(n, m, issues);
            compareWidthConstraints(n, m, issues);
        }
        return issues;
    }

    public void matchNodes() {

        HashMap<String, Node> nodes1 = cloner.deepClone(rlg1.nodes);
        HashMap<String, Node> nodes2 = cloner.deepClone(rlg2.nodes);

        // Match the nodes and their min/max values
        for (Node n1 : rlg1.nodes.values()) {
            String xpath1 = n1.xpath;
            for (Node n2 : rlg2.nodes.values()) {
                String xpath2 = n2.xpath;
                if (xpath1.equals(xpath2)) {
                    matchedNodes.put(n1, n2);
                    nodes1.remove(xpath1);
                    nodes2.remove(xpath2);
                }
            }
        }
        for (Node left1 : nodes1.values()) {
            System.out.println(left1.xpath + " wasn't matched in Graph 2");
        }
        for (Node left2 : nodes2.values()) {
            System.out.println(left2.xpath + " wasn't matched in Graph 1");
        }
    }

    public void compareVisibilityConstraints(Node n, Node m, ArrayList<String> i) {
        VisibilityConstraint a = n.getVisibilityConstraints().get(0);
        VisibilityConstraint b = m.getVisibilityConstraints().get(0);
        if ((a.appear != b.appear) || (a.disappear != b.disappear)) {
            VisibilityError ve = new VisibilityError(n, m);
            errors.add(ve);
            i.add("Unmatched Visibility Constraint : (" + a.appear +" , "+a.disappear + ") compared to (" + b.appear +" , " + b.disappear + ")");
        }
    }

    public void compareAlignmentConstraints(Node n, Node m, ArrayList<String> i) {
        ArrayList<AlignmentConstraint> ac1 = new ArrayList<AlignmentConstraint>(), ac2 = new ArrayList<AlignmentConstraint>();

        // Get all the alignment constraints for the matched nodes from the two graphs
        for (AlignmentConstraint a : rlg1.alignments.values()) {
            if (a.node1.xpath.equals(n.getXpath())) {
                ac1.add(a);
            }
        }

        for (AlignmentConstraint b : rlg2.alignments.values()) {
            if (b.node1.xpath.equals(m.getXpath())) {
                ac2.add(b);
            }
        }

        HashMap<AlignmentConstraint, AlignmentConstraint> matched = new HashMap<AlignmentConstraint, AlignmentConstraint>();
        ArrayList<AlignmentConstraint> unmatched1 = new ArrayList<AlignmentConstraint>(), unmatched2 = new ArrayList<AlignmentConstraint>();
        while (ac1.size() > 0) {
            AlignmentConstraint ac = ac1.remove(0);
            AlignmentConstraint match = null;
            for (AlignmentConstraint temp : ac2) {
                if ( (temp.node1.xpath.equals(ac.node1.xpath)) && (temp.node2.xpath.equals(ac.node2.xpath)) && (temp.min == ac.min) && (temp.max == ac.max) ) {
                    match = temp;
                    break;
                }
            }
            if (match != null) {
                matched.put(ac, match);
                ac2.remove(match);
            } else {
                unmatched1.add(ac);
            }
        }

        for (AlignmentConstraint acUM : ac2) {
            unmatched2.add(acUM);
        }

        if ( (unmatched1.size() > 0) || (unmatched2.size() > 0) ) {
            AlignmentError ae = new AlignmentError(unmatched1, unmatched2);
            errors.add(ae);
        }

        // Add any unmatched edges to issues list
        for (AlignmentConstraint a : unmatched1) {
            i.add("Unmatched alignment constraint from oracle: " + a);
        }
        for (AlignmentConstraint b : unmatched2) {
            i.add("Unmatched alignment constraint from test: " + b);
        }

        // Check alignments are correct
        for (AlignmentConstraint ac : matched.keySet()) {
            AlignmentConstraint match = matched.get(ac);
            if (match != null) {
                if (ac.type == Type.PARENT_CHILD) {
                    if (ac.attributes[0] != match.attributes[0])
                        i.add("Error with centre justification : " + ac);
                    if (ac.attributes[1] != match.attributes[1])
                        i.add("Error with left justification : " + ac);
                    if (ac.attributes[2] != match.attributes[2])
                        i.add("Error with right justification : " + ac);
                    if(ac.attributes[3] != match.attributes[3])
                        i.add("Error with middle justification : " + ac);
                    if (ac.attributes[4] != match.attributes[4])
                        i.add("Error with top justification : " + ac);
                    if (ac.attributes[5] != match.attributes[5])
                        i.add("Error with bottom justification : " + ac);
                } else {
                    if (ac.attributes[0] != match.attributes[0]) {
                        i.add("Error with below alignment : " + ac);
                    }
                    if (ac.attributes[1] != match.attributes[1]) {
                        i.add("Error with above alignment : " + ac);
                    }
                    if (ac.attributes[2] != match.attributes[2]) {
                        i.add("Error with left-of alignment : " + ac);
                    }
                    if (ac.attributes[3] != match.attributes[3]) {
                        i.add("Error with right-of alignment : " + ac);
                    }
                    if (ac.attributes[4] != match.attributes[4]) {
                        i.add("Error with top-edge alignment : " + ac);
                    }
                    if (ac.attributes[5] != match.attributes[5]) {
                        i.add("Error with bottom-edge alignment : " + ac);
                    }
                    if (ac.attributes[6] != match.attributes[6]) {
                        i.add("Error with left-edge alignment : " + ac);
                    }
                    if (ac.attributes[7] != match.attributes[7]) {
                        i.add("Error with right-edge alignment : " + ac);
                    }
                }
            }
        }

    }

    public void compareWidthConstraints(Node n, Node m, ArrayList<String> i) {
        ArrayList<WidthConstraint> wc1 = new ArrayList<WidthConstraint>(), wc2 = new ArrayList<WidthConstraint>();

        for (WidthConstraint w : n.getWidthConstraints()) {
            wc1.add(w);
        }
        for (WidthConstraint w : m.getWidthConstraints()) {
            wc2.add(w);
        }

        HashMap<WidthConstraint, WidthConstraint> matchedConstraints = new HashMap<WidthConstraint, WidthConstraint>();
        ArrayList<WidthConstraint> unmatch1 = new ArrayList<WidthConstraint>();
        ArrayList<WidthConstraint> unmatch2 = new ArrayList<WidthConstraint>();

        while (wc1.size() > 0) {
            WidthConstraint wc = wc1.remove(0);
            WidthConstraint match = null;
            for (WidthConstraint temp : wc2) {
                if ( (wc.percentage == temp.percentage) && (wc.adjustment == temp.adjustment) && (wc.min == temp.min) && (wc.max == temp.max)) {
                    match = temp;
                    break;
                }
            }

            // Update the sets
            if (match != null) {
                matchedConstraints.put(wc, match);
                wc2.remove(match);
            } else {
                unmatch1.add(wc);
            }
        }
        for (WidthConstraint c : wc2) {
            unmatch2.add(c);
        }

        if ( (unmatch1.size() > 0) || (unmatch2.size() > 0) ) {
            WidthError we = new WidthError(n, unmatch1, unmatch2);
            errors.add(we);
        }

        for (WidthConstraint c : unmatch1) {
            i.add("Unmatched constraint in graph 1: " + c);
        }
        for (WidthConstraint d : unmatch2) {
            i.add("Unmatched constraint in graph 2: " + d);
        }
    }

    public void writeRLGDiffToFile(String folder, String fileName, String baseUrl, ArrayList<String> rlgIssues) {
        PrintWriter output = null;
        String outFolder = "";
        try {
            outFolder = baseUrl.replace("file://","") + folder;
            FileUtils.forceMkdir(new File(outFolder));
            output = new PrintWriter(outFolder + fileName + ".txt");

            // Print out visibility errors
            output.append("====== Visibility Errors ====== \n\n");
            for (Error e : errors) {
                if (e instanceof VisibilityError) {
                    output.append(e.toString());
                }
            }

            // Print out alignment errors
            output.append("====== Alignment Errors ====== \n\n");
            for (Error e : errors) {
                if (e instanceof AlignmentError) {
                    output.append(e.toString());
                }
            }

            // Print out width errors
            output.append("====== Width Errors ====== \n\n");
            for (Error e : errors) {
                if (e instanceof WidthError) {
                    output.append(e.toString());
                }
            }

            output.close();
            Desktop d = Desktop.getDesktop();
            d.open(new File(outFolder + fileName + ".txt"));
        } catch (IOException e) {
            System.out.println("Failed to write the results to file.");
        }
    }
}
