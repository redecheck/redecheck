package twalsh.redecheck;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.io.File;
import org.apache.commons.io.FileUtils;
import java.io.IOException;

import com.beust.jcommander.JCommander;

import org.openqa.selenium.remote.DesiredCapabilities;
import twalsh.rlg.ResponsiveLayoutGraph;
import xpert.dom.JsonDomParser;
import xpert.dom.DomNode;
import xpert.ag.AlignmentGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Redecheck {
    // Instance variables
    public static String current;
    public static String preamble;
    public static int startWidth;
    public static int finalWidth;
    public static WebDriver driver;

    /**
     * Main method to handle execution of the whole tool
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("(  ____ )(  ____ \\(  __  \\ (  ____ \\(  ____ \\|\\     /|(  ____ \\(  ____ \\| \\    /\\\n"+
                "| (    )|| (    \\/| (  \\  )| (    \\/| (    \\/| )   ( || (    \\/| (    \\/|  \\  / /\n" +
                "| (____)|| (__    | |   ) || (__    | |      | (___) || (__    | |      |  (_/ / \n" +
                "|     __)|  __)   | |   | ||  __)   | |      |  ___  ||  __)   | |      |   _ (  \n" +
                "| (\\ (   | (      | |   ) || (      | |      | (   ) || (      | |      |  ( \\ \\ \n" +
                "| ) \\ \\__| (____/\\| (__/  )| (____/\\| (____/\\| )   ( || (____/\\| (____/\\|  /  \\ \n" +
                "|/   \\__/(_______/(______/ (_______/(_______/|/     \\|(_______/(_______/|_/    \\/");

        current = new java.io.File( "." ).getCanonicalPath();
        System.setProperty("phantomjs.binary.path", current + "/resources/phantomjs");
        CommandLineParser jce = new CommandLineParser();
        new JCommander(jce, args);
        String oracle = jce.oracle;
        String test = jce.test;
        preamble = jce.preamble;
        startWidth = jce.startWidth;
        finalWidth = jce.endWidth;
        int stepSize = jce.ss;
        int[] sampleWidths = buildWidthArray(startWidth, finalWidth, stepSize);

        runTool(oracle, test, preamble, sampleWidths);
    }

    /**
     * Samples each webpage and constructs an RLG for each, then compares the two, writing the model differences to a file.
     * @param oracle        the URL of the oracle version of the page
     * @param test          the URL of the test version of the page
     * @param widths        the set of sampling widths to use.
     * @throws InterruptedException
     */
    public static void runTool(String oracle, String test, String preamble, int[] widths) throws InterruptedException {
        // Set up the PhantomJS driver to gather the DOMs
        DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", false);
        driver = new PhantomJSDriver(dCaps);

        // Access oracle webpage and sample
        String oracleUrl = preamble + oracle + ".html";
        driver.get(oracleUrl);
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
        System.out.println("NUMBER OF DOMS: " + oracleRlg.getAlreadyGathered().size());
        oracleRlg.writeToGraphViz("oracle");

        // Access test webpage and sample
        String testUrl = preamble + test + ".html";
        driver.get(testUrl);
        capturePageModel(testUrl, widths);

        // Construct test RLG
        Map<Integer, DomNode> testDoms = loadDoms(widths, testUrl);
        ArrayList<AlignmentGraph> testAgs = new ArrayList<AlignmentGraph>();
        for (int width : widths) {
            DomNode dn = testDoms.get(width);
            AlignmentGraph ag = new AlignmentGraph(dn);
            testAgs.add(ag);
        }
        ResponsiveLayoutGraph testRlg = new ResponsiveLayoutGraph(testAgs, widths, testUrl, testDoms);
        System.out.println("NUMBER OF DOMS: " + testRlg.getAlreadyGathered().size());
        testRlg.writeToGraphViz("test");
        driver.close();

        // Perform the model comparison
        System.out.println("COMPARING TEST VERSION TO THE ORACLE \n");
        RLGComparator comp = new RLGComparator(oracleRlg, testRlg);
        comp.compare();
        comp.compareMatchedNodes();
        comp.writeRLGDiffToFile(current, "/" + oracle.replace("/","") + "-" + test.replace("/",""));
        System.out.println("TESTING COMPLETE.");

        driver.quit();
    }

    /**
     * Takes lower and upper bounds and a step size, and generates a set of widths at which to sample
     * @param startWidth        the lower bound i.e. width at which to start sampling
     * @param finalWidth        the upper boung i.e. width at which to finish sampling
     * @param stepSize          the step size or interval at which to sample
     * @return                  array of widths at which to sample the webpage
     */
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

    /**
     * Samples the webpage by resizing the browser window to a sequence of widths and then extracting the DOM at each.
     * @param url           URL of webpage to be sampled
     * @param widths        widths at which to sample the page
     * @throws InterruptedException
     */
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
                FileUtils.writeStringToFile(new File(outFolder + "/dom.js"), extractDOM(url, driver));
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Executes the WebDiff javascript code on the webpage to extract the DOM.
     * @param url           the URL of the webpage
     * @param driver        the driver being used to access the page
     * @return              the DOM as a JSON string
     * @throws IOException
     */
    public static String extractDOM(String url, WebDriver driver) throws IOException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String current = new java.io.File( "." ).getCanonicalPath();
        String script = Utils.readFile(current +"/resources/webdiff2.js");
        return (String) js.executeScript(script);
    }

    /**
     * Loads the DOM of a given webpage at a specified set of resolutions into a Map for easy access
     * @param widths        the widths at which to load the DOM
     * @param url           the URL of the webpage
     * @return
     */
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
