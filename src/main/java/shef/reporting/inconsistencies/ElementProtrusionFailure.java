package shef.reporting.inconsistencies;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.redecheck.RLGExtractor;
import shef.redecheck.Utils;
import shef.rlg.AlignmentConstraint;
import shef.rlg.Node;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by thomaswalsh on 31/05/2016.
 */
public class ElementProtrusionFailure extends ResponsiveLayoutFailure {
    AlignmentConstraint ac1, ac2, ofCon, match;
    HashMap<Node, ArrayList<AlignmentConstraint>> map;



    Node overflowed;
    Node intendedParent;
    ArrayList<Node> newParents;


    public HashMap<Node, ArrayList<AlignmentConstraint>> getMap() {
        return map;
    }

    public ElementProtrusionFailure(HashMap<Node, ArrayList<AlignmentConstraint>> m, Node ip, Node n) {
        this.map = m;
        intendedParent = ip;
        overflowed = n;
//        System.out.println(n);
    }

    public ElementProtrusionFailure(Node o, AlignmentConstraint ac) {
        overflowed = o;
        ofCon = ac;

    }

    public String toString() {
//        if (ofCon != null) {
            return overflowed.getXpath() + " OVERFLOWED ITS PARENT BETWEEN " + ofCon.getMin() + " AND " + ofCon.getMax() + "\n\t" + ofCon + "\n\t" + match;
//        } else {
//            String result = "OVERFLOWING ELEMENT ERROR: ";
//            result += "\n" + intendedParent.getXpath() + " was the intended parent of " + overflowed.getXpath();
//            for (AlignmentConstraint ac : map.get(intendedParent)) {
//                result += "\n\t" + ac.getMin() + " -> " + ac.getMax();
//            }
//
//            for (Node n : map.keySet()) {
//                if (n != intendedParent) {
//                    result+= "\nOverflowed into: " + n.getXpath();
//                    for (AlignmentConstraint ac : map.get(n)) {
//                        result += "\n\t" + ac.getMin() + " -> " + ac.getMax();
//                    }
//                }
//
//            }
//            return result;
//        }
    }

    /**
     * Captures a screenshot of the failure, highlights the overflowing elements and then saves it to disk
     * @param errorID   The error ID of the failure to uniquely identify it
     * @param url       The URL of the webpage under test
     * @param webDriver The WebDriver object currently rendering the page
     * @param fullUrl   The full file path used to save the image in the correct place
     * @param timeStamp The time stamp of the tool execution to uniquely identify different full test reports
     */
    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullUrl, String timeStamp) {

        // Determine the mid point of the constraint as that's what the browser will be resized to
        int captureWidth = (ofCon.getMin() + ofCon.getMax()) / 2;

        // Layout factory to store the DOM
        HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

        // Capture the image and the DOM
        BufferedImage img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);

        // Get the coordinates of the two overflowing elements
        LayoutFactory lf = lfs.get(captureWidth);
        Element e1 = lf.getElementMap().get(ofCon.getNode1().getXpath());
        int[] coords1 = e1.getBoundingCoords();
        Element e2 = lf.getElementMap().get(ofCon.getNode2().getXpath());
        int[] coords2 = e2.getBoundingCoords();

        // Set up Graphics@d object so the elements can be highlighted
        Graphics2D g2d = img.createGraphics();

        // Highlight the two elements in different colours
        g2d.setStroke(new BasicStroke(5));
        g2d.setColor(Color.RED);
        g2d.drawRect(coords1[0], coords1[1], coords1[2] - coords1[0], coords1[3] - coords1[1]);
        g2d.setColor(Color.CYAN);
        g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);
        g2d.dispose();

        try {
            // Set up the output file
            File output = Utils.getOutputFilePath(url, timeStamp, errorID);

            // Make sure the output directory exists
            FileUtils.forceMkdir(output);

            // Write the highlighted screenshot to file
            ImageIO.write(img, "png", new File(output + "/overflow-Width" + captureWidth + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashSet<Node> getNodes() {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(ofCon.getNode1());
        nodes.add(ofCon.getNode2());
        return nodes;
    }

    @Override
    public int[] getBounds() {
        return new int[] {ofCon.getMin(), ofCon.getMax()};
    }

    public Node getOverflowed() {
        return overflowed;
    }

    public Node getIntendedParent() {
        return intendedParent;
    }

    public AlignmentConstraint getOfCon() {
        return ofCon;
    }

    public AlignmentConstraint getMatch() {
        return match;
    }
}
