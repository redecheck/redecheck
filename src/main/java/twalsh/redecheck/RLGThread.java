package twalsh.redecheck;



import edu.gatech.xpert.dom.DomNode;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import twalsh.analysis.RLGAnalyser;
import twalsh.analysis.ResponsiveLayoutFailure;
import twalsh.layout.LayoutFactory;
import twalsh.rlg.ResponsiveLayoutGraph;
import twalsh.utils.StopwatchFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thomaswalsh on 15/02/2016.
 */
public class RLGThread implements Runnable {
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
    private boolean binarySearch;
    private int startW, endW, stepSize;
    private String preamble;
    private int sleep;
    private int[] sampleWidths;
    private int initialDoms;
    StopwatchFactory swf;
    private ArrayList<Integer> breakpoints;
    private String ts;



    public RLGThread(String current, String fullUrl, String shortUrl, HashMap<Integer, DomNode> doms, HashMap<Integer, LayoutFactory> facts, String b, String st, boolean bs, int start, int end, int ss, String preamble, int sleep, String timeStamp) throws IOException{
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
    }

    public void run() {
//        System.out.println("EXTRACTING RESPONSIVE LAYOUT GRAPH");
//        this.swf.getTotal().start();
        try {
//            this.swf.getSetup().start();
            this.swf.getRlg().start();
            if (browser.equals("chrome")) {
//                System.out.println("USING CHROME");
                DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                webDriver = new ChromeDriver(capabilities);
            } else if (browser.equals("firefox")) {
//                System.out.println("USING FIREFOX");
                DesiredCapabilities capabilities = DesiredCapabilities.firefox();
                capabilities.setCapability("marionette", true);
                webDriver = new FirefoxDriver(capabilities);
            } else if (browser.equals("phantom")) {
//                System.out.println("USING PHANTOMJS");
                DesiredCapabilities dCaps = new DesiredCapabilities();
                dCaps.setJavascriptEnabled(true);
                dCaps.setCapability("takesScreenshot", true);
                String[] phantomArgs = new String[]{"--webdriver-loglevel=NONE"};
                dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
                webDriver = Redecheck.getNewDriver(dCaps);
            }
            webDriver.get("file://" +fullUrl);
//            System.out.println(fullUrl);

//            this.swf.getSetup().stop();
            sampleWidths = Redecheck.getSampleWidths(sampleTechnique, shortUrl, webDriver, startW, endW, stepSize, preamble, breakpoints);
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
//            System.out.println(this.swf.getDetect());

            this.swf.getReport().start();
//            System.out.println("GATHERING SCREENSHOT EXAMPLES FOR FAULTS");
            HashMap<Integer, BufferedImage> imageMap = new HashMap<>();
            if (errors.size() > 0) {
                for (ResponsiveLayoutFailure error : errors) {
//                    System.out.println(error + "\n");
                    error.captureScreenshotExample(errors.indexOf(error)+1, shortUrl, webDriver, fullUrl, imageMap, ts);
                }
            } else {
//                System.out.println("No layout errors found!");
            }
            analyser.writeReport(shortUrl, errors, ts);
//            System.out.println(errors.size() + " ERRORS FOUND");
            this.swf.getReport().stop();

//            LogEntries logs = webDriver.manage().logs().get("browser");
//            for (LogEntry entry : logs) {
//                System.out.println(entry.getMessage());
//                //do something useful with the data
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            webDriver.quit();
        }
//        if (this.swf != null) {
//            this.swf.getTotal().stop();
//        }

//        System.out.println(this.swf.getRlg());
//        System.out.println(this.swf.getDetect());
//        System.out.println(this.swf.getReport());
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
}
