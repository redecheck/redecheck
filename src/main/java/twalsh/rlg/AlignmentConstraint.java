package twalsh.rlg;

/**
 * Created by thomaswalsh on 12/08/15.
 */
public class AlignmentConstraint {
    public Node node1, node2;
    public Type type;
    int min, max;
    boolean[] attributes;

    public AlignmentConstraint(Node f, Node s, Type t, int min, int max) {
        this.node1 = f;
        this.node2 = s;
        this.type = t;
        this.min = min;
        this.max = max;
        if (t == Type.PARENT_CHILD) {
            attributes = new boolean[6];
        } else {
            attributes = new boolean[8];
        }
    }

    public String toString() {
        return node1.xpath + " , " + node2.xpath + " , " + type + " , " + min + " , " + max;
    }

    public String generateKey() {
        String t = "";
        if (type == Type.PARENT_CHILD) {
            t = "contains";
        } else {
            t = "sibling";
        }
        return node1.xpath + node2.xpath + t;
    }

    public String generateLabelling() {
        String result = "";
        if (type == Type.PARENT_CHILD) {
            if (attributes[0])
                result+="centered";
            if (attributes[1])
                result += "leftJust";
            if (attributes[2])
                result += "rightJust";
            if(attributes[3])
                result+= "middle";
            if (attributes[4])
                result+= "top";
            if (attributes[5])
                result+= "bottom";
        } else {
            if (attributes[0]) {
                result = result + "below";
            }
            if (attributes[1]) {
                result = result + "above";
            }
            if (attributes[2]) {
                result = result + "leftOf";
            }
            if (attributes[3]) {
                result = result + "rightOf";
            }
            if (attributes[4]) {
                result = result + "topAlign";
            }
            if (attributes[5]) {
                result = result + "bottomAlign";
            }
            if (attributes[6]) {
                result = result + "leftAlign";
            }
            if (attributes[7]) {
                result = result + "rightAlign";
            }
        }
        return result;
    }

    public int getMax() { return max; }

    public void setMax(int m) {
        this.max = m;
    }
}
