package twalsh.rlg;

import com.google.common.collect.HashBasedTable;
import com.rits.cloning.Cloner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import twalsh.redecheck.Redecheck;
import xpert.ag.*;
import xpert.dom.DomNode;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;
//import static org.mockito.Mockito.*;

/**
 * Created by thomaswalsh on 17/09/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( Redecheck.class )
public class ResponsiveLayoutGraphTest {
    ResponsiveLayoutGraph rlg;
    Cloner cloner;
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
        assertEquals(Arrays.equals(rlg.getEquationOfLine(set1, set2), new double[]{1, 0.5, -10}), true);
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
    public void testUpdateRemainingEdges() {
        // Set up final alignment graph
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("one");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = new AGNode(dn1);
        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(dn2.getxPath()).thenReturn("two");
        AGNode agn2 = new AGNode(dn2);
        DomNode dn3 = mock(DomNode.class);
        when(dn3.getCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(dn3.getxPath()).thenReturn("three");
        AGNode agn3 = new AGNode(dn3);
        Contains c = new Contains(agn1, agn2);
        Contains c2 = new Contains(agn1, agn3);
        Sibling sibling = new Sibling(agn2, agn3);
        HashMap<String, Edge> previousMap = new HashMap<>();
        previousMap.put(c.getNode1().getxPath() + c.getNode2().getxPath() + "contains" + c.generateLabelling(), c);
        previousMap.put(c2.getNode1().getxPath() + c2.getNode2().getxPath() + "contains" + c2.generateLabelling(), c2);
        previousMap.put(sibling.getNode1().getxPath() + sibling.getNode2().getxPath() + "sibling" + sibling.generateLabelling(), sibling);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));

        Node n1 = new Node("one");
        Node n2 = new Node("two");
        Node n3 = new Node("three");
        AlignmentConstraint ac1 = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 400, 0, new boolean[]{true, false, false, true, false, false});
        AlignmentConstraint ac2 = new AlignmentConstraint(n1, n3, Type.PARENT_CHILD, 400, 0, new boolean[]{true, false, false, true, false, false});
        AlignmentConstraint ac3 = new AlignmentConstraint(n2, n3, Type.SIBLING, 400, 0, new boolean[]{false, false, false, false, true, true, true, true});

        HashMap<String, AlignmentConstraint> cons = new HashMap<>();
        cons.put(ac1.generateKey(), ac1);
        cons.put(ac2.generateKey(), ac2);
        cons.put(ac3.generateKey(), ac3);

//        HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints = HashBasedTable.create();
        rlg.alignmentConstraints.put(ac1.generateKey(), new int[]{rlg.widths[0],0}, ac1);
        rlg.alignmentConstraints.put(ac2.generateKey(), new int[]{rlg.widths[0],0}, ac2);
        rlg.alignmentConstraints.put(ac3.generateKey(), new int[]{rlg.widths[0],0}, ac3);

        doReturn(previousMap).when(rlg).generateEdgeMapFromAG(ag);

        rlg.updateRemainingEdges(cons, ag);
        assertEquals(700, cons.get(ac1.generateKey()).getMax());
        assertEquals(700, cons.get(ac2.generateKey()).getMax());
        assertEquals(700, cons.get(ac3.generateKey()).getMax());
    }

    @Test
    public void testUpdateAppearingEdges() throws InterruptedException {
        // Set up final alignment graph
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("one");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = new AGNode(dn1);
        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(dn2.getxPath()).thenReturn("two");
        AGNode agn2 = new AGNode(dn2);
        DomNode dn3 = mock(DomNode.class);
        when(dn3.getCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(dn3.getxPath()).thenReturn("three");
        AGNode agn3 = new AGNode(dn3);
        Contains c = new Contains(agn1, agn2);
        Contains c2 = new Contains(agn1, agn3);
        Sibling sibling = new Sibling(agn2, agn3);
        HashMap<String, Edge> previousMap = new HashMap<>();
        previousMap.put(c2.getNode1().getxPath() + c2.getNode2().getxPath() + "contains" + c2.generateLabelling(), c2);
        previousMap.put(sibling.getNode1().getxPath() + sibling.getNode2().getxPath() + "sibling" + sibling.generateLabelling(), sibling);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));

        // Set up RLG Nodes
        Node n1 = new Node("one");
        Node n2 = new Node("two");
        Node n3 = new Node("three");
        rlg.nodes.put(n1.getXpath(), n1);
        rlg.nodes.put(n2.getXpath(), n2);
        rlg.nodes.put(n3.getXpath(), n3);
        AlignmentConstraint ac1 = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 400, 0, new boolean[]{true, false, false, true, false, false});
        AlignmentConstraint ac2 = new AlignmentConstraint(n1, n3, Type.PARENT_CHILD, 400, 0, new boolean[]{true, false, false, true, false, false});
        AlignmentConstraint ac3 = new AlignmentConstraint(n2, n3, Type.SIBLING, 400, 0, new boolean[]{false, false, false, false, true, true, true, true});

        HashMap<String, AlignmentConstraint> cons = new HashMap<>();
        cons.put(ac1.generateKey(), ac1);
//        cons.put(ac2.generateKey(), ac2);

        rlg.alignmentConstraints.put(ac1.generateKey(), new int[]{rlg.widths[0],0}, ac1);
//        rlg.alignmentConstraints.put(ac2.generateKey(), new int[]{rlg.widths[0],0}, ac2);

        doReturn(520).when(rlg).findAppearPoint(anyString(), anyInt(), anyInt(), anyBoolean(), anyString());
        doReturn(0).when(rlg.restOfGraphs).indexOf(any(AlignmentGraph.class));

        rlg.updateAppearingEdges(previousMap, rlg.alignmentConstraints, cons, ag);
        assertEquals(rlg.alignmentConstraints.size(), 3);
        assertEquals(520, cons.get(ac3.generateKey()).getMin());
    }

    @Test
    public void testDisappearingNodes() throws InterruptedException {
        // Set up final alignment graph
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("one");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = new AGNode(dn1);
        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(dn2.getxPath()).thenReturn("two");
        AGNode agn2 = new AGNode(dn2);
        DomNode dn3 = mock(DomNode.class);
        when(dn3.getCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(dn3.getxPath()).thenReturn("three");
        AGNode agn3 = new AGNode(dn3);
        Contains c = new Contains(agn1, agn2);
        Contains c2 = new Contains(agn1, agn3);
        Sibling sibling = new Sibling(agn2, agn3);
        HashMap<String, Edge> previousMap = new HashMap<>();
        previousMap.put(c2.getNode1().getxPath() + c2.getNode2().getxPath() + "contains" + c2.generateLabelling(), c2);
        previousMap.put(sibling.getNode1().getxPath() + sibling.getNode2().getxPath() + "sibling" + sibling.generateLabelling(), sibling);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));

        Node n1 = new Node("one");
        Node n2 = new Node("two");
        Node n3 = new Node("three");
        AlignmentConstraint ac1 = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 400, 0, new boolean[]{true, false, false, true, false, false});
        AlignmentConstraint ac2 = new AlignmentConstraint(n1, n3, Type.PARENT_CHILD, 400, 0, new boolean[]{true, false, false, true, false, false});
        AlignmentConstraint ac3 = new AlignmentConstraint(n2, n3, Type.SIBLING, 400, 0, new boolean[]{false, false, false, false, true, true, true, true});

        HashMap<String, AlignmentConstraint> cons = new HashMap<>();
        cons.put(ac1.generateKey(), ac1);
        cons.put(ac2.generateKey(), ac2);
        cons.put(ac3.generateKey(), ac3);

        rlg.alignmentConstraints.put(ac1.generateKey(), new int[]{rlg.widths[0],0}, ac1);
        rlg.alignmentConstraints.put(ac2.generateKey(), new int[]{rlg.widths[0],0}, ac2);
        rlg.alignmentConstraints.put(ac3.generateKey(), new int[]{rlg.widths[0], 0}, ac3);

        doReturn(previousMap).when(rlg).generateEdgeMapFromAG(ag);
        doReturn(645).when(rlg).findDisappearPoint(anyString(), anyInt(), anyInt(), anyBoolean(), anyString());
        doReturn(0).when(rlg.restOfGraphs).indexOf(any(AlignmentGraph.class));

        rlg.updateDisappearingEdge(previousMap, rlg.alignmentConstraints, ag);
        assertEquals(0, cons.get(ac1.generateKey()).getMax());
        assertEquals(644, cons.get(ac2.generateKey()).getMax());
        assertEquals(644, cons.get(ac3.generateKey()).getMax());


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
    public void testPopulateWidthArrays() throws Exception {
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("parent");
        when(dn1.getTagName()).thenReturn("BODY");

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {25,10,75,90});
        when(dn2.getxPath()).thenReturn("node");
        AGNode agn1 = spy(new AGNode(dn1));
        AGNode agn2 = spy(new AGNode(dn2));
        HashMap<String, AGNode> vmap = spy(new HashMap<>());
        vmap.put(agn1.getDomNode().getxPath(), agn1);

        int[] validWidths = rlg.widths;
        int[] widthsTemp = new int[4];
        int[] parentWidths = new int[4];
        int[] childWidths = new int[4];
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        rlg.doms = spy(new HashMap<Integer, DomNode>());

        when(rlg.doms.get(anyInt())).thenReturn(dn1);

        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(vmap).when(ag).getVMap();
        when(vmap.get("parent")).thenReturn(agn1);
        when(vmap.get("node")).thenReturn(agn2);
        when(agn1.getDomNode()).thenReturn(dn1);
        when(agn2.getDomNode()).thenReturn(dn2);
        when(dn1.getWidth()).thenReturn(100);
        when(dn2.getWidth()).thenReturn(50);

        rlg.populateWidthArrays(validWidths, widthsTemp, parentWidths, childWidths, "node", "parent");

        assertEquals(400, widthsTemp[0]);
        assertEquals(100, parentWidths[0]);
        assertEquals(50, childWidths[0]);
    }

    @Test
    public void testFindAppearPointRecursiveBranch1NodeMatch() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("parent");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = spy(new AGNode(dn1));
        HashMap<String, AGNode> vmap = spy(new HashMap<>());
        vmap.put(agn1.getDomNode().getxPath(), agn1);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));

        doReturn(dn1).when(mockDoms).get(anyInt());
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(vmap).when(ag).getVMap();
        doReturn(dn1).when(vmap).get("node");

        rlg.findAppearPoint("node", 400, 500, true, "dontneed");
        verify(rlg, times(1)).findAppearPoint("node", 400, 450, true, "dontneed");
    }

    @Test
    public void testFindAppearPointRecursiveBranch1NodeNoMatch() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("parent");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = spy(new AGNode(dn1));
        HashMap<String, AGNode> vmap = spy(new HashMap<>());
        vmap.put(agn1.getDomNode().getxPath(), agn1);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));

        doReturn(dn1).when(mockDoms).get(anyInt());
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(vmap).when(ag).getVMap();
        doReturn(null).when(vmap).get("node");

        rlg.findAppearPoint("node", 400, 500, true, "dontneed");
        verify(rlg, times(1)).findAppearPoint("node", 450, 500, true, "dontneed");
    }

    @Test
    public void testFindAppearPointRecursiveBranch2ReturnMin() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = spy(new AGNode(dn1));

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn2.getxPath()).thenReturn("parent");
        when(dn2.getTagName()).thenReturn("BODY");
        AGNode agn2 = spy(new AGNode(dn1));

        HashMap<String, AGNode> vmap = spy(new HashMap<>());
        HashMap<String, AGNode> vmap2 = spy(new HashMap<>());
        vmap.put(agn1.getDomNode().getxPath(), agn1);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        AlignmentGraph ag2 = spy(new AlignmentGraph(dn2));

        doReturn(dn1).when(mockDoms).get(420);
        doReturn(dn2).when(mockDoms).get(421);
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(ag2).when(rlg).getAlignmentGraph(dn2);
        doReturn(vmap).when(ag).getVMap();
        doReturn(vmap2).when(ag2).getVMap();

        int expected = rlg.findAppearPoint("node", 420, 421, true, "dontneed");
        assertEquals(420, expected);
    }

    @Test
    public void testFindAppearPointRecursiveBranch2ReturnMax() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = spy(new AGNode(dn1));

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn2.getxPath()).thenReturn("parent");
        when(dn2.getTagName()).thenReturn("BODY");
        AGNode agn2 = spy(new AGNode(dn1));

        HashMap<String, AGNode> vmap = new HashMap<>();
        HashMap<String, AGNode> vmap2 = new HashMap<>();
//        vmap.put(agn2.getDomNode().getxPath(), agn2);
        vmap2.put(agn1.getDomNode().getxPath(), agn1);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        AlignmentGraph ag2 = spy(new AlignmentGraph(dn2));

        doReturn(dn1).when(mockDoms).get(420);
        doReturn(dn2).when(mockDoms).get(421);
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(ag2).when(rlg).getAlignmentGraph(dn2);
        doReturn(vmap).when(ag).getVMap();
        doReturn(vmap2).when(ag2).getVMap();
//        doReturn(null).when(vmap).get("node");

        int expected = rlg.findAppearPoint("node", 420, 421, true, "dontneed");
        assertEquals(421, expected);
    }

    @Test
    public void testFindAppearPointRecursiveBranch2ReturnMaxPlusOne() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = spy(new AGNode(dn1));

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn2.getxPath()).thenReturn("parent");
        when(dn2.getTagName()).thenReturn("BODY");
        AGNode agn2 = spy(new AGNode(dn1));

        HashMap<String, AGNode> vmap = spy(new HashMap<>());
        HashMap<String, AGNode> vmap2 = spy(new HashMap<>());
//        vmap2.put(agn1.getDomNode().getxPath(), agn1);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        AlignmentGraph ag2 = spy(new AlignmentGraph(dn2));

        doReturn(dn1).when(mockDoms).get(420);
        doReturn(dn2).when(mockDoms).get(421);
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(ag2).when(rlg).getAlignmentGraph(dn2);
        doReturn(vmap).when(ag).getVMap();
        doReturn(vmap2).when(ag2).getVMap();

        int expected = rlg.findAppearPoint("nomatch", 420, 421, true, "dontneed");
        assertEquals(422, expected);
    }

    @Test
    public void testFindDisappearPointRecursiveBranch1NodeMatch() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = spy(new AGNode(dn1));
        HashMap<String, AGNode> vmap = spy(new HashMap<>());
        vmap.put(agn1.getDomNode().getxPath(), agn1);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));

        doReturn(dn1).when(mockDoms).get(anyInt());
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(vmap).when(ag).getVMap();
//        doReturn(dn1).when(vmap).get("node");

        rlg.findDisappearPoint("node", 400, 500, true, "dontneed");
        verify(rlg, times(1)).findDisappearPoint("node", 450, 500, true, "dontneed");
    }

    @Test
    public void testFindDisappearPointRecursiveBranch1NodeNoMatch() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("parent");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = spy(new AGNode(dn1));
        HashMap<String, AGNode> vmap = spy(new HashMap<>());
        vmap.put(agn1.getDomNode().getxPath(), agn1);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));

        doReturn(dn1).when(mockDoms).get(anyInt());
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(vmap).when(ag).getVMap();
//        doReturn(null).when(vmap).get("node");

        rlg.findDisappearPoint("node", 400, 500, true, "dontneed");
        verify(rlg, times(1)).findDisappearPoint("node", 400, 450, true, "dontneed");
    }

    @Test
    public void testFindDisappearPointRecursiveBranch2ReturnMin() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = spy(new AGNode(dn1));

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn2.getxPath()).thenReturn("parent");
        when(dn2.getTagName()).thenReturn("BODY");
        AGNode agn2 = spy(new AGNode(dn1));

        HashMap<String, AGNode> vmap = spy(new HashMap<>());
        HashMap<String, AGNode> vmap2 = spy(new HashMap<>());
//        vmap.put(agn1.getDomNode().getxPath(), agn1);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        AlignmentGraph ag2 = spy(new AlignmentGraph(dn2));

        doReturn(dn1).when(mockDoms).get(420);
        doReturn(dn2).when(mockDoms).get(421);
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(ag2).when(rlg).getAlignmentGraph(dn2);
        doReturn(vmap).when(ag).getVMap();
        doReturn(vmap2).when(ag2).getVMap();

        int expected = rlg.findDisappearPoint("node", 420, 421, true, "dontneed");
        assertEquals(420, expected);
    }

    @Test
    public void testFindDisappearPointRecursiveBranch2ReturnMax() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = spy(new AGNode(dn1));

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn2.getxPath()).thenReturn("parent");
        when(dn2.getTagName()).thenReturn("BODY");
        AGNode agn2 = spy(new AGNode(dn1));

        HashMap<String, AGNode> vmap = spy(new HashMap<>());
        HashMap<String, AGNode> vmap2 = spy(new HashMap<>());
        vmap.put(agn1.getDomNode().getxPath(), agn1);
//        vmap2.put(agn1.getDomNode().getxPath(), agn1);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        AlignmentGraph ag2 = spy(new AlignmentGraph(dn2));

        doReturn(dn1).when(mockDoms).get(420);
        doReturn(dn2).when(mockDoms).get(421);
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(ag2).when(rlg).getAlignmentGraph(dn2);
        doReturn(vmap).when(ag).getVMap();
        doReturn(vmap2).when(ag2).getVMap();
//        doReturn(null).when(vmap).get("node");

        int expected = rlg.findDisappearPoint("node", 420, 421, true, "dontneed");
        assertEquals(421, expected);
    }

    @Test
    public void testFindDisappearPointRecursiveBranch2ReturnMaxPlusOne() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        when(dn1.getTagName()).thenReturn("BODY");
        AGNode agn1 = spy(new AGNode(dn1));

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn2.getxPath()).thenReturn("parent");
        when(dn2.getTagName()).thenReturn("BODY");
        AGNode agn2 = spy(new AGNode(dn1));

        HashMap<String, AGNode> vmap = new HashMap<>();
        HashMap<String, AGNode> vmap2 = new HashMap<>();
        vmap.put(agn1.getDomNode().getxPath(), agn1);
        vmap2.put(agn1.getDomNode().getxPath(), agn1);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        AlignmentGraph ag2 = spy(new AlignmentGraph(dn2));

        doReturn(dn1).when(mockDoms).get(420);
        doReturn(dn2).when(mockDoms).get(421);
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(ag2).when(rlg).getAlignmentGraph(dn2);
        doReturn(vmap).when(ag).getVMap();
        doReturn(vmap2).when(ag2).getVMap();

        int expected = rlg.findDisappearPoint("node", 420, 421, true, "dontneed");
        assertEquals(422, expected);
    }

    @Test
    public void testWidthBreakpointRecursiveTrue() {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        doReturn("BODY").when(dn1).getTagName();
        doReturn(80).when(dn1).getWidth();
        AGNode agn1 = spy(new AGNode(dn1));

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn2.getxPath()).thenReturn("parent");
        doReturn("BODY").when(dn2).getTagName();
        doReturn(100).when(dn2).getWidth();
        AGNode agn2 = spy(new AGNode(dn2));

        double[] eq = new double[]{1.0,1.0,-20};

        HashMap<String, AGNode> vmap = new HashMap<>();
        vmap.put(agn1.getDomNode().getxPath(), agn1);
        vmap.put(agn2.getDomNode().getxPath(), agn2);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        doReturn(dn1).when(mockDoms).get(anyInt());
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(vmap).when(ag).getVMap();

        try {
            rlg.findWidthBreakpoint(eq,700, 800, "node", "parent");
            verify(rlg, times(1)).findWidthBreakpoint(eq, 750, 800, "node", "parent");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testWidthBreakpointRecursiveFalse() {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        doReturn("BODY").when(dn1).getTagName();
        doReturn(50).when(dn1).getWidth();
        AGNode agn1 = spy(new AGNode(dn1));

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn2.getxPath()).thenReturn("parent");
        doReturn("BODY").when(dn2).getTagName();
        doReturn(100).when(dn2).getWidth();
        AGNode agn2 = spy(new AGNode(dn2));

        double[] eq = new double[]{1.0,1.0,-20};

        HashMap<String, AGNode> vmap = new HashMap<>();
        vmap.put(agn1.getDomNode().getxPath(), agn1);
        vmap.put(agn2.getDomNode().getxPath(), agn2);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        doReturn(dn1).when(mockDoms).get(anyInt());
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(vmap).when(ag).getVMap();

        try {
            rlg.findWidthBreakpoint(eq,700, 800, "node", "parent");
            verify(rlg, times(1)).findWidthBreakpoint(eq, 700, 750, "node", "parent");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFindWidthBreakpointRecursiveBranch2ReturnMin() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        when(dn1.getTagName()).thenReturn("BODY");
        doReturn(80).when(dn1).getWidth();
        AGNode agn1 = spy(new AGNode(dn1));

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn2.getxPath()).thenReturn("parent");
        when(dn2.getTagName()).thenReturn("BODY");
        doReturn(100).when(dn2).getWidth();
        AGNode agn2 = spy(new AGNode(dn2));

        DomNode dn3 = mock(DomNode.class);
        when(dn3.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn3.getxPath()).thenReturn("node");
        when(dn3.getTagName()).thenReturn("BODY");
        doReturn(50).when(dn3).getWidth();
        AGNode agn3 = spy(new AGNode(dn3));

        DomNode dn4 = mock(DomNode.class);
        when(dn4.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn4.getxPath()).thenReturn("parent");
        when(dn4.getTagName()).thenReturn("BODY");
        doReturn(100).when(dn4).getWidth();
        AGNode agn4 = spy(new AGNode(dn4));

        double[] eq = new double[]{1.0,1.0,-20};

        HashMap<String, AGNode> vmap = new HashMap<>();
        HashMap<String, AGNode> vmap2 = new HashMap<>();
        vmap.put(agn1.getDomNode().getxPath(), agn1);
        vmap.put(agn2.getDomNode().getxPath(), agn2);
        vmap2.put(agn3.getDomNode().getxPath(), agn3);
        vmap2.put(agn4.getDomNode().getxPath(), agn4);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        AlignmentGraph ag2 = spy(new AlignmentGraph(dn3));

        doReturn(dn1).when(mockDoms).get(767);
        doReturn(dn3).when(mockDoms).get(768);
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(ag2).when(rlg).getAlignmentGraph(dn3);
        doReturn(vmap).when(ag).getVMap();
        doReturn(vmap2).when(ag2).getVMap();

        int expected = rlg.findWidthBreakpoint(eq, 767, 768, "node", "parent");
        assertEquals(767, expected);
    }

    @Test
    public void testFindWidthBreakpointRecursiveBranch2ReturnMax() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        when(dn1.getTagName()).thenReturn("BODY");
        doReturn(80).when(dn1).getWidth();
        AGNode agn1 = spy(new AGNode(dn1));

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn2.getxPath()).thenReturn("parent");
        when(dn2.getTagName()).thenReturn("BODY");
        doReturn(100).when(dn2).getWidth();
        AGNode agn2 = spy(new AGNode(dn2));

        DomNode dn3 = mock(DomNode.class);
        when(dn3.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn3.getxPath()).thenReturn("node");
        when(dn3.getTagName()).thenReturn("BODY");
        doReturn(80).when(dn3).getWidth();
        AGNode agn3 = spy(new AGNode(dn3));

        DomNode dn4 = mock(DomNode.class);
        when(dn4.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn4.getxPath()).thenReturn("parent");
        when(dn4.getTagName()).thenReturn("BODY");
        doReturn(100).when(dn4).getWidth();
        AGNode agn4 = spy(new AGNode(dn4));

        double[] eq = new double[]{1.0,1.0,-20};

        HashMap<String, AGNode> vmap = new HashMap<>();
        HashMap<String, AGNode> vmap2 = new HashMap<>();
        vmap.put(agn1.getDomNode().getxPath(), agn1);
        vmap.put(agn2.getDomNode().getxPath(), agn2);
        vmap2.put(agn3.getDomNode().getxPath(), agn3);
        vmap2.put(agn4.getDomNode().getxPath(), agn4);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        AlignmentGraph ag2 = spy(new AlignmentGraph(dn3));

        doReturn(dn1).when(mockDoms).get(767);
        doReturn(dn3).when(mockDoms).get(768);
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(ag2).when(rlg).getAlignmentGraph(dn3);
        doReturn(vmap).when(ag).getVMap();
        doReturn(vmap2).when(ag2).getVMap();

        int expected = rlg.findWidthBreakpoint(eq, 767, 768, "node", "parent");
        assertEquals(768, expected);
    }

    @Test
    public void testFindWidthBreakpointRecursiveBranch2ReturnMinMinusOne() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);

        // Mock Dom and AG
        DomNode dn1 = mock(DomNode.class);
        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn1.getxPath()).thenReturn("node");
        when(dn1.getTagName()).thenReturn("BODY");
        doReturn(50).when(dn1).getWidth();
        AGNode agn1 = spy(new AGNode(dn1));

        DomNode dn2 = mock(DomNode.class);
        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn2.getxPath()).thenReturn("parent");
        when(dn2.getTagName()).thenReturn("BODY");
        doReturn(100).when(dn2).getWidth();
        AGNode agn2 = spy(new AGNode(dn2));

        DomNode dn3 = mock(DomNode.class);
        when(dn3.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn3.getxPath()).thenReturn("node");
        when(dn3.getTagName()).thenReturn("BODY");
        doReturn(50).when(dn3).getWidth();
        AGNode agn3 = spy(new AGNode(dn3));

        DomNode dn4 = mock(DomNode.class);
        when(dn4.getCoords()).thenReturn(new int[] {0,0,100,100});
        when(dn4.getxPath()).thenReturn("parent");
        when(dn4.getTagName()).thenReturn("BODY");
        doReturn(100).when(dn4).getWidth();
        AGNode agn4 = spy(new AGNode(dn4));

        double[] eq = new double[]{1.0,1.0,-20};

        HashMap<String, AGNode> vmap = new HashMap<>();
        HashMap<String, AGNode> vmap2 = new HashMap<>();
        vmap.put(agn1.getDomNode().getxPath(), agn1);
        vmap.put(agn2.getDomNode().getxPath(), agn2);
        vmap2.put(agn3.getDomNode().getxPath(), agn3);
        vmap2.put(agn4.getDomNode().getxPath(), agn4);
        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
        AlignmentGraph ag2 = spy(new AlignmentGraph(dn3));

        doReturn(dn1).when(mockDoms).get(767);
        doReturn(dn3).when(mockDoms).get(768);
        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
        doReturn(ag2).when(rlg).getAlignmentGraph(dn3);
        doReturn(vmap).when(ag).getVMap();
        doReturn(vmap2).when(ag2).getVMap();

        int expected = rlg.findWidthBreakpoint(eq, 767, 768, "node", "parent");
        assertEquals(766, expected);
    }

//    @Test
//    public void testWidthBreakpointReturnMid() {
//        PowerMockito.mockStatic(Redecheck.class);
//        rlg.url = "randomsite";
//        rlg.alreadyGathered = spy(new HashSet<Integer>());
//        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());
//        Map<Integer, DomNode> mockDoms = spy(new HashMap<Integer, DomNode>());
//        when(Redecheck.loadDoms(anyObject(), anyString())).thenReturn(mockDoms);
//
//        // Mock Dom and AG
//        // Mock Dom and AG
//        DomNode dn1 = mock(DomNode.class);
//        when(dn1.getCoords()).thenReturn(new int[] {0,0,100,100});
//        when(dn1.getxPath()).thenReturn("node");
//        doReturn("BODY").when(dn1).getTagName();
//        doReturn(50).when(dn1).getWidth();
//        AGNode agn1 = spy(new AGNode(dn1));
//
//        DomNode dn2 = mock(DomNode.class);
//        when(dn2.getCoords()).thenReturn(new int[] {0,0,100,100});
//        when(dn2.getxPath()).thenReturn("parent");
//        doReturn("BODY").when(dn2).getTagName();
//        doReturn(100).when(dn2).getWidth();
//        AGNode agn2 = spy(new AGNode(dn2));
//
//        double[] eq = new double[]{1.0,1.0,-20};
//
//        HashMap<String, AGNode> vmap = new HashMap<>();
//        vmap.put(agn1.getDomNode().getxPath(), agn1);
////        vmap.put(agn2.getDomNode().getxPath(), agn2);
//        AlignmentGraph ag = spy(new AlignmentGraph(dn1));
//        doReturn(dn1).when(mockDoms).get(anyInt());
//        doReturn(ag).when(rlg).getAlignmentGraph(dn1);
//        doReturn(vmap).when(ag).getVMap();
//
//        try {
//            int result = rlg.findWidthBreakpoint(eq,700, 800, "node", "parent");
////            verify(rlg, times(1)).findWidthBreakpoint(eq, 700, 750, "node", "parent");
//            assertEquals(750, result);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }


}