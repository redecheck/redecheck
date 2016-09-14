package twalsh.analysis;

import org.openqa.selenium.WebDriver;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Created by thomaswalsh on 31/05/2016.
 */
public abstract class ResponsiveLayoutError {
    public abstract void captureScreenshotExample(int errorID, String url, WebDriver webDriver, String fullUrl, HashMap<Integer, BufferedImage> imageMap, String timeStamp);


}
