package twalsh.rlg;
import com.rits.cloning.Cloner;

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
//        ArrayList<AlignmentConstraint> ac1
    }
}
