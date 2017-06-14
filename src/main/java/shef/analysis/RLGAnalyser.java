package shef.analysis;

import com.google.common.collect.HashBasedTable;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import shef.layout.Element;
import shef.layout.Layout;
import shef.layout.LayoutFactory;
import shef.main.Utils;
import shef.reporting.inconsistencies.*;
import shef.rlg.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static java.lang.Math.abs;

/**
 * Created by thomaswalsh on 20/05/2016.
 */
public class RLGAnalyser {
    ResponsiveLayoutGraph rlg;
    ArrayList<ResponsiveLayoutFailure> errors;
    WebDriver driver;
    String url;
    ArrayList<Integer> bpoints;
    ArrayList<Node> onePixelOverflows;
    HashMap<Integer, LayoutFactory> layouts;
    int vmin, vmax;

    public RLGAnalyser(ResponsiveLayoutGraph r, WebDriver webDriver, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax) {
        rlg = r;
        driver = webDriver;
        url = fullUrl;
        bpoints = breakpoints;
//        System.out.println(bpoints);
        onePixelOverflows = new ArrayList<>();
        layouts = lFactories;
        this.vmin = vmin;
        this.vmax = vmax;
        errors = new ArrayList<>();
    }

    /**
     * Analyses the RLG to identify responsive layout failures
     * @return
     */
    public ArrayList<ResponsiveLayoutFailure> analyse() {
        

        checkForViewportOverflows(rlg.getNodes());
        detectOverflowOrOverlap(rlg.getAlignmentConstraints());
        checkForSmallRanges(rlg.getAlignmentConstraints());
        checkForWrappingElements();
//        filterOutDuplicateReports();
//        FailureReportClusterBot clusterbot = new FailureReportClusterBot(errors);
        return errors;
    }


    /**
     * This method filters out repeated reports to make the final outputted reports more useful
     */
    private void filterOutDuplicateReports() {

        ArrayList<ResponsiveLayoutFailure> cloned1 = (ArrayList<ResponsiveLayoutFailure>) errors.clone();

        // Check to see if any of the element collisions are simply due to rounding errors
        for (ResponsiveLayoutFailure rle : cloned1) {
            if (rle instanceof CollisionFailure) {
                AlignmentConstraint ac = ((CollisionFailure) rle).getConstraint();
                int chosenWidth = getWidthWithinRange(ac.getMin(), ac.getMax(), layouts);
                LayoutFactory lf = layouts.get(chosenWidth);
                Layout l = lf.layout;
                HashMap<String, Element> elements = l.getElements();
                Element e1 = elements.get(ac.getNode1().getXpath());
                Element e2 = elements.get(ac.getNode2().getXpath());
                int[] c1 = e1.getBoundingCoords();
                int[] c2 = e2.getBoundingCoords();

                Rectangle r1 = new Rectangle(c1[0], c1[1], c1[2] - c1[0], c1[3] - c1[1]);
                Rectangle r2 = new Rectangle(c2[0], c2[1], c2[2] - c2[0], c2[3] - c2[1]);
                Rectangle intersection = r1.intersection(r2);
                if (intersection.getWidth() == 1 || intersection.getHeight() == 1) {
                    System.out.println("TINY INTERSECTION ON " + ac);
                    errors.remove(rle);
                }
            }
        }

        ArrayList<ResponsiveLayoutFailure> cloned2 = (ArrayList<ResponsiveLayoutFailure>) errors.clone();

        // Check to see if any of the element protrusion failures are due to rounding issues
        for (ResponsiveLayoutFailure rle : cloned2) {
            if (rle instanceof ElementProtrusionFailure) {

                AlignmentConstraint ac = ((ElementProtrusionFailure) rle).getOfCon();
//                AlignmentConstraint m = ((ElementProtrusionFailure) rle).getMatch();
                Node overflowed = ((ElementProtrusionFailure) rle).getOverflowed();
                Node parent = null;
                if (ac.getNode1() == overflowed) {
                    parent = ac.getNode2();
                } else {
                    parent = ac.getNode1();
                }
                int chosenWidth = getWidthWithinRange(ac.getMin(), ac.getMax(), layouts);
                LayoutFactory lf = layouts.get(chosenWidth);
                Layout l = lf.layout;
                HashMap<String, Element> elements = l.getElements();
                Element e = elements.get(overflowed.getXpath());

                Element ip = elements.get(parent.getXpath());

                int diffR = e.getBoundingCoords()[2] - ip.getBoundingCoords()[2];
                int diffB = e.getBoundingCoords()[3] - ip.getBoundingCoords()[3];
                int diffL = ip.getBoundingCoords()[0]-e.getBoundingCoords()[0];
                int diffT = ip.getBoundingCoords()[1]-e.getBoundingCoords()[1];

                if (diffR == 1 || diffB == 1 || diffL == 1 || diffT == 1) {
                    errors.remove(rle);
                }
            }
        }
    }

    /**
     * This method analyses a set of RLG nodes to determine whether any of them overflow the viewport
     * @param nodes The set of nodes to analyse
     */
    public void checkForViewportOverflows(HashMap<String, Node> nodes) {
        // Iterate through all the nodes
        for (Node n : nodes.values()) {
            // Don't analyse the BODY tag, as it's the root and therefore has no parent
            if (!n.getXpath().equals("/HTML/BODY")) {

                // Get all the constraints in which the node, n, is the child
                ArrayList<AlignmentConstraint> pCons = n.getParentConstraints();

                // Iterate through and order the constraints in ascending order of bounds
                TreeMap<Integer, Integer> conBounds = new TreeMap<>();
                for (AlignmentConstraint pc : pCons) {
                    conBounds.put(pc.getMin(), pc.getMax());
                }

                // Checks that at least one parent constraint exists, as if not the element is always outside the viewport and therefore
                // not reported as a failure
                if (pCons.size() > 0) {
                    // Initialises the min value of the gap
                    int gmin = vmin;
                    for (Map.Entry e : conBounds.entrySet()) {
                        // Updates the max bound of the gap
                        int gmax = (int) e.getKey() - 1;

                        // If the gap is genuine
                        if (gmin < gmax) {
                            // Looks to see if the node, n, is visible in the gap
                            String key = isVisible(n, gmin, gmax);
                            if (!key.equals("")) {
                                // If so, create a viewport overflow failure for the range at which n is visible
                                int repMin = getNumberFromKey(key, 0);
                                int repMax = getNumberFromKey(key, 1);
                                ViewportProtrusionFailure voe = new ViewportProtrusionFailure(n, repMin, repMax);
                                errors.add(voe);
                            }
                        }
                        // Update the value of the gap minimum
                        gmin = (int) e.getValue() + 1;
                    }

                    /* Check whether the last constraint end at the final width at which n is visible.
                        If not, report a viewport overflow failure.
                     */
                    if (gmin < vmax && !isVisible(n, gmin, vmax).equals("")) {
                        ViewportProtrusionFailure voe = new ViewportProtrusionFailure(n, gmin, vmax);
                        errors.add(voe);
                    }
                }
            }
        }
    }

