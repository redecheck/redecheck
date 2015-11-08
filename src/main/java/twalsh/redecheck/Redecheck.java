package twalsh.redecheck;

import edu.gatech.xpert.dom.layout.AlignmentGraphFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.io.FileUtils;
import java.io.IOException;
import java.io.InputStream;

import com.beust.jcommander.JCommander;

import com.thoughtworks.xstream.*;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.openqa.selenium.remote.DesiredCapabilities;

import twalsh.rlg.Node;
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



    /**
     * Main method to handle execution of the whole tool
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        current = new java.io.File( "." ).getCanonicalPath();
        System.setProperty("phantomjs.binary.path", current + "/../resources/phantomjs");
        CommandLineParser clp = new CommandLineParser();
        new JCommander(clp, args);
        String oracle = clp.oracle;
        String test = clp.test;
        preamble = clp.preamble;
        startWidth = clp.startWidth;
        finalWidth = clp.endWidth;
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
        
//      Access test webpage and sample
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
        driver.close();
        driver.quit();

        // Perform the model comparison
        RLGComparator comp = new RLGComparator(oracleRlg, testRlg, widthsToCheck);
        comp.compare();
        comp.compareMatchedNodes();
        comp.writeRLGDiffToFile(current, "/" + oracle.replace("/","") + "-" + test.replace("/",""));       
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

    
}
