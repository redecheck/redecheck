package twalsh.rlg;

/**
 * Created by thomaswalsh on 10/08/15.
 */
public class ParentChild {
    Node parent;
    Node child;
    boolean topJust,bottomJust,middleJust,leftJust,rightJust,centerJust;
    //	int[] insets = new int[4];
    int min, max;

    public ParentChild(Node p, Node c, int min, int max, boolean lj,boolean rj,boolean cj,boolean tj, boolean bj, boolean mj) {
        parent = p;
        child = c;
        this.min = min;
        this.max = max;
        this.topJust = tj;
        this.bottomJust =bj;
        this.middleJust = mj;
        this.leftJust = lj;
        this.rightJust = rj;
        this.centerJust = cj;
    }

    public boolean isTopJust() {
        return topJust;
    }

    public void setTopJust(boolean topJust) {
        this.topJust = topJust;
    }

    public boolean isBottomJust() {
        return bottomJust;
    }

    public void setBottomJust(boolean bottomJust) {
        this.bottomJust = bottomJust;
    }

    public boolean isMiddleJust() {
        return middleJust;
    }

    public void setMiddleJust(boolean middleJust) {
        this.middleJust = middleJust;
    }

    public boolean isLeftJust() {
        return leftJust;
    }

    public void setLeftJust(boolean leftJust) {
        this.leftJust = leftJust;
    }

    public boolean isRightJust() {
        return rightJust;
    }

    public void setRightJust(boolean rightJust) {
        this.rightJust = rightJust;
    }

    public boolean isCenterJust() {
        return centerJust;
    }

    public void setCenterJust(boolean centerJust) {
        this.centerJust = centerJust;
    }

    public Node getParent() {
        return parent;
    }

    public Node getChild() {
        return child;
    }

    //	public int[] getInsets() {
//		return insets;
//	}
//
    public String toString() {
//		String result = "Between " + min + " and " + max + "   ";
        String result = min + " -> " + max + " : ";
        if ((parent != null) && (child != null)) {
            result += parent.getXpath() + " contains " + child.getXpath();
        } else if ((parent == null) && (child != null)) {
            result += "parent was null here!   " + child;
        } else if ((parent != null) && (child == null)) {
            result += "child was null here!    " + parent;
        }
        return result + getAlignmentString();
    }

    public String getAlignmentString() {
        String result = min + " -> " + max + " : ";

        if (centerJust)
            result+=" CJ,";
        if (leftJust)
            result += " LJ,";
        if (rightJust)
            result += " RJ,";
        if(middleJust)
            result+= " MJ,";
        if (topJust)
            result+= " TJ,";
        if (bottomJust)
            result+= " BJ";

        return result;
    }
}