    /**
     * This method investigates whether a node, n, is visible at any viewport widths within a range
     * @param n the node being investigated
     * @param gmin the lower bound of the range
     * @param gmax the upper bound of the range
     * @return
     */
    private String isVisible(Node n, int gmin, int gmax) {
        // Get the visibility constraints of n
        ArrayList<VisibilityConstraint> vcons = n.getVisibilityConstraints();

        // Iterate through each one
        for (VisibilityConstraint vc : vcons) {
            int visMin = vc.appear;
            int visMax = vc.disappear;

            // Check if the constraint intersects the range
            if (gmax >= visMin && gmax <= visMax) {
                // If so, return the range of widths within the range at which n is visible
                if (visMin <= gmin) {
                    return gmin+":"+gmax;
                } else {
                    return visMin + ":" + gmax;
                }
            }
        }
        return "";
    }

    /**
     * This method examines the alignment constraints from the RLG under test to see if any overlapping or overflowing elements
     * have been found
     * @param alignmentConstraints the set of constraints to analyse
     */
    public void detectOverflowOrOverlap(HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints) {

        // Iterate through all constraints
        for (AlignmentConstraint ac : alignmentConstraints.values()) {
            // We only need to look at the sibling ones
            if (ac.getType() == Type.SIBLING) {

                // Only continue analysis if the "overlapping" attribute label is true
                if (ac.getAttributes()[10]) {
                    boolean collision = false;
                    AlignmentConstraint next = getPreviousOrNextConstraint(ac, false, false);

                    // Now, investigate whether the two elements were NOT overlapping at the wider range
                    if (next != null && next.getType() == Type.SIBLING) {
                        // Check if elements overlapping in next constraint
                        if (!next.getAttributes()[10]) {
                            // If they weren't then report a collision failure
                            CollisionFailure oe = new CollisionFailure(ac);
                            errors.add(oe);
                            collision = true;
                        }
                    }

                    // Only continue checking if the current constraint was not identified as a collision failure
                    if (!collision) {
                        // Get the ancestry of the two nodes, so we can see if the overlap is due to an overflow
                        HashSet<Node> n1Ancestry = getAncestry(ac.getNode1(), ac.getMax() + 1);
                        HashSet<Node> n2Ancestry = getAncestry(ac.getNode2(), ac.getMax() + 1);
                        
                        // If node2 in ancestry of node1 or vice verse, it's an overflow
                        if (n1Ancestry.contains(ac.getNode2())) {
                            ElementProtrusionFailure ofe = new ElementProtrusionFailure(ac.getNode1(), ac);
                            errors.add(ofe);
                        } else if (n2Ancestry.contains(ac.getNode1())) {
                            ElementProtrusionFailure ofe = new ElementProtrusionFailure(ac.getNode2(), ac);
                            errors.add(ofe);
                        }
                    }
                }
            }
        }
    }

