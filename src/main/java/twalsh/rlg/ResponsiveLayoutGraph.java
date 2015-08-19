package twalsh.rlg;
import com.google.common.collect.HashBasedTable;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.openqa.selenium.WebDriver;
import sun.java2d.pipe.SpanShapeRenderer;
import twalsh.redecheck.Redecheck;
import xpert.ag.*;
import xpert.ag.Sibling;
import xpert.dom.DomNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

import twalsh.redecheck.Utils;

/**
 * Created by thomaswalsh on 10/08/15.
 */
public class ResponsiveLayoutGraph {
    HashMap<String, Node> nodes = new HashMap<String, Node>();
    HashMap<String, AlignmentConstraint> alignments = new HashMap<String, AlignmentConstraint>();
    HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints = HashBasedTable.create();
    HashBasedTable<String, int[], WidthConstraint> widthConstraints = HashBasedTable.create();
    ArrayList<AlignmentGraph> graphs;
    AlignmentGraph first;
    ArrayList<AlignmentGraph> restOfGraphs;
    String url;
    Map<Integer, DomNode> doms;
    Map<Integer, DomNode> tempDoms;
    int[] widths;
    int[] restOfWidths;
    static HashSet<Integer> alreadyGathered;
    public static WebDriver driver;

    public ResponsiveLayoutGraph(ArrayList<AlignmentGraph> ags, int[] stringWidths, String url, Map<Integer, DomNode> doms, WebDriver driver) throws InterruptedException {
        this.graphs = ags;
        this.first = ags.get(0);
//        this.driver = driver;
//        this.driver.quit();
        restOfGraphs =  new ArrayList<AlignmentGraph>();
        for (AlignmentGraph ag : graphs) {
            restOfGraphs.add(ag);
        }
        restOfGraphs.remove(0);
        this.url = url;
        this.doms = doms;
        alreadyGathered = new HashSet<Integer>();
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
            alreadyGathered.add(s);
        }
        System.out.println(alreadyGathered.size());
        System.out.println("Constructor called.");
        extractVisibilityConstraints();
//        printVisibilityConstraints(this.nodes);
        System.out.println("DONE VISIBILITY CONSTRAINTS");
        extractAlignmentConstraints();
        System.out.println("DONE ALIGNMENT CONSTRAINTS");
        extractWidthConstraints();
//        printAlignmentConstraints(this.alignmentConstraints.values());
        writetoGraphViz("test", false);
//        driver.quit();
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
                int disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], true, "");
                VisibilityConstraint vc = visCons.get(prevUM);
                vc.setDisappear(disappearPoint-1);
            }

            // Handle any appearing elements
            for (String currUM : tempToMatch.keySet()) {
                int appearPoint = findAppearPoint(currUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], true, "");
                nodes.put(currUM, new Node(currUM));
                visCons.put(currUM, new VisibilityConstraint(appearPoint, 0));
            }
            // Update the previousMap variable to keep track of last set of nodes
            previousMap = (HashMap<String, AGNode>) ag.getVMap();
        }

        // Update visibility widthConstraints of everything still visible
        AlignmentGraph last = restOfGraphs.get(restOfGraphs.size()-1);
        for (String stilVis : last.getVMap().keySet()) {
            VisibilityConstraint vc = visCons.get(stilVis);
            if (vc.getDisappear() == 0) {
                vc.setDisappear(widths[widths.length - 1]);
            }
        }

        // Attach constraints to the nodes
        for (String x : this.nodes.keySet()) {
            Node n = this.nodes.get(x);
            VisibilityConstraint vc = visCons.get(x);
            n.addVisibilityConstraint(vc);
        }
    }

    private void extractAlignmentConstraints() throws InterruptedException {
        System.out.println("Extracting Alignment Constraints");
        HashMap<String, Edge> previousMap = first.getNewEdges();
        HashMap<String, AlignmentConstraint> alCons = new HashMap<String, AlignmentConstraint>();

        // Add initial edges to set.
        for (String s : previousMap.keySet()) {
            Edge e = previousMap.get(s);
            if (e instanceof Contains) {
                Contains c = (Contains) e;
                AlignmentConstraint con = new AlignmentConstraint(this.nodes.get(e.getNode2().getxPath()), this.nodes.get(e.getNode1().getxPath()), Type.PARENT_CHILD, this.widths[0], 0,
                        new boolean[] {c.isCentered(), c.isLeftJustified(),c.isRightJustified(),c.isMiddle(),c.isTopAligned(),c.isBottomAligned()});
                alCons.put(con.generateKey(), con);
                alignmentConstraints.put(con.generateKey(), new int[]{this.widths[0],0}, con);
            }
            else {
                xpert.ag.Sibling s2 = (xpert.ag.Sibling) e;
                AlignmentConstraint con = new AlignmentConstraint(this.nodes.get(e.getNode1().getxPath()), this.nodes.get(e.getNode2().getxPath()), Type.SIBLING, this.widths[0], 0,
                        new boolean[] {s2.isTopBottom(),s2.isBottomTop(),s2.isRightLeft(),s2.isLeftRight(), s2.isTopEdgeAligned(),s2.isBottomEdgeAligned(),s2.isLeftEdgeAligned(), s2.isRightEdgeAligned()});
                alCons.put(con.generateKey(), con);
                alignmentConstraints.put(con.generateKey(), new int[] {this.widths[0],0}, con);
            }
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
                    Contains cTemp = (Contains) e;
                    key += "contains" +cTemp.generateLabelling();
                } else {
                    xpert.ag.Sibling sTemp = (xpert.ag.Sibling) e;
                    key += "sibling" + sTemp.generateLabelling();
                    key2 += "sibling" + sTemp.generateFlippedLabelling();
                }
                if (temp.get(key) != null || temp.get(key2) != null) {
                    boolean matched = false;
                    if (e instanceof Contains) {
                        Contains c1 = (Contains) e;
                        Contains c2 = (Contains) temp.get(key);
                        matched = c1.isAlignmentTheSame(c2);
                    } else {
                        Sibling s1 = (Sibling) e;
                        Sibling s2;
                        if (temp.get(key) != null) {
                            s2 = (Sibling) temp.get(key);
                        } else {
                            s2 = (Sibling) temp.get(key2);
                        }
                        matched = s1.isAlignmentTheSame(s2);
                    }
                    if (matched) {
                        previousToMatch.remove(key);
                        tempToMatch.remove(key);
                        previousToMatch.remove(key2);
                        tempToMatch.remove(key2);
                    }
                }
            }

            // Handle disappearing edges
            for (String prevUM : previousToMatch.keySet()) {
                Edge e = previousToMatch.get(prevUM);
                int disappearPoint = 0;
                if (e instanceof Contains) {
                    disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], false, "");
                } else {
                    xpert.ag.Sibling s2 = (xpert.ag.Sibling) e;
                    String flip = s2.getNode2().getxPath()+s2.getNode1().getxPath()+"sibling" +s2.generateFlippedLabelling();
                    disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], false, flip);
                }
                    Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(prevUM);
                    for (int[] pair : cons.keySet()) {
                        // Get the one without a max value
                        if (pair[1] == 0) {
//                            System.out.println(pair[0]);
                            AlignmentConstraint aCon = cons.get(pair);
                            aCon.setMax(disappearPoint - 1);
                            pair[1] = disappearPoint-1;
                        }
                    }
