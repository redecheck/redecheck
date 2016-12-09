package shef.redecheck;


import com.beust.jcommander.JCommander;
import edu.gatech.xpert.dom.DomNode;
import edu.gatech.xpert.dom.JsonDomParser;
import edu2.gatech.xpert.dom.layout.AGDiff;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import shef.layout.LayoutFactory;
import shef.mutation.ResultClassifier;
import shef.rlg.ResponsiveLayoutGraph;
import shef.utils.ResultProcessor;
import shef.utils.StopwatchFactory;

import java.io.*;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

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
    public String sampleTechnique = "uniformBP";
    public boolean binarySearch = true;
    public boolean timing;
    public int timingID;
    public String browser = "phantom";
    public String mutantID;
    public boolean screenshot;
    public boolean baselines, results;
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
    static String redecheck = "/Users/thomaswalsh/Documents/PhD/Code-Projects/Redecheck/";
    static String reportDirectory = "/Users/thomaswalsh/Documents/PhD/Code-Projects/Redecheck/reports/";
    static String timesDirectory = "/Users/thomaswalsh/Documents/PhD/Code-Projects/Redecheck/times/";
    static String dataDirectory = "/Users/thomaswalsh/Documents/PhD/Papers/redecheck-journal-paper-data/";
    static int[] manualWidths = {480, 600, 640, 768, 1024, 1280};
    static int sleep = 50;
    public CommandLineParser clp = new CommandLineParser();


    public Redecheck(String[] args) throws IOException, InterruptedException {
        current = new java.io.File( "." ).getCanonicalPath();
        System.setProperty("phantomjs.binary.path", current + "/../resources/phantomjs");
        System.setProperty("webdriver.chrome.driver", current + "/../resources/chromedriver");
        System.setProperty("webdriver.opera.driver", current + "/../resources/operadriver");
        System.setProperty("webdriver.gecko.driver", current + "/../resources/geckodriver");
        
        clArgs = args;
        new JCommander(clp, clArgs);
        oracle = clp.oracle;
        test = clp.test;
        preamble = clp.preamble;
        
        startWidth = clp.startWidth;
        finalWidth = clp.endWidth;
        if (clp.ss != -1) {
        	stepSize = clp.ss;
        }
        sampleTechnique = clp.sampling;
        binarySearch = clp.binary;
        mutantID = clp.mutantID;
        screenshot = clp.screenshot;
        baselines = clp.baselines;
        results = clp.results;
        tool = clp.tool;
        xpert = clp.xpert;
        if (clp.browser != null) {
            browser = clp.browser;
        }
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

        if (results) {
            ResultProcessor rp = new ResultProcessor();
        }
    }

    private void runFaultDetector() {
        try {
            Date date = new Date();
            Format formatter = new SimpleDateFormat("YYYY-MM-dd_hh-mm-ss");
            String timeStamp = formatter.format(date);
            scriptToExtract = Utils.readFile(current +"/../resources/webdiff2.js");
            String fullUrl;
            if (preamble != null) {
                fullUrl = "file://" + preamble + url + "/index.html";
            } else {
                fullUrl = url;
            }
            System.out.println(fullUrl);
            RLGExtractor thread = new RLGExtractor(current, fullUrl, url, oracleDoms, layoutFactories, browser, sampleTechnique, binarySearch, startWidth, finalWidth, stepSize, preamble, sleep, timeStamp, baselines);
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
        RLGExtractor rlg1 = new RLGExtractor(current, testUrl, test, testDoms, tFactories, browser, sampleTechnique, binarySearch, startWidth, finalWidth, stepSize, preamble, sleep, timeStamp, baselines);
//        RLGExtractor rlg2 = new RLGExtractor(current, testUrl, test, testDoms, tFactories, "phantom");
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

    public static String getTimeStringFromStopwatch(StopWatch sw) {
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

            // Iterate through all viewport widths
            for (int w : widths) {
                // Check if DOM already saved for speed
                domFile = new File(outFolder + "/" + w + ".js");
                boolean consecutiveMatches = false;

                // Resize the browser window
                wdriver.manage().window().setSize(new Dimension(w, 1000));
                String previous = "";


                while (!consecutiveMatches) {
                    // Extract the DOM and save it to the HashMap.
                    Thread.sleep(sleep);
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

//    public static BufferedImage getScreenshot(String url, int w, int sleep, WebDriver d, int errorID) {
//        try {
//            d.manage().window().setSize(new Dimension(w, 1000));
//            Thread.sleep(sleep);
//
//            File scrFile = d.getScreenshotAs(OutputType.FILE);
//
//            String ssDir = redecheck + "reports/" + url + "/";
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




    public static void writeToFile(String testUrl, String content, String fileName, String directory) {
//        System.out.println(fileName);
        PrintWriter output = null;
        String outFolder = "";
        try {
            String[] splits = testUrl.split("/");
            String webpage = splits[8];
//            String mutant = splits[10];
            outFolder = directory + webpage + "/";

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

    /**
     * Main method to handle execution of the whole tool
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        Redecheck redecheck = new Redecheck(args);
    }
}
