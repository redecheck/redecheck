package shef.rlg;

import com.google.common.collect.HashBasedTable;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import shef.layout.*;
import shef.layout.Sibling;
import shef.main.Tool;
import edu.gatech.xpert.dom.*;
import edu.gatech.xpert.dom.layout.*;
import java.util.*;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

import shef.utils.StopwatchFactory;

/**
 * Created by thomaswalsh on 10/08/15.
 * Last modified on 07/09/15.
 */
public class ResponsiveLayoutGraph {
    // Initialising various variables needed throughout the model construction process
    HashMap<String, Node> nodes = new HashMap<String, Node>();
    private HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints = HashBasedTable.create();

    public HashMap<String, VisibilityConstraint> getVisCons() {
        return visCons;
    }

    HashMap<String, VisibilityConstraint> visCons = new HashMap<>();
    ArrayList<AlignmentGraphFactory> graphs;
    AlignmentGraphFactory first, last;
    LayoutFactory firstLF, lastLF;
    ArrayList<LayoutFactory> layouts;
    AlignmentGraph ag;
    ResponsiveLayoutGraph oracle;
    boolean binarySearch;
    StopwatchFactory swf;
    ArrayList<AlignmentGraphFactory> restOfGraphs;
    ArrayList<LayoutFactory> restOfLayouts;
    String url;
    HashMap<Integer, DomNode> doms;
    HashMap<Integer, LayoutFactory> lFactories;
    Map<Integer, DomNode> tempDoms;
    HashMap<Integer, DomNode> oracleDoms;
    HashMap<Integer, AlignmentGraphFactory> factories;
    int[] widths;
    int[] restOfWidths;
    static HashSet<Integer> alreadyGathered;
    PhantomJSDriver driver;
    WebDriver wdriver;
    int sleep;


    public ResponsiveLayoutGraph() {
        alreadyGathered = new HashSet<Integer>();
        last = null;
        // For testing purposes only
    }
    public ResponsiveLayoutGraph(ArrayList<LayoutFactory> layouts, int[] stringWidths, String url, HashMap<Integer, LayoutFactory> facts, boolean bs, WebDriver driver, StopwatchFactory swf, int sl) throws InterruptedException {
        this.swf = swf;
        this.layouts = layouts;
        this.firstLF = layouts.get(0);
        this.lastLF = layouts.get(layouts.size()-1);
        this.wdriver = driver;
        this.sleep = sl;
        binarySearch = bs;
        restOfGraphs =  new ArrayList<AlignmentGraphFactory>();
        restOfLayouts = new ArrayList<>();
        factories = new HashMap<Integer, AlignmentGraphFactory>();
        lFactories = new HashMap<>();
        for (LayoutFactory lf : layouts) {
            restOfLayouts.add(lf);
            lFactories.put(stringWidths[layouts.indexOf(lf)], lf);
        }
        restOfLayouts.remove(0);
        this.url = url;
        this.lFactories = facts;
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
        extractAlignmentConstraints();
//        printAlignmentConstraints();
//
//        System.out.println(this.getNodes().get("/HTML/BODY/DIV[3]/DIV/DIV/DIV/DIV[2]/DIV/DIV[2]/DIV/DIV[6]/DIV/DIV/DIV"));
//        System.out.println(this.getNodes().get("/HTML/BODY/DIV[3]/DIV/DIV/DIV/DIV[2]/DIV/DIV[2]/DIV/DIV[6]/DIV/DIV/DIV/H4"));

    }

