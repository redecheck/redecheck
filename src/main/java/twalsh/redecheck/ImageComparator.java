package twalsh.redecheck;

import magick.ImageInfo;
import magick.MagickException;
import magick.MagickImage;
import org.im4java.core.CompareCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by thomaswalsh on 14/10/2015.
 */
public class ImageComparator {
    String oracle, test;
    ImageInfo origInfo, testInfo;
    MagickImage origImage, testImage;
    static String current;

    public ImageComparator(String o, String t) {
        oracle = o;
        test = t;
//        try {
//            origInfo = new ImageInfo(o);
//            testInfo = new ImageInfo(t);
//            origImage = new MagickImage(origInfo);
//            testImage = new MagickImage(testInfo);
//        } catch (MagickException e) {
//            e.printStackTrace();
//        }



    }

    public int compare() {
        String s = null;
        int result = 0;
        try {
            Process p = Runtime.getRuntime().exec("compare -metric ae " +this.oracle + " " +this.test + " null:");

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

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
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        current = new java.io.File( "." ).getCanonicalPath();
        String desktopDir = current + "/../../../Desktop/";
        ImageComparator ic = new ImageComparator(desktopDir+"screenshot.png", desktopDir+"screenshot2.png");

        String s = null;

        try {
            Process p = Runtime.getRuntime().exec("compare -metric ae " +ic.oracle + " " +ic.test + " null:");

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

            System.exit(0);
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
