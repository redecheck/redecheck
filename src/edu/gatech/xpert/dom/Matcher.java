package edu.gatech.xpert.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gatech.xpert.dom.visitors.ApproxMatchVisitor;
import edu.gatech.xpert.dom.visitors.DomVisitor;
import edu.gatech.xpert.dom.visitors.LevelAssignVisitor;
import edu.gatech.xpert.dom.visitors.ExactMatchVisitor;
import edu.gatech.xpert.dom.visitors.UnmatchedVisitor;

public class Matcher {
	
	float THRESHOLD_LEVEL = 0.75f;
	
	Map<DomNode, DomNode> matched;
	List<DomNode> unmatched1, unmatched2;
	
	public Matcher(){
		matched = new HashMap<>();
		unmatched1 = new ArrayList<>();
		unmatched2 =  new ArrayList<>();
	}
	
	public MatchResult doMatch(Map<String, DomNode> doms, String[] browsers) {
		
		if(doms.size() < 2) {
			System.err.println("DOM compare error: list size="+doms.size());
			return null;
		}
		
		DomNode root1 = doms.get(browsers[0]);
		DomNode root2 = doms.get(browsers[1]);
		
		return doMatch(root1, root2);
		
	}
	
	public MatchResult doMatch(DomNode root1, DomNode root2){
		
		// 1: Perfect Match Visitor
		List<DomNode> worklist = new ArrayList<>();
		worklist.add(root1);
		while(!worklist.isEmpty()) {
			DomNode node = worklist.remove(0);
			if(node.isTag()) {
				DomVisitor pmv = new ExactMatchVisitor(node, matched);
				root2.accept(pmv, true);
				
				//debug
//				if("/HTML/HEAD/STYLE[3]".equals(node.getxPath())) {
//					System.out.println(pmv);
//				}
					
				for(DomNode child: node.getChildren()) {
					if(child.isTag()) {
						worklist.add(child);
					}
				}
			}
		}
		
		// Assign levels
		LevelAssignVisitor lvl = new LevelAssignVisitor(); 
		root1.accept(lvl, true);
		List<List<DomNode>> levels1 = lvl.getLevels();
		lvl.init();
		root2.accept(lvl, true);
		List<List<DomNode>> levels2 = lvl.getLevels();
		
		UnmatchedVisitor uv = new UnmatchedVisitor();
		root1.accept(uv);
		List<DomNode> unmatchedNodes = uv.getUnmatched();
		
		
		// 2: Level Match Visitor
		for(DomNode node : unmatchedNodes) {
			int level = node.getLevel();
			if(node.isTag() && level < levels2.size()) {
				List<DomNode> lNodes = levels2.get(level);
				float bestMatchIndex = 0;
				DomNode bestMatchNode = null;
				for(DomNode ln : lNodes) {
					if(!ln.isMatched() && ln.isTag()) {
						float matchIdx = DomUtils.calculateMatchIndex(node, ln);
						if(matchIdx >= THRESHOLD_LEVEL && matchIdx > bestMatchIndex) {
							bestMatchIndex = matchIdx;
							bestMatchNode = ln;
						}
					}
				}
				if(bestMatchNode != null) {
					node.setMatched(true);
					bestMatchNode.setMatched(true);
					matched.put(node, bestMatchNode);
				} else {
					worklist.add(node);
				}
			}
		}
		
		// 3: Approximate global matching
		for(DomNode node : worklist) {
			ApproxMatchVisitor amv = new ApproxMatchVisitor(node, matched);
			root2.accept(amv);
			amv.matchPost();
			if(!node.isMatched()) {
				unmatched1.add(node);
			}
		}
		
		uv.init();
		root2.accept(uv);
		unmatched2 = uv.getUnmatched();
		
		return new MatchResult(matched, unmatched1, unmatched2);
	}

}