    /**
     * Prints the alignment constraints to the terminal for debugging purposes
     */
    public void printAlignmentConstraints() {
//        System.out.println("SIBLINGS");
        for (String s : this.getAlignmentConstraints().rowKeySet()) {
            Map<int[],AlignmentConstraint> map = this.getAlignmentConstraints().row(s);

            for (AlignmentConstraint ac : map.values()) {
//                if ((ac.getMax() < 780) && (ac.getMin() > 760))
//                if (ac.getType() == Type.SIBLING) {
                    if (ac.getNode1().getXpath().contains("/HTML/BODY/DIV[3]/DIV/DIV/DIV/DIV[2]/DIV/DIV[2]/DIV/DIV[6]/DIV/DIV/DIV") ||
                            ac.getNode2().getXpath().contains("/HTML/BODY/DIV[3]/DIV/DIV/DIV/DIV[2]/DIV/DIV[2]/DIV/DIV[6]/DIV/DIV/DIV")) {
                        System.out.println(ac);
                    }
//                }
            }
        }
//        System.out.println("PARENT-CHILDS");
//        for (String s : this.getAlignmentConstraints().rowKeySet()) {
//            Map<int[], AlignmentConstraint> map = this.getAlignmentConstraints().row(s);
//            for (AlignmentConstraint ac : map.values()) {
//                if (ac.getType() == Type.PARENT_CHILD) {
//                    if (ac.getNode2().getXpath().equals("/HTML/BODY/DIV/DIV[2]/DIV/DIV[2]/DIV/DIV[2]/DIV/DIV")) {
//                        System.out.println(ac);
//                    }
//                }
//            }
//        }
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

    /**
     * Extracts all the visibility constraints for each node on the webpage by inspecting which elements are visible at
     * which resolutions.
     * @throws InterruptedException
     */
    private void extractVisibilityConstraints() throws InterruptedException {


        HashMap<String, Element> curr = firstLF.getElementMap();
        HashMap<String, Element> prev = firstLF.getElementMap();
        HashMap<String, Element> prevToMatch, currToMatch;

        setUpVisibilityConstraints(curr, visCons);

        for (LayoutFactory lf : restOfLayouts) {
            prevToMatch = (HashMap<String, Element>) prev.clone();
            curr = (HashMap<String, Element>) lf.getElementMap();
            currToMatch = (HashMap<String, Element>) curr.clone();

            // Matches the nodes seen at the last sample point to those seen at the current one
            checkForNodeMatch(prev, curr, prevToMatch, currToMatch);

            // Handle any disappearing elements
            updateDisappearingNode(prevToMatch, visCons, lf);

            // Handle any appearing elements
            updateAppearingNode(currToMatch, visCons, lf);

            // Update the previousMap variable to keep track of last set of nodes
            prev = (HashMap<String, Element>) lf.getElementMap();
        }
        
        // Update visibility constraints of everything still visible
        updateRemainingNodes(visCons, lastLF);

        // Attach constraints to the nodes
        attachVisConsToNodes(visCons);
    }

    /**
     * Takes a map of visibility constraints and adds them to the relevant nodes
     * @param visCons	the map of constraints to be added
     */
    public void attachVisConsToNodes(HashMap<String, VisibilityConstraint> visCons) {
        for (String x : this.nodes.keySet()) {
            Node n = this.nodes.get(x);
            VisibilityConstraint vc = visCons.get(x);
            n.addVisibilityConstraint(vc);
        }
    }

    /**
     * Updates the visibility constraints of any nodes visible at the final sample point
     * @param visCons	the visibility constraints that may need to be updated
     * @param last		the data from the final sample point
     */
    public void updateRemainingNodes(HashMap<String, VisibilityConstraint> visCons, LayoutFactory last) {
        for (String stilVis : last.layout.getElements().keySet()) {
            VisibilityConstraint vc = visCons.get(stilVis);
            if (vc.getDisappear() == 0) {
                vc.setDisappear(widths[widths.length - 1]);
            }
        }
    }

    /**
     * Takes a set of nodes appearing at a given sample point and created visibility constraints and node objects for each
     * @param tempToMatch	the set of appearing nodes
     * @param visCons		the map of visibility constraints to add to
     * @param lf			the LayoutFactory containing the appearing nodes
     */
    public void updateAppearingNode(HashMap<String, Element> tempToMatch, HashMap<String, VisibilityConstraint> visCons, LayoutFactory lf) {
        // Iterate through all appearing nodes
    	for (String currUM : tempToMatch.keySet()) {
            int appearPoint = 0;
            try {
            	// Find the point at which it appears
                if (binarySearch) {
                    appearPoint = searchForLayoutChange(currUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], true, "", true);
                } else {
                    appearPoint = widths[restOfLayouts.indexOf(lf) + 1];
                }
            } catch (InterruptedException e) {
            	
            }
            
            // Create a node object and a matching visibility constraint for the appearing element
            nodes.put(currUM, new Node(currUM));
            visCons.put(currUM, new VisibilityConstraint(appearPoint, 0));
        }
    }

