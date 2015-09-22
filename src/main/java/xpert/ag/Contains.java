package xpert.ag;

import xpert.dom.DomNode;

public class Contains extends Edge {

	AGNode parent, child;
	boolean hFill, vFill;
	boolean leftJustified, rightJustified, centered;
	boolean topAligned, bottomAligned, middle;

	boolean sizeDiffX, sizeDiffY;
	double xError, yError;

	// Thresholds for Parent-Child Size Differential
	double SIZE_DIFF_THRESH = 0.7, SIZE_DIFF_IGNORE = 0.1;

	int dH_delta = 5, dW_delta = 5;

	public Contains(AGNode parent, AGNode child) {
		this.parent = parent;
		this.child = child;
		
		populateAlignments();
	}

	public void populateAlignments() {
		
		int px = (parent.x1 + parent.x2) / 2;
		int py = (parent.y1 + parent.y2) / 2;
		int cx = (child.x1 + child.x2) / 2;
		int cy = (child.y1 + child.y2) / 2;

		int pw = parent.x2 - parent.x1;
		int cw = child.x2 - child.x1;
		int dW = cw / 3;

		int ph = parent.y2 - parent.y1;
		int ch = child.y2 - child.y1;
		int dH = ch / 3;

		// Ignore small children
		if (cw < 15 && pw < 15) {
			return;
		}

		// TODO

		if (hasSignificantSizeDiff(pw, cw)) {
			setSizeDiffX(true);

			if (equals(px, cx, dW_delta)
					&& equals(child.x1, parent.x1, dW_delta)
					&& equals(child.x2, parent.x2, dW_delta)) {
				sethFill(true);
			} else {
				if (equals(child.x1, parent.x1, dW)) {
					setLeftJustified(true);
					setxError(calcError(child.x1, parent.x1, dW));
				} else if (equals(child.x2, parent.x2, dW)) {
					setRightJustified(true);
					setxError(calcError(child.x2, parent.x2, dW));
				} else if (equals(px, cx, dW)) {
					setCentered(true);
					setxError(calcError(px, cx, dW));
				}	
			}
		}

		if (hasSignificantSizeDiff(ph, ch)) {
			setSizeDiffY(true);
			
			if (equals(py, cy, dH_delta) && equals(child.y1, parent.y1, dH_delta)
					&& equals(child.y2, parent.y2, dH_delta)) {
				setvFill(true);
			} else{
				if (equals(child.y1, parent.y1, dH)) {
					setTopAligned(true);
					setyError(calcError(child.y1, parent.y1, dH))	;
				} else if (equals(child.y2, parent.y2, dH)) {
					setBottomAligned(true);
					setyError(calcError(child.y2, parent.y2, dH));
				} else if (equals(py, cy, dH)) {
					setMiddle(true);
					setyError(calcError(py, cy, dH));
				}
			}
		}

	}
	


	private boolean hasSignificantSizeDiff(double p, double c) {
		double pcSizeDiff = c / p;
		if (pcSizeDiff < SIZE_DIFF_THRESH && pcSizeDiff > SIZE_DIFF_IGNORE) {
			return true;
		}
		return false;
	}
	
	private double calcError(int a, int b, int delta) {
		return ((double)Math.abs(a-b))/delta;
	}

	@Override
	public DomNode getNode1() {
		return child.domNode;
	}

	@Override
	public DomNode getNode2() {
		return parent.domNode;
	}

	@Override
	public String getAttributes() {
		return parent.domNode.getAttributes().toString();
	}
	
	public String getClassName() {
		return parent.domNode.getAttributes().get("class");
	}
	
	public String getId() {
		return parent.domNode.getId();
	}

	@Override
	public String toString() {
		String result= child.domNode.getxPath() + " contained in "
				+ parent.domNode.getxPath();
		if (centered)
			result+="  centered";
		if (leftJustified)
			result += "  leftJust";
		if (rightJustified)
			result += "  rightJust";
		if(middle)
			result+= "  middle";
		if (topAligned)
			result+= "  top";
		if (bottomAligned)
			result+= "  bottom";
		
		return result;
	}

	public String generateLabelling() {
		String result = "";
		if (centered)
			result+="centered";
		if (leftJustified)
			result += "leftJust";
		if (rightJustified)
			result += "rightJust";
		if(middle)
			result+= "middle";
		if (topAligned)
			result+= "top";
		if (bottomAligned)
			result+= "bottom";
		return result;
	}

	/**
	 * Setters & Getters
	 */

	/**
	 * Returns the parent of the edge
	 * @return		the parent node
	 */
	public AGNode getParent() {
		return parent;
	}

	public void setParent(AGNode parent) {
		this.parent = parent;
	}

	public AGNode getChild() {
		return child;
	}

	public void setChild(AGNode child) {
		this.child = child;
	}

	public boolean ishFill() {
		return hFill;
	}

	public void sethFill(boolean hFill) {
		this.hFill = hFill;
	}

	public boolean isvFill() {
		return vFill;
	}

