package shef.mutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import edu.gatech.xpert.dom.DomNode;

public class ResultClassifier {
	int[] widths;
	String oracleUrl, testUrl;
	HashMap<Integer, DomNode> oDoms, tDoms;
	boolean rlgDetected;
	public static final String[] tagsIgnore = { "AREA", "B", "BLOCKQUOTE",
			"BR", "CANVAS", "CENTER", "CSACTIONDICT", "CSSCRIPTDICT", "CUFON",
			"CUFONTEXT", "DD", "EM", "EMBED", "FIELDSET", "FONT", "FORM",
			"HEAD", "HR", "IFRAME", "I", "LABEL", "LEGEND", "LINK", "MAP", "MENUMACHINE",
			"META", "NOFRAMES", "NOSCRIPT", "OBJECT", "OPTGROUP", "OPTION",
			"PARAM", "S", "SCRIPT", "SMALL", "SPAN", "STRIKE", "STRONG",
			"STYLE", "TBODY", "TITLE", "TR", "TT", "U" };
	
	public ResultClassifier(int[] allWidths, String oracleUrl, String testUrl, HashMap<Integer, DomNode> oDoms, HashMap<Integer, DomNode> tDoms, boolean foundDiffsInRLG) {
		this.widths = allWidths;
		this.oracleUrl = oracleUrl;
		this.testUrl = testUrl;
		this.oDoms = oDoms;
		this.tDoms = tDoms;
		this.rlgDetected = foundDiffsInRLG;
	}
	
	public static TreeSet<Integer> diffAllDoms(HashMap<Integer, DomNode> oracleDoms, HashMap<Integer, DomNode> testDoms, HashMap<Integer, String> oracleDomsS, HashMap<Integer, String> testDomsS, int[] widthArray) {
		System.out.println(oracleDomsS.size());
		System.out.println(testDomsS.size());

		TreeSet<Integer> diffWidths = new TreeSet<>();
		for (int w : widthArray) {
            System.out.println(w);
			DomNode dn1 = oracleDoms.get(w);
			DomNode dn2 = testDoms.get(w);
			if (!domsEqual(dn1, dn2)) {
				diffWidths.add(w);
			}
		}
		return diffWidths;
	}

	public static TreeSet<Integer> diffAllDoms2(HashMap<Integer, edu2.gatech.xpert.dom.DomNode> oracleDoms, HashMap<Integer, edu2.gatech.xpert.dom.DomNode> testDoms, HashMap<Integer, String> oracleDomsS, HashMap<Integer, String> testDomsS, int[] widthArray) {

		TreeSet<Integer> diffWidths = new TreeSet<>();
		for (int w : widthArray) {
//			System.out.println(w);
			edu2.gatech.xpert.dom.DomNode dn1 = oracleDoms.get(w);
			edu2.gatech.xpert.dom.DomNode dn2 = testDoms.get(w);
			if (!domsEqual2(dn1, dn2)) {
//				System.out.println("Not equal");
				diffWidths.add(w);
			}
		}
		return diffWidths;
	}
	
	
	
