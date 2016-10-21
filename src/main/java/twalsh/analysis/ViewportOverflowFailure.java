package twalsh.analysis;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import twalsh.layout.Element;
import twalsh.layout.LayoutFactory;
import twalsh.redecheck.RLGThread;
import twalsh.rlg.Node;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by thomaswalsh on 19/08/2016.
 */
public class ViewportOverflowFailure extends ResponsiveLayoutFailure {
    Node node;
    int min, max;

    public Node getNode() {
        return node;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public ViewportOverflowFailure(Node n, int i, int key) {
        node = n;
        min = i;
        max = key;
    }

    public String toString() {
        return node.getXpath() + " overflowed the viewport window between " + min + " and " + max;
    }

    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullUrl, HashMap<Integer, BufferedImage> imageMap, String timeStamp) {
        try {
            int captureWidth = min;
            HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

            BufferedImage img;
            img = RLGThread.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);
            LayoutFactory lf = lfs.get(captureWidth);
            Element e1 = lf.getElementMap().get(node.getXpath());
            Element body = lf.getElementMap().get("/HTML/BODY");

            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2));

            int[] coords = e1.getBoundingCoords();
            g2d.drawRect(coords[0],coords[1],coords[2]-coords[0],coords[3]-coords[1]);

            g2d.setColor(Color.GREEN);
            int[] coords2 = body.getBoundingCoords();
            g2d.drawRect(coords2[0],coords2[1],coords2[2]-coords2[0],coords2[3]-coords2[1]);

            g2d.dispose();
            String[] splits = url.split("/");
            String webpage = splits[0];
            String mutant = "index-" + timeStamp;
//                    splits[1];
            File output = new File(new java.io.File( "." ).getCanonicalPath() + "/../reports/"  + webpage + "/" + mutant + "/fault"+errorID+"/");
            FileUtils.forceMkdir(output);
            ImageIO.write(img, "png", new File(output+ "/viewportOverflowWidth" + captureWidth + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashSet<Node> getNodes() {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(node);
        return nodes;
    }

    @Override
    public int[] getBounds() {
        return new int[] {min, max};
    }


}
