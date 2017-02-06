package edu.gatech.xpert.dom.layout;

import java.util.Comparator;


public class AGNodeComparator implements Comparator<AGNode> {

	@Override
	public int compare(AGNode a, AGNode b) {
		int diff = (int)(a.getArea() - b.getArea()); //small area 
		return (diff == 0) ? (b.domNode.getxPath().length() - a.domNode.getxPath().length()) : diff; // big xPath
	}

}
