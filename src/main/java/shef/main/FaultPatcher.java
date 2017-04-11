package shef.main;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleMedia;
import cz.vutbr.web.css.RuleSet;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import shef.layout.*;
import shef.mutation.WebpageMutator;
import shef.utils.BrowserFactory;
import shef.utils.ResultProcessor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
//            System.out.println(directory);

            layoutFactories = new HashMap<>();
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

                    // Try and parse all the CSS
                    WebpageMutator mutator = new WebpageMutator(url, url.split("/")[0], 0, nodes);
                    for (RuleSet r : mutator.getRuleCandidates()) {
                        System.out.println(r);
                    }

                    for (RuleMedia rm : mutator.getMqCandidates()) {
                        System.out.println(rm);
                    }

                    boolean faultFixed = false;
//                    while (!faultFixed) {
//                        String script = generateRandomInjectionScript(nodes, error);
////                                "
//                        System.out.println(script);
//                        String result = (String) js.executeScript(script);
//                        faultFixed = true;
//                    }
                }
            }



//            try {
//                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//                DocumentBuilder db = factory.newDocumentBuilder();
//                File f = new File("../../../Resources/fault-examples/"+url);
//                Document doc = db.parse(f);
////            CSSFactory.getUsedStyles();
//            } catch (ParserConfigurationException e) {
//                e.printStackTrace();
//            } catch (SAXException e) {
//                e.printStackTrace();
//            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Make sure the WebDriver is closed down
            if (webDriver != null) {
                webDriver.quit();
            }
        }
    }

    private String generateRandomInjectionScript(ArrayList<String> nodes, String error) {
        String result = "var element = document.evaluate(\"" +nodes.get(0) + "\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue; var styles = window.getComputedStyle(element);  element.style.fontSize = '20px';";
        return result;
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

        // Wrapping check
        } else if (category.equals("Wrapping")) {
            String wrapped = getWrappedNode(error);
            for (Relationship r : layout.getRelationships().values()) {
                if (r instanceof Sibling) {
                    Sibling s = (Sibling) r;
                    if (s.getNode1().getXpath().equals(wrapped) && nodes.contains(s.getNode2().getXpath()) && s.generateAttributeArray()[1]) {
                        return true;
                    } else if (s.getNode2().getXpath().equals(wrapped) && nodes.contains(s.getNode1().getXpath()) && s.generateAttributeArray()[0]) {
                        return true;
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
