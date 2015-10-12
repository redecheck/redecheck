package twalsh.mutation;

import com.rits.cloning.Cloner;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.*;
import cz.vutbr.web.css.RuleBlock.Priority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import twalsh.redecheck.Redecheck;
import twalsh.redecheck.Utils;

/**
 * Created by thomaswalsh on 05/10/2015.
 */
public class CSSMutator {
    String baseURL;
    ArrayList<String> cssFiles;
    HashSet<String> usedClasses;
    HashSet<String> usedTags;
    HashSet<String> usedIds;
    ArrayList<RuleMedia> mqCandidates;
    ArrayList<RuleSet> regCandidates;
    ArrayList<String> wantedProperties;
    ArrayList<String> mutatedFiles;
    Random random;
    int numMutants;
    String htmlContent;
    Document page;
    WebDriver driver;
    ArrayList<Integer> bpBoundaryValues;
    String strippedUrl;
    //	StyleSheet ss;
    StyleSheet originalSS;
    Cloner cloner;
    String shorthand;
    String preamble = "file:///Users/thomaswalsh/Documents/Workspace/rlt-tool/xpert/testing/";
    HashMap<Integer, Integer> resToCheckWith;
    String[] tagsIgnore = { "A", "AREA", "B", "BLOCKQUOTE",
            "BR", "CANVAS", "CENTER", "CSACTIONDICT", "CSSCRIPTDICT", "CUFON",
            "CUFONTEXT", "DD", "EM", "EMBED", "FIELDSET", "FONT", "FORM",
            "HEAD", "HR", "I", "LABEL", "LEGEND", "LINK", "MAP", "MENUMACHINE",
            "META", "NOFRAMES", "NOSCRIPT", "OBJECT", "OPTGROUP", "OPTION",
            "PARAM", "S", "SCRIPT", "SMALL", "SPAN", "STRIKE", "STRONG",
            "STYLE", "TBODY", "TITLE", "TR", "TT", "U" };

    public CSSMutator(String baseUrl, String shortH, int numMutants) {
        this.baseURL = baseUrl;
        this.shorthand = shortH;
        usedClasses = new HashSet<String>();
        usedTags = new HashSet<String>();
        usedIds = new HashSet<String>();
        mqCandidates = new ArrayList<RuleMedia>();
        regCandidates = new ArrayList<RuleSet>();
        wantedProperties = new ArrayList<String>();
        wantedProperties.add("padding-top");
        wantedProperties.add("padding-bottom");
        wantedProperties.add("padding-right");
        wantedProperties.add("padding-left");
        wantedProperties.add("width");
        wantedProperties.add("min-width");
        wantedProperties.add("max-width");
        wantedProperties.add("margin");
        wantedProperties.add("padding");
        wantedProperties.add("margin-top");
        wantedProperties.add("margin-bottom");
        wantedProperties.add("margin-left");
        wantedProperties.add("margin-right");
        mutatedFiles = new ArrayList<String>();
        random = new Random();
        this.numMutants = numMutants;
        bpBoundaryValues = new ArrayList<Integer>();
        String[] splits = baseUrl.split("/");
        strippedUrl = splits[splits.length-1];
        cloner = new Cloner();
        cssFiles = new ArrayList<String>();
        System.setProperty("webdriver.chrome.driver",
                "/Users/thomaswalsh/Downloads/chromedriver");
        resToCheckWith = new HashMap<Integer, Integer>();

    }

