package edu.gatech.xpert.dom.layout;

import edu.gatech.xpert.dom.DomNode;

/**
 * Alignment Graph Node
 * @author shauvik
 */
public class AGNode {
	int x1, y1, x2, y2;
	long area;
	DomNode domNode;
	AGNode parent;
	
	public AGNode(DomNode domNode) {
		this.domNode = domNode;
		int[] c = domNode.getCoords();
		x1 = c[0];
		y1 = c[1];
		x2 = c[2];
		y2 = c[3];
		area = (x2 - x1) * (y2 - y1);
	}

	public long getArea() {
		return area;
	}

	public boolean contains(AGNode n) {
		if (this.x1 <= n.x1 && this.y1 <= n.y1
				&& this.x2 >= n.x2 && this.y2 >= n.y2) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		return domNode.getxPath();
	};
}
