package twalsh.rlg;

import com.rits.cloning.Cloner;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import xpert.ag.AGNode;
import xpert.ag.AlignmentGraph;
import xpert.ag.Contains;
import xpert.ag.Edge;
import xpert.dom.DomNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by thomaswalsh on 17/09/15.
 */
public class ResponsiveLayoutGraphTest {
    ResponsiveLayoutGraph rlg;
    Cloner cloner;
    @Mock
    private AGNode node;
    Node n1,n2,n3;
    AlignmentConstraint acPC, acSib;

    @Before
    public void setup() {
        rlg = spy(new ResponsiveLayoutGraph());
        rlg.restOfGraphs = spy(new ArrayList<AlignmentGraph>());
        rlg.widths = new int[] {400,500,600,700};

        MockitoAnnotations.initMocks(this);
        cloner = new Cloner();

        n1 = new Node("first");
        n2 = new Node("second");
        n3 = new Node("third");
        acPC = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 300, 800, new boolean[] {true, false, false, false, true, false});
        acSib = new AlignmentConstraint(n1, n2, Type.SIBLING, 400,1000, new boolean[] {true, false, false, false, false, false, true, true});
    }

    @Test
    public void testGetNodes() {
        assertEquals(rlg.getNodes(), new HashMap<String, Node>());
        Node n = new Node("Test");
        rlg.getNodes().put(n.getXpath(), n);
        assertEquals(rlg.getNodes().size(), 1);
    }

    @Test
    public void testSetNodes() {
        Node n = new Node("Test");
        HashMap<String, Node> newNodes = new HashMap<>();
        newNodes.put(n.getXpath(), n);
        rlg.setNodes(newNodes);
        assertEquals(rlg.getNodes(), newNodes);
    }

    @Test
    public void testGetAlignments() {
        assertEquals(rlg.getAlignments(), new HashMap<String, AlignmentConstraint>());
        Node n1 = new Node("Test");
        Node n2 = new Node("Another");
        AlignmentConstraint ac  = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 300, 800, new boolean[] {true, false, false, false, true, false});
        rlg.getAlignments().put(ac.generateKey(), ac);
        assertEquals(rlg.getAlignments().size(), 1);
    }

    @Test
    public void testSetAlignments() {
        assertEquals(rlg.getAlignments(), new HashMap<String, AlignmentConstraint>());
        Node n1 = new Node("Test");
        Node n2 = new Node("Another");
        AlignmentConstraint ac  = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 300, 800, new boolean[] {true, false, false, false, true, false});
        HashMap<String,AlignmentConstraint> newAlCons = new HashMap<>();
        newAlCons.put(ac.generateKey(), ac);
        rlg.setAlignments(newAlCons);
        assertEquals(rlg.getAlignments(), newAlCons);
    }

    @Test
    public void testGetAlreadyGathered() {
        rlg.getAlreadyGathered().add(500);
        assertEquals(rlg.getAlreadyGathered().size(),1);
    }

    @Test
    public void testSetAlreadyGathered() {
        HashSet<Integer> newSet = new HashSet<>();
        newSet.add(300);
        newSet.add(500);
        rlg.setAlreadyGathered(newSet);
        assertEquals(rlg.getAlreadyGathered(), newSet);
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
        assertEquals(rlg.decideBreakpoint(min, max, b1, b2), max);
    }

    @Test
    public void decideBreakpointTestElse() {
        boolean b1 = false;
        boolean b2 = true;
        int min = 500;
        int max = 501;
        assertEquals(rlg.decideBreakpoint(min, max, b1, b2), min);
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
        assertEquals(Arrays.equals(rlg.getEquationOfLine(set1, set2), new double[]{1, 1, -400}), true);
    }

    @Test
    public void getEquationGradientNotOne() {
        double[] set1 = new double[] {500,520};
        double[] set2 = new double[] {240,250};
        assertEquals(Arrays.equals(rlg.getEquationOfLine(set1, set2), new double[] {1,0.5,-10}), true);
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
        int[] set1 = new int[] {460,480,500,520,540,560};
        int[] set2 = new int[] {440,460,480,500,520,540};
        double[] equation = new double[]{1, 1, -20};
        assertEquals(rlg.matchValuesToEquation(equation, set1, set2), 6);
    }

    @Test
    public void matchValuesBreaksOnFirst() {
        int[] set1 = new int[] {460,480,500,520,540,560};
        int[] set2 = new int[] {440,460,420,500,520,540};
        double[] equation = new double[]{1, 1, -20};
        assertEquals(rlg.matchValuesToEquation(equation, set1, set2), 2);
    }

    @Test
    public void matchValuesBreaksInMiddle() {
        int[] set1 = new int[] {460,480,500,520,540,560};
        int[] set2 = new int[] {440,460,480,500,560,540};
        double[] equation = new double[]{1, 1, -20};
        assertEquals(rlg.matchValuesToEquation(equation, set1, set2), 4);
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

    @Test
    public void testAttachVisConsToNodes() {
        Node n = new Node("test");
        rlg.getNodes().put(n.getXpath(), n);
        VisibilityConstraint vc = new VisibilityConstraint(400,800);
        HashMap<String, VisibilityConstraint> map = new HashMap<>();
        map.put(n.getXpath(), vc);
        rlg.attachVisConsToNodes(map);
        assertEquals(rlg.getNodes().get(n.getXpath()).getVisibilityConstraints().size(), 1);
    }

    @Test
    public void testUpdateRemainingNodes() throws Exception {
        DomNode dn = mock(DomNode.class);
        when(dn.getCoords()).thenReturn(new int[4]);
        when(dn.getxPath()).thenReturn("first");
        when(dn.getTagName()).thenReturn("BODY");
        AlignmentGraph ag = spy(new AlignmentGraph(dn));
        AGNode agn = new AGNode(dn);

        Node n = new Node(dn.getxPath());
        HashMap<String, VisibilityConstraint> map = new HashMap<>();
        VisibilityConstraint vc = new VisibilityConstraint(400, 0);
        map.put(n.getXpath(), vc);
        HashMap<String, AGNode> lastMap = new HashMap<>();
        lastMap.put("first", agn);

        when(ag.getVMap()).thenReturn(lastMap);
        rlg.updateRemainingNodes(map, ag);
        assertEquals(map.get(n.getXpath()).getDisappear(), 700);
    }

    @Test
    public void testUpdateAppearingNode() throws Exception {
        DomNode dn = mock(DomNode.class);
        when(dn.getCoords()).thenReturn(new int[4]);
        when(dn.getxPath()).thenReturn("newOne");
        when(dn.getTagName()).thenReturn("BODY");
        AlignmentGraph ag = spy(new AlignmentGraph(dn));
        AGNode agn = new AGNode(dn);
        HashMap<String, AGNode> tempToMatch = new HashMap<>();
        tempToMatch.put(agn.getDomNode().getxPath(), agn);

        // Set up VC map
        Node n1 = new Node("first");
        Node n2 = new Node("second");
        Node n3 = new Node("third");
        HashMap<String, VisibilityConstraint> map = new HashMap<>();
        VisibilityConstraint vc = new VisibilityConstraint(400, 0);
        map.put(n1.getXpath(), vc);
        map.put(n2.getXpath(), vc);
        map.put(n3.getXpath(), vc);
        doReturn(520).when(rlg).findAppearPoint(anyString(), anyInt(), anyInt(), anyBoolean(), anyString());
        doReturn(0).when(rlg.restOfGraphs).indexOf(any(AlignmentGraph.class));
        rlg.updateAppearingNode(tempToMatch, map, ag);
        assertEquals(map.entrySet().size(), 4);
        assertEquals(map.get(dn.getxPath()).getAppear(), 520);
        assertEquals(map.get(dn.getxPath()).getDisappear(), 0);
    }

    @Test
    public void testUpdateDisappearingNode() throws Exception {
        // Set up VC map
        Node n1 = new Node("first");
        Node n2 = new Node("second");
        Node n3 = new Node("third");
        HashMap<String, VisibilityConstraint> map = new HashMap<>();
        VisibilityConstraint vc = new VisibilityConstraint(400, 0);
        map.put(n1.getXpath(), vc);
        map.put(n2.getXpath(), vc);
        map.put(n3.getXpath(), vc);

        DomNode dn = mock(DomNode.class);
        when(dn.getCoords()).thenReturn(new int[4]);
        when(dn.getxPath()).thenReturn("first");
        when(dn.getTagName()).thenReturn("BODY");
        AlignmentGraph ag = spy(new AlignmentGraph(dn));
        AGNode agn = new AGNode(dn);
        HashMap<String, AGNode> prevToMatch = new HashMap<>();
        prevToMatch.put(agn.getDomNode().getxPath(), agn);

        doReturn(850).when(rlg).findDisappearPoint(anyString(), anyInt(), anyInt(), anyBoolean(), anyString());
        doReturn(0).when(rlg.restOfGraphs).indexOf(any(AlignmentGraph.class));
        rlg.updateDisappearingNode(prevToMatch, map, ag);

        // Check upper bound is 1 less than value returned by search
        assertEquals(map.get("first").getDisappear(), 849);
    }


    @Test
    public void setUpAlignmentCons() {
        HashMap<String, AlignmentConstraint> alCons = new HashMap<>();
//        HashMap<String, Node> nodes = new HashMap<>();
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("first");
        AGNode agn1 = new AGNode(dn1);
        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {25,25,75,75});
        when(dn2.getxPath()).thenReturn("second");
        AGNode agn2 = new AGNode(dn2);

        Node n1 = new Node(dn1.getxPath());
        Node n2 = new Node(dn2.getxPath());
        rlg.nodes.put(n1.getXpath(), n1);
        rlg.nodes.put(n2.getXpath(), n2);

        Contains c = new Contains(agn1, agn2);
//        when(c.getAttributes()).thenReturn(new boolean[] {true, false, false, false, false});
        System.out.print(c);
        HashMap<String, Edge> edgeMap = new HashMap<>();
        edgeMap.put(c.getNode1().getxPath()+c.getNode2().getxPath()+"contains"+c.generateLabelling(), c);

        rlg.setUpAlignmentConstraints(edgeMap, alCons);
        assertEquals(alCons.size(), 1);
        assertEquals(alCons.get(c.getNode1().getxPath()+c.getNode2().getxPath()+"contains"+c.generateLabelling()).generateKey(), c.getNode1().getxPath()+c.getNode2().getxPath()+"contains"+c.generateLabelling());

    }
}