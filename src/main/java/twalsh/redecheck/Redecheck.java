package twalsh.redecheck;

import edu.gatech.xpert.dom.layout.AlignmentGraphFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.io.File;
import org.apache.commons.io.FileUtils;
import java.io.IOException;

import com.beust.jcommander.JCommander;

import org.openqa.selenium.remote.DesiredCapabilities;
import twalsh.rlg.ResponsiveLayoutGraph;
import edu.gatech.xpert.dom.JsonDomParser;
import edu.gatech.xpert.dom.DomNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Redecheck {
    // Instance variables
    public static String current;
    public static String preamble;
    public static int startWidth;
    public static int finalWidth;
    public static PhantomJSDriver driver;
    public static JavascriptExecutor js;
    static String scriptToExtract;
    public static final String[] tagsIgnore = { "A", "AREA", "B", "BLOCKQUOTE",
            "BR", "CANVAS", "CENTER", "CSACTIONDICT", "CSSCRIPTDICT", "CUFON",
            "CUFONTEXT", "DD", "EM", "EMBED", "FIELDSET", "FONT", "FORM",
            "HEAD", "HR", "I", "LABEL", "LEGEND", "LINK", "MAP", "MENUMACHINE",
            "META", "NOFRAMES", "NOSCRIPT", "OBJECT", "OPTGROUP", "OPTION",
            "PARAM", "S", "SCRIPT", "SMALL", "SPAN", "STRIKE", "STRONG",
            "STYLE", "TBODY", "TITLE", "TR", "TT", "U" };


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
    public static void runTool(String oracle, String test, String preamble, int[] widths) throws InterruptedException, IOException {

        // Set up the PhantomJS driver to gather the DOMs
        DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", true);
        driver = getNewDriver(dCaps);
        js = (JavascriptExecutor) driver;
        scriptToExtract = Utils.readFile(current +"/resources/webdiff2.js");


        long startTime = System.nanoTime();
        // Access oracle webpage and sample
        System.out.println("\n\nGENERATING ORACLE RLG");
        String oracleUrl = preamble + oracle + ".html";
        driver.get(oracleUrl);
        capturePageModel(oracleUrl, widths, true);

        // Construct oracle RLG
        Map<Integer, DomNode> oracleDoms = loadDoms(widths, oracleUrl);
        ArrayList<AlignmentGraphFactory> oracleAgs = new ArrayList<AlignmentGraphFactory>();
        for (int width : widths) {
            DomNode dn = oracleDoms.get(width);
            AlignmentGraphFactory agf = new AlignmentGraphFactory(dn);
            oracleAgs.add(agf);
        }
        ResponsiveLayoutGraph oracleRlg = new ResponsiveLayoutGraph(oracleAgs, widths, oracleUrl, oracleDoms, null, null);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("EXECUTION TIME WAS : " + duration/1000000000 + " SECONDS");
        System.out.println("NUMBER OF DOMS: " + oracleRlg.getAlreadyGathered().size());
        oracleRlg.writeToGraphViz("oracle");

//      Access test webpage and sample
        startTime = System.nanoTime();
        System.out.println("\n\nGENERATING TEST RLG");
        String testUrl = preamble + test + ".html";
        driver.get(testUrl);
        capturePageModel(testUrl, widths, true);

        // Construct test RLG
        Map<Integer, DomNode> testDoms = loadDoms(widths, testUrl);
        ArrayList<AlignmentGraphFactory> testAgs = new ArrayList<AlignmentGraphFactory>();
        for (int width : widths) {
            DomNode dn = testDoms.get(width);
            AlignmentGraphFactory agf = new AlignmentGraphFactory(dn);
            testAgs.add(agf);
        }
        ResponsiveLayoutGraph testRlg = new ResponsiveLayoutGraph(testAgs, widths, testUrl, testDoms, oracleRlg, oracleDoms);
        driver.close();
        endTime = System.nanoTime();
        duration = (endTime - startTime);
        System.out.println("EXECUTION TIME WAS : " + duration/1000000000 + " SECONDS");


        // Perform the model comparison
        System.out.println("\n\nCOMPARING TEST VERSION TO THE ORACLE \n");
        RLGComparator comp = new RLGComparator(oracleRlg, testRlg);
        comp.compare();
        comp.compareMatchedNodes();
        comp.writeRLGDiffToFile(current, "/" + oracle.replace("/","") + "-" + test.replace("/",""));
        System.out.println("\n\nTESTING COMPLETE.");

        driver.quit();

        for (int width : widths) {
            DomNode dn = oracleDoms.get(width);
            DomNode dn2 = testDoms.get(width);
            System.out.println(width + " : " + domsEqual(dn, dn2));
            String oracleImage = current + "/../output/" + oracleUrl.replaceAll("/", "") + "/" + "width" + width + "/screenshot.png";
            String testImage = current + "/../output/" + testUrl.replaceAll("/", "") + "/" + "width" + width + "/screenshot.png";
            ImageComparator ic = new ImageComparator(oracleImage, testImage);
            System.out.println(ic.compare());
        }
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
    public static void capturePageModel(String url, int[] widths, boolean takeScreenshot) throws InterruptedException {
        try {
            int counter = 0;
            for (int i = 0; i < widths.length; i++) {

                String outFolder;
                int w = widths[i];
                outFolder = current + "/../output/" + url.replaceAll("/", "") + "/" + "width" + w;
//                System.out.println(outFolder);
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
                Thread.sleep(100);
                FileUtils.writeStringToFile(new File(outFolder + "/dom.js"), extractDOM(url, driver, scriptToExtract));
                if (takeScreenshot)
                    captureScreenshot(new File(outFolder + "/screenshot.png"), driver);
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
    public static String extractDOM(String url, PhantomJSDriver driver, String script) throws IOException {
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

    public static void captureScreenshot(File screenshot, PhantomJSDriver driver) {
        File scrFile = driver.getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(scrFile, screenshot);
        } catch (IOException e) {
            System.err.println("Error saving screenshot");
            e.printStackTrace();
        }
    }

    public static PhantomJSDriver getNewDriver(DesiredCapabilities dCaps) {
        return new PhantomJSDriver(dCaps);
    }

    public static boolean domsEqual(DomNode dn1, DomNode dn2) {
        if ( (dn1 == null) | (dn2 == null) ) {
            return false;
        }
        int numMatches = 0;
        DomNode match = null;
        ArrayList<DomNode> worklist1 = new ArrayList<DomNode>();
        ArrayList<DomNode> worklist2 = new ArrayList<DomNode>();
        worklist1.add(dn1);
        worklist2.add(dn2);
        while (!worklist1.isEmpty()) {
            DomNode toMatch = worklist1.remove(0);
            String xpath = null;
            if (toMatch.getxPath() == null) {
                xpath = "";
            } else {
                xpath = toMatch.getxPath();
            }
            String parent = null;
            String parent2 = null;
            if (toMatch.getParent() != null) {
                parent = toMatch.getParent().getxPath();
            } else {
                parent = "";
            }
            match=null;
            for (DomNode dn : worklist2) {
                String xpath2 = null;
                if (dn.getxPath() == null) {
                    xpath2 = "";
                } else {
                    xpath2 = dn.getxPath();
                }
                if (xpath.equals(xpath2)) {
//					System.out.println("xpath");
                    if (dn.getParent() != null) {
                        parent2 = dn.getParent().getxPath();
                    } else {
                        parent2 = "";
                    }
                    if (parent.equals(parent2)) {
//						System.out.println("parent");
                        if (coordsMatch(toMatch.getCoords(), dn.getCoords())) {
                            match = dn;
                        }
                    }
                }
            }
            if (match != null) {
                numMatches++;
                worklist2.remove(match);
                for (DomNode d : toMatch.getChildren()) {
                    if (d.getTagName() != null) {
                        if (!ignoreTag(d.getTagName())) {
                            worklist1.add(d);
                        }
                    }
                }
                for (DomNode d2 : match.getChildren()) {
                    if (d2.getTagName() != null) {
                        if (!ignoreTag(d2.getTagName())) {
                            worklist2.add(d2);
                        }
                    }
                }
            } else {
//                System.out.println(toMatch);
                return false;
            }
        }
        return true;
    }

    private static boolean coordsMatch(int[] coords1, int[] coords2) {
        if ((coords1 == null) && (coords2 == null)) {
            return true;
        } else if (!hasMeaningfulSize(coords1, coords2)) {
            return true;
        } else if ((coords1 == null) && (coords2 != null)) {
            return false;
        } else if ((coords1 != null) && (coords2 == null)) {
            return false;
        } else {
            for (int i = 0; i < 4; i++) {
                if (coords1[i] != coords2[i]) {
//                    System.out.println(coords1[0] + ","+coords1[1] + ","+coords1[2] + ","+coords1[3]);
//					System.out.println(coords1[i] +","+ coords2[i]);
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasMeaningfulSize(int[] coords, int[] coords2) {
        if ((coords[2] - coords[0]) < 5) {
            return false;
        } else if ((coords[3]- coords[1]) < 5) {
            return false;
        }
        return true;
    }

    private static boolean ignoreTag(String tagName) {
//		System.out.println(tagName);
        for (int i =0; i < tagsIgnore.length; i++) {

            if (tagsIgnore[i].equals(tagName)) {
//				System.out.println("TRUE");
                return true;
            }
        }
        return false;
    }
}
