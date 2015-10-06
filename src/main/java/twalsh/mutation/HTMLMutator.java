package twalsh.mutation;

import com.rits.cloning.Cloner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    String preamble = "file:///Users/thomaswalsh/Documents/Workspace/Redecheck/testing/";
    String[] tagsIgnore = { "A", "AREA", "B", "BLOCKQUOTE",
            "BR", "CANVAS", "CENTER", "CSACTIONDICT", "CSSCRIPTDICT", "CUFON",
            "CUFONTEXT", "DD", "EM", "EMBED", "FIELDSET", "FONT", "FORM",
            "HEAD", "HR", "I", "LABEL", "LEGEND", "LINK", "MAP", "MENUMACHINE",
            "META", "NOFRAMES", "NOSCRIPT", "OBJECT", "OPTGROUP", "OPTION",
            "PARAM", "S", "SCRIPT", "SMALL", "SPAN", "STRIKE", "STRONG",
            "STYLE", "TBODY", "TITLE", "TR", "TT", "U" };
    private String htmlContent;
    Pattern bsGridPattern = Pattern.compile("col-[a-z]*-[0-9]*");
    Pattern fGridPattern = Pattern.compile("([a-z]*-)*[0-9]");
    Matcher m, m2;
    ArrayList<Element> candidates;
    Random r;

    public HTMLMutator(String baseUrl, String shortH, int numMutants) {
        this.baseURL = baseUrl;
        this.shorthand = shortH;
        usedClasses = new HashSet<>();
        usedTags = new HashSet<>();
        usedIds = new HashSet<>();
        
        mutatedFiles = new ArrayList<>();
        candidates = new ArrayList<>();
        random = new Random();
        this.numMutants = numMutants;
        cloner = new Cloner();
        r = new Random();
        parseHTML(baseUrl);
    }

    public static void main(String[] args) {
        HTMLMutator mutator = new HTMLMutator("demo.com/index.html", "demo.com", 1);
//        System.out.println(mutator.page);
//        System.out.println(mutator.candidates);
        mutator.mutate();
//        System.out.println(mutator.page);
    }

    public void parseHTML(String url) {
        String contents = "";
        try {
            BufferedReader input = new BufferedReader(new FileReader((preamble + url).replace("file:", "")));
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
                            if (isGridSizingClass(c)) {
                                usedClasses.add(c);
                                candidates.add(e);
                            }
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
        for (int i =0; i < tagsIgnore.length; i++) {

            if (tagsIgnore[i].equals(tagName)) {
                return true;
            }
        }
        return false;
    }


    private boolean isGridSizingClass(String htmlClass) {
        m = bsGridPattern.matcher(htmlClass);
        m2 = fGridPattern.matcher(htmlClass);
        return m.matches() | m2.matches();
    }

    private ArrayList<String> getMutatableClasses(Element e) {
        ArrayList<String> classes = new ArrayList<>();
        for (String s : e.classNames()) {
            if (isGridSizingClass(s)) {
                classes.add(s);
            }
        }
        return classes;
    }



    private void mutate() {
        Collections.shuffle(candidates);
        Element selection = candidates.get(0);
        System.out.println("Mutation algorithm has chosen : " + selection.tagName());
        ArrayList<String> options = getMutatableClasses(selection);
        System.out.println("Mutatable classes are : " + options);
        Collections.shuffle(options);
        String classSelection = options.get(0);
        System.out.println("Chosen string is " + classSelection);

        m = bsGridPattern.matcher(classSelection);
        m2 = fGridPattern.matcher(classSelection);

        // Check if its a bootstrap one
        if (m.matches()) {
            String[] splits = classSelection.split("-");
            String range = splits[1];
            int num = Integer.valueOf(splits[2]);
            System.out.println(range);
            System.out.println(num);
            boolean toggle = r.nextBoolean();
            if (toggle) {
                // Mutate range
            } else {
                // Mutate number
            }
        }
    }

}
