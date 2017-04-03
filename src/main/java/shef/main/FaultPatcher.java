package shef.main;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import shef.layout.LayoutFactory;
import shef.utils.BrowserFactory;
import shef.utils.ResultProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thomaswalsh on 03/04/2017.
 */
public class FaultPatcher {
    private WebDriver webDriver;
    private String[] errors;
    private String[] classifications;
    public String browser, fullUrl, url, rPath, cPath;
    private HashMap<Integer, LayoutFactory> layoutFactories;

    public FaultPatcher(String fullUrl, String url, String browser, String current, String rp, String cp) {
        try {
            webDriver = BrowserFactory.getNewDriver(browser);
            webDriver.get(fullUrl);
            JavascriptExecutor js = (JavascriptExecutor) webDriver;

            layoutFactories = new HashMap<>();

            classifications = ResultProcessor.getClassifications(new File(current+"/../reports/" + url.split("/")[0]+"/"+cp), 3);
            errors = ResultProcessor.getFailureStrings(new File(current+"/../reports/" + url.split("/")[0]+"/"+rp));
            // Trying to interact with the CSS of the web page
            String originalValue = (String) js.executeScript("var myElement = document.querySelector('h1'); var styles = window.getComputedStyle(myElement); return styles.getPropertyValue('font-size');");
            js.executeScript("var myElement = document.querySelector('h1'); myElement.style.fontSize = '20px';");
            String afterValue = (String) js.executeScript("var myElement = document.querySelector('h1'); var styles = window.getComputedStyle(myElement); return styles.getPropertyValue('font-size');");
            Tool.capturePageModel(fullUrl, new int[] {800}, 50, false, false, webDriver, null, layoutFactories);
            LayoutFactory layoutFactory = layoutFactories.get(800);
            System.out.println(layoutFactory.layout.toString());
//            function getElementByXpath(path) {
//            return document.evaluate(path, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
//}
//
//            console.log( getElementByXpath("//html[1]/body[1]/div[1]") );

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Make sure the WebDriver is closed down
            if (webDriver != null) {
                webDriver.quit();
            }
        }
    }
}
