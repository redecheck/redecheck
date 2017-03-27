package shef.utils;

import org.apache.commons.io.FileUtils;
import shef.main.Utils;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

public class ResultProcessor {

//	String[] webpages = new String[] {"aftrnoon.com", "annettescreations.net", "ashtonsnook.com", "bittorrent.com", "coursera.com", "denondj.com", "getbootstrap.com", "issta.cispa", "namemesh.com", "paydemand.com", "rebeccamade.com", "reserve.com", "responsiveprocess.com", "shield.com", "teamtreehouse.com"};

	int[][] rq1results;

	int[][] rq2results;
	Double[][] rq3results;
//	static String preamble = "/Users/thomaswalsh/Documents/Workspace/Redecheck/testing/";
	static String preamble = "/Users/thomaswalsh/Documents/PhD/Papers/main-journal-paper-data/";
	static String target = "/Users/thomaswalsh/Documents/PhD/Code-Projects/Redecheck/target/";
	static String redecheck = "/Users/thomaswalsh/Documents/PhD/Code-Projects/Redecheck/";
	static String redecheckicst = "/Users/thomaswalsh/Documents/PhD/main-icst/";
	static String githubio = "/Users/thomaswalsh/Documents/PhD/Websites/redecheck-org/";
	static String faultExamples = "/Users/thomaswalsh/Documents/PhD/Resources/fault-examples/";
	ArrayList<File> allMutants;
	ArrayList<File> mutantsForAnalysis;
	ArrayList<File> nonDetected;
	ArrayList<File> falsePositives;
	HashMap<String, ArrayList<File>> groupedByPageTrue;
    HashMap<String, ArrayList<File>> groupedByPageFalse;
	HashMap<String, ArrayList<File>> groupedByOperator;
	ArrayList<String> operatorList;
	static NumberFormat formatter;

	String[] webpages = new String[] {
			"3-Minute-Journal",
			"AccountKiller",
			"AirBnb",
			"BugMeNot",
			"CloudConvert",
			"Consumer-Reports",
			"Covered-Calendar",
			"Days-Old",
			"Dictation",
			"Duolingo",
			"Honey",
			"HotelWifiTest",
			"Mailinator",
			"MidwayMeetup",
			"Ninite",
			"Pdf-Escape",
			"PepFeed",
			"Pocket",
			"RainyMood",
			"RunPee",
			"StumbleUpon",
			"TopDocumentary",
			"UserSearch",
			"WhatShouldIReadNext",
			"WillMyPhoneWork",
			"ZeroDollarMovies"};
	String[] urls = new String[] {
			"www.3minutejournal.com",
			"www.accountkiller.com/en",
			"www.airbnb.com",
			"bugmenot.com",
			"cloudconvert.com",
			"consumerreports.org",
			"www.coveredcalendar.com",
			"www.daysold.com",
			"dictation.io",
			"www.duolingo.com",
			"www.joinhoney.com/install",
			"www.hotelwifitest.com",
			"www.mailinator.com",
			"www.midwaymeetup.com",
			"ninite.com",
			"www.pdfescape.com",
			"www.pepfeed.com",
			"getpocket.com",
			"rainymood.com",
			"runpee.com",
			"www.stumbleupon.com",
			"topdocumentaryfilms.com",
			"usersearch.org",
			"www.whatshouldireadnext.com/search",
			"willmyphonework.net",
			"zerodollarmovies.com"};

	String[] commands = new String[] {
			"\\threeminutejournalplain",
			"\\accountkillerplain",
			"\\airbnbplain",
			"\\bugmenotplain",
			"\\cloudconvertplain",
			"\\consumerreportsplain",
			"\\coveredcalendarplain",
			"\\daysoldplain",
			"\\dictationplain",
			"\\duolingoplain",
			"\\honeyplain",
			"\\hotelwifitestplain",
			"\\mailinatorplain",
			"\\midwaymeetupplain",
			"\\niniteplain",
			"\\pdfescapeplain",
			"\\pepfeedplain",
			"\\getpocketplain",
			"\\rainymoodplain",
			"\\runpeeplain",
			"\\stumbleuponplain",
			"\\topdocumentaryfilmsplain",
			"\\usersearchplain",
			"\\whatshouldireadnextplain",
			"\\willmyphoneworkplain",
			"\\zerodollarmoviesplain"};
	
	public ResultProcessor() {
		allMutants = new ArrayList<>();
		mutantsForAnalysis = new ArrayList<>();
		nonDetected = new ArrayList<>();
		falsePositives = new ArrayList<>();

		groupedByPageTrue = new HashMap<>();
        groupedByPageFalse = new HashMap<>();
		for (String wp : webpages) {
			groupedByPageTrue.put(wp, new ArrayList<File>());
            groupedByPageFalse.put(wp, new ArrayList<File>());
		}
		operatorList = new ArrayList<>();
		operatorList.add("BREAKPOINT MUTATION");
		operatorList.add("RULE-VALUE");
		operatorList.add("MQ-EXPRESSION");
		operatorList.add("CLASS-ADDITION");
		operatorList.add("CLASS-DELETION");
		operatorList.add("CLASS-EXCHANGE");
		operatorList.add("HTML-CONTENT");
		operatorList.add("RULE-UNIT");

		groupedByOperator = new HashMap<>();
		for (String s : operatorList) {
			groupedByOperator.put(s, new ArrayList<File>());
		}
		formatter = new DecimalFormat("#0.00");
	}
	
	
	private void processResults() {
		for (String webpage : webpages) {
			ArrayList<File> mutantFolders = getMutantFolders(webpage);
			for (File mf : mutantFolders) {
				allMutants.add(mf);
				boolean rlgFound = changesDetectedByRlg(mf, "report-uniformBP-60-true.txt");
				if (rlgFound) {
					mutantsForAnalysis.add(mf);
                    groupedByPageTrue.get(webpage).add(mf);
				} else {
					nonDetected.add(mf);
                    groupedByPageFalse.get(webpage).add(mf);
				}

//				String mutationType = getTypeOfMutant(mf);
//				groupedByOperator.get(mutationType).add(mf);
			}
		}

//        for (String s : groupedByPageFalse.keySet()) {
//            System.out.println(s);
//            System.out.println(groupedByPageFalse.get(s).size());
//        }
//		for (File f : groupedByPageTrue.get("denondj.com")) {
//			System.out.println(f);
//		}
//		System.out.println("REDECHECK FOUND " + mutantsForAnalysis.size() + " POTENTIAL ISSUES");
//		System.out.println(nonDetected.size() + " MUTANTS CAUSED NO RLG CHANGES");
//		System.out.println("THERE WERE " + allMutants.size() + " TOTAL MUTANTS");
	}

