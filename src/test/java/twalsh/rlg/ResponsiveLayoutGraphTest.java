package twalsh.rlg;

import com.rits.cloning.Cloner;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import xpert.ag.*;
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
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("first");
        AGNode agn1 = new AGNode(dn1);
        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(dn2.getxPath()).thenReturn("second");
        AGNode agn2 = new AGNode(dn2);

        Node n1 = new Node(dn1.getxPath());
        Node n2 = new Node(dn2.getxPath());
        rlg.nodes.put(n1.getXpath(), n1);
        rlg.nodes.put(n2.getXpath(), n2);

        Contains c = new Contains(agn1, agn2);
        HashMap<String, Edge> edgeMap = new HashMap<>();
        edgeMap.put(c.getNode1().getxPath() + c.getNode2().getxPath() + "contains" + c.generateLabelling(), c);

        Sibling sibling = new Sibling(agn2, agn1);
        edgeMap.put(sibling.getNode1().getxPath() + sibling.getNode2().getxPath() + "sibling" + sibling.generateLabelling(), sibling);

        rlg.setUpAlignmentConstraints(edgeMap, alCons);
        assertEquals(alCons.size(), 2);
        assertEquals(alCons.get(c.getNode1().getxPath() + c.getNode2().getxPath() + "contains" + c.generateLabelling()).generateKey(), c.getNode1().getxPath() + c.getNode2().getxPath() + "contains" + c.generateLabelling());
        assertEquals(alCons.get(sibling.getNode1().getxPath() + sibling.getNode2().getxPath() + "sibling" + sibling.generateLabelling()).generateKey(), sibling.getNode1().getxPath() + sibling.getNode2().getxPath() + "sibling" + sibling.generateLabelling());
    }

    @Test
    public void testCheckForEdgeMatchTrue() {
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("first");
        AGNode agn1 = new AGNode(dn1);
        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(dn2.getxPath()).thenReturn("second");
        AGNode agn2 = new AGNode(dn2);

        Contains c = new Contains(agn1, agn2);
        Sibling sibling = new Sibling(agn2, agn1);

        HashMap<String, Edge> previousMap = new HashMap<>();
        previousMap.put(c.getNode1().getxPath() + c.getNode2().getxPath() + "contains" + c.generateLabelling(), c);
        previousMap.put(sibling.getNode1().getxPath() + sibling.getNode2().getxPath() + "sibling" + sibling.generateLabelling(), sibling);

        HashMap<String, Edge> temp = new HashMap<>();
        temp.put(c.getNode1().getxPath() + c.getNode2().getxPath() + "contains" + c.generateLabelling(), c);
        temp.put(sibling.getNode1().getxPath() + sibling.getNode2().getxPath() + "sibling" + sibling.generateLabelling(), sibling);


        HashMap<String, Edge> previousToMatch = cloner.deepClone(previousMap);
        HashMap<String, Edge> tempToMatch = cloner.deepClone(temp);

        rlg.checkForEdgeMatch(previousMap, previousToMatch, temp, tempToMatch);

        // Check matched edges have been removed from maps
        assertEquals(previousToMatch.size(), 0);
        assertEquals(tempToMatch.size(), 0);
    }

    @Test
    public void testCheckForEdgeMatchFlippedSibling() {
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("first");
        AGNode agn1 = new AGNode(dn1);
        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(dn2.getxPath()).thenReturn("second");
        AGNode agn2 = new AGNode(dn2);

        Sibling sibling = new Sibling(agn2, agn1);
        Sibling flipped = new Sibling(agn1, agn2);

        HashMap<String, Edge> previousMap = new HashMap<>();
        previousMap.put(sibling.getNode1().getxPath() + sibling.getNode2().getxPath() + "sibling" + sibling.generateLabelling(), sibling);

        HashMap<String, Edge> temp = new HashMap<>();
        temp.put(flipped.getNode1().getxPath() + flipped.getNode2().getxPath() + "sibling" + flipped.generateLabelling(), flipped);


        HashMap<String, Edge> previousToMatch = cloner.deepClone(previousMap);
        HashMap<String, Edge> tempToMatch = cloner.deepClone(temp);

        rlg.checkForEdgeMatch(previousMap, previousToMatch, temp, tempToMatch);

        // Check matched edges have been removed from maps
        assertEquals(previousToMatch.size(), 0);
        assertEquals(tempToMatch.size(), 0);
    }

    @Test
    public void testCheckForEdgeMatchFalse() {
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("first");
        AGNode agn1 = new AGNode(dn1);
        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(dn2.getxPath()).thenReturn("second");
        AGNode agn2 = new AGNode(dn2);

        Contains c = new Contains(agn1, agn2);
        Sibling sibling = new Sibling(agn2, agn1);

        HashMap<String, Edge> previousMap = new HashMap<>();
        previousMap.put(c.getNode1().getxPath() + c.getNode2().getxPath() + "contains" + c.generateLabelling(), c);

        HashMap<String, Edge> temp = new HashMap<>();
        temp.put(sibling.getNode1().getxPath() + sibling.getNode2().getxPath() + "sibling" + sibling.generateLabelling(), sibling);


        HashMap<String, Edge> previousToMatch = cloner.deepClone(previousMap);
        HashMap<String, Edge> tempToMatch = cloner.deepClone(temp);

        rlg.checkForEdgeMatch(previousMap, previousToMatch, temp, tempToMatch);

        // Check matched edges have been removed from maps
        assertEquals(previousToMatch.size(), 1);
        assertEquals(tempToMatch.size(), 1);
    }

    @Test
    public void testUpdateAppearingEdge() {


//        rlg.updateAppearingEdges(tempToMatch, alConsTable, alCons, ag);
    }

    @Test
    public void testAddParentConstraintsToNodes() {
        Node n = new Node("test");
        Node p = new Node("parent");
        rlg.nodes.put(n.getXpath(), n);
        rlg.nodes.put(p.getXpath(), p);
        AlignmentConstraint ac = new AlignmentConstraint(p, n, Type.PARENT_CHILD, 400, 800, new boolean[] {true, false, false, false, false, false});
        rlg.alignmentConstraints.put(ac.generateKey(), new int[]{400,0}, ac);
        rlg.addParentConstraintsToNodes();
        assertEquals(n.getParentConstraints().size(), 1);
        assertEquals(n.getParentConstraints().get(0), ac);
    }

    @Test
    public void testAddWidthConstraintsToNodes() {
        Node n = new Node("test");
        Node p = new Node("parent");
        rlg.nodes.put(n.getXpath(), n);
        rlg.nodes.put(p.getXpath(), p);

        WidthConstraint wc = new WidthConstraint(400,1000, 1, p, -10);
        rlg.widthConstraints.put(n.getXpath(), new int[] {400,0}, wc);
        rlg.addWidthConstraintsToNodes();
        assertEquals(n.getWidthConstraints().size(), 1);
        assertEquals(n.getWidthConstraints().get(0), wc);

    }

    @Test
    public void testGenerateEdgeMap() {
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("first");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = new AGNode(dn1);
        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(dn2.getxPath()).thenReturn("second");
        AGNode agn2 = new AGNode(dn2);

        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        Contains c = new Contains(agn1, agn2);
        Sibling s = new Sibling(agn2, agn1);
        ArrayList<Contains> cs = new ArrayList<Contains>();
        ArrayList<Sibling> ss = new ArrayList<Sibling>();
        cs.add(c);
        ss.add(s);

        when (ag.getContains()).thenReturn(cs);
        when (ag.getSiblings()).thenReturn(ss);

        HashMap<String, Edge> map = rlg.generateEdgeMapFromAG(ag);
        assertEquals(map.size(), 2);
        assertEquals(map.get(c.getNode1().getxPath() + c.getNode2().getxPath() + "contains" + c.generateLabelling()), c);
    }

    @Test
    public void testGetWidthsForConstraintsOneParent() {
        Node n = new Node("test");
        Node p = new Node("parent");
        AlignmentConstraint ac = new AlignmentConstraint(n, p, Type.PARENT_CHILD, 400, 1000, new boolean[] {false, false, false, false, false});
        ArrayList<AlignmentConstraint> acs = new ArrayList<>();
        acs.add(ac);
        ArrayList<int[]> ranges = rlg.getWidthsForConstraints(acs);
        assertEquals(ranges.size(), 1);
        int[] array = ranges.get(0);
        assertEquals(true, Arrays.equals(array, new int[] {400, 500, 600, 700}));
    }

    @Test
    public void testGetWidthsForConstraintsTwoParents() {
        Node n = new Node("test");
        Node p = new Node("parent");
        Node p2 = new Node("another");
        AlignmentConstraint ac = new AlignmentConstraint(p, n, Type.PARENT_CHILD, 400, 550, new boolean[] {false, false, false, false, false});
        AlignmentConstraint ac2 = new AlignmentConstraint(p2, n, Type.PARENT_CHILD, 551, 620, new boolean[] {false, false, false, false, false, false});
        ArrayList<AlignmentConstraint> acs = new ArrayList<>();
        acs.add(ac);
        acs.add(ac2);
        ArrayList<int[]> ranges = rlg.getWidthsForConstraints(acs);
        assertEquals(ranges.size(), 2);
        int[] array = ranges.get(0);
        assertEquals(true, Arrays.equals(array, new int[] {400, 500}));
        int[] array2 = ranges.get(1);
        assertEquals(true, Arrays.equals(array2, new int[] {600}));
    }

    @Test
    public void testPopulateWidthArrays() {
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("first");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = new AGNode(dn1);

        int[] validWidths = new int[4];
        int[] widthsTemp = new int[4];
        int[] parentWidths = new int[4];
        int[] childWidths = new int[4];
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));

        when(new AlignmentGraph(rlg.doms.get(anyInt()))).thenReturn(ag);

        rlg.populateWidthArrays(validWidths, widthsTemp, parentWidths, childWidths, "node", "parent");
    }
}