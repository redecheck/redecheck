package twalsh.rlg;
import com.google.common.collect.HashBasedTable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import twalsh.redecheck.Redecheck;
import edu.gatech.xpert.dom.*;
import edu.gatech.xpert.dom.layout.*;
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
    ArrayList<AlignmentGraphFactory> graphs;
    AlignmentGraphFactory first, last;
    AlignmentGraph ag;
    ResponsiveLayoutGraph oracle;



    ArrayList<AlignmentGraphFactory> restOfGraphs;
    String url;
    HashMap<Integer, DomNode> doms;
    Map<Integer, DomNode> tempDoms;
    HashMap<Integer, DomNode> oracleDoms;
    HashMap<Integer, AlignmentGraphFactory> factories;
    int[] widths;
    int[] restOfWidths;
    static HashSet<Integer> alreadyGathered;


    public ResponsiveLayoutGraph() {
        alreadyGathered = new HashSet<Integer>();
        last = null;
        // For testing purposes only
    }
    
    /**
     * Constructor to create the initial Responsive Layout Graph, which is then developed step by step.
     * @param ags               set of alignment graphs for each width sampled
     * @param stringWidths      set of ordered widths at which the page was sampled
     * @param url               the URL of the webpage under test
     * @param doms              the DOMs of the webpage at various viewport widths
     * @param oDoms
     * @throws InterruptedException
     */
    public ResponsiveLayoutGraph(ArrayList<AlignmentGraphFactory> ags, int[] stringWidths, String url, HashMap<Integer, DomNode> doms) throws InterruptedException {
        this.graphs = ags;
        this.first = ags.get(0);
        this.last = ags.get(ags.size()-1);
        restOfGraphs =  new ArrayList<AlignmentGraphFactory>();
        factories = new HashMap<Integer, AlignmentGraphFactory>();
        for (AlignmentGraphFactory agf : graphs) {
            restOfGraphs.add(agf);
            factories.put(stringWidths[graphs.indexOf(agf)], agf);
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
//        oracle = o;
//        oracleDoms = (HashMap<Integer, DomNode>) oDoms;
        
        extractVisibilityConstraints();
        System.out.println("DONE VISIBILITY CONSTRAINTS");
        extractAlignmentConstraints();
        System.out.println("DONE ALIGNMENT CONSTRAINTS");
        extractWidthConstraints();
        System.out.println("DONE WIDTH CONSTRAINTS");
        System.out.println("Number of nodes: " + this.nodes.size());
        System.out.println("Number of edges: " + this.alignmentConstraints.size());
    }




    public static HashSet<Integer> getAlreadyGathered() {
        return alreadyGathered;
    }

    public static void setAlreadyGathered(HashSet<Integer> alreadyGathered) {
        ResponsiveLayoutGraph.alreadyGathered = alreadyGathered;
    }

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



    /**
     * Extracts all the visibility constraints for each node on the webpage by inspecting which elements are visible at
     * which resolutions.
     * @throws InterruptedException
     */
    private void extractVisibilityConstraints() throws InterruptedException {
        System.out.println("Extracting Visibility Constraints.");
        HashMap<String, VisibilityConstraint> visCons = new HashMap<>();
        HashMap<String, DomNode> domNodes =  first.domNodeMap;
        HashMap<String, DomNode> previousMap = (HashMap<String, DomNode>) first.domNodeMap;

        setUpVisibilityConstraints(domNodes, visCons);

        for (AlignmentGraphFactory agf : restOfGraphs) {
            HashMap<String, DomNode> previousToMatch = (HashMap<String, DomNode>) previousMap.clone();
            HashMap<String, DomNode> temp = (HashMap<String, DomNode>) agf.domNodeMap;
            HashMap<String, DomNode> tempToMatch = (HashMap<String, DomNode>) temp.clone();


            checkForNodeMatch(previousMap, temp, previousToMatch, tempToMatch);

            // Handle any disappearing elements
            updateDisappearingNode(previousToMatch, visCons, agf);

            // Handle any appearing elements
            updateAppearingNode(tempToMatch, visCons, agf);

            // Update the previousMap variable to keep track of last set of nodes
            previousMap = (HashMap<String, DomNode>) agf.domNodeMap;
            double progressPerc = ((double) (restOfGraphs.indexOf(agf)+1)/ (double)restOfGraphs.size())* 100;
            System.out.print("\rPROGRESS : | " + StringUtils.repeat("=", (int) progressPerc) + StringUtils.repeat(" ", 100 - (int) progressPerc) + " | " + (int)progressPerc + "%");
        }
        System.out.print("\n");
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

    public void updateRemainingNodes(HashMap<String, VisibilityConstraint> visCons, AlignmentGraphFactory last) {
        for (String stilVis : last.getNodeMap().keySet()) {
            VisibilityConstraint vc = visCons.get(stilVis);
            if (vc.getDisappear() == 0) {
                vc.setDisappear(widths[widths.length - 1]);
            }
        }
    }

    public void updateAppearingNode(HashMap<String, DomNode> tempToMatch, HashMap<String, VisibilityConstraint> visCons, AlignmentGraphFactory agf) {
        for (String currUM : tempToMatch.keySet()) {
            int appearPoint = 0;
            try {
                appearPoint = findAppearPoint(currUM, widths[restOfGraphs.indexOf(agf)], widths[restOfGraphs.indexOf(agf) + 1], true, "");
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
            nodes.put(currUM, new Node(currUM));
            visCons.put(currUM, new VisibilityConstraint(appearPoint, 0));
        }
    }

    public void updateDisappearingNode(HashMap<String, DomNode> previousToMatch, HashMap<String, VisibilityConstraint> visCons, AlignmentGraphFactory agf) {
        for (String prevUM : previousToMatch.keySet()) {
            int disappearPoint = 0;
            try {
                disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(agf)], widths[restOfGraphs.indexOf(agf) + 1], true, "");
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
            VisibilityConstraint vc = visCons.get(prevUM);
            vc.setDisappear(disappearPoint - 1);
        }
    }

    public void checkForNodeMatch(HashMap<String, DomNode> previousMap, HashMap<String, DomNode> temp, HashMap<String, DomNode> previousToMatch, HashMap<String, DomNode> tempToMatch) {
        for (String s : previousMap.keySet()) {
            if (temp.get(s) != null) {
                // Found a node match
                previousToMatch.remove(s);
                tempToMatch.remove(s);
            }
        }
    }

    public void setUpVisibilityConstraints(HashMap<String, DomNode> domnodes, HashMap<String, VisibilityConstraint> cons) {
        for (DomNode node : domnodes.values()) {
            // Add each node to overall set
            String xpath = node.getxPath();
            nodes.put(xpath, new Node(xpath));

            // Create visibility constraint for each one
            cons.put(xpath, new VisibilityConstraint(widths[0], 0));
        }
    }

    /**
     * Extracts the alignment constraints for all the nodes on the webpage.
     * @throws InterruptedException
     */
    private void extractAlignmentConstraints() throws InterruptedException {
        System.out.println("Extracting Alignment Constraints");
        HashMap<String, AGEdge> previousMap = first.edgeMap;
        HashMap<String, AlignmentConstraint> alCons = new HashMap<String, AlignmentConstraint>();

        // Add initial edges to set.
        setUpAlignmentConstraints(previousMap, alCons);

        for (AlignmentGraphFactory ag : restOfGraphs) {
            HashMap<String, AGEdge> previousToMatch = (HashMap<String, AGEdge>) previousMap.clone();
            HashMap<String, AGEdge> temp = ag.getEdgeMap();
            HashMap<String, AGEdge> tempToMatch = (HashMap<String, AGEdge>) temp.clone();

            checkForEdgeMatch(previousMap, previousToMatch, temp, tempToMatch);

            // NEW METHOD FOR SAVING EFFORT
//            long startTime = System.nanoTime();
            HashMap<AGEdge, AGEdge> matchedChangingEdges = pairUnmatchedEdges(previousToMatch, tempToMatch);
//            long endTime = System.nanoTime();
//            long duration = (endTime - startTime);
//            System.out.println("Matching pairs " + duration*1.0/1000000);
            
//            startTime = System.nanoTime();
            updatePairedEdges(matchedChangingEdges, alignmentConstraints, alCons, ag);
//            endTime = System.nanoTime();
//            duration = (endTime - startTime);
//            System.out.println("Updating pairs " + duration*1.0/1000000);

//            startTime = System.nanoTime();
            
            if (previousToMatch.size() != 0) {
            	checkForNodeBasedDisappearances(previousToMatch, alignmentConstraints, ag, this.widths[restOfGraphs.indexOf(ag)], this.widths[restOfGraphs.indexOf(ag) + 1]);
            	// Handle disappearing edges
//            	System.out.println("Number of disappearing: " + previousToMatch.size());
            	updateDisappearingEdges(previousToMatch, alignmentConstraints, ag);
            }
//            endTime = System.nanoTime();
//            duration = (endTime - startTime);
//            System.out.println("Disappearing " + duration*1.0/1000000);
            
            // Handle appearing edges
//            startTime = System.nanoTime();
            
            if (tempToMatch.size() !=0) {
            	checkForNodeBasedAppearances(tempToMatch, alignmentConstraints, alCons, ag, this.widths[restOfGraphs.indexOf(ag)], this.widths[restOfGraphs.indexOf(ag) + 1]);
//            	System.out.println("Number of appearing: " + tempToMatch.size());
            	updateAppearingEdges(tempToMatch, alignmentConstraints, alCons, ag);
            }
//            endTime = System.nanoTime();
//            duration = (endTime - startTime);
////            System.out.println("Appearing " + duration*1.0/1000000);
            previousMap = ag.getEdgeMap();
//            System.out.println();
//            }

            double progressPerc = ((double) (restOfGraphs.indexOf(ag)+1)/ (double)restOfGraphs.size())* 100;
            System.out.print("\rPROGRESS : | " + StringUtils.repeat("=", (int)progressPerc) + StringUtils.repeat(" ", 100 - (int)progressPerc) + " | " + (int)progressPerc + "%");
        }

        // Update  alignment constraints of everything still visible
        AlignmentGraphFactory last = restOfGraphs.get(restOfGraphs.size()-1);
        updateRemainingEdges(alCons, last);

        System.out.println();
        addParentConstraintsToNodes();
        this.alignments = alCons;
    }




    private HashMap<AGEdge, AGEdge> pairUnmatchedEdges(HashMap<String, AGEdge> previous, HashMap<String, AGEdge> temp) {
        HashMap<String, AGEdge> previousToMatch = (HashMap<String, AGEdge>) previous.clone();
        HashMap<String, AGEdge> tempToMatch = (HashMap<String, AGEdge>) temp.clone();

        HashMap<AGEdge, AGEdge> paired = new HashMap<>();
        for (String s : previousToMatch.keySet()) {
            AGEdge e = previousToMatch.get(s);
            DomNode n1 = e.getNode1();
            DomNode n2 = e.getNode2();
            for (String s2 : tempToMatch.keySet()) {
                AGEdge e2 = tempToMatch.get(s2);
                DomNode n1m = e2.getNode1();
                DomNode n2m = e2.getNode2();

                // Checks to see if both node1 and node2 are the same.
                if ( (n1.getxPath().equals(n1m.getxPath())) && (n2.getxPath().equals(n2m.getxPath()))) {
                    paired.put(e, e2);
                    previous.remove(s);
                    temp.remove(s2);
                // Check for the flipped sibling node match
                } else if ( (n1.getxPath().equals(n2m.getxPath())) && (n2.getxPath().equals(n1m.getxPath())) ) {
                    paired.put(e, e2);
                    previous.remove(s);
                    temp.remove(s2);
                // Check for matching child, but differing parents
                } else if ( ((n1.getxPath().equals(n1m.getxPath())) && (!n2.getxPath().equals(n2m.getxPath())) && (e instanceof Contains) && (e2 instanceof Contains)) ) {
//                    System.out.println(e);
                    paired.put(e, e2);
                    previous.remove(s);
                    temp.remove(s2);
                }
            }
        }

        return paired;
    }

    public void updateRemainingEdges(HashMap<String, AlignmentConstraint> alCons, AlignmentGraphFactory last) {
        for (String stilVis : last.getEdgeMap().keySet()) {
            AGEdge e = last.getEdgeMap().get(stilVis);
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
                Sibling s = (Sibling) e;
                String flipped = s.getNode2().getxPath() + s.getNode1().getxPath()+"sibling"+AlignmentGraphFactory.generateFlippedLabelling(s);
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
                } else {
                    System.out.println("Couldn't find existing constraint for " + stilVis);
                }
            }
        }
    }

    private void updatePairedEdges(HashMap<AGEdge, AGEdge> matchedChangingEdges, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, HashMap<String, AlignmentConstraint> alCons, AlignmentGraphFactory ag) {
//        System.out.println("Number of pairs: " + matchedChangingEdges.size());
    	for (AGEdge e : matchedChangingEdges.keySet()) {
            String pairedkey1 = AlignmentGraphFactory.generateKey(e);
            AGEdge matched = matchedChangingEdges.get(e);

            int disappearPoint = 0;
            String flip="";
            if (e instanceof Contains) {
                try {
                    disappearPoint = findDisappearPoint(pairedkey1, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], false, "");
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } else {
                Sibling s2 = (Sibling) e;
                flip = s2.getNode2().getxPath()+s2.getNode1().getxPath()+"sibling" +AlignmentGraphFactory.generateFlippedLabelling(s2);
                try {
                    disappearPoint = findDisappearPoint(pairedkey1, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], false, flip);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(pairedkey1);
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

            // Create the appearing AC (matched)
            AlignmentConstraint ac;
            Type t;
            if (matched instanceof Contains) {
                t = Type.PARENT_CHILD;
                Contains c = (Contains) matched;
                ac = new AlignmentConstraint(this.nodes.get(e.getNode2().getxPath()), this.nodes.get(matched.getNode1().getxPath()), t, disappearPoint, 0,
                        new boolean[]{c.isCentered(), c.isLeftJustified(), c.isRightJustified(), c.isMiddle(), c.isTopAligned(), c.isBottomAligned()});
            } else {
                t = Type.SIBLING;
                Sibling s2 = (Sibling) matched;
                ac = new AlignmentConstraint(this.nodes.get(matched.getNode1().getxPath()), this.nodes.get(matched.getNode2().getxPath()), t, disappearPoint, 0,
                        new boolean[]{s2.isTopBottom(),s2.isBottomTop(),s2.isRightLeft(),s2.isLeftRight(), s2.isTopEdgeAligned(),s2.isBottomEdgeAligned(),s2.isLeftEdgeAligned(), s2.isRightEdgeAligned()});

            }
            if (ac != null) {
                alCons.put(ac.generateKey(), ac);
                alignmentConstraints.put(ac.generateKey(), new int[]{disappearPoint,0}, ac);
            }
        }

    }

    public void updateAppearingEdges(HashMap<String, AGEdge> tempToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, HashMap<String, AlignmentConstraint> alCons, AlignmentGraphFactory ag) {
        for (String currUM : tempToMatch.keySet()) {
            AGEdge e = tempToMatch.get(currUM);
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
                Sibling s2 = (Sibling) e;
                String flip = s2.getNode2().getxPath()+s2.getNode1().getxPath()+"sibling" + AlignmentGraphFactory.generateFlippedLabelling(s2);
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

    public void updateDisappearingEdges(HashMap<String, AGEdge> previousToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, AlignmentGraphFactory ag) {
        for (String prevUM : previousToMatch.keySet()) {
            AGEdge e = previousToMatch.get(prevUM);
//            System.out.println(e + " " + AlignmentGraphFactory.generateEdgeLabelling(e));
            int disappearPoint = 0;
            String flip="";
            if (e instanceof Contains) {
                try {
                    disappearPoint = findDisappearPoint(prevUM, widths[restOfGraphs.indexOf(ag)], widths[restOfGraphs.indexOf(ag) + 1], false, "");
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } else {
                Sibling s2 = (Sibling) e;
                flip = s2.getNode2().getxPath()+s2.getNode1().getxPath()+"sibling" +AlignmentGraphFactory.generateFlippedLabelling(s2);
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
            } else {
                System.out.println("Couldn't find existing constraint for " + prevUM);
            }
        }
    }

    private void checkForNodeBasedDisappearances(HashMap<String, AGEdge> previousToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, AlignmentGraphFactory ag, int min, int max) {
        DomNode node1, node2;
        String flip;
        HashMap<String, AGEdge> tempMap = (HashMap<String, AGEdge>) previousToMatch.clone();
        for (String s : tempMap.keySet()) {
            flip="";
            AGEdge edge = tempMap.get(s);
            if (edge instanceof Sibling) {
                Sibling s2 = (Sibling) edge;
                flip = s2.getNode2().getxPath() + s2.getNode1().getxPath() + "sibling" + AlignmentGraphFactory.generateFlippedLabelling(s2);
            }
            node1 = edge.getNode1();
            node2 = edge.getNode2();
            if ( (nodeDisappears(node1, min, max)) || (nodeDisappears(node2, min, max)) ) {
                // Get VC of disappearing node
                VisibilityConstraint vc = this.nodes.get(node1.getxPath()).getVisibilityConstraints().get(0);
                int disappearPoint = vc.getDisappear();

                // Update with correct value
                Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(s);
                Map<int[], AlignmentConstraint> cons2 = alignmentConstraints.row(flip);
                if (cons.size() > 0) {
                    for (int[] pair : cons.keySet()) {
                        // Get the one without a max value
                        if (pair[1] == 0) {
                            AlignmentConstraint aCon = cons.get(pair);
                            aCon.setMax(disappearPoint);
                            pair[1] = disappearPoint;
                            previousToMatch.remove(s);
                        }
                    }
                } else if (cons2.size() > 0) {
                    for (int[] pair : cons2.keySet()) {
                        // Get the one without a max value
                        if (pair[1] == 0) {
                            AlignmentConstraint aCon = cons2.get(pair);
                            aCon.setMax(disappearPoint);
                            pair[1] = disappearPoint;
                            previousToMatch.remove(flip);
                        }
                    }
                }
            }
        }
    }

    private void checkForNodeBasedAppearances(HashMap<String, AGEdge> tempToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints,HashMap<String, AlignmentConstraint> alCons, AlignmentGraphFactory ag, int min, int max) {
        DomNode node1, node2;
        String flip;
        HashMap<String, AGEdge> tempMap = (HashMap<String, AGEdge>) tempToMatch.clone();
        for (String s : tempMap.keySet()) {
            flip = "";
            AGEdge edge = tempMap.get(s);
            if (edge instanceof Sibling) {
                Sibling s2 = (Sibling) edge;
                flip = s2.getNode2().getxPath() + s2.getNode1().getxPath() + "sibling" + AlignmentGraphFactory.generateFlippedLabelling(s2);
            }
            node1 = edge.getNode1();
            node2 = edge.getNode2();
            if ((nodeAppears(node1, min, max)) || (nodeAppears(node2, min, max))) {
//                System.out.println(s);

                // Get VC of disappearing node
            	try {
	                VisibilityConstraint vc = this.nodes.get(node1.getxPath()).getVisibilityConstraints().get(0);
	                int appearPoint = vc.getAppear();
	
	                Type t = null;
	                AlignmentConstraint ac = null;
	                if (edge instanceof Contains) {
	                    Contains c = (Contains) edge;
	                    t = Type.PARENT_CHILD;
	                    ac = new AlignmentConstraint(this.nodes.get(edge.getNode2().getxPath()), this.nodes.get(edge.getNode1().getxPath()), t, appearPoint, 0,
	                            new boolean[]{c.isCentered(), c.isLeftJustified(), c.isRightJustified(), c.isMiddle(), c.isTopAligned(), c.isBottomAligned()});
	                }
	                else {
	                    t = Type.SIBLING;
	                    Sibling s2 = (Sibling) edge;
	
	                    ac = new AlignmentConstraint(this.nodes.get(edge.getNode1().getxPath()), this.nodes.get(edge.getNode2().getxPath()), t, appearPoint, 0,
	                            new boolean[]{s2.isTopBottom(),s2.isBottomTop(),s2.isRightLeft(),s2.isLeftRight(), s2.isTopEdgeAligned(),s2.isBottomEdgeAligned(),s2.isLeftEdgeAligned(), s2.isRightEdgeAligned()});
	
	                }
	                if (ac != null) {
	                    alCons.put(ac.generateKey(), ac);
	                    alignmentConstraints.put(ac.generateKey(), new int[]{appearPoint,0}, ac);
	                    tempToMatch.remove(s);
	                    tempToMatch.remove(flip);
	                }
            	} catch (NullPointerException e) {
            		System.out.println(edge);
            		System.out.println(this.nodes.get(node1.getxPath()));
            	}
            }
        }
    }

    private boolean nodeAppears(DomNode node1, int min, int max) {
        Node n = this.nodes.get(node1.getxPath());
        if ( n != null) {
            ArrayList<VisibilityConstraint> vcs = n.getVisibilityConstraints();
            for (VisibilityConstraint vc : vcs) {
                int ap = vc.getAppear();
                if ( (ap > min) && (ap < max) ) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean nodeDisappears(DomNode node1, int min, int max) {
        Node n = this.nodes.get(node1.getxPath());
        if ( n != null) {
            ArrayList<VisibilityConstraint> vcs = n.getVisibilityConstraints();
            for (VisibilityConstraint vc : vcs) {
                int dp = vc.getDisappear();
                if ( (dp > min) && (dp < max) ) {
                    return true;
                }
            }
        }
        return false;
    }


    public void checkForEdgeMatch(HashMap<String, AGEdge> previousMap, HashMap<String, AGEdge> previousToMatch, HashMap<String, AGEdge> temp, HashMap<String, AGEdge> tempToMatch) {
        String key = "", key2 = "";
        for (String s : previousMap.keySet()) {
            AGEdge e = previousMap.get(s);
            key = e.getNode1().getxPath() + e.getNode2().getxPath();
            key2 = e.getNode2().getxPath() + e.getNode1().getxPath();
            if (e instanceof Contains) {
                Contains cTemp = (Contains) e;
                key += "contains" +AlignmentGraphFactory.generateEdgeLabelling(cTemp);
            } else {
                Sibling sTemp = (Sibling) e;
                key += "sibling" + AlignmentGraphFactory.generateEdgeLabelling(sTemp);
                key2 += "sibling" + AlignmentGraphFactory.generateFlippedLabelling(sTemp);
            }
            if ((temp.get(key) != null) || (temp.get(key2) != null)) {

//            } else if  {
                previousToMatch.remove(key);
                tempToMatch.remove(key);
                previousToMatch.remove(key2);
                tempToMatch.remove(key2);
            }
//                boolean matched = false;
//                if (e instanceof Contains) {
//                    Contains c1 = (Contains) e;
//                    Contains c2 = (Contains) temp.get(key);
//                    matched = AlignmentGraphFactory.isAlignmentTheSame(c1, c2);
//                } else {
//                    Sibling s1 = (Sibling) e;
//                    Sibling s2;
//                    if (temp.get(key) != null) {
//                        s2 = (Sibling) temp.get(key);
//                    } else {
//                        s2 = (Sibling) temp.get(key2);
//                    }
//                    matched = AlignmentGraphFactory.isAlignmentTheSame(s1, s2);
//                }
//                if (matched) {


//                }
//            }

        }
    }

    public void setUpAlignmentConstraints(HashMap<String, AGEdge> previousMap, HashMap<String, AlignmentConstraint> alCons) {
        for (String s : previousMap.keySet()) {
        	try {
	            AGEdge e = previousMap.get(s);
	            if (e instanceof Contains) {
	                Contains c = (Contains) e;
	                if (this.nodes.get(e.getNode2().getxPath()) == null) {
	                	System.out.println(c);
	                }
	                AlignmentConstraint con = new AlignmentConstraint(this.nodes.get(e.getNode2().getxPath()), this.nodes.get(c.getNode1().getxPath()), Type.PARENT_CHILD, this.widths[0], 0,
	                        new boolean[] {c.isCentered(), c.isLeftJustified(),c.isRightJustified(),c.isMiddle(),c.isTopAligned(),c.isBottomAligned()});
	                alCons.put(con.generateKey(), con);
	                alignmentConstraints.put(con.generateKey(), new int[]{this.widths[0],0}, con);
	            }
	            else {
	                Sibling s2 = (Sibling) e;
	                AlignmentConstraint con = new AlignmentConstraint(this.nodes.get(e.getNode1().getxPath()), this.nodes.get(e.getNode2().getxPath()), Type.SIBLING, this.widths[0], 0,
	                        new boolean[] {s2.isTopBottom(),s2.isBottomTop(),s2.isRightLeft(),s2.isLeftRight(), s2.isTopEdgeAligned(),s2.isBottomEdgeAligned(),s2.isLeftEdgeAligned(), s2.isRightEdgeAligned()});
	                alCons.put(con.generateKey(), con);
	                alignmentConstraints.put(con.generateKey(), new int[] {this.widths[0],0}, con);
	            }
        	} catch (Exception e) {
        		
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
            int i = 0;
            for (String s : this.nodes.keySet()) {

                i++;
                try {
                    n = this.nodes.get(s);

                    if (n.parentConstraints.size() > 0) {
                        ArrayList<int[]> widths = getWidthsForConstraints(n.getParentConstraints());
                        for (int y = 0; y < widths.size(); y++) {
                            String parentXpath = n.getParentConstraints().get(y).node1.xpath;

                            int[] validWidths = widths.get(y);
                            int[] widthsTemp = new int[validWidths.length];
                            int[] parentWidths = new int[validWidths.length];
                            int[] childWidths = new int[validWidths.length];
//                            doms = Redecheck.loadDoms(validWidths, url);
                            // Gather parent and child widths
                            populateWidthArrays(validWidths, widthsTemp, parentWidths, childWidths, s, parentXpath);


                            // Get the equations
                            double[] bestFit;
                            int previousBreakpoint = n.getParentConstraints().get(y).getMin()-1;
                            while (parentWidths.length >= 2) {
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
//                    e.printStackTrace();
                }
                double progressPerc = ((double) (i)/ (double)this.nodes.size())* 100;
                System.out.print("\rPROGRESS : | " + StringUtils.repeat("=", (int)progressPerc) + StringUtils.repeat(" ", 100 - (int)progressPerc) + " | " +(int) progressPerc + "%");
            }
        System.out.println("");
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

    public void populateWidthArrays(int[] validWidths, int[] widthsTemp, int[] parentWidths, int[] childWidths, String s, String parentXpath) {
        for (int i = 0; i < validWidths.length; i++) {
            try {
//                DomNode dn = doms.get(validWidths[i]);
                AlignmentGraphFactory agf = getAlignmentGraphFactory(validWidths[i]);
                ag = agf.getAg();
                widthsTemp[i] = validWidths[i];
                HashMap<String, DomNode> dnMap = agf.getDomNodeMap();
                DomNode p = dnMap.get(parentXpath);
                DomNode c = dnMap.get(s);
                parentWidths[i] = p.getCoords()[2] - p.getCoords()[0];
                childWidths[i] = c.getCoords()[2] - c.getCoords()[0];
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
    }
    
    public AlignmentGraphFactory getAlignmentGraphFactory(int width) {
    	AlignmentGraphFactory agf = factories.get(width);
    	if (agf != null) {
    		return agf;
    	} else {
    		AlignmentGraphFactory newAGF = new AlignmentGraphFactory(doms.get(width));
    		
    		factories.put(width, newAGF);
    		return newAGF;	
    	}
    }

//    public AlignmentGraphFactory getAlignmentGraphFactory(DomNode dn) {
//        return new AlignmentGraphFactory(dn);
//    }

    public AlignmentGraph getAlignmentGraph(DomNode dn) {
        return new AlignmentGraph(dn);
    }

/*
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
     *//*
    public void printWidthConstraints(HashBasedTable<String, int[], WidthConstraint> cons) {
        for (WidthConstraint wc : cons.values()) {
            System.out.println(wc);
        }
    }
*/
    /*
    /**
     * Prints the nodes to the terminal for debugging purposes
     *//*
    private void printNodes() {
        for (Node n : this.nodes.values()) {
            System.out.println(n);
            for (WidthConstraint wc : n.getWidthConstraints()) {
                System.out.println(wc);
            }
        }
    }
*/
    /**
     * Goes through the full set of alignment constraints and adds the parent-child constraints to the node representing
     * the child element, for use in the width constraint extraction
     */
    public void addParentConstraintsToNodes() {
        for (AlignmentConstraint ac : this.alignmentConstraints.values()) {
        	try {
	            if (ac.type == Type.PARENT_CHILD) {
	                Node child = this.nodes.get(ac.node2.getXpath());
	                child.addParentConstraint(ac);
	            }
        	} catch (NullPointerException e) {
        		System.out.println("Tried adding parent constraint with " + ac);
        	}
        }
    }

    /**
     * Goes through the full set of extracted width constraints and adds them to the relevant nodes in the graph
     */
    public void addWidthConstraintsToNodes() {
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
        if (!searchForNode) {
//            System.out.println(min + " " + max);
//            System.out.println(searchKey.contains("contains"));
        }
        if (max-min==1) {
            int[] extraWidths = new int[] {min,max};
            ArrayList<AlignmentGraphFactory> extraGraphs = new ArrayList<AlignmentGraphFactory>();
//            if ( (!alreadyGathered.contains(min)) || (!alreadyGathered.contains(max)) ) {
//                Redecheck.capturePageModel(url, extraWidths, doms);
//                alreadyGathered.add(min);
//                alreadyGathered.add(max);
//            }
            captureExtraDoms(extraWidths);
//            tempDoms = Redecheck.loadDoms(extraWidths, url);

//            for (int w : extraWidths) {
////                DomNode dn = tempDoms.get(w);
//                AlignmentGraphFactory agf = getAlignmentGraphFactory(w);
//                extraGraphs.add(agf);
//            }
//            AlignmentGraphFactory ag1 = extraGraphs.get(0);
//            AlignmentGraphFactory ag2 = extraGraphs.get(1);
            AlignmentGraphFactory ag1 = getAlignmentGraphFactory(min);
            AlignmentGraphFactory ag2 = getAlignmentGraphFactory(max);

            boolean found1=false,found2 = false;

            if (searchForNode) {
                HashMap<String, DomNode> n1 = ag1.getDomNodeMap();
                HashMap<String, DomNode> n2 = ag2.getDomNodeMap();

                found1 = n1.get(searchKey) != null;
                found2 = n2.get(searchKey) != null;

                // Searching for parent-child edge
            } else {
                HashMap<String, AGEdge> e1 = ag1.getEdgeMap();
                HashMap<String, AGEdge> e2 = ag2.getEdgeMap();

                found1 = (e1.get(searchKey) != null) || (e1.get(flippedKey) != null);
                found2 = (e2.get(searchKey) != null) || (e2.get(flippedKey) != null);
            }
//            System.out.println("Done");
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
//            if (!alreadyGathered.contains(mid)) {
//                Redecheck.capturePageModel(url, extraWidths, doms);
//                alreadyGathered.add(mid);
//            }
            captureExtraDoms(extraWidths);
//            tempDoms = Redecheck.loadDoms(extraWidths, url);
//            DomNode dn = tempDoms.get(mid);

            AlignmentGraphFactory extraAG = getAlignmentGraphFactory(mid);
            boolean found = false;
            if (searchForNode) {
                HashMap<String, DomNode> n1 = extraAG.getDomNodeMap();
                found = n1.get(searchKey) != null;
            } else {
                HashMap<String, AGEdge> es = extraAG.getEdgeMap();
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
//        System.out.println(min + " " + max);
        if (max-min==1) {
            int[] extraWidths = new int[] {min,max};
            ArrayList<AlignmentGraphFactory> extraGraphs = new ArrayList<AlignmentGraphFactory>();
//            if ( (!alreadyGathered.contains(min)) || (!alreadyGathered.contains(max)) ) {
//                Redecheck.capturePageModel(url, extraWidths, doms);
//                alreadyGathered.add(min);
//                alreadyGathered.add(max);
//            }
            captureExtraDoms(extraWidths);
//            tempDoms = Redecheck.loadDoms(extraWidths, url);

//            for (int w : extraWidths) {
////                DomNode dn = tempDoms.get(w);
//                AlignmentGraphFactory ag =  getAlignmentGraphFactory(w);
//                extraGraphs.add(ag);
//            }
            AlignmentGraphFactory ag1 = getAlignmentGraphFactory(min);
            AlignmentGraphFactory ag2 = getAlignmentGraphFactory(max);
            boolean found1=false,found2 = false;

            if (searchForNode) {
                HashMap<String, DomNode> n1 = ag1.getDomNodeMap();
                HashMap<String, DomNode> n2 = ag2.getDomNodeMap();

                found1 = n1.get(searchKey) != null;
                found2 = n2.get(searchKey) != null;
            } else {
                HashMap<String, AGEdge> e1 = ag1.edgeMap;
                HashMap<String, AGEdge> e2 = ag2.edgeMap;
                found1 = (e1.get(searchKey) != null) || (e1.get(flippedKey) != null);
                found2 = (e2.get(searchKey) != null) || (e2.get(flippedKey) != null);
            }
//            System.out.println("Done");
            return decideBreakpoint(min, max, found1, found2);

        } else {
            int mid = (max+min)/2;
            int[] extraWidths = new int[] {mid};
            captureExtraDoms(extraWidths);
//            if (!alreadyGathered.contains(mid)) {
//                Redecheck.capturePageModel(url, extraWidths, doms);
//                alreadyGathered.add(mid);
//            }
//            tempDoms = Redecheck.loadDoms(extraWidths, url);
//            DomNode dn = tempDoms.get(extraWidths[0]);

            AlignmentGraphFactory extraAG = getAlignmentGraphFactory(mid);
            boolean found;

            if (searchForNode) {
                HashMap<String, AGNode> n1 = extraAG.getNodeMap();
                found = n1.get(searchKey) != null;
            } else {
                HashMap<String, AGEdge> es = extraAG.edgeMap;
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
    public ArrayList<int[]> getWidthsForConstraints(ArrayList<AlignmentConstraint> acs) {
        ArrayList<int[]> widthSets = new ArrayList<int[]>();
        TreeMap<Integer, AlignmentConstraint> ordered = new TreeMap<Integer, AlignmentConstraint>();
        for (AlignmentConstraint c : acs) {
            ordered.put(c.min,c);
        }
//        int numParents = 0;
        String previousParent = null;
//        HashMap<Integer, AlignmentConstraint> parentBreakpoints = new HashMap<Integer, AlignmentConstraint>();
        HashMap<String, int[]> parentRanges = new HashMap<>();


        // Get all the different parents
        for (AlignmentConstraint c : ordered.values()) {
            if (!c.node1.xpath.equals(previousParent)) {
//                parentBreakpoints.put(c.min, c);
//                previousParent = c.node1.getXpath();

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
            widthSets.add(widthSets.size(), widthArray);
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
    public int findWidthBreakpoint(double[] eq, int min, int max, String child, String parent) throws InterruptedException {
        if (max-min == 1) {
            int[] extraWidths = new int[] {min,max};
            ArrayList<AlignmentGraphFactory> extraGraphs = new ArrayList<AlignmentGraphFactory>();
//            if ( (!alreadyGathered.contains(min)) || (!alreadyGathered.contains(max)) ) {
//                Redecheck.capturePageModel(url, extraWidths, doms);
//                alreadyGathered.add(min);
//                alreadyGathered.add(max);
//            }
//            tempDoms = Redecheck.loadDoms(extraWidths, url);
            captureExtraDoms(extraWidths);
//            for (int w : extraWidths) {
////                DomNode dn = tempDoms.get(w);
//                AlignmentGraphFactory ag = getAlignmentGraphFactory(w);
//                extraGraphs.add(ag);
//            }
            AlignmentGraphFactory ag1 = getAlignmentGraphFactory(min);
            AlignmentGraphFactory ag2 = getAlignmentGraphFactory(max);

            Map<String, DomNode> map1 = ag1.getDomNodeMap();
            Map<String, DomNode> map2 = ag2.getDomNodeMap();

            DomNode c1 = map1.get(child);
            DomNode p1 = map1.get(parent);
            DomNode c2 = map2.get(child);
            DomNode p2 = map2.get(parent);

            int c1w = getWidthOfDomNode(c1);
            int p1w = getWidthOfDomNode(p1);
            int c2w = getWidthOfDomNode(c2);
            int p2w = getWidthOfDomNode(p2);
            boolean result1 = Math.abs((eq[0]*c1w) - ((eq[1]*p1w) + (eq[2]))) <=5;
            boolean result2 = Math.abs((eq[0]*c2w) - ((eq[1]*p2w) + (eq[2]))) <=5;

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
//            if (!alreadyGathered.contains(mid)) {
//                Redecheck.capturePageModel(url, extraWidths, doms);
//                alreadyGathered.add(mid);
//            }
            captureExtraDoms(extraWidths);
            AlignmentGraphFactory extraAG = getAlignmentGraphFactory(mid);

            Map<String, DomNode> map1 = extraAG.getDomNodeMap();

            DomNode c1 = map1.get(child);
            DomNode p1 = map1.get(parent);

            // Get parent and child widths
            int c = getWidthOfDomNode(c1);
            int p = getWidthOfDomNode(p1);

            // Check whether mid falls on the equation's line or not.
            boolean result = Math.abs((eq[0]*c) - ((eq[1]*p) + (eq[2]))) <=5;

            // Check which way to recurse
            if (result) {
                // Breakpoint is higher as mid is on the line
                return findWidthBreakpoint(eq, mid, max, child, parent);
            } else {
                // Breakpoint is lower as mid is not on the line (already passed breakpoint)
                return findWidthBreakpoint(eq, min, mid, child, parent);
            }
        }
        return (min+max)/2;
    }

    public int getWidthOfDomNode(DomNode dn) {
        return dn.getCoords()[2]-dn.getCoords()[0];
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
//            e.printStackTrace();
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

    public void captureExtraDoms(int[] widths) {
    	if (widths.length == 2) {
	    	if ((!alreadyGathered.contains(widths[0])) && (!alreadyGathered.contains(widths[1]))) {
	    		Redecheck.capturePageModel(url, widths, doms);
	    		alreadyGathered.add(widths[0]);
	    		alreadyGathered.add(widths[1]);
	    	} else if (!alreadyGathered.contains(widths[0])) {
	    		Redecheck.capturePageModel(url, new int[]{widths[0]}, doms);
	    		alreadyGathered.add(widths[0]);
	    	} else if (!alreadyGathered.contains(widths[1])) {
	    		Redecheck.capturePageModel(url, new int[]{widths[1]}, doms);
	    		alreadyGathered.add(widths[1]);
	    	}
    	} else if (widths.length == 1) {
    		if (!alreadyGathered.contains(widths[0])) {
	    		Redecheck.capturePageModel(url, new int[]{widths[0]}, doms);
	    		alreadyGathered.add(widths[0]);
    		}
    	}
    }
}
