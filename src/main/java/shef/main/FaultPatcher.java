package shef.main;

import cz.vutbr.web.css.RuleMedia;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.StyleSheet;
import edu.gatech.xpert.dom.DomNode;
import org.mockito.internal.matchers.Null;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import shef.layout.*;
import shef.mutation.CSSMutator;
import shef.mutation.ResultClassifier;
import shef.mutation.WebpageMutator;
import shef.utils.BrowserFactory;
import shef.utils.ResultProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by thomaswalsh on 03/04/2017.
 */
public class FaultPatcher {
    private WebDriver webDriver;
    private String[] errors, classifications, categories;
    ArrayList<int[]> bounds;
    public String browser, fullUrl, url, rPath, cPath;
    private HashMap<Integer, LayoutFactory> layoutFactories;
    private HashMap<Integer, String> domStrings;
    File directory;

    public FaultPatcher(String fullUrl, String url, String browser, String current) {

        try {
            webDriver = BrowserFactory.getNewDriver(browser);
            webDriver.get(fullUrl);
            JavascriptExecutor js = (JavascriptExecutor) webDriver;

            directory = ResultProcessor.lastFileModified(current+"/../reports/" + url.split("/")[0]+"/");
//            System.out.println(directory);

            layoutFactories = new HashMap<>();
            domStrings = new HashMap<>();
            errors = ResultProcessor.getFailureStrings(directory);
            classifications = ResultProcessor.getClassifications(directory, errors.length);
            categories = ResultProcessor.getCategories(directory, errors.length);
            bounds = ResultProcessor.getFailureBounds(directory);

            for (int i = 0; i < errors.length; i++) {
                if (classifications[i].equals("TP")) {
                    // General setup
                    String error = errors[i];
                    System.out.println(error);
                    ArrayList<String> nodes = getNodesFromError(error, categories[i]);
                    int[] fBounds = bounds.get(i);

                    WebpageMutator mutator = new WebpageMutator(url, url.split("/")[0], 0, nodes);
                    String newUrl = mutator.copyFromWebpageRepository();
                    mutator.writeInitialParsedCSS(newUrl);
                    webDriver.get(newUrl+"/index.html");

                    // Resize browser to upper bound of fault
                    int currentSize = fBounds[1];
                    webDriver.manage().window().setSize(new Dimension(currentSize, 600));

                    // Verify failure manifests at the upper bound
                    boolean failureManifesting = checkForFailure(nodes, categories[i], currentSize, error);
                    while (!failureManifesting) {
                        currentSize--;
                        webDriver.manage().window().setSize(new Dimension(currentSize, 600));
                        failureManifesting = checkForFailure(nodes, categories[i], currentSize, error);
                    }
                    String oldDomString = domStrings.get(currentSize);

                    // Try and parse all the CSS
//                    for (RuleSet r : mutator.getRuleCandidates()) {
//                        System.out.println(r);
//                    }

//                    for (RuleMedia rm : mutator.getMqCandidates()) {
//                        System.out.println(rm.getMediaQueries().toString());
//                    }
//
//                    ArrayList<LinkedHashMap<String, StyleSheet>> options = mutator.getMutationOptions();
                    CSSMutator cm = mutator.getCSSMutator();
                    boolean faultFixed = false;
                    ArrayList<CSSMutator> worklist = new ArrayList<>();
                    ArrayList<String> mutationStrings = new ArrayList<>();
                    worklist.add(cm);
                    mutationStrings.add("original");

//                    while (!faultFixed || !worklist.isEmpty()) {
                    for (int x = 1; x < 3; x++) {
                        ArrayList<CSSMutator> mutantsToKeep = new ArrayList<>();
                        ArrayList<String> descsToKeep = new ArrayList<>();
                        while (!worklist.isEmpty()) {
                            CSSMutator currentCM = worklist.remove(0);
                            String currentMD = mutationStrings.remove(0);
                            System.out.println(currentMD);
                            HashMap<String, CSSMutator> options = currentCM.getMutators(currentCM.getStylesheets());
                            currentCM.writeToFile(0, currentCM.getStylesheets(), mutator.getShorthand(), newUrl);
                            webDriver.get(newUrl + "/index.html");
                            checkForFailure(nodes, categories[i], currentSize, error);
                            oldDomString = domStrings.get(currentSize);

                            // Go through each mutation option
                            for (String mDesc : options.keySet()) {
//                                System.out.println("----------");
//                                System.out.println(mDesc);
//                                System.out.println("----------");
                                CSSMutator newCM = options.get(mDesc);
                                newCM.writeToFile(0, newCM.getStylesheets(), mutator.getShorthand(), newUrl);
                                webDriver.get(newUrl + "/index.html");
                                checkForFailure(nodes, categories[i], currentSize, error);
                                String newDomString = domStrings.get(currentSize);
                                LayoutFactory oldLF = new LayoutFactory(oldDomString);
                                LayoutFactory newLF = new LayoutFactory(newDomString);
                                boolean layoutsEqual = areLayoutsEqual(oldLF, newLF);
                                if (!layoutsEqual) {
                                    mutantsToKeep.add(newCM);
                                    descsToKeep.add(currentMD + mDesc);

                                } else {
//                                    System.out.println("Removing " + mDesc);
                                }
                            }
                            System.out.println("FINISHED WITH " + currentMD);
                        }
                        worklist.addAll(mutantsToKeep);
                        mutationStrings.addAll(descsToKeep);
                        System.out.println(worklist.size());
                        System.out.println("END OF ITERATION " + x);

                    }



                    int numIterations = 0;
//                    while (!faultFixed || numIterations < 20) {
//                        numIterations++;
//                        System.out.println(numIterations);
//                        mutator.mutate(newUrl);
//                        webDriver.get(newUrl+"/index.html");
//                        try {
//                            Thread.sleep(100);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        if (!checkForFailure(nodes, categories[i], currentSize, error)) {
//                            faultFixed = true;
//                        }
//                        String newDomString = domStrings.get(currentSize);
//                        LayoutFactory old = new LayoutFactory(oldDomString);
//                        LayoutFactory newLF = new LayoutFactory(newDomString);
//
//                        boolean layoutsEqual = areLayoutsEqual(old, newLF);
//                        System.out.println(layoutsEqual + "\n");
//                    }
//                    System.out.println("Think a fix has been found. VERIFYING NOW. . . . .");
//                    Tool.runFaultDetector(current, url, browser, "uniformBP", true, 320, 1400, 60, false);
//                    System.out.println("Fixed " + error);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Make sure the WebDriver is closed down
            if (webDriver != null) {
                webDriver.quit();
            }
        }
    }

    private boolean areLayoutsEqual(LayoutFactory old, LayoutFactory newLF) {
        HashMap<String, Element> oldMap = old.getElementMap();
        HashMap<String, Element> newMap = newLF.getElementMap();
        boolean foundDifferent = false;
        for (String xpath : oldMap.keySet()) {
            Element oldNode = oldMap.get(xpath);
            Element newNode = newMap.get(xpath);
            try {
                if (!oldNode.getRectangle().equals(newNode.getRectangle())) {
                    foundDifferent = true;
                }
            } catch (NullPointerException npe) {
                return false;
            }
        }
        return !foundDifferent;
    }

    private String generateRandomInjectionScript(ArrayList<String> nodes, String error) {
        String result = "var element = document.evaluate(\"" +nodes.get(0) + "\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue; var styles = window.getComputedStyle(element);  element.style.fontSize = '20px';";
        return result;
    }

    private boolean checkForFailure(ArrayList<String> nodes, String category, int fBound, String error) {

        Tool.capturePageModel(fullUrl, new int[] {fBound}, 50, false, false, webDriver, null, layoutFactories, domStrings);
        Layout layout = layoutFactories.get(fBound).layout;

        // Element collision or element protrusion check
        if (category.equals("Element Collision") || category.equals("Element Protrusion")) {

            for (Relationship r : layout.getRelationships().values()) {
                if (r instanceof Sibling) {
                    if (nodes.contains(r.getNode1().getXpath()) && nodes.contains(r.getNode2().getXpath())) {
                        // Check the matching sibling edge actually has the overlapping attribute set to true.
                        if (((Sibling) r).generateAttributeArray()[10]) {
                            return true;
                        }
                    }
                }
            }
            return false;

            // Viewport protrusion check
        } else if (category.equals("Viewport Protrusion")) {
            String vpNode = nodes.get(0);
            for (Relationship r : layout.getRelationships().values()) {
                // We're only interested in pc edges
                if (r instanceof ParentChild) {
                    // If the VP node is the child, return
                    if (r.getNode2().getXpath().equals(vpNode)) {
                        return false;
                    }
                }
            }
            return true;

            // Small Range Check
        } else if (category.equals("Small-Range")) {
            for (Relationship r : layout.getRelationships().values()) {
                if (nodes.contains(r.getNode1().getXpath()) && nodes.contains(r.getNode2().getXpath())) {
                    Sibling s = (Sibling) r;
                    String actual = s.generateAttributeSet();
                    String expected = getAttributesFromString(error);
                    if (actual.trim().equals(expected.trim())) {
                        return true;
                    }
                }
            }

            // Wrapping check
        } else if (category.equals("Wrapping")) {
            String wrapped = getWrappedNode(error);
            for (Relationship r : layout.getRelationships().values()) {
                if (r instanceof Sibling) {
                    Sibling s = (Sibling) r;
                    if (s.getNode1().getXpath().equals(wrapped) && nodes.contains(s.getNode2().getXpath())) {
                        if (s.generateAttributeArray()[1]) {
                            return true;
                        }

                    } else if (s.getNode2().getXpath().equals(wrapped) && nodes.contains(s.getNode1().getXpath())) {
                        if (s.generateAttributeArray()[0]) {
                            return true;
                        }
                    }
                }
            }
            // Return false if we haven't found a match
            return false;
        }
        return false;
    }

    private String getAttributesFromString(String error) {
        return error.split(" , ")[5].split(" ")[1].split("}")[0]+"}";
    }


    private ArrayList<String> getNodesFromError(String error, String category) {
        ArrayList<String> nodes = new ArrayList<>();
        if (category.equals("Wrapping")) {
            String wrapped = error.split(":\t")[1].split(" wrapped")[0];
            String[] row = error.split("row \t")[1].split(" ");
            for (String r : row) {
                if (!r.equals("[") && !r.equals("]")) {
                    nodes.add(r);
                }

            }
        } else if (category.equals("Viewport Protrusion")) {
            nodes.add(error.split(" overflowed")[0]);
        } else if (category.equals("Element Collision")) {
            String n1 = error.split("ELEMENTS ")[1].split(" AND ")[0];
            nodes.add(n1);
            String n2 = error.split(" AND ")[1].split(" ARE ")[0];
            nodes.add(n2);
        } else if (category.equals("Element Protrusion")) {
            String[] splits = error.split("\t")[1].split(" , ");
            nodes.add(splits[0]);
            nodes.add(splits[1]);
        } else if (category.equals("Small-Range")) {
            String offender = error.split("THIS: ")[1].split("\t")[0];
            System.out.println(offender);
            String[] splits = offender.split(" , ");
            nodes.add(splits[0]);
            nodes.add(splits[1]);
        }

        return nodes;
    }

    private String getWrappedNode(String error) {
        return error.split(":\t")[1].split(" wrapped")[0];
    }


}