	private void pickRandomSet() {
		Random random = new Random();
		ArrayList<File> randomSet = new ArrayList<>();
		String s2 = "";
		String s3 = "";
		String s4 = "";
        String s5 = "";

		int counter = 0;
        int uniqueID = 1;
		for (String s : groupedByPageTrue.keySet()) {
			ArrayList<File> truefiles = groupedByPageTrue.get(s);
            ArrayList<File> falseFiles = groupedByPageFalse.get(s);

            for (int i = 1; i <=4; i++) {
				boolean pickedOne = false;
				boolean trueOrFalse;
				while (!pickedOne) {
                    ArrayList<File> toShuffle;
					// Pick an operator
					int opNum = random.nextInt(8);
					String opString = operatorList.get(opNum);

					int trueFalseNum = random.nextInt(50);
					if (trueFalseNum > 1) {
						trueOrFalse = true;
                        toShuffle = truefiles;
					} else {
						trueOrFalse = false;
                        toShuffle = falseFiles;
					}
//					System.out.println(s + " " + i);
//					System.out.println(groupedByOperator.get(opString).size());
					if (groupedByOperator.get(opString).size() < 7) {

						Collections.shuffle(toShuffle);
						for (File f : toShuffle) {
							if (getTypeOfMutant(f).equals(opString)) {
								System.out.println("MATCHED OPSTRING");
								if (changesDetectedByRlg(f, "report-uniformBP-60-true.txt") == trueOrFalse) {
									System.out.println("MATCHED REPORT");
									File f2 = toShuffle.remove(0);
									randomSet.add(f2);
									groupedByOperator.get(opString).add(f2);
									pickedOne = true;
									if (trueOrFalse == false) {
										counter++;
									}
                                    String[] splits = f.toString().split("/");
                                    String toAdd = splits[splits.length-1];
                                    s2 += f.toString().replace(preamble,"") + "/" + toAdd + "\n";
                                    s3 += f.toString().replace(preamble,"").replace(toAdd,"") + "index/index" + "\n";
                                    if (uniqueID < 10) {
                                        s4 += "mutant000" + uniqueID + "\n";
                                    } else {
                                        s4 += "mutant00" + uniqueID + "\n";
                                    }
                                    s5 += opString + "\n";
                                    uniqueID++;
									break;
								}
							}
						}
					} else {
						System.out.println(groupedByOperator.get(opString).size());
					}
				}
			}
			System.out.println("GOT FOUR");
		}

		writeToFile(s2, target, "newmutatedwebsites.txt");
		writeToFile(s3, target, "neworiginalwebsites.txt");
		writeToFile(s4, target, "newuniqueIDs.txt");
        writeToFile(s5, target, "newmutant-types.txt");
	}

	public static void writeToFile(String content, String targetDir, String fileName) {
		PrintWriter output = null;
		String outFolder = targetDir;
		if (targetDir.charAt(targetDir.length()-1) != '/') {
			outFolder = targetDir + "/";
		}
		try {
			FileUtils.forceMkdir(new File(outFolder));
			String full = outFolder + fileName;
			output = new PrintWriter(full);
//			FileUtils.forceMkdir(new File(outFolder+fileName));
			output.append(content);
//			output.close();

		} catch (Exception e) {
			System.out.println("Issue with " + outFolder+fileName);
		} finally {
			output.close();
		}
	}

	private ArrayList<File> readInSetOfMutants(String fileName) {
        ArrayList<File> files = new ArrayList<>();
		String mutants;

		// This will reference one line at a time
		String line = null;

		try {
//			String current = new java.io.File( "." ).getCanonicalPath();
			// FileReader reads text files in the default encoding.
			String fullFN = redecheck + fileName;
			FileReader fileReader = new FileReader(fullFN);
			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while((line = bufferedReader.readLine()) != null) {
				files.add(new File(redecheck + "/reports/" + line));
			}

			// Always close files.
			bufferedReader.close();
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		}
        return files;
	}

    private void writeRQ1and2Data(ArrayList<File> files) {
        String types = "";
		String xpertResults = "";
		int counter = 1;
        for (File f : files) {
//            System.out.println(f);
//            System.out.println(getTypeOfMutant(f));
            types += getTypeOfMutant(f) + "\n";
			xpertResults += changedDetectedByXpert(f) + "\n";
			counter++;
        }
        writeToFile(types, target, "../manual-analysis/mutantTypes.txt");
		writeToFile(xpertResults, target, "../manual-analysis/xpert-results.txt");
    }


	private static boolean changesDetectedByRlg(File mf, String fileName) {
		String[] splits = mf.toString().split("/");
		String mutantString = splits[splits.length-1];
		try {
//			System.out.println(mf.getAbsolutePath() + "/" + fileName);
			String contents = Utils.readFile(mf.getAbsolutePath() + "/" + fileName).trim();
			if (contents.equals("")) {
				System.out.println("Couldn't read file for " + mf + " " + fileName);
			} else {
				boolean rlgFound = !contents.contains("NO ERRORS DETECTED");
//			System.out.println(rlgFound);
				return rlgFound;
			}
		} catch (Exception e ) {

		}
		return false;
	}

    private static int getNumDiffDoms(File mf) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(mf.getAbsolutePath() + "/dom-diff-result.txt"));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            int count = 0;
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                count ++;
                line = br.readLine();
            }
            br.close();
            return count;
        } catch (Exception e) {
			System.out.println("No Dom Diff for " + mf.getAbsolutePath());
        }
        return 0;
    }

