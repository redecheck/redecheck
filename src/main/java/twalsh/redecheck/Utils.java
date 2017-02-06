package twalsh.redecheck;

import java.io.IOException;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

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
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } catch (IOException e) {

        } finally {
            br.close();
        }
        return "";
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


}
