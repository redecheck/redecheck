package edu.gatech.xpert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.crawljax.oraclecomparator.AbstractComparator;
import com.crawljax.oraclecomparator.comparators.AttributeComparator;
import com.crawljax.oraclecomparator.comparators.RegexComparator;
import com.crawljax.oraclecomparator.comparators.SimpleComparator;
import com.crawljax.oraclecomparator.comparators.XPathExpressionComparator;
import com.crawljax.plugins.crawloverview.model.Edge;
import com.crawljax.plugins.crawloverview.model.State;

import edu.gatech.xpert.dom.DomNode;
import edu.gatech.xpert.dom.MatchResult;
import edu.gatech.xpert.dom.Matcher;
import edu.gatech.xpert.dom.layout.AGDiff;
import edu.gatech.xpert.dom.layout.AlignmentGraph;
import edu.gatech.xpert.dom.visitors.DomVisitor;
import edu.gatech.xpert.dom.visitors.NodeCountVisitor;

public class IsoChecker {

	private BrowserModel model1, model2;
	private Map<Edge, Edge> edgeMap;
	private Map<State, State> stateMap;
	private Set<Edge> uEdges1, uEdges2; // Unmatched Edges
	private XBIResult results;

	public IsoChecker(BrowserModel model1, BrowserModel model2) {
		this.model1 = model1;
		this.model2 = model2;

		this.edgeMap = new HashMap<Edge, Edge>();
		this.stateMap = new HashMap<State, State>();
		this.uEdges1 = new HashSet<Edge>();
		this.uEdges2 = new HashSet<Edge>();

		this.results = new XBIResult();
	}

	public void check() {
		checkSubGraphs("index", "index", new HashSet<String>(), new HashSet<String>());	
		
		populateBehaviorIssues();
	}
	
	private void populateBehaviorIssues() {
		List<String> behaviorIssues = new ArrayList<String>();
		for(Edge e : uEdges1) {
			behaviorIssues.add(behaviorErrMsg("MISSING-TRANSITION", e, null));
		}
		for(Edge e : uEdges2) {
			behaviorIssues.add(behaviorErrMsg("MISSING-TRANSITION", null, e));
		}
		this.results.addBehaviorIssues(behaviorIssues);
	}

	private String behaviorErrMsg(String msg, Edge e1, Edge e2) {
		return String.format("\"%s\",\"(%s)\",\"(%s)\"", msg, e1, e2);
	}

	public void checkSubGraphs(String state1, String state2, Set<String> visited1, Set<String> visited2) {

		if (visited1.contains(state1) || visited2.contains(state2)) {
			return;
		} else {
			visited1.add(state1);
			visited2.add(state2);
		}
		
		State s1 = model1.stg().getStates().get(state1);
		State s2 = model2.stg().getStates().get(state2);
		
		if (s1 == null || s2 == null) {
			System.err.println("Index page missing from graphs");
			return;
		}
		
		checkStates(s1, s2);
		checkEdges(s1, s2, visited1, visited2);
	}

	private void checkStates(State s1, State s2) {
		
		stateMap.put(s1, s2);

		X.debug("* Checking states " + s1.getName() + " & " + s2.getName());

		DomNode root1 = model1.domMap.get(s1.getName());
		DomNode root2 = model2.domMap.get(s2.getName());

		X.debug("-- Matching Doms for states " + s1.getName() + " & "
				+ s2.getName());
		Matcher domMatcher = new Matcher();
		MatchResult mr = domMatcher.doMatch(root1, root2);
		Map<DomNode, DomNode> matchedNodes = mr.getMatched();

		// if(X.DEBUG) {
		// printMatchStats(root1, root2);
		// }

		X.debug("-- Building Alignment Graphs for states " + s1.getName()
				+ " & " + s2.getName());
		AlignmentGraph ag1 = new AlignmentGraph(root1), 
				ag2 = new AlignmentGraph(root2);

		// if(X.DEBUG) {
		// printAlignmentGraph(ag1);
		// printAlignmentGraph(ag2);
		// }

		AGDiff agDiffer = new AGDiff(matchedNodes, ag1, ag2);
		List<String> layoutIssues = new ArrayList<>();

		for (DomNode a : matchedNodes.keySet()) {
			DomNode b = matchedNodes.get(a);
			if (X.DEBUG_LAYOUT) {
				System.out.println("\t comparing " + a.getxPath() + " - "
						+ b.getxPath());
			}
			List<String> issues = agDiffer.diff(a.getxPath(), b.getxPath());
			layoutIssues.addAll(issues);
		}

		this.results.addLayoutIssues(
				String.format("%s-%s", s1.getName(), s2.getName()),
				layoutIssues);

		// X.debug("Layout issues between "+s1.getName()+" & "+s2.getName());
		// for(String issue: layoutIssues) {
		// System.out.println(issue);
		// }

	}

	private void checkEdges(State s1, State s2, Set<String> visited1,
			Set<String> visited2) {

		List<Edge> out1 = model1.getOutgoingEdges(s1);
		List<Edge> out2 = model2.getOutgoingEdges(s2);

		for (Edge e1 : out1) {
			for (Edge e2 : out2) {
				if (!edgeMap.containsKey(e1) && !edgeMap.containsValue(e2)) {
					if (equalsEdge(e1, e2)) {
						edgeMap.put(e1, e2);
						checkSubGraphs(e1.getTo(), e2.getTo(), visited1, visited2);
					}
				}
			}
		}
		
		for(Edge e : out1) {
			if(!edgeMap.containsKey(e)) {
				uEdges1.add(e);
			}
		}
		
		for(Edge e : out2) {
			if(!edgeMap.containsValue(e)) {
				uEdges2.add(e);
			}
		}
	}

	private boolean equalsEdge(Edge e1, Edge e2) {

		if (e1 == e2 || StringUtils.equals(e1.toString(), e2.toString())) {
			return true;
		}

		if (StringUtils.isNotBlank(e1.getText())
				&& StringUtils.isNotBlank(e2.getText())) {
			if (StringUtils.equals(e1.getText(), e2.getText())) {
				return true;
			}
		}
		
		return false;
	}
	
	public XBIResult getResults(){
		return this.results;
	}
	

	private String cleanDom(String html) {
		AbstractComparator comp[] = {
				new SimpleComparator(),
				new XPathExpressionComparator("//SCRIPT", "//STYLE", "//META",
						"//LINK", "//TITLE", "//NOSCRIPT", "//comment()"),
				new AttributeComparator("xmlns", "type", "selected", "checked",
						"style", "value", "class", "tabindex", "align",
						"valign", "border"),
				new RegexComparator("(?s)<!--.*?-->") };

		for (AbstractComparator c : comp) {
			html = c.normalize(html);
		}

		return html;
	}

	private static void printMatchStats(DomNode root1, DomNode root2) {
		System.out.println("*** DOM Match Stats ***");
		DomVisitor v1 = new NodeCountVisitor();
		root1.accept(v1, true);
		System.out.println("\nDOM1 Stats:\n" + v1);

		DomVisitor v2 = new NodeCountVisitor();
		root2.accept(v2, true);
		System.out.println("\nDOM2 Stats:\n" + v2);
	}

	private static void printAlignmentGraph(AlignmentGraph ag) {
		System.out.println("***** ALIGNMENT GRAPH *****");
		System.out.println(ag);
	}

}
