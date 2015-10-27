package twalsh.rlg;

/**
 * Created by thomaswalsh on 12/08/15.
 * Last modified on 08/09/15
 */
public class AlignmentConstraint implements Comparable<AlignmentConstraint> {
    // Instance variables
    public Node node1, node2;
    public Type type;
    int min, max;
    boolean[] attributes;

    /**
     * Constructor for the AlignmentConstraint object
     * @param f         the first node (the parent if it's a parent-child constraint)
     * @param s         the second node (the child if it's a parent-child constraint)
     * @param t         the type of the constraint (parent-child or sibling)
     * @param min       the lower bound resolution for which the constraint holds
     * @param max       the upper bound resolution for which the constraint holds
     * @param atts      the set of additional alignment attributes
     */
    public AlignmentConstraint(Node f, Node s, Type t, int min, int max, boolean[] atts) {
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
        this.attributes = atts;
    }

    /**
     * Generates a formatted string to print out the alignment constraint
     * @return      the formatted string
     */
    public String toString() {
    	try {
    		return node1.xpath + " , " + node2.xpath + " , " + type + " , " + min + " , " + max + " , " + this.generateLabelling();
    	} catch (NullPointerException e) {
    		System.out.println("Node 1: " + node1.getXpath());
//    		System.out.println("Node 2: " + node2);
    		System.out.println(type);
    		System.out.println(min);
    		System.out.println(max);
    	}
    	return "";
    }

    public Node getNode1() {
        return node1;
    }

    public void setNode1(Node node1) {
        this.node1 = node1;
    }

    public Node getNode2() {
        return node2;
    }

    public void setNode2(Node node2) {
        this.node2 = node2;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public boolean[] getAttributes() {
        return attributes;
    }

    public void setAttributes(boolean[] attributes) {
        this.attributes = attributes;
    }

    /**
     * Utility function to generate a basic key to represent the constraint
     * @return      the basic key
     */
    public String generateKeyWithoutLabels() {
        String t = "";
        if (type == Type.PARENT_CHILD) {
            t = "contains";
            return node2.xpath + node1.xpath + t;
        } else {
            t = "sibling";
            return node1.xpath + node2.xpath + t;
        }

    }

    /**
     * Utility function to generate a full key representing the constraints and it's attributes
     * @return      the full key representing the constraint
     */
    public String generateKey() {
        String t = "";
        try {
	        if (type == Type.PARENT_CHILD) {
	            t = "contains";
	            return node2.xpath + node1.xpath + t + generateLabelling();
	        } else {
	            t = "sibling";
	            return node1.xpath + node2.xpath + t + generateLabelling();
	        }
        } catch (NullPointerException e) {
//        	System.out.println(node1 + "    " + node2);
        }
        return "";

    }

    /**
     * Utility function which uses the set of alignment attributes to generate the labelling string for the key.
     * @return      the labelling string
     */
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

    /**
     * Getter for the max attribute
     * @return      the upper bound of the constraint
     */
    public int getMax() { return max; }

    /**
     * Setter for the max attribute
     * @param m     the new value for the max attribute
     */
    public void setMax(int m) {
        this.max = m;
    }

    /**
     * Getter for the min attribute
     * @return      the lower bound of the constraint
     */
    public int getMin() { return min; }

    /**
     * Utility function used to sort the constraints into an order for printing in the report
     * @param ac2   the constraint being compared against
     * @return      whether the object should be arranged before or after the one being compared against.
     */
    public int compareTo(AlignmentConstraint ac2) {
        String key1 = this.generateKeyWithoutLabels();
        String key2 = ac2.generateKeyWithoutLabels();
        return key1.compareTo(key2);
    }
}
