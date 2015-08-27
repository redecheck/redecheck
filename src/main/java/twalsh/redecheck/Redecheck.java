package twalsh.redecheck;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Dimension;

import java.io.File;
import org.apache.commons.io.FileUtils;
import java.io.IOException;

import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.ErrorHandler;
import twalsh.rlg.RLGComparator;
import twalsh.rlg.ResponsiveLayoutGraph;
import twalsh.rlg.Node;
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
    static String baseUrl = "file:///Users/thomaswalsh/Documents/Workspace/Redecheck/testing/";
    public static String current;

    public static int startWidth;
    public static int finalWidth;

    public static WebDriver driver;

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("(  ____ )(  ____ \\(  __  \\ (  ____ \\(  ____ \\|\\     /|(  ____ \\(  ____ \\| \\    /\\\n"+
                "| (    )|| (    \\/| (  \\  )| (    \\/| (    \\/| )   ( || (    \\/| (    \\/|  \\  / /\n" +
                "| (____)|| (__    | |   ) || (__    | |      | (___) || (__    | |      |  (_/ / \n" +
                "|     __)|  __)   | |   | ||  __)   | |      |  ___  ||  __)   | |      |   _ (  \n" +
                "| (\\ (   | (      | |   ) || (      | |      | (   ) || (      | |      |  ( \\ \\ \n" +
                "| ) \\ \\__| (____/\\| (__/  )| (____/\\| (____/\\| )   ( || (____/\\| (____/\\|  /  \\ \n" +
                "|/   \\__/(_______/(______/ (_______/(_______/|/     \\|(_______/(_______/|_/    \\/");

        current = new java.io.File( "." ).getCanonicalPath();
        System.setProperty("phantomjs.binary.path", current + "/phantomjs");
        String oracle = args[0];
        String test = args[1];
        String ss = args[2];
        startWidth = Integer.valueOf(args[3]);
        finalWidth = Integer.valueOf(args[4]);
        int stepSize = Integer.valueOf(ss);

        int[] sampleWidths = buildWidthArray(startWidth, finalWidth, stepSize);

        runTool(oracle, test, sampleWidths);
    }

    public static void runTool(String oracle, String test, int[] widths) throws InterruptedException {
        String oracleUrl = baseUrl + oracle + "/index.html";
        driver = new PhantomJSDriver();
        long startTime = System.nanoTime();
        startPhantomJS(oracleUrl);
        System.out.println(oracleUrl);
        capturePageModel(oracleUrl, widths);


        // Construct oracle RLG
        Map<Integer, DomNode> oracleDoms = loadDoms(widths, oracleUrl);
        ArrayList<AlignmentGraph> oracleAgs = new ArrayList<AlignmentGraph>();

        for (int width : widths) {
            DomNode dn = oracleDoms.get(width);
            AlignmentGraph ag = new AlignmentGraph(dn);
            oracleAgs.add(ag);
        }
        ResponsiveLayoutGraph oracleRlg = new ResponsiveLayoutGraph(oracleAgs, widths, oracleUrl, oracleDoms);
        long endTime = System.nanoTime();
        long duration = (endTime-startTime) / 1000000000;
        System.out.println("TIME TAKEN : " + duration + " SECONDS");
//        driver.close();

        // Construct test version RLG
//        String testUrl = baseUrl + oracle + "/1.html";
//        startPhantomJS(testUrl);
//        capturePageModel(testUrl, widths);
//
//        Map<Integer, DomNode> testDoms = loadDoms(widths, testUrl);
//        ArrayList<AlignmentGraph> testAgs = new ArrayList<AlignmentGraph>();
//
//        for (int width : widths) {
//            DomNode dn = testDoms.get(width);
//            AlignmentGraph ag = new AlignmentGraph(dn);
//            testAgs.add(ag);
//        }
//        ResponsiveLayoutGraph testRlg = new ResponsiveLayoutGraph(testAgs, widths, testUrl, testDoms);
//        driver.close();
//        // Perform the diff
//        System.out.println("COMPARING TEST VERSION TO THE ORACLE \n");
//        RLGComparator comp = new RLGComparator(oracleRlg, testRlg);
//        comp.compare();
//        comp.compareMatchedNodes();
//        comp.writeRLGDiffToFile(oracle, "/rlgDiffResults", baseUrl, comp.issues);
//        System.out.println("TESTING COMPLETE.");

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
        try {
            int counter = 0;
            for (int i = 0; i < widths.length; i++) {

                String outFolder;
                int w = widths[i];
                outFolder = current + "/../output/" + url.replaceAll("/", "") + "/" + "width" + w;
                File theDir = new File(outFolder);
                if (!theDir.exists()) {

                    boolean result = false;
                    try {
                        theDir.mkdir();
                        result = true;
                    } catch (SecurityException se) {
                        //handle it
                    }
                }
                driver.manage().window().setSize(new Dimension(w, 600));
                FileUtils.writeStringToFile(new File(outFolder + "/dom.js"), extractDOM(url, driver, counter));
//                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    public static void startPhantomJS(String urlToGet) {
//        driver = new PhantomJSDriver();
        driver.get(urlToGet);
    }

    public static void closePhantomJS() {
        driver.close();
    }
}
