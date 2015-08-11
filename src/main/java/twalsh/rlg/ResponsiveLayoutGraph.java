package twalsh.rlg;
import xpert.ag.AGNode;
import xpert.ag.AlignmentGraph;
import xpert.dom.DomNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.rits.cloning.Cloner;

/**
 * Created by thomaswalsh on 10/08/15.
 */
public class ResponsiveLayoutGraph {
    HashMap<String, Node> nodes = new HashMap<String, Node>();
    ArrayList<AlignmentGraph> graphs;
    AlignmentGraph first;
    ArrayList<AlignmentGraph> restOfGraphs;
    String url;
    Map<String, DomNode> doms;
    double[] widths;
    double[] restOfWidths;

    public ResponsiveLayoutGraph(ArrayList<AlignmentGraph> ags, String[] stringWidths, String url, Map<String, DomNode> doms) {
        this.graphs = ags;
        this.first = ags.get(0);
        restOfGraphs =  new ArrayList<AlignmentGraph>();
        for (AlignmentGraph ag : graphs) {
            restOfGraphs.add(ag);
        }
        restOfGraphs.remove(0);
        this.url = url;
        this.doms = doms;
        widths = new double[stringWidths.length];
        restOfWidths = new double[stringWidths.length-1];
        for (int i = 0; i < stringWidths.length; i++) {
            String s = stringWidths[i];
            int width = Integer.parseInt(s);
            widths[i] = width;
            if (i > 0) {
                restOfWidths[i-1] = width;
            }
//            alreadyGathered.add(s);
        }
        System.out.println("Constructor called.");
        extractVisibilityConstraints();
    }

    private void extractVisibilityConstraints() {
        System.out.println("Extracting Visibility Constraints.");
        HashMap<String, VisibilityConstraint> visCons = new HashMap<>();
        ArrayList<AGNode> agnodes = (ArrayList<AGNode>) first.getVertices();
        ArrayList<AGNode> previousSet = (ArrayList<AGNode>) agnodes.clone();
        Cloner cloner = new Cloner();
        HashMap<String, AGNode> previousMap = (HashMap<String, AGNode>) first.getVMap();



        for (AGNode node : agnodes) {
            // Add each node to overall set
            String xpath = node.getDomNode().getxPath();
            nodes.put(xpath, new Node(xpath));

            // Create visibility constraint for each one
            visCons.put(xpath, new VisibilityConstraint((int) widths[0], 0));
        }

        for (AlignmentGraph ag : restOfGraphs) {
            HashMap<String, AGNode> previousToMatch = (HashMap<String, AGNode>) cloner.deepClone(previousMap);
            HashMap<String, AGNode> tempToMatch = (HashMap<String, AGNode>) cloner.deepClone(ag.getVMap());

            for (String s : previousMap.keySet()) {
                for (String s2 : ag.getVMap().keySet()) {
                    if (s.equals(s2)) {
                        // Found a node match
                        previousToMatch.remove(s);
                        tempToMatch.remove(s2);
                    }
                }
            }

            // Handle any disappearing elements
            for (String prevUM : previousToMatch.keySet()) {
                int disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag)+1]);
                VisibilityConstraint vc = visCons.get(prevUM);
                vc.setDisappear(disappearPoint);
            }

            // Handle any appearing elements
            for (String currUM : tempToMatch.keySet()) {
                int appearPoint = findAppearPoint(currUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag)+1]);
                nodes.put(currUM, new Node(currUM));
                visCons.put(currUM, new VisibilityConstraint(appearPoint, 0));

            }
            System.out.println("Number unmatched from previous : " + previousToMatch.entrySet().size());
            System.out.println("Number unmatched from current : " + tempToMatch.entrySet().size());
        }
    }

    public int findAppearPoint(String xpath, double min, double max) {
        return 0;
    }

    public int findDisappearPoint(String xpath, double min, double max) {
        return 0;
    }
}