//	private static String getTimeData(File mf, String type) {
//		String result = "";
//		try {
////			String trimmedFP = mf.getAbsolutePath().substring(0, mf.getAbsolutePath().lastIndexOf("/"));
//			BufferedReader br = new BufferedReader(new FileReader(mf.getAbsolutePath() + "/timings-" + type + ".txt"));
////			System.out.println(br.toString());
//			StringBuilder sb = new StringBuilder();
//			String line = br.readLine();
//			int count = 0;
//			while (line != null) {
//				sb.append(line + ",");
//				count ++;
//				line = br.readLine();
//			}
//			br.close();
//			return sb.toString();
//		} catch (Exception e) {
////			e.printStackTrace();
//		}
//		return "";
//	}

	private static String getMultiExecutionTime(String webpage, int i) {
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(redecheck+"times/"+webpage + "/timings" + i + ".csv"));

			result += br.readLine();

			br.close();
			return result.substring(0, result.length()-1);
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return "";
	}

	private static String getExecutionTime(File f) {
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath() + "/timings.csv"));

			result += br.readLine();

			br.close();
			return result.substring(0, result.length()-1);
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return "";
	}

	private static double getTotalTime(File mf, String type) {
		double result = 0;
		try {
//			String trimmedFP = mf.getAbsolutePath().substring(0, mf.getAbsolutePath().lastIndexOf("/"));
			BufferedReader br = new BufferedReader(new FileReader(mf.getAbsolutePath() +  "/timings-" + type + ".txt"));
//			System.out.println(br.toString());
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			int count = 0;
			while (line != null) {
				if (count == 5)
					result = result + Double.valueOf(line);
				count ++;
				line = br.readLine();
			}
			br.close();
			return result;
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return result;
	}

	private static char changedDetectedByXpert(File mf) {
		String[] splits = mf.toString().split("/");
		String mutantString = splits[splits.length-1];
		String webPage = splits[splits.length-3];
		try {
			String fileName = preamble +webPage + "/" + mutantString + "/xpert-result.txt";
			System.out.println(fileName);
			String contents = Utils.readFile(fileName).trim();
            if (!contents.equals("")) {
                return 'T';
            } else {
                return 'F';
            }
		} catch (Exception e ) {

		}
		return 'F';
	}

	private String getMutantDescription(File mf) {
		String[] splits = mf.toString().split("/");
		String mutantString = splits[splits.length-1];
		try {
			String desc = Utils.readFile(mf.getAbsolutePath() + "/" + mutantString + ".txt").trim();
//			System.out.println(rlgFound);
			return desc;
		} catch (Exception e ) {

		}
		return "";
	}

	private static String getTypeOfMutant(File mf) {
		String type = "";
		try {
			String[] splits = mf.getAbsolutePath().split("/");
			String mutantString = splits[splits.length-1];
			String fullFilePath = mf.getAbsolutePath() + ".txt";
//			System.out.println(fullFilePath);
			String content = Utils.readFile(fullFilePath).trim();
			if (content.contains("value")) {
				type = "BREAKPOINT MUTATION";
			} else if (content.contains("term")){
				type = "RULE-VALUE";
			} else if (content.contains("UNIT")) {
				type = "RULE-UNIT";
			} else if (content.contains("expression")) {
				type = "MQ-EXPRESSION";
			} else if (content.contains("Added a class")) {
				type = "CLASS-ADDITION";
			} else if (content.contains("Removed")) {
				type = "CLASS-DELETION";
			} else if (content.contains("Changed class")) {
				type = "CLASS-EXCHANGE";
			} else if (content.contains("content")) {
				type = "HTML-CONTENT";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return type;
	}

	private static Double getTimeFromFile(File mf, String type) {
		String contents = "";
		try {
			contents = Utils.readFile(mf.getAbsolutePath() + "/" + type + ".txt").trim();
			return Double.valueOf(contents);
		} catch (Exception e) {
			System.out.println(mf.getAbsolutePath() + "/" + type + ".txt");
		}
		return 0.0;
	}

	private static int getDomsUsed(File mf, String config) {
		String contents = "";
		try {
			contents = Utils.readFile(mf.getAbsolutePath() + "/doms-" + config + ".txt").trim();
			return Integer.valueOf(contents);
		} catch (Exception e) {
			System.out.println("Error with " + mf.getAbsolutePath() + "/doms-" + config + ".txt");
		}
		return 0;
	}


	private boolean getDomDiff(File mf) {
		String classification;
		try {
			classification = Utils.readFile(mf.getAbsolutePath() + "/domdiff.txt").trim();
			return classification.contains("TRUE");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}


	private static ArrayList<File> getMutantFolders(String webpage) {
		ArrayList<File> folders = new ArrayList<File>();
		
		File directory = new File(preamble + webpage);

	    // get all the files from a directory
	    File[] fList = directory.listFiles();
	    for (File file : fList) {
	    	if ( (file.isDirectory()) && (file.getName().contains("mutant")) ) {
	            folders.add(file);
	        }
	    }
	    return folders;
	}

    private static void setUpBuckets(HashMap<int[], int[]> buckets, int[] ranges) {
        for (int i = 0; i < ranges.length; i++) {
            int value = ranges[i];
            if (i == 0) {
//                buckets.put(new int[] {0, value}, new int[3]);
            } else {
                buckets.put(new int[] {ranges[i-1]+1, ranges[i]}, new int[3]);
            }
        }
//        System.out.println(buckets.keySet());
    }

    private static int[] getBucket(HashMap<int[], int[]> buckets, int value) {
        for (int[] range : buckets.keySet()) {
            if ( (value >= range[0]) && (value <= range[1])) {
                return range;
            }
        }
        return new int[] {};
    }

	private static void writeTimesAndDomsToFile(String[] webpages, int numMutants) {
		String domsResults = "";
		String initialDomResults = "";

		String timeResults = "";
//		String[] sampleTechniques = new String[] {"uniform", "uniformBP"};
//		int[] stepSizes = new int[] {10, 20, 40, 60, 80, 100};
//		boolean[] binarySearching = new boolean[] {false, true};
////		boolean bs = binarySearching[1];
//		for (String wp : webpages) {
//			for (int mutantNum = 1; mutantNum <= numMutants; mutantNum++) {
//				for (String st : sampleTechniques) {
//					for (boolean bs : binarySearching) {
//						if (getNumDiffDoms(new File(preamble + wp + "/mutant" + mutantNum)) != 0) {
//
//							if (st.equals("breakpoint")) {
//								int domsUsed = getDomsUsed(new File(preamble + wp + "/mutant" + mutantNum), st + "-" + 0 + "-" + bs);
//								int initialUsed = getDomsUsed(new File(preamble + wp + "/mutant" + mutantNum), "initial-" + st + "-" + 0 + "-" + bs);
//								domsResults += wp + "," + mutantNum + "," + st + "," + 0 + "," + bs + "," + domsUsed + "\n";
//								if (initialUsed != 0) {
//									initialDomResults += wp + "," + mutantNum + "," + st + "," + 0 + "," + bs + "," + initialUsed + "\n";
//								}
//							} else {
//								for (int ss : stepSizes) {
//									// Handle DOMS
//									int domsUsed = getDomsUsed(new File(preamble + wp + "/mutant" + mutantNum), st + "-" + ss + "-" + bs);
//									int initialUsed = getDomsUsed(new File(preamble + wp + "/mutant" + mutantNum), "initial-" + st + "-" + ss + "-" + bs);
//									domsResults += wp + "," + mutantNum + "," + st + "," + ss + "," + bs + "," + domsUsed + "\n";
//									if (initialUsed != 0) {
//										initialDomResults += wp + "," + mutantNum + "," + st + "," + ss + "," + bs + "," + initialUsed + "\n";
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//		writeToFile(domsResults, preamble, "generated-tables/domsUsed-filtered.csv");
//		writeToFile(initialDomResults, preamble, "generated-tables/initialDomsUsed-filtered.csv");

		for (String wp : webpages) {
			for (int mutantNum = 1; mutantNum <= numMutants; mutantNum++) {
				File file = new File(preamble + wp + "/mutant" + mutantNum);
				getTotalTime(file, "exhaustive");
			}
		}

	}

	private static void generateStepSizeResults(String[] pages) {
		int mutantsToProcess = 30;
		String results = "";
		String resultsBP = "";

		int[] mutants = new int[mutantsToProcess];
		String[] sampleTechniques = new String[] {"uniform", "uniformBP"};
		int[] stepSizes = new int[] {10, 20, 40, 60, 80, 100};
//
		for (int i = 1; i < mutantsToProcess; i++) {
			mutants[i] = i;
		}
		int[][] uniformArray = new int[pages.length][stepSizes.length];
		int[][] uniformBPArray = new int[pages.length][stepSizes.length];
		int[][] resultsArray = new int[pages.length][stepSizes.length*2];
		int pageCounter=0;
		int ssCounter=0;
		int ssCounter2;
		for (String wp : pages) {
//			System.out.println(wp);
			for (int mutantNum = 1; mutantNum <= mutantsToProcess; mutantNum++) {
				for (String st : sampleTechniques) {
					ssCounter=0;
					ssCounter2 = 0;
					for (int ss : stepSizes) {
						String resultsFileName = preamble + wp + "/mutant" + mutantNum + "/report-" + st + "-" + ss + "-true.txt";
						String domDiffFileName = preamble + wp + "/mutant" + mutantNum;
						try {
							File tester = new File(resultsFileName);
							File domDiff = new File(domDiffFileName);
							String contents = Utils.readFile(resultsFileName);
							boolean errorsReported = !contents.contains("NO ERRORS DETECTED");
							boolean actualMutation = getNumDiffDoms(domDiff) != 0;
							if (actualMutation) {
								if (errorsReported) {
									if (st.equals("uniform")) {
										uniformArray[pageCounter][ssCounter]++;
										resultsArray[pageCounter][ssCounter2]++;
									} else {
										uniformBPArray[pageCounter][ssCounter]++;
										resultsArray[pageCounter][ssCounter2+1]++;
									}
								}
							}
						} catch (IOException e) {
							System.out.println("Didn't read results for " + resultsFileName);
						}
						ssCounter++;
						ssCounter2+=2;
					}
				}
			}
			pageCounter++;
		}

		String uniformTable = "";

		for (int x =0; x< uniformArray.length; x++) {
			uniformTable += (pages[x] + ", ");
			for (int y = 0; y < uniformArray[x].length; y++) {
				if (y != uniformArray[x].length-1) {
					uniformTable += (uniformArray[x][y] + ", ");
				} else {
					uniformTable += (uniformArray[x][y]);
				}
			}
			uniformTable += "\n";
		}
//		System.out.println(uniformTable);

//		writeToFile(uniformTable, preamble, "uniform-results-table.csv");

		String uniformBPTable = "";

		for (int x =0; x< uniformBPArray.length; x++) {
			uniformBPTable += (pages[x] + ", ");
			for (int y = 0; y < uniformBPArray[x].length; y++) {
				if (y != uniformBPArray[x].length-1) {
					uniformBPTable += uniformBPArray[x][y] + ", ";
				} else {
					uniformBPTable += uniformBPArray[x][y];
				}
			}
			uniformBPTable += "\n";
		}

//		System.out.println(uniformBPTable);

		String resultsTable = "";

		for (int x =0; x< resultsArray.length; x++) {
			resultsTable += (pages[x] + " & ");
			for (int y = 0; y < resultsArray[x].length; y++) {
				if (y != resultsArray[x].length-1) {
					resultsTable += resultsArray[x][y] + " & ";
				} else {
					resultsTable += resultsArray[x][y];
				}
			}
			resultsTable += "\n";
		}
		System.out.println(resultsTable);

//		writeToFile(uniformBPTable, preamble, "uniformBP-results-table.csv");
	}

	private static void processAllMutants(String[] pages) {
        HashMap<int[], int[]> buckets = new HashMap<>();
        int[] ranges = new int[] {0,1,3,5,10,50,100,300,500,1001};
		int[] stepSizes = new int[] {10,20,40,60,80,100};
		int mutantsToProcess = 30;
		int[] mutants = new int[mutantsToProcess];
		for (int i = 1; i < mutantsToProcess; i++) {
			mutants[i] = i;
		}
        setUpBuckets(buckets, ranges);
		String rq4Result = "";
        String rq3Result = "";
		String rq3Recalls = "";
		String times = "";
		for (String wp : pages) {
			ArrayList<File> folders = getMutantFolders(wp);

			int[] pageScores = new int[8];
			int counter = 0;
			for (int i = 0; i < mutantsToProcess; i++) {

				File f = folders.get(i);
				try {
					String[] split = f.toString().split("mutant");
					int mutantNumber = Integer.valueOf(split[1]);
					if (mutantNumber <= mutantsToProcess) {
						int numberOfDifferentDoms = getNumDiffDoms(f);
						System.out.println(numberOfDifferentDoms);
						if (numberOfDifferentDoms > 0) {
							System.out.println(f);
							int[] mutantScores = new int[3];
							boolean uniformWithSearch = changesDetectedByRlg(f, "report-uniform-60-true.txt");
							boolean uniformNoSearch = changesDetectedByRlg(f, "report-uniform-60-false.txt");
							boolean uniformBPWithSearch = changesDetectedByRlg(f, "report-uniformBP-60-true.txt");
							boolean uniformBPNoSearch = changesDetectedByRlg(f, "report-uniformBP-60-false.txt");
							boolean breakpointWithSearch = changesDetectedByRlg(f, "report-breakpoint-0-true.txt");
							boolean breakpointNoSearch = changesDetectedByRlg(f, "report-breakpoint-0-false.txt");
							boolean exhaustive = changesDetectedByRlg(f, "report-exhaustive-0-true.txt");
							boolean detectedByXpert = (changedDetectedByXpert(f) == 'T');

							int[] range = getBucket(buckets, numberOfDifferentDoms);
							buckets.get(range)[0]++;
							if (detectedByXpert) {
								buckets.get(range)[2]++;
							}
							if (uniformBPWithSearch) {
								buckets.get(range)[1]++;
							}

							// Handle RQ3 data storage
							if (uniformBPWithSearch)
								mutantScores[0]++;
							if (detectedByXpert)
								mutantScores[1]++;

							// Handle RQ4 data storage
							pageScores[0]++;
							if (uniformBPWithSearch)
								pageScores[1]++;
							if (uniformBPNoSearch)
								pageScores[2]++;
							if (uniformWithSearch)
								pageScores[3]++;
							if (uniformNoSearch)
								pageScores[4]++;
							if (breakpointWithSearch)
								pageScores[5]++;
							if (breakpointNoSearch)
								pageScores[6]++;
							if (exhaustive)
								pageScores[7]++;
							counter++;
						}
					}
				} catch(Exception e){
					System.out.println("Problem with " + f);
					e.printStackTrace();
				}

			}

			rq4Result += wp + " , " + pageScores[0] + "," + pageScores[1] + "," + pageScores[2] + "," + pageScores[3] + "," + pageScores[4] + "," + pageScores[5] + "," + pageScores[6] + "," + pageScores[7] + " \n";

		}

		for (int i = 1; i < ranges.length; i++) {
			int[] range = getBucket(buckets, ranges[i]);
			try {
				int[] scores = buckets.get(range);
				double rRecall = (scores[1]*1.0) / scores[0];
				double xRecall = (scores[2]*1.0) / scores[0];

				String rangeString = "";
				if (range[0] == 1) {
					rangeString = "1";
				} else if (range[1] == 1001) {
					rangeString = range[0]+"+";
				} else {
					rangeString = range[0]+"-"+range[1];
				}
				rq3Recalls += rangeString + ",ReDeCheck," + rRecall + " \n";
				rq3Recalls += rangeString + ",XPERT," + xRecall + " \n";
				rq3Result += rangeString + "," + scores[0] + "," + scores[1] +  "," + scores[2] + " \n";
			} catch (Exception e) {
				System.out.println("Failed");
			}
        }
		writeToFile(rq3Result, preamble, "generated-tables/fullrq3counts.csv");
		writeToFile(rq3Recalls, preamble, "generated-tables/fullrq3recalls.csv");
		writeToFile(rq4Result, preamble, "generated-tables/fullrq4results.csv");
	}
	
	public static void main(String[] args) throws IOException {
		ResultProcessor rp = new ResultProcessor();
//		rp.getInconsistencyResults();
		rp.writeArchiveWebpages();
	}

	public void getInconsistencyResults() {


		final int[] SPOT_CHECK_WIDTHS = new int[] {320, 375, 384, 414, 480, 600, 768, 1024, 1280};
		ArrayList<File> files = readInSetOfMutants( "/src/main/java/icst-websites.txt");
		String timeData = "";
		String multiTimeData = "";
		String subjectData = "";
		int totalFailures = 0;
		int totalDistinctRanges = 0;
		int totalTestRanges = 0;


		for (File f : files) {
			File mostRecentRun = lastFileModified(f.getAbsolutePath()+"");
			if (mostRecentRun == null) {
				System.out.println("Boo on " + f.getAbsolutePath());
			} else {
				String[] splits = f.toString().split("/");
				String webpage = splits[splits.length - 1];
				String command = commands[files.indexOf(f)];

				// Put all thirty iterations of timing data together
				for (int i = 1; i <= 30; i++) {
					multiTimeData += webpage + "," + i + "," + getMultiExecutionTime(webpage, i) + "\n";
				}

				int errorCount = getErrorCount(mostRecentRun);
//
				String distinctFailures = getActualFaults(mostRecentRun);
				try {
					totalFailures += Integer.valueOf(distinctFailures);
					if (errorCount != -1) {
						ArrayList<Integer> tpIndexes = new ArrayList<>();
						String classificationString = getClassification(mostRecentRun, tpIndexes);
//						ArrayList<int[]> ranges = getFailuresFromFile(mostRecentRun);
//						int totalRanges = ranges.size();
//						totalDistinctRanges += totalRanges;

						// Print out main RQ1 results row for table
//						System.out.println(command + classificationString + " & " + totalRanges + " & " + distinctFailures + " \\\\");
					}
				} catch (NumberFormatException nfe) {
					System.out.println(mostRecentRun.getAbsolutePath());
				}
			}
		}
		System.out.println("{\\sc Total}         &  &  &  &  &  &  &  &  &  &  &  &  &  &  & & " + totalDistinctRanges + " & " + totalFailures + "\\\\");
//		writeToFile(multiTimeData, main+"time-processing/", "timing-data-issta-rerun.csv");
	}

	private String getRQ2Result(HashSet<String> ranges, int[] scw, String webpage) {
		String result = "";
		for (String r : ranges) {
			result += webpage;
			String[] splits = r.split(" - ");
			int min = Integer.parseInt(splits[0]);
			int max = Integer.parseInt(splits[1]);
			for (int w : scw) {
				if (w >= min && w <= max) {
					result += ", T";
				} else {
					result += ", F";
				}
			}
			result += "\n";
		}
		return result;
	}

	private ArrayList<String> groupFailureRanges(HashSet<String> ranges) {
		ArrayList<String> grouped = new ArrayList<>();

		for (String r : ranges) {
//			System.out.println(r);
			String[] splits = r.split(" - ");
			int rMin = Integer.parseInt(splits[0]);
			int rMax = Integer.parseInt(splits[1]);
			String match = "";
			ArrayList<String> groupedClone = (ArrayList<String>) grouped.clone();
//			for (String g : grouped) {
			while (groupedClone.size() > 0) {
				String g = groupedClone.remove(0);
//				System.out.println(g);
				if (match.equals("")) {
					splits = g.split(" - ");
					int gMin = Integer.parseInt(splits[0]);
					int gMax = Integer.parseInt(splits[1]);
//					System.out.println("Comparing " + r + " and " + g);
					if (rMin <= gMax && gMin <= rMax) {
						// We've found a match
						// x1 <= y2 && y1 <= x2
//						System.out.println("Overlap between " + r + " and " + g);
						match = g;
						grouped.remove(match);
						if (rMin < gMin) {
							grouped.add(gMin + " - " + rMax);
						} else {
							grouped.add(rMin + " - " + gMax);
						}
//						break;

					}
				}
			}

			if (match.equals("")) {
//				System.out.println("Adding " + r);
				grouped.add(r);
			} else {
				// Remove the original match
//				grouped.remove(match);
				// Add in the new value, representing the overlap between the two

			}
		}
		return grouped;
	}

	public void writeArchiveWebpages() throws IOException {
		ArrayList<File> files = readInSetOfMutants( "/src/main/java/icst-websites.txt");
		String[] types = new String[] {"Element Collision","Element Protrusion", "Viewport Protrusion", "Small-Range","Wrapping"};
		String[] classes = new String[] {"TP", "FP", "NOI"};
		HashMap<String, String> classificationMap = new HashMap<>();
		classificationMap.put("TP", "True Positives");
		classificationMap.put("FP", "False Positives");
		classificationMap.put("NOI", "Non-Observable Issues");
		HashMap<String, HashMap<String, ArrayList<String>>> groupedResults = new HashMap<>();

		for (String c : classes) {
			groupedResults.put(c, new HashMap<>());
			for (String t : types) {
				HashMap<String, ArrayList<String>> hm = groupedResults.get(c);
				hm.put(t, new ArrayList<>());
			}
		}

		int totalCount = 1;
		LocalDateTime now = LocalDateTime.now();
		int year = now.getYear();
		int month = now.getMonthValue();
		int day = now.getDayOfMonth();
		for (File f : files) {
			File mostRecentRun = lastFileModified(f.getAbsolutePath() + "");
			String webpage = this.webpages[files.indexOf(f)];

			// Write out results into webpage for Jekyll
			int errorCount = getErrorCount(mostRecentRun);
			String[] classifications = getClassifications(mostRecentRun, errorCount);
			String[] categories = getCategories(mostRecentRun, errorCount);
			String[] reasons = getReasons(mostRecentRun, errorCount);
			String[] distinctRLFMapping = getDistinctRLFMapping(mostRecentRun, errorCount);
			ArrayList<int[]> bounds = getFailureBounds(mostRecentRun);
			for (int i = 1; i <= errorCount; i++) {
				String category = categories[i-1];
				String classification = classifications[i-1];
				String jekyllCode = "---\nlayout: post\ntitle: \"" + webpage + " Failure " + i + "\"\npermalink: " + webpage + "-failure-" + i + "\n---\n";
				if (classification.equals("TP")) {
					jekyllCode += "Below is a summary of the failure report produced by the tool. If it has been classified as a *true positive (TP)*, then further information about the distinct responsive layout failure is available if you scroll down the page.\n\n";
					jekyllCode += "| **Report Type** | **Distinct RLF** | **Viewport Range** | **Classification** | **Reason** |\n";
				} else {
					jekyllCode += "| **Report Type** | **Viewport Range** | **Classification** | **Reason** |\n";
				}
				String tableRow =   generateTableRow(totalCount, i, mostRecentRun, webpage, classifications, categories, reasons, bounds, distinctRLFMapping, false, classification.equals("TP"));
				String fullTableRow = generateTableRow(totalCount, i, mostRecentRun, webpage, classifications, categories, reasons, bounds, distinctRLFMapping, true, classification.equals("TP"));
				jekyllCode += tableRow;

				if (classification.equals("TP")) {
					try {
						String distinctContent = FileUtils.readFileToString(new File(redecheck + "reports/distinct/" + distinctRLFMapping[i - 1] + ".txt"));
						jekyllCode += "\n\n## About Distinct RLF " + distinctRLFMapping[i-1] +" {#About-drlf}\n\n" + distinctContent;
					} catch (FileNotFoundException fnfe) {

					}
				}

				try {
					groupedResults.get(classification).get(category).add(fullTableRow);
					writeToFile(jekyllCode, githubio + "/_posts", year + "-" + month + "-" + day + "-" + f.getName() + "-failure-" + i + ".markdown");
					totalCount++;
				} catch(Exception e ) {
					e.printStackTrace();
				}
			}
		}
		String archiveString = "---\n" +
				"layout: page\n" +
				"title: Results Archive\n" +
				"permalink: /results-archive/\n" +
				"---\n\n";

		String introduction = FileUtils.readFileToString(new File(redecheck+ "reports/distinct/intro.txt"));

		archiveString += introduction + "\n\n";

		for (String classification : classes) {
			archiveString += "\n### " + classificationMap.get(classification) + "### {#" + classification + "}\n\n";
			if (classification.equals("TP")) {
				archiveString += "| **Report Type** | **Web Page** | **Distinct RLF** | **Viewport Range** | **Classification** | **Reason** |\n";
			} else {
				archiveString += "| **Report Type** | **Web Page** | **Viewport Range** | **Classification** | **Reason** |\n";
			}
			HashMap<String, ArrayList<String>> hm = groupedResults.get(classification);
			for (String key : types) {
				for (String row : hm.get(key)) {
					archiveString += row + "\n";
				}
			}
		}

		writeToFile(archiveString, githubio, "results-archive.md");
	}



	private static String generateTableRow(int count, int i, File mostRecentRun, String webpage, String[] classifications, String[] categories, String[] reasons, ArrayList<int[]> bounds, String[] distinctRLFMapping, boolean full, boolean tp) {
//		System.out.println(totalCount + " " + i);
		String row = "";
		File ssFile = new File(mostRecentRun + "/fault" + i);
		String imageName = ssFile.listFiles()[0].getName();
		int[] bs = bounds.get(i-1);

		if (full) {
			if (tp) {
				row += "| " + categories[i - 1] + "| " + webpage + " | [" + distinctRLFMapping[i - 1] + "](" + "../" + webpage + "-failure-" + i + ".html#About-drlf) | " + bs[0] + "px-" + bs[1] + "px | " + classifications[i - 1] + " | " + reasons[i - 1] + " [Screenshot and Detailed Information](../" + webpage + "-failure-" + i + ".html) |";
			} else {
				row += "| " + categories[i - 1] + "| " + webpage + " | " + bs[0] + "px-" + bs[1] + "px | " + classifications[i - 1] + " | " + reasons[i - 1] + " [Screenshot and Detailed Information](../" + webpage + "-failure-" + i + ".html) |";
			}
		} else {
			if (tp) {
				row += "| " + categories[i - 1] + "| " + distinctRLFMapping[i - 1] + " | " + bs[0] + "px-" + bs[1] + "px | " + classifications[i - 1] + " | " + reasons[i - 1] + " | \n\n![Screenshot of the fault](../assets/images/" + webpage + "/fault" + i + "/" + imageName + "){: .center-image }";
			} else {
				row += "| " + categories[i - 1] + "| " + bs[0] + "px-" + bs[1] + "px | " + classifications[i - 1] + " | " + reasons[i - 1] + " | \n\n![Screenshot of the fault](../assets/images/" + webpage + "/fault" + i + "/" + imageName + "){: .center-image }";
			}
		}
		return row;
	}

//	private static String addInScreenshotTable(int count, File mostRecentRun, String webpage, String url, int totalReports, String[] classifications, String[] categories, String[] reasons, ArrayList<int[]> bounds) throws IOException {
//		String jekyllCode = "";
//		try {
//
//			String current = new java.io.File(".").getCanonicalPath();
//			System.setProperty("phantomjs.binary.path", current + "/resources/phantomjs");
//
//			WebpageMutator mutator = new WebpageMutator(webpage + "/index.html", webpage, 0);
//			int numElements = mutator.getElementCount(faultExamples + webpage);
//			int numDecs = mutator.getDeclarationCount();
//
//			jekyllCode += "---\nlayout: post\ntitle: \"" + webpage + "\"\nelements: " + numElements + "\ndecs: " + numDecs + "\nfullurl: " + url + "\n---\n";
//			jekyllCode += "| Report No. | Report Type | Distinct RLF No. | Viewport Range | Classification | Reason | Screenshot |\n";
//
//
//			for (int i = 1; i <= totalReports; i++) {
//				jekyllCode += generateTableRow(count+i-1, i, mostRecentRun, webpage, classifications, categories, reasons, bounds, distinctRLFMapping, false) + "\n";
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return jekyllCode;
//	}

	private static String[] getReasons(File f, int count) {
		String[] results = new String[count];
		try {
			BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath() + "/reasons.txt"));
			String line = br.readLine();
			int counter = 0;
			while (line != null) {
				results[counter] = line;
				counter++;
				line = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				new File(f.getAbsolutePath() + "/reasons.txt").createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return results;
	}

	private String[] getDistinctRLFMapping(File mostRecentRun, int errorCount) {
		String[] results = new String[errorCount];
		try {
			BufferedReader br = new BufferedReader(new FileReader(mostRecentRun.getAbsolutePath() + "/distinct-mapping.txt"));
			String line = br.readLine();
			int counter = 0;
			while (line != null) {
				if (!line.equals("-")) {
					results[counter] = line;
				} else {
					results[counter] = "";
				}

				counter++;
				line = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				new File(mostRecentRun.getAbsolutePath() + "/reasons.txt").createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return results;
	}

	private static String[] getCategories(File f, int count) {
		String[] results = new String[count];
		try {
			BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath() + "/error-types.txt"));
			String line = br.readLine();
			int counter = 0;
			while (line != null) {
				if (line.equals("VO")) {
					results[counter] = "Viewport Protrusion";
				} else if (line.equals("OF")) {
					results[counter] = "Element Protrusion";
				} else if (line.equals("OL")) {
					results[counter] = "Element Collision";
				} else if (line.equals("W")) {
					results[counter] = "Wrapping";
				} else if (line.equals("SR")) {
					results[counter] = "Small-Range";
				}
				counter++;
				line = br.readLine();
			}
		} catch (Exception e) {

		}
		return results;
	}

	private static String[] getClassifications(File f, int count) {
		String[] results = new String[count];
		try {
			BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath() + "/classification.txt"));
			String line = br.readLine();
			int counter = 0;
			while (line != null) {
				if (line.equals("1")) {
					results[counter] = "TP";
				} else if (line.equals("0")) {
					results[counter] = "NOI";
				} else if (line.equals("-1")) {
					results[counter] = "FP";
				}
				counter++;
				line = br.readLine();
			}
		} catch (Exception e) {

		}
		return results;
	}

//	public static ArrayList<int[]> getFailuresFromFile(File f) {
//		HashSet<String> errorStrings = new HashSet<>();
//		ArrayList<int[]> boundsList = new ArrayList<>();
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath() + "/fault-report.txt"));
//			String line = br.readLine();
//			int errorIndex = 1;
//			boolean foundBounds = false;
//
//			String contents = "";
//
//			while(line != null) {
////				contents += line;
//				int[] bounds = getFailureBounds(line, foundBounds);
//				boundsList.add(bounds);
//				int min = bounds[0];
//				int max = bounds[1];
////
////				if (b) {
////					if (tpIndexes.contains(errorIndex)) {
////						errorStrings.add(min + " - " + max);
////					}
////				} else {
////					if (min != 0 && max != 0) {
////						errorStrings.add(min + " - " + max);
////					}
////				}
//
//				if (line.equals("")) {
//					errorIndex++;
//					foundBounds = false;
//				}
//				line = br.readLine();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return boundsList;
//	}

	private static ArrayList<int[]> getFailureBounds(File f) {
		ArrayList<int[]> boundsList = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath() + "/fault-report.txt"));
			String failureString = "";
			String line = br.readLine();
			int errorIndex = 1;
			boolean foundBounds = false;

			String contents = "";

			while (line != null) {
				try {
					while (!line.equals("")) {
						failureString += line;
						line = br.readLine();
					}
					String[] splits, splits2;
					int min = 0, max = 0;

					if (failureString.contains("overflowed the viewport")) {
						splits = failureString.split(" and ");
						splits2 = splits[0].split("between ");
						min = Integer.valueOf(splits2[1]);
						max = Integer.valueOf(splits[1]);
						foundBounds = true;
					} else if (failureString.contains("OVERLAPPING")) {
						try {
							//						System.out.println(line);
							splits = failureString.split(" AND ");
							splits2 = splits[1].split("BETWEEN ");
							min = Integer.valueOf(splits2[1]);
							max = Integer.valueOf(splits[2]);
							//
							foundBounds = true;
						} catch (Exception e) {
							System.out.println("Issue with " + line);
						}
					} else if (failureString.contains("THIS:")) {
						splits = failureString.split(" , ");
						min = Integer.valueOf(splits[3]);
						max = Integer.valueOf(splits[4]);
						foundBounds = true;
					} else if (failureString.contains("WRAPPING")) {
						splits = failureString.split(" -> ");
						splits2 = splits[0].split("RANGE ");
						min = Integer.valueOf(splits2[1]);
						max = Integer.valueOf(splits[1].split(":")[0]);
						foundBounds = true;
					} else if (failureString.contains("OVERFLOWED ITS PARENT")) {
//						try {
							//						System.out.println(line);
						splits = failureString.split(" AND ");
//						splits2 = splits[1].split("\\t");
						min = Integer.valueOf(splits[0].split("BETWEEN ")[1]);
						max = Integer.valueOf(splits[1].split("\\t")[0]);
						//
						foundBounds = true;

					}

					if (min == 0 || max == 0) {
						System.out.println(line);
					}
					boundsList.add(new int[] {min, max});
					failureString = "";
//				if (line.equals("")) {
					errorIndex++;
					foundBounds = false;
				} catch (Exception ex) {
					System.out.println("Issue with " + failureString);
				}
//				if (b) {
//					if (tpIndexes.contains(errorIndex)) {
//						errorStrings.add(min + " - " + max);
//					}
//				} else {
//					if (min != 0 && max != 0) {
//						errorStrings.add(min + " - " + max);
//					}
//				}

//				}
				line = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return boundsList;
	}


	private static String getActualFaults(File f) {
		String contents = "";
		try {

			contents = Utils.readFile(f.getAbsolutePath() + "/../actual-fault-count.txt").trim();
			return (contents);
		} catch (Exception e) {
//			System.out.println("Error with " + f);
		}
		return "Null";
	}

	public static String getClassification(File f, ArrayList<Integer> tpIndexes) {
		String result = "";
		HashMap<String, HashMap<Integer, Integer>> counts = new HashMap<>();
		counts.put("SR", new HashMap<Integer, Integer>());
		counts.put("VO", new HashMap<Integer, Integer>());
		counts.put("OF", new HashMap<Integer, Integer>());
		counts.put("OL", new HashMap<Integer, Integer>());
		counts.put("W", new HashMap<Integer, Integer>());
//		counts.put("M", new HashMap<Integer, Integer>());

		for (String s : counts.keySet()) {
			HashMap<Integer, Integer> map = counts.get(s);
			if (map.entrySet().size() < 3) {
				map.put(1, 0);
				map.put(0, 0);
				map.put(-1, 0);
			}
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath() + "/classification.txt"));
			BufferedReader br2 = new BufferedReader(new FileReader(f.getAbsolutePath() + "/error-types.txt"));

			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			String line2 = br2.readLine();
			int count = 1;
			while (line != null) {
				if (line.equals("1")) {
					tpIndexes.add(count);
				}
				updateCounts(counts, line, line2);
				count ++;
				line = br.readLine();
				line2 = br2.readLine();
			}
			br.close();
			result = getResultsFromMap("OL", counts) + getResultsFromMap("OF", counts) + getResultsFromMap("VO", counts)
					+ getResultsFromMap("SR", counts) + getResultsFromMap("W", counts);
