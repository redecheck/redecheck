package twalsh.mutation;

import com.rits.cloning.Cloner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
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
        for (int i = 1; i <= mutator.numMutants; i++) {
            Document copy = mutator.cloner.deepClone(mutator.page);
            mutator.mutate(copy);
            mutator.writeNewHtml(i, copy);
        }
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

    private Element getElementToMutate(Document copy, Element e) {
        System.out.println("Element to match: \n" + e.cssSelector());
        for (Element e2 : copy.getAllElements()) {
            if (e.cssSelector().equals(e2.cssSelector())) {
                return e2;
            }
        }
        return null;
    }

    private void mutate(Document toMutate) {
        boolean managedToMutate = false;
        while(!managedToMutate) {
            Collections.shuffle(candidates);
            Element selection = candidates.get(0);
            Element actualElement = getElementToMutate(toMutate, selection);
            if (actualElement != null) {
                ArrayList<String> options = getMutatableClasses(selection);
                System.out.println(actualElement.classNames());
                Collections.shuffle(options);
                String classSelection = options.get(0);

                m = bsGridPattern.matcher(classSelection);
                m2 = fGridPattern.matcher(classSelection);
                String[] bsRanges = new String[]{"xs", "sm", "md", "lg"};
                String[] fRanges = new String[]{"small", "medium", "large"};
                boolean changeOrAdd = r.nextBoolean();
                if (changeOrAdd) {
                    // We're mutating an existing class

                    // Check if its a bootstrap one
                    if (m.matches()) {
                        String[] splits = classSelection.split("-");
                        String range = splits[1];
                        int num = Integer.valueOf(splits[2]);
                        boolean toggle = r.nextBoolean();
                        if (toggle) {
                            // Mutate range
                            //                    System.out.println("Mutating range");
                            int index = r.nextInt(bsRanges.length);
                            range = bsRanges[index];
                        } else {
                            // Mutate number
                            //                    System.out.println("Mutating number");
                            boolean plusMinus = r.nextBoolean();
                            if (plusMinus)
                                num++;
                            else
                                num--;
                        }
                        String newClass = "col-" + range + "-" + num;
                        actualElement.removeClass(classSelection);
                        actualElement.addClass(newClass);
                        managedToMutate = true;
                    } else if (m2.matches()) {
                        String[] splits = classSelection.split("-");
                        String range = splits[0];
                        int num = Integer.valueOf(splits[1]);
                        boolean toggle = r.nextBoolean();
                        if (toggle) {
                            // Mutate range
                            System.out.println("Mutating range");
                            int index = r.nextInt(fRanges.length);
                            range = bsRanges[index];
                        } else {
                            // Mutate number
                            System.out.println("Mutating number");
                            boolean plusMinus = r.nextBoolean();
                            if (plusMinus)
                                num++;
                            else
                                num--;
                        }
                        String newClass = range + "-" + num;
                        actualElement.removeClass(classSelection);
                        actualElement.addClass(newClass);
                        managedToMutate = true;
                    }
                } else {
                    // We're adding a new class
                    int index = r.nextInt(4);
                    String newClassRange = bsRanges[index];
                    int newClassNumber = r.nextInt(12) + 1;
                    String newClass = "col-" + newClassRange + "-" + newClassNumber;
                    selection.addClass(newClass);
                    managedToMutate = true;
                }
            }
            System.out.println(actualElement.classNames());
        }
    }

    public void writeNewHtml(int i, Document mutated) {

        PrintWriter output = null;
        try {
            String dirName = preamble.replace("file:///", "/") + shorthand + "/";
            String fileName = i + ".html";

            File file = new File (dirName + fileName);
            output = new PrintWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        output.append(mutated.toString());
        output.close();
    }

}
