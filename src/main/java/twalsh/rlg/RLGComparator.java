package twalsh.rlg;
import com.rits.cloning.Cloner;
import xpert.ag.*;
import xpert.ag.Sibling;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thomaswalsh on 20/08/15.
 */
public class RLGComparator {
    ResponsiveLayoutGraph rlg1,rlg2;
    public HashMap<Node, Node> matchedNodes;
    Cloner cloner;

    public RLGComparator(ResponsiveLayoutGraph r1, ResponsiveLayoutGraph r2) {
        rlg1 = r1;
        rlg2 = r2;
    }

    public void compare() {
        matchedNodes = new HashMap<Node, Node>();
        cloner = new Cloner();
        matchNodes();
    }

    public ArrayList<String> compareMatchedNodes() {
        ArrayList<String> issues = new ArrayList<String>();

        for (Node n : matchedNodes.keySet()) {
            Node m = matchedNodes.get(n);
            compareVisibilityConstraints(n,m,issues);
//            compareAlignmentConstraints(n, m, issues);
//            compareWidthConstraints(n,m,issues);
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
//		System.out.println("Number of matches nodes : " + matchedNodes.size());
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
            i.add("Unmatched Visibility Constraint : (" + a.appear +" , "+a.disappear + ") compared to (" + b.appear +" , " + b.disappear + ")");
        }
    }

    public void compareAlignmentConstraints(Node n, Node m, ArrayList<String> i) {
        ArrayList<AlignmentConstraint> ac1 = new ArrayList<AlignmentConstraint>(), ac2 = new ArrayList<AlignmentConstraint>();

        // Get all the alignment constraints for the matched nodes from the two graphs
        for (AlignmentConstraint a : rlg1.alignments.values()) {
//            if (a.type == Type.SIBLING) {
                if (a.node1.xpath.equals(n.getXpath())) {
                    ac1.add(a);
                }
//            }
        }

        for (AlignmentConstraint b : rlg2.alignments.values()) {
//            if (b.type == Type.SIBLING) {
                if (b.node1.xpath.equals(m.getXpath())) {
                    ac2.add(b);
                }
//            }
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

        // Add any unmatched edges to issues list
        for (AlignmentConstraint a : unmatched1) {
            i.add("Unmatched alignment constraint from oracle: " + a);
        }
        for (AlignmentConstraint a : unmatched2) {
            i.add("Unmatched alignment constraint from test: " + a);
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
}
