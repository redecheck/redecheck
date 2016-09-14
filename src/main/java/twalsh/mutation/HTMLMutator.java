package twalsh.mutation;

import com.rits.cloning.Cloner;

import cz.vutbr.web.css.StyleSheet;
import de.svenjacobs.loremipsum.LoremIpsum;
import org.apache.commons.io.FileUtils;
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
    private final int mutantNumber;
    private final String shorthand;
    HashSet<String> usedClasses, usedTags, usedIds;
    ArrayList<String> mutatedFiles;
    Random random;
    Cloner cloner;
    Document page;
    ArrayList<String> cssFiles;
    HashMap<String, StyleSheet> stylesheets;
    String preamble = "file:///Users/thomaswalsh/Documents/Workspace/Redecheck/testing/";
//    String[] tagsIgnore = { "A", "AREA", "B", "BLOCKQUOTE",
//            "BR", "CANVAS", "CENTER", "CSACTIONDICT", "CSSCRIPTDICT", "CUFON",
//            "CUFONTEXT", "DD", "EM", "EMBED", "FIELDSET", "FONT", "FORM",
//            "HEAD", "HR", "I", "LABEL", "LEGEND", "LINK", "MAP", "MENUMACHINE",
//            "META", "NOFRAMES", "NOSCRIPT", "OBJECT", "OPTGROUP", "OPTION",
//            "PARAM", "S", "SCRIPT", "SMALL", "SPAN", "STRIKE", "STRONG",
//            "STYLE", "TBODY", "TITLE", "TR", "TT", "U" };
    private String htmlContent;
    static Pattern bsGridPattern = Pattern.compile("col-[a-z]*-[0-9]*");
    static Pattern fGridPattern = Pattern.compile("([a-z]*-)*[0-9]");
    static Matcher m;
	static Matcher m2;
    ArrayList<Element> classCandidates, htmlCandidates;
    Random r;
    LoremIpsum loremIpsum;

    

    public HTMLMutator(String baseURL, String shorthand, HashMap<String, StyleSheet> stylesheets2, ArrayList<Element> classCandidates, ArrayList<Element> htmlCandidates, Document page, HashSet<String> usedClassesHTML, HashSet<String> usedIdsHTML, HashSet<String> usedTagsHTML, int mutantNumber) {
		// TODO Auto-generated constructor stub
    	this.baseURL = baseURL;
        this.shorthand = shorthand;
        this.mutantNumber = mutantNumber;
        usedClasses = usedClassesHTML;
        usedTags = usedTagsHTML;
        usedIds = usedIdsHTML;
        stylesheets = stylesheets2;
        this.classCandidates = classCandidates;
        this.htmlCandidates = htmlCandidates;
        random = new Random();
        this.page = page;
        
        cloner = new Cloner();
        r = new Random();
        loremIpsum = new LoremIpsum();
	}

    static boolean isGridSizingClass(String htmlClass) {
        m = bsGridPattern.matcher(htmlClass);
        m2 = fGridPattern.matcher(htmlClass);
        return m.matches() | m2.matches();
    }

    private ArrayList<String> getMutatableClasses(Element e) {
        ArrayList<String> classes = new ArrayList<>();
        for (String s : e.classNames()) {
//            if (isGridSizingClass(s)) {
                classes.add(s);
//            }
        }
        return classes;
    }

     public Element getElementToMutate(Document copy, Element e) {
         for (Element e2 : copy.getAllElements()) {
             if (e.cssSelector().equals(e2.cssSelector())) {
                 return e2;
             }
         }
         return null;
     }
     
     private void mutateContent(Document toMutate) {
    	 boolean managedToMutate = false;
    	 while(!managedToMutate) {
    		 // Shuffle the mutation locations/candidates
    		 Collections.shuffle(htmlCandidates);
    		 
    		 // Pick a candidate and retrieve the actual element from HTML document
    		 Element candidate = htmlCandidates.get(0);
    		 Element actualElement = getElementToMutate(toMutate, candidate);
    		 
    		 // Check we found an actual element to mutate
    		 if (actualElement != null) {
    			 String[] words = actualElement.ownText().split(" ");
    			 int numberOfWords = words.length;
    			 int incrementer;
    			 if (numberOfWords < 10) {
    				 
    				 incrementer = r.nextInt(5);
    			 } else {
    				 int size = numberOfWords/10;
    				 incrementer = r.nextInt(size);
    			 }
    			 boolean toggle = r.nextBoolean();
    			 if (toggle) {
    				 actualElement.text(actualElement.ownText() + " " + loremIpsum.getWords(incrementer, 3));
    				 managedToMutate = true;
    			 } else {
    				 int newNumWords = numberOfWords-incrementer;
    				 String newContent = "";
    				 int counter = 0;
    				 while (newContent.length() < newNumWords) {
    					 newContent = newContent + words[counter] + " ";
    					 counter++;
    				 }
    				 actualElement.text(newContent);
    				 managedToMutate = true;
    			 }
                 if (managedToMutate) {
                    writeMutantToFile(this.mutantNumber, "Changed content in " + actualElement.toString());
                 }
    			 
    		 }
    	 }
     }

    private void mutateClass(Document toMutate, int selector) {
        boolean managedToMutate = false;
        while(!managedToMutate) {
            Collections.shuffle(classCandidates);
            Element selection = classCandidates.get(0);
            Element actualElement = getElementToMutate(toMutate, selection);
            if (actualElement != null) {
                ArrayList<String> options = getMutatableClasses(selection);
                Collections.shuffle(options);
                String classSelection = options.get(0);

//                m = bsGridPattern.matcher(classSelection);
//                m2 = fGridPattern.matcher(classSelection);
//                String[] bsRanges = new String[]{"xs", "sm", "md", "lg"};
//                String[] fRanges = new String[]{"small", "medium", "large"};
//                boolean changeOrAdd = r.nextBoolean();
                if (selector == 6) {

                    int indexToRemove = random.nextInt(options.size());
                    String classString = "";
                    actualElement.removeClass(options.get(indexToRemove));

                    int rand = random.nextInt(usedClasses.size());
                    int i = 0;
                    for (String s : usedClasses) {
                        if ( i == rand ) {
                            actualElement.addClass(s);
                        }
                        i = i+1;
                    }

                    writeMutantToFile(this.mutantNumber, "Exchanged a class in element \n" + actualElement.toString());
                    managedToMutate = true;

                    // We're mutating an existing class
                    // Check if its a bootstrap one
//                    if (m.matches()) {
//                        String[] splits = classSelection.split("-");
//                        String range = splits[1];
//                        int num = Integer.valueOf(splits[2]);
//                        boolean toggle = r.nextBoolean();
//                        if (toggle) {
//                            // Mutate range
//                            int index = r.nextInt(bsRanges.length);
//                            range = bsRanges[index];
//                        } else {
//                            // Mutate number
//                            boolean plusMinus = r.nextBoolean();
//                            if (plusMinus)
//                                num++;
//                            else
//                                num--;
//                        }
//                        String newClass = "col-" + range + "-" + num;
//                        actualElement.removeClass(classSelection);
//                        actualElement.addClass(newClass);
//                        managedToMutate = true;
//                    } else if (m2.matches()) {
//                        try {
//                            String[] splits = classSelection.split("-");
//                            String range = splits[0];
//                            int num = Integer.valueOf(splits[1]);
//                            boolean toggle = r.nextBoolean();
//                            if (toggle) {
//                                // Mutate range
//                                int index = r.nextInt(fRanges.length);
//                                range = bsRanges[index];
//                            } else {
//                                // Mutate number
//                                boolean plusMinus = r.nextBoolean();
//                                if (plusMinus)
//                                    num++;
//                                else
//                                    num--;
//                            }
//                            String newClass = range + "-" + num;
//                            actualElement.removeClass(classSelection);
//                            actualElement.addClass(newClass);
//                            managedToMutate = true;
//                        } catch (Exception e) {
//
//                        }
//                    }
                    writeMutantToFile(this.mutantNumber, "Changed class in element " + actualElement.toString());

                } else if (selector == 4){
                    // We're adding a new class
                    int rand = random.nextInt(usedClasses.size());
                    int i = 0;
                    for (String s : usedClasses) {
                        if ( i == rand ) {
                            actualElement.addClass(s);
                        }
                        i = i+1;
                    }
                    writeMutantToFile(this.mutantNumber, "Added a class to element \n" + actualElement.toString());
                    managedToMutate = true;
                } else if (selector == 5) {
                    int indexToRemove = random.nextInt(options.size());
                    String classString = "";
                    actualElement.removeClass(options.get(indexToRemove));
                    writeMutantToFile(this.mutantNumber, "Removed a class from element \n" + actualElement.toString());
                    managedToMutate = true;
                }


            }
        }
    }

    public void writeNewHtml(int i, Document mutated) {
        Elements sss = mutated.getElementsByAttributeValue("rel", "stylesheet");
        for (Element s : sss) {
            if (!s.toString().contains("need")) {
                s.remove();
            }
        }
        for (String s : stylesheets.keySet()) {
            String[] splits = s.split("/");
            String actualName = splits[splits.length-1].replace(".css", "");
            String fileName = actualName + this.mutantNumber + ".css";
            mutated.head().append("<link href=\"" + "./resources/" + fileName + "\" rel=\"stylesheet\">");
        }

        PrintWriter output = null;
        try {
        	File theDir = new File(preamble.replace("file:///", "/") + shorthand + "/mutant" + i + "/");
        	boolean result = false;
            String dirName = preamble.replace("file:///", "/") + shorthand + "/mutant" + i + "/";
            if (!theDir.exists()) {
            	theDir.mkdirs();
            	result = true;
            }
            String fileName = "mutant" + i + ".html";

            File file = new File (dirName + fileName);
            output = new PrintWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        output.append(mutated.toString());
        output.close();
    }

	public void mutate(int selector) {
        Document toMutate = cloner.deepClone(this.page);

        if ((selector > 3) && (selector < 7)) {
            mutateClass(toMutate, selector);
        } else if (selector == 7) {
            mutateContent(toMutate);
        }
        CSSMutator.writeToFile(this.mutantNumber, this.stylesheets, this.shorthand);
	    writeNewHtml(this.mutantNumber, toMutate);
		
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
        } catch (Exception e) {
            System.out.println("HTML");
            e.printStackTrace();
        }


    }

}
