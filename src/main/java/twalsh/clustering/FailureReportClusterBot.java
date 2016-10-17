package twalsh.clustering;

import twalsh.analysis.ResponsiveLayoutError;

import java.util.ArrayList;

/**
 * Created by thomaswalsh on 17/10/2016.
 */
public class FailureReportClusterBot {
    ArrayList<ResponsiveLayoutError> failures;
    public FailureReportClusterBot(ArrayList<ResponsiveLayoutError> errors) {
        failures = errors;
        ArrayList<Cluster> clusters = cluster();
    }

    private ArrayList<Cluster> cluster() {
        ArrayList<Cluster> clusters = new ArrayList<>();
        
        return clusters;
    }

}
