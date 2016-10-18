package twalsh.clustering;

import twalsh.analysis.ResponsiveLayoutFailure;

import java.util.ArrayList;

/**
 * Created by thomaswalsh on 17/10/2016.
 */
public class FailureReportClusterBot {
    ArrayList<ResponsiveLayoutFailure> failures;
    public FailureReportClusterBot(ArrayList<ResponsiveLayoutFailure> errors) {
        failures = errors;
        ArrayList<Cluster> clusters = cluster();
        System.out.println(clusters.size());
    }

    private ArrayList<Cluster> cluster() {
        ArrayList<Cluster> clusters = new ArrayList<>();
        for (ResponsiveLayoutFailure e : failures) {
            Cluster closestCluster = calculateClosestCluster(e, failures);
            if (closestCluster == null) {
                Cluster newCluster = new Cluster(e);
                clusters.add(newCluster);
            }
        }
        return clusters;
    }

    private Cluster calculateClosestCluster(ResponsiveLayoutFailure e, ArrayList<ResponsiveLayoutFailure> failures) {
        Cluster closest = null;


        return closest;
    }

}
