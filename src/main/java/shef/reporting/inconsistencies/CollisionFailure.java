package shef.reporting.inconsistencies;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Thomas Walsh
 * @version 2.1
 * This class extends ResponsiveLayoutFailure and represents element collision failures
 */
public class CollisionFailure extends ResponsiveLayoutFailure {

    // Instance variable representing the colliding constraint
    private AlignmentConstraint constraint;

    public CollisionFailure(AlignmentConstraint con) {
        constraint = con;
    }

    /**
     * @return String describing the failure for either console or text file printing
     */
    public String toString() {
        return "ELEMENTS " + constraint.getNode1().getXpath() + " AND " + constraint.getNode2().getXpath() + " ARE OVERLAPPING BETWEEN " + constraint.getMin() + " AND " + constraint.getMax();
    }

    /**
     * Captures a screenshot of the failure, highlights the colliding elements and then saves it to disk
     * @param errorID   The error ID of the failure to uniquely identify it
     * @param url       The URL of the webpage under test
     * @param webDriver The WebDriver object currently rendering the page
     * @param fullUrl   The full file path used to save the image in the correct place
     * @param timeStamp The time stamp of the tool execution to uniquely identify different full test reports
     */
    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullUrl, String timeStamp) {
        
        int captureWidth = (constraint.getMin()+constraint.getMax())/2;

        // Layout factory to store the DOM
        HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

        // Capture the image and the DOM
        BufferedImage img = RLGExtractor.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);

        // Get the coordinates of the two colliding elements
        LayoutFactory lf = lfs.get(captureWidth);
        Element e1 = lf.getElementMap().get(constraint.getNode1().getXpath());
        int[] coords1 = e1.getBoundingCoords();
        Element e2 = lf.getElementMap().get(constraint.getNode2().getXpath());
        int[] coords2 = e2.getBoundingCoords();

        // Set up Graphics@d object so the elements can be highlighted
        Graphics2D g2d = img.createGraphics();

        // Highlight the two elements in different colours
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRect(coords1[0],coords1[1],coords1[2]-coords1[0],coords1[3]-coords1[1]);
        g2d.setColor(Color.CYAN);
        g2d.drawRect(coords2[0],coords2[1],coords2[2]-coords2[0],coords2[3]-coords2[1]);
        g2d.dispose();

        try {
            // Set up the output file
            File output = Utils.getOutputFilePath(url, timeStamp, errorID);

            // Make sure the output directory exists
            FileUtils.forceMkdir(output);

            // Write the highlighted screenshot to file
            ImageIO.write(img, "png", new File(output+"/overlapWidth" + captureWidth + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns the two colliding elements as a HashMap
     * @return HashMap containing the two elements
     */
    @Override
    public HashSet<Node> getNodes() {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(constraint.getNode1());
        nodes.add(constraint.getNode2());
        return nodes;
    }

    /**
     * Returns the lower and upper bounds of the constraint
     * @return  Array containing the bounds
     */
    @Override
    public int[] getBounds() {
        return new int[] {constraint.getMin(), constraint.getMax()};
    }

    /**
     * Accessor for the constraint
     * @return  the colliding constraint
     */
    public AlignmentConstraint getConstraint() {
        return constraint;
    }
}