	public void setvFill(boolean vFill) {
		this.vFill = vFill;
	}

	public boolean isLeftJustified() {
		return leftJustified;
	}

	public void setLeftJustified(boolean leftJustified) {
		this.leftJustified = leftJustified;
	}

	public boolean isRightJustified() {
		return rightJustified;
	}

	public void setRightJustified(boolean rightJustified) {
		this.rightJustified = rightJustified;
	}

	public boolean isCentered() {
		return centered;
	}

	public void setCentered(boolean centered) {
		this.centered = centered;
	}

	public boolean isTopAligned() {
		return topAligned;
	}

	public void setTopAligned(boolean topAligned) {
		this.topAligned = topAligned;
	}

	public boolean isBottomAligned() {
		return bottomAligned;
	}

	public void setBottomAligned(boolean bottomAligned) {
		this.bottomAligned = bottomAligned;
	}

	public boolean isMiddle() {
		return middle;
	}

	public void setMiddle(boolean middle) {
		this.middle = middle;
	}

	public boolean isSizeDiffX() {
		return sizeDiffX;
	}

	public void setSizeDiffX(boolean sizeDiffX) {
		this.sizeDiffX = sizeDiffX;
	}

	public boolean isSizeDiffY() {
		return sizeDiffY;
	}

	public void setSizeDiffY(boolean sizeDiffY) {
		this.sizeDiffY = sizeDiffY;
	}

	public double getxError() {
		return xError;
	}

	public void setxError(double xError) {
		this.xError = xError;
	}

	public double getyError() {
		return yError;
	}

	public void setyError(double yError) {
		this.yError = yError;
	}

	public double getSIZE_DIFF_THRESH() {
		return SIZE_DIFF_THRESH;
	}

	public void setSIZE_DIFF_THRESH(double sIZE_DIFF_THRESH) {
		SIZE_DIFF_THRESH = sIZE_DIFF_THRESH;
	}

	public double getSIZE_DIFF_IGNORE() {
		return SIZE_DIFF_IGNORE;
	}

	public void setSIZE_DIFF_IGNORE(double sIZE_DIFF_IGNORE) {
		SIZE_DIFF_IGNORE = sIZE_DIFF_IGNORE;
	}

	public int getConst_dH() {
		return dH_delta;
	}

	public void setConst_dH(int const_dH) {
		this.dH_delta = const_dH;
	}

	public int getConst_dW() {
		return dW_delta;
	}

	public void setConst_dW(int const_dW) {
		this.dW_delta = const_dW;
	}

	public boolean isAlignmentTheSame(Contains toMatch) {
		if (this.isTopAligned() != toMatch.isTopAligned()) {
			return false;
		} else if (this.isBottomAligned() != toMatch.isBottomAligned()) {
			return false;
		} else if (this.isMiddle() != toMatch.isMiddle()) {
			return false;
		} else if (this.isRightJustified() != toMatch.isRightJustified()) {
			return false;
		} else if (this.isLeftJustified() != toMatch.isLeftJustified()) {
			return false;
		} else if (this.isCentered() != toMatch.isCentered()) {
			return false;
		} else {
			return true;
		}
	}

	public void debug() {
		int px = (parent.x1 + parent.x2) / 2;
		int py = (parent.y1 + parent.y2) / 2;
		int cx = (child.x1 + child.x2) / 2;
		int cy = (child.y1 + child.y2) / 2;

		int pw = parent.x2 - parent.x1;
		int cw = child.x2 - child.x1;
		int dW = cw / 3;

		int ph = parent.y2 - parent.y1;
		int ch = child.y2 - child.y1;
		int dH = ch / 3;
//		System.out.println((equals(py, cy, dH_delta) && equals(child.y1, parent.y1, dH_delta)
//				&& equals(child.y2, parent.y2, dH_delta)));
//		
//		System.out.println(child.y1 + " against " + parent.y1 + "  with delta " + dH);
//		System.out.println(equals(child.y1, parent.y1,dH));
		System.out.println(ph + " against " + ch);
		System.out.println(hasSignificantSizeDiff(ph, ch));
		if (hasSignificantSizeDiff(ph, ch)) {
			setSizeDiffY(true);
			System.out.println("Diff size");
			if (equals(py, cy, dH_delta) && equals(child.y1, parent.y1, dH_delta) && equals(child.y2, parent.y2, dH_delta)) {
				System.out.println("Fills vertically");
				setvFill(true);
			} else{
				System.out.println(child.y1 + " against " + parent.y1 + "  with delta " + dH);
				if (equals(child.y1, parent.y1, dH)) {
					System.out.println("TOP ALIGNED");
					setTopAligned(true);
					setyError(calcError(child.y1, parent.y1, dH))	;
				} else if (equals(child.y2, parent.y2, dH)) {
					setBottomAligned(true);
					setyError(calcError(child.y2, parent.y2, dH));
				} else if (equals(py, cy, dH)) {
					setMiddle(true);
					setyError(calcError(py, cy, dH));
				}
			}
		}
	}

}
