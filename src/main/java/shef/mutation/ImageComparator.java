package shef.mutation;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by thomaswalsh on 14/10/2015.
 */
public class ImageComparator {
    String oracle, test;
    int[] widthsToCompare;
    static String current;
    static String preamble;
    double cumulativeDiffScore;
    final int NUMBER_OF_PIXELS = 722500;


    public ImageComparator(String o, String t, int low, int high) throws IOException, InterruptedException {
        System.setProperty("webdriver.chrome.driver",
                "/Users/thomaswalsh/Downloads/chromedriver");
        current = new java.io.File( "." ).getCanonicalPath();
        String outputDir = current + "/output/";
        preamble = current + "/testing/";
        oracle = o;
        test = t;
        cumulativeDiffScore = 0;
        widthsToCompare = new int[(high-low)+1];
        for (int i = 0; i < widthsToCompare.length; i++) {
        	widthsToCompare[i] = low + i;
        }
        captureScreenshots(oracle, widthsToCompare);
        captureScreenshots(test, widthsToCompare);
        for (int w : widthsToCompare) {
            String oracleFP = outputDir + oracle.replaceAll("/", "") + "/" + "width" + w + "/screenshot.png";
            String testFP = outputDir + test.replaceAll("/", "") + "/" + "width" + w + "/screenshot.png";            
            int numDiffPixels = compare(oracleFP, testFP);
            double diffPercentage =  ((numDiffPixels * 1.0) / NUMBER_OF_PIXELS);
            cumulativeDiffScore += diffPercentage;
        }
        System.out.println(cumulativeDiffScore);
        System.out.println(cumulativeDiffScore / widthsToCompare.length);
    }

    public int compare(String oracle, String test) {
        String s = null;
        int result = 0;
        try {
        	// Resize the images for a direct comparison
        	Runtime runtime = Runtime.getRuntime();
//        	runtime.exec("/usr/local/bin/convert " + oracle + " -resize 850x850! oracle.png");
//        	runtime.exec("/usr/local/bin/convert " + test + " -resize 850x850! test.png");
        	
//        	Process p = runtime.exec("/usr/local/bin/compare -metric ae oracle.png test.png null:");
            Process p = Runtime.getRuntime().exec("/usr/local/bin/compare -metric ae " + oracle + " " + test + " null:");

            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));


            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                result = Integer.valueOf(s);
            }

        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        } catch (NumberFormatException e) {
            System.out.println("Width/height of two images differs");
        }
        return result;
    }

    private void captureScreenshots(String url, int[] widths) throws InterruptedException {
    	DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", true);
        String[] phantomArgs = new  String[] {
        	    "--webdriver-loglevel=NONE"
        	};
        dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
    	PhantomJSDriver driver = new PhantomJSDriver(dCaps);
        driver.get("file:///" + preamble + url + ".html");
        Thread.sleep(1500);
//        try {
        int counter = 0;
        for (int i = 0; i < widths.length; i++) {

            String outFolder;
            int w = widths[i];
            outFolder = current + "/output/" + url.replaceAll("/", "") + "/" + "width" + w;
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
//            Thread.sleep(500);

            captureScreenshot(new File(outFolder + "/screenshot.png"), driver);

            counter++;
        }
        driver.quit();
//        }
    }

    public static void captureScreenshot(File screenshot, PhantomJSDriver driver) {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(scrFile, screenshot);
            Runtime.getRuntime().exec("/usr/local/bin/convert " + screenshot + " -resize 850x850! " + screenshot);
        } catch (IOException e) {
            System.err.println("Error saving screenshot");
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
    	
    	current = new java.io.File( "." ).getCanonicalPath();
        System.setProperty("phantomjs.binary.path", current + "/resources/phantomjs");
        ImageComparator ic = new ImageComparator("demo.com/index", "demo.com/mutant0", 400, 1400);

    }
}
