package twalsh.rlg;
import org.openqa.selenium.WebDriver;
import twalsh.redecheck.Redecheck;
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
    Map<Integer, DomNode> doms;
    Map<Integer, DomNode> tempDoms;
    int[] widths;
    int[] restOfWidths;
    public static WebDriver driver;

    public ResponsiveLayoutGraph(ArrayList<AlignmentGraph> ags, int[] stringWidths, String url, Map<Integer, DomNode> doms, WebDriver driver) throws InterruptedException {
        this.graphs = ags;
        this.first = ags.get(0);
        this.driver = driver;
        this.driver.quit();
        restOfGraphs =  new ArrayList<AlignmentGraph>();
        for (AlignmentGraph ag : graphs) {
            restOfGraphs.add(ag);
        }
        restOfGraphs.remove(0);
        this.url = url;
        this.doms = doms;
//        widths = new double[stringWidths.length];
        restOfWidths = new int[stringWidths.length-1];
        this.widths = stringWidths;
        for (int i = 0; i < stringWidths.length; i++) {
            int s = stringWidths[i];
//            int width = Integer.parseInt(s);
//            this.widths[i] = s;
            if (i > 0) {
                restOfWidths[i-1] = s;
            }
//            alreadyGathered.add(s);
        }
        System.out.println("Constructor called.");
        extractVisibilityConstraints();
        driver.quit();
    }

    private void extractVisibilityConstraints() throws InterruptedException {
        System.out.println("Extracting Visibility Constraints.");
        HashMap<String, VisibilityConstraint> visCons = new HashMap<>();
        ArrayList<AGNode> agnodes = (ArrayList<AGNode>) first.getVertices();
        HashMap<String, AGNode> previousMap = (HashMap<String, AGNode>) first.getVMap();

        for (AGNode node : agnodes) {
            // Add each node to overall set
            String xpath = node.getDomNode().getxPath();
            nodes.put(xpath, new Node(xpath));

            // Create visibility constraint for each one
            visCons.put(xpath, new VisibilityConstraint((int) widths[0], 0));
        }

        for (AlignmentGraph ag : restOfGraphs) {
            HashMap<String, AGNode> previousToMatch = (HashMap<String, AGNode>) previousMap.clone();
            HashMap<String, AGNode> temp = (HashMap<String, AGNode>) ag.getVMap();
            HashMap<String, AGNode> tempToMatch = (HashMap<String, AGNode>) temp.clone();

            for (String s : previousMap.keySet()) {
                if (ag.getVMap().get(s) != null) {
                    // Found a node match
                    previousToMatch.remove(s);
                    tempToMatch.remove(s);
                }
            }

            // Handle any disappearing elements
            for (String prevUM : previousToMatch.keySet()) {
                int disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag)+1]);
                System.out.println("Disappear point: " + disappearPoint);
                VisibilityConstraint vc = visCons.get(prevUM);
                vc.setDisappear(disappearPoint-1);
            }

            // Handle any appearing elements
            for (String currUM : tempToMatch.keySet()) {
                int appearPoint = findAppearPoint(currUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag)+1]);
                nodes.put(currUM, new Node(currUM));
                visCons.put(currUM, new VisibilityConstraint(appearPoint, 0));
            }
            // Update the previousMap variable to keep track of last set of nodes
            previousMap = (HashMap<String, AGNode>) ag.getVMap();
        }

        // Update visibility constraints of everything still visible
        AlignmentGraph last = restOfGraphs.get(restOfGraphs.size()-1);
        for (String stilVis : last.getVMap().keySet()) {
            VisibilityConstraint vc = visCons.get(stilVis);
            if (vc.getDisappear() == 0) {
                vc.setDisappear(widths[widths.length-1]);
            }
        }

        printVisibilityConstraints(this.nodes, visCons);
    }

    public void printVisibilityConstraints(HashMap<String, Node> nodes, HashMap<String, VisibilityConstraint> visCons) {
        for (String s : nodes.keySet()) {
            VisibilityConstraint vc = visCons.get(s);
            System.out.println (s + "    " + vc);
        }

    }

    public int findAppearPoint(String xpath, int min, int max) throws InterruptedException {
//        System.out.println(min + "    " + max);
        if (max-min==1) {
            int[] extraWidths = new int[] {min,max};
            ArrayList<AlignmentGraph> extraGraphs = new ArrayList<AlignmentGraph>();
//            Redecheck.capturePageModel(url, extraWidths);
            tempDoms = Redecheck.loadDoms(extraWidths, url);

            for (int w : extraWidths) {
                DomNode dn = tempDoms.get(w);
                AlignmentGraph ag = new AlignmentGraph(dn);
                extraGraphs.add(ag);
            }
            AlignmentGraph ag1 = extraGraphs.get(0);
            AlignmentGraph ag2 = extraGraphs.get(1);
            HashMap<String, AGNode> n1 = (HashMap<String, AGNode>) ag1.getVMap();
            HashMap<String, AGNode> n2 = (HashMap<String, AGNode>) ag2.getVMap();

            boolean found1 = n1.get(xpath) != null;
            boolean found2 = n2.get(xpath) != null;
            if (found1) {
              return min;
            } else if (!found1 && found2) {
                return max;
            } else {
                return max+1;
            }
        } else {
            int mid = (max+min)/2;
            int[] extraWidths = new int[] {mid};
//            Redecheck.capturePageModel(url, extraWidths);
            tempDoms = Redecheck.loadDoms(extraWidths, url);
            DomNode dn = tempDoms.get(mid);

            AlignmentGraph extraAG = new AlignmentGraph(dn);

            HashMap<String, AGNode> n1 = (HashMap<String, AGNode>) extraAG.getVMap();
//            System.out.println(n1.entrySet().size());
            boolean found = n1.get(xpath) != null;
            if (found) {
                return findAppearPoint(xpath, min, mid);
            } else {
                return findAppearPoint(xpath, mid, max);
            }
        }
    }

    public int findDisappearPoint(String xpath, int min, int max) throws InterruptedException {
//        System.out.println(min + "    " + max);
        if (max-min==1) {
            int[] extraWidths = new int[] {min,max};
            ArrayList<AlignmentGraph> extraGraphs = new ArrayList<AlignmentGraph>();
//            Redecheck.capturePageModel(url, extraWidths);
            tempDoms = Redecheck.loadDoms(extraWidths, url);

            for (int w : extraWidths) {
                DomNode dn = tempDoms.get(w);
                AlignmentGraph ag = new AlignmentGraph(dn);
                extraGraphs.add(ag);
            }
            AlignmentGraph ag1 = extraGraphs.get(0);
            AlignmentGraph ag2 = extraGraphs.get(1);
            HashMap<String, AGNode> n1 = (HashMap<String, AGNode>) ag1.getVMap();
            HashMap<String, AGNode> n2 = (HashMap<String, AGNode>) ag2.getVMap();

            boolean found1 = n1.get(xpath) != null;
            boolean found2 = n2.get(xpath) != null;

            if (found1 && found2) {
                return max+1;
            } else if (found1 && !found2) {
                return max;
            } else {
                return min;
            }
        } else {
            int mid = (max+min)/2;
            int[] extraWidths = new int[] {mid};
//            Redecheck.capturePageModel(url, extraWidths);
            tempDoms = Redecheck.loadDoms(extraWidths, url);
            DomNode dn = tempDoms.get(extraWidths[0]);

            AlignmentGraph extraAG = new AlignmentGraph(dn);

            HashMap<String, AGNode> n1 = (HashMap<String, AGNode>) extraAG.getVMap();
            boolean found = n1.get(xpath) != null;
            if (found) {
                return findDisappearPoint(xpath, mid, max);
            } else {
                return findDisappearPoint(xpath, min, mid);
            }
        }
    }
}
