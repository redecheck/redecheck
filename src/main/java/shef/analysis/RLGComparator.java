package shef.analysis;
import com.rits.cloning.Cloner;
import shef.reporting.regression.AlignmentError;
import shef.reporting.regression.Error;
import shef.reporting.regression.VisibilityError;
import shef.reporting.regression.WidthError;
import shef.rlg.*;

import java.io.IOException;
import java.util.*;

import java.io.PrintWriter;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by thomaswalsh on 20/08/15.
 */
public class RLGComparator {
    public ResponsiveLayoutGraph getRlg1() {
        return rlg1;
    }

    public ResponsiveLayoutGraph getRlg2() {
        return rlg2;
    }

    // Instance variables
    public ResponsiveLayoutGraph rlg1,rlg2;
    public HashMap<Node, Node> matchedNodes;
    public Cloner cloner;
    public ArrayList<String> issues;
    public static ArrayList<Error> errors;
    public static ArrayList<String> unmatched;
    public static ArrayList<VisibilityError> vcErrors;
    public static ArrayList<AlignmentError> acErrors;
    static String redecheck = "/Users/thomaswalsh/Documents/Workspace/Redecheck/";
    static String dataDirectory = "/Users/thomaswalsh/Documents/Workspace/main-journal-paper-data/";

    // public static ArrayList<WidthError> wcErrors;
    public int[] defaultWidths;
    TreeSet<Integer> widthsWithMutant;
    /**
     * Constructor for the RLGComparator object
     * @param r1    the oracle RLG
     * @param r2    the test RLG
     */
    public RLGComparator(ResponsiveLayoutGraph r1, ResponsiveLayoutGraph r2, int[] ws) {
        rlg1 = r1;
        rlg2 = r2;
        unmatched = new ArrayList<>();
        vcErrors = new ArrayList<>();
        // wcErrors = new ArrayList<>();
        acErrors = new ArrayList<>();
        errors = new ArrayList<Error>();
        defaultWidths = ws;
        widthsWithMutant = new TreeSet<>();
    }


    /**
     * Executes the overall comparison process
     */
    public void compare() {
        matchedNodes = new HashMap<Node, Node>();
        cloner = new Cloner();
        matchNodes();
    }

    public int[] getSetOfDifferentWidths() {
        for (VisibilityError ve : vcErrors) {
            ArrayList<int[]> errorWidths = ve.calculateRangeOfViewportWidths();
            for (int[] array : errorWidths) {
                for (int x = array[0]; x <=array[1]; x++) {
                    widthsWithMutant.add(x);
                }
            }
        }
        for (AlignmentError ae : acErrors) {
            ArrayList<int[]> errorWidths = ae.calculateRangeOfViewportWidths();
            for (int[] array : errorWidths) {
                for (int x = array[0]; x <=array[1]; x++) {
                    widthsWithMutant.add(x);
                }
            }
        }
        if (widthsWithMutant.size() > 0) {
            Integer[] results = widthsWithMutant.toArray(new Integer[widthsWithMutant.size()]);
            int x = results.length / 2;
            return new int[]{results[0], results[x], results[results.length - 1]};
        } else {
            return new int[] {0,0,0};
        }
    }

    /**
     * Takes a set of matched nodes from the two RLG models and compares all the different constraints on them, producing
     * a list of model differences.
     * @return      the list of model differences between the two versions of the page
     */
    public ArrayList<String> compareMatchedNodes() {
        int counter = 1;
        for (Node n : matchedNodes.keySet()) {
            Node m = matchedNodes.get(n);
            compareVisibilityConstraints(n, m);
            compareAlignmentConstraints(n, m);
            // compareWidthConstraints(n, m);
            counter++;
        }
        return issues;
    }

