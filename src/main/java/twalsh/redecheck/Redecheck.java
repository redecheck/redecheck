package twalsh.redecheck;

import edu.gatech.xpert.dom.layout.AlignmentGraphFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;

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
    public static int[] widthsToCheck;
    static HashMap<Integer, DomNode> oracleDoms;
	static HashMap<Integer, DomNode> testDoms;
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
        System.setProperty("phantomjs.binary.path", current + "/../resources/phantomjs");
        CommandLineParser clp = new CommandLineParser();
        new JCommander(clp, args);
        String oracle = clp.oracle;
        String test = clp.test;
        preamble = clp.preamble;
        startWidth = clp.startWidth;
        finalWidth = clp.endWidth;
//        String[] widthSplits = clp.checkWidths.split(",");
//        widthsToCheck = new int[widthSplits.length];
//        for (int i = 0; i < widthSplits.length; i++) {
//        	widthsToCheck[i] = Integer.valueOf(widthSplits[i]);
//        }
        widthsToCheck = new int[]{};
        
        oracleDoms = new HashMap<Integer, DomNode>();
        testDoms = new HashMap<Integer, DomNode>();
        
        int stepSize = clp.ss;
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
        String[] phantomArgs = new  String[] {
        	    "--webdriver-loglevel=NONE"
        	};
        dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        driver = getNewDriver(dCaps);
        js = (JavascriptExecutor) driver;
        scriptToExtract = Utils.readFile(current +"/../resources/webdiff2.js");
        
        // Access oracle webpage and sample
        System.out.println("\n\nGENERATING ORACLE RLG for " + oracle);
        String oracleUrl = oracle + ".html";
        driver.get(preamble + oracleUrl);
        capturePageModel(oracleUrl, widths, oracleDoms);

        // Construct oracle RLG
        ArrayList<AlignmentGraphFactory> oracleAgs = new ArrayList<AlignmentGraphFactory>();
        for (int width : widths) {
            DomNode dn = oracleDoms.get(width);
            AlignmentGraphFactory agf = new AlignmentGraphFactory(dn);
            oracleAgs.add(agf);
        }
        ResponsiveLayoutGraph oracleRlg = new ResponsiveLayoutGraph(oracleAgs, widths, oracleUrl, oracleDoms);
        oracleRlg.writeToGraphViz("oracle");
        
//      Access test webpage and sample
        System.out.println("\n\nGENERATING TEST RLG");
        String testUrl = test + ".html";
        driver.get(preamble + testUrl);
        capturePageModel(testUrl, widths, testDoms);

        // Construct test RLG
        ArrayList<AlignmentGraphFactory> testAgs = new ArrayList<AlignmentGraphFactory>();
        for (int width : widths) {
            DomNode dn = testDoms.get(width);
            AlignmentGraphFactory agf = new AlignmentGraphFactory(dn);
            testAgs.add(agf);
        }
        
        ResponsiveLayoutGraph testRlg = new ResponsiveLayoutGraph(testAgs, widths, testUrl, testDoms);
        testRlg.writeToGraphViz("test");
        driver.close();


        // Perform the model comparison
        System.out.println("\n\nCOMPARING TEST VERSION TO THE ORACLE \n");
        RLGComparator comp = new RLGComparator(oracleRlg, testRlg, widthsToCheck);
        comp.compare();
        comp.compareMatchedNodes();
        comp.writeRLGDiffToFile(current, "/" + oracle.replace("/","") + "-" + test.replace("/",""));
        System.out.println("\n\nTESTING COMPLETE.");

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

        // Adds the upper bound to the sample width set, if it's not already there
        if (!Integer.toString(widths.get(widths.size()-1)).equals(Integer.toString(finalWidth))) {
            widths.add(finalWidth);
        }

        // Copy the contents of the arraylist into an array
        int[] widthsArray = new int[widths.size()];

        int counter = 0;
        for (Integer i : widths) {
            widthsArray[counter] = i;
            counter++;
        }
        return widthsArray;
    }

    /**
     * This method samples the DOM of a webpage at a set of viewports, and saves the DOMs into a HashMap
     * @param url		The url of the webpage
     * @param widths	The viewport widths to sample
     * @param doms		The HashMap to save the DOMs into.
     */
    public static void capturePageModel(String url, int[] widths, HashMap<Integer, DomNode> doms) {
    	
    	// Create a parser for the DOM strings
    	JsonDomParser parser = new JsonDomParser();
    	try {            
            // Iterate through all viewport widths
            for (int i = 0; i < widths.length; i++) {  
            	
                int w = widths[i];
                // Resize the browser window
                driver.manage().window().setSize(new Dimension(w, 600));
                
                // Extract the DOM and save it to the HashMap.
                String extractedDom = extractDOM(url, driver, scriptToExtract);
                doms.put(w, parser.parseJsonDom(extractedDom));
                
            }
    	} catch (Exception e) {
    		
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
            String file = current + "/../output/" + url.replaceAll("/", "").replace(".html", "") + "/" + "width" + width + "/dom.js";
            try {
                String domStr = FileUtils.readFileToString(new File(file));
                doms.put(width, parser.parseJsonDom(domStr));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return doms;
    }

    /**
     * Captures a screenshot of the webpage
     * @param screenshot	File into which to save the image
     * @param driver		The driver to use to get the screenshot
     */
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

    /**
     * Utility method which compares two DOMs
     * @param dn1	The first DOM
     * @param dn2	The second DOM
     * @return		Whether the DOMs are equal
     */
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

    /**
     * Takes 2 sets of coordinates and compares them
     * @param coords1		The first set of coords
     * @param coords2		The second set of coords
     * @return
     */
    private static boolean coordsMatch(int[] coords1, int[] coords2) {
        if ((coords1 == null) && (coords2 == null)) {
            return true;
        } else if (!hasMeaningfulSize(coords1, coords2)) {
            return true;
        } else if (coords1 == null) {
            return false;
        } else if (coords2 == null) {
            return false;
        } else {
            for (int i = 0; i < 4; i++) {
                if (coords1[i] != coords2[i]) {
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
        for (int i =0; i < tagsIgnore.length; i++) {
            if (tagsIgnore[i].equals(tagName)) {
                return true;
            }
        }
        return false;
    }
}
