package shef.mutation;

import com.rits.cloning.Cloner;

import cz.vutbr.web.css.*;
import cz.vutbr.web.csskit.TermIntegerImpl;
import cz.vutbr.web.csskit.TermLengthImpl;
import cz.vutbr.web.csskit.TermNumericImpl;
import cz.vutbr.web.csskit.TermPercentImpl;

import java.io.IOException;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import shef.main.Utils;

/**
 * Created by thomaswalsh on 05/10/2015.
 */
public class CSSMutator {
    String baseURL;
    String current;
    int mutantNumber;
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
    WebDriver driver;
    ArrayList<Integer> bpBoundaryValues;
    String strippedUrl;
    //	StyleSheet ss;
    StyleSheet originalSS;

    public LinkedHashMap<String, StyleSheet> getStylesheets() {
        return stylesheets;
    }

    LinkedHashMap<String, StyleSheet> stylesheets;
    Cloner cloner;
    String shorthand;
    Document htmlDoc;
    static String preamble = "file:///Users/thomaswalsh/Documents/Workspace/Redecheck/testing/";
    HashMap<Integer, Integer> resToCheckWith;
    String[] tagsIgnore = { "A", "AREA", "B", "BLOCKQUOTE",
            "BR", "CANVAS", "CENTER", "CSACTIONDICT", "CSSCRIPTDICT", "CUFON",
            "CUFONTEXT", "DD", "EM", "EMBED", "FIELDSET", "FONT", "FORM",
            "HEAD", "HR", "I", "LABEL", "LEGEND", "LINK", "MAP", "MENUMACHINE",
            "META", "NOFRAMES", "NOSCRIPT", "OBJECT", "OPTGROUP", "OPTION",
            "PARAM", "S", "SCRIPT", "SMALL", "SPAN", "STRIKE", "STRONG",
            "STYLE", "TBODY", "TITLE", "TR", "TT", "U" };

    String[] units = { "px", "em", "%"};
    String[] floatOptions = { "none", "left", "right", "initial", "inherit"};
    String[] positionOptions = { "static", "absolute","fixed","relative","initial", "inherit"};
    String[] displayOptions = {"none", "inline", "block", "inline-block", "initial", "inherit"};

    public String mutantDesc;

    public CSSMutator(String baseURL, String sh, LinkedHashMap<String, StyleSheet> stylesheets2, ArrayList<RuleSet> ruleCandidates, ArrayList<RuleMedia> mqCandidates2, Document page, int mutantNumber) {
		this.baseURL = baseURL;
		this.shorthand = sh;
		this.stylesheets = stylesheets2;
		this.regCandidates = ruleCandidates;
		this.mqCandidates = mqCandidates2;
		this.mutantNumber = mutantNumber;
		htmlDoc = page;
		cloner = new Cloner();
		random = new Random();
		mutantDesc = "";
	}

