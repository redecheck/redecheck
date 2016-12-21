package shef.layout;

/**
 * Created by thomaswalsh on 13/05/2016.
 */
public class ParentChild extends Relationship {
    public Element getNode1() {
//        PARENT
        return parent;
    }

    public Element getNode2() {
//        CHILD
        return child;
    }

    Element parent, child;

    public boolean isTopJust() {
        return topJust;
    }

    public boolean isMiddleJust() {
        return middleJust;
    }

    public boolean isBottomJust() {
        return bottomJust;
    }

    public boolean isLeftJust() {
        return leftJust;
    }

    public boolean isCentreJust() {
        return centreJust;
    }

    public boolean isRightJust() {
        return rightJust;
    }

    boolean topJust;
    boolean middleJust;
    boolean bottomJust;
    boolean leftJust;
    boolean centreJust;
    boolean rightJust;

    public void setTopJust(boolean topJust) {
        this.topJust = topJust;
    }

    public void setMiddleJust(boolean middleJust) {
        this.middleJust = middleJust;
    }

    public void setBottomJust(boolean bottomJust) {
        this.bottomJust = bottomJust;
    }

    public void setLeftJust(boolean leftJust) {
        this.leftJust = leftJust;
    }

    public void setCentreJust(boolean centreJust) {
        this.centreJust = centreJust;
    }

    public void setRightJust(boolean rightJust) {
        this.rightJust = rightJust;
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

    boolean hFill;
    boolean vFill;
    int dH_delta = 5, dW_delta = 5;;

    public ParentChild(Element parent, Element child) {
        this.parent = parent;
        this.child = child;
        assignAttributes();
    }

    private void assignAttributes() {
        int px = (parent.x1 + parent.x2) / 2;
        int py = (parent.y1 + parent.y2) / 2;
        int cx = (child.x1 + child.x2) / 2;
        int cy = (child.y1 + child.y2) / 2;

        int pw = parent.x2 - parent.x1;
        int cw = child.x2 - child.x1;
        int dW = 3;
//        int dW = cw / 3;

        int ph = parent.y2 - parent.y1;
        int ch = child.y2 - child.y1;
        int dH = 3;
//        int dH = ch / 3;


        if (equals(px, cx, dW_delta)
                && equals(child.x1, parent.x1, dW_delta)
                && equals(child.x2, parent.x2, dW_delta)) {
            sethFill(true);
        } else {
            if (equals(px, cx, dW)) {
                setCentreJust(true);
//                String mLeft = child.styles.get("margin-left");
//                String mRight = child.styles.get("margin-right");
//                if (child.getXpath().equals("/HTML/BODY/DIV[3]/DIV/DIV[2]/DIV/DIV/DIV/DIV[9]")) {
//                    System.out.println(mLeft + " " + mRight);
//                }
//                if (mLeft.equals("0px") && mRight.equals("0px")) {
////                    System.out.println(child.getXpath() + " was intentionally centralized");
//                } else {
//                    System.out.println(child.getXpath() + " was coincidentally centralized");
//                }
            } else if (equals(child.x1, parent.x1, dW)) {
                setLeftJust(true);
            } else if (equals(child.x2, parent.x2, dW)) {
                setRightJust(true);
            }
        }

        if (equals(py, cy, dH_delta) && equals(child.y1, parent.y1, dH_delta)
                && equals(child.y2, parent.y2, dH_delta)) {
            setvFill(true);
        } else{
            if (equals(py, cy, dH)) {
                setMiddleJust(true);
            } else if (equals(child.y1, parent.y1, dH)) {
                setTopJust(true);
            } else if (equals(child.y2, parent.y2, dH)) {
                setBottomJust(true);
            }
        }

    }

    public String getKey() {
        return parent.getXpath() + " contains " + child.getXpath() + generateAttributeSet();
    }

    private String generateAttributeSet() {
        String result = " {";
        if (isCentreJust())
            result+="centered";
        if (isLeftJust())
            result += "leftJust";
        if (isRightJust())
            result += "rightJust";
        if (isMiddleJust())
            result+= "middle";
        if (isTopJust())
            result+= "top";
        if (isBottomJust())
            result+= "bottom";

        return result + "}";
    }

    public boolean[] generateAttributeArray() {
        return new boolean[] {centreJust, leftJust, rightJust, middleJust, topJust, bottomJust};
    }

    public String toString() {
        return getKey();
    }
}
