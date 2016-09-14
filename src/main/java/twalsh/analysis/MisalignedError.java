package twalsh.analysis;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import twalsh.layout.Element;
import twalsh.layout.LayoutFactory;
import twalsh.redecheck.RLGThread;
import twalsh.rlg.AlignmentConstraint;
import twalsh.rlg.Node;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by thomaswalsh on 14/06/2016.
 */
public class MisalignedError extends ResponsiveLayoutError {
    HashMap<String, ArrayList<AlignmentConstraint>> alignments;
    String rangeKey;
            Node buggy;
    ArrayList<Node> notBuggy;
    HashSet<Node> aligned, notAligned;
    int min;
    int max;


    public MisalignedError(HashMap<String, ArrayList<AlignmentConstraint>> aligned, String key) {
        this.alignments = aligned;
        notBuggy = new ArrayList<>();
        this.rangeKey = key;
    }

    public MisalignedError(HashSet<Node> al, HashSet<Node> notAligned, String key, int min, int max) {
        this.aligned = al;
        this.notAligned = notAligned;
        this.rangeKey = key;
        this.min = min;
        this.max = max;
    }


    public String toString() {
        String rowString = "[ ";
        for (Node nr : aligned) {
            rowString += nr.getXpath() + " ";
        }
        rowString += "]";
        String buggyS = "[";
        for (Node na : notAligned) {
            buggyS += na.getXpath() + " ";
        }
        buggyS+="]";

        String result = "MISALIGNED ELEMENTS FOR RANGE: "+rangeKey +
                "\n\t" + buggyS + " were misaligned with row/column \n\t" + rowString;
        return result;
    }

    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullUrl, HashMap<Integer, BufferedImage> imageMap, String timeStamp) {
        int captureWidth = 0;
        captureWidth = (min + max) / 2;


        HashMap<Integer, LayoutFactory> lfs = new HashMap<>();
        BufferedImage img;
//        if (imageMap.containsKey(captureWidth)) {
//            img = imageMap.get(captureWidth);
//        } else {
            img = RLGThread.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);
//            imageMap.put(captureWidth, img);
//        }
        LayoutFactory lf = lfs.get(captureWidth);

        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.CYAN);
//        g2d.setStroke(new BasicStroke(5));
        for (Node n : aligned) {
            Element e1 = lf.getElementMap().get(n.getXpath());
            int[] coords1 = e1.getCoordsArray();
            g2d.drawRect(coords1[0],coords1[1],coords1[2]-coords1[0],coords1[3]-coords1[1]);
        }

        g2d.setColor(Color.RED);
        for (Node n2 : notAligned) {
            Element e2 = lf.getElementMap().get(n2.getXpath());
            int[] coords2 = e2.getCoordsArray();
            g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);
        }

        g2d.dispose();
        try {
            String[] splits = url.split("/");
            String webpage = splits[0];
            String mutant = "index-" + timeStamp;
            File output = new File(new java.io.File( "." ).getCanonicalPath() + "/../reports/"  + webpage + "/" + mutant + "/fault" + errorID + "/");
            FileUtils.forceMkdir(output);
            ImageIO.write(img, "png", new File(output + "/misalignedWidth" + captureWidth + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullUrl) {
//        int captureWidth = 0;
//        for (String alignment : alignments.keySet()) {
//            HashMap<Node, ArrayList<AlignmentConstraint>> nodeCounts = new HashMap<>();
//            for (AlignmentConstraint ac : alignments.get(alignment)) {
//                captureWidth = (ac.getMin() + ac.getMax()) / 2;
//                if (!nodeCounts.keySet().contains(ac.getNode1())) {
//                    nodeCounts.put(ac.getNode1(), new ArrayList<AlignmentConstraint>());
//                }
//                nodeCounts.get(ac.getNode1()).add(ac);
//
//                if (!nodeCounts.keySet().contains(ac.getNode2())) {
//                    nodeCounts.put(ac.getNode2(), new ArrayList<AlignmentConstraint>());
//                }
//                nodeCounts.get(ac.getNode2()).add(ac);
//            }
//            buggy = null;
//            if (notSameValues(nodeCounts)) {
//                for (Node node : nodeCounts.keySet()) {
//                    notBuggy.add(node);
//                    if (nodeCounts.get(node).size() > 1) {
//                        buggy = node;
//                        notBuggy.remove(node);
////                        break;
//                    }
//                }
//
//                HashMap<Integer, LayoutFactory> lfs = new HashMap<>();
//                BufferedImage img = RLGThread.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);
//                LayoutFactory lf = lfs.get(captureWidth);
//                Element e1 = lf.getElementMap().get(buggy.getXpath());
//
//                Graphics2D g2d = img.createGraphics();
//                g2d.setColor(Color.RED);
//                g2d.setStroke(new BasicStroke(5));
//                int[] coords1 = e1.getCoordsArray();
//                g2d.drawRect(coords1[0],coords1[1],coords1[2]-coords1[0],coords1[3]-coords1[1]);
//
//                g2d.setColor(Color.CYAN);
//                for (Node n : nodeCounts.keySet()) {
//                    if (!n.getXpath().equals(buggy.getXpath())) {
//                        Element e2 = lf.getElementMap().get(n.getXpath());
//                        int[] coords2 = e2.getCoordsArray();
//                        g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);
//                    }
//                }
//                g2d.dispose();
//                try {
//                    String[] splits = url.split("/");
//                    String webpage = splits[0];
//                    String mutant = splits[1];
//                    File output = new File(new java.io.File( "." ).getCanonicalPath() + "/../reports/"  + webpage + "/" + mutant + "/fault" + errorID + "/");
//                    FileUtils.forceMkdir(output);
//                    ImageIO.write(img, "png", new File(output + "/misalignedWidth" + captureWidth + ".png"));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//
//            } else {
////                System.out.println("ALL IS GOOD");
//            }
//
//        }
//    }

    private boolean notSameValues(HashMap<Node, ArrayList<AlignmentConstraint>> values) {
        int previous = 0;
        for (ArrayList<AlignmentConstraint> cons : values.values()) {
            if ((cons.size() != previous) && (previous != 0)) {
                return true;
            } else {
                previous = cons.size();
            }
        }
        return false;
    }
}
