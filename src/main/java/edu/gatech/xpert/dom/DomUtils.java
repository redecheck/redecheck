package edu.gatech.xpert.dom;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class DomUtils {

	static String[] ignoredAttr = { "style", "type" };

	static Map<String, String> ignoredDomData = new HashMap<String, String>() {
		{
			put("zindex", "0");
		}
	};

	
	// Calculates the DOM Match-Index Metric from the WebDiff paper (Ref: roychoudhary10icsm)
	public static float calculateMatchIndex(DomNode x, DomNode y) {
		// Weights for the MatchIndex formula
		float XPATH = 0.7f, ATTRIB = 0.2f, DOMDATA = 0.1f;

		//System.out.println("MI for "+x.getNodeType()+":"+x+"; "+y.getNodeType()+":"+y);
		
		try{
		
		String xPath1 = processXpath(x.getxPath());
		String xPath2 = processXpath(y.getxPath());

		
		
		Map<String, String> attr1 = processAttr(x.getAttributes());
		Map<String, String> attr2 = processAttr(y.getAttributes());

		Map<String, String> dd1 = processDomData(x.getDynDomData());
		Map<String, String> dd2 = processDomData(y.getDynDomData());

		float xPathSim = 1 - StringUtils.getLevenshteinDistance(xPath1, xPath2)
				/ (float) Math.max(xPath1.length(), xPath2.length());
		
		float attrSim = getMapSimilarity(attr1, attr2);
		float ddSim = getMapSimilarity(dd1, dd2);

		// Plug-in each value into the MatchIndex formula
		float matchIndex = (XPATH * xPathSim) + (ATTRIB * attrSim)
				+ (DOMDATA * ddSim);
		
		return matchIndex;
		
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println(x.getNodeType()+":"+x+"; "+y.getNodeType()+":"+y);
		}
		
		return 0;
	}

	private static String processXpath(String xPath) {
		
//		System.out.println(xPath);
		xPath = xPath.replace("//", "/");
		return xPath;
	}

	private static Map<String, String> processAttr(Map<String, String> attr) {
		for (String ignored : ignoredAttr) {
			if(attr.containsKey(ignored)) {
				attr.remove(ignored);
			}
		}
		return attr;
	}

	private static Map<String, String> processDomData(Map<String, String> dd) {
		for (String k : ignoredDomData.keySet()) {
			if (dd.containsKey(k)) {
				if (ignoredDomData.get(k).equalsIgnoreCase(dd.get(k))) {
					dd.remove(k);
				}
			}
		}
		return dd;
	}

	private static float getMapSimilarity(Map<String, String> m1,
			Map<String, String> m2) {

		if (m1 == null && m2 == null)
			return 1;
		if (m1 == null || m2 == null)
			return 0;

		float total = m1.size() + m2.size();
		if (total == 0)
			return 1;

		return mapDiff(m1, m2) / total;
	}

	private static int mapDiff(Map<String, String> a, Map<String, String> b) {
		int matchCount = 0;
		for (String key : a.keySet()) {
			if (b.containsKey(key) && cleanAndCompare(a.get(key), b.get(key))) {
				matchCount++;
			}
		}
		for (String key : b.keySet()) {
			if (a.containsKey(key) && cleanAndCompare(a.get(key), b.get(key))) {
				matchCount++;
			}
		}
		return matchCount;
	}

	public static boolean cleanAndCompare(String str1, String str2) {
		str1 = str1.replaceAll("[\'\"\\s]", "");
		str2 = str2.replaceAll("[\'\"\\s]", "");
		return StringUtils.equals(str1, str2);
	}

}