    /**
     * This method traverses the RLG to obtain a set of ancestors for a node at a given viewport width.
     * @param node1 the node whose ancestry we want
     * @param i the viewport width at which to traverse
     * @return
     */
    private HashSet<Node> getAncestry(Node node1, int i) {
        HashSet<Node> ancestors = new HashSet<>();

        // Initialise the worklist and add the initial node
        ArrayList<Node> workList = new ArrayList<>();
        workList.add(node1);

        // Keeping track of the nodes we've analysed.
        ArrayList<Node> analysed = new ArrayList<>();
        try {
            // While there's still nodes left.
            while (!workList.isEmpty()) {
                // Take the next node from the list
                Node n = workList.remove(0);

                // Get the parent constraints for the current node
                ArrayList<AlignmentConstraint> cons = n.getParentConstraints();
                for (AlignmentConstraint ac : cons) {
                    // Check if this constraint is true at the desired width
                    if (ac.getMin() <= i && ac.getMax() >= i) {
                        // Add the parent to the list of ancestors and the worklist
                        ancestors.add(ac.getNode1());
                        if (!analysed.contains(ac.getNode1())) {
                            workList.add(ac.getNode1());
                        }
                    }
                }
                analysed.add(n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ancestors;
    }


//    private ArrayList<AlignmentConstraint> getParentChildConstraints(Node node1, Node node2) {
//        ArrayList<AlignmentConstraint> toReturn = new ArrayList<>();
//        for (AlignmentConstraint ac : rlg.getAlignmentConstraints().values()) {
//            if (ac.getType() == Type.PARENT_CHILD) {
//                if (ac.getNode1() == node1 && ac.getNode2() == node2) {
//                    toReturn.add(ac);
//                } else if (ac.getNode1() == node2 && ac.getNode2() == node1) {
//                    toReturn.add(ac);
//                }
//            }
//        }
//        return toReturn;
//    }


//    private boolean checkForRelatedOverflowErrors(AlignmentConstraint ac) {
//        Node ac1 = ac.getNode1();
//        Node ac2 = ac.getNode2();
//
//        for (ResponsiveLayoutFailure e : errors) {
//            if (e instanceof ElementProtrusionFailure) {
//                Node overflowed = ((ElementProtrusionFailure) e).getOverflowed();
//
////                for (Node intended : ((ElementProtrusionFailure) e).getMap().keySet()) {
//                    if ((ac1.getXpath().equals(overflowed.getXpath()) || ac2.getXpath().equals(overflowed.getXpath()))) {
////                        System.out.println("Wanted to remove " + ac + " because of " + overflowed.getXpath());
////                        System.out.println(overflowed.getXpath());
////                        System.out.println();
//                        return true;
//                    }
////                }
//            }
//        }
//        return false;
//    }

    /**
     * This method analyses the constraints to see if any represent potential small-range layout inconsistencies
     * @param alignmentConstraints the set of constraints to analyse
     */
    public void checkForSmallRanges(HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints) {
        for (AlignmentConstraint ac : alignmentConstraints.values()) {
            // See if the bounds width is less than the threshold, in this case, 5 pixels
            if ( (ac.getMax() - ac.getMin()) < 5 ) {
                // Obtain the previous and following constraints
                AlignmentConstraint prev = getPreviousOrNextConstraint(ac, true, true);
                AlignmentConstraint next = getPreviousOrNextConstraint(ac, false, true);

                // If matches were found for both, report the constraint as a small-range failure
                if (prev != null && next != null) {
                    SmallRangeFailure sre = new SmallRangeFailure(ac, prev, next);
                    errors.add(sre);
                }
            }
        }
    }

    /**
     * This method searches for the constraint either immediately before or after a given constraint
     * @param ac the constraint we're investigating
     * @param i whether we want the preceding (true) or following (false) constraint
     * @param matchType whether we care about matching the type as well as the nodes
     * @return the matched constraint, if one was found
     */
    private AlignmentConstraint getPreviousOrNextConstraint(AlignmentConstraint ac, boolean i, boolean matchType) {
        String ac1xp = ac.getNode1().getXpath();
        String ac2xp = ac.getNode2().getXpath();

        for (AlignmentConstraint con : rlg.getAlignmentConstraints().values()) {
//
                String con1xp = con.getNode1().getXpath();
                String con2xp = con.getNode2().getXpath();
                if ( (ac1xp.equals(con1xp) && ac2xp.equals(con2xp)) || (ac1xp.equals(con2xp) && ac2xp.equals(con1xp)) ) {
                    if (i) {
                        if (con.getMax() == ac.getMin()-1) {
                            if (!matchType) {
                                return con;
                            } else {
                                if (con.getType() == ac.getType()) {
                                    return con;
                                }
                            }
                        }
                    } else {
                        if (con.getMin() == ac.getMax()+1) {
                            if (!matchType) {
                                return con;
                            } else {
                                if (con.getType() == ac.getType()) {
                                    return con;
                                }
                            }
                        }
                    }
                }
//            }
        }
        return null;
    }

    /**
     * This method returns a width for which the layout has been extracted, between two values
     * @param min  the minimum value
     * @param max  the maximum value
     * @param layouts the map of layouts already extracted
     * @return
     */
    public int getWidthWithinRange(int min, int max, HashMap<Integer, LayoutFactory> layouts) {
        // Iterate through all layouts
        for (Integer i : layouts.keySet()) {
            // If the width is within the range, return it
            if (i >= min && i <= max) {
                return i;
            }
        }
        // Return the midpoint of the range as a last resort
        return (min+max)/2;
    }

//    private boolean parentAlsoOverflowed(Node overflowed, ArrayList<ResponsiveLayoutFailure> cloned) {
//        String overflowedXP = overflowed.getXpath();
//
//        for (ResponsiveLayoutFailure e : cloned) {
//            if (e instanceof ElementProtrusionFailure) {
//                String comparisonXP = ((ElementProtrusionFailure) e).getOverflowed().getXpath();
//                if (overflowedXP.contains(comparisonXP) && !overflowedXP.equals(comparisonXP)) {
////                    System.out.println("Matched " + overflowedXP + " to " + comparisonXP);
//                    return true;
//                }
//            } else if (e instanceof ViewportProtrusionFailure) {
//                String comparisonXP = ((ViewportProtrusionFailure) e).getNode().getXpath();
//                if (overflowedXP.contains(comparisonXP) || overflowedXP.equals(comparisonXP)) {
////                    System.out.println("Matched " + overflowedXP + " to " + comparisonXP);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    /**
     * Checks each node in turn and looks whether any of its children elements wrap incongruously at any point
     */
    private void checkForWrappingElements() {
        // Iterate through each node in the RLG
        for (Node n : rlg.getNodes().values()) {

            // Get the children of the node
            ArrayList<Node> children = getChildrenOfNode(n);

            // Continure if more than one child element
            if (children.size() > 1) {
                TreeSet<Integer> values = new TreeSet<>();
                TreeSet<Integer> pcValues = new TreeSet<>();
                ArrayList<AlignmentConstraint> sibs = new ArrayList<>();

                // Iterate through all alignment constraints
                for (AlignmentConstraint ac : rlg.getAlignmentConstraints().values()) {
                    if (ac.getType() == Type.SIBLING) {

                        // If its a sibling containing two of n's children, add to the set
                        if ((children.contains(ac.getNode1())) && (children.contains(ac.getNode2()))) {
                            if (childrenWithinParent(ac, n)) {
                                sibs.add(ac);
                                values.add(ac.getMin());
                                values.add(ac.getMax());
                            }
                        }
                    }
                }

                // Extract the different behaviour ranges from the set of upper and lower bounds
                ArrayList<String> keys = extractLayoutRanges(values);

                // Match based on bounds
                HashMap<String, HashSet<AlignmentConstraint>> grouped = new HashMap<>();
                for (String k : keys) {
                    grouped.put(k, new HashSet<>());
                }

                // Group the sibling constraints
                for (AlignmentConstraint ac : sibs) {
                    putConstraintIntoGroups(grouped, ac);
                }

                HashMap<String, ArrayList<ArrayList<Node>>> totalRows = new HashMap<>();
                HashMap<String, ArrayList<Node>> totalNotInRows = new HashMap<>();
                HashMap<String, HashMap<String, ArrayList<AlignmentConstraint>>> totalRowCons = new HashMap<>();
                HashMap<String, HashSet<Node>> nodesInParentMap = new HashMap<>();

                // Iterate through each layout range
                for (String key : grouped.keySet()) {
                    nodesInParentMap.put(key, new HashSet<Node>());
                    try {
                        // Try and put elements into rows
                        ArrayList<ArrayList<Node>> rows = new ArrayList<>();
                        HashMap<String, ArrayList<AlignmentConstraint>> rowSibConstraints = new HashMap<>();
                        ArrayList<Node> nodesNotInRows = (ArrayList<Node>) children.clone();
                        HashSet<AlignmentConstraint> fullSet = grouped.get(key);
                        HashSet<AlignmentConstraint> overlapping = new HashSet<>();

                        // Iterate through each constraint true at the current width
                        for (AlignmentConstraint ac : fullSet) {
                            nodesInParentMap.get(key).add(ac.getNode1());
                            nodesInParentMap.get(key).add(ac.getNode2());

                            // Check whether the two elements are aligned in a row
//                            int toggle = alignedInRow(ac);

                            // If they are in a row
                            if (alignedInRow(ac)) {

                                // See if the constraint matches an existing row
                                ArrayList<Node> match = getMatchingExistingRow(ac, rows, fullSet);
                                if (match == null) {
                                    // If no match, create a new row with both elements in it.
                                    ArrayList<Node> newRow = new ArrayList<>();
                                    newRow.add(ac.getNode1());
                                    newRow.add(ac.getNode2());

                                    // Add the new row to the full set of rows
                                    rows.add(newRow);
                                    rowSibConstraints.put(setOfNodesToString(newRow), new ArrayList<AlignmentConstraint>());
                                    rowSibConstraints.get(setOfNodesToString(newRow)).add(ac);
//
                                } else {
                                    // If a match is found, add the elements to the row
                                    String matchKey = setOfNodesToString(match);
                                    if (!match.contains(ac.getNode1())) {
                                        match.add(ac.getNode1());
                                    } else if (!match.contains(ac.getNode2())) {
                                        match.add(ac.getNode2());
                                    }
                                    // Update the full set of rows to have a key for the expanded row
                                    ArrayList<AlignmentConstraint> cons = rowSibConstraints.get(matchKey);
                                    rowSibConstraints.remove(matchKey);
                                    rowSibConstraints.put(setOfNodesToString(match), cons);
                                    rowSibConstraints.get(setOfNodesToString(match)).add(ac);
                                }

                                // Remove from ArrayLists to detect elements not placed into row or column
                                nodesNotInRows.remove(ac.getNode1());
                                nodesNotInRows.remove(ac.getNode2());

                            } else if (ac.getAttributes()[10]) {
                                overlapping.add(ac);
                            }
                        }

                        // FILTER ANY OVERLAPPING ELEMENTS
                        for (AlignmentConstraint acOV : overlapping) {

                            // See if the elements were in a row or column
                            ArrayList<ArrayList<Node>> clonedRows = (ArrayList<ArrayList<Node>>) rows.clone();
                            for (ArrayList<Node> row : clonedRows) {
                                if (row.contains(acOV.getNode1()) && row.contains(acOV.getNode2())) {
                                    ArrayList<Node> actualrow = rows.get(clonedRows.indexOf(row));
                                    actualrow.remove(acOV.getNode1());
                                    actualrow.remove(acOV.getNode2());
                                    removeConstraintsFromCol(acOV.getNode1(), acOV.getNode2(), rowSibConstraints, row);
                                }
                            }
                        }
                        totalRows.put(key, rows);
                        totalNotInRows.put(key, nodesNotInRows);
                        totalRowCons.put(key, rowSibConstraints);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Iterate again through the set of ranges where rows have been found
                for (String key: totalRows.keySet()) {
                    // Get the rows for that range
                    ArrayList<ArrayList<Node>> rows = totalRows.get(key);

                    // Get the nodes NOT in rows
                    ArrayList<Node> not = totalNotInRows.get(key);

                    // Iterate through each element NOT in a row
                    for (Node notInRow : not) {

                        // Check a row exists, because you can't have a wrap without a row
                        if (rows.size() > 0) {

                            // Check to see if a matching row is found at the wider viewport range
                            ArrayList<Node> nonWrappedRow = getRowInNextRange(notInRow, totalRows, key);

                            // If a matching row found
                            if (nonWrappedRow != null) {
                                ArrayList<Node> wrappedRow = getWrappedRow(rows, nonWrappedRow);
                                if ((nonWrappedRow.size() - wrappedRow.size() == 1) || (children.size()-wrappedRow.size()==1)) {
                                    if (elementVisible(notInRow, key)) {
                                        if (elementStillWithinParent(notInRow, n, key)) {

                                            // Check the element is now below the rest of the row
                                            if (elementNowBelowRow(notInRow, nonWrappedRow, grouped.get(key))) {
                                                // If so, report the wrapping failure
                                                WrappingFailure we = new WrappingFailure(notInRow, nonWrappedRow, getNumberFromKey(key, 0), getNumberFromKey(key, 1));
                                                errors.add(we);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This method returns the set of nodes representing the row from which an element has wrapped
     * @param rows              the set of rows through which to search
     * @param nonWrappedRow     the row in which the element has not wrapped
     * @return                  the row in which the element has wrapped
     */
    private ArrayList<Node> getWrappedRow(ArrayList<ArrayList<Node>> rows, ArrayList<Node> nonWrappedRow) {
        // Iterate through
        for (Node n : nonWrappedRow) {
            for (ArrayList<Node> row : rows) {
                if (row.contains(n)) {
                    return row;
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * This method inspects the alignment constraints to see whether the supposedly 'wrapped' element is now below the rest of the row
     * @param notInRow          The node no longer in a row
     * @param nonWrappedRow     The row in which the element has not wrapped
     * @param grouped           The alignment constraints for the elements, grouped by behaviour range
     * @return                  Whether the element is now below the row
     */
    private boolean elementNowBelowRow(Node notInRow, ArrayList<Node> nonWrappedRow, HashSet<AlignmentConstraint> grouped) {
        // Iterate through each constraint until one we can analyse
        for (AlignmentConstraint ac : grouped) {
            Node n1 = ac.getNode1();
            Node n2 = ac.getNode2();

            // Check the constraint contains the 'wrapped' element and one of the non-wrapped ones
            if ((n1.getXpath().equals(notInRow.getXpath()) && nonWrappedRow.contains(n2)) || (n2.getXpath().equals(notInRow.getXpath()) && nonWrappedRow.contains(n1))) {

                // Check the alignment attributes and return true if the wrapped element is below the non-wrapped one.
                if (n1.getXpath().equals(notInRow.getXpath()) && ac.getAttributes()[1]) {
                    return true;
                } else if (n2.getXpath().equals(notInRow.getXpath()) && ac.getAttributes()[2]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method checks whether the element supposedly 'wrapped' is in a row at the next behaviour range and returns it if it is.
     * @param notInRow  The node no longer in a row
     * @param rows      The set of rows across all behaviour ranges
     * @param key       The key of the current behaviour range
     * @return          The row containing the wrapped element, or null.
     */
    private ArrayList<Node> getRowInNextRange(Node notInRow, HashMap<String, ArrayList<ArrayList<Node>>> rows, String key) {
        // Get the upper bound of the current range
        int x = getNumberFromKey(key,1);

        // Iterate through all the keys in the set
        for (String key2 : rows.keySet()) {

            // Check it's not the same key
            if (!key.equals(key2)) {
                // Get the lower bound of the different key
                int first = getNumberFromKey(key2, 0);

                // If this one is the immediately wider range, do our further analysis
                if (first - x == 1) {

                    // Get the rows for this range
                    ArrayList<ArrayList<Node>> rs = rows.get(key2);

                    // Iterate through all the rows
                    for (ArrayList<Node> r : rs) {
                        // If the row contains the element we're looking for, return the row
                        if (r.contains(notInRow)) {
                            return r;
                        }
                    }
                }

            }
        }
        return null;
    }

//    private void checkForWrappingElements() {
//        for (Node n : rlg.getNodes().values()) {
//            ArrayList<Node> children = getChildrenOfNode(n);
//            if (children.size() > 1) {
//                TreeSet<Integer> values = new TreeSet<>();
//                TreeSet<Integer> pcValues = new TreeSet<>();
//                ArrayList<AlignmentConstraint> sibs = new ArrayList<>();
//                for (AlignmentConstraint ac : rlg.getAlignmentConstraints().values()) {
//                    if (ac.getType() == Type.SIBLING) {
//                        if ((children.contains(ac.getNode1())) && (children.contains(ac.getNode2()))) {
//                            if (childrenWithinParent(ac, n)) {
//                                sibs.add(ac);
//                                values.add(ac.getMin());
//                                values.add(ac.getMax());
//                            }
//                        }
//                    }
//                }
//
//
//                ArrayList<String> keys = extractLayoutRanges(values);
//                ArrayList<String> pcKeys = extractLayoutRanges(pcValues);
//
//                // Match based on bounds
//                HashMap<String, HashSet<AlignmentConstraint>> grouped = new HashMap<>();
//                for (String k : keys) {
//                    grouped.put(k, new HashSet<AlignmentConstraint>());
//                }
//                HashMap<String, ArrayList<AlignmentConstraint>> pcGrouped = new HashMap<>();
//                for (String pck : pcKeys) {
//                    pcGrouped.put(pck, new ArrayList<AlignmentConstraint>());
//                }
//
//                // Group both the sibling and parent-child constraints
//                for (AlignmentConstraint ac : sibs) {
//                    putConstraintIntoGroups(grouped, ac);
//                }
//
//                HashMap<String, ArrayList<ArrayList<Node>>> totalRows = new HashMap<>();
////                HashMap<String, ArrayList<ArrayList<Node>>> totalCols = new HashMap<>();
//                HashMap<String, ArrayList<Node>> totalNotInRows = new HashMap<>();
//                HashMap<String, HashMap<String, ArrayList<AlignmentConstraint>>> totalRowCons = new HashMap<>();
////                HashMap<String, HashMap<String, ArrayList<AlignmentConstraint>>> totalColCons = new HashMap<>();
//                HashMap<String, HashSet<Node>> nodesInParentMap = new HashMap<>();
//
//
//
//                for (String key : grouped.keySet()) {
////                    System.out.println(key);
//                    nodesInParentMap.put(key, new HashSet<Node>());
//                    try {
//                        // Try and put elements into rows
//                        ArrayList<ArrayList<Node>> rows = new ArrayList<>();
//                        ArrayList<ArrayList<Node>> columns = new ArrayList<>();
//                        HashMap<String, ArrayList<AlignmentConstraint>> rowSibConstraints = new HashMap<>();
//                        HashMap<String, ArrayList<AlignmentConstraint>> rowPCConstraints = new HashMap<>();
//
////                        HashMap<String, ArrayList<AlignmentConstraint>> colSibConstraints = new HashMap<>();
////                        HashMap<String, ArrayList<AlignmentConstraint>> colPCConstraints = new HashMap<>();
//                        ArrayList<Node> nodesNotInRows = (ArrayList<Node>) children.clone();
////                        ArrayList<Node> nodesNotInColumns = (ArrayList<Node>) children.clone();
//                        HashSet<AlignmentConstraint> fullSet = grouped.get(key);
//                        HashSet<AlignmentConstraint> overlapping = new HashSet<>();
//
//                        for (AlignmentConstraint ac : fullSet) {
//                            nodesInParentMap.get(key).add(ac.getNode1());
//                            nodesInParentMap.get(key).add(ac.getNode2());
//                            int toggle = alignedInRow(ac);
//
//                            if (toggle != 0) {
//                                ArrayList<Node> match = matchingExistingPattern(ac, rows, columns, toggle, fullSet);
//                                if (match == null) {
//                                    // Create a new row with both elements in it.
//                                    ArrayList<Node> newRowCol = new ArrayList<>();
//                                    newRowCol.add(ac.getNode1());
//                                    newRowCol.add(ac.getNode2());
////                                    System.out.println("Creating new row for " + ac);
//
//                                    if (toggle == 1) {
//                                        rows.add(newRowCol);
//                                        rowSibConstraints.put(setOfNodesToString(newRowCol), new ArrayList<AlignmentConstraint>());
//                                        rowSibConstraints.get(setOfNodesToString(newRowCol)).add(ac);
//                                    } else {
////                                        columns.add(newRowCol);
////                                        colSibConstraints.put(setOfNodesToString(newRowCol), new ArrayList<AlignmentConstraint>());
////                                        colSibConstraints.get(setOfNodesToString(newRowCol)).add(ac);
//                                    }
//                                } else {
//                                    // Add the remaining element to the row
//                                    String matchKey = setOfNodesToString(match);
//                                    if (!match.contains(ac.getNode1())) {
//                                        match.add(ac.getNode1());
////                                        System.out.println(ac.getNode1().getXpath() + " added to " + matchKey + "  " + match.size());
//                                    } else if (!match.contains(ac.getNode2())) {
//                                        match.add(ac.getNode2());
////                                        System.out.println(ac.getNode2().getXpath() + " added to " + matchKey + "  " + match.size());
//                                    }
//                                    if (toggle == 1) {
//                                        ArrayList<AlignmentConstraint> cons = rowSibConstraints.get(matchKey);
//                                        rowSibConstraints.remove(matchKey);
//                                        rowSibConstraints.put(setOfNodesToString(match), cons);
//                                        rowSibConstraints.get(setOfNodesToString(match)).add(ac);
//                                    } else {
////                                        ArrayList<AlignmentConstraint> cons = colSibConstraints.get(matchKey);
////                                        colSibConstraints.remove(matchKey);
////                                        colSibConstraints.put(setOfNodesToString(match), cons);
////                                        colSibConstraints.get(setOfNodesToString(match)).add(ac);
//                                    }
//                                }
//
//                                // Remove from ArrayLists to detect elements not placed into row or column
//                                if (toggle == 1) {
//                                    nodesNotInRows.remove(ac.getNode1());
//                                    nodesNotInRows.remove(ac.getNode2());
//                                } else {
////                                    nodesNotInColumns.remove(ac.getNode1());
////                                    nodesNotInColumns.remove(ac.getNode2());
//                                }
//
//                            } else if (toggle == 0) {
//                                overlapping.add(ac);
//                            }
//                        }
//
//                        // FILTER ANY OVERLAPPING ELEMENTS
//
//                        for (AlignmentConstraint acOV : overlapping) {
//
//                            // See if the elements were in a row or column
//                            ArrayList<ArrayList<Node>> clonedRows = (ArrayList<ArrayList<Node>>) rows.clone();
//                            for (ArrayList<Node> row : clonedRows) {
//                                if (row.contains(acOV.getNode1()) && row.contains(acOV.getNode2())) {
//                                    ArrayList<Node> actualrow = rows.get(clonedRows.indexOf(row));
//                                    actualrow.remove(acOV.getNode1());
//                                    actualrow.remove(acOV.getNode2());
//                                    removeConstraintsFromCol(acOV.getNode1(), acOV.getNode2(), rowSibConstraints, row);
//                                }
//                            }
//                        }
////                        if (n.getXpath().equals("/HTML/BODY/DIV[5]/FOOTER/DIV[2]/UL")) {
////                            System.out.println();
////                        }
//                        totalRows.put(key, rows);
//                        totalNotInRows.put(key, nodesNotInRows);
//                        totalRowCons.put(key, rowSibConstraints);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
////                if (n.getXpath().equals("/HTML/BODY/DIV[5]/FOOTER/DIV[3]")) {
////                    System.out.println("Boo");
////                }
////                System.out.println(n.getXpath());
//                for (String key: totalRows.keySet()) {
//                    ArrayList<ArrayList<Node>> rows = totalRows.get(key);
//                    ArrayList<Node> not = totalNotInRows.get(key);
//                    HashMap<String, ArrayList<AlignmentConstraint>> consRow = totalRowCons.get(key);
//                    for (Node notInRow : not) {
//
//                        // Need to refine this to the entire row becoming a column!
//                        if (rows.size() > 0) {
//                            ArrayList<Node> nonWrappedRow = inRowInNextRange(notInRow, totalRows, key);
//
//                            if (nonWrappedRow != null) {
//                                ArrayList<Node> wrappedRow = getWrappedRow(rows, nonWrappedRow);
//                                if ((nonWrappedRow.size() - wrappedRow.size() == 1) || (children.size()-wrappedRow.size()==1)) {
//                                    if (elementVisible(notInRow, key)) {
//                                        //                                    System.out.println(elementStillWithinParent(notInRow, n, key));
//                                        if (elementStillWithinParent(notInRow, n, key)) {
//                                            if (elementNowBelowRow(notInRow, rows, nonWrappedRow, grouped.get(key))) {
//                                                WrappingFailure we = new WrappingFailure(notInRow, nonWrappedRow, getNumberFromKey(key, 0), getNumberFromKey(key, 1));
//                                                errors.add(we);
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        } else {
//
//                        }
//                    }
//                }
//
//
//            }
//        }
//    }
//
//    private ArrayList<Node> getWrappedRow(ArrayList<ArrayList<Node>> rows, ArrayList<Node> nonWrappedRow) {
//        for (Node n : nonWrappedRow) {
//            for (ArrayList<Node> row : rows) {
//                if (row.contains(n)) {
//                    return row;
//                }
//            }
//        }
//        return new ArrayList<>();
//    }
//
//    private boolean elementNowBelowRow(Node notInRow, ArrayList<ArrayList<Node>> rows, ArrayList<Node> nonWrappedRow, HashSet<AlignmentConstraint> grouped) {
//
//        for (AlignmentConstraint ac : grouped) {
//            Node n1 = ac.getNode1();
//            Node n2 = ac.getNode2();
//
//            if ((n1.getXpath().equals(notInRow.getXpath()) && nonWrappedRow.contains(n2))
//                    || (n2.getXpath().equals(notInRow.getXpath()) && nonWrappedRow.contains(n1))) {
////                if (nonWrappedRow.contains(n1) || nonWrappedRow.contains(n2)) {
////                    System.out.println(ac);
//                if (n1.getXpath().equals(notInRow.getXpath()) && ac.getAttributes()[1]) {
//                    return true;
//                } else if (n2.getXpath().equals(notInRow.getXpath()) && ac.getAttributes()[2]) {
//                    return true;
//                }
////                }
//            }
//        }
//        return false;
//    }
//
//    private ArrayList<Node> inRowInNextRange(Node notInRow, HashMap<String, ArrayList<ArrayList<Node>>> rows, String key) {
//        int x = getNumberFromKey(key,1);
//        for (String key2 : rows.keySet()) {
//            if (!key.equals(key2)) {
//                int first = getNumberFromKey(key2, 0);
//                if (first - x == 1) {
//                    ArrayList<ArrayList<Node>> rs = rows.get(key2);
//                    for (ArrayList<Node> r : rs) {
//                        if (r.contains(notInRow)) {
//                            return r;
//                        }
//                    }
//                }
//
//            }
//        }
//        return null;
//    }
//
//    private ArrayList<Node> matchingExistingPattern(AlignmentConstraint ac, ArrayList<ArrayList<Node>> rows, ArrayList<ArrayList<Node>> columns, int rowCol, HashSet<AlignmentConstraint> fullSet) {
//        Node n1 = ac.getNode1();
//        Node n2 = ac.getNode2();
//
//        ArrayList<AlignmentConstraint> n1cons = getSiblingEdges(n1, fullSet);
//        ArrayList<AlignmentConstraint> n2cons = getSiblingEdges(n2, fullSet);
//
//        // Check through all existing rows
//        if (rowCol == 1) {
//            for (ArrayList<Node> row : rows) {
//                for (Node n : row) {
//                    for (AlignmentConstraint acon : n1cons) {
//                        if ((acon.getNode1().equals(n)) || (acon.getNode2().equals(n))) {
//                            if (alignedInRow(acon) == 1) {
//                                return row;
//                            }
//                        }
//                    }
//                    for (AlignmentConstraint acon : n2cons) {
//                        if ((acon.getNode1().equals(n)) || (acon.getNode2().equals(n))) {
//                            if (alignedInRow(acon) == 1) {
//                                return row;
//                            }
//                        }
//                    }
//                }
//            }
//        } else if (rowCol == 2) {
//            for (ArrayList<Node> col : columns) {
//                for (Node n : col) {
//                    for (AlignmentConstraint acon : n1cons) {
//                        if ((acon.getNode1().equals(n)) || (acon.getNode2().equals(n))) {
//                            if (alignedInRow(acon) == 2) {
//                                return col;
//                            }
//                        }
//                    }
//                    for (AlignmentConstraint acon : n2cons) {
//                        if ((acon.getNode1().equals(n)) || (acon.getNode2().equals(n))) {
//                            if (alignedInRow(acon) == 2) {
//                                return col;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }

    /**
     * This method checks whether the 'wrapped' element is still within the parent element
     * @param wrapped   The wrapped element
     * @param parent    The parent element
     * @param key       The behaviour range we need to check for
     * @return
     */
    private boolean elementStillWithinParent(Node wrapped, Node parent, String key) {
        // Extract the lower and upper bounds of the behaviour range
        int kMin = getNumberFromKey(key, 0);
        int kMax = getNumberFromKey(key, 1);

        // Get the parent constraints of the wrapped element
        ArrayList<AlignmentConstraint> pCons = wrapped.getParentConstraints();

        // Iterate through each of the parent constraints
        for (AlignmentConstraint con : pCons) {

            // Make sure the constraint holds for the behaviour range
            if ((con.getMin() <= kMax) && (kMin <= con.getMax())) {
                // If the parent in the constraint equals the parent we passed in, return true.
                if (con.getNode1().getXpath().equals(parent.getXpath())) {
                    return true;
                }
            }
        }
        return false;

    }

    private void removeConstraintsFromCol(Node f1, Node f2, HashMap<String, ArrayList<AlignmentConstraint>> colSibConstraints, ArrayList<Node> newCol) {
        HashMap<String, ArrayList<AlignmentConstraint>> clonedMap = (HashMap<String, ArrayList<AlignmentConstraint>>) colSibConstraints.clone();
        for (String s : clonedMap.keySet()) {
            if (s.contains(f1.getXpath()) && s.contains(f2.getXpath())) {
                ArrayList<AlignmentConstraint> cons = clonedMap.get(s);
                ArrayList<AlignmentConstraint> temp = (ArrayList<AlignmentConstraint>) cons.clone();
                for (AlignmentConstraint con : temp) {
                    if (con.getNode1().getXpath().equals(f1.getXpath()) || (con.getNode1().getXpath().equals(f2.getXpath()))) {
                        cons.remove(con);
                    } else if (con.getNode2().getXpath().equals(f1.getXpath()) || (con.getNode2().getXpath().equals(f2.getXpath()))) {
                        cons.remove(con);
                    }
                }

                // Update everything
                colSibConstraints.remove(s);
                colSibConstraints.put(setOfNodesToString(newCol), cons);
            }
        }
    }

    private boolean childrenWithinParent(AlignmentConstraint ac, Node n) {
        Node n1 = ac.getNode1();
        Node n2 = ac.getNode2();

        for (Node node : new Node[] {n1, n2}) {
            ArrayList<AlignmentConstraint> cons = node.getParentConstraints();
            if (sameParent(cons)) {
                return true;
            } else {
                for (AlignmentConstraint con : cons) {
                    if ((con.getMin() <= ac.getMin()) && (con.getMax() >= ac.getMax())) {
                        if (con.getNode1().getXpath().equals(n.getXpath())) {
                            return true;
                        }
                    }
                }
            }

        }
        return false;
    }

    public boolean sameParent(ArrayList<AlignmentConstraint> cons) {
        HashSet<String> parentXPaths = new HashSet<>();
        for (AlignmentConstraint ac : cons) {
            parentXPaths.add(ac.getNode1().getXpath());
        }
        return parentXPaths.size() == 1;
    }


    private boolean elementVisible(Node wrapped, String key) {
        int min = getNumberFromKey(key, 0);
        int max = getNumberFromKey(key, 1);
        int nodeMin = wrapped.getVisibilityConstraints().get(0).getAppear();
        int nodeMax = wrapped.getVisibilityConstraints().get(0).getDisappear();

        if ((nodeMax < min) || (nodeMin > max)) {
            return false;
        } else {
            return true;
        }
    }


    public String setOfNodesToString(ArrayList<Node> nodes) {
        String result = "";
        if (nodes.size() > 0) {
            for (Node n : nodes) {
                result += n.getXpath() + " ";
            }
        } else {
            result = "EMPTY";
        }
        return result;
    }

    /**
     * This method puts an alignment constraint into groups based on its bounds.
     * @param grouped   The set into which the alignment constraint will be placed
     * @param ac        The alignment constraint to examine
     */
    private void putConstraintIntoGroups(HashMap<String, HashSet<AlignmentConstraint>> grouped, AlignmentConstraint ac) {
        for (String k : grouped.keySet()) {
            int min = getNumberFromKey(k, 0);
            int max = getNumberFromKey(k, 1);
            if ( (ac.getMin() <= min) && (ac.getMax() >= max) ) {
                grouped.get(k).add(ac);
            }
        }
    }

    /**
     * Extracts the various ranges of layout behaviour from a set of upper and lower bounds
     * @param values    The set of lower and upper bounds, in ascending order
     * @return          A set of ranges, saved as a list of string keys
     */
    public ArrayList<String> extractLayoutRanges(TreeSet<Integer> values) {
        ArrayList<String> keys = new ArrayList<>();
        int prev=0;
        int curr;
        // Iterate through all the values
        for (Integer i : values) {
            curr = i;
            // If this isn't the first value we've looked at...
            if (prev != 0) {

                // If the two numbers aren't immediately consecutive...
                if (curr-prev != 1) {
                    // Create the new key and add to the list
                    String key = prev + ":" + curr;
                    keys.add(key);
                }
            }
            // Update the value of prev so we can repeat the process
            prev = curr;
        }
        return keys;
    }

    /**
     * Simple utility to extract a numeric bound from a string key
     * @param key   The string key to extract from
     * @param i     The bound we want (either 0 or 1)
     * @return      The extracted bound
     */
    private int getNumberFromKey(String key, int i) {
        String[] splits = key.split(":");
        return Integer.valueOf(splits[i]);
    }


    /**
     *  This method analyses the existing rows extracted to see if the current constraint matches any of them
     * @param ac        The alignment constraint describing the two elements in a row
     * @param rows      The set of rows currently extracted for this behaviour range
     * @param fullSet   The set of all alignment constraints for the current behaviour range
     * @return          The matching row if one is found, else null
     */
    private ArrayList<Node> getMatchingExistingRow(AlignmentConstraint ac, ArrayList<ArrayList<Node>> rows, HashSet<AlignmentConstraint> fullSet) {
        // Get the nodes of the current constraints
        Node n1 = ac.getNode1();
        Node n2 = ac.getNode2();

        // Get all the sibling constraints linked to the two nodes
        ArrayList<AlignmentConstraint> n1cons = getSiblingEdges(n1, fullSet);
        ArrayList<AlignmentConstraint> n2cons = getSiblingEdges(n2, fullSet);

        // Check through all existing rows
        for (ArrayList<Node> row : rows) {
            // Go through each node in the current row
            for (Node n : row) {

                // Check each constraint linked to n1
                for (AlignmentConstraint acon : n1cons) {
                    // If the constraint linked to the element we already know is in a row...
                    if ((acon.getNode1().equals(n)) || (acon.getNode2().equals(n))) {

                        // Check if the constraint also describes a row
                        if (alignedInRow(acon)) {
                            // If so, we've found a matching row, so return it
                            return row;
                        }
                    }
                }

                // Do exactly the same but with the second node if we haven't already found a match
                for (AlignmentConstraint acon : n2cons) {
                    if ((acon.getNode1().equals(n)) || (acon.getNode2().equals(n))) {
                        if (alignedInRow(acon)) {
                            return row;
                        }
                    }
                }
            }
        }
        return null;
    }

    private ArrayList<AlignmentConstraint> getSiblingEdges(Node n1, HashSet<AlignmentConstraint> fullSet) {
        ArrayList<AlignmentConstraint> cons = new ArrayList<>();
        for (AlignmentConstraint ac : fullSet) {
            if (ac.getType() == Type.SIBLING) {
                if ( (ac.getNode1().equals(n1)) || (ac.getNode2().equals(n1))) {
                    cons.add(ac);
                }
            }
        }
        return cons;
    }

    /**
     * Analyses an alignment constraint to determine whether the two nodes are aligned in a row
     * @param ac    The alignment constraint to analyse
     * @return      1 for yes, 0 or -1 for no.
     */
    private boolean alignedInRow(AlignmentConstraint ac) {
        boolean[] attrs = ac.getAttributes();

        if (attrs[10]) {
            return false;
        }
        if ( (attrs[2] || attrs[3]) ) {
            if (!attrs[0] && !attrs[1]) {
                return true;
            }
        }

        return false;
    }


    private ArrayList<Node> getChildrenOfNode(Node n) {
        ArrayList<Node> children = new ArrayList<>();
        for (AlignmentConstraint ac : rlg.getAlignmentConstraints().values()) {
            if ((ac.getNode1().getXpath().equals(n.getXpath())) && (ac.getType() == Type.PARENT_CHILD)) {
                if (!children.contains(ac.getNode2())) {
                    children.add(ac.getNode2());
                }
            }
        }
        return children;
    }

    public void writeReport(String url, ArrayList<ResponsiveLayoutFailure> errors, String ts) {
        PrintWriter output = null;
        PrintWriter output2 = null;
        PrintWriter output3 = null;
        try {
        	File outputFile = null;
            if (!url.contains("www.") && (!url.contains("http://"))) {
                String[] splits = url.split("/");
                String webpage = splits[0];
                String mutant = "index-" + ts;
                //                    splits[1];
                try {
                    outputFile = new File(new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (url.contains("http://")) {
                String[] splits = url.split("http://");
                String webpage = splits[1];
                String mutant = ts;
                try {
                    outputFile = new File(new java.io.File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                String[] splits = url.split("www.");
                String webpage = splits[1];
                String mutant = ts;
                try {
                    outputFile = new File(new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileUtils.forceMkdir(outputFile);
            File dir = new File(outputFile+"/fault-report.txt");
//            File countDir = new File(outputFile + "/error-count.txt");
//            File typeFile = new File(outputFile + "/error-types.txt");
            File classification = new File(outputFile + "/classification.txt");
            File actualFaultsFile = new File(outputFile + "/../actual-fault-count.txt");
//            classification.createNewFile();
//            actualFaultsFile.createNewFile();
            output = new PrintWriter(dir);
//            output2 = new PrintWriter(countDir);
//            output3 = new PrintWriter(typeFile);
            if (errors.size() > 0) {
//                output2.append(Integer.toString(errors.size()));
                for (ResponsiveLayoutFailure rle : errors) {
                    output.append(rle.toString() + "\n\n");
//                    output3.append(errorToKey(rle) + "\n");
                }
            } else {
                output.append("NO FAULTS DETECTED.");
//                output2.append("0");
            }

            output.close();
//            output2.close();
//            output3.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String errorToKey(ResponsiveLayoutFailure rle) {
        if (rle instanceof SmallRangeFailure) {
            return "SR";
        } else if (rle instanceof ViewportProtrusionFailure) {
            return "VO";
        } else if (rle instanceof ElementProtrusionFailure) {
            return "OF";
        } else if (rle instanceof CollisionFailure) {
            return "OL";
        } else if (rle instanceof WrappingFailure) {
            return "W";
        } else if (rle instanceof MisalignedFailure) {
            return "M";
        }
        return "NULL";
    }
    
    public ResponsiveLayoutGraph getRlg() {
    	return rlg;
    }
}
