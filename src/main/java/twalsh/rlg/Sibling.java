package twalsh.rlg;

/**
 * Created by thomaswalsh on 10/08/15.
 */
public class Sibling {
    Node first;
    Node second;
    int min, max;
    int gap;
    boolean leftOf, rightOf, above, below, leftAlign, rightAlign, topAlign, bottomAlign;

    public Sibling(Node f, Node s, int min, int max, boolean lo, boolean ro, boolean above, boolean below, int gap, boolean leftAlign, boolean rightAlign, boolean topAlign, boolean bottomAlign) {
        this.first = f;
        this.second = s;
        this.min = min;
        this.max = max;
        this.leftOf = lo;
        this.rightOf = ro;
        this.above = above;
        this.below = below;
        this.gap = gap;
        this.leftAlign = leftAlign;
        this.rightAlign = rightAlign;
        this.topAlign = topAlign;
        this.bottomAlign = bottomAlign;
    }

    public Sibling(Node f, Node s) {
        this.first = f;
        this.second = s;
    }
}
