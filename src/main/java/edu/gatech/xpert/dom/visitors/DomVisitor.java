package edu.gatech.xpert.dom.visitors;

import edu.gatech.xpert.dom.DomNode;

public abstract class DomVisitor {
	
	public abstract void visit(DomNode node);

	public void endVisit(DomNode node) {
		// Do nothing
	}
}
