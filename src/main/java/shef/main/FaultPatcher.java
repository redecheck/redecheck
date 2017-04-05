package shef.main;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import shef.layout.*;
import shef.utils.BrowserFactory;
import shef.utils.ResultProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thomaswalsh on 03/04/2017.
 */
public class FaultPatcher {
    private WebDriver webDriver;
    private String[] errors, classifications, categories;
    ArrayList<int[]> bounds;
    public String browser, fullUrl, url, rPath, cPath;
    private HashMap<Integer, LayoutFactory> layoutFactories;
    File directory;

    public FaultPatcher(String fullUrl, String url, String browser, String current) {

        try {
            webDriver = BrowserFactory.getNewDriver(browser);
            webDriver.get(fullUrl);
            JavascriptExecutor js = (JavascriptExecutor) webDriver;

            directory = ResultProcessor.lastFileModified(current+"/../reports/" + url.split("/")[0]+"/");
            System.out.println(directory);

            layoutFactories = new HashMap<>();
            errors = ResultProcessor.getFailureStrings(directory);
            classifications = ResultProcessor.getClassifications(directory, errors.length);
            categories = ResultProcessor.getCategories(directory, errors.length);
            bounds = ResultProcessor.getFailureBounds(directory);

            for (int i = 0; i < errors.length; i++) {
                if (classifications[i].equals("TP")) {

                    // General setup
                    String error = errors[i];
                    ArrayList<String> nodes = getNodesFromError(error, categories[i]);
                    int[] fBounds = bounds.get(i);

                    // Resize browser to upper bound of fault
                    int currentSize = fBounds[1];
                    webDriver.manage().window().setSize(new Dimension(currentSize, 1000));

                    // Verify failure manifests at the upper bound
                    boolean failureManifesting = checkForFailure(nodes, categories[i], currentSize, error);
                    while (!failureManifesting) {
                        currentSize--;
                        webDriver.manage().window().setSize(new Dimension(currentSize, 1000));
                        failureManifesting = checkForFailure(nodes, categories[i], currentSize, error);
                    }
                    System.out.println(failureManifesting);
                }
            }

            // Trying to interact with the CSS of the web page
//            String originalValue = (String) js.executeScript("var myElement = document.querySelector('h1'); var styles = window.getComputedStyle(myElement); return styles.getPropertyValue('font-size');");
            js.executeScript("var myElement = document.querySelector('h1'); myElement.style.fontSize = '20px';");
//            String afterValue = (String) js.executeScript("var myElement = document.querySelector('h1'); var styles = window.getComputedStyle(myElement); return styles.getPropertyValue('font-size');");
//            Tool.capturePageModel(fullUrl, new int[] {800}, 50, false, false, webDriver, null, layoutFactories);
//            LayoutFactory layoutFactory = layoutFactories.get(800);
//            System.out.println(layoutFactory.layout.toString());
//            function getElementByXpath(path) {
//            return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
//}
//
//            console.log( getElementByXpath("//html[1]/body[1]/div[1]") );

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Make sure the WebDriver is closed down
            if (webDriver != null) {
                webDriver.quit();
            }
        }
    }

    private boolean checkForFailure(ArrayList<String> nodes, String category, int fBound, String error) {
        Tool.capturePageModel(fullUrl, new int[] {fBound}, 50, false, false, webDriver, null, layoutFactories);
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
                nodes.add(r);
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


}
