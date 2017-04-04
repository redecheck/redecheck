package shef.main;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import shef.layout.LayoutFactory;
import shef.rlg.Node;
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
    private String[] errors, classifications, categories;
    public String browser, fullUrl, url, rPath, cPath;
    private HashMap<Integer, LayoutFactory> layoutFactories;

    public FaultPatcher(String fullUrl, String url, String browser, String current, String rp, String cp) {

        try {
            webDriver = BrowserFactory.getNewDriver(browser);
            webDriver.get(fullUrl);
            JavascriptExecutor js = (JavascriptExecutor) webDriver;

            layoutFactories = new HashMap<>();
            errors = ResultProcessor.getFailureStrings(new File(current+"/../reports/" + url.split("/")[0]+"/"+rp));
            classifications = ResultProcessor.getClassifications(new File(current+"/../reports/" + url.split("/")[0]+"/"+cp), errors.length);
            categories = ResultProcessor.getCategories(new File(current+"/../reports/" + url.split("/")[0]+"/"+cp), errors.length);


            for (int i = 0; i < errors.length; i++) {
//                if (classifications[i].equals("TP")) {
                    String error = errors[i];
                    ArrayList<String> nodes = getNodesFromError(error, categories[i]);
//                }
            }

            // Trying to interact with the CSS of the web page
//            String originalValue = (String) js.executeScript("var myElement = document.querySelector('h1'); var styles = window.getComputedStyle(myElement); return styles.getPropertyValue('font-size');");
//            js.executeScript("var myElement = document.querySelector('h1'); myElement.style.fontSize = '20px';");
//            String afterValue = (String) js.executeScript("var myElement = document.querySelector('h1'); var styles = window.getComputedStyle(myElement); return styles.getPropertyValue('font-size');");
//            Tool.capturePageModel(fullUrl, new int[] {800}, 50, false, false, webDriver, null, layoutFactories);
//            LayoutFactory layoutFactory = layoutFactories.get(800);
//            System.out.println(layoutFactory.layout.toString());
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


//    WRAPPING ELEMENT ERROR FOR RANGE 398 -> 470:	/HTML/BODY/DIV/DIV/DIV/NAV/UL/LI[4] wrapped from row
// [ /HTML/BODY/DIV/DIV/DIV/NAV/UL/LI[3] /HTML/BODY/DIV/DIV/DIV/NAV/UL/LI[2] /HTML/BODY/DIV/DIV/DIV/NAV/UL/LI /HTML/BODY/DIV/DIV/DIV/NAV/UL/LI[4] ]
    private ArrayList<String> getNodesFromError(String error, String category) {
        ArrayList<String> nodes = new ArrayList<>();
        if (category.equals("Wrapping")) {
            String wrapped = error.split(":\t")[1].split(" wrapped")[0];
            System.out.println(wrapped);
            String[] row = error.split("row \t")[1].split(" ");
            for (String r : row) {
                nodes.add(r);
            }
        } else if (category.equals("Viewport Protrusion")) {
            nodes.add(error.split(" overflowed")[0]);
        } else if (category.equals("Element Collision")) {
            String n1 = error.split("ELEMENTS ")[1].split(" AND ")[0];
            nodes.add(n1);
            String n2 = error.split(" AND ")[1].split(" ARE ")[0];
            nodes.add(n2);
        } else if (category.equals("Element Protrusion")) {
            String[] splits = error.split("\t")[1].split(" , ");
            nodes.add(splits[0]);
            nodes.add(splits[1]);
        } else {

        }

        return nodes;
    }
}
