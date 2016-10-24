package twalsh.clustering;

import info.debatty.java.stringsimilarity.*;
import twalsh.analysis.ResponsiveLayoutFailure;
import twalsh.rlg.Node;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


/**
 * Created by thomaswalsh on 17/10/2016.
 */
public class FailureReportClusterBot {
    ArrayList<ResponsiveLayoutFailure> failures;
    public FailureReportClusterBot(ArrayList<ResponsiveLayoutFailure> errors) {
        failures = errors;
        ArrayList<Cluster> clusters = cluster();
//        for (Cluster cluster : clusters) {
//            System.out.println(cluster + "\n");
//        }
    }

    private ArrayList<Cluster> cluster() {
        ArrayList<Cluster> clusters = new ArrayList<>();
        for (ResponsiveLayoutFailure e : failures) {
            Cluster closestCluster = calculateClosestCluster(e, failures, clusters);
            if (closestCluster == null) {
                Cluster newCluster = new Cluster(e);
                clusters.add(newCluster);
            } else {
                closestCluster.add(e);
            }
        }
        return clusters;
    }

    private Cluster calculateClosestCluster(ResponsiveLayoutFailure e, ArrayList<ResponsiveLayoutFailure> failures, ArrayList<Cluster> clusters) {
        Cluster closest = null;

        for (Cluster c : clusters) {
            double similarity = calculateSimilarity(e, c);

        }

        return closest;
    }

    private double calculateSimilarity(ResponsiveLayoutFailure e, Cluster c) {
        double similarity = 0;
        int[] bounds = e.getBounds();
        Rectangle r1 = new Rectangle(bounds[0], 0, bounds[1]-bounds[0], 10);

        HashSet<Node> nodes = e.getNodes();
        for (ResponsiveLayoutFailure f : c.failures) {
            int[] bounds2 = f.getBounds();
            Rectangle r2 = new Rectangle(bounds2[0], 0, bounds2[1]-bounds2[0], 10);
            double boundsSim;
            if (r1.equals(r2)) {
                boundsSim = 1;
            } else if (r1.contains(r2) || r2.contains(r1)) {
                boundsSim = 0.7;
            } else if (r1.intersects(r2)) {
                boundsSim = 0.3;
            } else {
                boundsSim = 0;
            }
            System.out.println(boundsSim);
        }


        return similarity;
    }

}
