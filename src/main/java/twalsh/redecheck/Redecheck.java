package twalsh.redecheck;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Dimension;

import java.io.File;
import org.apache.commons.io.FileUtils;
import java.io.IOException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Redecheck {
    public static String OUTPUT = "./output/";
    public static int SLEEP_TIME = 250;
    public static String url;
    static String baseUrl = "file:///Users/thomaswalsh/Documents/Workspace/Redecheck/testing/";


    public static void main(String[] args) {
//        String oracle = args[0];
//        String test = args[1];
        System.out.println("Hello World");
//        System.out.println(test);
    }

    public static void capturePageModel(String url, String[] widths) throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        try {
            driver.get(url);
            Thread.sleep(2500);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String dropdownScript = Utils.getPkgFileContents(Redecheck.class, "dropdown.js");
//			js.executeScript(dropdownScript);
            Thread.sleep(1000);

            int counter = 0;
            for (int i = 0; i < widths.length; i++) {
                String outFolder;
                int w = Integer.parseInt(widths[i]);
                outFolder = OUTPUT + url.replaceAll("/", "") + "/" + "width" + w;
                if (new File(outFolder + "/dom.js").exists() == false) {
                    driver.manage().window().setSize(new Dimension(w, 600));
                    Thread.sleep(SLEEP_TIME);

                    FileUtils.forceMkdir(new File(outFolder));
                    FileUtils.writeStringToFile(new File(outFolder + "/dom.js"), extractDOM(url, driver, counter));
                }
                counter++;
            }
            driver.quit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    public static String extractDOM(String url, WebDriver driver, int c) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String script = Utils.getPkgFileContents(Redecheck.class,"webdiff2.js");

//        if (c == 0) {
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                System.out.println("Failed to sleep");
//            }
//        }
        String domStr = (String) js.executeScript(script);
        return domStr;
    }
}
