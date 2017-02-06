package edu.gatech.xpert.dom.visitors;

import java.util.ArrayList;
import java.util.List;

import edu.gatech.xpert.dom.DomNode;

public class LevelAssignVisitor extends DomVisitor {

	public LevelAssignVisitor() {
		init();
	}

	List<List<DomNode>> levels;

	public void init() {
		levels = new ArrayList<>();
	}

	@Override
	public void visit(DomNode node) {
		DomNode parent = node.getParent();
		int level = -1;
		if(parent == null) {
			level = 0;
		} else {
			level = parent.getLevel() + 1;
		}
		if(level >= levels.size()) {
			List<DomNode> l = new ArrayList<DomNode>();
			l.add(node);
			levels.add(l);
		} else {
			levels.get(level).add(node);
		}
		node.setLevel(level);
	}
	
	public List<List<DomNode>> getLevels() {
		return levels;
	}

}