    @SuppressWarnings("unchecked")
    public void initialiseFiles() {
        driver = new ChromeDriver();
        driver.get(preamble + baseURL);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = Utils.getPkgFileContents(CSSMutator.class, "getCssFiles.js");
        cssFiles = (ArrayList<String>) js.executeScript(script);

        driver.quit();
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    public void loadInCss(String base) {
        URL cssUrl = null;
        URLConnection conn;
//		String contents = "";
//		this.cssFiles.remove(2);
        String[] cssContent = new String[cssFiles.size()];
        int counter = 0;
        for (String cssFile : this.cssFiles) {
            String contents = "";
            try {
                if (cssFile.contains("http")) {
                    cssUrl = new URL(cssFile);
                } else if (cssFile.substring(0, 2).equals("//")) {
                    cssUrl = new URL("http:" + cssFile);
                    break;
                } else {
                    cssUrl = new URL((preamble + base + cssFile));
                }
                System.out.println(cssUrl);
                conn = cssUrl.openConnection();
                BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                while ((inputLine = input.readLine()) != null) {
                    contents += inputLine;
                }
                contents += "\n\n";
                cssContent[counter] = contents;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Problem loading or parsing the CSS file " + cssUrl.toString());
            }
            counter++;
        }
        StyleSheet ss = null;
        for (int i = 0; i < cssContent.length; i++) {
            String s = cssContent[i];
//			System.out.println(s);
            try {
                System.out.println(cssFiles.get(i));
//				String prettified = prettifyCss(s);
                String prettified = s;
                StyleSheet temp = CSSFactory.parse(prettified);
//				System.out.println(temp);
                if (ss == null) {
                    ss = cloner.deepClone(temp);
                } else {
                    ss.addAll(cloner.deepClone(temp.asList()));
                }
                System.out.println(temp.size());
                System.out.println(ss.size());
//				originalSS = cloner.deepClone(ss);
                for (RuleBlock rb : temp.asList()) {
                    if (rb instanceof RuleSet) {
                        if (selectorUsed(((RuleSet) rb).getSelectors())) {
                            RuleSet rs = (RuleSet) rb;
                            List<Declaration> decs = rs.asList();
                            ArrayList<Declaration> decsToKeep = new ArrayList<Declaration>();
                            for (Declaration d : decs) {
                                List<Term<?>> terms = d.asList();
                                for (Term t : terms) {
                                    if ((t instanceof TermLength) || (t instanceof TermPercent)){
                                        for (String p : wantedProperties) {
                                            if (d.getProperty().toLowerCase().equals(p)) {
                                                if (!decsToKeep.contains(d)) {
                                                    decsToKeep.add(d);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (decsToKeep.size() > 0) {
                                rs.replaceAll(decsToKeep);
                                regCandidates.add(rs);
                            }
                        }
                    } else if (rb instanceof RuleMedia) {
                        RuleMedia rm = (RuleMedia) rb;
                        if (hasNumericQuery(rm)) {
                            ArrayList<RuleSet> blocksToKeep = new ArrayList<RuleSet>();
                            List<RuleSet> sets = rm.asList();
                            for (RuleSet rs : sets) {
                                if (selectorUsed(rs.getSelectors())) {

                                    List<Declaration> decs = rs.asList();
                                    ArrayList<Declaration> decsToKeep = new ArrayList<Declaration>();
                                    for (Declaration d : decs) {
                                        List<Term<?>> terms = d.asList();
                                        for (Term t : terms) {
                                            if ((t instanceof TermLength) || (t instanceof TermPercent)) {
                                                for (String p : wantedProperties) {
                                                    if (d.getProperty().toLowerCase().equals(p)) {
                                                        if (!decsToKeep.contains(d)) {
                                                            decsToKeep.add(d);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (decsToKeep.size() > 0) {
                                        rs.replaceAll(decsToKeep);
                                        blocksToKeep.add(rs);
                                    }
                                }
                            }
                            rm.replaceAll(blocksToKeep);
                            if (rm.asList().size() > 0) {
                                mqCandidates.add(rm);
                            }
                        }
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (CSSException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                System.out.println("Null pointer for some reason on " + i);
            }
        }
        this.originalSS = cloner.deepClone(ss);


//		System.out.println(originalSS.size());
    }

    private boolean hasNumericQuery(RuleBlock rb) {
        for (MediaQuery mq : ((RuleMedia) rb).getMediaQueries()) {
            try {
                for (MediaExpression me : mq.asList()) {
                    if (me.toString().contains("width")) {
                        return true;
                    }
                }

            } catch (Exception e) {

            }
        }
        return false;
    }

    public boolean selectorUsed(List<CombinedSelector> sels) {
        for (CombinedSelector s : sels) {
            String[] splits = s.toString().split(" ");
            String[] trimmedArray = splits[splits.length-1].split("[.#]");
            String trimmed = trimmedArray[trimmedArray.length-1];
            if (usedClasses.contains(s.toString()) || usedTags.contains(s.toString()) || usedIds.contains(s.toString())) {
                return true;
            }

//			if (usedClasses.contains(trimmed) || usedTags.contains(trimmed)) {
//				return true;
//			}

//			for (String uc : usedClasses) {
//				if (trimmed.toString().contains(uc)) {
//					return true;
//				}
//			}
//
//			for (String ut : usedTags) {
//				if (trimmed.toString().contains(ut)) {
//					return true;
//				}
//			}
        }




        return false;
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void mutateBreakpoint(int i, StyleSheet toMutate) {
        boolean madeMutant = false;
        while(!madeMutant) {
            try {
                Collections.shuffle(mqCandidates);
                RuleMedia temp = mqCandidates.get(0);
                RuleMedia rm = (RuleMedia) getRuleBlockFromStyleSheet(toMutate, temp);
                RuleMedia toPrint = cloner.deepClone(rm);
                List<MediaQuery> queries = rm.getMediaQueries();
                Collections.shuffle(queries);
                MediaQuery mq = queries.get(0);

                List<MediaExpression> exs = mq.asList();
                Collections.shuffle(exs);
                MediaExpression me = exs.get(0);
                Term t = me.asList().get(0);
                int mutator = random.nextInt(10)+1;
//				mutator = 0;

                int sign = random.nextInt(10);
                float orig = (float) t.getValue();
                float mutated;
                if (sign <= 5) {
                    mutated = (float)orig + mutator;
                } else {
                    mutated = (float)orig - mutator;
                }
                t.setValue(mutated);
                writeMutantToFile(i, "Changed media query from " + orig + " to " + mutated + "\n" + toPrint + "\n" + rm);
//				System.out.println("Changed media query from " + orig + " to " + mutated + "\n" + toPrint + "\n" + rm);
                resToCheckWith.put(i, (int) ((orig + mutated)/2));
                madeMutant = true;
            } catch (Exception e) {
//				System.out.println("Failed on this one");
            }
        }
    }

    public void shiftRuleBlock(int i, StyleSheet toMutate) {
        int index;
        int controller = random.nextInt(10);
        if (controller <= 3) {
            // Shift a non-breakpointed rule into a media query
            Collections.shuffle(regCandidates);
            RuleSet rs = regCandidates.get(0);
//			RuleSet rs = (RuleSet) getRuleBlockFromStyleSheet(ss, temp);
            Collections.shuffle(mqCandidates);
            RuleMedia rm = mqCandidates.get(0);
//			RuleMedia rm = (RuleMedia) getRuleBlockFromStyleSheet(ss, tempRm);

            // Add the chosen rule to the chosen media query
            rm.add(rs);
            // Remove chosen rule from set of non-breakpointed rules.
            index = regCandidates.indexOf(rs);
            regCandidates.remove(rs);
//			sortMediaQueries();
//			mergeOriginalAndMutated(originalSS, regCandidates, mqCandidates);
//			writeToFile(i, toM);
            writeMutantToFile(i, "Shifted " + rs + " from outside all media queries to " + rm);

            // Return to original state
            rm.remove(rs);
            regCandidates.add(index, rs);
        } else {
            Collections.shuffle(mqCandidates);
            RuleMedia rm = mqCandidates.get(0);
//			RuleMedia rm = (RuleMedia) getRuleBlockFromStyleSheet(ss, temp);
            List<RuleSet> rules = rm.asList();
            Collections.shuffle(rules);
            RuleSet rs = rules.get(0);
//			RuleSet rs = (RuleSet) getRuleBlockFromStyleSheet(ss, tempRs);
            int rand = random.nextInt(10);
            if (rand <= 5) {
                // Shift the chosen rule outside all the media queries
                index = rules.indexOf(rs);
                rules.remove(rs);
                regCandidates.add(rs);
//				sortMediaQueries();
//				mergeOriginalAndMutated(originalSS, regCandidates, mqCandidates);
//				writeToFile(i);
                writeMutantToFile(i, "Shifted " + rs + " from " + rm + " to outside all media queries.");

                // Return to original state
                regCandidates.remove(rs);
                rules.add(index, rs);
            } else {
                // Create a temp array to store extra media query sets
                ArrayList<RuleMedia> extras = (ArrayList<RuleMedia>) mqCandidates.clone();
                extras.remove(rm);
                boolean foundAnMQ = false;
                RuleMedia toMoveTo = null;
                while (!foundAnMQ) {
                    Collections.shuffle(extras);
                    toMoveTo = extras.get(0);

                    if (!mediaQueriesEqual(rm, toMoveTo)) {
                        foundAnMQ = true;
                    }
                }
                // Move the RuleSet to the new media query.
                index = rm.indexOf(rs);
                rm.remove(rs);
                toMoveTo.add(rs);
//				sortMediaQueries();
//				mergeOriginalAndMutated(originalSS, regCandidates, mqCandidates);
//				writeToFile(i);
                writeMutantToFile(i, "Shifted " + rs + " from " + rm + " to " + toMoveTo);

                // Return to original state until I work out a way of copying
                toMoveTo.remove(rs);
                rm.add(index, rs);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void mutateRule(int i, StyleSheet copy) {
        boolean mutatedARule = false;
        while (!mutatedARule) {
            try {
                RuleSet selectedPruned;
                int controller = random.nextInt(10);
                RuleSet toMutate;
                if (controller <= 3) {
                    // Mutate a rule with no MQ
                    Collections.shuffle(regCandidates);
                    selectedPruned = regCandidates.get(0);
                    toMutate = (RuleSet) getRuleBlockFromStyleSheet(copy, selectedPruned);
                } else {
                    Collections.shuffle(mqCandidates);
                    RuleMedia rm = mqCandidates.get(0);
                    RuleMedia toMutateRM = (RuleMedia) getRuleBlockFromStyleSheet(copy, rm);
                    List<RuleSet> rules = rm.asList();
                    Collections.shuffle(rules);
                    selectedPruned = rules.get(0);
                    toMutate = (RuleSet) getRuleSetFromRuleMedia(toMutateRM, selectedPruned);
                }

                RuleSet toPrint = cloner.deepClone(toMutate);

                List<Declaration> decs = selectedPruned.asList();
                Collections.shuffle(decs);
                Declaration chosen = decs.get(0);
                Declaration decToMutate = getDeclarationFromRuleBlock(chosen, toMutate);

                boolean madeMutant = false;
                Term t = null;
                while(!madeMutant) {
                    try {
                        List<Term<?>> terms = decToMutate.asList();
                        int selector = random.nextInt(terms.size());
//						System.out.println(terms.size());
                        t = terms.get(selector);
                        int mutator = random.nextInt(10)+1;
                        int sign = random.nextInt(10);
                        float orig = (float) t.getValue();
                        float mutated;
                        if (orig == 0.0f) {
                            mutated = orig + mutator;
                        } else {
                            if (sign <= 5) {
                                mutated = (float)orig + mutator;
                            } else {
                                mutated = (float)orig - mutator;
                            }
                        }
                        if (t instanceof TermInteger) {
                            t.setValue(mutated+ "px");
                        } else {
                            t.setValue(mutated);
                        }
                        madeMutant = true;
                        writeMutantToFile(i, "Changed term from " + orig + " to " + mutated + "\n" + toPrint + "\n" + toMutate);
                        mutatedARule=true;
                    } catch (Exception e) {
                        System.out.println("Failed to mutate properly"+ t);
                    }
                }
            } catch (Exception e) {
            }
        }
    }




    private RuleSet getRuleSetFromRuleMedia(RuleMedia toMutateRM, RuleSet selectedPruned) {
        ArrayList<RuleSet> matches = new ArrayList<RuleSet>();
        for (RuleSet rs : toMutateRM.asList()) {
            if (rs.getSelectors().equals(selectedPruned.getSelectors())) {
//				return rs;
                matches.add(rs);
            }
        }
        if (matches.size() > 0) {
            return matches.get(matches.size()-1);
        }
        return null;
    }

    private Declaration getDeclarationFromRuleBlock(Declaration chosen,
                                                    RuleSet toMutate) {
        for (Declaration d : toMutate.asList()) {
            if (chosen.getProperty().equals(d.getProperty())) {
                return d;
            }
        }
        return null;
    }

    public void writeMutantToFile(int i, String contents) {
        PrintWriter output = null;
        try {
            String dirName = preamble.replace("file:///", "/") + shorthand + "/";
            String fileName2 = i + ".txt";

            File file = new File (dirName + fileName2);
//			System.out.println(file);
//			output = new PrintWriter(fileName);
            output = new PrintWriter(new FileWriter(file));
            output.append(contents);
        } catch (Exception e) {

        }
        output.close();

    }

    public void writeToFile(int counter, StyleSheet toMutate) {
        PrintWriter output = null;
        try {
            String dirName = preamble.replace("file:///", "/") + shorthand + "/";
            String fileName = counter + ".css";

            File file = new File (dirName + fileName);
//			System.out.println(file);
//			output = new PrintWriter(fileName);
            output = new PrintWriter(new FileWriter(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
//			e.printStackTrace();
        }

        for (RuleBlock rb : toMutate.asList()) {
            output.append(rb.toString());
        }

        output.close();
    }

    public String prettifyCss(String minified) {
        String res="";
        try {
            WebDriver newdriver = new ChromeDriver();
            newdriver.get("http://mrcoles.com/blog/css-unminify/");
            JavascriptExecutor js = (JavascriptExecutor) newdriver;
            String prettifyScript = Utils.getPkgFileContents(Redecheck.class, "prettify.js");
            res = (String) js.executeScript(prettifyScript, minified);
//			System.out.print(res);
            newdriver.quit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public boolean mediaQueriesEqual(RuleMedia rm, RuleMedia toMoveTo) {
        List<MediaQuery> mq1 = rm.getMediaQueries();
        List<MediaQuery> mq2 = toMoveTo.getMediaQueries();
        if (mq1.size() != mq2.size()) {
            // Number of media queries are different, so can't be equal
            return false;
        } else {
            boolean foundMatch;
            for (MediaQuery toMatch : mq1) {
                foundMatch = false;
                for (MediaQuery comparison : mq2) {
                    if (toMatch.toString().equals(comparison.toString())) {
                        foundMatch = true;
                    }
                }
                if (!foundMatch) {
                    return false;
                }
            }
        }

        return true;
    }

    public void writeNewHtml(int i, ArrayList<String> mutatedFiles) {
        Document copy = page.clone();
        Elements sss = copy.getElementsByAttributeValue("rel", "stylesheet");
        for (Element s : sss) {
            if (!s.toString().contains("need")) {
                s.remove();
            }
        }
        copy.head().append("<link href=\"" + i + ".css\" rel=\"stylesheet\">");
        PrintWriter output = null;
        try {
            String dirName = preamble.replace("file:///", "/") + shorthand + "/";
            String fileName = i + ".html";

            File file = new File (dirName + fileName);
            output = new PrintWriter(new FileWriter(file));
            mutatedFiles.add(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        output.append(copy.toString());
        output.close();
    }

    public void mutate(int i) {
        StyleSheet toMutate = cloner.deepClone(originalSS);

        Random rand = new Random();
        boolean b = rand.nextBoolean();
        if (b) {
            mutateRule(i, toMutate);
        } else if (!b) {
            mutateBreakpoint(i, toMutate);
        }
        writeToFile(i, toMutate);
    }

    @SuppressWarnings("rawtypes")
    public RuleBlock getRuleBlockFromStyleSheet(StyleSheet sheet, RuleBlock block) {
        ArrayList<RuleBlock> matches = new ArrayList<RuleBlock>();
        if (block instanceof RuleSet) {
            for (RuleBlock rb : sheet.asList()) {
                if (rb instanceof RuleSet) {
                    if (rb.equals((RuleSet)block)) {
                        return rb;
                    } else if (((RuleSet) rb).getSelectors().equals(((RuleSet)block).getSelectors())) {
                        //Check the declarations
                        List<Declaration> d1 = ((RuleSet)rb).asList();
                        List<Declaration> d2 = ((RuleSet)block).asList();
                        if (d1.equals(d2)) {
                            matches.add(rb);
//							return rb;
                        } else {
                            // Check rb contains all rules of block
                            int numMatched = 0;
                            for (Declaration toMatch : d2) {
//								String property = toMatch.getProperty();
                                for (Declaration origD : d1) {
//									if (property.equals(origD.getProperty())) {
                                    if (toMatch.equals(origD)) {
                                        numMatched++;
                                    }
                                }
                            }
                            //Return rb if we matched them all
                            if (numMatched == d2.size()) {
//								return rb;
                                matches.add(rb);
                            }
                        }
                    }
                }
            }
        } else if (block instanceof RuleMedia) {
            for (RuleBlock rb : sheet.asList()) {
                if (rb instanceof RuleMedia) {
                    if (rb.equals(block)) {
//						return rb;
                        matches.add(rb);
//					} else if (((RuleMedia) rb).getMediaQueries().equals(((RuleMedia) block).getMediaQueries())) {
                    } else if (haveSameMediaQueries((RuleMedia)rb, (RuleMedia)block)) {
                        // Media queries match, check if ruleblocks match
                        if (rb.asList().equals(block.asList())) {
//							return rb;
                            matches.add(rb);
                        } else {

                            List<RuleBlock> b1 = (List<RuleBlock>) rb.asList();
                            List<RuleBlock> b2 = (List<RuleBlock>) block.asList();

                            int numMatched = 0;
                            for (RuleBlock toMatch : b2) {
                                boolean foundMatchForthisOne = false;
                                List<CombinedSelector> sels = ((RuleSet) toMatch).getSelectors();
                                for (RuleBlock origRb : b1) {
                                    List<CombinedSelector> selsOrig = ((RuleSet)origRb).getSelectors();
                                    if (sels.equals(selsOrig)) {
                                        if (!foundMatchForthisOne) {
                                            numMatched++;
                                            foundMatchForthisOne = true;
                                        }
                                        break;
                                    }
                                }
                            }
//							System.out.println(numMatched + " against " + b2.size());
                            if (numMatched == b2.size()) {
//								return rb;
                                matches.add(rb);
                            } else {
//								System.out.println(b1);
//								System.out.println(b2);
                            }

                        }
                    }
                }
            }
        }
        if (matches.size() > 0) {
            return matches.get(matches.size()-1);
        }
        System.out.println("Couldn't find the following in SS");
        System.out.println(block);
        return null;
    }

    private boolean haveSameMediaQueries(RuleMedia rb, RuleMedia block) {
        List<MediaQuery> mq1 = rb.getMediaQueries();
        List<MediaQuery> mq2 = block.getMediaQueries();
        if (mq1.size() != mq2.size()) {
            return false;
        } else {
            if (mq1.toString().equals(mq2.toString())) {
//				System.out.println(rb);
//				System.out.println(block);
                return true;
            }
        }
        return false;
    }

    public void getBreakpoints() {
        TreeSet<Integer> bps = new TreeSet<Integer>();
        for (RuleMedia rm : mqCandidates) {
            List<MediaQuery> mqs = rm.getMediaQueries();
            for (MediaQuery mq : mqs) {
                for (MediaExpression me : mq.asList()) {
                    try {
                        if (me.toString().contains("width")) {
                            Double bp = (Double) me.asList().get(0).getValue();
                            bps.add(bp.intValue());
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        System.out.println(bps);
        TreeMap<Integer, Integer> ranges = new TreeMap<Integer, Integer>();
        Iterator iter = bps.iterator();
        int i = (int) iter.next();
        ranges.put(0, i-1);
        int previous = i;
        while (iter.hasNext()) {
            int x = (int) iter.next();
            ranges.put(previous, x-1);
            previous = x;
        }
        ranges.put(previous, 1300);
        System.out.println(ranges);
    }



    public static void main(String[] args) throws IOException, CSSException {
        CSSMutator mt = new CSSMutator("shield.com/index.html", "shield.com", 20);
        mt.initialiseFiles();
        int[] newOnes = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
//		int[] newOnes = {0};
        Set<Integer> set = new HashSet<Integer>();
        for (int i : newOnes) {
            set.add(i);
        }
        mt.parseHTML(mt.shorthand);
        mt.loadInCss(mt.shorthand + "/");
        System.out.println(mt.getStats());
        for (int i = 0; i < mt.numMutants; i++) {
            if (set.contains((i))) {
                mt.writeNewHtml(i, mt.mutatedFiles);
                mt.mutate(i);
            }
        }



    }

    private String getStats() {
        int numBlocks = 0;
        int numDecs = 0;
        int numDomNodes = 2;
        ArrayList<Element> worklist = new ArrayList<Element>();
        worklist.add(this.page.head());
        worklist.add(this.page.body());
        while (worklist.size() > 0) {
            Element e = worklist.remove(0);
            numDomNodes += e.children().size();
            worklist.addAll(e.children());
        }
        for (RuleBlock rb : originalSS) {
            numBlocks++;
            if (rb instanceof RuleSet) {
                RuleSet casted = (RuleSet) rb;
                for (Declaration d : casted.asList()) {
                    numDecs++;
                }
            } else if (rb instanceof RuleMedia) {
                RuleMedia casted2 = (RuleMedia) rb;
                for (RuleSet rs : casted2.asList()) {
                    numBlocks++;
                    for (Declaration d : rs.asList()) {
                        numDecs++;
                    }
                }
            }
        }
        return "Number of DOM Nodes: " + numDomNodes + "\nNumber of rule blocks: " + numBlocks + "\nNumber of declarations: " + numDecs;
    }
}
