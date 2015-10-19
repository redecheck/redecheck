package twalsh.mutation;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

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


    public ImageComparator(String o, String t, int[] widths) throws IOException, InterruptedException {
        System.setProperty("webdriver.chrome.driver",
                "/Users/thomaswalsh/Downloads/chromedriver");
        current = new java.io.File( "." ).getCanonicalPath();
        String outputDir = current + "/output/";
        preamble = current + "/testing/";
        oracle = o;
        test = t;
        widthsToCompare = widths;
        captureScreenshots(oracle, widthsToCompare);
        captureScreenshots(test, widthsToCompare);
        for (int w : widthsToCompare) {
            String oracleFP = outputDir + oracle.replaceAll("/", "") + "/" + "width" + w + "/screenshot.png";
            System.out.println(oracleFP);
            String testFP = outputDir + test.replaceAll("/", "") + "/" + "width" + w + "/screenshot.png";
            System.out.println(compare(oracleFP, testFP));
        }
    }

    public int compare(String oracle, String test) {
        String s = null;
        int result = 0;
        try {
            Process p = Runtime.getRuntime().exec("compare -metric ae " + oracle + " " + test + " null:");

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read the output from the command
//            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // read any errors from the attempted command
//            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
                result = Integer.valueOf(s);
            }

//            System.exit(0);
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
        ChromeDriver driver = new ChromeDriver();
        driver.get("file:///" + preamble + url + ".html");
        Thread.sleep(500);
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
                Thread.sleep(250);

                captureScreenshot(new File(outFolder + "/screenshot.png"), driver);

                counter++;
            }
        driver.quit();
//        }
    }

    public static void captureScreenshot(File screenshot, ChromeDriver driver) {
        File scrFile = driver.getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(scrFile, screenshot);
        } catch (IOException e) {
            System.err.println("Error saving screenshot");
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        int[] ws = new int[] {400,500,600,700,800,900,1000};
        ImageComparator ic = new ImageComparator("rebeccamade.com/index", "rebeccamade.com/0", ws);

    }
}