    /**
     * Takes a set of disappearing elements and updates the visibility constraints linked to them
     * @param previousToMatch	the set of disappearing nodes
     * @param visCons			the map of visibility constraints to update
     * @param lf				the LayoutFactory containing the disappearing nodes
     */
    public void updateDisappearingNode(HashMap<String, Element> previousToMatch, HashMap<String, VisibilityConstraint> visCons, LayoutFactory lf) {
        // Iterate through all disappearing nodes
    	for (String prevUM : previousToMatch.keySet()) {
            int disappearPoint = 0;
            try {
            	// Find the point at which it disappears
//                if (binarySearch) {
//                    disappearPoint = findDisappearPoint(prevUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], true, "");
//                } else {
//                    disappearPoint = widths[restOfLayouts.indexOf(lf) + 1];
//                }
                disappearPoint = searchForLayoutChange(prevUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], true, "", false);
            } catch (InterruptedException e) {
            }
            
            // Get the existing visibility constraint for the node and update it with the disappearPoint
            VisibilityConstraint vc = visCons.get(prevUM);
            vc.setDisappear(disappearPoint - 1);
        }
    }

    /**
     * Takes the set of nodes from two consecutive sample points and matches the nodes
     * @param previousMap		the previous set of nodes
     * @param temp				the current set of nodes
     * @param previousToMatch	a copy of previousMap used for matching
     * @param tempToMatch		a copy of temp used for matching
     */
    public void checkForNodeMatch(HashMap<String, Element> previousMap, HashMap<String, Element> temp, HashMap<String, Element> previousToMatch, HashMap<String, Element> tempToMatch) {
        // Iterate through all nodes in the previous map
    	for (String s : previousMap.keySet()) {
    		// See if that node is visible in the current map
            if (temp.get(s) != null) {
                // If so, remove the matched nodes from their respective sets
                previousToMatch.remove(s);
                tempToMatch.remove(s);
            }
        }
    }

    /**
     * Sets up visibility constraints for all nodes visible at the first sample point
     * @param elements		the map of elements visible at the first sample point
     * @param cons			the map into which to add the new constraints
     */
    public void setUpVisibilityConstraints(HashMap<String, Element> elements, HashMap<String, VisibilityConstraint> cons) {
        // Iterate through all elements
    	for (Element e : elements.values()) {
//            for (String s : e.getStyles().keySet()) {
//                System.out.println(s + " : " + e.getStyles().get(s));
//            }

            // Add each node to overall set
            String xpath = e.getXpath();
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
        HashMap<String, Relationship> prev = firstLF.getRelationships();
        HashMap<String, Relationship> curr = firstLF.getRelationships();
        HashMap<String, AlignmentConstraint> alCons = new HashMap<String, AlignmentConstraint>();
        // Add initial edges to set.
        setUpAlignmentConstraints(prev, alCons);
        int currentWidth = this.widths[0];
        int prevWidth = this.widths[0];

        HashMap<String, Relationship> prevToMatch, currToMatch;

        for (LayoutFactory lf : restOfLayouts) {
            currentWidth = this.widths[restOfLayouts.indexOf(lf)+1];
//            System.out.println(currentWidth);
//            if (currentWidth == 1400) {
//                System.out.println();
//            }
            prevToMatch = (HashMap<String, Relationship>) prev.clone();
            curr = lf.getRelationships();
            currToMatch = (HashMap<String, Relationship>) curr.clone();

            // Match the edges visible at both sample points
            checkForEdgeMatch(prev, prevToMatch, curr, currToMatch);
//            System.out.println("MATCHED EDGES");

            // Pair any unmatched edges and update the alignment constraints
            HashMap<Relationship, Relationship> matchedChangingEdges = pairUnmatchedEdges(prevToMatch, currToMatch);
            updatePairedEdges(matchedChangingEdges, getAlignmentConstraints(), alCons, lf);
//            System.out.println("PAIRED EDGES");

            // If there are still some disappearing edges left
            if (prevToMatch.size() != 0) {
            	// Check whether the edge has disappeared because one of the nodes has
            	checkForNodeBasedDisappearances(prevToMatch, getAlignmentConstraints(), prevWidth, currentWidth);

            	// Update any remaining disappearing edges
            	updateDisappearingEdges(prevToMatch, getAlignmentConstraints(), lf);
            }
//            System.out.println("DISAPPEARED EDGES");

            // If there are still appearing edges left
            if (currToMatch.size() !=0) {
            	// Check whether the edge has appeared because one of the nodes has
            	checkForNodeBasedAppearances(currToMatch, getAlignmentConstraints(), alCons, prevWidth, currentWidth);

            	// Update any remaining appearing edges
            	updateAppearingEdges(currToMatch, getAlignmentConstraints(), alCons, lf);
            }
//            System.out.println("APPEARED EDGES");
            prev = lf.getRelationships();
            prevWidth = currentWidth;
        }

        // Update alignment constraints of everything still visible
        LayoutFactory last = restOfLayouts.get(restOfLayouts.size()-1);
        updateRemainingEdges(alCons, last);
        addParentConstraintsToNodes();
//        this.alignments = alCons;
//        System.out.println("FINISHED");
    }




    private HashMap<Relationship, Relationship> pairUnmatchedEdges(HashMap<String, Relationship> previous, HashMap<String, Relationship> curr) {
        HashMap<String, Relationship> previousToMatch = (HashMap<String, Relationship>) previous.clone();
        HashMap<String, Relationship> currToMatch = (HashMap<String, Relationship>) curr.clone();

        HashMap<Relationship, Relationship> paired = new HashMap<>();
        for (String s : previousToMatch.keySet()) {
            Relationship r = previousToMatch.get(s);

            Element e1 = r.getNode1();
            Element e2 = r.getNode2();
            for (String s2 : currToMatch.keySet()) {
                Relationship r2 = currToMatch.get(s2);
                Element e1m = r2.getNode1();
                Element e2m = r2.getNode2();

                // Checks to see if both node1 and node2 are the same.
                if ( (e1.getXpath().equals(e1m.getXpath())) && (e2.getXpath().equals(e2m.getXpath())) && (r.getClass() == r2.getClass())) {

//                    System.out.println("PAIRED " + r + " and " +r2);
                    paired.put(r, r2);
                    previous.remove(s);
                    curr.remove(s2);
                // Check for the flipped sibling node match
                } else if ( (e1.getXpath().equals(e2m.getXpath())) && (e2.getXpath().equals(e1m.getXpath())) && (r.getClass() == r2.getClass()) && (r instanceof Sibling)) {
                    paired.put(r, r2);
                    previous.remove(s);
                    curr.remove(s2);
                // Check for matching child, but differing parents
                } else if ( ((!e1.getXpath().equals(e1m.getXpath())) && (e2.getXpath().equals(e2m.getXpath())) && (r instanceof ParentChild) && (r2 instanceof ParentChild)) ) {
//
                    paired.put(r, r2);
                    previous.remove(s);
                    curr.remove(s2);
                }
            }
        }

        return paired;
    }

    private void updatePairedEdges(HashMap<Relationship, Relationship> matchedChangingEdges, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, HashMap<String, AlignmentConstraint> alCons, LayoutFactory lf) {
        for (Relationship e : matchedChangingEdges.keySet()) {
            String pairedkey1 = LayoutFactory.generateKey(e);
            Relationship matched = matchedChangingEdges.get(e);

//            if (e.getNode2().getXpath().equals("/HTML/BODY/DIV[2]/DIV/DIV[11]") && e.getNode1().getXpath().contains("DIV[17]")) {
//                System.out.println("PAIRING " + e);
//            }
//            if (matched.getNode2().getXpath().equals("/HTML/BODY/DIV[2]/DIV/DIV/DIV[2]/DIV[2]/DIV[2]/DIV/DIV/DIV/DIV[6]")) {
//                System.out.println(e);
//            }

            int disappearPoint = 0;
            String flip="";

            if (e instanceof Sibling) {
                Sibling s2 = (Sibling) e;
                flip = s2.getNode2().getXpath()+" sibling of " +s2.getNode1().getXpath()+LayoutFactory.generateFlippedLabelling(s2);
            }
            try {
                disappearPoint = searchForLayoutChange(pairedkey1, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], false, flip, false);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(pairedkey1);
            Map<int[], AlignmentConstraint> cons2 = alignmentConstraints.row(flip);

            if (cons.size() > 0) {
                updateAlignmentConstraint(cons, disappearPoint-1);
//
            }
            if ((cons2.size() > 0)) {
                updateAlignmentConstraint(cons2, disappearPoint-1);
            }

            // Create the appearing AC (matched)
            AlignmentConstraint ac;
            Type t;
            if (matched instanceof ParentChild) {
                t = Type.PARENT_CHILD;
                ParentChild c = (ParentChild) matched;
                ac = new AlignmentConstraint(this.nodes.get(matched.getNode1().getXpath()), this.nodes.get(matched.getNode2().getXpath()), t, disappearPoint, 0,
                        new boolean[]{c.isCentreJust(), c.isLeftJust(), c.isRightJust(), c.isMiddleJust(), c.isTopJust(), c.isBottomJust()}, new boolean[] {c.ishFill(), c.isvFill()});
            } else {
                t = Type.SIBLING;
                Sibling s2 = (Sibling) matched;
                ac = new AlignmentConstraint(this.nodes.get(matched.getNode1().getXpath()), this.nodes.get(matched.getNode2().getXpath()), t, disappearPoint, 0,
                        new boolean[]{s2.isAbove(),s2.isBelow(),s2.isLeftOf(),s2.isRightOf(), s2.isTopEdge(),s2.isBottomEdge(),s2.isyMid(),s2.isLeftEdge(), s2.isRightEdge(),s2.isxMid(), s2.isOverlapping()}, null);

            }
            alCons.put(ac.generateKey(), ac);
            alignmentConstraints.put(ac.generateKey(), new int[]{disappearPoint,0}, ac);
        }

    }

    public void updateRemainingEdges(HashMap<String, AlignmentConstraint> alCons, LayoutFactory last) {
        HashMap<String, Relationship> rels = last.getRelationships();
        for (String stilVis : rels.keySet()) {
            Relationship r = rels.get(stilVis);
            if (r instanceof ParentChild) {
                ParentChild cTemp = (ParentChild) r;
//                AlignmentConstraint ac = alCons.get(stilVis);
//                if (ac != null) {
                Map<int[], AlignmentConstraint> cons = getAlignmentConstraints().row(stilVis);
                if (cons.size() != 0) {
                    updateAlignmentConstraint(cons, widths[widths.length-1]);
                }
            } else {
                Sibling s = (Sibling) r;

                String flipped = s.getNode2().getXpath() +" sibling of "+ s.getNode1().getXpath()+LayoutFactory.generateFlippedLabelling(s);

                Map<int[], AlignmentConstraint> cons = getAlignmentConstraints().row(stilVis);
                Map<int[], AlignmentConstraint> cons2 = getAlignmentConstraints().row(flipped);

                if (cons.size() != 0) {
                    updateAlignmentConstraint(cons, widths[widths.length-1]);
                } else if (cons2.size() != 0) {
                    updateAlignmentConstraint(cons2, widths[widths.length-1]);
                }
            }
        }
    }



    private void updateAlignmentConstraint(Map<int[], AlignmentConstraint> cons, int disappearPoint) {
        for (int[] pair : cons.keySet()) {
            // Get the one without a max value
            if (pair[1] == 0) {
                AlignmentConstraint aCon = cons.get(pair);
                aCon.setMax(disappearPoint);
                pair[1] = disappearPoint;
            }
        }
    }

    public void updateAppearingEdges(HashMap<String, Relationship> tempToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, HashMap<String, AlignmentConstraint> alCons, LayoutFactory lf) {
        for (String currUM : tempToMatch.keySet()) {
            Relationship e = tempToMatch.get(currUM);
            int appearPoint = 0;
            Type t;
            AlignmentConstraint ac;
            if (e instanceof ParentChild) {
                ParentChild c = (ParentChild) e;
                try {
                    if (binarySearch) {
                        appearPoint = searchForLayoutChange(currUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], false, "", true);
                    } else {
                        appearPoint = widths[restOfLayouts.indexOf(lf) + 1];
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                t = Type.PARENT_CHILD;
                ac = new AlignmentConstraint(this.nodes.get(e.getNode1().getXpath()), this.nodes.get(e.getNode2().getXpath()), t, appearPoint, 0,
                        new boolean[]{c.isCentreJust(), c.isLeftJust(), c.isRightJust(), c.isMiddleJust(), c.isTopJust(), c.isBottomJust()}, new boolean[] {c.ishFill(), c.isvFill()});
            }
            else {
                t = Type.SIBLING;
                Sibling s2 = (Sibling) e;
                String flip = s2.getNode2().getXpath()+" sibling of " + s2.getNode1().getXpath() + LayoutFactory.generateFlippedLabelling(s2);
                try {
                    if (binarySearch) {
                        appearPoint = searchForLayoutChange(currUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], false, flip, true);
                    } else {
                        appearPoint = widths[restOfLayouts.indexOf(lf) + 1];
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                ac = new AlignmentConstraint(this.nodes.get(e.getNode1().getXpath()), this.nodes.get(e.getNode2().getXpath()), t, appearPoint, 0,
                        new boolean[]{s2.isAbove(),s2.isBelow(),s2.isLeftOf(),s2.isRightOf(), s2.isTopEdge(),s2.isBottomEdge(),s2.isyMid(),s2.isLeftEdge(), s2.isRightEdge(),s2.isxMid(), s2.isOverlapping()}, null);

            }
            if (ac != null) {
                alCons.put(ac.generateKey(), ac);
                alignmentConstraints.put(ac.generateKey(), new int[]{appearPoint,0}, ac);
            }

        }
    }

    public void updateDisappearingEdges(HashMap<String, Relationship> previousToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, LayoutFactory lf) {
        for (String prevUM : previousToMatch.keySet()) {
            Relationship e = previousToMatch.get(prevUM);
            int disappearPoint = 0;
            String flip="";
            if (e instanceof ParentChild) {
                try {
                    disappearPoint = searchForLayoutChange(prevUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], false, "", false);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } else {
                Sibling s2 = (Sibling) e;
                flip = s2.getNode2().getXpath()+" sibling of " + s2.getNode1().getXpath() +LayoutFactory.generateFlippedLabelling(s2);
                try {
                    disappearPoint = searchForLayoutChange(prevUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], false, flip, false);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

            Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(prevUM);
            Map<int[], AlignmentConstraint> cons2 = alignmentConstraints.row(flip);
            if (cons.size() > 0) {
                updateAlignmentConstraint(cons, disappearPoint-1);
            } else if (cons2.size() > 0) {
                updateAlignmentConstraint(cons2, disappearPoint-1);
            } else {
                System.out.println("Couldn't find existing constraint for " + prevUM);
            }
        }
    }

    private void checkForNodeBasedDisappearances(HashMap<String, Relationship> previousToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, int min, int max) {
        Element node1, node2;
        String flip;
        HashMap<String, Relationship> tempMap = (HashMap<String, Relationship>) previousToMatch.clone();
        for (String s : tempMap.keySet()) {
            flip="";
            Relationship rel = tempMap.get(s);
            if (rel instanceof Sibling) {
                Sibling s2 = (Sibling) rel;
                flip = s2.getNode2().getXpath() + " sibling of "+ s2.getNode1().getXpath()  + LayoutFactory.generateFlippedLabelling(s2);
            }
            node1 = rel.getNode1();
            node2 = rel.getNode2();
            if ( (nodeDisappears(node1, min, max)) || (nodeDisappears(node2, min, max)) ) {
                // Get VC of disappearing node
                VisibilityConstraint vc = this.nodes.get(node1.getXpath()).getVisibilityConstraints().get(0);
                int disappearPoint = vc.getDisappear();

                // Update with correct value
                Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(s);
                Map<int[], AlignmentConstraint> cons2 = alignmentConstraints.row(flip);
                if (cons.size() > 0) {
                    updateAlignmentConstraint(cons, disappearPoint);
                } else if (cons2.size() > 0) {
                    updateAlignmentConstraint(cons2, disappearPoint);
                }
            }
        }
    }

    private void checkForNodeBasedAppearances(HashMap<String, Relationship> tempToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints,HashMap<String, AlignmentConstraint> alCons, int min, int max) {
        Element node1, node2;
        String flip;
        HashMap<String, Relationship> tempMap = (HashMap<String, Relationship>) tempToMatch.clone();
        for (String s : tempMap.keySet()) {
            flip = "";
            Relationship edge = tempMap.get(s);
            if (edge instanceof Sibling) {
                Sibling s2 = (Sibling) edge;
                flip = s2.getNode2().getXpath() + " sibling of " + s2.getNode1().getXpath()  + LayoutFactory.generateFlippedLabelling(s2);
            }
            node1 = edge.getNode1();
            node2 = edge.getNode2();
            if ((nodeAppears(node1, min, max)) || (nodeAppears(node2, min, max))) {
                // Get VC of disappearing node
            	try {
	                VisibilityConstraint vc = this.nodes.get(node1.getXpath()).getVisibilityConstraints().get(0);
	                int appearPoint = vc.getAppear();

	                Type t = null;
	                AlignmentConstraint ac = null;
	                if (edge instanceof ParentChild) {
	                    ParentChild c = (ParentChild) edge;
	                    t = Type.PARENT_CHILD;
	                    ac = new AlignmentConstraint(this.nodes.get(edge.getNode1().getXpath()), this.nodes.get(edge.getNode2().getXpath()), t, appearPoint, 0,
	                            new boolean[]{c.isCentreJust(), c.isLeftJust(), c.isRightJust(), c.isMiddleJust(), c.isTopJust(), c.isBottomJust()}, new boolean[] {c.ishFill(), c.isvFill()});

                    }
	                else {
	                    t = Type.SIBLING;
	                    Sibling s2 = (Sibling) edge;

	                    ac = new AlignmentConstraint(this.nodes.get(edge.getNode1().getXpath()), this.nodes.get(edge.getNode2().getXpath()), t, appearPoint, 0,
	                            new boolean[]{s2.isAbove(),s2.isBelow(),s2.isLeftOf(),s2.isRightOf(), s2.isTopEdge(),s2.isBottomEdge(),s2.isyMid(),s2.isLeftEdge(), s2.isRightEdge(),s2.isxMid(), s2.isOverlapping()}, null);

	                }
	                if (ac != null) {
	                    alCons.put(ac.generateKey(), ac);
	                    alignmentConstraints.put(ac.generateKey(), new int[]{appearPoint,0}, ac);
	                    tempToMatch.remove(s);
	                    tempToMatch.remove(flip);
	                }
            	} catch (NullPointerException e) {
            	}
            }
        }
    }

    private boolean nodeAppears(Element node1, int min, int max) {
        Node n = this.nodes.get(node1.getXpath());
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

    private boolean nodeDisappears(Element node1, int min, int max) {
        Node n = this.nodes.get(node1.getXpath());
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


    public void checkForEdgeMatch(HashMap<String, Relationship> previousMap, HashMap<String, Relationship> previousToMatch, HashMap<String, Relationship> temp, HashMap<String, Relationship> tempToMatch) {
        String key = "", key2 = "";
        for (String sKey : previousMap.keySet()) {
            Relationship r = previousMap.get(sKey);

            if (r instanceof ParentChild) {
                ParentChild cTemp = (ParentChild) r;

                key = cTemp.getKey();

                if (temp.get(key) != null) {
                    previousToMatch.remove(key);
                    tempToMatch.remove(key);
                }
            } else {
                Sibling s = (Sibling) r;
                key = s.getKey();
                key2 = s.getFlipKey();
                if ((temp.get(key) != null)) {
                    previousToMatch.remove(key);
                    tempToMatch.remove(key);
                } else if (temp.get(key2) != null) {
                    previousToMatch.remove(key);
                    tempToMatch.remove(key2);
                }
            }

        }

    }

    public void setUpAlignmentConstraints(HashMap<String, Relationship> previousMap, HashMap<String, AlignmentConstraint> alCons) {
        for (String sKey : previousMap.keySet()) {
        	try {
	            Relationship r = previousMap.get(sKey);
	            if (r instanceof ParentChild) {
	                ParentChild pc = (ParentChild) r;
	                AlignmentConstraint con = new AlignmentConstraint(this.nodes.get(pc.getNode1().getXpath()), this.nodes.get(pc.getNode2().getXpath()), Type.PARENT_CHILD, this.widths[0], 0,
	                        new boolean[] {pc.isCentreJust(), pc.isLeftJust(),pc.isRightJust(),pc.isMiddleJust(),pc.isTopJust(),pc.isBottomJust()}, new boolean[] {pc.ishFill(), pc.isvFill()});
	                alCons.put(con.generateKey(), con);
	                getAlignmentConstraints().put(con.generateKey(), new int[]{this.widths[0],0}, con);
	            }
	            else {
	                Sibling s = (Sibling) r;
	                AlignmentConstraint con = new AlignmentConstraint(this.nodes.get(s.getNode1().getXpath()), this.nodes.get(s.getNode2().getXpath()), Type.SIBLING, this.widths[0], 0,
	                        new boolean[] {s.isAbove(), s.isBelow(), s.isLeftOf(),s.isRightOf(), s.isTopEdge(),s.isBottomEdge(),s.isyMid(),s.isLeftEdge(), s.isRightEdge(),s.isxMid(), s.isOverlapping()}, null);
	                alCons.put(con.generateKey(), con);
	                getAlignmentConstraints().put(con.generateKey(), new int[] {this.widths[0],0}, con);
	            }
        	} catch (Exception e) {
        		
        	}
        }
    }

//    /**
//     * Extracts the width constraints for all the nodes visible on the webpage across the range of resolutions sampled
//     * @throws InterruptedException
//     */
//    public void extractWidthConstraints() throws InterruptedException {
//        Node n;
//            for (String s : this.nodes.keySet()) {
//                try {
//                    n = this.nodes.get(s);
//
//                    if (n.parentConstraints.size() > 0) {
//                        ArrayList<int[]> widths = getWidthsForConstraints(n.getParentConstraints());
//                        for (int y = 0; y < widths.size(); y++) {
//                            String parentXpath = n.getParentConstraints().get(y).node1.xpath;
//
//                            int[] validWidths = widths.get(y);
//                            int[] widthsTemp = new int[validWidths.length];
//                            int[] parentWidths = new int[validWidths.length];
//                            int[] childWidths = new int[validWidths.length];
//                            // Gather parent and child widths
//                            populateWidthArrays(validWidths, widthsTemp, parentWidths, childWidths, s, parentXpath);
//
//
//                            // Get the equations
//                            double[] bestFit;
//                            int previousBreakpoint = n.getParentConstraints().get(y).getMin()-1;
//                            while (parentWidths.length >= 2) {
//                                int breakpointIndex;
//                                double[] firstTwoWidths = new double[]{parentWidths[0], parentWidths[1]};
//                                double[] firstTwoValues = new double[]{childWidths[0], childWidths[1]};
//                                double[] equation = getEquationOfLine(firstTwoWidths, firstTwoValues);
//                                breakpointIndex = matchValuesToEquation(equation, parentWidths, childWidths);
//
//                                // Generate best fit equation
//                                bestFit = getBestFitLine(parentWidths, childWidths, breakpointIndex);
//
//                                int breakpoint;
//                                if (breakpointIndex != parentWidths.length) {
//                                    breakpoint = findWidthBreakpoint(bestFit, widthsTemp[breakpointIndex - 1], widthsTemp[breakpointIndex], s, parentXpath);
//                                } else {
//                                    breakpoint = widthsTemp[widthsTemp.length - 1];
//                                }
//                                WidthConstraint wc = new WidthConstraint(previousBreakpoint + 1, breakpoint, bestFit[1], this.nodes.get(parentXpath), bestFit[2]);
//                                this.widthConstraints.put(s, new int[]{previousBreakpoint + 1, breakpoint}, wc);
//                                previousBreakpoint = breakpoint;
//
//                                int[] tempWidths = new int[parentWidths.length];
//                                int[] tempValues = new int[childWidths.length];
//                                int[] tempScreenWidths = new int[widthsTemp.length];
//                                System.arraycopy(parentWidths, 0, tempWidths, 0, parentWidths.length);
//                                System.arraycopy(childWidths, 0, tempValues, 0, childWidths.length);
//                                System.arraycopy(widthsTemp, 0, tempScreenWidths, 0, widthsTemp.length);
//
//                                // Redefine the arrays we're using to extract equations
//                                parentWidths = new int[tempWidths.length - breakpointIndex];
//                                childWidths = new int[tempValues.length - breakpointIndex];
//                                widthsTemp = new int[tempScreenWidths.length - breakpointIndex];
//                                // Copy across reduced arrays
//                                System.arraycopy(tempWidths, breakpointIndex, parentWidths, 0, parentWidths.length);
//                                System.arraycopy(tempValues, breakpointIndex, childWidths, 0, childWidths.length);
//                                System.arraycopy(tempScreenWidths, breakpointIndex, widthsTemp, 0, widthsTemp.length);
//                            }
//                        }
//                    }
//                } catch (NullPointerException e) {
//                }
//        }
//        addWidthConstraintsToNodes();
//    }
//
//    public int matchValuesToEquation(double[] equation, int[] parentWidths, int[] childWidths) {
////    	System.out.println("original was " +equation[0] + "," + equation[1] + "," + equation[2]);
//        for (int i = 2; i < parentWidths.length; i++) {
//            double result = (equation[0] * childWidths[i]) - ((equation[1] * parentWidths[i]) + (equation[2]));
//            if (Math.abs(result) > 5) {
//                return i;
//            }
//            equation = getBestFitLine(parentWidths, childWidths, i+1);
////            System.out.println(equation[0] + "," + equation[1] + "," + equation[2]);
//        }
//        return parentWidths.length;
//    }

//    public void populateWidthArrays(int[] validWidths, int[] widthsTemp, int[] parentWidths, int[] childWidths, String s, String parentXpath) {
//        for (int i = 0; i < validWidths.length; i++) {
//            try {
//                AlignmentGraphFactory agf = getAlignmentGraphFactory(validWidths[i]);
//                ag = agf.getAg();
//                widthsTemp[i] = validWidths[i];
//                HashMap<String, DomNode> dnMap = agf.getDomNodeMap();
//                DomNode p = dnMap.get(parentXpath);
//                DomNode c = dnMap.get(s);
//                parentWidths[i] = p.getCoords()[2] - p.getCoords()[0];
//                childWidths[i] = c.getCoords()[2] - c.getCoords()[0];
//            } catch (Exception e) {
//            }
//        }
//    }
    
    public LayoutFactory getLayoutFactory(int width) {
    	LayoutFactory lf = lFactories.get(width);
    	if (lf != null) {
    		return lf;
    	} else {
            if (!lFactories.containsKey(width)) {
                Tool.capturePageModel(url, new int[]{width}, 50, false, false, driver, swf, lFactories);
                alreadyGathered.add(width);
            }
    		return lFactories.get(width);
    	}
    }

/*


    /**
     * Goes through the full set of alignment constraints and adds the parent-child constraints to the node representing
     * the child element, for use in the width constraint extraction
     */
    public void addParentConstraintsToNodes() {
        for (AlignmentConstraint ac : this.getAlignmentConstraints().values()) {
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
//    public void addWidthConstraintsToNodes() {
//        for (Node n : this.nodes.values()) {
//            Map<int[], WidthConstraint> wcs = this.widthConstraints.row(n.xpath);
//            for (WidthConstraint wc : wcs.values()) {
//                if ((wc.min < wc.max) && (!Double.isNaN(wc.percentage)) && (!Double.isNaN(wc.max))) {
//                    n.addWidthConstraint(wc);
//                }
//            }
//        }
//    }

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
    public int searchForLayoutChange(String searchKey, int min, int max, boolean searchForNode, String flippedKey, boolean appear) throws InterruptedException {
        if (max-min==1) {
            return max;
        } else {
            int mid = (max+min)/2;
            int[] extraWidths = new int[] {mid};
            captureExtraDoms(extraWidths);

            LayoutFactory extra = getLayoutFactory(mid);
            boolean found = checkLayoutForKey(extra, searchForNode, searchKey, flippedKey);
            if ((found && appear) || (!found && !appear)) {
                return searchForLayoutChange(searchKey, min, mid, searchForNode, flippedKey, appear);
            } else {
                return searchForLayoutChange(searchKey, mid, max, searchForNode, flippedKey, appear);
            }
        }
    }

    private boolean checkLayoutForKey(LayoutFactory lf1, boolean searchForNode, String searchKey, String flippedKey) {
        if (searchForNode) {
            HashMap<String, Element> n1 = lf1.getElementMap();
            return n1.get(searchKey) != null;
        } else {
            HashMap<String, Relationship> e1 = lf1.getRelationships();
            return (e1.get(searchKey) != null) || (e1.get(flippedKey) != null);
        }
    }



//    /**
//     * Takes a set of parent-child alignment constraints for a particular element and return a sequence of sets of
//     * widths for which each alignment constraint holds
//     * @param acs       the set of alignment constraints for the element
//     * @return          a set of arrays representing the sets of widths
//     */
//    public ArrayList<int[]> getWidthsForConstraints(ArrayList<AlignmentConstraint> acs) {
//        ArrayList<int[]> widthSets = new ArrayList<int[]>();
//        TreeMap<Integer, AlignmentConstraint> ordered = new TreeMap<Integer, AlignmentConstraint>();
//        for (AlignmentConstraint c : acs) {
//            ordered.put(c.min,c);
//        }
//        String previousParent = null;
//        HashMap<String, int[]> parentRanges = new HashMap<>();
//
//
//        // Get all the different parents
//        for (AlignmentConstraint c : ordered.values()) {
//            if (!c.node1.xpath.equals(previousParent)) {
//                parentRanges.put(c.node1.getXpath(), new int[] {c.min, c.getMax()});
//                previousParent = c.node1.getXpath();
//            } else {
//                // Update max value
//                int[] range = parentRanges.get(c.getNode1().getXpath());
//                range[1] = c.getMax();
//            }
//        }
//        for (int[] range : parentRanges.values()) {
//            ArrayList<Integer> temp = new ArrayList<>();
//            for (int w : this.widths) {
//                if ((w >= range[0]) && (w <= range[1])) {
//                    temp.add(w);
//                }
//            }
//            int[] widthArray = new int[temp.size()];
//            for (Integer i : temp) {
//                widthArray[temp.indexOf(i)] = i;
//            }
//            widthSets.add(widthSets.size(), widthArray);
//        }
//
//
//        return widthSets;
//    }

//    /**
//     * Takes a set of parent-child width pairs and uses the first two of them to construct an initial equation
//     * @param widths    the set of parent widths
//     * @param values    the set of child widths
//     * @return          the coefficients of the initial equation
//     */
//    public double[] getEquationOfLine(double[] widths, double[] values) {
//
//        double[] coefficients = new double[3];
//        boolean allValuesTheSame = Utils.areAllItemsSame(values);
//
//        if (allValuesTheSame) {
//            coefficients[0] = 1;
//            coefficients[1] = 0;
//            coefficients[2] = values[0];
//        } else {
//            double y1 = values[0];
//            double x1 = widths[0];
//            double y2 = values[values.length-1];
//            double x2 = widths[widths.length-1];
//            double gradient = ( (y2-y1) / (x2-x1) );
//            coefficients[0] = 1.0;
//            double yintercept = y1- (gradient * x1);
//            coefficients[1] = gradient;
//            coefficients[2] = yintercept;
//        }
//        return coefficients;
//    }

//    /**
//     * Takes a sequence of parent-child width pairs and fits a line of best fit onto the values
//     * @param ps    the set of parent widths
//     * @param cs    the set of child widths
//     * @param i     the index at which to stop extracting values
//     * @return      the coefficients of the best fit line
//     */
//    public double[] getBestFitLine(int[] ps, int[] cs, int i) {
//        double[] valuesForEq = new double[i-1];
//        SimpleRegression reg = new SimpleRegression();
//        // Add in values to the data set
//        for (int i2 = 0; i2 < i-1; i2++) {
//            reg.addData(ps[i2], cs[i2]);
//            valuesForEq[i2] = cs[i2];
//        }
//        double[] regressionEq;
//
//        // Generate the line of best fit
//        if (Utils.areAllItemsSame(valuesForEq)) {
//            // Child widths consistent, so just a flat line
//            regressionEq = new double[] {1.0, 0, valuesForEq[0]};
//        } else {
//            // Plot the line and return the coefficients
//            try {
//                regressionEq = new double[] {1.0, reg.getSlope(),reg.getIntercept()};
//            } catch (IllegalArgumentException e) {
//                regressionEq = null;
//            }
//        }
//        return regressionEq;
//    }

//    /**
//     * Returns the viewport width at which the width equation for a particular elements ceases to become true.
//     * This is done through the use of a binary search between two viewport widths, extracted during the main model
//     * construction process.
//     * @param eq        the width equation for the element
//     * @param min       the lower bound for the binary search
//     * @param max       the upper bound for the binary search
//     * @param child     the element to which the width equation applies
//     * @param parent    the parent of the element between the upper and lower bounds. Used to determine whether the element's
//     *                  width matches the equation
//     * @return          the viewport width at which the equation ceases to hold for the element
//     * @throws InterruptedException
//     */
//    public int findWidthBreakpoint(double[] eq, int min, int max, String child, String parent) throws InterruptedException {
//        if (max-min == 1) {
//            int[] extraWidths = new int[] {min,max};
//            ArrayList<AlignmentGraphFactory> extraGraphs = new ArrayList<AlignmentGraphFactory>();
//            captureExtraDoms(extraWidths);
//
//            AlignmentGraphFactory ag1 = getAlignmentGraphFactory(min);
//            AlignmentGraphFactory ag2 = getAlignmentGraphFactory(max);
//
//            Map<String, DomNode> map1 = ag1.getDomNodeMap();
//            Map<String, DomNode> map2 = ag2.getDomNodeMap();
//
//            DomNode c1 = map1.get(child);
//            DomNode p1 = map1.get(parent);
//            DomNode c2 = map2.get(child);
//            DomNode p2 = map2.get(parent);
//
//            int c1w = getWidthOfDomNode(c1);
//            int p1w = getWidthOfDomNode(p1);
//            int c2w = getWidthOfDomNode(c2);
//            int p2w = getWidthOfDomNode(p2);
//            boolean result1 = Math.abs((eq[0]*c1w) - ((eq[1]*p1w) + (eq[2]))) <=5;
//            boolean result2 = Math.abs((eq[0]*c2w) - ((eq[1]*p2w) + (eq[2]))) <=5;
//
//            if (result1 && result2) {
//                return max;
//            } else if (result1 && !result2) {
//                return min;
//            } else if (!result1 && !result2) {
//                return min-1;
//            } else {
//                return max+1;
//            }
//        } else if (max > min) {
//        	try {
//	            int mid = (max+min)/2;
//	            int[] extraWidths = new int[] {mid};
//
//	            captureExtraDoms(extraWidths);
//	            AlignmentGraphFactory extraAG = getAlignmentGraphFactory(mid);
//
//	            Map<String, DomNode> map1 = extraAG.getDomNodeMap();
//
//	            DomNode c1 = map1.get(child);
//	            DomNode p1 = map1.get(parent);
//
//	            // Get parent and child widths
//	            int c = getWidthOfDomNode(c1);
//	            int p = getWidthOfDomNode(p1);
//
//	            // Check whether mid falls on the equation's line or not.
//	            boolean result = Math.abs((eq[0]*c) - ((eq[1]*p) + (eq[2]))) <=5;
//
//	            // Check which way to recurse
//	            if (result) {
//	                // Breakpoint is higher as mid is on the line
//	                return findWidthBreakpoint(eq, mid, max, child, parent);
//	            } else {
	                // Breakpoint is lower as mid is not on the line (already passed breakpoint)
//	                return findWidthBreakpoint(eq, min, mid, child, parent);
//	            }
//        	} catch (NullPointerException e) {
//        		return (min+max)/2;
//        	}
//        }
//        return (min+max)/2;
//    }
//
//    public int getWidthOfDomNode(DomNode dn) {
//        return dn.getCoords()[2]-dn.getCoords()[0];
//    }

    /**
     * Takes the RLG and presents it in a visual format so it can be further examined if required.
     * Visibility, alignment and width constraints are added to the graph so all aspects can be inspected.
     * @param graphName the file name of the GraphViz file you wish to create
     */
    public void writeToGraphViz(String graphName, String dir) {
        PrintWriter output = null;
        try {
            output = new PrintWriter(dir + graphName + ".gv");
            output.append("digraph G {");

            for (AlignmentConstraint ac : this.getAlignmentConstraints().values()) {
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



    }

    public void captureExtraDoms(int[] widths) {
    	if (widths.length == 2) {
	    	if ((!alreadyGathered.contains(widths[0])) && (!alreadyGathered.contains(widths[1]))) {
	    		Tool.capturePageModel(url, widths, sleep, false, false, wdriver, swf, lFactories);
	    		alreadyGathered.add(widths[0]);
	    		alreadyGathered.add(widths[1]);
	    	} else if (!alreadyGathered.contains(widths[0])) {
	    		Tool.capturePageModel(url, new int[]{widths[0]}, sleep, false, false, wdriver, swf, lFactories);
	    		alreadyGathered.add(widths[0]);
	    	} else if (!alreadyGathered.contains(widths[1])) {
	    		Tool.capturePageModel(url, new int[]{widths[1]}, sleep, false, false, wdriver, swf, lFactories);
	    		alreadyGathered.add(widths[1]);
	    	}
    	} else if (widths.length == 1) {
    		if (!alreadyGathered.contains(widths[0])) {
	    		Tool.capturePageModel(url, new int[]{widths[0]}, sleep, false, false, wdriver, swf, lFactories);
	    		alreadyGathered.add(widths[0]);
    		}
    	}
    }

	public HashBasedTable<String, int[], AlignmentConstraint> getAlignmentConstraints() {
		return alignmentConstraints;
	}

	public void setAlignmentConstraints(HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints) {
		this.alignmentConstraints = alignmentConstraints;
	}
}
