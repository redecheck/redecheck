package shef.reporting.inconsistencies;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import shef.layout.Element;
import shef.layout.LayoutFactory;
import shef.redecheck.RLGThread;
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
 * Created by thomaswalsh on 31/05/2016.
 */
public class SmallRangeFailure extends ResponsiveLayoutFailure {
    AlignmentConstraint ac, prev, next;

    public SmallRangeFailure(AlignmentConstraint ac, AlignmentConstraint prev, AlignmentConstraint next) {
        this.ac = ac;
        this.prev = prev;
        this.next = next;
    }

    public String toString() {
        return "SMALL RANGE ERROR:" + "\n\tTHIS: " + ac + "\n\tPREV: " + prev + "\n\tNEXT: " + next;
    }


    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullUrl, HashMap<Integer, BufferedImage> imageMap, String timeStamp) {
        int captureWidth = (ac.getMin()+ac.getMax())/2;
        HashMap<Integer, LayoutFactory> lfs = new HashMap<>();

        BufferedImage img;
//        if (imageMap.containsKey(captureWidth)) {
//            img = imageMap.get(captureWidth);
//        } else {
            img = RLGThread.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);
////            imageMap.put(captureWidth, img);
//        }
//        System.out.println(captureWidth + " " + (img == null));
        LayoutFactory lf = lfs.get(captureWidth);
        Element e1 = lf.getElementMap().get(ac.getNode1().getXpath());
        Element e2 = lf.getElementMap().get(ac.getNode2().getXpath());

        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.RED);
//        g2d.setStroke(new BasicStroke(5));
        int[] coords1 = e1.getBoundingCoords();
        g2d.drawRect(coords1[0],coords1[1],coords1[2]-coords1[0],coords1[3]-coords1[1]);

        g2d.setColor(Color.CYAN);
        int[] coords2 = e2.getBoundingCoords();
        g2d.drawRect(coords2[0],coords2[1],coords2[2]-coords2[0],coords2[3]-coords2[1]);
        g2d.dispose();
        try {
            File output;
            if (!url.contains("www.")) {
                String[] splits = url.split("/");
                String webpage = splits[0];
                String mutant = "index-" + timeStamp;
                //                    splits[1];
                output = new File(new java.io.File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/fault" + errorID + "/");
            } else {
                String[] splits = url.split("www.");
                String webpage = splits[1];
                String mutant = "index-" + timeStamp;
                output = new File(new java.io.File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/fault" + errorID + "/");
            }
            FileUtils.forceMkdir(output);
            ImageIO.write(img, "png", new File(output+ "/smallrangeWidth" + captureWidth + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

//        captureWidth = (prev.getMin()+prev.getMax())/2;
//        img = RLGThread.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);
//        lf = lfs.get(captureWidth);
//        e1 = lf.getElementMap().get(ac.getNode1().getXpath());
//        e2 = lf.getElementMap().get(ac.getNode2().getXpath());
//
//        g2d = img.createGraphics();
//        g2d.setColor(Color.RED);
//        g2d.setStroke(new BasicStroke(5));
//        coords1 = e1.getBoundingCoords();
//        g2d.drawRect(coords1[0],coords1[1],coords1[2]-coords1[0],coords1[3]-coords1[1]);
//
//        g2d.setColor(Color.CYAN);
//        coords2 = e2.getBoundingCoords();
//        g2d.drawRect(coords2[0],coords2[1],coords2[2]-coords2[0],coords2[3]-coords2[1]);
//        g2d.dispose();
//        try {
//            String[] splits = url.split("/");
//            String webpage = splits[0];
//            String mutant = "index-" + timeStamp;
////                    splits[1];
//            File output = new File(new java.io.File( "." ).getCanonicalPath() + "/../reports/"  + webpage + "/" + mutant + "/fault"+errorID+"/");
//            FileUtils.forceMkdir(output);
//            ImageIO.write(img, "png", new File(output+ "/prev-Width" + captureWidth + ".png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        captureWidth = (next.getMin()+next.getMax())/2;
//        img = RLGThread.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);
//        lf = lfs.get(captureWidth);
//        e1 = lf.getElementMap().get(ac.getNode1().getXpath());
//        e2 = lf.getElementMap().get(ac.getNode2().getXpath());
//
//        g2d = img.createGraphics();
//        g2d.setColor(Color.RED);
//        g2d.setStroke(new BasicStroke(5));
//        coords1 = e1.getBoundingCoords();
//        g2d.drawRect(coords1[0],coords1[1],coords1[2]-coords1[0],coords1[3]-coords1[1]);
//
//        g2d.setColor(Color.CYAN);
//        coords2 = e2.getBoundingCoords();
//        g2d.drawRect(coords2[0],coords2[1],coords2[2]-coords2[0],coords2[3]-coords2[1]);
//        g2d.dispose();
//        try {
//            String[] splits = url.split("/");
//            String webpage = splits[0];
//            String mutant = "index-" + timeStamp;
////                    splits[1];
//            File output = new File(new java.io.File( "." ).getCanonicalPath() + "/../reports/"  + webpage + "/" + mutant + "/fault"+errorID+"/");
//            FileUtils.forceMkdir(output);
//            ImageIO.write(img, "png", new File(output+ "/next-Width" + captureWidth + ".png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public HashSet<Node> getNodes() {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(ac.getNode1());
        nodes.add(ac.getNode2());
        return nodes;
    }

    @Override
    public int[] getBounds() {
        return new int[] {ac.getMin(), ac.getMax()};
    }
}
