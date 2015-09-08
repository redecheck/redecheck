package twalsh.rlg;

/**
 * Created by thomaswalsh on 10/08/15.
 */
public class VisibilityConstraint {
    // Instance variables
    public int appear;
    public int disappear;

    /**
     * Constructs a new visibility constraint which can be added to a node
     * @param a     the lower bound/appear point of the element
     * @param d     the upper bound/disappear point of the element
     */
    public VisibilityConstraint(int a, int d) {
        this.appear = a;
        this.disappear = d;
    }

    /**
     * Setter for the disappear attribute
     * @param d     the new value for the disappear attribute
     */
    public void setDisappear(int d) {
        this.disappear = d;
    }

    /**
     * Getter for the disappear attribute
     * @return      the current value of the disappear attribute
     */
    public int getDisappear() {
        return disappear;
    }

    /**
     * Creates a simple string to represent the visibility constraint
     * @return      the string presenting the attributes of the constraint
     */
    public String toString() {
        return this.appear + " -> " + this.disappear;
    }
}
