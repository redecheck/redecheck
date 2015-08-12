package twalsh.rlg;
import org.openqa.selenium.WebDriver;
import twalsh.redecheck.Redecheck;
import xpert.ag.AGNode;
import xpert.ag.AlignmentGraph;
import xpert.ag.Contains;
import xpert.ag.Edge;
import xpert.dom.DomNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
//        extractAlignmentConstraints();
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
                if (temp.get(s) != null) {
                    // Found a node match
                    previousToMatch.remove(s);
                    tempToMatch.remove(s);
                }
            }

            // Handle any disappearing elements
            for (String prevUM : previousToMatch.keySet()) {
                int disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag)+1], true);
//                System.out.println("Disappear point: " + disappearPoint-1);
                VisibilityConstraint vc = visCons.get(prevUM);
                vc.setDisappear(disappearPoint-1);
            }

            // Handle any appearing elements
            for (String currUM : tempToMatch.keySet()) {
                int appearPoint = findAppearPoint(currUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag)+1], true);
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

    private void extractAlignmentConstraints() throws InterruptedException {
        System.out.println("Extracting Alignment Constraints");
        HashMap<String, Edge> previousMap = first.getNewEdges();
        HashMap<ParentChild, String> pcs = new HashMap<ParentChild, String>();
        HashMap<Sibling, String> sibs = new HashMap<Sibling, String>();
        HashMap<String, AlignmentConstraint> alCons = new HashMap<String, AlignmentConstraint>();

        // Add initial edges to set.
        for (String s : previousMap.keySet()) {
            Edge e = previousMap.get(s);
            if (e instanceof Contains) {
                pcs.put(new ParentChild(this.nodes.get(e.getNode1().getxPath()), this.nodes.get(e.getNode2().getxPath())), "");
                AlignmentConstraint con = new AlignmentConstraint(this.nodes.get(e.getNode1().getxPath()), this.nodes.get(e.getNode2().getxPath()), Type.PARENT_CHILD, this.widths[0], 0);
                alCons.put(con.generateKey(), con);
            } else {
                sibs.put(new Sibling(this.nodes.get(e.getNode1().getxPath()), this.nodes.get(e.getNode2().getxPath())),"");
                AlignmentConstraint con = new AlignmentConstraint(this.nodes.get(e.getNode1().getxPath()), this.nodes.get(e.getNode2().getxPath()), Type.SIBLING, this.widths[0], 0);
                alCons.put(con.generateKey(), con);}
        }

        for (AlignmentGraph ag : restOfGraphs) {
            HashMap<String, Edge> previousToMatch = (HashMap<String, Edge>) previousMap.clone();
            HashMap<String, Edge> temp = ag.getNewEdges();
            HashMap<String, Edge> tempToMatch = (HashMap<String, Edge>) temp.clone();
            String key = "", key2 = "";
            for (String s : previousMap.keySet()) {
                Edge e = previousMap.get(s);
                key = e.getNode1().getxPath() + e.getNode2().getxPath();
                key2 = e.getNode2().getxPath() + e.getNode1().getxPath();
                if (e instanceof Contains) {
                    key += "contains";
                } else {
                    key += "sibling";
                    key2 += "sibling";
                }
                if (temp.get(key) != null || temp.get(key2) != null) {
                    previousToMatch.remove(key);
                    tempToMatch.remove(key);
                    previousToMatch.remove(key2);
                    tempToMatch.remove(key2);
                }
            }

            // Handle disappearing edges
            for (String prevUM : previousToMatch.keySet()) {
                Edge e = previousToMatch.get(prevUM);
                int disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag)+1], false);
                AlignmentConstraint ac = alCons.get(prevUM);
                ac.setMax(disappearPoint-1);
            }
            System.out.println();
            System.out.println(restOfWidths[restOfGraphs.indexOf(ag)]);
            System.out.println("Appearing: " + tempToMatch.size());
            System.out.println("Disappearing: " + previousToMatch.size());
            previousMap = temp;
        }

//        for (AlignmentConstraint ac : alCons) {
//            System.out.println(ac);
//        }

    }

    public void printVisibilityConstraints(HashMap<String, Node> nodes, HashMap<String, VisibilityConstraint> visCons) {
        for (String s : nodes.keySet()) {
            VisibilityConstraint vc = visCons.get(s);
            System.out.println (s + "    " + vc);
        }

    }

    public int findAppearPoint(String xpath, int min, int max, boolean searchForNode) throws InterruptedException {
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
                return findAppearPoint(xpath, min, mid, searchForNode);
            } else {
                return findAppearPoint(xpath, mid, max, searchForNode);
            }
        }
    }

    public int findDisappearPoint(String searchKey, int min, int max, boolean searchForNode) throws InterruptedException {
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
            boolean found1=false,found2 = false;

            if (searchForNode) {
                HashMap<String, AGNode> n1 = (HashMap<String, AGNode>) ag1.getVMap();
                HashMap<String, AGNode> n2 = (HashMap<String, AGNode>) ag2.getVMap();

                found1 = n1.get(searchKey) != null;
                found2 = n2.get(searchKey) != null;
            } else {
                HashMap<String, Edge> e1 = (HashMap<String, Edge>) ag1.getNewEdges();
                HashMap<String, Edge> e2 = (HashMap<String, Edge>) ag2.getNewEdges();

                found1 = e1.get(searchKey) != null;
                found2 = e2.get(searchKey) != null;
            }

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
            boolean found = n1.get(searchKey) != null;
            if (found) {
                return findDisappearPoint(searchKey, mid, max, searchForNode);
            } else {
                return findDisappearPoint(searchKey, min, mid, searchForNode);
            }
        }
    }
}
