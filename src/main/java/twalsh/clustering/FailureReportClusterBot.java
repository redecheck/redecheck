package twalsh.clustering;

import info.debatty.java.stringsimilarity.*;
import twalsh.analysis.ResponsiveLayoutFailure;
import twalsh.rlg.Node;

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
        for (Cluster cluster : clusters) {
            System.out.println(cluster + "\n");
        }
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
        int[] bounds = e.getBounds();
        HashSet<Node> nodes = e.getNodes();
        for (Cluster c : clusters) {
            for (ResponsiveLayoutFailure f : c.failures) {
                int[] bounds2 = f.getBounds();
                if (Arrays.equals(bounds, bounds2)) {
                    return c;
//                    System.out.println(e);
//                    System.out.println(f);
//                    LongestCommonSubsequence lcs = new LongestCommonSubsequence();
//                    System.out.println(lcs.distance(n1.getXpath(), n2.getXpath()) + "\n");
//                    System.out.println();
                }
//                HashSet<Node> nodes2 = f.getNodes();
//                for (Node n1 : nodes) {
//                    for (Node n2 : nodes2) {
//                        LongestCommonSubsequence lcs = new LongestCommonSubsequence();
//                        System.out.println(n1.getXpath() + " - " + n2.getXpath());
//                        System.out.println(lcs.distance(n1.getXpath(), n2.getXpath()) + "\n");
//                    }
//                }
            }
        }

        return closest;
    }

}
