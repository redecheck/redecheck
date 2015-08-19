package twalsh.redecheck;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Dimension;

import java.io.File;
import org.apache.commons.io.FileUtils;
import java.io.IOException;

import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.ErrorHandler;
import twalsh.rlg.ResponsiveLayoutGraph;
import xpert.dom.JsonDomParser;
import xpert.dom.DomNode;
import xpert.ag.AlignmentGraph;

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
    static String baseUrl = "file:///Users/thomaswalsh/Documents/Workspace/Redecheck/testing/";
    public static String current;

    public static int startWidth;
    public static int finalWidth;

    public static WebDriver driver;

    public static void main(String[] args) throws InterruptedException, IOException {
        current = new java.io.File( "." ).getCanonicalPath();
//        System.setProperty("webdriver.chrome.driver", "/Users/thomaswalsh/Documents/Workspace/JARs/chromedriver");
        String oracle = args[0];
        String test = args[1];
        String ss = args[2];
        startWidth = Integer.valueOf(args[3]);
        finalWidth = Integer.valueOf(args[4]);
        int stepSize = Integer.valueOf(ss);
        System.out.println(oracle);
        System.out.println(test);

        int[] sampleWidths = buildWidthArray(startWidth, finalWidth, stepSize);

        runTool(oracle, test, sampleWidths);
    }

    public static void runTool(String oracle, String test, int[] widths) throws InterruptedException {
        String oracleUrl = baseUrl + oracle + "/index.html";
        driver = new PhantomJSDriver();
        driver.get(oracleUrl);
        capturePageModel(oracleUrl, widths);

//        String testUrl = baseUrl + test + "/index.html";
//        capturePageModel(testUrl, widths);

        Map<Integer, DomNode> oracleDoms = loadDoms(widths, oracleUrl);
        ArrayList<AlignmentGraph> oracleAgs = new ArrayList<AlignmentGraph>();

        for (int width : widths) {
            DomNode dn = oracleDoms.get(width);
            AlignmentGraph ag = new AlignmentGraph(dn);
            oracleAgs.add(ag);
        }

        ResponsiveLayoutGraph rlg = new ResponsiveLayoutGraph(oracleAgs, widths, oracleUrl, oracleDoms, driver);
        driver.quit();
    }

    public static int[] buildWidthArray(int startWidth, int finalWidth, int stepSize) {
        int currentWidth = startWidth;
        ArrayList<Integer> widths = new ArrayList<Integer>();
        widths.add(startWidth);

        while (currentWidth + stepSize <= finalWidth) {
            currentWidth = currentWidth + stepSize;
            widths.add(currentWidth);
        }

        if (!Integer.toString(widths.get(widths.size()-1)).equals(Integer.toString(finalWidth))) {
            System.out.println("Adding final width");
            widths.add(finalWidth);
        }

        int[] widthsArray = new int[widths.size()];

        int counter = 0;
        for (Integer i : widths) {
            widthsArray[counter] = i;
            counter++;
        }
        return widthsArray;
    }

    public static void capturePageModel(String url, int[] widths) throws InterruptedException {
//        driver = new PhantomJSDriver();
//        driver.get(url);
        try {

//            Thread.sleep(2500);
//            JavascriptExecutor js = (JavascriptExecutor) driver;
//            String dropdownScript = Utils.getPkgFileContents(Redecheck.class, "dropdown.js");
//			js.executeScript(dropdownScript);
            Thread.sleep(1000);

            int counter = 0;
            for (int i = 0; i < widths.length; i++) {

                String outFolder;
//                int w = Integer.parseInt(widths[i]);
                int w = widths[i];
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

//            driver.quit();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ErrorHandler.UnknownServerException e) {

        } finally {
//            driver.close();
//            driver.quit();
        }
    }

    public static String extractDOM(String url, WebDriver driver, int c) throws IOException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String current = new java.io.File( "." ).getCanonicalPath();
        String script = Utils.readFile(current +"/../src/main/java/twalsh/redecheck/webdiff2.js");
        return (String) js.executeScript(script);
    }

    public static Map<Integer, DomNode> loadDoms(int[] widths, String url) {
        Map<Integer, DomNode> doms = new HashMap<Integer, DomNode>();
        JsonDomParser parser = new JsonDomParser();
        for (int width : widths) {
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
