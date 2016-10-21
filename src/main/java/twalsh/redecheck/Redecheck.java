package twalsh.redecheck;


import edu2.gatech.xpert.dom.layout.AGDiff;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;

import java.awt.*;
import java.io.*;

import org.apache.commons.io.FileUtils;

import java.net.URL;
import java.net.URLConnection;

import com.beust.jcommander.JCommander;

import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.MediaExpression;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleMedia;
import cz.vutbr.web.css.StyleSheet;

import org.openqa.selenium.remote.DesiredCapabilities;

import twalsh.mutation.CSSMutator;
import twalsh.mutation.ResultClassifier;
import twalsh.layout.LayoutFactory;
import edu.gatech.xpert.dom.JsonDomParser;
import edu.gatech.xpert.dom.DomNode;
import twalsh.rlg.Node;
import twalsh.rlg.ResponsiveLayoutGraph;
import twalsh.utils.StopwatchFactory;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Redecheck {
    // Instance variables
    public static String oracle;
    public static String test;
    public String url;
    String[] clArgs;
    public String current;
    public String preamble;
    public int startWidth;
    public int finalWidth;
    public int stepSize;
    public String sampleTechnique;
    public boolean binarySearch;
    public boolean timing;
    public int timingID;
    public String browser;
    public String mutantID;
    public boolean screenshot;
    public boolean tool;
    public boolean xpert;
    public int[] widthsToCheck;
    public int[] allWidths;
    public TreeSet<Integer> allTS;
    public boolean saveToExtras;
    static HashMap<Integer, DomNode> oracleDoms;
    static HashMap<Integer, String> oracleDomStrings;
	static HashMap<Integer, DomNode> testDoms;
    static HashMap<Integer, String> testDomStrings;
    static HashMap<Integer, LayoutFactory> oFactories;
    static HashMap<Integer, LayoutFactory> tFactories;
    HashMap<Integer, LayoutFactory> layoutFactories;
    public static PhantomJSDriver driver;
    public static JavascriptExecutor js;
    static String scriptToExtract;
    static String redecheck = "/Users/thomaswalsh/Documents/PhD/Redecheck/";
    static String reportDirectory = "/Users/thomaswalsh/Documents/PhD/Redecheck/reports/";
    static String timesDirectory = "/Users/thomaswalsh/Documents/PhD/Redecheck/times/";
    static String dataDirectory = "/Users/thomaswalsh/Documents/PhD/redecheck-journal-paper-data/";
    static int[] manualWidths = {480, 600, 640, 768, 1024, 1280};
    static int sleep = 50;


    public Redecheck(String[] args) throws IOException, InterruptedException {
        current = new java.io.File( "." ).getCanonicalPath();
        System.setProperty("phantomjs.binary.path", current + "/../resources/phantomjs");
        System.setProperty("webdriver.chrome.driver", current + "/../../../../Downloads/chromedriver");
        CommandLineParser clp = new CommandLineParser();
        clArgs = args;
        new JCommander(clp, clArgs);
        oracle = clp.oracle;
        test = clp.test;
        preamble = clp.preamble;
        startWidth = clp.startWidth;
        finalWidth = clp.endWidth;
        stepSize = clp.ss;
        sampleTechnique = clp.sampling;
        binarySearch = clp.binary;
        mutantID = clp.mutantID;
        screenshot = clp.screenshot;
        tool = clp.tool;
        xpert = clp.xpert;
        browser = clp.browser;
        timing = clp.timing;
        timingID = clp.timingID;
        url = clp.url;
        widthsToCheck = new int[]{};

        oracleDoms = new HashMap<Integer, DomNode>();
        testDoms = new HashMap<Integer, DomNode>();
        oFactories = new HashMap<>();
        tFactories = new HashMap<>();
        allWidths = new int[(finalWidth - startWidth) + 1];
        allTS = new TreeSet<>();
        for (int i = 0; i < allWidths.length; i++) {
            allWidths[i] = i + startWidth;
            allTS.add(i+startWidth);
        }

//        if (tool) {
//            stepSize = clp.ss;
//            runTool();
//        }
//
//        if (xpert) {
//            runXpert(oracle, test);
//        }


        // Setup for new version of tool
        layoutFactories = new HashMap<>();
        runFaultDetector();
    }

    private void runFaultDetector() {
        try {
            Date date = new Date();
            Format formatter = new SimpleDateFormat("YYYY-MM-dd_hh-mm-ss");
            String timeStamp = formatter.format(date);
            scriptToExtract = Utils.readFile(current +"/../resources/webdiff2.js");
            String fullUrl = preamble + url + "/index.html";
            RLGThread thread = new RLGThread(current, fullUrl, url, oracleDoms, layoutFactories, browser, sampleTechnique, binarySearch, startWidth, finalWidth, stepSize, preamble, sleep, timeStamp);
            Thread t = new Thread(thread);
            t.start();
            while(t.isAlive()){}

            ResponsiveLayoutGraph rlg = thread.getRlg();
            int numNodes = rlg.getNodes().size();
            int numVCs = rlg.getVisCons().size();
            int numACs = rlg.getAlignmentConstraints().size();
//            writeRlgStats(url, timeStamp, numNodes, numVCs, numACs);
//            writeTimes(url, thread.getSwf(), timeStamp);
//            if (timing) {
//                writeTimesSpecial(thread.swf, url, timingID);
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    /**
     * Main method to handle execution of the whole tool
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        Redecheck redecheck = new Redecheck(args);
    }

    /**
     * Samples each webpage and constructs an RLG for each, then compares the two, writing the model differences to a file.
     * @throws InterruptedException
     */
    public void runTool() throws InterruptedException, IOException {
        scriptToExtract = Utils.readFile(current +"/../resources/webdiff2.js");
        Date date = new Date();
        Format formatter = new SimpleDateFormat("YYYY-MM-dd_hh-mm-ss");
        String timeStamp = formatter.format(date);
        String oracleUrl = preamble + oracle + ".html";
        String testUrl = preamble + test + ".html";
        RLGThread rlg1 = new RLGThread(current, testUrl, test, testDoms, tFactories, browser, sampleTechnique, binarySearch, startWidth, finalWidth, stepSize, preamble, sleep, timeStamp);
//        RLGThread rlg2 = new RLGThread(current, testUrl, test, testDoms, tFactories, "phantom");
        Thread t1 = new Thread(rlg1);
//        Thread t2 = new Thread(rlg2);

        t1.start();
        while (t1.isAlive()) {

        }
//        t2.start();
//        while (t1.isAlive() || t2.isAlive()) {
//        while ( t2.isAlive()) {
//        }

//        System.out.println(oFactories.size()== tFactories.size());
//        for (Integer i : tFactories.keySet()) {
//            System.out.println(i);
//        }


//        ResponsiveLayoutGraph r = rlg2.getRlg();
//        ResponsiveLayoutGraph r2 = rlg2.getRlg();
//        RLGComparator comp = new RLGComparator(r, r2, widthsToCheck);
//        comp.compare();
//        comp.compareMatchedNodes();
//        comp.printDiff();
//        comp.writeRLGDiffToFile(testUrl, "report-" + sampleTechnique + "-" + stepSize + "-" + binarySearch, true);
//        copyMutantInfo();

//        if (mutantID != null) {
//            comp.writeRLGDiffToFile(redecheck + "screenshots/" + mutantID, "/redecheck-report", false);
//        }

//        for (int w : rlg1.getSampleWidths()) {
//            DomNode or = rlg1.doms.get(w);
//            DomNode mod = rlg2.doms.get(w);
//            System.out.println(w + " " + ResultClassifier.domsEqual(or, mod));
//        }
//        System.out.println(testDoms.size());
//        writeToFile(testUrl, String.valueOf(duration), "time-"+sampleTechnique+"-"+binarySearch );
//        writeToFile(testUrl, String.valueOf(testDoms.size()), "doms-"+sampleTechnique+"-" + stepSize + "-" + binarySearch, dataDirectory);
//        writeToFile(testUrl, String.valueOf(rlg2.getInitialDoms()), "doms-initial-"+sampleTechnique+"-" + stepSize + "-" + binarySearch, dataDirectory);
//        processTimes(rlg2.getSwf(), testUrl);


    }

    private void processTimes(StopwatchFactory swf, String testUrl) {
        String results = "";
        DecimalFormat df = new DecimalFormat("#.##");
        results+= getTimeStringFromStopwatch(swf.getSetup()) + "\n";
        results+= getTimeStringFromStopwatch(swf.getCapture()) + "\n";
        results+= getTimeStringFromStopwatch(swf.getExtract()) + "\n";
        results+= getTimeStringFromStopwatch(swf.getSleep()) + "\n";
        results+= getTimeStringFromStopwatch(swf.getProcess()) + "\n";
        results+= getTimeStringFromStopwatch(swf.getTotal()) + "\n";


        writeToFile(testUrl, results, "timings-" + sampleTechnique + "-" + stepSize+ "-" + binarySearch, dataDirectory);
    }

    private void writeRlgStats(String url, String timeStamp, int numNodes, int numVCs, int numACs) {
        String results = "";
        results+= numNodes + ",";
        results+= numVCs + ",";
        results+= numACs;
        try {
            String outFolder = reportDirectory + url;

            File dir = new File(outFolder+"/rlg-stats.csv");
            PrintWriter output = new PrintWriter(dir);
            output.append(results);
            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeTimes(String url, StopwatchFactory swf, String timeStamp) {
        String results = "";
        DecimalFormat df = new DecimalFormat("#.##");
        results+= getTimeStringFromStopwatch(swf.getRlg()) + ",";
        results+= getTimeStringFromStopwatch(swf.getDetect()) + ",";
        results+= getTimeStringFromStopwatch(swf.getReport()) + ",";

        try {
            String outFolder = reportDirectory + url;

            File dir = new File(outFolder+"/index-" + timeStamp+"/timings.csv");
            PrintWriter output = new PrintWriter(dir);
            output.append(results);
            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeTimesSpecial(StopwatchFactory swf, String url, int timingID) {
        String results = "";
        DecimalFormat df = new DecimalFormat("#.##");
        results+= getTimeStringFromStopwatch(swf.getRlg()) + ",";
        results+= getTimeStringFromStopwatch(swf.getDetect()) + ",";
        results+= getTimeStringFromStopwatch(swf.getReport()) + ",";

        try {
            String outFolder = timesDirectory + url;

            FileUtils.forceMkdir(new File(outFolder));
            File dir = new File(outFolder+"/timings" + timingID + ".csv");
            PrintWriter output = new PrintWriter(dir);
            output.append(results);
            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getTimeStringFromStopwatch(StopWatch sw) {
//        System.out.println(sw.getTime());
//        String[] splits = sw.toString().split(":");
        double time = (sw.getTime()) / 1000.0;
        String timeS =  String.valueOf(time);
//        System.out.println(timeS);
        return timeS;
    }

    private static void copyMutantInfo() {
        try {
            String[] splits = test.split("/");
            String webpage = splits[0];
            String mutant = splits[1];
            File original = new File(redecheck+"testing/"+webpage+"/"+mutant +'/'+mutant+".txt");
//            System.out.println(original.toString());
            File copied = new File(dataDirectory +webpage+"/"+mutant +'/'+mutant+".txt");
            FileUtils.copyFile(original, copied, false);
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    private void runXpert(String oracle, String test) throws IOException {
        String oracleUrl = preamble + oracle + ".html";
        String testUrl = preamble + test + ".html";
        DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", true);
        String[] phantomArgs = new String[]{
                "--webdriver-loglevel=NONE"
        };
        dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        scriptToExtract = Utils.readFile(current + "/../resources/webdiffOld.js");
        driver = getNewDriver(dCaps);

        HashMap<Integer, edu2.gatech.xpert.dom.DomNode> oDNs = new HashMap<>();
        HashMap<Integer, edu2.gatech.xpert.dom.DomNode> tDNs = new HashMap<>();
        driver.get(oracleUrl);
        capturePageModel2(oracleUrl, manualWidths, oDNs, 50, true, driver);
        driver.get(testUrl);
        capturePageModel2(testUrl, manualWidths, tDNs, 50, true, driver);
        driver.close();
        driver.quit();
        String result = "";
        for (int sc : manualWidths) {
//            System.out.println(sc);
            ArrayList<String> totalIssues = new ArrayList<>();
            edu2.gatech.xpert.dom.DomNode oDN = oDNs.get(sc);
            edu2.gatech.xpert.dom.DomNode tDN = tDNs.get(sc);
            edu2.gatech.xpert.dom.layout.AlignmentGraph oAG = new edu2.gatech.xpert.dom.layout.AlignmentGraph(oDN);
            edu2.gatech.xpert.dom.layout.AlignmentGraph tAG = new edu2.gatech.xpert.dom.layout.AlignmentGraph(tDN);

            edu2.gatech.xpert.dom.Matcher matcher = new edu2.gatech.xpert.dom.Matcher();
            edu2.gatech.xpert.dom.MatchResult mr = matcher.newMatcher(oDN, tDN, sc);

            Map<edu2.gatech.xpert.dom.DomNode, edu2.gatech.xpert.dom.DomNode> matchedNodes = mr.getMatched();

            AGDiff agDiff = new AGDiff(matchedNodes, oAG, tAG);
            for (edu2 .gatech.xpert.dom.DomNode a : matchedNodes.keySet()) {
                edu2.gatech.xpert.dom.DomNode b = matchedNodes.get(a);
                List<String> issues = agDiff.diff(a.getxPath(), b.getxPath());
                totalIssues.addAll(issues);
            }
            if (totalIssues.size() > 0) {
                result += sc + "\n\n";
                for (String issue : totalIssues) {
                    result += issue + "\n";
                }
            }
        }
        writeToFile(testUrl, result, "xpert-result", dataDirectory);
//        oracleDoms = new HashMap<>();
//        testDoms = new HashMap<>();
//        oracleDomStrings = new HashMap<>();
//        testDomStrings = new HashMap<>();
//        scriptToExtract = Utils.readFile(current + "/../resources/webdiff2.js");
//
//        gatherAllDoms(oracleDoms, testDoms, oracleDomStrings, testDomStrings, allWidths, oracleUrl, testUrl);
//        System.out.println(oracleDomStrings.size());
        TreeSet<Integer> diffWidths = ResultClassifier.diffAllDoms2(oDNs, tDNs, oracleDomStrings, testDomStrings, manualWidths);
        System.out.println(diffWidths.size());
        String domDiffResult = "";
        int[] ssWidths = new int[diffWidths.size()];
        int counter = 0;
        for (Integer i : diffWidths) {
            ssWidths[counter] = i;
            domDiffResult += i + "\n";
            counter++;
        }
//        driver.quit();
//        writeToFile(testUrl, domDiffResult, "dom-diff-result", dataDirectory);
    }

    public static int[] getSampleWidths(String technique, String shortUrl, WebDriver drive, int startWidth, int finalWidth, int stepSize, String preamble, ArrayList<Integer> breakpoints) {
        int[] widths = null;
        ArrayList<Integer> widthsAL = new ArrayList<Integer>();
        if (technique.equals("uniform")) {
//            System.out.println("Using uniform sampling");
            int currentWidth = startWidth;

            widthsAL.add(startWidth);

            while (currentWidth + stepSize <= finalWidth) {
                currentWidth = currentWidth + stepSize;
                widthsAL.add(currentWidth);
            }

            // Adds the upper bound to the sample width set, if it's not already there
            if (!Integer.toString(widthsAL.get(widthsAL.size()-1)).equals(Integer.toString(finalWidth))) {
                widthsAL.add(finalWidth);
            }

            // Copy the contents of the arraylist into an array
            widths = new int[widthsAL.size()];

            int counter = 0;
            for (Integer i : widthsAL) {
                widths[counter] = i;
                counter++;
            }
        } else if (technique.equals("exhaustive")) {
//            System.out.println("Using exhaustive sampling");
//            widths = allWidths;
        } else if (technique.equals("random")) {

        } else if (technique.equals("breakpoint")) {
//            System.out.println("Using breakpoint sampling");
//            String url = preamble + oracle + ".html";
            ArrayList<String> cssFiles = initialiseFiles(shortUrl, drive);
            ArrayList<RuleMedia> mqSet = getMediaQueries(shortUrl, cssFiles, preamble);
            widths = getBreakpoints(mqSet);

            widthsAL.add(startWidth);
            for (int i : widths) {
                if ( (i >= startWidth) && (i <= finalWidth) ) {
                    widthsAL.add(i);
                }
            }
            widthsAL.add(finalWidth);

            widths = new int[widthsAL.size()];

            int counter = 0;
            for (Integer i : widthsAL) {
                widths[counter] = i;
                counter++;
            }
        } else if (technique.equals("uniformBP")) {
//            System.out.println("Using uniform & breakpoint sampling");
            TreeSet<Integer> widthsTS = new TreeSet<Integer>();
            int currentWidth = startWidth;

            widthsTS.add(startWidth);

            while (currentWidth + stepSize <= finalWidth) {
                currentWidth = currentWidth + stepSize;
                widthsTS.add(currentWidth);
            }
            widthsTS.add(finalWidth);

//            String url = preamble + oracle + ".html";
            ArrayList<String> cssFiles = initialiseFiles(shortUrl, drive);
//            System.out.println("Got " + cssFiles.size() + " files");
            ArrayList<RuleMedia> mqSet = getMediaQueries(shortUrl, cssFiles, preamble);
//            System.out.println("Got " + mqSet.size() + " MQ's");
            int[] widthsBP = getBreakpoints(mqSet);
            for (int w : widthsBP) {
                if ( (w >= startWidth) && (w <= finalWidth) ) {
//                    System.out.println("Added " + w);
                    widthsTS.add(w);
                    breakpoints.add(w);
                } else {
//                    System.out.println("Didn't add " + w);
                }
            }
//            System.out.println(widthsTS);
            widths = new int[widthsTS.size()];
            Iterator iter = widthsTS.iterator();
            int counter = 0;
            while(iter.hasNext()) {
                try {
                    int i = (int) iter.next();
                    widths[counter] = i;
                    counter++;
                } catch (Exception e) {

                }
            }
        }


        // Return the array of widths
        if (widths != null) {
            return widths;
        } else {
            return new int[]{};
        }
    }

    public static void gatherAllDoms(HashMap<Integer, DomNode> oDoms, HashMap<Integer, DomNode> tDoms,HashMap<Integer, String> oDomsS, HashMap<Integer, String> tDomsS, int[] widths, String oracle, String test) {
        System.out.println("GATHERING ALL DOMS");
        System.out.println(widths.length);
        DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", true);
        String[] phantomArgs = new  String[] {
                "--webdriver-loglevel=NONE"
        };
        dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
        driver = getNewDriver(dCaps);
        driver.get(oracle);
        System.out.println(oracle);
        capturePageModel(oracle, widths, oDoms, sleep, false, true, driver, new StopwatchFactory(), new HashMap<Integer, LayoutFactory>());
//        System.out.println("O" + oDoms.size());
        driver.get(test);
        System.out.println(test);
        capturePageModel(test, widths, tDoms, sleep, false, false, driver, new StopwatchFactory(), new HashMap<Integer, LayoutFactory>());
//        System.out.println("T" + tDoms.size());
        driver.close();
        driver.quit();
    }

    /**
     * This method samples the DOM of a webpage at a set of viewports, and saves the DOMs into a HashMap
     * @param url        The url of the webpage
     * @param widths    The viewport widths to sample
     * @param doms        The HashMap to save the DOMs into.
     */
    public static void capturePageModel(String url, int[] widths, HashMap<Integer, DomNode> doms, int sleep, boolean takeScreenshot, boolean saveDom, WebDriver wdriver, StopwatchFactory swf, HashMap<Integer, LayoutFactory> lFactories) {
        // Create a parser for the DOM strings
//        if (!swf.getCapture().isStarted()) {
//            swf.getCapture().start();
//        } else if (swf.getCapture().isSuspended()) {
//            swf.getCapture().resume();
//        }
//        if (swf.getProcess().isStarted()) {
//            swf.getProcess().suspend();
//        }
        JsonDomParser parser = new JsonDomParser();
        File domFile=null;
        try {
            // Set up storage directory
            String outFolder = "";

            if (saveDom) {
                String[] splits = url.split("/");
                outFolder = redecheck + "output/" + splits[7] + "/" + splits[8];
                File dir = new File(outFolder);
                FileUtils.forceMkdir(dir);
            }

//            Toolkit toolkit = Toolkit.getDefaultToolkit();
//            int Width = (int) toolkit.getScreenSize().getWidth();
//            int Height = (int) toolkit.getScreenSize().getHeight();
//            System.out.print (Width + " x " + Height);

            // Iterate through all viewport widths
            for (int w : widths) {
//                System.out.println("\n" + w);
                // Check if DOM already saved for speed
                domFile = new File(outFolder + "/" + w + ".js");
                boolean consecutiveMatches = false;

//                wdriver.manage().window().setPosition(new Point(0,0));

                // Resize the browser window
                wdriver.manage().window().setSize(new Dimension(w, 1000));
                String previous = "";


                while (!consecutiveMatches) {
                    // Extract the DOM and save it to the HashMap.
//                    if (!swf.getSleep().isStarted()) {
//                        swf.getSleep().start();
//                    } else {
//                        swf.getSleep().resume();
//                    }
                    Thread.sleep(sleep);
//                    swf.getSleep().suspend();

                    String extractedDom = extractDOM(wdriver, scriptToExtract, swf.getExtract());

                    if (previous.equals(extractedDom)) {

                        lFactories.put(w, new LayoutFactory(extractedDom));
                        if (saveDom) {
                            FileUtils.writeStringToFile(domFile, extractedDom);
                        }
                        consecutiveMatches = true;
                    } else {

                        previous = extractedDom;
                    }


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        swf.getCapture().suspend();
//        if (swf.getProcess().isStarted()) {
//            swf.getProcess().resume();
//        }
    }

    public static String extractDOM(PhantomJSDriver pjsdriver, String script, StopWatch sw) throws IOException {

//        if (!sw.isStarted()) {
//            sw.start();
//        } else if (sw.isSuspended()) {
//            sw.resume();
//        }

        String result =  (String) pjsdriver.executeScript(script);
//        sw.suspend();
        return result;
    }

    public static String extractDOM(WebDriver cdriver, String script, StopWatch sw) throws IOException {

        if (!sw.isStarted()) {
            sw.start();
        } else if (sw.isSuspended()) {
            sw.resume();
        }

        String result =  (String) ((JavascriptExecutor) cdriver).executeScript(script);
        sw.suspend();
        return result;
    }


    /**
     * Loads the DOM of a given webpage at a specified set of resolutions into a Map for easy access
     * @param widths        the widths at which to load the DOM
     * @param url           the URL of the webpage
     * @return
     */
    public Map<Integer, DomNode> loadDoms(int[] widths, String url) {
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

//    /**
//     * Captures a screenshot of the webpage
//     * @param screenshot	File into which to save the image
//     * @param driver		The driver to use to get the screenshot
//     */
//    public static void captureScreenshot(File screenshot, PhantomJSDriver driver, int width) {
//        String ssDir = redecheck + "screenshots/" + mutantID + "/";
//
//        if (saveToExtras) {
//            ssDir += "extras/";
//        }
//        String ssFile = mutantID;
//        if (!screenshot.toString().contains("mutant")) {
//            ssFile += "-oracle-" + width;
//        } else {
//            ssFile += "-mutant-" + width;
//        }
//        System.out.println(ssFile);
//        File scrFile = driver.getScreenshotAs(OutputType.FILE);
//        try {
//            FileUtils.forceMkdir(new File(ssFile));
//            FileUtils.copyFile(scrFile, new File(ssDir + ssFile + ".png"));
//        } catch (IOException e) {
//            System.err.println("Error saving screenshot");
//            e.printStackTrace();
//        }
//    }

//    public static BufferedImage getScreenshot(String url, int w, int sleep, PhantomJSDriver d, int errorID) {
//        try {
//            d.manage().window().setSize(new Dimension(w, 1000));
//            Thread.sleep(sleep);
//
//            File scrFile = d.getScreenshotAs(OutputType.FILE);
//
//            String ssDir = redecheck + "reports/" + mutantID + "/";
//            String ssFile = "error"+errorID + "atWidth" + w;
//            try {
//                FileUtils.forceMkdir(new File(ssFile));
//                FileUtils.copyFile(scrFile, new File(ssDir + ssFile + ".png"));
//            } catch (IOException e) {
//                System.err.println("Error saving screenshot");
//                e.printStackTrace();
//            }
//
//
//            BufferedImage img = ImageIO.read(new File(ssDir+ssFile+".png"));
//            return img;
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException ie) {
//            System.out.println("INTERRUPTED");
//        }
//        return null;
//    }



    public static PhantomJSDriver getNewDriver(DesiredCapabilities dCaps) {
        return new PhantomJSDriver(dCaps);
    }


    public static int[] getBreakpoints(ArrayList<RuleMedia> mqueries) {
    	Pattern p = Pattern.compile("\\(([A-Za-z]*-width:[ ]+)([0-9]*[.]+[0-9]*)(em|px)\\)");
		TreeSet<Integer> bps = new TreeSet<Integer>();
		for (RuleMedia rm : mqueries) {
			List<MediaQuery> mqs = rm.getMediaQueries();
				for (MediaQuery mq : mqs) {
					for (MediaExpression me : mq.asList()) {
						try {
							if (me.toString().contains("width")) {
								String s = me.toString().trim();
								Matcher m = p.matcher(s);
								m.matches();
								double bp = Double.valueOf(m.group(2));
                                int bpFinal = 0;
                                if (m.group(3).equals("em")) {
                                    bpFinal = (int) (bp * 16);
                                } else if (m.group(3).equals("px")) {
                                    bpFinal = (int) bp;
                                }
                                if (bpFinal != 0) {
                                    bps.add(bpFinal);
                                    if (me.toString().contains("min")) {
                                        bps.add(bpFinal-1);
                                    } else if (me.toString().contains("max")) {
                                        bps.add(bpFinal+1);
                                    }
                                }
							}
						} catch (Exception e) {
						}
					}
				}
		}
		int[] extras = new int[bps.size()];
		Iterator iter = bps.iterator();
		int counter = 0;
		while(iter.hasNext()) {
			try {
				int i = (int) iter.next();
				extras[counter] = i;
				counter++;
			} catch (Exception e) {

			}
		}
		return extras;

	}

    @SuppressWarnings("unchecked")
	public static ArrayList<String> initialiseFiles(String url, WebDriver driver) {
//		JavascriptExecutor js = (JavascriptExecutor) driver;
		String script;
		try {
			script = Utils.readFile(new java.io.File( "." ).getCanonicalPath() +"/../resources/getCssFiles.js");
			return (ArrayList<String>) ((JavascriptExecutor) driver).executeScript(script);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}

    @SuppressWarnings({ "unused", "rawtypes" })
    public static ArrayList<RuleMedia> getMediaQueries(String base, ArrayList<String> cssFiles, String preamble) {
        ArrayList<RuleMedia> mqCandidates = new ArrayList<RuleMedia>();
    	URL cssUrl = null;
        URLConnection conn;
        String[] cssContent = new String[cssFiles.size()];
        int counter = 0;

        for (String cssFile : cssFiles) {
//            System.out.println(cssFile);
            String contents = "";
            try {
                if (cssFile.contains("http")) {
                    cssUrl = new URL(cssFile);
                } else if (cssFile.substring(0, 2).equals("//")) {
                    cssUrl = new URL("http:" + cssFile);
                } else {
//                    System.out.println("LOCAL");
                    cssUrl = new URL(("file://" + preamble + base + "/" + cssFile.replace("./", "")));
//                    cssUrl = new URL(("file://" + preamble + base.split("/")[0] + "/" + base.split("/")[1] + "/" + cssFile.replace("./", "")));
                }
//                System.out.println(cssUrl);

                conn = cssUrl.openConnection();

                BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));

//                long start = System.nanoTime();
                String inputLine;
                while ((inputLine = input.readLine()) != null) {
                    contents += inputLine;
                }
//                long end = System.nanoTime();
//                double duration = ((end - start) / 1000000000.0);
//                System.out.println(duration);
                contents += "\n\n";
                cssContent[counter] = contents;


            } catch (Exception e) {
//                e.printStackTrace();
//                System.out.println("Problem loading or layout the CSS file " + cssUrl.toString());
            }
            counter++;
        }

        StyleSheet ss = null;
        for (int i = 0; i < cssContent.length; i++) {
            String s = cssContent[i];
//            System.out.println(s);
            try {
                String prettified = s;
//                        CSSMutator.prettifyCss(s);
                StyleSheet temp = CSSFactory.parse(prettified);
//                System.out.println(temp);
                for (RuleBlock rb : temp.asList()) {
                    if (rb instanceof RuleMedia) {
                        RuleMedia rm = (RuleMedia) rb;
                        if (CSSMutator.hasNumericQuery(rm)) {

                            if (rm.asList().size() > 0) {
                                mqCandidates.add(rm);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CSSException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                System.out.println("Null pointer for some reason on " + i);
            }
        }
        return mqCandidates;
    }

    public static void writeToFile(String testUrl, String content, String fileName, String directory) {
//        System.out.println(fileName);
        PrintWriter output = null;
        String outFolder = "";
        try {
            String[] splits = testUrl.split("/");
            String webpage = splits[7];
            String mutant = splits[8];
            outFolder = directory + webpage + "/" + mutant + "/";

            File dir = new File(outFolder+fileName+".txt");
            System.out.println(dir.toString());
            // create multiple directories at one time
//            boolean successful = dir.mkdirs();
//            if (successful)
//            {
//                // created the directories successfully
////                System.out.println("directories were created successfully");
//            }
//            else
//            {
//                // something failed trying to create the directories
////                System.out.println("failed trying to create the directories");
//            }

//            int index = testUrl.lastIndexOf('/');
//            outFolder = testUrl.substring(0, index+1);
//            System.out.println(outFolder);
            output = new PrintWriter(dir);
            output.append(content);
            output.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void capturePageModel2(String url, int[] widths, HashMap<Integer, edu2.gatech.xpert.dom.DomNode> doms, int sleep, boolean takeScreenshot, PhantomJSDriver driver) {
        // Create a parser for the DOM strings
        edu2.gatech.xpert.dom.JsonDomParser parser = new edu2.gatech.xpert.dom.JsonDomParser();
        File domFile=null;
        System.out.println("GATHERING " + url);
        try {
            String outFolder = "";

//            if (url.contains("index")) {
                String[] splits = url.split("/");
                outFolder = redecheck + "output/" + splits[7] + "/" + splits[8];
                File dir = new File(outFolder);
                FileUtils.forceMkdir(dir);
//            }


            // Iterate through all viewport widths
            for (int w : widths) {
                domFile = new File(outFolder + "/" + w + ".js");
                if (domFile.exists()) {
////                    System.out.println("Reading");
                    String domStr = FileUtils.readFileToString(domFile);
                    doms.put(w, parser.parseJsonDom(domStr));
                } else {
//                    System.out.println("Writing");
                    boolean consecutiveMatches = false;
//                    System.out.println(w);
                    // Resize the browser window
                    driver.manage().window().setSize(new Dimension(w, 1000));
                    //                Thread.sleep(sleep);
                    String previous = "";

                    while (!consecutiveMatches) {
                        // Extract the DOM and save it to the HashMap.
                        String extractedDom = extractDOM(driver, scriptToExtract, new StopWatch());

                        if (previous.equals(extractedDom)) {
                            doms.put(w, parser.parseJsonDom(extractedDom));

//                            if (url.contains("index")) {
                                FileUtils.writeStringToFile(domFile, extractedDom);
//                            }
                            consecutiveMatches = true;
                        } else {
                            previous = extractedDom;
                            Thread.sleep(sleep);
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