    /**
     * Matches the nodes from the oracle version to those in the test version, so their constraints can then be compared.
     */
    public void matchNodes() {
        HashMap<String, Node> nodes1 = cloner.deepClone(rlg1.getNodes());
        HashMap<String, Node> nodes2 = cloner.deepClone(rlg2.getNodes());

        // Match the nodes and their min/max values
        for (Node n1 : rlg1.getNodes().values()) {
            String xpath1 = n1.getXpath();
            for (Node n2 : rlg2.getNodes().values()) {
                String xpath2 = n2.getXpath();
                if (xpath1.equals(xpath2)) {
                    matchedNodes.put(n1, n2);
                    nodes1.remove(xpath1);
                    nodes2.remove(xpath2);
                }
            }
        }
        for (Node left1 : nodes1.values()) {
            unmatched.add(left1.getXpath() + " wasn't matched in Graph 2");
        }
        for (Node left2 : nodes2.values()) {
            unmatched.add(left2.getXpath() + " wasn't matched in Graph 1");
        }
    }

    /**
     * Compares the visibility constraints of a pair of matched nodes
     * @param n     the first node being compared
     * @param m     the second node being compared
     */
    public void compareVisibilityConstraints(Node n, Node m) {
        VisibilityConstraint a = n.getVisibilityConstraints().get(0);
        VisibilityConstraint b = m.getVisibilityConstraints().get(0);
        if ((a.appear != b.appear) || (a.disappear != b.disappear)) {
            VisibilityError ve = new VisibilityError(n, m);
            vcErrors.add(ve);
        }
    }

