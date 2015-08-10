package twalsh.rlg;

/**
 * Created by thomaswalsh on 10/08/15.
 */
public class WidthConstraint {
    int min, max;
    double percentage;
    Node parent;
    double adjustment;

    public WidthConstraint(int min, int max, double p, Node parent, double a) {
        this.min = min;
        this.max = max;
        this.percentage = p;
        this.parent = parent;
        this.adjustment = a;
    }
}
