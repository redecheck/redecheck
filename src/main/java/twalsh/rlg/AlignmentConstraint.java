package twalsh.rlg;

/**
 * Created by thomaswalsh on 12/08/15.
 */
public class AlignmentConstraint {
    public Node node1, node2;
    public Type type;
    int min, max;

    public AlignmentConstraint(Node f, Node s, Type t, int min, int max) {
        this.node1 = f;
        this.node2 = s;
        this.type = t;
        this.min = min;
        this.max = max;
    }

    public String toString() {
        return node1.xpath + " , " + node2.xpath + " , " + type + " , " + min + " , " + max;
    }

    public String generateKey() {
        return node1.xpath + node2.xpath + type;
    }

    public void setMax(int m) {
        this.max = m;
    }
}