    /**
     * Compares the alignment constraints of a pair of matched nodes
     * @param n     the first node being compared
     * @param m     the second node being compared
     */
    public void compareAlignmentConstraints(Node n, Node m) {
        ArrayList<AlignmentConstraint> ac1 = new ArrayList<AlignmentConstraint>(), ac2 = new ArrayList<AlignmentConstraint>();
        ArrayList<AlignmentConstraint> ac1f = new ArrayList<AlignmentConstraint>(), ac2f = new ArrayList<AlignmentConstraint>();
        // Get all the alignment constraints for the matched nodes from the two graphs
        for (AlignmentConstraint a : rlg1.getAlignmentConstraints().values()) {
            if (a.node1.getXpath().equals(n.getXpath())) {
                ac1.add(a);
            }
            if (a.node2.getXpath().equals(n.getXpath())) {
                ac1f.add(a);
            }
        }

        for (AlignmentConstraint b : rlg2.getAlignmentConstraints().values()) {
            if (b.node1.getXpath().equals(m.getXpath())) {
                ac2.add(b);
            }
            if (b.node2.getXpath().equals(m.getXpath())) {
                ac2f.add(b);
            }
        }

        HashMap<AlignmentConstraint, AlignmentConstraint> matched = new HashMap<AlignmentConstraint, AlignmentConstraint>();
        ArrayList<AlignmentConstraint> unmatched1 = new ArrayList<AlignmentConstraint>(), unmatched2 = new ArrayList<AlignmentConstraint>();
        while (ac1.size() > 0) {
            AlignmentConstraint ac = ac1.remove(0);
            AlignmentConstraint match = null;
            for (AlignmentConstraint temp : ac2) {
                if ( (temp.node1.getXpath().equals(ac.node1.getXpath())) && (temp.node2.getXpath().equals(ac.node2.getXpath()))) {
                    if ((temp.getMin() == ac.getMin()) && (temp.getMax() == ac.getMax()) && (Arrays.equals(ac.getAttributes(), temp.getAttributes()))) {
                        match = temp;
                    } else if ((temp.getMin()== ac.getMin()) && (temp.getMax() == ac.getMax()) && (!Arrays.equals(ac.getAttributes(), temp.getAttributes()))){
                        AlignmentError ae = new AlignmentError(ac, temp, "diffAttributes");
                        acErrors.add(ae);
                        match = temp;
                    } else if ( (Arrays.equals(ac.getAttributes(), temp.getAttributes())) && ((temp.getMin() != ac.getMin()) || (temp.getMax() != ac.getMax())) ) {
                        AlignmentError ae = new AlignmentError(ac, temp, "diffBounds");
                        acErrors.add(ae);
                        match = temp;
                    }
                }
            }
            if (match != null) {
                matched.put(ac, match);
                ac2.remove(match);
            } else {
                // Check for a flipped match
                for (AlignmentConstraint f : ac2f) {
                    if ((ac.node1.getXpath().equals(f.node2.getXpath())) && (ac.node2.getXpath().equals(f.node1.getXpath()))) {
                        if ((f.getMin() == ac.getMin()) && (f.getMax() == ac.getMax()) && (ac.generateLabelling().equals(f.generateFlippedLabelling()))) {
                            match = f;
                        } else if ((f.getMin()== ac.getMin()) && (f.getMax() == ac.getMax()) && (!ac.generateLabelling().equals(f.generateFlippedLabelling()))){
                            AlignmentError ae = new AlignmentError(ac, f, "diffAttributes");
                            acErrors.add(ae);
                            match = f;
                        } else if ( (ac.generateLabelling().equals(f.generateFlippedLabelling())) && ((f.getMin() != ac.getMin()) || (f.getMax() != ac.getMax())) ) {
                            AlignmentError ae = new AlignmentError(ac, f, "diffBounds");
                            acErrors.add(ae);
                            match = f;
                        }

                    }
                }
                if (match != null) {
                    matched.put(ac, match);
                    ac2.remove(match);
                } else {
                    AlignmentError ae = new AlignmentError(ac, null, "unmatched-oracle");
                    acErrors.add(ae);
                }
            }
        }
        AlignmentConstraint match2 = null;
        for (AlignmentConstraint acUM : ac2) {
            for (AlignmentConstraint f2 : ac1f) {
                if ((acUM.node1.getXpath().equals(f2.node2.getXpath())) && (acUM.node2.getXpath().equals(f2.node1.getXpath()))) {
                    if ((f2.getMin() == acUM.getMin()) && (f2.getMax() == acUM.getMax()) && (acUM.generateLabelling().equals(f2.generateFlippedLabelling()))) {
                        match2 = f2;
                    } else if ((f2.getMin()== acUM.getMin()) && (f2.getMax() == acUM.getMax()) && (!acUM.generateLabelling().equals(f2.generateFlippedLabelling()))){
                        AlignmentError ae = new AlignmentError(acUM, f2, "diffAttributes");
                        acErrors.add(ae);
                        match2 = f2;
                    } else if ( (acUM.generateLabelling().equals(f2.generateFlippedLabelling())) && ((f2.getMin() != acUM.getMin()) || (f2.getMax() != acUM.getMax())) ) {
                        AlignmentError ae = new AlignmentError(acUM, f2, "diffBounds");
                        acErrors.add(ae);
                        match2 = f2;
                    }

                }
            }
            if (match2 != null) {
                matched.put(acUM, match2);
                ac2.remove(match2);
            } else {
                AlignmentError ae = new AlignmentError(null, acUM, "unmatched-test");
                acErrors.add(ae);
            }
        }
    }

//    /**
//     * Compares the width constraints for a pair of matched nodes
//     * @param n     the first node being compared
//     * @param m     the second node being compared
//     */
//    public void compareWidthConstraints(Node n, Node m) {
//        ArrayList<WidthConstraint> wc1 = new ArrayList<WidthConstraint>(), wc2 = new ArrayList<WidthConstraint>();
//
//        for (WidthConstraint w : n.getWidthConstraints()) {
//            wc1.add(w);
//        }
//        for (WidthConstraint w : m.getWidthConstraints()) {
//            wc2.add(w);
//        }
//
//        HashMap<WidthConstraint, WidthConstraint> matchedConstraints = new HashMap<WidthConstraint, WidthConstraint>();
//        ArrayList<WidthConstraint> unmatch1 = new ArrayList<WidthConstraint>();
//        ArrayList<WidthConstraint> unmatch2 = new ArrayList<WidthConstraint>();
//
//        while (wc1.size() > 0) {
//            WidthConstraint wc = wc1.remove(0);
//            WidthConstraint match = null;
//            for (WidthConstraint temp : wc2) {
//                if ( (wc.getPercentage() == temp.getPercentage()) && (wc.getAdjustment() == temp.getAdjustment()) && (wc.getMin() == temp.getMin()) && (wc.getMax() == temp.getMax())) {
//                    match = temp;
//                } else if ( ((wc.getMin()==temp.getMin()) && (wc.getMax()==temp.getMax())) && ( (wc.getPercentage()!=temp.getPercentage()) || (wc.getAdjustment()!=temp.getAdjustment()) ) ) {
//                    WidthError we = new WidthError(wc, temp, "diffCoefficients", n.getXpath());
//                    wcErrors.add(we);
//                    match = temp;
//                } else if ( ((wc.getMin()!=temp.getMin()) || (wc.getMax()!=temp.getMax())) && ( (wc.getPercentage()==temp.getPercentage()) && (wc.getAdjustment()==temp.getAdjustment()) ) ) {
//                    WidthError we = new WidthError(wc, temp, "diffBounds",n.getXpath());
//                    System.out.println(n);
//                    System.out.println(m);
//                    wcErrors.add(we);
//                    match = temp;
//                }
//            }
//
//            // Update the sets
//            if (match != null) {
//                matchedConstraints.put(wc, match);
//                wc2.remove(match);
//            } else {
//                WidthError we = new WidthError(wc, null, "unmatched-oracle",n.getXpath());
//                wcErrors.add(we);
//            }
//        }
//        for (WidthConstraint c : wc2) {
//            WidthError we = new WidthError(null, c, "unmatched-test",n.getXpath());
//            wcErrors.add(we);
//        }
//
//    }

