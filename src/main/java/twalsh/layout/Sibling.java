package twalsh.layout;

import java.awt.*;

/**
 * Created by thomaswalsh on 13/05/2016.
 */
public class Sibling extends Relationship {
    public Element getNode1() {
        return node1;
    }

    public Element getNode2() {
        return node2;
    }

    Element node1, node2;
    int deltaW = 3, deltaH = 3;

    public boolean isLeftOf() {
        return leftOf;
    }

    public boolean isRightOf() {
        return rightOf;
    }

    public boolean isAbove() {
        return above;
    }

    public boolean isBelow() {
        return below;
    }

    public boolean isLeftEdge() {
        return leftEdge;
    }

    public boolean isRightEdge() {
        return rightEdge;
    }

    public boolean isTopEdge() {
        return topEdge;
    }

    public boolean isBottomEdge() {
        return bottomEdge;
    }

    public boolean isOverlapping() {
        return overlapping;
    }

    public void setLeftOf(boolean leftOf) {
        this.leftOf = leftOf;
    }

    public void setRightOf(boolean rightOf) {
        this.rightOf = rightOf;
    }

    public void setAbove(boolean above) {
        this.above = above;
    }

    public void setBelow(boolean below) {
        this.below = below;
    }

    public void setLeftEdge(boolean leftEdge) {
        this.leftEdge = leftEdge;
    }

    public void setRightEdge(boolean rightEdge) {
        this.rightEdge = rightEdge;
    }

    public void setTopEdge(boolean topEdge) {
        this.topEdge = topEdge;
    }

    public void setBottomEdge(boolean bottomEdge) {
        this.bottomEdge = bottomEdge;
    }

    public void setOverlapping(boolean overlapping) {
        this.overlapping = overlapping;
    }

    boolean leftOf;
    boolean rightOf;
    boolean above;
    boolean below;
    boolean leftEdge;
    boolean rightEdge;
    boolean topEdge;
    boolean bottomEdge;
    boolean overlapping;



    boolean xMid;
    boolean yMid;

    public Sibling (Element n1, Element n2) {
        node1 = n1;
        node2 = n2;
        assignAttributes();
    }

    private void assignAttributes() {
        int midx1 = (node1.x2+node1.x1) /2;
        int midx2 = (node2.x2+node2.x1) /2;
        int midy1 = (node1.y2+node1.y1) /2;
        int midy2 = (node2.y2+node2.y1) /2;



        if (equals(node1.x1, node2.x1, deltaW)) {
            setLeftEdge(true);
        }

        if (equals(node1.x2, node2.x2, deltaW)) {
            setRightEdge(true);
        }

        if (equals(node1.y1, node2.y1, deltaH)) {
            setTopEdge(true);
        }

        if (equals(node1.y2, node2.y2, deltaH)) {
            setBottomEdge(true);
        }

        if (equals(midx1, midx2, deltaW)) {
            setxMid(true);
        }

        if (equals(midy1, midy2, deltaH)) {
            setyMid(true);
        }

//        if (node1.x2 <= midx2) {
//            setLeftOf(true);
//        }
//
//        if (node2.x2 <= midx1) {
//            setRightOf(true);
//        }

//        if (node1.x2 <= node2.x1) {
//            setLeftOf(true);
//        }
//
//        if (node2.x2 <= node1.x1) {
//            setRightOf(true);
//        }

        if ((midx1 <= node2.x1) && (node1.x2 < node2.x2)) {
            setLeftOf(true);
        }

        if ((node2.x2 <= midx1) && (node1.x1 > node2.x1)) {
            setRightOf(true);
        }

        if (node1.y2 <= midy2) {
            setAbove(true);
        }
        if (node2.y2 <= midy1) {
            setBelow(true);
        }

//        if (node1.y2 <= node2.y1) {
//            setAbove(true);
//        }
//        if (node2.y2 <= node1.y1) {
//            setBelow(true);
//        }

//        if ( (node1.y2 <= node2.y1 ) && (equals(midx1, midx2, deltaW*2))) {
//            setAbove(true);
//        }
//
//        if ( (node2.y2 <= node1.y1) && (equals(midx1, midx2, deltaW*2))) {
//            setBelow(true);
//        }

        Rectangle r1 = new Rectangle(node1.x1, node1.y1, node1.x2-node1.x1, node1.y2 - node1.y1);
        Rectangle r2 = new Rectangle(node2.x1, node2.y1, node2.x2-node2.x1, node2.y2 - node2.y1);
        Rectangle intersection = r1.intersection(r2);

        if (r1.intersects(r2)) {
            if ( (intersection.width > 1) && (intersection.height > 1)) {
                setOverlapping(true);
            }
        }

//        if (node1.getXpath().contains("DIV[17]") && node2.getXpath().contains("DIV[11]")) {
//            System.out.println(this);
//        }
    }

    public String getKey() {
        return node1.getXpath() + " sibling of " + node2.getXpath() + generateAttributeSet();
    }

    public String getFlipKey() { return node2.getXpath() + " sibling of " + node1.getXpath() + generateFlipAttributeSet();}

    private String generateFlipAttributeSet() {
        String result = " {";

        if (above) {
            result = result + "below";
        }
        if (below) {
            result = result + "above";
        }
        if (leftOf) {
            result = result + "rightOf";
        }
        if (rightOf) {
            result = result + "leftOf";
        }

        if (topEdge) {
            result = result + "topAlign";
        }
        if (bottomEdge) {
            result = result + "bottomAlign";
        }
        if (yMid) {
            result = result + "yMidAlign";
        }
        if (leftEdge) {
            result = result + "leftAlign";
        }
        if (rightEdge) {
            result = result + "rightAlign";
        }
        if (xMid) {
            result = result + "xMidAlign";
        }
        if (overlapping) {
            result = result + "overlapping";
        }
        return result + "}";
    }

    public String generateAttributeSet() {
        String result = " {";

        if (above) {
            result = result + "above";
        }
        if (below) {
            result = result + "below";
        }
        if (leftOf) {
            result = result + "leftOf";
        }
        if (rightOf) {
            result = result + "rightOf";
        }

        if (topEdge) {
            result = result + "topAlign";
        }
        if (bottomEdge) {
            result = result + "bottomAlign";
        }
        if (yMid) {
            result = result + "yMidAlign";
        }
        if (leftEdge) {
            result = result + "leftAlign";
        }
        if (rightEdge) {
            result = result + "rightAlign";
        }
        if (xMid) {
            result = result + "xMidAlign";
        }
        if (overlapping) {
            result = result + "overlapping";
        }
        return result + "}";
    }

    public boolean[] generateAttributeArray() {
        return new boolean[] {above, below, leftOf, rightOf, topEdge, bottomEdge, yMid, leftEdge, rightEdge, xMid, overlapping};
    }

    public boolean isxMid() {
        return xMid;
    }

    public void setxMid(boolean xMid) {
        this.xMid = xMid;
    }

    public boolean isyMid() {
        return yMid;
    }

    public void setyMid(boolean yMid) {
        this.yMid = yMid;
    }

    public String toString() {
        return getKey();
    }
}