//
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private static String getResultsFromMap(String sr, HashMap<String, HashMap<Integer, Integer>> counts) {
		HashMap<Integer, Integer> iCounts = counts.get(sr);
		String mCount, nmCount, fpCount;
		if (iCounts.get(1) == 0) {
			mCount = "\\ZERO";
		} else {
			mCount = String.valueOf(iCounts.get(1));
		}
		if (iCounts.get(0) == 0) {
			nmCount = "\\ZERO";
		} else {
			nmCount = String.valueOf(iCounts.get(0));
		}
		if (iCounts.get(-1) == 0) {
			fpCount = "\\ZERO";
		} else {
			fpCount = String.valueOf(iCounts.get(-1));
		}
		return " & " + mCount + " & " + fpCount + " & " + nmCount;
	}

	private static void updateCounts(HashMap<String, HashMap<Integer, Integer>> counts, String line, String line2) {
		HashMap<Integer, Integer> map = counts.get(line2);

		int vis = counts.get(line2).get(1);
		int invis = counts.get(line2).get(0);
		int fp = counts.get(line2).get(-1);

		if (line.equals("1")) {
			map.put(1, vis+1);
		} else if (line.equals("0")) {
			map.put(0, invis+1);
		} else if (line.equals("-1")) {
			map.put(-1, fp+1);
		}
	}

	private static int getErrorCount(File f) {
		String contents = "";
		try {

			contents = Utils.readFile(f.getAbsolutePath() + "/error-count.txt").trim();
			return Integer.valueOf(contents);
		} catch (Exception e) {
//			System.out.println("Error with " + f);
		}
		return -1;
	}

	public static File lastFileModified(String dir) {
		File fl = new File(dir);
		File[] files = fl.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		long lastMod = Long.MIN_VALUE;
		File choice = null;
		if (files != null) {
			for (File file : files) {
//			System.out.println(file);
				if (file.lastModified() > lastMod) {
					choice = file;
					lastMod = file.lastModified();
				}
			}

		}

		return choice;
	}
}
