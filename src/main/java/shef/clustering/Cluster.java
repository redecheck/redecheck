package shef.clustering;

import shef.reporting.inconsistencies.ResponsiveLayoutFailure;
import shef.rlg.Node;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by thomaswalsh on 17/10/2016.
 */
public class Cluster {
    ArrayList<ResponsiveLayoutFailure> failures;
    HashSet<Node> nodes;

    public Cluster(ResponsiveLayoutFailure error) {
        failures = new ArrayList<>();
        nodes = new HashSet<>();
        failures.add(error);
        nodes.addAll(error.getNodes());
    }

    public ArrayList<ResponsiveLayoutFailure> getFailures() {
        return failures;
    }

    public void setFailures(ArrayList<ResponsiveLayoutFailure> failures) {
        this.failures = failures;
    }

    public String toString() {
        String result = "CLUSTER OF " + failures.size() + " FAILURES";
        for (ResponsiveLayoutFailure rlf : failures) {
            result += "\n\t" + rlf;
        }
        return result;
    }

    public void add(ResponsiveLayoutFailure rlf) {
        this.failures.add(rlf);
    }
}
