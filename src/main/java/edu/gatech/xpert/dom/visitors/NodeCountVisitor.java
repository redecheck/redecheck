package edu.gatech.xpert.dom.visitors;

import java.util.ArrayList;
import java.util.List;

import edu.gatech.xpert.dom.DomNode;

public class NodeCountVisitor extends DomVisitor {
	int matched = 0, unmatched = 0;
	List<DomNode> unmatchedList;
	
	public NodeCountVisitor() {
		unmatchedList = new ArrayList<>();
	}
	
	@Override
	public void visit(DomNode node) {
		if(node.isMatched()){
			matched++;
		} else {
			unmatchedList.add(node);
			unmatched++;
		}
	}

	@Override
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("Total nodes="+ (matched+unmatched) + "; match/unmatched=("+matched+"/"+unmatched+")\nUnmatched:\n");
		for(DomNode u : unmatchedList) {
			sb.append(" - ");
			sb.append(u.getxPath() + " :: "+ u.getAttributes());
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
}
