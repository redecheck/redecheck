package twalsh.mutation;

import com.rits.cloning.Cloner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by thomaswalsh on 05/10/2015.
 */
public class HTMLMutator {
    private final String baseURL;
    private final int numMutants;
    private final String shorthand;
    HashSet<String> usedClasses, usedTags, usedIds;
    ArrayList<String> mutatedFiles;
    Random random;
    Cloner cloner;
    Document page;
    ArrayList<String> cssFiles;
    String preamble = "file:///Users/thomaswalsh/Documents/Workspace/rlt-tool/xpert/testing/";
    String[] tagsIgnore = { "A", "AREA", "B", "BLOCKQUOTE",
            "BR", "CANVAS", "CENTER", "CSACTIONDICT", "CSSCRIPTDICT", "CUFON",
            "CUFONTEXT", "DD", "EM", "EMBED", "FIELDSET", "FONT", "FORM",
            "HEAD", "HR", "I", "LABEL", "LEGEND", "LINK", "MAP", "MENUMACHINE",
            "META", "NOFRAMES", "NOSCRIPT", "OBJECT", "OPTGROUP", "OPTION",
            "PARAM", "S", "SCRIPT", "SMALL", "SPAN", "STRIKE", "STRONG",
            "STYLE", "TBODY", "TITLE", "TR", "TT", "U" };
    private String htmlContent;

    public HTMLMutator(String baseUrl, String shortH, int numMutants) {
        this.baseURL = baseUrl;
        this.shorthand = shortH;
        usedClasses = new HashSet<String>();
        usedTags = new HashSet<String>();
        usedIds = new HashSet<String>();
        
        mutatedFiles = new ArrayList<String>();
        random = new Random();
        this.numMutants = numMutants;
        String[] splits = baseUrl.split("/");
//        strippedUrl = splits[splits.length-1];
        cloner = new Cloner();
        cssFiles = new ArrayList<String>();
        parseHTML(baseUrl);

    }

    public void parseHTML(String url) {
        String contents = "";
        try {
            BufferedReader input = new BufferedReader(new FileReader((preamble + url + "/index.html").replace("file:", "")));
            String inputLine;
            while ((inputLine = input.readLine()) != null) {
                contents += inputLine;
            }
            Document doc = Jsoup.parse(contents);
            page = doc;
            for (Element e : doc.getAllElements()) {
                if (!ignoreTag(e.tagName().toUpperCase())) {
                    if (e.classNames().size() > 0) {
                        for (String c : e.classNames()) {
                            usedClasses.add("." + c);
                        }
                    }
                    if (!e.id().equals("")) {
                        usedIds.add("#" + e.id());
                    }
                    usedTags.add(e.tagName());
                }
            }
            this.htmlContent = contents;
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("There was a problem parsing the HTML of the specified website :(");
        }
    }


    private boolean ignoreTag(String tagName) {
//		System.out.println(tagName);
        for (int i =0; i < tagsIgnore.length; i++) {

            if (tagsIgnore[i].equals(tagName)) {
//				System.out.println("TRUE");
                return true;
            }
        }
        return false;
    }

}
