package twalsh.analysis;

import org.openqa.selenium.WebDriver;
import twalsh.rlg.Node;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by thomaswalsh on 31/05/2016.
 */
public abstract class ResponsiveLayoutError {
    public abstract void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullUrl, HashMap<Integer, BufferedImage> imageMap, String timeStamp);


    public abstract HashSet<Node> getNodes();

}
