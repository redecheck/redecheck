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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by thomaswalsh on 31/05/2016.
 */
public class OverflowFailure extends ResponsiveLayoutFailure {
    AlignmentConstraint ac1, ac2, ofCon, match;
    HashMap<Node, ArrayList<AlignmentConstraint>> map;



    Node overflowed;
    Node intendedParent;
    ArrayList<Node> newParents;


    public HashMap<Node, ArrayList<AlignmentConstraint>> getMap() {
        return map;
    }

    public OverflowFailure(HashMap<Node, ArrayList<AlignmentConstraint>> m, Node ip, Node n) {
        this.map = m;
        intendedParent = ip;
        overflowed = n;
//        System.out.println(n);
    }

    public OverflowFailure(Node o, AlignmentConstraint ac) {
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

    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullUrl, HashMap<Integer, BufferedImage> imageMap, String timeStamp) {

        try {
//            for (Node p : map.keySet()) {
                AlignmentConstraint aCon = ofCon;
//                        map.get(p).get(0);
                int captureWidth = (aCon.getMin() + aCon.getMax()) / 2;
                HashMap<Integer, LayoutFactory> lfs = new HashMap<>();
                BufferedImage img;
//                if (imageMap.containsKey(captureWidth)) {
//                    img = imageMap.get(captureWidth);
//                } else {
                    img = RLGThread.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);
//                    imageMap.put(captureWidth, img);
//                }
                LayoutFactory lf = lfs.get(captureWidth);
                Element e1 = lf.getElementMap().get(aCon.getNode1().getXpath());
                Element e2 = lf.getElementMap().get(aCon.getNode2().getXpath());
//                Element ip = lf.getElementMap().get(intendedParent.getXpath());

//                System.out.println(captureWidth);
//                System.out.println(e1 + e1.getParent().getXpath());
//                System.out.println(e2 + e2.getParent().getXpath());
//                System.out.println(ip + ip.getParent().getXpath());



                Graphics2D g2d = img.createGraphics();
                g2d.setColor(Color.RED);
//                g2d.setStroke(new BasicStroke(5));
                int[] coords1 = e1.getBoundingCoords();
                g2d.drawRect(coords1[0], coords1[1], coords1[2] - coords1[0], coords1[3] - coords1[1]);

                g2d.setColor(Color.CYAN);
//                g2d.setStroke(new BasicStroke(3));
                int[] coords2 = e2.getBoundingCoords();
                g2d.drawRect(coords2[0], coords2[1], coords2[2] - coords2[0], coords2[3] - coords2[1]);

//                if (e1 != ip) {
////                    System.out.println(e2.getXpath() + );
//                    g2d.setColor(Color.GREEN);
//                    g2d.setStroke(new BasicStroke(2));
//                    int[] coords3 = ip.getBoundingCoords();
//                    g2d.drawRect(coords3[0], coords3[1], coords3[2] - coords3[0], coords3[3] - coords3[1]);
//                }


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
                    File image;
//                    if (p.getXpath().equals(intendedParent.getXpath())) {
//                        image = new File(output + "/overflow-intendedWidth" + captureWidth + ".png");
//                    } else {
//                        image = new File(output + "/overflow-overflowedWidth" + captureWidth + ".png");
//                    }
                    image = new File(output + "/overflow-Width" + captureWidth + ".png");
                    FileUtils.forceMkdir(output);
                    ImageIO.write(img, "png", image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
//
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
