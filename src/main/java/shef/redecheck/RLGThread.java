package shef.redecheck;



import edu.gatech.xpert.dom.DomNode;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.MarionetteDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import shef.analysis.RLGAnalyser;
import shef.reporting.failures.ResponsiveLayoutFailure;
import shef.layout.LayoutFactory;
import shef.rlg.ResponsiveLayoutGraph;
import shef.utils.StopwatchFactory;

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
            if (fullUrl.contains("www.") == false) {
            	webDriver.get("file://" + fullUrl);
            } else {
            	webDriver.get(fullUrl);
            }
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

            this.swf.getReport().start();
            HashMap<Integer, BufferedImage> imageMap = new HashMap<>();
            if (errors.size() > 0) {
                for (ResponsiveLayoutFailure error : errors) {
                    error.captureScreenshotExample(errors.indexOf(error)+1, shortUrl, webDriver, fullUrl, imageMap, ts);
                }
            }
            analyser.writeReport(shortUrl, errors, ts);
            this.swf.getReport().stop();

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (webDriver != null) {
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
