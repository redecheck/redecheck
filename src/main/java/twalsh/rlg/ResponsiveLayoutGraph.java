package twalsh.rlg;
import com.google.common.collect.HashBasedTable;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import twalsh.redecheck.Redecheck;
import xpert.ag.*;
import xpert.ag.Sibling;
import xpert.dom.DomNode;

import java.util.*;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

import twalsh.redecheck.Utils;

/**
 * Created by thomaswalsh on 10/08/15.
 * Last modified on 07/09/15.
 */
public class ResponsiveLayoutGraph {
    // Initialising various variables needed throughout the model construction process
    HashMap<String, Node> nodes = new HashMap<String, Node>();
    HashMap<String, AlignmentConstraint> alignments = new HashMap<String, AlignmentConstraint>();
    HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints = HashBasedTable.create();
    HashBasedTable<String, int[], WidthConstraint> widthConstraints = HashBasedTable.create();
    ArrayList<AlignmentGraph> graphs;
    AlignmentGraph first, last;

    public HashMap<String, Node> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<String, Node> nodes) {
        this.nodes = nodes;
    }

    public HashMap<String, AlignmentConstraint> getAlignments() {
        return alignments;
    }

    public void setAlignments(HashMap<String, AlignmentConstraint> alignments) {
        this.alignments = alignments;
    }

    ArrayList<AlignmentGraph> restOfGraphs;
    String url;
    Map<Integer, DomNode> doms;
    Map<Integer, DomNode> tempDoms;
    int[] widths;
    int[] restOfWidths;
    static HashSet<Integer> alreadyGathered;

    public static HashSet<Integer> getAlreadyGathered() {
        return alreadyGathered;
    }

    public static void setAlreadyGathered(HashSet<Integer> alreadyGathered) {
        ResponsiveLayoutGraph.alreadyGathered = alreadyGathered;
    }

    public ResponsiveLayoutGraph() {
        widths = new int[]{400,500,600};
        alreadyGathered = new HashSet<Integer>();
        last = null;
//        restOfGraphs = new ArrayList<AlignmentGraph>();
        // For testing purposes only
    }
    /**
     * Constructor to create the initial Responsive Layout Graph, which is then developed step by step.
     * @param ags               set of alignment graphs for each width sampled
     * @param stringWidths      set of ordered widths at which the page was sampled
     * @param url               the URL of the webpage under test
     * @param doms              the DOMs of the webpage at various viewport widths
     * @throws InterruptedException
     */
    public ResponsiveLayoutGraph(ArrayList<AlignmentGraph> ags, int[] stringWidths, String url, Map<Integer, DomNode> doms) throws InterruptedException {
        this.graphs = ags;
        this.first = ags.get(0);
        this.last = ags.get(ags.size()-1);
        restOfGraphs =  new ArrayList<AlignmentGraph>();
        for (AlignmentGraph ag : graphs) {
            restOfGraphs.add(ag);
        }
        restOfGraphs.remove(0);
        this.url = url;
        this.doms = doms;
        alreadyGathered = new HashSet<Integer>();
        restOfWidths = new int[stringWidths.length-1];
        this.widths = stringWidths;
        for (int i = 0; i < stringWidths.length; i++) {
            int s = stringWidths[i];
            if (i > 0) {
                restOfWidths[i-1] = s;
            }
            alreadyGathered.add(s);
        }
        extractVisibilityConstraints();
        System.out.println("DONE VISIBILITY CONSTRAINTS");
        extractAlignmentConstraints();
        System.out.println("DONE ALIGNMENT CONSTRAINTS");
        extractWidthConstraints();
        printNodes();
        System.out.println("DONE WIDTH CONSTRAINTS");
    }

    /**
     * Extracts all the visibility constraints for each node on the webpage by inspecting which elements are visible at
     * which resolutions.
     * @throws InterruptedException
     */
    private void extractVisibilityConstraints() throws InterruptedException {
        System.out.println("Extracting Visibility Constraints.");
        HashMap<String, VisibilityConstraint> visCons = new HashMap<>();
        ArrayList<AGNode> agnodes = (ArrayList<AGNode>) first.getVertices();
        HashMap<String, AGNode> previousMap = (HashMap<String, AGNode>) first.getVMap();

        setUpVisibilityConstraints(agnodes, visCons);

        for (AlignmentGraph ag : restOfGraphs) {
            HashMap<String, AGNode> previousToMatch = (HashMap<String, AGNode>) previousMap.clone();
            HashMap<String, AGNode> temp = (HashMap<String, AGNode>) ag.getVMap();
            HashMap<String, AGNode> tempToMatch = (HashMap<String, AGNode>) temp.clone();


            checkForNodeMatch(previousMap, temp, previousToMatch, tempToMatch);

            // Handle any disappearing elements
            updateDisappearingNode(previousToMatch, visCons, ag);

            // Handle any appearing elements
            updateAppearingNode(tempToMatch, visCons, ag);

            // Update the previousMap variable to keep track of last set of nodes
            previousMap = (HashMap<String, AGNode>) ag.getVMap();
        }

        // Update visibility widthConstraints of everything still visible
        updateRemainingNodes(visCons, last);

        // Attach constraints to the nodes

        attachVisConsToNodes(visCons);
    }

    public void attachVisConsToNodes(HashMap<String, VisibilityConstraint> visCons) {
        for (String x : this.nodes.keySet()) {
            Node n = this.nodes.get(x);
            VisibilityConstraint vc = visCons.get(x);
            n.addVisibilityConstraint(vc);
        }
    }

    public void updateRemainingNodes(HashMap<String, VisibilityConstraint> visCons, AlignmentGraph last) {
        for (String stilVis : last.getVMap().keySet()) {
            VisibilityConstraint vc = visCons.get(stilVis);
            if (vc.getDisappear() == 0) {
                vc.setDisappear(widths[widths.length - 1]);
            }
        }
    }

    public void updateAppearingNode(HashMap<String, AGNode> tempToMatch, HashMap<String, VisibilityConstraint> visCons, AlignmentGraph ag) {
        for (String currUM : tempToMatch.keySet()) {
            int appearPoint = 0;
            try {
                appearPoint = findAppearPoint(currUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], true, "");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nodes.put(currUM, new Node(currUM));
            visCons.put(currUM, new VisibilityConstraint(appearPoint, 0));
        }
    }

    public void updateDisappearingNode(HashMap<String, AGNode> previousToMatch, HashMap<String, VisibilityConstraint> visCons, AlignmentGraph ag) {
        for (String prevUM : previousToMatch.keySet()) {
            int disappearPoint = 0;
            try {
                disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], true, "");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            VisibilityConstraint vc = visCons.get(prevUM);
            vc.setDisappear(disappearPoint - 1);
        }
    }

    public void checkForNodeMatch(HashMap<String, AGNode> previousMap, HashMap<String, AGNode> temp, HashMap<String, AGNode> previousToMatch, HashMap<String, AGNode> tempToMatch) {
        for (String s : previousMap.keySet()) {
            if (temp.get(s) != null) {
                // Found a node match
                previousToMatch.remove(s);
                tempToMatch.remove(s);
            }
        }
    }

    public void setUpVisibilityConstraints(ArrayList<AGNode> agnodes, HashMap<String, VisibilityConstraint> cons) {
        for (AGNode node : agnodes) {
            // Add each node to overall set
            String xpath = node.getDomNode().getxPath();
            nodes.put(xpath, new Node(xpath));

            // Create visibility constraint for each one
            cons.put(xpath, new VisibilityConstraint((int) widths[0], 0));
        }
    }

    /**
     * Extracts the alignment constraints for all the nodes on the webpage.
     * @throws InterruptedException
     */
    private void extractAlignmentConstraints() throws InterruptedException {
        System.out.println("Extracting Alignment Constraints");
        HashMap<String, Edge> previousMap = first.getNewEdges();
        HashMap<String, AlignmentConstraint> alCons = new HashMap<String, AlignmentConstraint>();

        // Add initial edges to set.
        setUpAlignmentConstraints(previousMap, alCons);

        for (AlignmentGraph ag : restOfGraphs) {
            HashMap<String, Edge> previousToMatch = (HashMap<String, Edge>) previousMap.clone();
            HashMap<String, Edge> temp = ag.getNewEdges();
            HashMap<String, Edge> tempToMatch = (HashMap<String, Edge>) temp.clone();

            checkForEdgeMatch(previousMap, previousToMatch, temp, tempToMatch);

            // Handle disappearing edges
            updateDisappearingEdge(previousToMatch, alignmentConstraints, ag);

            // Handle appearing edges
            updateAppearingEdges(tempToMatch, alignmentConstraints, alCons, ag);

            previousMap = ag.getNewEdges();
        }

        // Update  alignment constraints of everything still visible
        updateRemainingEdges(alCons);

        addParentConstraintsToNodes();
        this.alignments = alCons;
    }

    private void updateRemainingEdges(HashMap<String, AlignmentConstraint> alCons) {
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
                xpert.ag.Sibling s = (xpert.ag.Sibling) e;
                String flipped = s.getNode2().getxPath() + s.getNode1().getxPath()+"sibling"+s.generateFlippedLabelling();
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
    }

    private void updateAppearingEdges(HashMap<String, Edge> tempToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, HashMap<String, AlignmentConstraint> alCons, AlignmentGraph ag) {
        for (String currUM : tempToMatch.keySet()) {
            Edge e = tempToMatch.get(currUM);
            int appearPoint = 0;
            Type t = null;
            AlignmentConstraint ac = null;
            if (e instanceof Contains) {
                Contains c = (Contains) e;
                try {
                    appearPoint = findAppearPoint(currUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], false, "");
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                t = Type.PARENT_CHILD;
                ac = new AlignmentConstraint(this.nodes.get(e.getNode2().getxPath()), this.nodes.get(e.getNode1().getxPath()), t, appearPoint, 0,
                        new boolean[]{c.isCentered(), c.isLeftJustified(), c.isRightJustified(), c.isMiddle(), c.isTopAligned(), c.isBottomAligned()});
            }
            else {
                t = Type.SIBLING;
                xpert.ag.Sibling s2 = (xpert.ag.Sibling) e;
                String flip = s2.getNode2().getxPath()+s2.getNode1().getxPath()+"sibling" +s2.generateFlippedLabelling();
                try {
                    appearPoint = findAppearPoint(currUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], false, flip);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                ac = new AlignmentConstraint(this.nodes.get(e.getNode1().getxPath()), this.nodes.get(e.getNode2().getxPath()), t, appearPoint, 0,
                        new boolean[]{s2.isTopBottom(),s2.isBottomTop(),s2.isRightLeft(),s2.isLeftRight(), s2.isTopEdgeAligned(),s2.isBottomEdgeAligned(),s2.isLeftEdgeAligned(), s2.isRightEdgeAligned()});

            }
            if (ac != null) {
                alCons.put(ac.generateKey(), ac);
                alignmentConstraints.put(ac.generateKey(), new int[]{appearPoint,0}, ac);
            }
        }
    }

    private void updateDisappearingEdge(HashMap<String, Edge> previousToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, AlignmentGraph ag) {
        for (String prevUM : previousToMatch.keySet()) {
            Edge e = previousToMatch.get(prevUM);
            int disappearPoint = 0;
            String flip="";
            if (e instanceof Contains) {
                try {
                    disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], false, "");
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } else {
                xpert.ag.Sibling s2 = (xpert.ag.Sibling) e;
                flip = s2.getNode2().getxPath()+s2.getNode1().getxPath()+"sibling" +s2.generateFlippedLabelling();
                try {
                    disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], false, flip);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(prevUM);
            Map<int[], AlignmentConstraint> cons2 = alignmentConstraints.row(flip);
            if (cons.size() > 0) {
                for (int[] pair : cons.keySet()) {
                    // Get the one without a max value
                    if (pair[1] == 0) {
                        AlignmentConstraint aCon = cons.get(pair);
                        aCon.setMax(disappearPoint - 1);
                        pair[1] = disappearPoint-1;
                    }
                }
            } else if (cons2.size() > 0) {
                for (int[] pair : cons2.keySet()) {
                    // Get the one without a max value
                    if (pair[1] == 0) {
                        AlignmentConstraint aCon = cons2.get(pair);
                        aCon.setMax(disappearPoint - 1);
                        pair[1] = disappearPoint-1;
                    }
                }
            }
        }

    }

    private void checkForEdgeMatch(HashMap<String, Edge> previousMap, HashMap<String, Edge> previousToMatch, HashMap<String, Edge> temp, HashMap<String, Edge> tempToMatch) {
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
    }

    private void setUpAlignmentConstraints(HashMap<String, Edge> previousMap, HashMap<String, AlignmentConstraint> alCons) {
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
    }

    /**
     * Extracts the width constraints for all the nodes visible on the webpage across the range of resolutions sampled
     * @throws InterruptedException
     */
    public void extractWidthConstraints() throws InterruptedException {
        System.out.println("Extracting Width Constraints.");
        Node n;

            for (String s : this.nodes.keySet()) {
                try {
                    n = this.nodes.get(s);
                    System.out.println(s);

                    if (n.parentConstraints.size() > 0) {
                        ArrayList<int[]> widths = getWidthsForConstraints(n.getParentConstraints());
                        System.out.println(widths.size());
                        for (int y = 0; y < widths.size(); y++) {
                            String parentXpath = n.getParentConstraints().get(y).node1.xpath;

                            int[] validWidths = widths.get(y);
                            int[] widthsTemp = new int[validWidths.length];
                            int[] parentWidths = new int[validWidths.length];
                            int[] childWidths = new int[validWidths.length];
                            doms = Redecheck.loadDoms(validWidths, url);
                            // Gather parent and child widths
                            populateWidthArrays(validWidths, widthsTemp, parentWidths, childWidths, s, parentXpath);


                            // Get the equations
                            double[] bestFit;
//                            TreeMap<Integer, double[]> equations = new TreeMap<Integer, double[]>();
//                            int previousBreakpoint = widthsTemp[0] - 1;
                            int previousBreakpoint = n.getParentConstraints().get(y).getMin()-1;
                            while (parentWidths.length >= 2) {
//                                foundBreakpoint = false;
                                int breakpointIndex;
                                double[] firstTwoWidths = new double[]{parentWidths[0], parentWidths[1]};
                                double[] firstTwoValues = new double[]{childWidths[0], childWidths[1]};
                                double[] equation = getEquationOfLine(firstTwoWidths, firstTwoValues);
                                breakpointIndex = matchValuesToEquation(equation, parentWidths, childWidths);

                                // Generate best fit equation
                                bestFit = getBestFitLine(parentWidths, childWidths, breakpointIndex);

                                int breakpoint;
                                if (breakpointIndex != parentWidths.length) {
                                    breakpoint = findWidthBreakpoint(bestFit, widthsTemp[breakpointIndex - 1], widthsTemp[breakpointIndex], s, parentXpath);
                                } else {
                                    breakpoint = widthsTemp[widthsTemp.length - 1];
                                }
                                System.out.println(breakpoint);
                                WidthConstraint wc = new WidthConstraint(previousBreakpoint + 1, breakpoint, bestFit[1], this.nodes.get(parentXpath), bestFit[2]);
                                this.widthConstraints.put(s, new int[]{previousBreakpoint + 1, breakpoint}, wc);
                                previousBreakpoint = breakpoint;

                                int[] tempWidths = new int[parentWidths.length];
                                int[] tempValues = new int[childWidths.length];
                                int[] tempScreenWidths = new int[widthsTemp.length];
                                System.arraycopy(parentWidths, 0, tempWidths, 0, parentWidths.length);
                                System.arraycopy(childWidths, 0, tempValues, 0, childWidths.length);
                                System.arraycopy(widthsTemp, 0, tempScreenWidths, 0, widthsTemp.length);

                                // Redefine the arrays we're using to extract equations
                                parentWidths = new int[tempWidths.length - breakpointIndex];
                                childWidths = new int[tempValues.length - breakpointIndex];
                                widthsTemp = new int[tempScreenWidths.length - breakpointIndex];
                                // Copy across reduced arrays
                                System.arraycopy(tempWidths, breakpointIndex, parentWidths, 0, parentWidths.length);
                                System.arraycopy(tempValues, breakpointIndex, childWidths, 0, childWidths.length);
                                System.arraycopy(tempScreenWidths, breakpointIndex, widthsTemp, 0, widthsTemp.length);
                            }
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        addWidthConstraintsToNodes();
    }

    public int matchValuesToEquation(double[] equation, int[] parentWidths, int[] childWidths) {
        for (int i = 2; i < parentWidths.length; i++) {
            double result = (equation[0] * childWidths[i]) - ((equation[1] * parentWidths[i]) + (equation[2]));
            if (Math.abs(result) > 5) {
                return i;
            }
        }
        return parentWidths.length;
    }

    private void populateWidthArrays(int[] validWidths, int[] widthsTemp, int[] parentWidths, int[] childWidths, String s, String parentXpath) {
        for (int i = 0; i < validWidths.length; i++) {
            try {
                AlignmentGraph ag = new AlignmentGraph(doms.get(validWidths[i]));
                widthsTemp[i] = validWidths[i];
                parentWidths[i] = ag.getVMap().get(parentXpath).getDomNode().getWidth();
                childWidths[i] = ag.getVMap().get(s).getDomNode().getWidth();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Prints the width constraints to the terminal for debugging purposes
     */
    public void printVisibilityConstraints() {
        for (String s : this.nodes.keySet()) {
            Node n = this.nodes.get(s);
            VisibilityConstraint vc = n.getVisibilityConstraints().get(0);
            System.out.println(s + "    " + vc);
        }

    }

    /**
     * Prints the alignment constraints to the terminal for debugging purposes
     * @param cons      the table of alignment constraints
     */
    public void printAlignmentConstraints(HashBasedTable<String, int[], AlignmentConstraint> cons) {
        for (String s : cons.rowKeySet()) {
            Map<int[],AlignmentConstraint> map = cons.row(s);
            System.out.println("\n" + s);
            for (AlignmentConstraint ac : map.values()) {
                System.out.println("\t" + ac);
            }
        }
    }

    /**
     * Prints the width constraints to the terminal for debugging purposes
     * @param cons      the table of width constraints
     */
    public void printWidthConstraints(HashBasedTable<String, int[], WidthConstraint> cons) {
        for (WidthConstraint wc : cons.values()) {
            System.out.println(wc);
        }
    }

    /**
     * Prints the nodes to the terminal for debugging purposes
     */
    private void printNodes() {
        for (Node n : this.nodes.values()) {
            System.out.println(n);
//            for (WidthConstraint wc : n.getWidthConstraints()) {
//                System.out.println(wc);
//            }
        }
    }

    /**
     * Goes through the full set of alignment constraints and adds the parent-child constraints to the node representing
     * the child element, for use in the width constraint extraction
     */
    private void addParentConstraintsToNodes() {
        for (AlignmentConstraint ac : this.alignmentConstraints.values()) {
            if (ac.type == Type.PARENT_CHILD) {
                Node child = this.nodes.get(ac.node2.getXpath());
                child.addParentConstraint(ac);
            }
        }
    }

    /**
     * Goes through the full set of extracted width constraints and adds them to the relevant nodes in the graph
     */
    private void addWidthConstraintsToNodes() {
        for (Node n : this.nodes.values()) {
            Map<int[], WidthConstraint> wcs = this.widthConstraints.row(n.xpath);
            for (WidthConstraint wc : wcs.values()) {
                n.addWidthConstraint(wc);
            }
        }
    }

    /**
     * Returns the viewport width at which a particular node or edge comes into view
     * @param searchKey         the search key to look for. Can be a node's XPath or a custom edge key
     * @param min               the lower bound of the search
     * @param max               the upper bound of the search
     * @param searchForNode     whether the search is for a node or an edge, as the code is different for each
     * @param flippedKey        an alternate key for searching for sibling edges
     * @return                  the viewport width at which the object represented by the key comes into view
     * @throws InterruptedException
     */
    public int findAppearPoint(String searchKey, int min, int max, boolean searchForNode, String flippedKey) throws InterruptedException {
        if (max-min==1) {
            int[] extraWidths = new int[] {min,max};
            ArrayList<AlignmentGraph> extraGraphs = new ArrayList<AlignmentGraph>();
            if ( (!alreadyGathered.contains(min)) || (!alreadyGathered.contains(max)) ) {
                Redecheck.capturePageModel(url, extraWidths);
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
                Redecheck.capturePageModel(url, extraWidths);
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

    /**
     * Returns the viewport width at which a particular node or edge disappears from view
     * @param searchKey         the search key to look for. Can be a node's XPath or a custom edge key
     * @param min               the lower bound of the search
     * @param max               the upper bound of the search
     * @param searchForNode     whether the search is for a node or an edge, as the code is different for each
     * @param flippedKey        an alternate key for searching for sibling edges
     * @return                  the viewport width at which the object represented by the key disappears from view
     * @throws InterruptedException
     */
    public int findDisappearPoint(String searchKey, int min, int max, boolean searchForNode, String flippedKey) throws InterruptedException {
        if (max-min==1) {
            int[] extraWidths = new int[] {min,max};
            ArrayList<AlignmentGraph> extraGraphs = new ArrayList<AlignmentGraph>();
            if ( (!alreadyGathered.contains(min)) || (!alreadyGathered.contains(max)) ) {
                Redecheck.capturePageModel(url, extraWidths);
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

            return decideBreakpoint(min,max, found1, found2);

        } else {
            int mid = (max+min)/2;
            int[] extraWidths = new int[] {mid};
            if (!alreadyGathered.contains(mid)) {
                Redecheck.capturePageModel(url, extraWidths);
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

    public int decideBreakpoint(int min, int max, boolean found1, boolean found2) {
        if (found1 && found2) {
            return max+1;
        } else if (found1 && !found2) {
            return max;
        } else {
            return min;
        }
    }

    /**
     * Takes a set of parent-child alignment constraints for a particular element and return a sequence of sets of
     * widths for which each alignment constraint holds
     * @param acs       the set of alignment constraints for the element
     * @return          a set of arrays representing the sets of widths
     */
    private ArrayList<int[]> getWidthsForConstraints(ArrayList<AlignmentConstraint> acs) {
        ArrayList<int[]> widthSets = new ArrayList<int[]>();
        TreeMap<Integer, AlignmentConstraint> ordered = new TreeMap<Integer, AlignmentConstraint>();
        for (AlignmentConstraint c : acs) {
            ordered.put(c.min,c);
        }
//        int numParents = 0;
        String previousParent = null;
        HashMap<Integer, AlignmentConstraint> parentBreakpoints = new HashMap<Integer, AlignmentConstraint>();
        HashMap<String, int[]> parentRanges = new HashMap<>();


        // Get all the different parents
        for (AlignmentConstraint c : ordered.values()) {
            if (!c.node1.xpath.equals(previousParent)) {
                parentBreakpoints.put(c.min, c);
                previousParent = c.node1.getXpath();

                parentRanges.put(c.node1.getXpath(), new int[] {c.min, c.getMax()});
                previousParent = c.node1.getXpath();
            } else {
                // Update max value
                int[] range = parentRanges.get(c.getNode1().getXpath());
                range[1] = c.getMax();
            }
        }

        for (int[] range : parentRanges.values()) {
            ArrayList<Integer> temp = new ArrayList<>();
            for (int w : this.widths) {
                if ((w >= range[0]) && (w <= range[1])) {
                    temp.add(w);
                }
            }
            int[] widthArray = new int[temp.size()];
            for (Integer i : temp) {
                widthArray[temp.indexOf(i)] = i;
            }
            widthSets.add(widthArray);
        }


        return widthSets;
    }

    /**
     * Takes a set of parent-child width pairs and uses the first two of them to construct an initial equation
     * @param widths    the set of parent widths
     * @param values    the set of child widths
     * @return          the coefficients of the initial equation
     */
    public double[] getEquationOfLine(double[] widths, double[] values) {

        double[] coefficients = new double[3];
        boolean allValuesTheSame = Utils.areAllItemsSame(values);

        if (allValuesTheSame) {
            coefficients[0] = 1;
            coefficients[1] = 0;
            coefficients[2] = values[0];
        } else {
            double y1 = values[0];
            double x1 = widths[0];
            double y2 = values[values.length-1];
            double x2 = widths[widths.length-1];
            double gradient = ( (y2-y1) / (x2-x1) );
            coefficients[0] = 1.0;
            double yintercept = y1- (gradient * x1);
            coefficients[1] = gradient;
            coefficients[2] = yintercept;
        }
        return coefficients;
    }

    /**
     * Takes a sequence of parent-child width pairs and fits a line of best fit onto the values
     * @param ps    the set of parent widths
     * @param cs    the set of child widths
     * @param i     the index at which to stop extracting values
     * @return      the coefficients of the best fit line
     */
    public double[] getBestFitLine(int[] ps, int[] cs, int i) {
        double[] valuesForEq = new double[i-1];
        SimpleRegression reg = new SimpleRegression();
        // Add in values to the data set
        for (int i2 = 0; i2 < i-1; i2++) {
            reg.addData(ps[i2], cs[i2]);
            valuesForEq[i2] = cs[i2];
        }
        double[] regressionEq;

        // Generate the line of best fit
        if (Utils.areAllItemsSame(valuesForEq)) {
            // Child widths consistent, so just a flat line
            regressionEq = new double[] {1.0, 0, valuesForEq[0]};
        } else {
            // Plot the line and return the coefficients
            try {
                regressionEq = new double[] {1.0, reg.getSlope(),reg.getIntercept()};
            } catch (IllegalArgumentException e) {
                regressionEq = null;
            }
        }
        return regressionEq;
    }

    /**
     * Returns the viewport width at which the width equation for a particular elements ceases to become true.
     * This is done through the use of a binary search between two viewport widths, extracted during the main model
     * construction process.
     * @param eq        the width equation for the element
     * @param min       the lower bound for the binary search
     * @param max       the upper bound for the binary search
     * @param child     the element to which the width equation applies
     * @param parent    the parent of the element between the upper and lower bounds. Used to determine whether the element's
     *                  width matches the equation
     * @return          the viewport width at which the equation ceases to hold for the element
     * @throws InterruptedException
     */
    private int findWidthBreakpoint(double[] eq, int min, int max, String child, String parent) throws InterruptedException {
        if (max-min == 1) {
            int[] extraWidths = new int[] {min,max};
            ArrayList<AlignmentGraph> extraGraphs = new ArrayList<AlignmentGraph>();
            if ( (!alreadyGathered.contains(min)) || (!alreadyGathered.contains(max)) ) {
                Redecheck.capturePageModel(url, extraWidths);
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
            boolean result1 = Math.abs((eq[0]*c1) - ((eq[1]*p1) + (eq[2]))) <=5;
            boolean result2 = Math.abs((eq[0]*c2) - ((eq[1]*p2) + (eq[2]))) <=5;
            if (result1 && result2) {
                return max;
            } else if (result1 && !result2) {
                return min;
            } else if (!result1 && !result2) {
                return min-1;
            } else {
                return max+1;
            }
        } else if (max > min) {
            int mid = (max+min)/2;
            int[] extraWidths = new int[] {mid};
            if (!alreadyGathered.contains(mid)) {
                Redecheck.capturePageModel(url, extraWidths);
                alreadyGathered.add(mid);
            }
            tempDoms = Redecheck.loadDoms(extraWidths, url);
            DomNode dn = tempDoms.get(extraWidths[0]);
            AlignmentGraph extraAG = new AlignmentGraph(dn);

            // Get parent and child widths
            int c = extraAG.getVMap().get(child).getDomNode().getWidth();
            int p = extraAG.getVMap().get(parent).getDomNode().getWidth();

            // Check whether mid falls on the equation's line or not.
            boolean result = Math.abs((eq[0]*c) - ((eq[1]*p) + (eq[2]))) <=5;

            // Check which way to recurse
            if (result) {
                // Breakpoint is higher as mid is on the line
                return findWidthBreakpoint(eq, mid, max, child, parent);
            } else if (!result){
                // Breakpoint is lower as mid is not on the line (already passed breakpoint)
                return findWidthBreakpoint(eq, min, mid, child, parent);
            }
        }
        return (min+max)/2;
    }

    /**
     * Takes the RLG and presents it in a visual format so it can be further examined if required.
     * Visibility, alignment and width constraints are added to the graph so all aspects can be inspected.
     * @param graphName the file name of the GraphViz file you wish to create
     */
    public void writeToGraphViz(String graphName) {
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
                output.append(" [ label = \"" + parent.generateGraphVizLabel() + " \" ];");

                output.append("\n\t");
                output.append(child.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" [ label = \"" + child.generateGraphVizLabel() + " \" ];");
            }
            else {
                Node node1 = ac.node1;
                Node node2 = ac.node2;
                output.append("\n\t");
                output.append(node1.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" -> ");
                output.append(node2.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" [ style=dotted, label= \"" + ac.min + " ==> " + ac.max + " " + ac.generateLabelling() + " \" ];");
                output.append("\n\t");
                output.append(node1.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" [ label = \"" + node1.generateGraphVizLabel() + " \" ];");

                output.append("\n\t");
                output.append(node2.xpath.replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" [ label = \"" + node2.generateGraphVizLabel() + " \" ];");
            }
        }

        output.append("\n}");
        output.close();

    }
}
