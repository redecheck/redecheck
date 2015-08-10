package twalsh.redecheck;

import java.io.IOException;
import java.io.StringWriter;
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
}
