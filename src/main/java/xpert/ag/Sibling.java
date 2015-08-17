package xpert.ag;

import xpert.dom.DomNode;

public class Sibling extends Edge {

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
	
	public String getLabelling() {
		String result = "";
		if (topBottom) {
			result = result + "Below,";
		}
		if (bottomTop) {
			result = result + "Above,";
		}
		if (leftRight) {
			result = result + "Left of,";
		}
		if (rightLeft) {
			result = result + "Right of,";
		}
		if (topEdgeAligned) {
			result = result + "TopAlign,";
		}
		if (bottomEdgeAligned) {
			result = result + "BottomAlign,";
		}
		if (leftEdgeAligned) {
			result = result + "LeftAlign,";
		}
		if (rightEdgeAligned) {
			result = result + "RightAlign.";
		}
		
		return result;
	}

	public String generateLabelling() {
		String result = "";
		if (topBottom) {
			result = result + "below";
		}
		if (bottomTop) {
			result = result + "above";
		}
		if (rightLeft) {
			result = result + "leftOf";
		}
		if (leftRight) {
			result = result + "rightOf";
		}
		if (topEdgeAligned) {
			result = result + "topAlign";
		}
		if (bottomEdgeAligned) {
			result = result + "bottomAlign";
		}
		if (leftEdgeAligned) {
			result = result + "leftAlign";
		}
		if (rightEdgeAligned) {
			result = result + "rightAlign";
		}
		return result;
	}

	public String generateFlippedLabelling() {
		String result = "";
		if (topBottom) {
			result = result + "above";
		}
		if (bottomTop) {
			result = result + "below";
		}
		if (rightLeft) {
			result = result + "rightOf";
		}
		if (leftRight) {
			result = result + "leftOf";
		}
		if (topEdgeAligned) {
			result = result + "topAlign";
		}
		if (bottomEdgeAligned) {
			result = result + "bottomAlign";
		}
		if (leftEdgeAligned) {
			result = result + "leftAlign";
		}
		if (rightEdgeAligned) {
			result = result + "rightAlign";
		}
		return result;
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
	
	public boolean isAlignmentTheSame(Sibling s2) {
		if (this == null || s2 == null) {
			System.out.println(this);
			System.out.println(s2);
			return false;
		} else if (this.getNode1().getxPath().equals(s2.getNode1().getxPath())) {
			if (this.isBelow() != s2.isBelow()) {
				return false;
			} else if (this.isAbove() != s2.isAbove()) {
				return false;
			} else if (this.isLeftOf() != s2.isLeftOf()) {
				return false;
			} else if (this.isRightOf() != s2.isRightOf()) {
				return false;
			} else if (this.isLeftEdgeAligned() != s2.isLeftEdgeAligned()) {
				return false;
			} else if (this.isRightEdgeAligned() != s2.isRightEdgeAligned()) {
				return false;
			} else if (this.isTopEdgeAligned() != s2.isTopEdgeAligned()) {
				return false;
			} else if (this.isBottomEdgeAligned() != s2.isBottomEdgeAligned()) {
				return false;
			} else {
				return true;
			}
		} else {
			if (this.isBelow() != s2.isAbove()) {
				return false;
			} else if (this.isAbove() != s2.isBelow()) {
				return false;
			} else if (this.isLeftOf() != s2.isRightOf()) {
				return false;
			} else if (this.isRightOf() != s2.isLeftOf()) {
				return false;
			} else if (this.isLeftEdgeAligned() != s2.isLeftEdgeAligned()) {
				return false;
			} else if (this.isRightEdgeAligned() != s2.isRightEdgeAligned()) {
				return false;
			} else if (this.isTopEdgeAligned() != s2.isTopEdgeAligned()) {
				return false;
			} else if (this.isBottomEdgeAligned() != s2.isBottomEdgeAligned()) {
				return false;
			} else {
				return true;
			}
		}
		
	}

	public boolean getRightLeft() {
		return rightLeft;
	}
	
	public boolean getLeftRight() {
		return leftRight;
	}
	
	public boolean getTopBottom() {
		return topBottom;
	}
	
	public boolean getBottomTop() {
		return bottomTop;
	}

	public double getGap() {
		int[] coords1 = node1.getDomNode().getCoords();
		int[] coords2 = node2.getDomNode().getCoords();
		if (rightLeft) {
			return coords1[0] - coords2[2];
		} else if (leftRight) {
			return coords2[0] - coords1[2];
		} else if (topBottom) {
			return coords2[1] - coords1[3];
		} else if (bottomTop) {
			return coords1[1] - coords2[3];
		}
		return 0;
	}
	
	public boolean isLeftOf() {
		int[] sib1 = node1.domNode.getCoords();
		int[] sib2 = node2.domNode.getCoords();
		if (leftRight) {
			return true;
		} else if (sib1[2]-1 <= sib2[0]) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isRightOf() {
		int[] sib1 = node1.domNode.getCoords();
		int[] sib2 = node2.domNode.getCoords();
		if (rightLeft) {
			return true;
		} else if (sib1[0] >= sib2[2]-1) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isAbove() {
		int[] sib1 = node1.domNode.getCoords();
		int[] sib2 = node2.domNode.getCoords();
		if (topBottom) {
			return true;
		} else if (sib1[3]-1 <= sib2[1]) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isBelow() {
		int[] sib1 = node1.domNode.getCoords();
		int[] sib2 = node2.domNode.getCoords();
		if (bottomTop) {
			return true;
		} else if (sib1[1] >= sib2[3]-1) {
			return true;
		} else { 
			return false;
		}
	}

	public String getFancyAttributes() {
		return "{above:" + isAbove() + ", below:" + isBelow()
				+ ", leftOf:" + isLeftOf() + ", rightOf:" + isRightOf() + "}";
	}

}
