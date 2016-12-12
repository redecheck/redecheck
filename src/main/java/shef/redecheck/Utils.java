package shef.redecheck;

import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.screentaker.ViewportPastingStrategy;

import javax.imageio.ImageIO;

/**
 * Created by thomaswalsh on 10/08/15.
 */
public class Utils {
    public static String getPkgFileContents(Class cls, String pkgFileName) {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(cls.getResourceAsStream(pkgFileName), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    /**
     * Utility function which reads the contents of a file into a String variable
     * @param fileName      the file name to read
     * @return              a string containing the files contents.
     * @throws IOException
     */
    public static String readFile(String fileName) throws IOException {
        try {
        	BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
        	return "";
        } finally {
            
        }
    }

    /**
     * Check whether all values in an array are equal
     * @param values        the array of values to check
     * @return              whether the values are all equal
     */
    public static boolean areAllItemsSame(double[] values) {
        double first = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] != first) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("")
    public static BufferedImage getScreenshot(String url, int w, int sleep, WebDriver d, int errorID) {
        try {
            d.manage().window().setSize(new Dimension(w, 1000));
//            System.out.println(w);
            Thread.sleep(sleep);
            Screenshot screenshot = null;
            File src = null;
            if (d instanceof ChromeDriver) {
                screenshot = new AShot().shootingStrategy(
                        new ViewportPastingStrategy(500)).takeScreenshot(d);
            } else {
                screenshot = new AShot().takeScreenshot(d);
//                src= ((TakesScreenshot)d). getScreenshotAs(OutputType.FILE);
            }
            String ssDir = Redecheck.redecheck + "target/";
            String ssFile = "error"+errorID + "atWidth" + w;
            try {
                FileUtils.forceMkdir(new File(ssFile));
                BufferedImage image;
//                if (d instanceof ChromeDriver) {
                    image= screenshot.getImage();
//                } else {
//                    System.out.println("Reading image from " + src);
//                    image = ImageIO.read(src);
//                }
                return image;
//                return image;
//                ImageIO.write(image, "PNG", new File(ssDir + ssFile + ".png"));
//                FileUtils.copyFile(scrFile, new File(ssDir + ssFile + ".png"));
            } catch (IOException e) {
                System.err.println("Error saving screenshot");
                e.printStackTrace();
            }


//            BufferedImage img = ImageIO.read(new File(ssDir+ssFile+".png"));
//            FileUtils.forceDelete(new File(ssDir + ssFile + ".png"));
//            return img;
        } catch (InterruptedException ie) {
            System.out.println("INTERRUPTED");
        }
//        } catch (IOException e) {
//            System.out.println("Failed to read image");
//        }
        return null;
    }

}
