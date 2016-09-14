package twalsh.analysis;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.internal.WrapsDriver;
import twalsh.layout.Element;
import twalsh.layout.Layout;
import twalsh.layout.LayoutFactory;
import twalsh.layout.Sibling;
import twalsh.redecheck.RLGThread;
import twalsh.rlg.AlignmentConstraint;
import twalsh.rlg.Node;
import twalsh.rlg.ResponsiveLayoutGraph;
import twalsh.rlg.Type;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import static java.lang.Math.abs;
import static org.apache.commons.math3.util.CombinatoricsUtils.factorial;

/**
 * Created by thomaswalsh on 20/05/2016.
 */
public class RLGAnalyser {
    ResponsiveLayoutGraph rlg;
    ArrayList<ResponsiveLayoutError> errors;
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
    }

    public ArrayList<ResponsiveLayoutError> analyse() {
        errors = new ArrayList<>();

        checkForViewportOverflows();
//        checkForOverflowingElements();
//        checkForOverlappingElements();
        detectOverflowOrOverlap();
        checkForSmallRanges();
        checkForWrappingElements();
        filterOutDuplicateReports();
        return errors;
    }



    private void filterOutDuplicateReports() {



//        ArrayList<ResponsiveLayoutError> cloned0 = (ArrayList<ResponsiveLayoutError>) errors.clone();
//
//        for (ResponsiveLayoutError rle : cloned0) {
//            if (rle instanceof SmallRangeError) {
//                for (ResponsiveLayoutError rle2 : cloned0) {
//                    if (rle2 instanceof OverlappingError) {
//                        if (((SmallRangeError) rle).ac == ((OverlappingError) rle2).constraint) {
//                            errors.remove(rle);
//                        }
//                    }
//                    if (rle2 instanceof OverflowError) {
//                        if (((SmallRangeError) rle).ac == ((OverflowError) rle2).getOfCon()) {
//                            errors.remove(rle);
//                        }
//                    }
//                }
//            }
//        }

        ArrayList<ResponsiveLayoutError> cloned1 = (ArrayList<ResponsiveLayoutError>) errors.clone();

        for (ResponsiveLayoutError rle : cloned1) {
            if (rle instanceof OverlappingError) {
                AlignmentConstraint ac = ((OverlappingError) rle).getConstraint();
                int chosenWidth = getWidthWithinRange(ac.getMin(), ac.getMax(), layouts);
                LayoutFactory lf = layouts.get(chosenWidth);
                Layout l = lf.layout;
                HashMap<String, Element> elements = l.getElements();
                Element e1 = elements.get(ac.getNode1().getXpath());
                Element e2 = elements.get(ac.getNode2().getXpath());
                int[] c1 = e1.getCoordsArray();
                int[] c2 = e2.getCoordsArray();

                Rectangle r1 = new Rectangle(c1[0], c1[1], c1[2] - c1[0], c1[3] - c1[1]);
                Rectangle r2 = new Rectangle(c2[0], c2[1], c2[2] - c2[0], c2[3] - c2[1]);
                Rectangle intersection = r1.intersection(r2);
                if (intersection.getWidth() == 1 || intersection.getHeight() == 1) {
                    System.out.println("TINY INTERSECTION ON " + ac);
                    errors.remove(rle);
                }
            }
        }

        ArrayList<ResponsiveLayoutError> cloned2 = (ArrayList<ResponsiveLayoutError>) errors.clone();
        for (ResponsiveLayoutError rle : cloned2) {
            if (rle instanceof OverflowError) {

                AlignmentConstraint ac = ((OverflowError) rle).getOfCon();
//                AlignmentConstraint m = ((OverflowError) rle).getMatch();
                Node overflowed = ((OverflowError) rle).getOverflowed();
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

                int diffR = e.getCoordsArray()[2] - ip.getCoordsArray()[2];
                int diffB = e.getCoordsArray()[3] - ip.getCoordsArray()[3];
                int diffL = ip.getCoordsArray()[0]-e.getCoordsArray()[0];
                int diffT = ip.getCoordsArray()[1]-e.getCoordsArray()[1];

                if (diffR == 1 || diffB == 1 || diffL == 1 || diffT == 1) {
//                    System.out.println("One pixel overflow");
                    errors.remove(rle);
                }
//                else if (parentAlsoOverflowed(((OverflowError) rle).getOverflowed(), cloned2)) {
////                    System.out.println("REMOVING " + rle+ " BECAUSE OF PARENT OVERFLOW");
//                    errors.remove(rle);
//                }
            }
        }
    }

    private void checkForViewportOverflows() {
        for (Node n : rlg.getNodes().values()) {
            ArrayList<AlignmentConstraint> pCons = n.getParentConstraints();
            int visMin = n.getVisibilityConstraints().get(0).appear;
            int visMax = n.getVisibilityConstraints().get(0).disappear;
            TreeMap<Integer, Integer> conBounds = new TreeMap<>();
            for (AlignmentConstraint pc : pCons) {
                conBounds.put(pc.getMin(), pc.getMax());
            }
            int prevMax = 0;
            for (Map.Entry e : conBounds.entrySet()) {
                if ((int) e.getKey() != prevMax+1 && (prevMax != 0)) {
//                    IS IT VISIBLE
                    ViewportOverflowError voe = new ViewportOverflowError(n, prevMax+1, (int) e.getKey());
                    errors.add(voe);
//
                }
                prevMax = (int) e.getValue();
            }
        }
    }

    private void detectOverflowOrOverlap() {
        for (AlignmentConstraint ac : rlg.getAlignmentConstraints().values()) {
            if (ac.getType() == Type.SIBLING) {
                if (ac.getAttributes()[10]) {


                    HashSet<Node> n1Ancestry = getAncestry(ac.getNode1(), ac.getMax()+1);
                    HashSet<Node> n2Ancestry = getAncestry(ac.getNode2(), ac.getMax()+1);
                    if (n1Ancestry.contains(ac.getNode2())) {
                        OverflowError ofe = new OverflowError(ac.getNode1(), ac);
                        errors.add(ofe);
                    } else if (n2Ancestry.contains(ac.getNode1())) {
                        OverflowError ofe = new OverflowError(ac.getNode2(), ac);
                        errors.add(ofe);
                    } else {
                        AlignmentConstraint prev = getPreviousOrNextConstraint(ac, true);
                        AlignmentConstraint next = getPreviousOrNextConstraint(ac, false);
                        if (ac.getNode2().getXpath().equals("/HTML/BODY/DIV/HEADER/DIV/FORM/BUTTON")) {
                            System.out.println();
                        }
                        if (prev != null && prev.getType() == Type.SIBLING) {
                            if (prev.getAttributes()[10] == false) {
                                OverlappingError oe = new OverlappingError(ac);
                                errors.add(oe);
                            }
                        } else if (next != null && next.getType() == Type.SIBLING) {
                            if (next.getAttributes()[10] == false) {
                                OverlappingError oe = new OverlappingError(ac);
                                errors.add(oe);
                            }
                        }
                    }
                }
            }
        }
    }

    private HashSet<Node> getAncestry(Node node1, int i) {
        HashSet<Node> ancestors = new HashSet<>();

        ArrayList<Node> workList = new ArrayList<>();
        workList.add(node1);

        while (!workList.isEmpty()) {
            Node n = workList.remove(0);
            ArrayList<AlignmentConstraint> cons = n.getParentConstraints();
            for (AlignmentConstraint ac : cons) {
                if (ac.getMin()<=i && ac.getMax() >= i) {
                    ancestors.add(ac.getNode1());
                    workList.add(ac.getNode1());
                }
            }
        }
        return ancestors;
    }


    private ArrayList<AlignmentConstraint> getParentChildConstraints(Node node1, Node node2) {
        ArrayList<AlignmentConstraint> toReturn = new ArrayList<>();
        for (AlignmentConstraint ac : rlg.getAlignmentConstraints().values()) {
            if (ac.getType() == Type.PARENT_CHILD) {
                if (ac.getNode1() == node1 && ac.getNode2() == node2) {
                    toReturn.add(ac);
                } else if (ac.getNode1() == node2 && ac.getNode2() == node1) {
                    toReturn.add(ac);
                }
            }
        }
        return toReturn;
    }

    private void checkForOverlappingElements() {
        for (AlignmentConstraint ac : rlg.getAlignmentConstraints().values()) {
            if (ac.getType() == Type.SIBLING) {
                if (!onePixelOverflows.contains(ac.getNode1()) && !onePixelOverflows.contains(ac.getNode2())) {
                    if (ac.getAttributes()[10]) {
                        try {
//                            int captureWidth = (ac.getMin() + ac.getMax()) / 2;
//                            HashMap<Integer, LayoutFactory> lfs = new HashMap<Integer, LayoutFactory>();
//                            BufferedImage img = RLGThread.getScreenshot(captureWidth, -1, lfs, driver, url);
//                            LayoutFactory lf = lfs.get(captureWidth);
//                            Layout l = lf.layout;
//                            HashMap<String, Element> elements = l.getElements();
//                            Element e1 = elements.get(ac.getNode1().getXpath());
//                            Element e2 = elements.get(ac.getNode2().getXpath());
//                            int[] c1 = e1.getCoordsArray();
//                            int[] c2 = e2.getCoordsArray();
//
//                            Rectangle r1 = new Rectangle(c1[0], c1[1], c1[2] - c1[0], c1[3] - c1[1]);
//                            Rectangle r2 = new Rectangle(c2[0], c2[1], c2[2] - c2[0], c2[3] - c2[1]);
//                            Rectangle intersection = r1.intersection(r2);
////                        System.out.println(intersection);
//
//                            HashMap<Integer, Integer> colourCounts = new HashMap<>();
//                            for (int x = intersection.x; x < intersection.x + intersection.width; x++) {
//                                for (int y = intersection.y; y < intersection.y + intersection.height; y++) {
//                                    try {
//                                        int clr = img.getRGB(x, y);
//                                        if (!colourCounts.containsKey(clr)) {
//                                            colourCounts.put(clr, 0);
//                                        }
//                                        int currentCount = colourCounts.get(clr);
//                                        colourCounts.put(clr, currentCount + 1);
//                                    } catch (ArrayIndexOutOfBoundsException e) {
//                                    }
//
//
//                                }
//                            }
//                            if (colourCounts.size() > 1) {
//                                int totalPixels = intersection.height * intersection.width;
//                                int largest = 0;
//                                for (Integer i : colourCounts.keySet()) {
//                                    if (largest == 0) {
//                                        largest = i;
//                                    } else if (colourCounts.get(i) > colourCounts.get(largest)) {
//                                        largest = i;
//                                    }
//                                }
//                                float percent = (float) colourCounts.get(largest) / totalPixels;
//                                if (percent < 0.9) {
//                                if (!checkForRelatedOverflowErrors(ac)) {

                                    OverlappingError oe = new OverlappingError(ac);
//                                    System.out.println(oe);
                                    errors.add(oe);
//                                }

//                                }
//                                }
//                            }
                        } catch (Exception e) {
                            System.out.println("ERROR WITH " + ac);
                        }
                    }
                }
            }
        }
    }

    private boolean checkForRelatedOverflowErrors(AlignmentConstraint ac) {
        Node ac1 = ac.getNode1();
        Node ac2 = ac.getNode2();

        for (ResponsiveLayoutError e : errors) {
            if (e instanceof OverflowError) {
                Node overflowed = ((OverflowError) e).getOverflowed();

//                for (Node intended : ((OverflowError) e).getMap().keySet()) {
                    if ((ac1.getXpath().equals(overflowed.getXpath()) || ac2.getXpath().equals(overflowed.getXpath()))) {
//                        System.out.println("Wanted to remove " + ac + " because of " + overflowed.getXpath());
//                        System.out.println(overflowed.getXpath());
//                        System.out.println();
                        return true;
                    }
//                }
            }
        }
        return false;
    }

    private void checkForSmallRanges() {
        for (AlignmentConstraint ac : rlg.getAlignmentConstraints().values()) {
            if ( (ac.getMax() - ac.getMin()) == 0 ) {
                AlignmentConstraint prev = getPreviousOrNextConstraint(ac, true);
                AlignmentConstraint next = getPreviousOrNextConstraint(ac, false);
                if (prev != null && next != null) {
//                if (ac.getMin() != vmin && ac.getMax() != vmax) {
                    SmallRangeError sre = new SmallRangeError(ac, prev, next);
                    errors.add(sre);
                }
            }
        }
    }

    private AlignmentConstraint getPreviousOrNextConstraint(AlignmentConstraint ac, boolean i) {
        String ac1xp = ac.getNode1().getXpath();
        String ac2xp = ac.getNode2().getXpath();

        for (AlignmentConstraint con : rlg.getAlignmentConstraints().values()) {
//            if (con.getType() == ac.getType()) {
                String con1xp = con.getNode1().getXpath();
                String con2xp = con.getNode2().getXpath();
                if ( (ac1xp.equals(con1xp) && ac2xp.equals(con2xp)) || (ac1xp.equals(con2xp) && ac2xp.equals(con1xp)) ) {
                    if (i) {
                        if (con.getMax() == ac.getMin()-1) {
                            return con;
                        }
                    } else {
                        if (con.getMin() == ac.getMax()+1) {
                            return con;
                        }
                    }
                }
//            }
        }
        return null;
    }

    private void checkForOverflowingElements() {

        for (Node n : rlg.getNodes().values()) {
            ArrayList<AlignmentConstraint> pCons = n.getParentConstraints();
            if (pCons.size() > 1) {
                // Check the parent nodes are the same for each one.
                HashMap<Node, ArrayList<AlignmentConstraint>> grouped = new HashMap<>();
                for (int i = 0; i < pCons.size(); i++) {
                    AlignmentConstraint nextCon = pCons.get(i);
                    if (!grouped.containsKey(nextCon.getNode1())) {
                        grouped.put(nextCon.getNode1(), new ArrayList<AlignmentConstraint>());
                    }
                    grouped.get(nextCon.getNode1()).add(nextCon);
                }
                if (grouped.size() > 1) {
                    String ipXPath = "";
                    Node intendedParent = null;

                    // Work out the intended parent based upon the length's of the node's XPath expressions
                    for (Node nn : grouped.keySet()) {
                        int countNN = StringUtils.countMatches(nn.getXpath(), "/");
                        int countIP = StringUtils.countMatches(ipXPath, "/");
                        if ((grouped.get(nn).get(0).getNode2().getXpath()).contains(nn.getXpath()) && ((countNN > countIP) )) {
//                                || ipXPath.equals(""))) {
                            if (nn.getXpath().length() > ipXPath.length()) {
                                intendedParent = nn;
                                ipXPath = nn.getXpath();
                            }
                        }
                    }

                    if (intendedParent == null) {
//                        System.out.println();
                        intendedParent = pCons.get(0).getNode1();
                        ipXPath = intendedParent.getXpath();
                    }

//                    ArrayList<AlignmentConstraint> ol = getOverlappingConstraints(n);
                    boolean onePxOverflow = false;

                    for (Node nn : grouped.keySet()) {
                        try {
                            if (!nn.getXpath().equals(intendedParent.getXpath())) {

                                // CHECK HERE FOR OVERLAPPING PREVIOUS PARENT!!!

                                AlignmentConstraint ac = grouped.get(nn).get(0);

                                // Check if intended parent is visible
                                if (intendedParentVisible(intendedParent, grouped.get(nn).get(0))) {
                                    int captureWidth =  (ac.getMin() + ac.getMax()) / 2;
//                                    HashMap<Integer, LayoutFactory> lfs = new HashMap<Integer, LayoutFactory>();

//                                    BufferedImage img = RLGThread.getScreenshot(captureWidth, -1, lfs, driver, url);
                                    int chosenWidth = getWidthWithinRange(ac.getMin(), ac.getMax(), layouts);
                                    LayoutFactory lf = layouts.get(chosenWidth);
                                    Layout l = lf.layout;
                                    HashMap<String, Element> elements = l.getElements();
                                    Element e1 = elements.get(n.getXpath());
                                    Element ip = elements.get(intendedParent.getXpath());

                                    int diffR = ip.getCoordsArray()[2]-e1.getCoordsArray()[2];
                                    int diffB = ip.getCoordsArray()[3]-e1.getCoordsArray()[3];
                                    int diffL = e1.getCoordsArray()[0]-ip.getCoordsArray()[0];
                                    int diffT = e1.getCoordsArray()[1]-ip.getCoordsArray()[1];


//
                                    if (diffR > 1 || diffB > 1) {
//                                        if (checkNodeOverlappingIntendedParent(ol, n, intendedParent)) {
                                            OverflowError oe = new OverflowError(grouped, intendedParent, n);
                                            errors.add(oe);
//                                            System.out.println();
//                                        }
                                    } else {
                                        System.out.println(n + "overflowed by one");
                                        onePixelOverflows.add(n);
                                    }

                                }
                            }
                        } catch (NullPointerException npe) {
                            npe.printStackTrace();
                        }
                    }

                    if (!onePxOverflow) {
                        ArrayList<AlignmentConstraint> ol = getOverlappingConstraints(n);
                        if (checkNodeOverlappingIntendedParent(ol, n, intendedParent)) {
                            OverflowError oe = new OverflowError(grouped, intendedParent, n);
                            errors.add(oe);
                        }
                    }
                }
            }
        }

    }

    private int getWidthWithinRange(int min, int max, HashMap<Integer, LayoutFactory> layouts) {
        for (Integer i : layouts.keySet()) {
            if (i >= min && i <= max) {
                return i;
            }
        }
        return (min+max)/2;
    }

    private boolean parentAlsoOverflowed(Node overflowed, ArrayList<ResponsiveLayoutError> cloned) {
        String overflowedXP = overflowed.getXpath();

        for (ResponsiveLayoutError e : cloned) {
            if (e instanceof OverflowError) {
                String comparisonXP = ((OverflowError) e).getOverflowed().getXpath();
                if (overflowedXP.contains(comparisonXP) && !overflowedXP.equals(comparisonXP)) {
//                    System.out.println("Matched " + overflowedXP + " to " + comparisonXP);
                    return true;
                }
            } else if (e instanceof ViewportOverflowError) {
                String comparisonXP = ((ViewportOverflowError) e).getNode().getXpath();
                if (overflowedXP.contains(comparisonXP) || overflowedXP.equals(comparisonXP)) {
//                    System.out.println("Matched " + overflowedXP + " to " + comparisonXP);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkNodeOverlappingIntendedParent(ArrayList<AlignmentConstraint> ol, Node n, Node ip) {
        for (AlignmentConstraint ac : ol) {
            Node overlappingNode = null;
            if (ac.getNode1() == n && ac.getNode2() == ip) {
                System.out.print(ac);
                return true;
            } else if (ac.getNode2() == n && ac.getNode1() == ip) {
                System.out.print(ac);
                return true;
            }

//            ArrayList<AlignmentConstraint> pCons = overlappingNode.getParentConstraints();
//            for (AlignmentConstraint pc : pCons) {
//                if ((pc.getMin() <= ac.getMax()) && (ac.getMin() <= pc.getMax())) {
//                    if (pc.getNode1().getXpath().equals(newP.getXpath())) {
//                        System.out.println(ac);
//                        System.out.println(pc);
//                        return true;
//                    }
//                }
//            }

        }
        return false;
    }

    private boolean checkOverlapChildOfNewParent(ArrayList<AlignmentConstraint> ol, Node n, Node newP) {
        for (AlignmentConstraint ac : ol) {
            Node overlappingNode = null;
            if (ac.getNode1() == n) {
                overlappingNode = ac.getNode2();
            } else {
                overlappingNode = ac.getNode1();
            }

            ArrayList<AlignmentConstraint> pCons = overlappingNode.getParentConstraints();
            for (AlignmentConstraint pc : pCons) {
                if ((pc.getMin() <= ac.getMax()) && (ac.getMin() <= pc.getMax())) {
                    if (pc.getNode1().getXpath().equals(newP.getXpath())) {
                        System.out.println(ac);
                        System.out.println(pc);
                        return true;
                    }
                }
            }
            System.out.println();

        }
        return false;
    }

    private ArrayList<AlignmentConstraint> getOverlappingConstraints(Node n) {
        String target = n.getXpath();
        ArrayList<AlignmentConstraint> cons = new ArrayList<>();
        for (AlignmentConstraint ac : rlg.getAlignmentConstraints().values()) {
            if (ac.getType() == Type.SIBLING) {
                String n1x = ac.getNode1().getXpath();
                String n2x = ac.getNode2().getXpath();
                if (n1x.equals(target) || n2x.equals(target)) {
                    if (ac.getAttributes()[10]) {
                        cons.add(ac);
                    }
                }
            }
        }
        return cons;
    }

    private boolean intendedParentVisible(Node intendedParent, AlignmentConstraint ac) {
        int ipMin = intendedParent.getVisibilityConstraints().get(0).appear;
        int ipMax = intendedParent.getVisibilityConstraints().get(0).disappear;
        Rectangle r1 = new Rectangle(ipMin, 0, ipMax-ipMin, 10);
        Rectangle r2 = new Rectangle(ac.getMin(), 0, ac.getMax()-ac.getMin(), 10);
        return (r1.intersects(r2));
    }


    private void checkForWrappingElements() {
        for (Node n : rlg.getNodes().values()) {
            ArrayList<Node> children = getChildrenOfNode(n);
            if (children.size() > 1) {
                TreeSet<Integer> values = new TreeSet<>();
                TreeSet<Integer> pcValues = new TreeSet<>();
                ArrayList<AlignmentConstraint> sibs = new ArrayList<>();
                for (AlignmentConstraint ac : rlg.getAlignmentConstraints().values()) {
                    if (ac.getType() == Type.SIBLING) {
                        if ((children.contains(ac.getNode1())) && (children.contains(ac.getNode2()))) {
                            if (childrenWithinParent(ac, n)) {
                                sibs.add(ac);
                                values.add(ac.getMin());
                                values.add(ac.getMax());
                            }
                        }
                    }
                }
//
//                if (n.getXpath().equals("/HTML/BODY/DIV[2]/DIV[3]/UL")) {
//                    System.out.println("BOO");
//                }
                ArrayList<String> keys = extractRanges(values);
                ArrayList<String> pcKeys = extractRanges(pcValues);

                // Match based on bounds
                HashMap<String, HashSet<AlignmentConstraint>> grouped = new HashMap<>();
                for (String k : keys) {
                    grouped.put(k, new HashSet<AlignmentConstraint>());
                }
                HashMap<String, ArrayList<AlignmentConstraint>> pcGrouped = new HashMap<>();
                for (String pck : pcKeys) {
                    pcGrouped.put(pck, new ArrayList<AlignmentConstraint>());
                }

                // Group both the sibling and parent-child constraints
                for (AlignmentConstraint ac : sibs) {
                    putConstraintIntoGroups(grouped, ac);
                }

                HashMap<String, ArrayList<ArrayList<Node>>> totalRows = new HashMap<>();
                HashMap<String, ArrayList<ArrayList<Node>>> totalCols = new HashMap<>();
                HashMap<String, ArrayList<Node>> totalNotInRows = new HashMap<>();
                HashMap<String, HashMap<String, ArrayList<AlignmentConstraint>>> totalRowCons = new HashMap<>();
                HashMap<String, HashMap<String, ArrayList<AlignmentConstraint>>> totalColCons = new HashMap<>();
                HashMap<String, HashSet<Node>> nodesInParentMap = new HashMap<>();



                for (String key : grouped.keySet()) {
//                    System.out.println(key);
                    nodesInParentMap.put(key, new HashSet<Node>());
                    try {
                        // Try and put elements into rows
                        ArrayList<ArrayList<Node>> rows = new ArrayList<>();
                        ArrayList<ArrayList<Node>> columns = new ArrayList<>();
                        HashMap<String, ArrayList<AlignmentConstraint>> rowSibConstraints = new HashMap<>();
                        HashMap<String, ArrayList<AlignmentConstraint>> rowPCConstraints = new HashMap<>();

                        HashMap<String, ArrayList<AlignmentConstraint>> colSibConstraints = new HashMap<>();
                        HashMap<String, ArrayList<AlignmentConstraint>> colPCConstraints = new HashMap<>();
                        ArrayList<Node> nodesNotInRows = (ArrayList<Node>) children.clone();
                        ArrayList<Node> nodesNotInColumns = (ArrayList<Node>) children.clone();
                        HashSet<AlignmentConstraint> fullSet = grouped.get(key);
                        HashSet<AlignmentConstraint> overlapping = new HashSet<>();

                        for (AlignmentConstraint ac : fullSet) {
                            nodesInParentMap.get(key).add(ac.getNode1());
                            nodesInParentMap.get(key).add(ac.getNode2());
                            int toggle = alignedInRowOrColumn(ac);

                            if (toggle != 0) {
                                ArrayList<Node> match = matchingExistingPattern(ac, rows, columns, toggle, fullSet);
                                if (match == null) {
                                    // Create a new row with both elements in it.
                                    ArrayList<Node> newRowCol = new ArrayList<>();
                                    newRowCol.add(ac.getNode1());
                                    newRowCol.add(ac.getNode2());

                                    if (toggle == 1) {
                                        rows.add(newRowCol);
                                        rowSibConstraints.put(setOfNodesToString(newRowCol), new ArrayList<AlignmentConstraint>());
                                        rowSibConstraints.get(setOfNodesToString(newRowCol)).add(ac);
                                    } else {
                                        columns.add(newRowCol);
                                        colSibConstraints.put(setOfNodesToString(newRowCol), new ArrayList<AlignmentConstraint>());
                                        colSibConstraints.get(setOfNodesToString(newRowCol)).add(ac);
                                    }
                                } else {
                                    // Add the remaining element to the row
                                    String matchKey = setOfNodesToString(match);
                                    if (!match.contains(ac.getNode1())) {
                                        match.add(ac.getNode1());
                                    } else if (!match.contains(ac.getNode2())) {
                                        match.add(ac.getNode2());
                                    }
                                    if (toggle == 1) {
                                        ArrayList<AlignmentConstraint> cons = rowSibConstraints.get(matchKey);
                                        rowSibConstraints.remove(matchKey);
                                        rowSibConstraints.put(setOfNodesToString(match), cons);
                                        rowSibConstraints.get(setOfNodesToString(match)).add(ac);
                                    } else {
                                        ArrayList<AlignmentConstraint> cons = colSibConstraints.get(matchKey);
                                        colSibConstraints.remove(matchKey);
                                        colSibConstraints.put(setOfNodesToString(match), cons);
                                        colSibConstraints.get(setOfNodesToString(match)).add(ac);
                                    }
                                }

                                // Remove from ArrayLists to detect elements not placed into row or column
                                if (toggle == 1) {
                                    nodesNotInRows.remove(ac.getNode1());
                                    nodesNotInRows.remove(ac.getNode2());
                                } else {
                                    nodesNotInColumns.remove(ac.getNode1());
                                    nodesNotInColumns.remove(ac.getNode2());
                                }

                            } else if (toggle == 0) {
                                overlapping.add(ac);
                            }
                        }



                        // Filter elements that are in rows and the same column. CAN HAPPEN, TRUST ME!
                        for (ArrayList<Node> colToFilter : columns) {

                            ArrayList<Node> temp = (ArrayList<Node>) colToFilter.clone();
                            for (Node f1 : temp) {
                                for (Node f2 : temp) {
                                    if (f1 != f2) {
                                        if (elementsAlsoInRow(f1, f2, rows)) {
                                            // Need to remove from the column
                                            colToFilter.remove(f1);
                                            colToFilter.remove(f2);
                                            removeConstraintsFromCol(f1, f2, colSibConstraints, colToFilter);
                                        }
                                    }
                                }
                            }
                        }

                        for (ArrayList<Node> rowToFilter : rows) {

                            ArrayList<Node> temp = (ArrayList<Node>) rowToFilter.clone();
                            for (Node f1 : temp) {
                                for (Node f2 : temp) {
                                    if (f1 != f2) {
                                        if (elementsAlsoInRow(f1, f2, columns)) {
                                            // Need to remove from the column
                                            rowToFilter.remove(f1);
                                            rowToFilter.remove(f2);
                                            removeConstraintsFromCol(f1, f2, rowSibConstraints, rowToFilter);
                                        }
                                    }
                                }
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


                            ArrayList<ArrayList<Node>> clonedCols = (ArrayList<ArrayList<Node>>) columns.clone();
                            for (ArrayList<Node> col : clonedCols) {
                                if (col.contains(acOV.getNode1()) && col.contains(acOV.getNode2())) {
                                    ArrayList<Node> actualColumn = columns.get(clonedCols.indexOf(col));
                                    actualColumn.remove(acOV.getNode1());
                                    actualColumn.remove(acOV.getNode2());
                                    removeConstraintsFromCol(acOV.getNode1(), acOV.getNode2(), colSibConstraints, col);
                                }
                            }
                        }

                        totalRows.put(key, rows);
                        totalCols.put(key, columns);
                        totalNotInRows.put(key, nodesNotInRows);
                        totalRowCons.put(key, rowSibConstraints);
                        totalColCons.put(key, colSibConstraints);



                        // Inspect the constraints for each row, to see if any are out of alignment



                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                for (String key: totalRows.keySet()) {
                    ArrayList<ArrayList<Node>> rows = totalRows.get(key);
                    ArrayList<ArrayList<Node>> cols = totalCols.get(key);
                    ArrayList<Node> not = totalNotInRows.get(key);
                    HashMap<String, ArrayList<AlignmentConstraint>> consRow = totalRowCons.get(key);
                    HashMap<String, ArrayList<AlignmentConstraint>> consCol = totalColCons.get(key);
                    for (Node notInRow : not) {

                        // Need to refine this to the entire row becoming a column!
                        if (rows.size() > 0) {
                            ArrayList<Node> nonWrappedRow = inRowInNextRange(notInRow, totalRows, key);
                            if (nonWrappedRow != null) {
                                if (elementVisible(notInRow, key)) {
                                    if (elementStillWithinParent(notInRow, n, key)) {
                                        WrappingError we = new WrappingError(notInRow, nonWrappedRow, getNumberFromKey(key, 0), getNumberFromKey(key, 1));
                                        errors.add(we);
                                    }
                                }
                            }
                        } else {

                        }
                    }
                    checkAlignments(consRow, key, true);
                    checkAlignments(consCol, key, false);
                }


            }
        }
    }

    private ArrayList<Node> inRowInNextRange(Node notInRow, HashMap<String, ArrayList<ArrayList<Node>>> rows, String key) {
        int x = getNumberFromKey(key,1);
        for (String key2 : rows.keySet()) {
            if (!key.equals(key2)) {
                int first = getNumberFromKey(key2, 0);
                if (first - x == 1) {
                    ArrayList<ArrayList<Node>> rs = rows.get(key2);
                    for (ArrayList<Node> r : rs) {
                        if (r.contains(notInRow)) {
                            return r;
                        }
                    }
                }

            }
        }
        return null;
    }

    private boolean inColumnInstead(ArrayList<ArrayList<Node>> cols, Node notInRow) {
        for (ArrayList<Node> col : cols) {
            if (col.contains(notInRow)) {
//                System.out.println(notInRow.getXpath() + " was in a column instead");
                return true;
            }
        }
        return false;
    }

    private boolean elementStillWithinParent(Node wrapped, Node n, String key) {
        int kMin = getNumberFromKey(key, 0);
        int kMax = getNumberFromKey(key, 1);
        boolean foundFault = false;

        ArrayList<AlignmentConstraint> pCons = wrapped.getParentConstraints();
        for (AlignmentConstraint con : pCons) {
            if ((con.getMin() <= kMax) && (kMin <= con.getMax())) {
                if (con.getNode1().getXpath().equals(n.getXpath())) {
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

    private boolean elementsAlsoInRow(Node f1, Node f2, ArrayList<ArrayList<Node>> rows) {
        for (ArrayList<Node> row : rows) {
            if (row.contains(f1) && (row.contains(f2))) {
                return true;
            }
        }
        return false;
    }

    private boolean childrenWithinParent(AlignmentConstraint ac, Node n) {
        Node n1 = ac.getNode1();
        Node n2 = ac.getNode2();
//        if (n.getXpath().equals("/HTML/BODY/DIV/FOOTER")) {
//            System.out.println("Debug");
//        }
//        ArrayList<AlignmentConstraint> n1cons = n1.getParentConstraints();
//        ArrayList<AlignmentConstraint> n2cons = n2.getParentConstraints();
        for (Node node : new Node[] {n1, n2}) {
            ArrayList<AlignmentConstraint> cons = node.getParentConstraints();
            if (sameParent(cons)) {
                return true;
            } else {
                for (AlignmentConstraint con : cons) {
                    if ((con.getMin() <= ac.getMin()) && (con.getMax() >= ac.getMax())) {
                        if (con.getNode1().getXpath().equals(n.getXpath())) {
//                        System.out.println(ac);
                            return true;
                        } else {
//                        System.out.println(ac);
//                        System.out.println(n + "\n\n");
                        }
                    }
                }
            }

        }
        return false;
    }

    private boolean sameParent(ArrayList<AlignmentConstraint> cons) {
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

    private void checkAlignments(HashMap<String, ArrayList<AlignmentConstraint>> rowColConstraints, String key, boolean isRow) {
        for (String nodes : rowColConstraints.keySet()) {
            ArrayList<AlignmentConstraint> constraints = rowColConstraints.get(nodes);
            ArrayList<AlignmentConstraint> aligned1 = new ArrayList<>();
            ArrayList<AlignmentConstraint> aligned2 = new ArrayList<>();
            ArrayList<AlignmentConstraint> aligned3 = new ArrayList<>();

            HashSet<Node> alNodes1 = new HashSet<>();
            HashSet<Node> alNodes2 = new HashSet<>();
            HashSet<Node> alNodes3 = new HashSet<>();
            HashSet<Node> totalNodes = new HashSet<>();

            for (AlignmentConstraint ac : constraints) {

                boolean[] attkey = ac.generateAlignmentsOnly();
                boolean att1, att2, att3;
                if (isRow) {
                    att1 = attkey[0];
                    att2 = attkey[1];
                    att3 = attkey[2];
                } else {
                    att1 = attkey[3];
                    att2 = attkey[4];
                    att3 = attkey[5];
                }
                totalNodes.add(ac.getNode1());
                totalNodes.add(ac.getNode2());

                if (att1) {
                    aligned1.add(ac);
                    alNodes1.add(ac.getNode1());
                    alNodes1.add(ac.getNode2());
                }
                if (att2) {
                    aligned2.add(ac);
                    alNodes2.add(ac.getNode1());
                    alNodes2.add(ac.getNode2());
                }
                if (att3) {
                    aligned3.add(ac);
                    alNodes3.add(ac.getNode1());
                    alNodes3.add(ac.getNode2());
                }
            }

            int sum = (totalNodes.size()-1)*(totalNodes.size())/2;
            if (constraints.size() > 1 && constraints.size() == sum) {
                // Check if not all nodes were aligned, and if enough were to consider it a pattern
                HashSet<Node> notAligned = (HashSet<Node>) totalNodes.clone();
                if ((alNodes1.size() < totalNodes.size()) && (alNodes1.size() >= 2)) {
                    for (Node n : alNodes1) {
                        notAligned.remove(n);
                    }
                    if (alNodes1.size() > notAligned.size() && alNodes3.size() < totalNodes.size()) {
                        MisalignedError me = new MisalignedError(alNodes1, notAligned, key, getNumberFromKey(key,0), getNumberFromKey(key,1));
                        errors.add(me);
                    }
                } else if ((alNodes2.size() < totalNodes.size()) && (alNodes2.size() >= 2)) {
                    for (Node n : alNodes2) {
                        notAligned.remove(n);
                    }
                    if (alNodes2.size() > notAligned.size() && alNodes3.size() < totalNodes.size() && alNodes1.size() < totalNodes.size()) {
                        MisalignedError me = new MisalignedError(alNodes2, notAligned, key, getNumberFromKey(key,0), getNumberFromKey(key,1));
                        errors.add(me);
                    }
                }
            }
        }
    }


    private String setOfNodesToString(ArrayList<Node> nodes) {
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

    private void putConstraintIntoGroups(HashMap<String, HashSet<AlignmentConstraint>> grouped, AlignmentConstraint ac) {
        for (String k : grouped.keySet()) {
            int min = getNumberFromKey(k, 0);
            int max = getNumberFromKey(k, 1);
            if ( (ac.getMin() <= min) && (ac.getMax() >= max) ) {
                grouped.get(k).add(ac);
            }
        }
    }

    private ArrayList<String> extractRanges(TreeSet<Integer> values) {
        ArrayList<String> keys = new ArrayList<>();
        int prev=0;
        int curr;
        for (Integer i : values) {
            curr = i;
            if (prev != 0) {
                if (curr-prev != 1) {
                    String key = prev + ":" + curr;
                    keys.add(key);
                }
            }
            prev = curr;
        }
        return keys;
    }

    private ArrayList<AlignmentConstraint> addInAdditionalConstraints(String key, HashMap<String, ArrayList<AlignmentConstraint>> grouped) {
        ArrayList<AlignmentConstraint> original = grouped.get(key);
        if (original != null) {
            ArrayList<AlignmentConstraint> fullSet = (ArrayList<AlignmentConstraint>) original.clone();
            int minO = getNumberFromKey(key, 0);
            int maxO = getNumberFromKey(key, 1);

            for (String key2 : grouped.keySet()) {
                // Not readding the same constraints
                if (!key.equals(key2)) {
                    int minN = getNumberFromKey(key2, 0);
                    int maxN = getNumberFromKey(key2, 1);
                    if ((minN <= minO) && (maxN >= maxO)) {
                        fullSet.addAll(grouped.get(key2));
                    }
                }
            }

            return fullSet;
        }
        return new ArrayList<>();
    }

    private int getNumberFromKey(String key, int i) {
        String[] splits = key.split(":");
        return Integer.valueOf(splits[i]);
    }


    private ArrayList<Node> matchingExistingPattern(AlignmentConstraint ac, ArrayList<ArrayList<Node>> rows, ArrayList<ArrayList<Node>> columns, int rowCol, HashSet<AlignmentConstraint> fullSet) {
        Node n1 = ac.getNode1();
        Node n2 = ac.getNode2();

        ArrayList<AlignmentConstraint> n1cons = getSiblingEdges(n1, fullSet);
        ArrayList<AlignmentConstraint> n2cons = getSiblingEdges(n2, fullSet);

        // Check through all existing rows
        if (rowCol == 1) {
            for (ArrayList<Node> row : rows) {
                for (Node n : row) {
                    for (AlignmentConstraint acon : n1cons) {
                        if ((acon.getNode1().equals(n)) || (acon.getNode2().equals(n))) {
                            if (alignedInRowOrColumn(acon) == 1) {
                                return row;
                            }
                        }
                    }
                    for (AlignmentConstraint acon : n2cons) {
                        if ((acon.getNode1().equals(n)) || (acon.getNode2().equals(n))) {
                            if (alignedInRowOrColumn(acon) == 1) {
                                return row;
                            }
                        }
                    }
                }
            }
        } else if (rowCol == 2) {
            for (ArrayList<Node> col : columns) {
                for (Node n : col) {
                    for (AlignmentConstraint acon : n1cons) {
                        if ((acon.getNode1().equals(n)) || (acon.getNode2().equals(n))) {
                            if (alignedInRowOrColumn(acon) == 2) {
                                return col;
                            }
                        }
                    }
                    for (AlignmentConstraint acon : n2cons) {
                        if ((acon.getNode1().equals(n)) || (acon.getNode2().equals(n))) {
                            if (alignedInRowOrColumn(acon) == 2) {
                                return col;
                            }
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

    private int alignedInRowOrColumn(AlignmentConstraint ac) {
        boolean[] attrs = ac.getAttributes();

        if (attrs[10]) {
            return 0;
        }
        if ( (attrs[2] || attrs[3]) ) {
            if (!attrs[0] && !attrs[1]) {
                return 1;
            }
        } else if ( (attrs[0] || attrs[1]) ) {
            if (!attrs[2] && !attrs[3]) {
                return 2;
            }
        }

        return 0;
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

    public void writeReport(String url, ArrayList<ResponsiveLayoutError> errors, String ts) {
        PrintWriter output = null;
        PrintWriter output2 = null;
        PrintWriter output3 = null;
        try {
            String[] splits = url.split("/");
            String webpage = splits[0];
            String mutant = "index";
//                    splits[1];
            File outputFile = new File("../reports/"  + webpage + "/" + mutant + "-"+ts);
            FileUtils.forceMkdir(outputFile);
            File dir = new File(outputFile+"/fault-report.txt");
            File countDir = new File(outputFile + "/error-count.txt");
            File typeFile = new File(outputFile + "/error-types.txt");
            File classification = new File(outputFile + "/classification.txt");
            File actualFaultsFile = new File(outputFile + "/../actual-fault-count.txt");
            classification.createNewFile();
            actualFaultsFile.createNewFile();
            output = new PrintWriter(dir);
            output2 = new PrintWriter(countDir);
            output3 = new PrintWriter(typeFile);
            if (errors.size() > 0) {
                output2.append(Integer.toString(errors.size()));
                for (ResponsiveLayoutError rle : errors) {
                    output.append(rle.toString() + "\n\n");
                    output3.append(errorToKey(rle) + "\n");
                }
            } else {
                output.append("NO FAULTS DETECTED.");
                output2.append("0");
            }

            output.close();
            output2.close();
            output3.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String errorToKey(ResponsiveLayoutError rle) {
        if (rle instanceof SmallRangeError) {
            return "SR";
        } else if (rle instanceof ViewportOverflowError) {
            return "VO";
        } else if (rle instanceof OverflowError) {
            return "OF";
        } else if (rle instanceof OverlappingError) {
            return "OL";
        } else if (rle instanceof WrappingError) {
            return "W";
        } else if (rle instanceof MisalignedError) {
            return "M";
        }
        return "NULL";
    }
}
