package twalsh.rlg;

import com.rits.cloning.Cloner;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import xpert.ag.AGNode;
import xpert.dom.DomNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by thomaswalsh on 17/09/15.
 */
public class ResponsiveLayoutGraphTest {
    ResponsiveLayoutGraph rlg;
    Cloner cloner;
    @Mock
    private AGNode node;

    @Before
    public void setup() {
        rlg = new ResponsiveLayoutGraph();
        MockitoAnnotations.initMocks(this);
        cloner = new Cloner();
    }

    @Test
    public void decideBreakpointBothTrue() {
        boolean b1 = true;
        boolean b2 = true;
        int min = 500;
        int max = 501;
        assertEquals(rlg.decideBreakpoint(min, max, b1, b2), max + 1);
    }

    @Test
    public void decideBreakpointTrueFalse() {
        boolean b1 = true;
        boolean b2 = false;
        int min = 500;
        int max = 501;
        assertEquals(rlg.decideBreakpoint(min,max,b1,b2), max);
    }

    @Test
    public void decideBreakpointTestElse() {
        boolean b1 = false;
        boolean b2 = true;
        int min = 500;
        int max = 501;
        assertEquals(rlg.decideBreakpoint(min,max,b1,b2), min);
    }

    @Test
    public void getEquationAllSame() {
        double[] set1 = new double[] {500,520};
        double[] set2 = new double[] {100,100};
        assertEquals(Arrays.equals(rlg.getEquationOfLine(set1, set2), new double[]{1, 0, set2[0]}), true);
    }

    @Test
    public void getEquationGradientOne() {
        double[] set1 = new double[] {500,520};
        double[] set2 = new double[] {100,120};
        assertEquals(Arrays.equals(rlg.getEquationOfLine(set1,set2), new double[] {1,1,-400}), true);
    }

    @Test
    public void getEquationGradientNotOne() {
        double[] set1 = new double[] {500,520};
        double[] set2 = new double[] {240,250};
        assertEquals(Arrays.equals(rlg.getEquationOfLine(set1,set2), new double[] {1,0.5,-10}), true);
    }

    @Test
    public void bestFitAllSameUseAll() {
        int[] set1 = new int[] {500,520,540,560};
        int[] set2 = new int[] {100,100,100,100};
        assertEquals(Arrays.equals(rlg.getBestFitLine(set1, set2, 4), new double[]{1, 0, set2[0]}), true);
    }

    @Test
    public void bestFitAllSameWithSplit() {
        int[] set1 = new int[] {500,520,540,800};
        int[] set2 = new int[] {100,100,100,400};
        assertEquals(Arrays.equals(rlg.getBestFitLine(set1, set2, 3), new double[]{1, 0, set2[0]}), true);
    }

    @Test
    public void bestFitGradientOneUseAll() {
        int[] set1 = new int[] {500,520,540,560};
        int[] set2 = new int[] {480,500,520,540};
        assertTrue(Arrays.equals(rlg.getBestFitLine(set1, set2, 4), new double[]{1, 1, -20}));
    }

    @Test
    public void bestFitGradientOneWithSplit() {
        int[] set1 = new int[] {500,520,540,700};
        int[] set2 = new int[] {480,500,520,350};
        assertTrue(Arrays.equals(rlg.getBestFitLine(set1, set2, 3), new double[]{1, 1, -20}));
    }

    @Test
    public void matchValuesAllMatch() {

    }

    @Test
    public void setUpVisCons() {
        DomNode dn = mock(DomNode.class);
        when(dn.getCoords()).thenReturn(new int[4]);
        AGNode agn = new AGNode(dn);
        ArrayList<AGNode> nodes = new ArrayList<>();
        nodes.add(agn);
        HashMap<String, VisibilityConstraint> visCons = new HashMap<String, VisibilityConstraint>();

        rlg.setUpVisibilityConstraints(nodes, visCons);
        assertEquals(visCons.entrySet().size(), 1);
    }

    @Test
    public void nodeMatchWithMatch() {
        DomNode dn = mock(DomNode.class);
        when(dn.getCoords()).thenReturn(new int[4]);
        AGNode agn = new AGNode(dn);

        HashMap<String, AGNode> prev = new HashMap<String, AGNode>();
        HashMap<String, AGNode> temp = new HashMap<String, AGNode>();
        prev.put(dn.getxPath(), agn);
        temp.put(dn.getxPath(), agn);

        HashMap<String, AGNode> prevToMatch = cloner.deepClone(prev);
        HashMap<String, AGNode> tempToMatch = cloner.deepClone(temp);

        rlg.checkForNodeMatch(prev, temp, prevToMatch, tempToMatch);
        assertEquals(prevToMatch.entrySet().size(), 0);
    }

    @Test
    public void nodeMatchWithNoMatch() {
        DomNode dn = mock(DomNode.class);
        when(dn.getCoords()).thenReturn(new int[4]);
        when(dn.getxPath()).thenReturn("first");
        AGNode agn = new AGNode(dn);

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[4]);
        when(dn2.getxPath()).thenReturn("second");
        AGNode agn2 = new AGNode(dn2);

        HashMap<String, AGNode> prev = new HashMap<String, AGNode>();
        HashMap<String, AGNode> temp = new HashMap<String, AGNode>();
        prev.put(dn.getxPath(), agn);
        temp.put(dn2.getxPath(), agn2);

        HashMap<String, AGNode> prevToMatch = cloner.deepClone(prev);
        HashMap<String, AGNode> tempToMatch = cloner.deepClone(temp);

        rlg.checkForNodeMatch(prev, temp, prevToMatch, tempToMatch);
        assertEquals(prevToMatch.entrySet().size(), 1);
    }
}