    /**
     * Writes the model differences between the oracle and test versions into a text file, and then opens the file in
     * the default program of the user's system.
     * @param folder        the folder name in which the file is saved
     * @param fileName      the name of the results file
     */
    public void writeRLGDiffToFile(String folder, String fileName, boolean toDataDirectory) {
        PrintWriter output = null;
//        System.out.println(folder);
        String outFolder = "";
        try {
            if (toDataDirectory) {
                String[] splits = folder.split("/");
                String webpage = splits[7];
                String mutant = splits[8];
                outFolder = dataDirectory + webpage + "/" + mutant + "/";

            } else {
                outFolder = folder;
            }
            System.out.println(outFolder+fileName);
            FileUtils.forceMkdir(new File(outFolder));
            output = new PrintWriter(outFolder + fileName + ".txt");
            if (foundDiffs() == true) {
                // Print out unmatched nodes
                output.append("====== Unmatched Nodes ====== \n\n");
                for (String s : unmatched) {
                    output.append(s + "\n");
                }

                // Print out visibility errors
                output.append("====== Visibility Errors ====== \n\n");
                for (VisibilityError e : vcErrors) {
                    if (isErrorUnseen(e, defaultWidths)) {
                        output.append(e.toString());
                    }
                }

                // Print out alignment errors
                output.append("====== Alignment Errors ====== \n\n");
                Collections.sort(acErrors);
                String previousKey = "";
                for (AlignmentError e : acErrors) {
                    if (isErrorUnseen(e, defaultWidths)) {
                        // Check if this is a different edge
                        if (!e.generateKey().equals(previousKey)) {
                            String type = "";

                            if (e.getOracle() != null) {
                                if (e.getOracle().getType() == Type.PARENT_CHILD) {
                                    type = " contains ";
                                } else {
                                    type = " is sibling of ";
                                }
                                output.append("\n" + e.getOracle().node1.getXpath() + type + e.getOracle().node2.getXpath() + "\n");
                            } else {
                                if (e.getTest().getType() == Type.PARENT_CHILD) {
                                    type = " contains ";
                                } else {
                                    type = " is sibling of ";
                                }
                                output.append("\n" + e.getTest().node1.getXpath() + type + e.getTest().node2.getXpath() + "\n");
                            }
                            previousKey = e.generateKey();
                        }
                        output.append("\n" + e.toString());
                    }
                }
            } else {
                output.append("NO ERRORS DETECTED. :-)");
            }

            // Print out width errors
//            output.append("====== Width Errors ====== \n\n");
//            Collections.sort(wcErrors);
//            String previousXP = "";
//            for (WidthError e : wcErrors) {
//                if (!e.getXPath().equals(previousXP)) {
//
//                    if (isErrorUnseen(e, defaultWidths)) {
//                        if (!e.getXPath().equals(previousXP)) {
//                            output.append(e.getXPath() + "\n");
//                        }
//                        output.append(e.toString());
//                    }
//
//                    previousXP = e.getXPath();
//                }
//            }

            output.close();
//             Desktop d = Desktop.getDesktop();
//             d.open(new File(outFolder + fileName + ".txt"));
        } catch (IOException e) {
            System.out.println("Failed to write the results to file.");
        }
    }

