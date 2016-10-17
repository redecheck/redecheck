package twalsh.clustering;

import twalsh.analysis.ResponsiveLayoutError;
import twalsh.layout.Element;
import twalsh.rlg.Node;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by thomaswalsh on 17/10/2016.
 */
public class Cluster {
    ArrayList<ResponsiveLayoutError> failures;
    HashSet<Node> nodes;

    public Cluster(ResponsiveLayoutError error) {
        failures = new ArrayList<>();
        failures.add(error);
        nodes.addAll(error.getNodes());
    }

    public ArrayList<ResponsiveLayoutError> getFailures() {
        return failures;
    }

    public void setFailures(ArrayList<ResponsiveLayoutError> failures) {
        this.failures = failures;
    }
}