    public static boolean hasNumericQuery(RuleBlock rb) {
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



    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void mutateMediaQuery(int i, HashMap<String, StyleSheet> toMutate, int selector) {
        boolean madeMutant = false;
        while(!madeMutant) {
            try {
                Collections.shuffle(mqCandidates);
                RuleMedia temp = mqCandidates.get(0);
                RuleMedia rm = (RuleMedia) getRuleBlockFromStyleSheet(toMutate, temp);
                List<MediaQuery> queries = rm.getMediaQueries();
                Collections.shuffle(queries);
                MediaQuery mq = queries.get(0);

                List<MediaExpression> exs = mq.asList();
                Collections.shuffle(exs);
                MediaExpression me = exs.get(0);
                if (selector == 3) {
                    Term t = me.asList().get(0);
                    changeBreakpoint(t, i, rm);
                    madeMutant = true;
                } else {
                    changeCondition(me);
//                    writeMutantToFile(i, "Changed expression to " + me.getFeature() + " in \n" + rm);
                    madeMutant = true;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void changeBreakpoint(Term t, int i, RuleMedia rm) {
        int mutatorValue = random.nextInt(10) + 1;

        int sign = random.nextInt(10);
        float orig = (float) t.getValue();
        float mutated;
        if (sign <= 5) {
            mutated = (float) orig + mutatorValue;
        } else {
            mutated = (float) orig - mutatorValue;
        }
        t.setValue(mutated);
        writeMutantToFile(i, "Changed value from " + orig + " to " + mutated + " in \n" + rm);
        System.out.println("Changed value from " + orig + " to " + mutated + " in \n" + rm);
    }

    public void changeCondition(MediaExpression me) {
        if (me.getFeature().equals("min-width")) {
            me.setFeature("max-width");
        } else if (me.getFeature().equals("max-width")) {
            me.setFeature("min-width");
        }
        System.out.println("Changed condition in " + me);
    }



    public void shiftRuleBlock(int i, StyleSheet toMutate) {
        int index;
        int controller = random.nextInt(10);
        if (controller <= 3) {
            // Shift a non-breakpointed rule into a media query
            Collections.shuffle(regCandidates);
            RuleSet rs = regCandidates.get(0);
            Collections.shuffle(mqCandidates);
            RuleMedia rm = mqCandidates.get(0);

            // Add the chosen rule to the chosen media query
            rm.add(rs);
            // Remove chosen rule from set of non-breakpointed rules.
            index = regCandidates.indexOf(rs);
            regCandidates.remove(rs);
            writeMutantToFile(i, "Shifted " + rs + " from outside all media queries to " + rm);

            // Return to original state
            rm.remove(rs);
            regCandidates.add(index, rs);
        } else {
            Collections.shuffle(mqCandidates);
            RuleMedia rm = mqCandidates.get(0);
            List<RuleSet> rules = rm.asList();
            Collections.shuffle(rules);
            RuleSet rs = rules.get(0);
            int rand = random.nextInt(10);
            if (rand <= 5) {
                // Shift the chosen rule outside all the media queries
                index = rules.indexOf(rs);
                rules.remove(rs);
                regCandidates.add(rs);
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
                writeMutantToFile(i, "Shifted " + rs + " from " + rm + " to " + toMoveTo);

                // Return to original state until I work out a way of copying
                toMoveTo.remove(rs);
                rm.add(index, rs);
            }
        }
    }

    private void mutateRule(HashMap<String, StyleSheet> sss, RuleSet rs, Declaration dec, int termIndex, int i) {
        RuleSet ruleSet = (RuleSet) getRuleBlockFromStyleSheet(sss, rs);
        RuleSet toPrint = cloner.deepClone(ruleSet);
        Declaration decToMutate = getDeclarationFromRuleBlock(dec, ruleSet);
//        System.out.println(decToMutate);
        String property = decToMutate.getProperty().toLowerCase();
        List<Term<?>> terms = decToMutate.asList();
        Term t = terms.get(termIndex);
        if ((!property.equals("display")) && (!property.equals("position")) && (!property.equals("float"))) {
            changeRuleValueNumeric(property, t, true, true, i, ruleSet, toPrint, decToMutate, i);
        } else {
            changeRuleValueFixed(property, t, true, true, i, ruleSet, toPrint, decToMutate, i);
        }

    }

    @SuppressWarnings("unchecked")
    public void mutateRandomRule(int i, HashMap<String, StyleSheet> toMutate2, int selector2, int toggle) {
        boolean mutatedARule = false;
        while (!mutatedARule) {
            try {
                RuleSet selectedPruned = null;
                int controller = random.nextInt(10);
                RuleSet toMutate = null;
                if (controller <= 3) {
//                    System.out.println("No MQ");
                    // Mutate a rule with no MQ
                    Collections.shuffle(regCandidates);
                    selectedPruned = regCandidates.get(0);
                    toMutate = (RuleSet) getRuleBlockFromStyleSheet(toMutate2, selectedPruned);
                } else if (mqCandidates.size() > 0){
//                    System.out.println("Yes MQ");
                    Collections.shuffle(mqCandidates);
                    RuleMedia rm = mqCandidates.get(0);

                    RuleMedia toMutateRM = (RuleMedia) getRuleBlockFromStyleSheet(toMutate2, rm);
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
                String property = decToMutate.getProperty().toLowerCase();
                boolean madeMutant = false;
                Term t = null;
//                while(!madeMutant) {
//                    try {
                        List<Term<?>> terms = decToMutate.asList();
                        int selector = random.nextInt(terms.size());
                        t = terms.get(selector);

                        // If the rule-value mutation operator is selected
                        if (selector2 == 0) {
//                            System.out.println("Mutating rule-value");
//                            changeRuleValue(property, t, mutatedARule, madeMutant, i, toMutate, toPrint, decToMutate, 0);
	                    // If the rule-unit mutation operator is selected
                        } else if (selector2 == 1){
//                            System.out.println("Mutating rule-unit");
                            changeRuleUnit(t, mutatedARule, madeMutant, toMutate2, terms, selector, i, decToMutate);
                        } else {
//                            deleteDeclaration(decToMutate);
                            // Try to remove the declaration from the ruleblock
                            toMutate.remove(decToMutate);
//                            System.out.println("Removed " + decToMutate + " from " + decs.toString());
                        }
                        mutatedARule = true;

//                    } catch (Exception e) {
//                    	e.printStackTrace();
//                    }
//                }
            } catch (Exception e) {
//                e.printStackTrace();
//                System.out.println("WENT WRONG SOMEWHERE");
            }
        }
    }



    private void changeRuleUnit(Term t, boolean mutatedARule, boolean madeMutant, HashMap<String, StyleSheet> toMutate2, List<Term<?>> terms, int selector, int i, Declaration decToMutate) {
        boolean mutated = false;

        while (!mutated) {

            int unitSelector = random.nextInt(units.length);
            String selectedUnit = units[unitSelector];

//            System.out.println("Mutating unit of " + t + " with new unit " + selectedUnit);
            if ((t instanceof TermIntegerImpl) || (t instanceof TermLengthImpl)) {
                TermNumericImpl<Float> t2 = (TermNumericImpl<Float>) t;
                switch (selectedUnit) {
                    case "none":
                        t2.setUnit(TermNumeric.Unit.none);
                        break;
                    case "px":
                        t2.setUnit(TermNumeric.Unit.px);
                        break;
                    case "em":
                        t2.setUnit(TermNumeric.Unit.em);
                        break;
                    case "%":
                        TermNumericImpl<Float> temp = (TermNumericImpl<Float>) t;
                        t2 = cloner.deepClone(getTermObject(toMutate2, false));
                        t2.setValue(temp.getValue());
                        terms.remove(selector);
                        terms.add(selector, t2);
                        break;
                }
                mutatedARule = true;
            } else if (t instanceof TermPercentImpl) {
                TermNumericImpl<Float> tli = cloner.deepClone(getTermObject(toMutate2, true));
                TermNumericImpl<Float> t2 = (TermNumericImpl<Float>) t;
                tli.setValue(t2.getValue());
                switch (selectedUnit) {
                    case "none":
                        tli.setUnit(TermNumeric.Unit.none);
                        break;
                    case "px":
                        tli.setUnit(TermNumeric.Unit.px);
                        break;
                    case "em":
                        tli.setUnit(TermNumeric.Unit.em);
                        break;
                    default:
                        break;
                }
                terms.remove(selector);
                terms.add(selector, tli);
                mutatedARule = true;
            }
            writeMutantToFile(i, "Changed UNIT in " + t + " to " + terms + " in \n" + decToMutate);
//            System.out.println("Changed UNIT in " + t + " to " + terms + " in \n" + decToMutate);
            mutated = true;
        }
    }

    private void changeRuleValueNumeric(String property, Term t, boolean mutatedARule, boolean madeMutant, int i, RuleSet toMutate, RuleSet toPrint, Declaration decToMutate, int upOrDown) {
        int mutator, sign;

        if ((!property.equals("display")) && (!property.equals("position")) && (!property.equals("float"))) {
            if (upOrDown == 0) {
                mutator = random.nextInt(10) + 1;
                sign = random.nextInt(10);
            } else if (upOrDown > 0) {
                mutator = 1;
                sign = 2;
            } else {
                mutator = 1;
                sign = 7;
            }
            float orig = (float) t.getValue();
            float mutated;
            if (orig == 0.0f) {
                mutated = orig + mutator;
            } else {
                if (sign <= 5) {
                    mutated = (float) orig + mutator;
                } else {
                    mutated = (float) orig - mutator;
                }
            }
            t.setValue(mutated);
            writeMutantToFile(i, "Changed term from " + orig + " to " + mutated + "\n" + toPrint + "\n" + toMutate);
            mutantDesc = "Changed term from " + orig + " to " + mutated + "\n" + toPrint + "\n" + toMutate;
//            System.out.println("Changed term from " + orig + " to " + mutated + "\n" + toPrint + "\n" + toMutate);
            mutatedARule = true;
            madeMutant = true;
        }
    }

    private void changeRuleValueFixed(String property, Term t, boolean mutatedARule, boolean madeMutant, int i, RuleSet toMutate, RuleSet toPrint, Declaration decToMutate, int upOrDown) {
        String current = (String) t.getValue();

        String newValue = "";
        if (decToMutate.getProperty().equals("float")) {
            if (upOrDown < 0) {
                int valSelector = random.nextInt(floatOptions.length);
                newValue = floatOptions[valSelector];
            } else {
                newValue = floatOptions[upOrDown];
            }
        } else if (decToMutate.getProperty().equals("position")) {
            if (upOrDown < 0) {
                int valSelector = random.nextInt(positionOptions.length);
                newValue = positionOptions[valSelector];
            } else {
                newValue = positionOptions[upOrDown];
            }
        } else if (decToMutate.getProperty().equals("display")) {
            if (upOrDown < 0) {
                int valSelector = random.nextInt(displayOptions.length);
                newValue = displayOptions[valSelector];
            } else {
                newValue = displayOptions[upOrDown];
            }
        }
//                                System.out.println(newValue);
//        if (!newValue.equals(current)) {
            t.setValue(newValue);
            writeMutantToFile(i, "Changed term from " + current + " to " + newValue + "\n" + toPrint + "\n" + toMutate);
            mutantDesc = "Changed term from " + current + " to " + newValue + "\n" + toPrint + "\n" + toMutate;
//            System.out.println("Changed term from " + current + " to " + newValue + "\n" + toPrint + "\n" + toMutate);
            mutatedARule = true;
            madeMutant = true;
//        }
    }


    @SuppressWarnings("unchecked")
	private TermNumericImpl<Float> getTermObject(HashMap<String, StyleSheet> toMutate2, boolean needTermLengthObject) {
    	ArrayList<TermNumericImpl<Float>> floats = new ArrayList<>();
    	for (StyleSheet ss : toMutate2.values()) {
            for (RuleBlock rb : ss.asList()) {
            	if (rb instanceof RuleSet) {
            		ArrayList<Declaration> decs = (ArrayList<Declaration>) ((RuleSet)rb).asList();
            		for (Declaration d : decs) {
            			ArrayList<Term<?>> terms = (ArrayList<Term<?>>) d.asList();
            			for (Term t : terms) {
                            if (needTermLengthObject) {
                                if (t instanceof TermLengthImpl) {
                                    floats.add((TermNumericImpl<Float>) t);
                                }
                            } else {
                                if (t instanceof TermPercentImpl) {
                                    floats.add((TermNumericImpl<Float>) t);
                                }
                            }
            			}
            		}
            	} else if (rb instanceof RuleMedia) {
//            		ArrayList<RuleSet> sets = (ArrayList<RuleSet>) ((RuleMedia) rb).asList();
//            		for (RuleSet rs : sets) {
//            			ArrayList<Declaration> decs = (ArrayList<Declaration>) ((RuleSet)rb).asList();
//                		for (Declaration d : decs) {
//                			ArrayList<Term<?>> terms = (ArrayList<Term<?>>) d.asList();
//                			for (Term t : terms) {
//                				if (t instanceof TermLengthImpl) {
//                					floats.add((TermNumericImpl<Float>) t);
//                				}
//                			}
//                		}
//            		}
            	}
            }
    	}
    	if (floats.size() != 0) {
    		Collections.shuffle(floats);
    		return floats.get(0);
    	} else {
    		return null;
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

    private Declaration getDeclarationFromRuleBlock(Declaration chosen, RuleSet toMutate) {
        for (Declaration d : toMutate.asList()) {
            if (chosen.getProperty().equals(d.getProperty())) {
                return d;
            }
        }
        return null;
    }

    public void writeMutantToFile(int i, String contents) {
        
        try {
        	PrintWriter output = null;
            String dirName = preamble.replace("file:///", "/") + shorthand + "/mutant"+ i + "/";
            String fileName2 = "mutant" + i + ".txt";

            File file = new File (dirName + fileName2);
            FileUtils.forceMkdir(new File(dirName));
            output = new PrintWriter(new FileWriter(file));
            output.append(contents);
            output.close();
            FileUtils.writeStringToFile(file, contents);
        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }

    public void writeToFile(int counter, HashMap<String, StyleSheet> toMutate, String shorthand, String newUrl) {
        PrintWriter output = null;
        for (String s : toMutate.keySet()) {
	        try {
	            if (newUrl == null) {
                    String dirName = preamble.replace("file:///", "/") + shorthand + "/mutant" + counter + "/resources/";
                    File theDir = new File(preamble.replace("file:///", "/") + shorthand + "/mutant" + counter + "/resources/");
                    boolean result = false;
                    if (!theDir.exists()) {
                        theDir.mkdirs();
                        result = true;
                    }
                    String[] splits = s.split("/");
                    String actualName = splits[splits.length - 1].replace(".css", "");
                    String fileName = actualName + counter + ".css";
                    File file = new File(dirName + fileName);
                    output = new PrintWriter(new FileWriter(file));

                    for (RuleBlock rb : toMutate.get(s).asList()) {
                        output.append(generateCSSString(rb));
                    }
                    output.close();
                } else {
	                // Fault fixing
                    String[] splits = s.split("/");
                    String actualName = splits[splits.length - 1];


                    File file = new File(newUrl + "/" + actualName);
//                    System.out.println(file.getAbsolutePath());
                    output = new PrintWriter(new FileWriter(file));

                    for (RuleBlock rb : toMutate.get(s).asList()) {
                        output.append(generateCSSString(rb));
                    }
                    output.close();
                }
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	        }
        }

    }

    public static String generateCSSString(RuleBlock rb) {
        String result = "";
        if (rb instanceof RuleSet) {
            RuleSet rs = (RuleSet) rb;
            String join = StringUtils.join(rs.getSelectors(), ", ");
            result += join + " { \n";
            for (Declaration d : rs.asList()) {
                if (d.getProperty().equals("font-weight")) {
                    result += "\t" + d.toString().replace(".0", "");
                } else if (d.getProperty().equals("z-index")) {
                    result += "\t" + d.toString().replace(".0", "");
                }else {
                    result += "\t" + d.toString();
                }
            }
            result += "}\n";
        } else if (rb instanceof RuleMedia) {
            RuleMedia rm = (RuleMedia) rb;
            result += "\n@media " + rm.getMediaQueries().get(0) + " { \n";
            for (RuleSet rs : rm.asList()) {
                String join = StringUtils.join(rs.getSelectors(), ", ");
                result += join + " { \n";
                for (Declaration d : rs.asList()) {
                    if (d.getProperty().equals("font-weight")) {
                        result += "\t" + d.toString().replace(".0", "");
                    } else if (d.getProperty().equals("z-index")) {
                        result += "\t" + d.toString().replace(".0", "");
                    } else {
                        result += "\t" + d.toString();
                    }
                }
                result += "}\n";
            }
            result += "}\n";
        } else if (rb instanceof RuleFontFace) {
            RuleFontFace rff = (RuleFontFace) rb;
            result +="\n@font-face { \n";
            for (Declaration d : rff.asList()) {
                if (d.getProperty().equals("font-weight")) {
                    result += "\t" + d.toString().replace(".0", "");
                } else {
                    result += "\t" + d.toString();
                }
            }
            result += "}\n";

        } else {
            result += rb.toString();
        }
        return result;
    }
    
    public void writeNewHtml(HashMap<String, StyleSheet> toMutate) {
        Elements sss = htmlDoc.getElementsByAttributeValue("rel", "stylesheet");
        for (Element s : sss) {
            if (!s.toString().contains("need")) {
                s.remove();
            }
        }
        for (String s : toMutate.keySet()) {
        	String[] splits = s.split("/");
            String actualName = splits[splits.length-1].replace(".css", "");
            String fileName = actualName + this.mutantNumber + ".css";
        	htmlDoc.head().append("<link href=\"" + "./resources/" + fileName + "\" rel=\"stylesheet\">");
        }
        PrintWriter output = null;
        try {
        	File theDir = new File(preamble.replace("file:///", "/") + shorthand + "/mutant" + this.mutantNumber + "/");
        	boolean result = false;
            String dirName = preamble.replace("file:///", "/") + shorthand + "/mutant" + this.mutantNumber + "/";
            if (!theDir.exists()) {
            	theDir.mkdirs();
            	result = true;
            }
            String fileName = "mutant" + this.mutantNumber + ".html";

            File file = new File (dirName + fileName);
            output = new PrintWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        output.append(htmlDoc.toString());
        output.close();
    }

    public static String prettifyCss(String minified, WebDriver d) {
        String res="";
        try {
        	String current = new java.io.File( "." ).getCanonicalPath();
        	DesiredCapabilities dCaps = new DesiredCapabilities();
            dCaps.setJavascriptEnabled(true);
            dCaps.setCapability("takesScreenshot", true);
            String[] phantomArgs = new  String[] {
            	    "--webdriver-loglevel=NONE"
            	};
//            dCaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
//            Fire newdriver = new PhantomJSDriver(dCaps);
            d.get("http://mrcoles.com/blog/css-unminify/");
            JavascriptExecutor js = (JavascriptExecutor) d;
            String prettifyScript = Utils.readFile(current +"/resources/prettify.js");
            res = (String) js.executeScript(prettifyScript, minified);
            d.quit();
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

    

    public void mutate(int selector, String newUrl) {
        HashMap<String, StyleSheet> toMutate = cloner.deepClone(this.stylesheets);

        if (selector <=2) {
//            System.out.println("Mutating Rule");
            mutateRandomRule(this.mutantNumber, toMutate, selector, 1);
        } else if (selector <=4) {
//            System.out.println("Mutating media query");
            mutateMediaQuery(this.mutantNumber, toMutate, selector);
        }
        writeToFile(this.mutantNumber, toMutate, this.shorthand, newUrl);
        writeNewHtml(toMutate);
    }



    @SuppressWarnings("rawtypes")
    public RuleBlock getRuleBlockFromStyleSheet(HashMap<String, StyleSheet> toMutate2, RuleBlock block) {
        ArrayList<RuleBlock> matches = new ArrayList<RuleBlock>();
        if (block instanceof RuleSet) {
        	for (StyleSheet ss : toMutate2.values()) {
	            for (RuleBlock rb : ss.asList()) {
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
            }
        } else if (block instanceof RuleMedia) {
        	for (StyleSheet ss : toMutate2.values()) {
	            for (RuleBlock rb : ss.asList()) {
	                if (rb instanceof RuleMedia) {
	                    if (rb.equals(block)) {
	                        matches.add(rb);
	//					} else if (((RuleMedia) rb).getMediaQueries().equals(((RuleMedia) block).getMediaQueries())) {
	                    } else if (haveSameMediaQueries((RuleMedia)rb, (RuleMedia)block)) {
	                        // Media queries match, check if ruleblocks match
	                        if (rb.asList().equals(block.asList())) {
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
	                            if (numMatched == b2.size()) {
	                                matches.add(rb);
	                            }
	                        }
	                    }
	                }
	            }
            }
        }
        if (matches.size() > 0) {
            return matches.get(matches.size()-1);
        }
        return null;
    }

    private boolean haveSameMediaQueries(RuleMedia rb, RuleMedia block) {
        List<MediaQuery> mq1 = rb.getMediaQueries();
        List<MediaQuery> mq2 = block.getMediaQueries();
        if (mq1.size() != mq2.size()) {
            return false;
        } else {
            if (mq1.toString().equals(mq2.toString())) {
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
    }

    private String getStats() {
        int numBlocks = 0;
        int numDecs = 0;
        int numDomNodes = 2;
        ArrayList<Element> worklist = new ArrayList<Element>();
        worklist.add(this.htmlDoc.head());
        worklist.add(this.htmlDoc.body());
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

    public HashMap<String, LinkedHashMap<String, StyleSheet>> getMutationOptions(LinkedHashMap<String, StyleSheet> currentSS) {

        HashMap<String, LinkedHashMap<String,StyleSheet>> mutated = new HashMap<>();
        for (RuleSet rs : regCandidates) {
            for (Declaration dec : rs) {
                System.out.println(dec);
                String property = dec.getProperty().toLowerCase();
                // Iterate through all the different terms
                if ((!property.equals("display")) && (!property.equals("position")) && (!property.equals("float"))) {
                    for (int t = 0; t < dec.asList().size(); t++) {
                        mutantDesc = "";
                        HashMap<String, StyleSheet> toMutate = cloner.deepClone(currentSS);
                        mutateRule(toMutate, rs, dec, t, 1);
                        mutated.put(mutantDesc, (LinkedHashMap<String, StyleSheet>) toMutate);

                        mutantDesc = "";
                        toMutate = cloner.deepClone(currentSS);
                        mutateRule(toMutate, rs, dec, t, -1);
                        mutated.put(mutantDesc, (LinkedHashMap<String, StyleSheet>) toMutate);
                    }
                } else {
                    HashMap<String, StyleSheet> toMutate;
                    if (property.equals("display")) {
                        for (int d = 0; d < displayOptions.length; d++) {
                            mutantDesc = "";
                            toMutate = cloner.deepClone(currentSS);
                            mutateRule(toMutate, rs, dec, 0, d);
                            mutated.put(mutantDesc, (LinkedHashMap<String, StyleSheet>) toMutate);
                        }
                    } else if (property.equals("position")) {
                        for (int d = 0; d < positionOptions.length; d++) {
                            mutantDesc = "";
                            toMutate = cloner.deepClone(currentSS);
                            mutateRule(toMutate, rs, dec, 0, d);
                            mutated.put(mutantDesc, (LinkedHashMap<String, StyleSheet>) toMutate);
                        }
                    } else if (property.equals("float")) {
                        for (int d = 0; d < floatOptions.length; d++) {
                            mutantDesc = "";
                            toMutate = cloner.deepClone(currentSS);
                            mutateRule(toMutate, rs, dec, 0, d);
                            mutated.put(mutantDesc, (LinkedHashMap<String, StyleSheet>) toMutate);
                        }
                    }
                }

            }
        }
        return mutated;
    }



}
