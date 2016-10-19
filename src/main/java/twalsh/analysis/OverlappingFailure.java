package twalsh.analysis;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by thomaswalsh on 16/06/2016.
 */
public class OverlappingFailure extends ResponsiveLayoutFailure {
    public AlignmentConstraint getConstraint() {
        return constraint;
    }

    AlignmentConstraint constraint;

    public OverlappingFailure(AlignmentConstraint con) {
        constraint = con;
    }

    public String toString() {
        return "ELEMENTS " + constraint.getNode1().getXpath() + " AND " + constraint.getNode2().getXpath() + " ARE OVERLAPPING BETWEEN " + constraint.getMin() + " AND " + constraint.getMax();
    }

    @Override
    public void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullUrl, HashMap<Integer, BufferedImage> imageMap, String timeStamp) {
        int captureWidth = (constraint.getMin()+constraint.getMax())/2;
        HashMap<Integer, LayoutFactory> lfs = new HashMap<>();


        BufferedImage img;
//        if (imageMap.containsKey(captureWidth)) {
//            img = imageMap.get(captureWidth);
//        } else {
            img = RLGThread.getScreenshot(captureWidth, errorID, lfs, webDriver, fullUrl);
//            imageMap.put(captureWidth, img);
//        }

        LayoutFactory lf = lfs.get(captureWidth);
        Element e1 = lf.getElementMap().get(constraint.getNode1().getXpath());
        Element e2 = lf.getElementMap().get(constraint.getNode2().getXpath());

        System.out.println(Arrays.toString(e1.getContentCoords()));

//        WebElement we1 = webDriver.findElement(By.xpath(constraint.getNode1().getXpath()));
//        System.out.println(we1.getRect());

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
            String[] splits = url.split("/");
            String webpage = splits[0];
            String mutant = "index-" + timeStamp;
//                    splits[1];
            File output = new File(new java.io.File( "." ).getCanonicalPath() + "/../reports/"  + webpage + "/" + mutant + "/fault" + errorID + "/");
            FileUtils.forceMkdir(output);
            ImageIO.write(img, "png", new File(output+"/overlapWidth" + captureWidth + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashSet<Node> getNodes() {
        HashSet<Node> nodes = new HashSet<>();
        nodes.add(constraint.getNode1());
        nodes.add(constraint.getNode2());
        return nodes;
    }
}
