package shef.redecheck;



import cz.vutbr.web.css.*;
import edu.gatech.xpert.dom.DomNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import shef.analysis.RLGAnalyser;
import shef.mutation.CSSMutator;
import shef.reporting.inconsistencies.ResponsiveLayoutFailure;
import shef.layout.LayoutFactory;
import shef.rlg.ResponsiveLayoutGraph;
import shef.utils.ResultProcessor;
import shef.utils.StopwatchFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by thomaswalsh on 15/02/2016.
 */
public class RLGExtractor implements Runnable {
    public ResponsiveLayoutGraph rlg;
    public WebDriver webDriver;
    public String browser;
    static String scriptToExtract;

    private String current;
    private String fullUrl;
    private String shortUrl;
    private HashMap<Integer, DomNode> doms;
    private HashMap<Integer, LayoutFactory> lFactories;
    private String sampleTechnique;
    private boolean binarySearch, baselines;
    private int startW, endW, stepSize;
    private String preamble;
    private int sleep;
    private int[] sampleWidths;
    private int initialDoms;
    StopwatchFactory swf;
    private ArrayList<Integer> breakpoints;
    private String ts;
    final int[] SPOT_CHECK_WIDTHS = new int[] {480, 600, 640, 768, 1024, 1280};
    int[] allWidths;



    public RLGExtractor(String current, String fullUrl, String shortUrl, HashMap<Integer, DomNode> doms, HashMap<Integer, LayoutFactory> facts, String b, String st, boolean bs, int start, int end, int ss, String preamble, int sleep, String timeStamp, boolean baselines) throws IOException{
        this.current = current;
        this.fullUrl = fullUrl;
        this.shortUrl = shortUrl;
        this.doms = doms;
        this.lFactories = facts;
        this.browser = b;
        this.sampleTechnique = st;
        this.binarySearch = bs;
        this.startW = start;
        this.endW = end;
        this.stepSize = ss;
        this.preamble = preamble;
        swf = new StopwatchFactory();
        this.sleep = sleep;
        breakpoints = new ArrayList<>();
        ts = timeStamp;
        this.baselines = baselines;
    }

