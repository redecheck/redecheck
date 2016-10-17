package twalsh.clustering;

import twalsh.analysis.ResponsiveLayoutError;

import java.util.ArrayList;

/**
 * Created by thomaswalsh on 17/10/2016.
 */
public class Cluster {
    ArrayList<ResponsiveLayoutError> failures;

    public Cluster(ResponsiveLayoutError error) {
        failures = new ArrayList<>();
        failures.add(error);
    }

    public ArrayList<ResponsiveLayoutError> getFailures() {
        return failures;
    }

    public void setFailures(ArrayList<ResponsiveLayoutError> failures) {
        this.failures = failures;
    }
}
