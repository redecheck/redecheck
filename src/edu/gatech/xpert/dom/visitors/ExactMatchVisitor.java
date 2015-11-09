package edu.gatech.xpert.dom.visitors;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.gatech.xpert.dom.DomNode;
import edu.gatech.xpert.dom.DomUtils;

public class ExactMatchVisitor extends DomVisitor {
	DomNode ref;
	Map<DomNode, DomNode> matched;
	boolean found;
	int cnt = 0;
	
	public ExactMatchVisitor(DomNode node, Map<DomNode, DomNode> matched) {
//		System.out.println("processing "+node);
		ref = node;
		this.matched = matched;
	}

	@Override
	public void visit(DomNode node) {
		cnt++;
		
		if(!found && !node.isMatched()) {
			if(StringUtils.equals(ref.getTagName(), node.getTagName())) {
				float matchIndex = DomUtils.calculateMatchIndex(ref, node);
				if(matchIndex==1.0) {
					matched.put(ref, node);
					ref.setMatched(true);
					node.setMatched(true);
					found = true;
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return "EMV visited #nodes:"+cnt;
	}

}