    public void run() {
        try {
            this.swf.getRlg().start();
            if (browser.equals("chrome")) {
                DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                webDriver = new ChromeDriver(capabilities);
            } else if (browser.equals("firefox")) {
                DesiredCapabilities capabilities = DesiredCapabilities.firefox();
                capabilities.setCapability("marionette", true);
//                capabilities.setVersion("47.0.1");
                webDriver = new FirefoxDriver();
            } else if (browser.equals("phantom")) { 
                DesiredCapabilities dCaps = new DesiredCapabilities();
                dCaps.setJavascriptEnabled(true);
                dCaps.setCapability("takesScreenshot", true);
                dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, current + "/../resources/phantomjs");
                String[] phantomArgs = new String[]{"--webdriver-loglevel=NONE"};
                dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
                webDriver = new PhantomJSDriver(dCaps);
            } else if (browser.equals("opera")) {
                DesiredCapabilities capabilities = DesiredCapabilities.operaBlink();
                capabilities.setJavascriptEnabled(true);
                webDriver = new OperaDriver(capabilities);
            }
            webDriver.get(fullUrl);
            sampleWidths = calculateSampleWidths(sampleTechnique, shortUrl, webDriver, startW, endW, stepSize, preamble, breakpoints);
            initialDoms = sampleWidths.length;
            Redecheck.capturePageModel(fullUrl, sampleWidths, doms, sleep, false, false, webDriver, swf, lFactories);
//            swf.getProcess().start();
            ArrayList<LayoutFactory> oracleLFs = new ArrayList<>();
            for (int width : sampleWidths) {
                LayoutFactory lf = lFactories.get(width);
                oracleLFs.add(lf);
            }

            this.rlg = new ResponsiveLayoutGraph(oracleLFs, sampleWidths, fullUrl, lFactories, binarySearch, webDriver, swf, sleep);
            this.swf.getRlg().stop();
            this.swf.getDetect().start();
            RLGAnalyser analyser = new RLGAnalyser(this.getRlg(), webDriver, fullUrl, breakpoints, lFactories, startW, endW);
            ArrayList<ResponsiveLayoutFailure> errors = analyser.analyse();
            this.swf.getDetect().stop();

            this.swf.getReport().start();
//            HashMap<Integer, BufferedImage> imageMap = new HashMap<>();
//            if (errors.size() > 0) {
//                for (ResponsiveLayoutFailure error : errors) {
//                    error.captureScreenshotExample(errors.indexOf(error)+1, shortUrl, webDriver, fullUrl, imageMap, ts);
//                }
//            }
//            analyser.writeReport(shortUrl, errors, ts);
            this.swf.getReport().stop();

            // BASELINE SCREENSHOT CAPTURE
            if (baselines) {
                File spotcheckDir, exhaustiveDir;
                if (!shortUrl.contains("www")) {
                    String[] splits = shortUrl.split("/");
                    String webpage = splits[0];
                    String mutant = "index";
                    spotcheckDir = new File(Redecheck.redecheck + "/screenshots/" + webpage + "/spotcheck/");
                    exhaustiveDir = new File(Redecheck.redecheck + "/screenshots/" + webpage + "/exhaustive/");
                } else {
                    String[] splits = shortUrl.split("www.");
                    spotcheckDir = new File(Redecheck.redecheck + "/screenshots/" + splits[1] + "/spotcheck/");
                    exhaustiveDir = new File(Redecheck.redecheck + "/screenshots/" + splits[1] + "/exhaustive/");
                }
                FileUtils.forceMkdir(spotcheckDir);
                FileUtils.forceMkdir(exhaustiveDir);
                for (int scw : SPOT_CHECK_WIDTHS) {
                    BufferedImage ss = Utils.getScreenshot(shortUrl, scw, sleep*2, webDriver, scw);
                    File outputfile = new File(spotcheckDir + "/" + scw + ".png");
                    ImageIO.write(ss, "png", outputfile);
                }
                allWidths = new int[(endW - startW) + 1];
                for (int i = 0; i < allWidths.length; i++) {
                    allWidths[i] = i + startW;
                }

                // Obtain failures from classification files
                ResultProcessor rp = new ResultProcessor();
                String path = new java.io.File( "." ).getCanonicalPath() + "/../reports-final/" + shortUrl;
                File mostRecentRun = rp.lastFileModified(path);
                System.out.println(mostRecentRun);

                ArrayList<Integer> tpIndexes = new ArrayList<>();

                String classificationString = rp.getClassification(mostRecentRun, tpIndexes);
//				int distinctRanges = getFailuresFromFile(mostRecentRun, tpIndexes, true);
                HashMap<String, String> failures = rp.getFailuresFromFile(mostRecentRun, tpIndexes, true);
                for (String failure : failures.keySet()) {
                    String bounds = failures.get(failure);
                    String[] splits = bounds.split(" - ");
                    int fMin = Integer.parseInt(splits[0]);
                    int fMax = Integer.parseInt(splits[1]);
                    boolean detectedBySpotCheck = false;
                    for (int scw : SPOT_CHECK_WIDTHS) {
                        if ( (scw >= fMin) && (scw <= fMax) ) {
                            detectedBySpotCheck = true;
                        }
                    }
                    System.out.println(failure + " " + detectedBySpotCheck);
                }

//                StopWatch exhaustive = new StopWatch();
//                exhaustive.start();
//                for (int scw : allWidths) {
//                    BufferedImage ss = Utils.getScreenshot(shortUrl, scw, sleep, webDriver, scw);
//                    File outputfile = new File(exhaustiveDir + "/" + scw + ".png");
//                    ImageIO.write(ss, "png", outputfile);
//                }
//                exhaustive.stop();
//                String exhaustiveTime = Redecheck.getTimeStringFromStopwatch(exhaustive);
//                Redecheck.writeToFile(fullUrl, exhaustiveTime, "exhaustive-time", Redecheck.timesDirectory);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (webDriver != null) {
            webDriver.quit();
        }

    }


    public ResponsiveLayoutGraph getRlg() {
        return this.rlg;
    }

    public static BufferedImage getScreenshot(int captureWidth, int errorID, HashMap<Integer, LayoutFactory> lfs, WebDriver d, String fullUrl) {
        Redecheck.capturePageModel(fullUrl, new int[] {captureWidth}, new HashMap<Integer, DomNode>(), Redecheck.sleep, false, false, d, new StopwatchFactory(), lfs);
        return Utils.getScreenshot(fullUrl,captureWidth, Redecheck.sleep, d, errorID);
    }

    public StopwatchFactory getSwf() {
        return swf;
    }

    public int getInitialDoms() {
        return initialDoms;
    }

    public int[] getSampleWidths() {
        return sampleWidths;
    }

    public static int[] calculateSampleWidths(String technique, String shortUrl, WebDriver drive, int startWidth, int finalWidth, int stepSize, String preamble, ArrayList<Integer> breakpoints) {
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
}