	public static boolean domsEqual(DomNode dn1, DomNode dn2) {
		HashMap<DomNode, DomNode> coordsDiff = new HashMap<>();
        ArrayList<DomNode> nonMatching = new ArrayList<>();
		int numMatches = 0;
		DomNode match = null;
		ArrayList<DomNode> worklist1 = new ArrayList<DomNode>();
		ArrayList<DomNode> worklist2 = new ArrayList<DomNode>();
		worklist1.add(dn1);
		worklist2.add(dn2);
		while (!worklist1.isEmpty()) {
			DomNode toMatch = worklist1.remove(0);
			String xpath = null;
			try {
				if (toMatch.getxPath() == null) {
					xpath = "";
				} else {
					xpath = toMatch.getxPath();
				}
			} catch (NullPointerException e) {
				xpath = "";
			}

			String parent = null;
			String parent2 = null;
			try {
				if (toMatch.getParent() != null) {
					parent = toMatch.getParent().getxPath();
				} else {
					parent = "";
				}
			} catch (NullPointerException e) {
				parent = "";
			}
			match=null;
			for (DomNode dn : worklist2) {
				String xpath2 = null;
				if (dn.getxPath() == null) {
					xpath2 = "";
				} else {
					xpath2 = dn.getxPath();
				}
				if (xpath.equals(xpath2)) {
					if (dn.getParent() != null) {
						parent2 = dn.getParent().getxPath();
					} else {
						parent2 = "";	
					}
					if (parent.equals(parent2)) {
						if (coordsMatch(toMatch.getCoords(), dn.getCoords())) {
						    System.out.println("Matched " + xpath);
							match = dn;
						} else {
                            match = dn;
                            nonMatching.add(toMatch);
                            coordsDiff.put(toMatch, dn);
                        }
					}
				}
			}
			if (match != null) {
				numMatches++;
				worklist2.remove(match);
				for (DomNode d : toMatch.getChildren()) {
					if (d.getTagName() != null) {
						if (!ignoreTag(d.getTagName())) {
							worklist1.add(d);
						}
					}
				}
				for (DomNode d2 : match.getChildren()) {
					if (d2.getTagName() != null) {
						if (!ignoreTag(d2.getTagName())) {
							worklist2.add(d2);
						}
					}
				}
			} else {
                nonMatching.add(toMatch);
//				System.out.println(toMatch + "\n");
//
//                for (DomNode d2 : match.getChildren()) {
//                    if (d2.getTagName() != null) {
//                        if (!ignoreTag(d2.getTagName())) {
//                            worklist2.add(d2);
//                        }
//                    }
//                }
//				return false;
			}
		}

//		Check worklist2 empty
//		System.out.println(worklist2.size());
		if (!worklist2.isEmpty()) {
			return false;
		}

		if (nonMatching.size() == 0) {
//            System.out.println("EQUAL");
			return true;
		} else {
//            System.out.println("NOT EQUAL");
            for (DomNode dn : coordsDiff.keySet()) {
                DomNode pair = coordsDiff.get(dn);
                int[] coords1 = dn.getCoords();
                int[] coords2 = pair.getCoords();
                System.out.println(dn.getxPath());
                System.out.println(coords1[0] + ","+coords1[1] + ","+coords1[2] + ","+coords1[3]);
                System.out.println(coords2[0] + ","+coords2[1] + ","+coords2[2] + ","+coords2[3]);
                System.out.println();
            }
//			for (DomNode n : nonMatching) {
//				System.out.println(n.getxPath() + " was not matched");
//			}
			return false;
		}
	}

