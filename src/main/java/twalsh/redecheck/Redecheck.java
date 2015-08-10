package twalsh.redecheck;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Dimension;

import java.io.File;
import org.apache.commons.io.FileUtils;
import java.io.IOException;

import xpert.dom.JsonDomParser;
import xpert.dom.DomNode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Redecheck {
    public static String OUTPUT = "file:///Users/thomaswalsh/Documents/Workspace/Redecheck/output/";
    public static int SLEEP_TIME = 250;
    public static String url;
    static String baseUrl = "file:///Users/thomaswalsh/Documents/Workspace/Redecheck/testing/";
    public static String current;

    public static int startWidth = 400;
    public static int finalWidth = 1300;

    public static void main(String[] args) throws InterruptedException, IOException {
        current = new java.io.File( "." ).getCanonicalPath();
        System.setProperty("webdriver.chrome.driver", "/Users/thomaswalsh/Documents/Workspace/JARs/chromedriver");
        String oracle = args[0];
        String test = args[1];
        String ss = args[2];
        int stepSize = Integer.valueOf(ss);
        System.out.println(oracle);
        System.out.println(test);

        String[] sampleWidths = buildWidthArray(startWidth, finalWidth, stepSize);

        runTool(oracle, test, sampleWidths);
    }

    public static void runTool(String oracle, String test, String[] widths) throws InterruptedException {
        url = baseUrl + oracle + "/index.html";
        capturePageModel(url, widths);

        url = baseUrl + test + "/index.html";
        capturePageModel(url, widths);
    }

    public static String[] buildWidthArray(int startWidth, int finalWidth, int stepSize) {
        int currentWidth = startWidth;
        ArrayList<String> widths = new ArrayList<String>();
        widths.add(Integer.toString(startWidth));

        while (currentWidth + stepSize <= finalWidth) {
            currentWidth = currentWidth + stepSize;
            widths.add(Integer.toString(currentWidth));
        }
        if (!widths.contains(Integer.toString(finalWidth))) {
            widths.add(Integer.toString(finalWidth));
        }

        String[] widthsArray = new String[widths.size()];

        int counter = 0;
        for (String s : widths) {
            widthsArray[counter] = s;
            counter++;
        }
        return widthsArray;
    }

    public static void capturePageModel(String url, String[] widths) throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(url);
//            Thread.sleep(2500);
//            JavascriptExecutor js = (JavascriptExecutor) driver;
//            String dropdownScript = Utils.getPkgFileContents(Redecheck.class, "dropdown.js");
//			js.executeScript(dropdownScript);
            Thread.sleep(1000);

            int counter = 0;
            for (int i = 0; i < widths.length; i++) {

                String outFolder;
                int w = Integer.parseInt(widths[i]);
                System.out.println(w);
                outFolder = current + "/../output/" + url.replaceAll("/", "") + "/" + "width" + w;
                File theDir = new File(outFolder);
                if (!theDir.exists()) {
                    driver.manage().window().setSize(new Dimension(w, 600));
                    Thread.sleep(SLEEP_TIME);
                    boolean result = false;
                    try{
                        theDir.mkdir();
                        result = true;
                    } catch (SecurityException se) {
                        //handle it
                    }
                    FileUtils.writeStringToFile(new File(outFolder + "/dom.js"), extractDOM(url, driver, counter));
                }
                counter++;
            }
            driver.quit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    public static String extractDOM(String url, WebDriver driver, int c) throws IOException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String current = new java.io.File( "." ).getCanonicalPath();
        String script = Utils.readFile(current +"/../src/main/java/twalsh/redecheck/webdiff2.js");
        return (String) js.executeScript(script);
    }

    private static Map<String, DomNode> loadDoms(String[] widths, String url, boolean testing) {
        Map<String, DomNode> doms = new HashMap<String, DomNode>();
        JsonDomParser parser = new JsonDomParser();
        for (String width : widths) {
            String file = current + "/../output/" + url.replaceAll("/", "") + "/" + "width" + width + "/dom.js";
            try {
                String domStr = FileUtils.readFileToString(new File(file));
                doms.put(width, parser.parseJsonDom(domStr));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return doms;
    }
}
