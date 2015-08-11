package twalsh.rlg;

/**
 * Created by thomaswalsh on 10/08/15.
 */
public class VisibilityConstraint {
    public int appear;
    public int disappear;

    public VisibilityConstraint(int a, int d) {
        this.appear = a;
        this.disappear = d;
    }

    public void setDisappear(int d) {
        this.disappear = d;
    }
}