	public static boolean domsEqual2(edu2.gatech.xpert.dom.DomNode dn1, edu2.gatech.xpert.dom.DomNode dn2) {
		HashMap<edu2.gatech.xpert.dom.DomNode, edu2.gatech.xpert.dom.DomNode> coordsDiff = new HashMap<>();
		ArrayList<edu2.gatech.xpert.dom.DomNode> nonMatching = new ArrayList<>();
		int numMatches = 0;
		edu2.gatech.xpert.dom.DomNode match = null;
		ArrayList<edu2.gatech.xpert.dom.DomNode> worklist1 = new ArrayList<edu2.gatech.xpert.dom.DomNode>();
		ArrayList<edu2.gatech.xpert.dom.DomNode> worklist2 = new ArrayList<edu2.gatech.xpert.dom.DomNode>();
		worklist1.add(dn1);
		worklist2.add(dn2);
		while (!worklist1.isEmpty()) {
			edu2.gatech.xpert.dom.DomNode toMatch = worklist1.remove(0);
			String xpath = null;
			try {
				if (toMatch.getxPath() == null) {
					xpath = "";
				} else {
					xpath = toMatch.getxPath();
				}
			} catch (NullPointerException e) {
				xpath = "";
			}

			if (xpath.contains("/HTML/BODY/DIV/DIV/DIV/DIV/DIV/A")) {
				System.out.println(xpath);
			}

			String parent = null;
			String parent2 = null;
			try {
				if (toMatch.getParent() != null) {
					parent = toMatch.getParent().getxPath();
				} else {
					parent = "";
				}
			} catch (NullPointerException e) {
				parent = "";
			}
			match=null;
			for (edu2.gatech.xpert.dom.DomNode dn : worklist2) {
				String xpath2 = null;
				if (dn.getxPath() == null) {
					xpath2 = "";
				} else {
					xpath2 = dn.getxPath();
				}
				if (xpath.equals(xpath2)) {
//					if (xpath.contains("/HTML/BODY/DIV[2]/DIV[5]/")) {
//						System.out.println("Matched xpath");
//					}
					if (dn.getParent() != null) {
						parent2 = dn.getParent().getxPath();
					} else {
						parent2 = "";
					}
					if (parent.equals(parent2)) {
//						if (xpath.contains("/HTML/BODY/DIV[2]/DIV[5]/")) {
//							System.out.println("Matched parent");
//
//						}
						if (coordsMatch(toMatch.getCoords(), dn.getCoords())) {
//							if (xpath.contains("/HTML/BODY/DIV[2]/DIV[5]/")) {
//								System.out.println();
//							}
							match = dn;
						} else {
							match = dn;
							nonMatching.add(toMatch);
							coordsDiff.put(toMatch, dn);
						}
					}
				}
			}
			if (match != null) {
				numMatches++;
				worklist2.remove(match);
				for (edu2.gatech.xpert.dom.DomNode d : toMatch.getChildren()) {
					if (d.getTagName() != null) {
						if (!ignoreTag(d.getTagName())) {
							worklist1.add(d);
						}
					}
				}
				for (edu2.gatech.xpert.dom.DomNode d2 : match.getChildren()) {
					if (d2.getTagName() != null) {
						if (!ignoreTag(d2.getTagName())) {
							worklist2.add(d2);
						}
					}
				}
			} else {
				nonMatching.add(toMatch);
//				System.out.println(toMatch + "\n");
//
//                for (DomNode d2 : match.getChildren()) {
//                    if (d2.getTagName() != null) {
//                        if (!ignoreTag(d2.getTagName())) {
//                            worklist2.add(d2);
//                        }
//                    }
//                }
//				return false;
			}
		}
//		System.out.println(worklist2.size());
		if (!worklist2.isEmpty()) {
			return false;
		}

		if (nonMatching.size() == 0) {
//			System.out.println("EQUAL");
			return true;
		} else {
//			System.out.println("NOT EQUAL");
//			for (edu2.gatech.xpert.dom.DomNode dn : coordsDiff.keySet()) {
//				edu2.gatech.xpert.dom.DomNode pair = coordsDiff.get(dn);
//				int[] coords1 = dn.getCoords();
//				int[] coords2 = pair.getCoords();
//				System.out.println(dn.getxPath() + "["+coords1[0] + ","+coords1[1] + ","+coords1[2] + ","+coords1[3] + "]  vs  [" +coords2[0] + ","+coords2[1] + ","+coords2[2] + ","+coords2[3] + "]");
//			}
//			for (edu2.gatech.xpert.dom.DomNode n : nonMatching) {
//				System.out.println(n.getxPath() + " was not matched");
//			}
			return false;
		}
	}
	
	private static boolean coordsMatch(int[] coords1, int[] coords2) {
		if ((coords1 == null) && (coords2 == null)) {
			return true;
//		} else if (!hasMeaningfulSize(coords1, coords2)) {
//			System.out.println("Too small");
//			return true;
		} else if ((coords1 == null) && (coords2 != null)) {
			return false;
		} else if ((coords1 != null) && (coords2 == null)) {
			return false;
		} else {
			for (int i = 0; i < 4; i++) {
				if (coords1[i] != coords2[i]) {
//					System.out.println(coords1[0] + ","+coords1[1] + ","+coords1[2] + ","+coords1[3]);
//					System.out.println(coords2[0] + ","+coords2[1] + ","+coords2[2] + ","+coords2[3]);
					return false;
				}
			}
		}
		return true;
	}
	
	private static boolean hasMeaningfulSize(int[] coords, int[] coords2) {
		boolean oneSmall, twoSmall;

		if ((coords[2] - coords[0]) < 5) {
			return oneSmall = false;
		} else if ((coords[3]- coords[1]) < 5) {
			return false;
		}
		return true;
	}
	
	private static boolean ignoreTag(String tagName) {
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