    public boolean isErrorUnseen(Error e, int[] widths) {
        ArrayList<int[]> errorRanges;
        if (e instanceof VisibilityError)
            errorRanges = ((VisibilityError) e).calculateRangeOfViewportWidths();
        else if (e instanceof AlignmentError) {
            errorRanges = ((AlignmentError) e).calculateRangeOfViewportWidths();
        } else {
            errorRanges = ((WidthError) e).calculateRangeOfViewportWidths();
        }

        for (int i = 0; i < widths.length; i++) {
            int w = widths[i];
            for (int[] range : errorRanges) {
                if ((range[0] <= w) && (range[1] >= w)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    
    public void writeDiffToGraphViz() {
    	PrintWriter output = null;
        try {
            output = new PrintWriter("diff.gv");
        } catch (FileNotFoundException e) {
        }

        output.append("digraph G {");

        for (AlignmentConstraint ac : rlg1.getAlignmentConstraints().values()) {
            if (ac.type == Type.PARENT_CHILD) {
                Node parent = ac.node1;
                Node child = ac.node2;
//                output.append("\n\t");
                output.append(parent.getXpath().replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" -> ");
                output.append(child.getXpath().replaceAll("\\[|\\]", "").replaceAll("/", ""));

                output.append(" [ label= \"" + ac.getMin() + " ==> " + ac.getMax() + " " + ac.generateGraphVizLabelling() + "\" ];");

                output.append("\n\t");
                output.append(parent.getXpath().replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" [ label = \"" + parent.generateGraphVizLabel() + " \" shape=box];");

                output.append("\n\t");
                output.append(child.getXpath().replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" [ label = \"" + child.generateGraphVizLabel() + " \" shape=box];");
            }
            else {
                Node node1 = ac.node1;
                Node node2 = ac.node2;
//                output.append("\n\t");
                output.append(node1.getXpath().replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" -> ");
                output.append(node2.getXpath().replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" [ style=dotted, label= \"" + ac.getMin() + " ==> " + ac.getMax() + " " + ac.generateGraphVizLabelling() + " \" ];");
                output.append("\n\t");
                output.append(node1.getXpath().replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" [ label = \"" + node1.generateGraphVizLabel() + " \" shape=box];");

                output.append("\n\t");
                output.append(node2.getXpath().replaceAll("\\[|\\]", "").replaceAll("/", ""));
                output.append(" [ label = \"" + node2.generateGraphVizLabel() + " \" shape=box];");
            }
        }

        output.append("\n}");
        output.close();
    }


	public boolean foundDiffs() {
		if ((vcErrors.size() != 0) || (acErrors.size() != 0)) {
			return true;
		}
		return false;
	}

    public void printDiff() {
        if (foundDiffs() == true) {
            // Print out unmatched nodes
            System.out.println("====== Unmatched Nodes ======");
            for (String s : unmatched) {
                System.out.println(s);
            }

            // Print out visibility errors
            System.out.println("====== Visibility Errors ======");
            for (VisibilityError e : vcErrors) {
                if (isErrorUnseen(e, defaultWidths)) {
                    System.out.println(e.toString());
                }
            }

            // Print out alignment errors
            System.out.println("====== Alignment Errors ======");
            Collections.sort(acErrors);
            String previousKey = "";
            for (AlignmentError e : acErrors) {
                if (isErrorUnseen(e, defaultWidths)) {
                    // Check if this is a different edge
                    if (!e.generateKey().equals(previousKey)) {
                        String type = "";

                        if (e.getOracle() != null) {
                            if (e.getOracle().getType() == Type.PARENT_CHILD) {
                                type = " contains ";
                            } else {
                                type = " is sibling of ";
                            }
                            System.out.println(e.getOracle().node1.getXpath() + type + e.getOracle().node2.getXpath());
                        } else {
                            if (e.getTest().getType() == Type.PARENT_CHILD) {
                                type = " contains ";
                            } else {
                                type = " is sibling of ";
                            }
                            System.out.println(e.getTest().node1.getXpath() + type + e.getTest().node2.getXpath());
                        }
                        previousKey = e.generateKey();
                    }
                    System.out.println(e.toString());
                }
            }
        } else {
            System.out.println("NO ERRORS DETECTED. :-)");
        }
    }
}
