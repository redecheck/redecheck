package shef.reporting.failures;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.redecheck.RLGThread;
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
 * Created by thomaswalsh on 10/06/2016.
 */
public class WrappingFailure extends ResponsiveLayoutFailure {
    Node wrapped;
    ArrayList<Node> row;
    String range;
    int min, max;

    public WrappingFailure(Node n, ArrayList<Node> r, int min, int max) {
        this.wrapped = n;
        this.row = r;
        this.range = min + " -> " + max;
        this.min = min;
        this.max = max;
    }

    public String toString() {
        String rowString = "[ ";
        for (Node nr : row) {
            rowString += nr.getXpath() + " ";
        }
        rowString += "]";
        return "WRAPPING ELEMENT ERROR FOR RANGE " + range + ":" +
                "\n\t" + wrapped.getXpath() + " wrapped from row \n\t" + rowString;
    }

    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullurl, HashMap<Integer, BufferedImage> imageMap, String timeStamp) {
        try {
            int captureWidth = (min + max) / 2;
            HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

            BufferedImage img;
//            if (imageMap.containsKey(captureWidth)) {
//                img = imageMap.get(captureWidth);
//            } else {
                img = RLGThread.getScreenshot(captureWidth, errorID, lfs, webDriver, url);
//                imageMap.put(captureWidth, img);
//            }
            LayoutFactory lf = lfs.get(captureWidth);
            Element e1 = lf.getElementMap().get(wrapped.getXpath());

            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));
            int[] coords1 = e1.getBoundingCoords();
            g2d.drawRect(coords1[0], coords1[1], coords1[2] - coords1[0], coords1[3] - coords1[1]);

            g2d.setColor(Color.CYAN);
            g2d.setStroke(new BasicStroke(1));
            for (Node n : row) {
                Element e2 = lf.getElementMap().get(n.getXpath());
                int[] coords2 = e2.getBoundingCoords();
                g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);
            }
            g2d.dispose();
            try {
                String[] splits = url.split("/");
                String webpage = splits[0];
                String mutant = "index-" + timeStamp;
                File output = new File(new java.io.File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/fault"+ errorID +"/");
                FileUtils.forceMkdir(output);
                ImageIO.write(img, "png", new File(output + "/wrappingWidth" + captureWidth + ".png"));
            } catch (IOException e) {
//                e.printStackTrace();
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    @Override
    public HashSet<Node> getNodes() {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(wrapped);
        nodes.addAll(row);
        return nodes;
    }

    @Override
    public int[] getBounds() {
        return new int[] {min, max};
    }
}