//                }
            }

            // Handle appearing edges
            for (String currUM : tempToMatch.keySet()) {
                Edge e = tempToMatch.get(currUM);
                int appearPoint = 0;
                Type t = null;
                AlignmentConstraint ac = null;
                if (e instanceof Contains) {
//                    System.out.println(currUM);
                    Contains c = (Contains) e;
                    appearPoint = findAppearPoint(currUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], false, "");
                    t = Type.PARENT_CHILD;
                    ac = new AlignmentConstraint(this.nodes.get(e.getNode2().getxPath()), this.nodes.get(e.getNode1().getxPath()), t, appearPoint, 0,
                            new boolean[]{c.isCentered(), c.isLeftJustified(), c.isRightJustified(), c.isMiddle(), c.isTopAligned(), c.isBottomAligned()});
                }
                else {
//                    System.out.println(currUM);
                    t = Type.SIBLING;
                    xpert.ag.Sibling s2 = (xpert.ag.Sibling) e;
                    String flip = s2.getNode2().getxPath()+s2.getNode1().getxPath()+"sibling" +s2.generateFlippedLabelling();
                    appearPoint = findAppearPoint(currUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], false, flip);
                    ac = new AlignmentConstraint(this.nodes.get(e.getNode1().getxPath()), this.nodes.get(e.getNode2().getxPath()), t, appearPoint, 0,
                            new boolean[]{s2.isTopBottom(),s2.isBottomTop(),s2.isRightLeft(),s2.isLeftRight(), s2.isTopEdgeAligned(),s2.isBottomEdgeAligned(),s2.isLeftEdgeAligned(), s2.isRightEdgeAligned()});

                }
                if (ac != null) {
                    alCons.put(ac.generateKey(), ac);
                    alignmentConstraints.put(ac.generateKey(), new int[]{appearPoint,0}, ac);
                }
            }
            previousMap = ag.getNewEdges();
        }



        // Update  alignment constraints of everything still visible
        AlignmentGraph last = restOfGraphs.get(restOfGraphs.size()-1);
        for (String stilVis : last.getNewEdges().keySet()) {
            Edge e = last.getNewEdges().get(stilVis);
            if (e instanceof Contains) {
                Contains cTemp = (Contains) e;
                AlignmentConstraint ac = alCons.get(stilVis);
                if (ac != null) {
                    Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(stilVis);
                    for (int[] pair : cons.keySet()) {
                        // Get the one without a max value
                        if (pair[1] == 0) {
                            AlignmentConstraint aCon = cons.get(pair);
                            aCon.setMax(widths[widths.length-1]);
                            pair[1] = widths[widths.length-1];
                        }
                    }
                }
            } else {
                String flipped = e.getNode2().getxPath() + e.getNode1().getxPath()+"sibling";

                Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(stilVis);
                Map<int[], AlignmentConstraint> cons2 = alignmentConstraints.row(flipped);
                if (cons.size() != 0) {
                    for (int[] pair : cons.keySet()) {
                        // Get the one without a max value
                        if (pair[1] == 0) {
                            AlignmentConstraint aCon = cons.get(pair);
                            aCon.setMax(widths[widths.length-1]);
                            pair[1] = widths[widths.length-1];
                        }
                    }
                } else if (cons2.size() != 0) {
                    for (int[] pair : cons2.keySet()) {
                        // Get the one without a max value
                        if (pair[1] == 0) {
                            AlignmentConstraint aCon = cons2.get(pair);
                            aCon.setMax(widths[widths.length - 1]);
                            pair[1] = widths[widths.length - 1];
                        }
                    }
                }
            }
        }
        addParentConstraintsToNodes();
        this.alignments = alCons;
    }

    public void extractWidthConstraints() {
        for (String s : this.nodes.keySet()) {
            Node n = this.nodes.get(s);
            if (n.parentConstraints.size() > 0) {
                if (areParentsConsistent(n.parentConstraints)) {
                    String parentXpath = n.parentConstraints.get(0).node1.xpath;
                    int[] validWidths = getValidWidths(n.parentConstraints);
                    int[] widthsTemp = new int[validWidths.length];
                    int[] parentWidths = new int[validWidths.length];
                    int[] childWidths = new int[validWidths.length];
                    doms = Redecheck.loadDoms(validWidths, url);
                    // Gather parent and child widths
                    for (int i = 0; i < validWidths.length; i++) {
                        AlignmentGraph ag = new AlignmentGraph(doms.get(validWidths[i]));
                        widthsTemp[i] = validWidths[i];
                        parentWidths[i] = ag.getVMap().get(parentXpath).getDomNode().getWidth();
                        childWidths[i] = ag.getVMap().get(s).getDomNode().getWidth();
                    }

                    // Get the equations
                    int iteratingIndex = 0;
                    TreeMap<Integer, double[]> equations = new TreeMap<Integer, double[]>();
                    while (iteratingIndex +2 <= parentWidths.length) {
                        double[] firstTwoWidths = new double[] { parentWidths[0], parentWidths[1]};
                        double[] firstTwoValues = new double[] { childWidths[0], childWidths[1]};
                        double[] equation = getEquationOfLine(firstTwoWidths, firstTwoValues);

                        for (int i = iteratingIndex+2; i < parentWidths.length; i++) {
                            double result = (equation[0]*parentWidths[i]) + (equation[1]*childWidths[i]) + equation[2];
                            if (Math.abs(result) > 5) {
                                double[] bestFit = getBestFitLine(parentWidths, childWidths, i);
//                                int breakpoint = findWidthBreakpoint(bestFit, validWidths[i-1], validWidths[i], s, parentXpath);
                            }
                        }

                        double[] tempWidths = new double[parentWidths.length];
                        double[] tempValues = new double[childWidths.length];
                        double[] tempScreenWidths = new double[widthsTemp.length];
                        System.arraycopy(parentWidths, 0, tempWidths, 0, parentWidths.length);
                        System.arraycopy(childWidths, 0, tempValues, 0, childWidths.length);
                        System.arraycopy(widthsTemp, 0, tempScreenWidths, 0, widthsTemp.length);
                    }

                }
            }
        }
    }

    public void printVisibilityConstraints(HashMap<String, Node> nodes) {
        for (String s : nodes.keySet()) {
            Node n = this.nodes.get(s);
            VisibilityConstraint vc = n.getVisibilityConstraints().get(0);
            System.out.println (s + "    " + vc);
        }

    }

    public void printAlignmentConstraints(HashBasedTable<String, int[], AlignmentConstraint> cons) {
        for (AlignmentConstraint ac : cons.values()) {
            System.out.println(ac);
        }
    }

    private void addParentConstraintsToNodes() {
        for (AlignmentConstraint ac : this.alignmentConstraints.values()) {
            if (ac.type == Type.PARENT_CHILD) {
                Node child = this.nodes.get(ac.node2.getXpath());
                child.addParentConstraint(ac);
            }
        }
    }

    public int findAppearPoint(String searchKey, int min, int max, boolean searchForNode, String flippedKey) throws InterruptedException {
//        System.out.println(min + "    " + max);
        if (max-min==1) {
            int[] extraWidths = new int[] {min,max};
            ArrayList<AlignmentGraph> extraGraphs = new ArrayList<AlignmentGraph>();
            if ( (!alreadyGathered.contains(min)) || (!alreadyGathered.contains(max)) ) {
//                Redecheck.capturePageModel(url, extraWidths);
//                System.out.println(min + " or " + max);
                alreadyGathered.add(min);
                alreadyGathered.add(max);
            }
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

                // Searching for parent-child edge
            } else {
                HashMap<String, Edge> e1 = (HashMap<String, Edge>) ag1.getNewEdges();
                HashMap<String, Edge> e2 = (HashMap<String, Edge>) ag2.getNewEdges();

                found1 = (e1.get(searchKey) != null) || (e1.get(flippedKey) != null);
                found2 = (e2.get(searchKey) != null) || (e2.get(flippedKey) != null);
            }
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
            if (!alreadyGathered.contains(mid)) {
//                System.out.println(mid);
//                Redecheck.capturePageModel(url, extraWidths);
                alreadyGathered.add(mid);
            }
            tempDoms = Redecheck.loadDoms(extraWidths, url);
            DomNode dn = tempDoms.get(mid);

            AlignmentGraph extraAG = new AlignmentGraph(dn);
            boolean found = false;
            if (searchForNode) {
                HashMap<String, AGNode> n1 = (HashMap<String, AGNode>) extraAG.getVMap();
                found = n1.get(searchKey) != null;
            } else {
                HashMap<String, Edge> es = (HashMap<String, Edge>) extraAG.getNewEdges();
                found = (es.get(searchKey) != null) || (es.get(flippedKey) != null);
            }
            if (found) {
                return findAppearPoint(searchKey, min, mid, searchForNode, flippedKey);
            } else {
                return findAppearPoint(searchKey, mid, max, searchForNode, flippedKey);
            }
        }
    }

    public int findDisappearPoint(String searchKey, int min, int max, boolean searchForNode, String flippedKey) throws InterruptedException {
//        System.out.println(searchKey);
        if (max-min==1) {
            int[] extraWidths = new int[] {min,max};
            ArrayList<AlignmentGraph> extraGraphs = new ArrayList<AlignmentGraph>();
            if ( (!alreadyGathered.contains(min)) || (!alreadyGathered.contains(max)) ) {
//                System.out.println(min + " or " + max);
//                Redecheck.capturePageModel(url, extraWidths);
                alreadyGathered.add(min);
                alreadyGathered.add(max);
            }
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
                found1 = (e1.get(searchKey) != null) || (e1.get(flippedKey) != null);
                found2 = (e2.get(searchKey) != null) || (e2.get(flippedKey) != null);
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
            if (!alreadyGathered.contains(mid)) {
//                System.out.println(mid);
//                Redecheck.capturePageModel(url, extraWidths);
                alreadyGathered.add(mid);
            }
            tempDoms = Redecheck.loadDoms(extraWidths, url);
            DomNode dn = tempDoms.get(extraWidths[0]);

            AlignmentGraph extraAG = new AlignmentGraph(dn);
            boolean found;

            if (searchForNode) {
                HashMap<String, AGNode> n1 = (HashMap<String, AGNode>) extraAG.getVMap();
                found = n1.get(searchKey) != null;
            } else {
                HashMap<String, Edge> es = (HashMap<String, Edge>) extraAG.getNewEdges();
                found = (es.get(searchKey) != null) || (es.get(flippedKey) != null);
            }
            if (found) {
                return findDisappearPoint(searchKey, mid, max, searchForNode, flippedKey);
            } else {
                return findDisappearPoint(searchKey, min, mid, searchForNode, flippedKey);
            }
        }
    }

    public boolean areAlignmentsEqual(Edge e1, Edge e2) {
        if (e1.getClass() != e2.getClass()) {
            return false;
        } else if (e1 instanceof Contains) {
            Contains c1 = (Contains) e1;
            Contains c2 = (Contains) e2;
            return c1.isAlignmentTheSame(c2);
        } else {
            xpert.ag.Sibling s1 = (xpert.ag.Sibling) e1;
            xpert.ag.Sibling s2 = (xpert.ag.Sibling) e2;
            return s1.isAlignmentTheSame(s2);
        }

    }

    private boolean areParentsConsistent(ArrayList<AlignmentConstraint> acs) {
        for (AlignmentConstraint a1: acs) {
            for (AlignmentConstraint a2 : acs) {
                if (a1 != a2) {
                    if (a1.node1 != a2.node1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private int[] getValidWidths(ArrayList<AlignmentConstraint> acs) {
        ArrayList<Integer> widths = new ArrayList<Integer>();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (AlignmentConstraint ac : acs) {
            if (ac.min < min) {
                min = ac.min;
            }
            if (ac.max > max) {
                max = ac.max;
            }
        }
        for (int w : this.widths) {
            if ((w >= min) && (w <= max)) {
                widths.add(w);
            }
        }
        int[] widthArray = new int[widths.size()];
        for (Integer i : widths) {
            widthArray[widths.indexOf(i)] = i;
        }
        return widthArray;
    }

    private double[] getEquationOfLine(double[] widths, double[] values) {

        double[] coefficients = new double[3];
        boolean allValuesTheSame = Utils.areAllItemsSame(values);

        if (allValuesTheSame) {
            coefficients[0] = 0;
            coefficients[1] = -1;
            coefficients[2] = values[0];
        } else {
            double y1 = values[0];
            double x1 = widths[0];
            double y2 = values[values.length-1];
            double x2 = widths[widths.length-1];
            double gradient = ( (y2-y1) / (x2-x1) );
            coefficients[0] = gradient;
            double yintercept = y1- (gradient * x1);
            coefficients[1] = -1.0;
            coefficients[2] = yintercept;
        }
        return coefficients;
    }

    private double[] getBestFitLine(int[] ps, int[] cs, int i) {
        double[] widthsForEq = new double[i-1];
        double[] valuesForEq = new double[i-1];
        SimpleRegression reg = new SimpleRegression();
        for (int i2 = 0; i2 < i-1; i2++) {
            System.out.println(ps[i2] + " " + cs[i2]);
            reg.addData(ps[i2], cs[i2]);
        }
        System.out.println(reg.getSlope());
        double[] regressionEq;
//        if (Utils.areAllItemsSame(valuesForEq)) {
//            regressionEq = new double[] {0.0, -1.0, valuesForEq[0]};
//        } else {
//            try {
//                reg.linear();
//                double[] coeffs = reg.getBestEstimates();
//                regressionEq = new double[] {coeffs[1], -1.0, coeffs[0]};
//            } catch (IllegalArgumentException e) {
//                regressionEq = null;
//            }
//        }
        return new double[]{};
    }

    private int findWidthBreakpoint(double[] eq, int min, int max, String child, String parent) {
        if (max-min == 1) {
            int[] extraWidths = new int[] {min,max};
            ArrayList<AlignmentGraph> extraGraphs = new ArrayList<AlignmentGraph>();
            if ( (!alreadyGathered.contains(min)) || (!alreadyGathered.contains(max)) ) {
//                Redecheck.capturePageModel(url, extraWidths);
                alreadyGathered.add(min);
                alreadyGathered.add(max);
            }
            tempDoms = Redecheck.loadDoms(extraWidths, url);

            for (int w : extraWidths) {
                DomNode dn = tempDoms.get(w);
                AlignmentGraph ag = new AlignmentGraph(dn);
                extraGraphs.add(ag);
            }
            AlignmentGraph ag1 = extraGraphs.get(0);
            AlignmentGraph ag2 = extraGraphs.get(1);

            int c1 = ag1.getVMap().get(child).getDomNode().getWidth();
            int p1 = ag1.getVMap().get(parent).getDomNode().getWidth();
            int c2 = ag2.getVMap().get(child).getDomNode().getWidth();
            int p2 = ag2.getVMap().get(parent).getDomNode().getWidth();

            boolean result1 = Math.abs(p1*eq[0] + c1*eq[1] + eq[2]) <= 5;
            boolean result2 = Math.abs(p2*eq[0] + c2*eq[1] + eq[2]) <= 5;
            if (result1 && result2) {
                return max;
            } else if (result1 && !result2) {
                return min;
            } else if (!result1 && !result2) {
                return min-1;
            } else {
                return max+1;
            }
        } else {
            int mid = (max+min)/2;
            int[] extraWidths = new int[] {mid};
            if (!alreadyGathered.contains(mid)) {
//                Redecheck.capturePageModel(url, extraWidths);
                alreadyGathered.add(mid);
            }
            tempDoms = Redecheck.loadDoms(extraWidths, url);
            DomNode dn = tempDoms.get(extraWidths[0]);

            AlignmentGraph extraAG = new AlignmentGraph(dn);

            // Get parent and child widths
            int c = extraAG.getVMap().get(child).getDomNode().getWidth();
            int p = extraAG.getVMap().get(parent).getDomNode().getWidth();

            // Check whether mid falls on the equation's line or not.
            boolean result = Math.abs(p*eq[0] + c*eq[1] + eq[2]) <= 5;

            // Check which way to recurse
            if (result) {
                // Breakpoint is higher as mid is on the line
                findWidthBreakpoint(eq, mid, max, child, parent);
            } else {
                // Breakpoint is lower as mid is not on the line (already passed breakpoint)
                findWidthBreakpoint(eq, min, mid, child, parent);
            }
        }
        return 0;
    }

    public void writetoGraphViz(String graphName, boolean siblings) {
        PrintWriter output = null;
        try {
            output = new PrintWriter(graphName + ".gv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        output.append("digraph G {");


        for (AlignmentConstraint ac : this.alignmentConstraints.values()) {
            if (ac.type == Type.PARENT_CHILD) {
                Node parent = ac.node1;
                Node child = ac.node2;
                output.append("\n\t");
                output.append(parent.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" -> ");
                output.append(child.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));

                output.append(" [ label= \"" + ac.min + " ==> " + ac.max + " " + ac.generateLabelling() + "\" ];");


                output.append("\n\t");
                output.append(parent.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));

//                output.append(" [ label = \"" + parent.getPres() + " " + parent.label + " " + parent.printConstraints() + " \" ];");

                output.append("\n\t");
                output.append(child.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
//                output.append(" [ label = \"" + child.getPres() + " " + child.label + " \n " + child.printConstraints() + " \" ];");
            }
            else {
                Node node1 = ac.node1;
                Node node2 = ac.node2;
                output.append("\n\t");
                output.append(node1.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" -> ");
                output.append(node2.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
//
                output.append(" [ style=dotted, label= \"" + ac.min + " ==> " + ac.max + " " + ac.generateLabelling() + " \" ];");
//
//
//                output.append("\n\t");
//                output.append(node1.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
//                output.append(" [ label = \"" + node1.getPres() + " " + node1.label + " " + node1.printConstraints()  + " \" ];");
//
//                output.append("\n\t");
//                output.append(node2.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
//                output.append(" [ label = \"" + node2.getPres() + " " + node2.label + " \n " + node2.printConstraints() + " \" ];");
            }
        }
//        if (siblings) {
//            for (RLGSibling s : this.siblings) {
//                RLGNode node1 = s.first;
//                RLGNode node2 = s.second;
//                output.append("\n\t");
//                output.append(node1.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
//                output.append(" -> ");
//                output.append(node2.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
//
//                output.append(" [ style=dotted, label= \"" + s.getLabelString() + "\" ];");
//
//
//                output.append("\n\t");
//                output.append(node1.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
//                output.append(" [ label = \"" + node1.getPres() + " " + node1.label + " " + node1.printConstraints()  + " \" ];");
//
//                output.append("\n\t");
//                output.append(node2.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
//                output.append(" [ label = \"" + node2.getPres() + " " + node2.label + " \n " + node2.printConstraints() + " \" ];");
//            }
//        }

        output.append("\n}");
        output.close();

    }
}
