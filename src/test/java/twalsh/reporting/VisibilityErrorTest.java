package twalsh.reporting;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import twalsh.rlg.Node;
import twalsh.rlg.VisibilityConstraint;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by thomaswalsh on 28/09/15.
 */
public class VisibilityErrorTest extends TestCase {

    VisibilityError ve;
    Node oracle, test;


    @Test
    public void testToString() throws Exception {
        oracle = new Node("oracle");
        test = new Node("test");
        VisibilityConstraint vc = new VisibilityConstraint(400, 1200);
        VisibilityConstraint vc2 = new VisibilityConstraint(400, 1100);
        oracle.addVisibilityConstraint(vc);
        test.addVisibilityConstraint(vc2);
        ve = new VisibilityError(oracle, test);
        String expected = "oracle\n Oracle: 400 -> 1200\n Test: 400 -> 1100";
        assertEquals(expected, ve.toString());
    }

    @Test
    public void testCalculateRangeOfViewportWidthsBranchOne() throws Exception {
        oracle = new Node("oracle");
        test = new Node("test");
        VisibilityConstraint vc = new VisibilityConstraint(400, 1200);
        VisibilityConstraint vc2 = new VisibilityConstraint(400, 1100);
        oracle.addVisibilityConstraint(vc);
        test.addVisibilityConstraint(vc2);
        ve = new VisibilityError(oracle, test);
        ArrayList<int[]> ranges = ve.calculateRangeOfViewportWidths();
        assertEquals(1, ranges.size());
        int[] range = ranges.get(0);
        int[] expected = new int[]{1101, 1200};
        assertEquals(true, Arrays.equals(range, expected));
    }

    @Test
    public void testCalculateRangeOfViewportWidthsBranchTwo() throws Exception {
        oracle = new Node("oracle");
        test = new Node("test");
        VisibilityConstraint vc = new VisibilityConstraint(400, 1200);
        VisibilityConstraint vc2 = new VisibilityConstraint(420, 1200);
        oracle.addVisibilityConstraint(vc);
        test.addVisibilityConstraint(vc2);
        ve = new VisibilityError(oracle, test);
        ArrayList<int[]> ranges = ve.calculateRangeOfViewportWidths();
        assertEquals(1, ranges.size());
        int[] range = ranges.get(0);
        int[] expected = new int[]{400, 419};
        assertEquals(true, Arrays.equals(range, expected));
    }

    @Test
    public void testCalculateRangeOfViewportWidthsBranchThree() throws Exception {
        oracle = new Node("oracle");
        test = new Node("test");
        VisibilityConstraint vc = new VisibilityConstraint(400, 1200);
        VisibilityConstraint vc2 = new VisibilityConstraint(420, 1190);
        oracle.addVisibilityConstraint(vc);
        test.addVisibilityConstraint(vc2);
        ve = new VisibilityError(oracle, test);
        ArrayList<int[]> ranges = ve.calculateRangeOfViewportWidths();
        assertEquals(2, ranges.size());
        int[] range1 = ranges.get(0);
        int[] expected1 = new int[]{400, 419};
        assertEquals(true, Arrays.equals(range1, expected1));
        int[] range2 = ranges.get(1);
        int[] expected2 = new int[]{1191, 1200};
        assertEquals(true, Arrays.equals(range2, expected2));
    }
}