package twalsh.rlg;

/**
 * Created by thomaswalsh on 10/08/15.
 * Last modified on 08/09/15.
 */
public class WidthConstraint {
    // Instance variables
    int min, max;
    double percentage;
    Node parent;
    double adjustment;

    /**
     * Constructor for the WidthConstraint object
     * @param min       the lower bound viewport width at which the constraint holds
     * @param max       the upper bound viewport width at which the constraint holds
     * @param p         the 'percentage' coefficient of the constraint
     * @param parent    the parent element of the node to which this constraint applies
     * @param a         the 'adjustment' coefficient of the constraint
     */
    public WidthConstraint(int min, int max, double p, Node parent, double a) {
        this.min = min;
        this.max = max;
        this.percentage = p;
        this.parent = parent;
        this.adjustment = a;
    }

    /**
     * Creates a formatted string to nicely display the attributes of the constraint
     * @return          the formatted string for the constraint
     */
    public String toString() {
        return min + " --> " + max + " : " + percentage*100 + "% of " + parent.xpath + " + " + adjustment;
    }
}
