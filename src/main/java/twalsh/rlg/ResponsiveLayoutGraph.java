package twalsh.rlg;
import java.util.HashMap;
/**
 * Created by thomaswalsh on 10/08/15.
 */
public class ResponsiveLayoutGraph {
    HashMap<String, Node> nodes;

    public  ResponsiveLayoutGraph() {

    }

    public void init() {
        extractVisibilityConstraints();
    }

    private void extractVisibilityConstraints() {
        HashMap<String, VisibilityConstraint> visCons = new HashMap<>();
    }
}
