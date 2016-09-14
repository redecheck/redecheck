package edu.gatech.xpert.dom.visitors;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.gatech.xpert.dom.DomNode;
import edu.gatech.xpert.dom.DomUtils;

public class ApproxMatchVisitor extends DomVisitor {
	DomNode ref;
	Map<DomNode, DomNode> matched;
	int cnt = 0;
	float bestMatchIdx;
	DomNode bestMatch;

	float THRESHOLD_GLOBAL = 0.85f;
	
	public ApproxMatchVisitor(DomNode node, Map<DomNode, DomNode> matched) {
//		System.out.println("processing "+node);
		ref = node;
		this.matched = matched;
		this.bestMatchIdx = 0;
		this.bestMatch = null;
	}

	@Override
	public void visit(DomNode node) {
		cnt++;
		
		if(!node.isMatched()) {
			if(StringUtils.equals(ref.getTagName(), node.getTagName())) {
				float matchIndex = DomUtils.calculateMatchIndex(ref, node);
				if(matchIndex > THRESHOLD_GLOBAL && matchIndex > bestMatchIdx) {
					bestMatchIdx = matchIndex;
					bestMatch = node;
				}
			}
		}
	}
	
	public void matchPost() {
		if(bestMatch != null) {
			matched.put(ref, bestMatch);
			ref.setMatched(true);
			bestMatch.setMatched(true);
		}
	}
	
	@Override
	public String toString() {
		return "AMV visited #nodes:"+cnt;
	}

}
