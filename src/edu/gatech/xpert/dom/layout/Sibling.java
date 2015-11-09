package edu.gatech.xpert.dom.layout;

import edu.gatech.xpert.dom.DomNode;

public class Sibling extends AGEdge {

	AGNode node1, node2;
	boolean topBottom, bottomTop, leftRight, rightLeft;
	boolean topEdgeAligned, bottomEdgeAligned, leftEdgeAligned,
			rightEdgeAligned;
	
	int deltaW = 5, deltaH = 5;

	public Sibling(AGNode a, AGNode b) {
		node1 = a;
		node2 = b;
		populateProperties();
	}

	public void populateProperties() {
		if (equals(node1.x1, node2.x1, deltaW)) {
			setLeftEdgeAligned(true);
		}

		if (equals(node1.x2, node2.x2, deltaW)) {
			setRightEdgeAligned(true);
		}

		if (equals(node1.y1, node2.y1, deltaH)) {
			setTopEdgeAligned(true);
		}

		if (equals(node1.y2, node2.y2, deltaH)) {
			setBottomEdgeAligned(true);
		}

		if (isLeftRight(node1, node2)) {
			setLeftRight(true);
		}

		if (isLeftRight(node2, node1)) {
			setRightLeft(true);
		}

		if (isTopBottom(node1, node2)) {
			setTopBottom(true);
		}

		if (isTopBottom(node2, node1)) {
			setBottomTop(true);
		}
	}

	@Override
	public DomNode getNode1() {
		return node1.domNode;
	}

	@Override
	public DomNode getNode2() {
		return node2.domNode;
	}

	@Override
	public String getAttributes() {
		return "{topBottom:" + topBottom + ", bottomTop:" + bottomTop
				+ ", leftRight:" + leftRight + ", rightLeft:" + rightLeft
				+ ", topEdgeAligned:" + topEdgeAligned + ", bottomEdgeAligned:"
				+ bottomEdgeAligned + ", leftEdgeAligned:" + leftEdgeAligned
				+ ", rightEdgeAligned:" + rightEdgeAligned + "}";
	}

	@Override
	public String toString() {
		return this.node1.domNode.getxPath() + "->"
				+ this.node2.domNode.getxPath();
	}

	// Setters and Getters

	public boolean isTopBottom() {
		return topBottom;
	}

	public void setTopBottom(boolean topBottom) {
		this.topBottom = topBottom;
	}

	public boolean isBottomTop() {
		return bottomTop;
	}

	public void setBottomTop(boolean bottomTop) {
		this.bottomTop = bottomTop;
	}

	public boolean isLeftRight() {
		return leftRight;
	}

	public void setLeftRight(boolean leftRight) {
		this.leftRight = leftRight;
	}

	public boolean isRightLeft() {
		return rightLeft;
	}

	public void setRightLeft(boolean rightLeft) {
		this.rightLeft = rightLeft;
	}

	public boolean isTopEdgeAligned() {
		return topEdgeAligned;
	}

	public void setTopEdgeAligned(boolean topEdgeAligned) {
		this.topEdgeAligned = topEdgeAligned;
	}

	public boolean isBottomEdgeAligned() {
		return bottomEdgeAligned;
	}

	public void setBottomEdgeAligned(boolean bottomEdgeAligned) {
		this.bottomEdgeAligned = bottomEdgeAligned;
	}

	public boolean isLeftEdgeAligned() {
		return leftEdgeAligned;
	}

	public void setLeftEdgeAligned(boolean leftEdgeAligned) {
		this.leftEdgeAligned = leftEdgeAligned;
	}

	public boolean isRightEdgeAligned() {
		return rightEdgeAligned;
	}

	public void setRightEdgeAligned(boolean rightEdgeAligned) {
		this.rightEdgeAligned = rightEdgeAligned;
	}

}
