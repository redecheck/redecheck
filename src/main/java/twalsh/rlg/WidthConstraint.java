package twalsh.rlg;

/**
 * Created by thomaswalsh on 10/08/15.
 * Last modified on 08/09/15.
 */
public class WidthConstraint {
    // Instance variables
    int min;
    int max;
    double percentage;
    Node parent;
    double adjustment;

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public double getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(double adjustment) {
        this.adjustment = adjustment;
    }

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
//        System.out.println(Double.isNaN(p));
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

    public String generateEquationString() {
        return percentage*100 + "% of " + parent.xpath + " + " + adjustment;
    }
}
