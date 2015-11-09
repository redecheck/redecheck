package edu.gatech.xpert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XBIResult {

	Map<String, List<String>> layoutIssues;
	Map<String, List<String>> contentIssues;
	List<String> behaviorIssues;

	public XBIResult() {
		layoutIssues = new HashMap<String, List<String>>();
		contentIssues = new HashMap<String, List<String>>();
		behaviorIssues = new ArrayList<String>();
	}
	
	public void addLayoutIssues(String statePair, List<String> issues) {
		layoutIssues.put(statePair, issues);
	}
	
	public void addContentIssues(String statePair, List<String> issues) {
		contentIssues.put(statePair, issues);
	}
	
	public void addBehaviorIssues(List<String> issues) {
		behaviorIssues.addAll(issues);
	}
	
	public Map<String, List<String>> getLayoutIssues() {
		return layoutIssues;
	}
	
	public Map<String, List<String>> getContentIssues() {
		return contentIssues;
	}
	
	public List<String> getBehaviorIssues() {
		return behaviorIssues;
	}

}
