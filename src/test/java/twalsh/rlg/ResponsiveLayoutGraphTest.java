package twalsh.rlg;

import com.rits.cloning.Cloner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import twalsh.layout.*;
import twalsh.redecheck.Redecheck;

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
        rlg.binarySearch = true;
        rlg.restOfLayouts = spy(new ArrayList<LayoutFactory>());
        rlg.widths = new int[] {400,500,600,700};

        MockitoAnnotations.initMocks(this);
        cloner = new Cloner();

        n1 = new Node("first");
        n2 = new Node("second");
        n3 = new Node("third");
        acPC = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 300, 800, new boolean[] {true, false, false, false, true, false}, null);
        acSib = new AlignmentConstraint(n1, n2, Type.SIBLING, 400,1000, new boolean[] {true, false, false, false, false, false, true, true}, null);
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

//    @Test
//    public void testGetAlignments() {
//        assertEquals(rlg.getAlignments(), new HashMap<String, AlignmentConstraint>());
//        Node n1 = new Node("Test");
//        Node n2 = new Node("Another");
//        AlignmentConstraint ac  = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 300, 800, new boolean[] {true, false, false, false, true, false});
//        rlg.getAlignments().put(ac.generateKey(), ac);
//        assertEquals(rlg.getAlignments().size(), 1);
//    }
//
//    @Test
//    public void testSetAlignments() {
//        assertEquals(rlg.getAlignments(), new HashMap<String, AlignmentConstraint>());
//        Node n1 = new Node("Test");
//        Node n2 = new Node("Another");
//        AlignmentConstraint ac  = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 300, 800, new boolean[] {true, false, false, false, true, false});
//        HashMap<String,AlignmentConstraint> newAlCons = new HashMap<>();
//        newAlCons.put(ac.generateKey(), ac);
//        rlg.setAlignments(newAlCons);
//        assertEquals(rlg.getAlignments(), newAlCons);
//    }

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

//    @Test
//    public void decideBreakpointBothTrue() {
//        boolean b1 = true;
//        boolean b2 = true;
//        int min = 500;
//        int max = 501;
//        assertEquals(rlg.decideBreakpoint(min, max, b1, b2, false), max + 1);
//    }
//
//    @Test
//    public void decideBreakpointTrueFalse() {
//        boolean b1 = true;
//        boolean b2 = false;
//        int min = 500;
//        int max = 501;
//        assertEquals(rlg.decideBreakpoint(min, max, b1, b2, false), max);
//    }
//
//    @Test
//    public void decideBreakpointTestElse() {
//        boolean b1 = false;
//        boolean b2 = true;
//        int min = 500;
//        int max = 501;
//        assertEquals(rlg.decideBreakpoint(min, max, b1, b2, false), min);
//    }

//    @Test
//    public void getEquationAllSame() {
//        double[] set1 = new double[] {500,520};
//        double[] set2 = new double[] {100,100};
//        assertEquals(Arrays.equals(rlg.getEquationOfLine(set1, set2), new double[]{1, 0, set2[0]}), true);
//    }
//
//    @Test
//    public void getEquationGradientOne() {
//        double[] set1 = new double[] {500,520};
//        double[] set2 = new double[] {100,120};
//        assertEquals(Arrays.equals(rlg.getEquationOfLine(set1, set2), new double[]{1, 1, -400}), true);
//    }
//
//    @Test
//    public void getEquationGradientNotOne() {
//        double[] set1 = new double[] {500,520};
//        double[] set2 = new double[] {240,250};
//        assertEquals(Arrays.equals(rlg.getEquationOfLine(set1, set2), new double[]{1, 0.5, -10}), true);
//    }
//
//    @Test
//    public void bestFitAllSameUseAll() {
//        int[] set1 = new int[] {500,520,540,560};
//        int[] set2 = new int[] {100,100,100,100};
//        assertEquals(Arrays.equals(rlg.getBestFitLine(set1, set2, 4), new double[]{1, 0, set2[0]}), true);
//    }
//
//    @Test
//    public void bestFitAllSameWithSplit() {
//        int[] set1 = new int[] {500,520,540,800};
//        int[] set2 = new int[] {100,100,100,400};
//        assertEquals(Arrays.equals(rlg.getBestFitLine(set1, set2, 3), new double[]{1, 0, set2[0]}), true);
//    }
//
//    @Test
//    public void bestFitGradientOneUseAll() {
//        int[] set1 = new int[] {500,520,540,560};
//        int[] set2 = new int[] {480,500,520,540};
//        assertTrue(Arrays.equals(rlg.getBestFitLine(set1, set2, 4), new double[]{1, 1, -20}));
//    }
//
//    @Test
//    public void bestFitGradientOneWithSplit() {
//        int[] set1 = new int[] {500,520,540,700};
//        int[] set2 = new int[] {480,500,520,350};
//        assertTrue(Arrays.equals(rlg.getBestFitLine(set1, set2, 3), new double[]{1, 1, -20}));
//    }

//    @Test
//     public void matchValuesAllMatch() {
//        int[] set1 = new int[] {460,480,500,520,540,560};
//        int[] set2 = new int[] {440,460,480,500,520,540};
//        double[] equation = new double[]{1, 1, -20};
//        assertEquals(rlg.matchValuesToEquation(equation, set1, set2), 6);
//    }
//
//    @Test
//    public void matchValuesBreaksOnFirst() {
//        int[] set1 = new int[] {460,480,500,520,540,560};
//        int[] set2 = new int[] {440,460,420,500,520,540};
//        double[] equation = new double[]{1, 1, -20};
//        assertEquals(rlg.matchValuesToEquation(equation, set1, set2), 2);
//    }
//
//    @Test
//    public void matchValuesBreaksInMiddle() {
//        int[] set1 = new int[] {460,480,500,520,540,560};
//        int[] set2 = new int[] {440,460,480,500,560,540};
//        double[] equation = new double[]{1, 1, -20};
//        assertEquals(rlg.matchValuesToEquation(equation, set1, set2), 4);
//    }

    @Test
    public void setUpVisCons() {
        Element el = mock(Element.class);
        when(el.getBoundingCoords()).thenReturn(new int[4]);
        when(el.getXpath()).thenReturn("ANode");
        // AGNode agn = new AGNode(el);
        HashMap<String, Element> nodes = new HashMap<>();
        nodes.put(el.getXpath(), el);
        HashMap<String, VisibilityConstraint> visCons = new HashMap<String, VisibilityConstraint>();

        rlg.setUpVisibilityConstraints(nodes, visCons);
        assertEquals(visCons.entrySet().size(), 1);
    }

    @Test
    public void nodeMatchWithMatch() {
        Element el = mock(Element.class);
        when(el.getBoundingCoords()).thenReturn(new int[4]);
        when(el.getXpath()).thenReturn("matchable");
       // AGNode agn = new AGNode(el);

        HashMap<String, Element> prev = new HashMap<String, Element>();
        HashMap<String, Element> temp = new HashMap<String, Element>();
        prev.put(el.getXpath(), el);
        temp.put(el.getXpath(), el);

        HashMap<String, Element> prevToMatch = cloner.deepClone(prev);
        HashMap<String, Element> tempToMatch = cloner.deepClone(temp);

        rlg.checkForNodeMatch(prev, temp, prevToMatch, tempToMatch);
        assertEquals(prevToMatch.entrySet().size(), 0);
    }

    @Test
    public void nodeMatchWithNoMatch() {
        Element el = mock(Element.class);
        when(el.getBoundingCoords()).thenReturn(new int[4]);
        when(el.getXpath()).thenReturn("first");
        // AGNode agn = new AGNode(el);

        Element el2 = mock(Element.class);
        when(el2.getBoundingCoords()).thenReturn(new int[4]);
        when(el2.getXpath()).thenReturn("second");
        // AGNode el2 = new AGNode(el2);

        HashMap<String, Element> prev = new HashMap<String, Element>();
        HashMap<String, Element> temp = new HashMap<String, Element>();
        prev.put(el.getXpath(), el);
        temp.put(el2.getXpath(), el2);

        HashMap<String, Element> prevToMatch = cloner.deepClone(prev);
        HashMap<String, Element> tempToMatch = cloner.deepClone(temp);

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
        Element el = mock(Element.class);
        when(el.getBoundingCoords()).thenReturn(new int[4]);
        when(el.getXpath()).thenReturn("first");
        LayoutFactory lf = spy(new LayoutFactory("[]"));

        Node n = new Node(el.getXpath());
        HashMap<String, VisibilityConstraint> map = new HashMap<>();
        VisibilityConstraint vc = new VisibilityConstraint(400, 0);
        map.put(n.getXpath(), vc);
        HashMap<String, Element> lastMap = new HashMap<>();
        lastMap.put("first", el);

        when(lf.getElementMap()).thenReturn(lastMap);
        rlg.updateRemainingNodes(map, lf);
//        assertEquals(map.get(n.getXpath()).getDisappear(), 700);
    }

    @Test
    public void testUpdateAppearingNode() throws Exception {
        Element el = mock(Element.class);
        when(el.getBoundingCoords()).thenReturn(new int[4]);
        when(el.getXpath()).thenReturn("newOne");
        LayoutFactory lf = spy(new LayoutFactory("[]"));
        HashMap<String, Element> tempToMatch = new HashMap<>();
        tempToMatch.put(el.getXpath(), el);

        // Set up VC map
        Node n1 = new Node("first");
        Node n2 = new Node("second");
        Node n3 = new Node("third");
        HashMap<String, VisibilityConstraint> map = new HashMap<>();
        VisibilityConstraint vc = new VisibilityConstraint(400, 0);
        map.put(n1.getXpath(), vc);
        map.put(n2.getXpath(), vc);
        map.put(n3.getXpath(), vc);
        doReturn(520).when(rlg).searchForLayoutChange(anyString(), anyInt(), anyInt(), anyBoolean(), anyString(), anyBoolean());
        doReturn(0).when(rlg.restOfLayouts).indexOf(any(LayoutFactory.class));
        rlg.updateAppearingNode(tempToMatch, map, lf);
        assertEquals(map.entrySet().size(), 4);
        assertEquals(map.get(el.getXpath()).getAppear(), 520);
        assertEquals(map.get(el.getXpath()).getDisappear(), 0);
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

        Element el = mock(Element.class);
        when(el.getBoundingCoords()).thenReturn(new int[4]);
        when(el.getXpath()).thenReturn("first");
        LayoutFactory lf = spy(new LayoutFactory("[]"));
        HashMap<String, Element> prevToMatch = new HashMap<>();
        prevToMatch.put(el.getXpath(), el);

        doReturn(850).when(rlg).searchForLayoutChange(anyString(), anyInt(), anyInt(), anyBoolean(), anyString(), anyBoolean());
        doReturn(0).when(rlg.restOfLayouts).indexOf(any(LayoutFactory.class));
        rlg.updateDisappearingNode(prevToMatch, map, lf);

        // Check upper bound is 1 less than value returned by search
        assertEquals(map.get("first").getDisappear(), 849);
    }


    @Test
    public void setUpAlignmentCons() {
        HashMap<String, AlignmentConstraint> alCons = new HashMap<>();
        Element el1 = mock(Element.class);
        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
        when(el1.getXpath()).thenReturn("first");
        Element el2 = mock(Element.class);
        when(el2.getBoundingCoords()).thenReturn(new int[]{25, 25, 75, 75});
        when(el2.getXpath()).thenReturn("second");
        Element el3 = mock(Element.class);
        when(el3.getBoundingCoords()).thenReturn(new int[]{75, 75, 90, 90});
        when(el3.getXpath()).thenReturn("third");

        Node n1 = new Node(el1.getXpath());
        Node n2 = new Node(el2.getXpath());
        Node n3 = new Node(el3.getXpath());
        rlg.nodes.put(n1.getXpath(), n1);
        rlg.nodes.put(n2.getXpath(), n2);
        rlg.nodes.put(n3.getXpath(), n3);

        ParentChild c = new ParentChild(el1, el2);
        HashMap<String, Relationship> edgeMap = new HashMap<>();
        edgeMap.put(c.getKey(), c);

        Sibling sibling = new Sibling(el2, el3);
//        when(sibling.generateAttributeSet()).thenReturn("{}");
        edgeMap.put(sibling.getKey(), sibling);

        rlg.setUpAlignmentConstraints(edgeMap, alCons);
        assertEquals(alCons.size(), 2);
        assertEquals(alCons.get(c.getNode1().getXpath() + " contains " + c.getNode2().getXpath() + LayoutFactory.generateEdgeLabelling(c)).generateKey(),
                c.getNode1().getXpath()  + " contains "  + c.getNode2().getXpath()+ LayoutFactory.generateEdgeLabelling(c));

        String key = sibling.getNode1().getXpath() + " sibling of " + sibling.getNode2().getXpath() + sibling.generateAttributeSet();
        assertEquals(alCons.get(key).generateKey(),
                sibling.getNode1().getXpath()  + " sibling of "+ sibling.getNode2().getXpath() + sibling.generateAttributeSet());
    }

    @Test
    public void testCheckForEdgeMatchTrue() {
        Element el1 = new Element("first",0,0,100,100);
        Element el2 = new Element("second", 25, 25, 75, 75);
        Element el3 = new Element("third", 75, 75, 90, 90);

        ParentChild c = new ParentChild(el1, el2);
        Sibling sibling = new Sibling(el2, el3);

        HashMap<String, Relationship> previousMap = new HashMap<>();
        previousMap.put(c.getKey(), c);
        previousMap.put(sibling.getKey(), sibling);

        HashMap<String, Relationship> temp = new HashMap<>();
        temp.put(c.getKey(), c);
        temp.put(sibling.getKey(), sibling);

        HashMap<String, Relationship> previousToMatch = cloner.deepClone(previousMap);
        HashMap<String, Relationship> tempToMatch = cloner.deepClone(temp);

        rlg.checkForEdgeMatch(previousMap, previousToMatch, temp, tempToMatch);

        // Check matched edges have been removed from maps
        assertEquals(previousToMatch.size(), 0);
        assertEquals(tempToMatch.size(), 0);
    }
////
    @Test
    public void testCheckForEdgeMatchFlippedSibling() {
        Element el1 = new Element("first",0,0,100,100);
        Element el2 = new Element("second", 25, 25, 75, 75);
        Element el3 = new Element("third", 75, 75, 90, 90);

        Sibling sibling = new Sibling(el2, el3);
        Sibling flipped = new Sibling(el3, el2);

        HashMap<String, Relationship> previousMap = new HashMap<>();
        previousMap.put(sibling.getKey(), sibling);

        HashMap<String, Relationship> temp = new HashMap<>();
        temp.put(flipped.getKey(), flipped);
        assertEquals(previousMap.size(), 1);
        assertEquals(temp.size(), 1);

        HashMap<String, Relationship> previousToMatch = cloner.deepClone(previousMap);
        HashMap<String, Relationship> tempToMatch = cloner.deepClone(temp);

        rlg.checkForEdgeMatch(previousMap, previousToMatch, temp, tempToMatch);

        // Check matched edges have been removed from maps
        assertEquals(previousToMatch.size(), 0);
        assertEquals(tempToMatch.size(), 0);
    }

    @Test
    public void testCheckForEdgeMatchFalse() {
        Element el1 = new Element("first",0,0,100,100);
        Element el2 = new Element("second", 25, 25, 75, 75);
        Element el3 = new Element("third", 75, 75, 90, 90);

        ParentChild c = new ParentChild(el1, el2);
        Sibling sibling = new Sibling(el2, el3);

        HashMap<String, Relationship> previousMap = new HashMap<>();
        previousMap.put(c.getKey(), c);

        HashMap<String, Relationship> temp = new HashMap<>();
        temp.put(sibling.getKey(), sibling);


        HashMap<String, Relationship> previousToMatch = cloner.deepClone(previousMap);
        HashMap<String, Relationship> tempToMatch = cloner.deepClone(temp);

        rlg.checkForEdgeMatch(previousMap, previousToMatch, temp, tempToMatch);

        // Check matched edges have been removed from maps
        assertEquals(previousToMatch.size(), 1);
        assertEquals(tempToMatch.size(), 1);
    }

    @Test
    public void testUpdateRemainingEdges() {
        // Set up final alignment graph
        Element el1 = new Element("first",0,0,100,100);
        Element el2 = new Element("second", 25, 25, 75, 75);
        Element el3 = new Element("third", 75, 75, 90, 90);
        ParentChild c = new ParentChild(el1, el2);
        ParentChild c2 = new ParentChild(el1, el3);
        Sibling sibling = new Sibling(el2, el3);
        HashMap<String, Relationship> previousMap = new HashMap<>();
        previousMap.put(c.getKey(), c);
        previousMap.put(c2.getKey(), c2);
        previousMap.put(sibling.getKey(), sibling);
        LayoutFactory lf = spy(new LayoutFactory("[]"));

        AlignmentConstraint ac1 = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 400, 0, c.generateAttributeArray(), null);
        AlignmentConstraint ac2 = new AlignmentConstraint(n1, n3, Type.PARENT_CHILD, 400, 0, c2.generateAttributeArray(), null);
        AlignmentConstraint ac3 = new AlignmentConstraint(n2, n3, Type.SIBLING, 400, 0, sibling.generateAttributeArray(), null);

        HashMap<String, AlignmentConstraint> cons = new HashMap<>();
        cons.put(ac1.generateKey(), ac1);
        cons.put(ac2.generateKey(), ac2);
        cons.put(ac3.generateKey(), ac3);

        rlg.getAlignmentConstraints().put(ac1.generateKey(), new int[]{rlg.widths[0],0}, ac1);
        rlg.getAlignmentConstraints().put(ac2.generateKey(), new int[]{rlg.widths[0],0}, ac2);
        rlg.getAlignmentConstraints().put(ac3.generateKey(), new int[]{rlg.widths[0],0}, ac3);

        doReturn(previousMap).when(lf).getRelationships();

        rlg.updateRemainingEdges(cons, lf);
        assertEquals(700, cons.get(ac1.generateKey()).getMax());
        assertEquals(700, cons.get(ac2.generateKey()).getMax());
        assertEquals(700, cons.get(ac3.generateKey()).getMax());
    }
//
    @Test
    public void testUpdateAppearingEdges() throws InterruptedException {
        // Set up final alignment graph
        Element el1 = new Element("first",0,0,100,100);
        Element el2 = new Element("second", 25, 25, 75, 75);
        Element el3 = new Element("third", 75, 75, 90, 90);
        ParentChild c = new ParentChild(el1, el2);
        ParentChild c2 = new ParentChild(el1, el3);
        Sibling sibling = new Sibling(el2, el3);
        HashMap<String, Relationship> previousMap = new HashMap<>();
//        previousMap.put(c.getKey(), c);
//        previousMap.put(c2.getKey(), c2);
        previousMap.put(sibling.getKey(), sibling);
        LayoutFactory ag = spy(new LayoutFactory("[]"));

        // Set up RLG Nodes
        Node n1 = new Node("first");
        Node n2 = new Node("second");
        Node n3 = new Node("third");
        rlg.nodes.put(n1.getXpath(), n1);
        rlg.nodes.put(n2.getXpath(), n2);
        rlg.nodes.put(n3.getXpath(), n3);
        AlignmentConstraint ac1 = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 400, 0, c.generateAttributeArray(), null);
        AlignmentConstraint ac2 = new AlignmentConstraint(n1, n3, Type.PARENT_CHILD, 400, 0, c2.generateAttributeArray(), null);
        AlignmentConstraint ac3 = new AlignmentConstraint(n2, n3, Type.SIBLING, 400, 0, sibling.generateAttributeArray(), null);

        HashMap<String, AlignmentConstraint> cons = new HashMap<>();
        cons.put(ac1.generateKey(), ac1);
        cons.put(ac2.generateKey(), ac2);
        rlg.getAlignmentConstraints().put(ac1.generateKey(), new int[]{rlg.widths[0],0}, ac1);
        rlg.getAlignmentConstraints().put(ac2.generateKey(), new int[]{rlg.widths[0],0}, ac2);
        assertEquals(rlg.getAlignmentConstraints().size(), 2);


        doReturn(520).when(rlg).searchForLayoutChange(anyString(), anyInt(), anyInt(), anyBoolean(), anyString(), anyBoolean());
        doReturn(0).when(rlg.restOfLayouts).indexOf(any(LayoutFactory.class));

        rlg.updateAppearingEdges(previousMap, rlg.getAlignmentConstraints(), cons, ag);
        assertEquals(rlg.getAlignmentConstraints().size(), 3);
        assertEquals(520, cons.get(ac3.generateKey()).getMin());
    }

    @Test
    public void testDisappearingNodes() throws InterruptedException {
        // Set up final alignment graph
        Element el1 = new Element("first",0,0,100,100);
        Element el2 = new Element("second", 25, 25, 75, 75);
        Element el3 = new Element("third", 75, 75, 90, 90);
        ParentChild c = new ParentChild(el1, el2);
        ParentChild c2 = new ParentChild(el1, el3);
        Sibling sibling = new Sibling(el2, el3);
        HashMap<String, Relationship> previousMap = new HashMap<>();
        previousMap.put(c2.getKey(), c2);
        previousMap.put(sibling.getKey(), sibling);
        LayoutFactory lf = spy(new LayoutFactory("[]"));

        Node n1 = new Node("first");
        Node n2 = new Node("second");
        Node n3 = new Node("third");
        AlignmentConstraint ac1 = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 400, 0, c.generateAttributeArray(), null);
        AlignmentConstraint ac2 = new AlignmentConstraint(n1, n3, Type.PARENT_CHILD, 400, 0, c2.generateAttributeArray(), null);
        AlignmentConstraint ac3 = new AlignmentConstraint(n2, n3, Type.SIBLING, 400, 0, sibling.generateAttributeArray(), null);

        HashMap<String, AlignmentConstraint> cons = new HashMap<>();
        cons.put(ac1.generateKey(), ac1);
        cons.put(ac2.generateKey(), ac2);
        cons.put(ac3.generateKey(), ac3);

        rlg.getAlignmentConstraints().put(ac1.generateKey(), new int[]{rlg.widths[0],0}, ac1);
        rlg.getAlignmentConstraints().put(ac2.generateKey(), new int[]{rlg.widths[0],0}, ac2);
        rlg.getAlignmentConstraints().put(ac3.generateKey(), new int[]{rlg.widths[0], 0}, ac3);
        assertEquals(3, rlg.getAlignmentConstraints().size());

        doReturn(previousMap).when(lf).getRelationships();
        doReturn(645).when(rlg).searchForLayoutChange(anyString(), anyInt(), anyInt(), anyBoolean(), anyString(), anyBoolean());
        doReturn(0).when(rlg.restOfLayouts).indexOf(any(LayoutFactory.class));

        rlg.updateDisappearingEdges(previousMap, rlg.getAlignmentConstraints(), lf);
        assertEquals(0, cons.get(ac1.generateKey()).getMax());
        assertEquals(644, cons.get(ac2.generateKey()).getMax());
        assertEquals(644, cons.get(ac3.generateKey()).getMax());
    }
////
    @Test
    public void testAddParentConstraintsToNodes() {
        Node n = new Node("test");
        Node p = new Node("parent");
        rlg.nodes.put(n.getXpath(), n);
        rlg.nodes.put(p.getXpath(), p);
        AlignmentConstraint ac = new AlignmentConstraint(p, n, Type.PARENT_CHILD, 400, 800, new boolean[] {true, false, false, false, false, false}, null);
        rlg.getAlignmentConstraints().put(ac.generateKey(), new int[]{400,0}, ac);
        rlg.addParentConstraintsToNodes();
        assertEquals(n.getParentConstraints().size(), 1);
        assertEquals(n.getParentConstraints().get(0), ac);
    }
//
////    @Test
////    public void testAddWidthConstraintsToNodes() {
////        Node n = new Node("test");
////        Node p = new Node("parent");
////        rlg.nodes.put(n.getXpath(), n);
////        rlg.nodes.put(p.getXpath(), p);
////
////        WidthConstraint wc = new WidthConstraint(400,1000, 1, p, -10);
////        rlg.widthConstraints.put(n.getXpath(), new int[] {400,0}, wc);
////        rlg.addWidthConstraintsToNodes();
////        assertEquals(n.getWidthConstraints().size(), 1);
////        assertEquals(n.getWidthConstraints().get(0), wc);
////
////    }
////
////    @Test
////    public void testGenerateEdgeMap() {
////        Element el1 = mock(Element.class);
////        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
////        when(el1.getXpath()).thenReturn("first");
////        when(el1.getTagName()).thenReturn("BODY");
//       // AGNode el1 = new AGNode(el1);
////        Element el2 = mock(Element.class);
////        when(el2.getBoundingCoords()).thenReturn(new int[]{25, 25, 75, 75});
////        when(el2.getXpath()).thenReturn("second");
//       // AGNode el2 = new AGNode(el2);
////
////        AlignmentGraph ag = spy(new AlignmentGraph(el1));
////        ParentChild c = new ParentChild(el1, el2);
////        Sibling s = new Sibling(el2, el1);
////        ArrayList<ParentChild> cs = new ArrayList<ParentChild>();
////        ArrayList<Sibling> ss = new ArrayList<Sibling>();
////        cs.add(c);
////        ss.add(s);
////
////        when (ag.getParentChild()).thenReturn(cs);
////        when (ag.getSiblings()).thenReturn(ss);
////
////        HashMap<String, Edge> map = rlg.generateEdgeMapFromAG(ag);
////        assertEquals(map.size(), 2);
////        assertEquals(map.get(c.getNode1().getXpath() + c.getNode2().getXpath() + " contains " + c.generateEdgeLabelling()), c);
////    }
//
////    @Test
////    public void testGetWidthsForConstraintsOneParent() {
////        Node n = new Node("test");
////        Node p = new Node("parent");
////        AlignmentConstraint ac = new AlignmentConstraint(n, p, Type.PARENT_CHILD, 400, 1000, new boolean[] {false, false, false, false, false});
////        ArrayList<AlignmentConstraint> acs = new ArrayList<>();
////        acs.add(ac);
////        ArrayList<int[]> ranges = rlg.getWidthsForConstraints(acs);
////        assertEquals(ranges.size(), 1);
////        int[] array = ranges.get(0);
////        assertEquals(true, Arrays.equals(array, new int[] {400, 500, 600, 700}));
////    }
////
////    @Test
////    public void testGetWidthsForConstraintsTwoParents() {
////        Node n = new Node("test");
////        Node p = new Node("parent");
////        Node p2 = new Node("another");
////        AlignmentConstraint ac = new AlignmentConstraint(p, n, Type.PARENT_CHILD, 400, 550, new boolean[] {false, false, false, false, false});
////        AlignmentConstraint ac2 = new AlignmentConstraint(p2, n, Type.PARENT_CHILD, 551, 720, new boolean[] {false, false, false, false, false, false});
////        ArrayList<AlignmentConstraint> acs = new ArrayList<>();
////        acs.add(ac);
////        acs.add(ac2);
////        ArrayList<int[]> ranges = rlg.getWidthsForConstraints(acs);
////        assertEquals(ranges.size(), 2);
////        Iterator iter = ranges.iterator();
////        int[] array = (int[]) iter.next();
////        int[] array2 = (int[]) iter.next();
////        boolean rightWayRound = Arrays.equals(array, new int[] {400, 500}) && (Arrays.equals(array2, new int[] {600, 700}));
////        boolean flipped = Arrays.equals(array2, new int[] {400, 500}) && (Arrays.equals(array, new int[] {600, 700}));
////        assertEquals(true, rightWayRound || flipped);
////
////    }
//
//    @Test
//    public void testPopulateWidthArrays() throws Exception {
//        Element el1 = mock(Element.class);
//        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el1.getXpath()).thenReturn("parent");
//        when(el1.getTagName()).thenReturn("BODY");
//
//        Element el2 = mock(Element.class);
//        when(el2.getBoundingCoords()).thenReturn(new int[] {25,10,75,90});
//        when(el2.getXpath()).thenReturn("node");
//        when(el2.getTagName()).thenReturn("DIV");
//        el1.addChild(el2);
//
//        HashMap<String, Element> elmap = spy(new HashMap<String, Element>());
//        elmap.put(el1.getXpath(), el1);
//        elmap.put(el2.getXpath(), el2);
//
//        int[] validWidths = rlg.widths;
//        int[] widthsTemp = new int[4];
//        int[] parentWidths = new int[4];
//        int[] childWidths = new int[4];
//        LayoutFactory agf = spy(new LayoutFactory(el1));
//        rlg.doms = spy(new HashMap<Integer, Element>());
//        agf.ElementMap.put(el1.getXpath(), el1);
//        agf.ElementMap.put(el2.getXpath(), el2);
//
//        when(rlg.doms.get(anyInt())).thenReturn(el1);
//        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(any(Integer.class));
//        PowerMockito.doReturn(elmap).when(agf).getElementMap();
//
//        rlg.populateWidthArrays(validWidths, widthsTemp, parentWidths, childWidths, "node", "parent");
//
//        assertEquals(400, widthsTemp[0]);
//        assertEquals(100, parentWidths[0]);
//        assertEquals(50, childWidths[0]);
//    }
//
    @Test
    public void testFindAppearPointRecursiveBranch1NodeMatch() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        Element el1 = new Element("first",0,0,100,100);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());

        HashMap<String, Element> elmap = spy(new HashMap<String, Element>());
        elmap.put(el1.getXpath(), el1);
        LayoutFactory lf = spy(new LayoutFactory("[]"));

        PowerMockito.doReturn(lf).when(rlg).getLayoutFactory(any(Integer.class));
        PowerMockito.doReturn(elmap).when(lf).getElementMap();

        rlg.searchForLayoutChange("first", 400, 500, true, "dontneed", true);
        verify(rlg, times(1)).searchForLayoutChange("first", 400, 450, true, "dontneed", true);
    }

    @Test
    public void testFindAppearPointRecursiveBranch1NodeNoMatch() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        Element el1 = new Element("first",0,0,100,100);
        Element el2 = new Element("second", 25, 25, 75, 75);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());

        HashMap<String, Element> elmap = spy(new HashMap<String, Element>());
        elmap.put(el2.getXpath(), el2);
        LayoutFactory lf = spy(new LayoutFactory("[]"));

        PowerMockito.doReturn(lf).when(rlg).getLayoutFactory(any(Integer.class));
        PowerMockito.doReturn(elmap).when(lf).getElementMap();

        rlg.searchForLayoutChange("first", 400, 500, true, "dontneed", true);
        verify(rlg, times(1)).searchForLayoutChange("first", 450, 500, true, "dontneed", true);
    }

    @Test
    public void testSearchDisappearRecursiveBranch1NodeNoMatch() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        Element el1 = new Element("first",0,0,100,100);
        Element el2 = new Element("second", 25, 25, 75, 75);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());

        HashMap<String, Element> elmap = spy(new HashMap<String, Element>());
        elmap.put(el1.getXpath(), el1);
        LayoutFactory lf = spy(new LayoutFactory("[]"));

        PowerMockito.doReturn(lf).when(rlg).getLayoutFactory(any(Integer.class));
        PowerMockito.doReturn(elmap).when(lf).getElementMap();

        rlg.searchForLayoutChange("first", 400, 500, true, "dontneed", false);
        verify(rlg, times(1)).searchForLayoutChange("first", 450, 500, true, "dontneed", false);
    }

    @Test
    public void testSearchDisappearRecursiveBranch1NodeMatch() throws InterruptedException {
        PowerMockito.mockStatic(Redecheck.class);
        Element el1 = new Element("first",0,0,100,100);
        Element el2 = new Element("second", 25, 25, 75, 75);
        rlg.url = "randomsite";
        rlg.alreadyGathered = spy(new HashSet<Integer>());
        doReturn(true).when(rlg.alreadyGathered).contains(anyInt());

        HashMap<String, Element> elmap = spy(new HashMap<String, Element>());
        elmap.put(el2.getXpath(), el2);
        LayoutFactory lf = spy(new LayoutFactory("[]"));

        PowerMockito.doReturn(lf).when(rlg).getLayoutFactory(any(Integer.class));
        PowerMockito.doReturn(elmap).when(lf).getElementMap();

        rlg.searchForLayoutChange("first", 400, 500, true, "dontneed", false);
        verify(rlg, times(1)).searchForLayoutChange("first", 400, 450, true, "dontneed", false);
    }
//
//    @Test
//    public void testFindAppearPointRecursiveBranch2ReturnMin() throws InterruptedException {
//        PowerMockito.mockStatic(Redecheck.class);
//        rlg.url = "randomsite";
//        rlg.alreadyGathered = spy(new HashSet<Integer>());
//        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
//        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
//        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
//
//        // Mock Dom and AG
//        Element el1 = mock(Element.class);
//        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el1.getXpath()).thenReturn("node");
//        when(el1.getTagName()).thenReturn("BODY");
//        AGNode el1 = spy(new AGNode(el1));
//
//        Element el2 = mock(Element.class);
//        when(el2.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el2.getXpath()).thenReturn("parent");
//        when(el2.getTagName()).thenReturn("BODY");
//        AGNode el2 = spy(new AGNode(el1));
//
//        HashMap<String, Element> vmap = spy(new HashMap<String, Element>());
//        HashMap<String, Element> vmap2 = spy(new HashMap<String, Element>());
//        vmap.put(el1.getXpath(), el1);
//        LayoutFactory agf = spy(new LayoutFactory(el1));
//        LayoutFactory agf2 = spy(new LayoutFactory(el2));
//
//        doReturn(el1).when(mockDoms).get(420);
//        doReturn(el2).when(mockDoms).get(421);
//        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(420);
//        PowerMockito.doReturn(vmap).when(agf).getElementMap();
//        PowerMockito.doReturn(agf2).when(rlg).getLayoutFactory(421);
//        PowerMockito.doReturn(vmap2).when(agf2).getElementMap();
//
//        int expected = rlg.searchForLayoutChange("node", 420, 421, true, "dontneed");
//        assertEquals(420, expected);
//    }
//
//    @Test
//    public void testFindAppearPointRecursiveBranch2ReturnMax() throws InterruptedException {
//        PowerMockito.mockStatic(Redecheck.class);
//        rlg.url = "randomsite";
//        rlg.alreadyGathered = spy(new HashSet<Integer>());
//        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
//        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
//        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
//
//        // Mock Dom and AG
//        Element el1 = mock(Element.class);
//        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el1.getXpath()).thenReturn("node");
//        when(el1.getTagName()).thenReturn("BODY");
//        AGNode el1 = spy(new AGNode(el1));
//
//        Element el2 = mock(Element.class);
//        when(el2.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el2.getXpath()).thenReturn("parent");
//        when(el2.getTagName()).thenReturn("BODY");
//        AGNode el2 = spy(new AGNode(el1));
//
//        HashMap<String, Element> vmap = new HashMap<>();
//        HashMap<String, Element> vmap2 = new HashMap<>();
////        vmap.put(el2.getElement().getXpath(), el2);
//        vmap2.put(el1.getXpath(), el1);
//        LayoutFactory agf = spy(new LayoutFactory(el1));
//        LayoutFactory agf2 = spy(new LayoutFactory(el2));
//
//        doReturn(el1).when(mockDoms).get(420);
//        doReturn(el2).when(mockDoms).get(421);
//        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(420);
//        PowerMockito.doReturn(vmap).when(agf).getElementMap();
//        PowerMockito.doReturn(agf2).when(rlg).getLayoutFactory(421);
//        PowerMockito.doReturn(vmap2).when(agf2).getElementMap();
//
//        int expected = rlg.searchForLayoutChange("node", 420, 421, true, "dontneed");
//        assertEquals(421, expected);
//    }
//
//    @Test
//    public void testFindAppearPointRecursiveBranch2ReturnMaxPlusOne() throws InterruptedException {
//        PowerMockito.mockStatic(Redecheck.class);
//        rlg.url = "randomsite";
//        rlg.alreadyGathered = spy(new HashSet<Integer>());
//        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
//        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
//        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
//
//        // Mock Dom and AG
//        Element el1 = mock(Element.class);
//        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el1.getXpath()).thenReturn("node");
//        when(el1.getTagName()).thenReturn("BODY");
//        AGNode el1 = spy(new AGNode(el1));
//
//        Element el2 = mock(Element.class);
//        when(el2.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el2.getXpath()).thenReturn("parent");
//        when(el2.getTagName()).thenReturn("BODY");
//        AGNode el2 = spy(new AGNode(el1));
//
//        HashMap<String, Element> vmap = spy(new HashMap<String, Element>());
//        HashMap<String, Element> vmap2 = spy(new HashMap<String, Element>());
//        LayoutFactory agf = spy(new LayoutFactory(el1));
//        LayoutFactory agf2 = spy(new LayoutFactory(el2));
//
//        doReturn(el1).when(mockDoms).get(420);
//        doReturn(el2).when(mockDoms).get(421);
//        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(420);
//        PowerMockito.doReturn(vmap).when(agf).getElementMap();
//        PowerMockito.doReturn(agf2).when(rlg).getLayoutFactory(421);
//        PowerMockito.doReturn(vmap2).when(agf2).getElementMap();
//
//        int expected = rlg.searchForLayoutChange("nomatch", 420, 421, true, "dontneed");
//        assertEquals(422, expected);
//    }
//
//    @Test
//    public void testFindDisappearPointRecursiveBranch1NodeMatch() throws InterruptedException {
//        PowerMockito.mockStatic(Redecheck.class);
//        rlg.url = "randomsite";
//        rlg.alreadyGathered = spy(new HashSet<Integer>());
//        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
//        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
//        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
//
//        // Mock Dom and AG
//        Element el1 = mock(Element.class);
//        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el1.getXpath()).thenReturn("node");
//        when(el1.getTagName()).thenReturn("BODY");
//        AGNode el1 = spy(new AGNode(el1));
//        HashMap<String, Element> vmap = spy(new HashMap<String, Element>());
//        vmap.put(el1.getXpath(), el1);
//        LayoutFactory agf = spy(new LayoutFactory(el1));
//
//        doReturn(el1).when(mockDoms).get(anyInt());
//        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(anyInt());
//        PowerMockito.doReturn(vmap).when(agf).getElementMap();
//
//        rlg.findDisappearPoint("node", 400, 500, true, "dontneed");
//        verify(rlg, times(1)).findDisappearPoint("node", 450, 500, true, "dontneed");
//    }
//
//    @Test
//    public void testFindDisappearPointRecursiveBranch1NodeNoMatch() throws InterruptedException {
//        PowerMockito.mockStatic(Redecheck.class);
//        rlg.url = "randomsite";
//        rlg.alreadyGathered = spy(new HashSet<Integer>());
//        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
//        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
//        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
//
//        // Mock Dom and AG
//        Element el1 = mock(Element.class);
//        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el1.getXpath()).thenReturn("parent");
//        when(el1.getTagName()).thenReturn("BODY");
//        AGNode el1 = spy(new AGNode(el1));
//        HashMap<String, Element> vmap = spy(new HashMap<String, Element>());
//        vmap.put(el1.getXpath(), el1);
//        LayoutFactory agf = spy(new LayoutFactory(el1));
//
//        doReturn(el1).when(mockDoms).get(anyInt());
//        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(anyInt());
//        PowerMockito.doReturn(vmap).when(agf).getElementMap();
//
//        rlg.findDisappearPoint("node", 400, 500, true, "dontneed");
//        verify(rlg, times(1)).findDisappearPoint("node", 400, 450, true, "dontneed");
//    }
//
//    @Test
//    public void testFindDisappearPointRecursiveBranch2ReturnMin() throws InterruptedException {
//        PowerMockito.mockStatic(Redecheck.class);
//        rlg.url = "randomsite";
//        rlg.alreadyGathered = spy(new HashSet<Integer>());
//        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
//        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
//        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
//
//        // Mock Dom and AG
//        Element el1 = mock(Element.class);
//        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el1.getXpath()).thenReturn("node");
//        when(el1.getTagName()).thenReturn("BODY");
//
//        Element el2 = mock(Element.class);
//        when(el2.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el2.getXpath()).thenReturn("parent");
//        when(el2.getTagName()).thenReturn("BODY");
//
//        HashMap<String, AGNode> vmap = spy(new HashMap<String, AGNode>());
//        HashMap<String, AGNode> vmap2 = spy(new HashMap<String, AGNode>());
//        LayoutFactory agf = spy(new LayoutFactory(el1));
//        LayoutFactory agf2 = spy(new LayoutFactory(el2));
//
//        doReturn(el1).when(mockDoms).get(420);
//        doReturn(el2).when(mockDoms).get(421);
//        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(420);
//        PowerMockito.doReturn(vmap).when(agf).getElementMap();
//        PowerMockito.doReturn(agf2).when(rlg).getLayoutFactory(421);
//        PowerMockito.doReturn(vmap2).when(agf2).getElementMap();
//
//        int expected = rlg.findDisappearPoint("node", 420, 421, true, "dontneed");
//        assertEquals(420, expected);
//    }
//
//    @Test
//    public void testFindDisappearPointRecursiveBranch2ReturnMax() throws InterruptedException {
//        PowerMockito.mockStatic(Redecheck.class);
//        rlg.url = "randomsite";
//        rlg.alreadyGathered = spy(new HashSet<Integer>());
//        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
//        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
//        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
//
//        // Mock Dom and AG
//        Element el1 = mock(Element.class);
//        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el1.getXpath()).thenReturn("node");
//        when(el1.getTagName()).thenReturn("BODY");
//        AGNode el1 = spy(new AGNode(el1));
//
//        Element el2 = mock(Element.class);
//        when(el2.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el2.getXpath()).thenReturn("parent");
//        when(el2.getTagName()).thenReturn("BODY");
//        AGNode el2 = spy(new AGNode(el1));
//
//        HashMap<String, Element> vmap = spy(new HashMap<String, Element>());
//        HashMap<String, Element> vmap2 = spy(new HashMap<String, Element>());
//        vmap.put(el1.getXpath(), el1);
//        LayoutFactory agf = spy(new LayoutFactory(el1));
//        LayoutFactory agf2 = spy(new LayoutFactory(el2));
//
//        doReturn(el1).when(mockDoms).get(420);
//        doReturn(el2).when(mockDoms).get(421);
//        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(420);
//        PowerMockito.doReturn(vmap).when(agf).getElementMap();
//        PowerMockito.doReturn(agf2).when(rlg).getLayoutFactory(421);
//        PowerMockito.doReturn(vmap2).when(agf2).getElementMap();
//
//        int expected = rlg.findDisappearPoint("node", 420, 421, true, "dontneed");
//        assertEquals(421, expected);
//    }
//
//    @Test
//    public void testFindDisappearPointRecursiveBranch2ReturnMaxPlusOne() throws InterruptedException {
//        PowerMockito.mockStatic(Redecheck.class);
//        rlg.url = "randomsite";
//        rlg.alreadyGathered = spy(new HashSet<Integer>());
//        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
//        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
//        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
//
//        // Mock Dom and AG
//        Element el1 = mock(Element.class);
//        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el1.getXpath()).thenReturn("node");
//        when(el1.getTagName()).thenReturn("BODY");
//        AGNode el1 = spy(new AGNode(el1));
//
//        Element el2 = mock(Element.class);
//        when(el2.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
//        when(el2.getXpath()).thenReturn("parent");
//        when(el2.getTagName()).thenReturn("BODY");
//        AGNode el2 = spy(new AGNode(el1));
//
//        // HashMap<String, AGNode> vmap = new HashMap<>();
//        // HashMap<String, AGNode> vmap2 = new HashMap<>();
//        vmap.put(el1.getXpath(), el1);
//        vmap2.put(el1.getXpath(), el1);
//        LayoutFactory agf = spy(new LayoutFactory(el1));
//        LayoutFactory agf2 = spy(new LayoutFactory(el2));
//
//        doReturn(el1).when(mockDoms).get(420);
//        doReturn(el2).when(mockDoms).get(421);
//        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(420);
//        PowerMockito.doReturn(vmap).when(agf).getElementMap();
//        PowerMockito.doReturn(agf2).when(rlg).getLayoutFactory(421);
//        PowerMockito.doReturn(vmap2).when(agf2).getElementMap();
//
//        int expected = rlg.findDisappearPoint("node", 420, 421, true, "dontneed");
//        assertEquals(422, expected);
//    }
//
////    @Test
////    public void testWidthBreakpointRecursiveTrue() {
////        PowerMockito.mockStatic(Redecheck.class);
////        rlg.url = "randomsite";
////        rlg.alreadyGathered = spy(new HashSet<Integer>());
////        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
////        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
////        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
////
////        // Mock Dom and AG
////        // Mock Dom and AG
////        Element el1 = mock(Element.class);
////        when(el1.getBoundingCoords()).thenReturn(new int[] {10,0,90,100});
////        when(el1.getXpath()).thenReturn("node");
////        doReturn("BODY").when(el1).getTagName();
////        doReturn(80).when(rlg).getWidthOfElement(el1);
//       // AGNode el1 = spy(new AGNode(el1));
////
////        Element el2 = mock(Element.class);
////        when(el2.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
////        when(el2.getXpath()).thenReturn("parent");
////        doReturn("BODY").when(el2).getTagName();
////        doReturn(100).when(rlg).getWidthOfElement(el2);
//       // AGNode el2 = spy(new AGNode(el2));
////
////        double[] eq = new double[]{1.0,1.0,-20};
////
////        HashMap<String, Element> vmap = new HashMap<>();
////        vmap.put(el1.getXpath(), el1);
////        vmap.put(el2.getXpath(), el2);
////        LayoutFactory agf = spy(new LayoutFactory(el1));
////        doReturn(el1).when(mockDoms).get(anyInt());
////        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(anyInt());
////        PowerMockito.doReturn(vmap).when(agf).getElementMap();
////
////        try {
////            rlg.findWidthBreakpoint(eq,700, 800, "node", "parent");
////            verify(rlg, times(1)).findWidthBreakpoint(eq, 750, 800, "node", "parent");
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
////
////    }
////
////    @Test
////    public void testWidthBreakpointRecursiveFalse() {
////        PowerMockito.mockStatic(Redecheck.class);
////        rlg.url = "randomsite";
////        rlg.alreadyGathered = spy(new HashSet<Integer>());
////        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
////        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
////        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
////
////        // Mock Dom and AG
////        // Mock Dom and AG
////        Element el1 = mock(Element.class);
////        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
////        when(el1.getXpath()).thenReturn("node");
////        doReturn("BODY").when(el1).getTagName();
////        doReturn(50).when(rlg).getWidthOfElement(el1);
//       // AGNode el1 = spy(new AGNode(el1));
////
////        Element el2 = mock(Element.class);
////        when(el2.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
////        when(el2.getXpath()).thenReturn("parent");
////        doReturn("BODY").when(el2).getTagName();
////        doReturn(100).when(rlg).getWidthOfElement(el2);
//       // AGNode el2 = spy(new AGNode(el2));
////
////        double[] eq = new double[]{1.0,1.0,-20};
////
////        HashMap<String, Element> vmap = new HashMap<>();
////        vmap.put(el1.getXpath(), el1);
////        vmap.put(el2.getXpath(), el2);
////        LayoutFactory agf = spy(new LayoutFactory(el1));
////        doReturn(el1).when(mockDoms).get(anyInt());
////        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(anyInt());
////        PowerMockito.doReturn(vmap).when(agf).getElementMap();
////
////        try {
////            rlg.findWidthBreakpoint(eq,700, 800, "node", "parent");
////            verify(rlg, times(1)).findWidthBreakpoint(eq, 700, 750, "node", "parent");
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
////    }
////
////    @Test
////    public void testFindWidthBreakpointRecursiveBranch2ReturnMin() throws InterruptedException {
////        PowerMockito.mockStatic(Redecheck.class);
////        rlg.url = "randomsite";
////        rlg.alreadyGathered = spy(new HashSet<Integer>());
////        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
////        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
////        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
////
////        // Mock Dom and AG
////        Element el1 = mock(Element.class);
////        when(el1.getBoundingCoords()).thenReturn(new int[] {10,0,90,100});
////        when(el1.getXpath()).thenReturn("node");
////        when(el1.getTagName()).thenReturn("DIV");
//////        doReturn(80).when(rlg).getWidthOfElement(el1);
////
////        Element el2 = mock(Element.class);
////        when(el2.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
////        when(el2.getXpath()).thenReturn("parent");
////        when(el2.getTagName()).thenReturn("BODY");
//////        doReturn(100).when(rlg).getWidthOfElement(el2);
////
////        Element el3 = mock(Element.class);
////        when(el3.getBoundingCoords()).thenReturn(new int[] {25,0,75,100});
////        when(el3.getXpath()).thenReturn("node");
////        when(el3.getTagName()).thenReturn("DIV");
//////        doReturn(50).when(rlg).getWidthOfElement(el3);
////
////        Element el4 = mock(Element.class);
////        when(el4.getBoundingCoords()).thenReturn(new int[] {0,0,90,100});
////        when(el4.getXpath()).thenReturn("parent");
////        when(el4.getTagName()).thenReturn("BODY");
//////        doReturn(100).when(rlg).getWidthOfElement(el4);
////
////        double[] eq = new double[] {1.0,1.0,-20};
////
////        HashMap<String, Element> vmap = new HashMap<>();
////        HashMap<String, Element> vmap2 = new HashMap<>();
////        vmap.put(el1.getXpath(), el1);
////        vmap.put(el2.getXpath(), el2);
////        vmap2.put(el3.getXpath(), el3);
////        vmap2.put(el4.getXpath(), el4);
////        LayoutFactory agf = spy(new LayoutFactory(el1));
////        LayoutFactory agf2 = spy(new LayoutFactory(el3));
////
////        doReturn(el1).when(mockDoms).get(767);
////        doReturn(el3).when(mockDoms).get(768);
////        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(767);
////        PowerMockito.doReturn(vmap).when(agf).getElementMap();
////        PowerMockito.doReturn(agf2).when(rlg).getLayoutFactory(768);
////        PowerMockito.doReturn(vmap2).when(agf2).getElementMap();
////
////        int expected = rlg.findWidthBreakpoint(eq, 767, 768, "node", "parent");
////        assertEquals(767, expected);
////    }
////
////    @Test
////    public void testFindWidthBreakpointRecursiveBranch2ReturnMax() throws InterruptedException {
////        PowerMockito.mockStatic(Redecheck.class);
////        rlg.url = "randomsite";
////        rlg.alreadyGathered = spy(new HashSet<Integer>());
////        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
////        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
////        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
////
////        // Mock Dom and AG
////        Element el1 = mock(Element.class);
////        when(el1.getBoundingCoords()).thenReturn(new int[] {10,0,90,100});
////        when(el1.getXpath()).thenReturn("node");
////        when(el1.getTagName()).thenReturn("BODY");
//////        doReturn(80).when(el1).getWidth();
//       AGNode el1 = spy(new AGNode(el1));
////
////        Element el2 = mock(Element.class);
////        when(el2.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
////        when(el2.getXpath()).thenReturn("parent");
////        when(el2.getTagName()).thenReturn("BODY");
//////        doReturn(100).when(el2).getWidth();
//       AGNode el2 = spy(new AGNode(el2));
////
////        Element el3 = mock(Element.class);
////        when(el3.getBoundingCoords()).thenReturn(new int[] {10,0,90,100});
////        when(el3.getXpath()).thenReturn("node");
////        when(el3.getTagName()).thenReturn("BODY");
//////        doReturn(80).when(el3).getWidth();
//       AGNode agn3 = spy(new AGNode(el3));
////
////        Element el4 = mock(Element.class);
////        when(el4.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
////        when(el4.getXpath()).thenReturn("parent");
////        when(el4.getTagName()).thenReturn("BODY");
//////        doReturn(100).when(el4).getWidth();
//       AGNode agn4 = spy(new AGNode(el4));
////
////        double[] eq = new double[]{1.0,1.0,-20};
////
////        HashMap<String, Element> vmap = new HashMap<>();
////        HashMap<String, Element> vmap2 = new HashMap<>();
////        vmap.put(el1.getXpath(), el1);
////        vmap.put(el2.getXpath(), el2);
////        vmap2.put(el3.getXpath(), el3);
////        vmap2.put(el4.getXpath(), el4);
////        LayoutFactory agf = spy(new LayoutFactory(el1));
////        LayoutFactory agf2 = spy(new LayoutFactory(el3));
////
////        doReturn(el1).when(mockDoms).get(767);
////        doReturn(el3).when(mockDoms).get(768);
////        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(767);
////        PowerMockito.doReturn(vmap).when(agf).getElementMap();
////        PowerMockito.doReturn(agf2).when(rlg).getLayoutFactory(768);
////        PowerMockito.doReturn(vmap2).when(agf2).getElementMap();
////
////        int expected = rlg.findWidthBreakpoint(eq, 767, 768, "node", "parent");
////        assertEquals(768, expected);
////    }
////
////    @Test
////    public void testFindWidthBreakpointRecursiveBranch2ReturnMinMinusOne() throws InterruptedException {
////        PowerMockito.mockStatic(Redecheck.class);
////        rlg.url = "randomsite";
////        rlg.alreadyGathered = spy(new HashSet<Integer>());
////        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
////        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
////        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
////
////        // Mock Dom and AG
////        Element el1 = mock(Element.class);
////        when(el1.getBoundingCoords()).thenReturn(new int[] {25,0,75,100});
////        when(el1.getXpath()).thenReturn("node");
////        when(el1.getTagName()).thenReturn("BODY");
//////        doReturn(50).when(el1).getWidth();
//       AGNode el1 = spy(new AGNode(el1));
////
////        Element el2 = mock(Element.class);
////        when(el2.getBoundingCoords()).thenReturn(new int[] {25,0,75,100});
////        when(el2.getXpath()).thenReturn("parent");
////        when(el2.getTagName()).thenReturn("BODY");
//////        doReturn(100).when(el2).getWidth();
//       AGNode el2 = spy(new AGNode(el2));
////
////        Element el3 = mock(Element.class);
////        when(el3.getBoundingCoords()).thenReturn(new int[] {25,0,75,100});
////        when(el3.getXpath()).thenReturn("node");
////        when(el3.getTagName()).thenReturn("BODY");
//////        doReturn(50).when(el3).getWidth();
//       AGNode agn3 = spy(new AGNode(el3));
////
////        Element el4 = mock(Element.class);
////        when(el4.getBoundingCoords()).thenReturn(new int[] {25,0,75,100});
////        when(el4.getXpath()).thenReturn("parent");
////        when(el4.getTagName()).thenReturn("BODY");
//////        doReturn(100).when(el4).getWidth();
//       AGNode agn4 = spy(new AGNode(el4));
////
////        double[] eq = new double[]{1.0,1.0,-20};
////
////        HashMap<String, Element> vmap = new HashMap<>();
////        HashMap<String, Element> vmap2 = new HashMap<>();
////        vmap.put(el1.getXpath(), el1);
////        vmap.put(el2.getXpath(), el2);
////        vmap2.put(el3.getXpath(), el3);
////        vmap2.put(el4.getXpath(), el4);
////
////        LayoutFactory agf = spy(new LayoutFactory(el1));
////        LayoutFactory agf2 = spy(new LayoutFactory(el3));
////
////        doReturn(el1).when(mockDoms).get(767);
////        doReturn(el3).when(mockDoms).get(768);
////        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(767);
////        PowerMockito.doReturn(vmap).when(agf).getElementMap();
////        PowerMockito.doReturn(agf2).when(rlg).getLayoutFactory(768);
////        PowerMockito.doReturn(vmap2).when(agf2).getElementMap();
////
////        int expected = rlg.findWidthBreakpoint(eq, 767, 768, "node", "parent");
////        assertEquals(766, expected);
////    }
////
////    @Test
////    public void testWidthBreakpointReturnMid() {
////        PowerMockito.mockStatic(Redecheck.class);
////        rlg.url = "randomsite";
////        rlg.alreadyGathered = spy(new HashSet<Integer>());
////        doReturn(true).when(rlg.alreadyGathered).ParentChild(anyInt());
////        Map<Integer, Element> mockDoms = spy(new HashMap<Integer, Element>());
////        when(Redecheck.loadDoms((int[]) anyObject(), anyString())).thenReturn(mockDoms);
////
////        // Mock Dom and AG
////        Element el1 = mock(Element.class);
////        when(el1.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
////        when(el1.getXpath()).thenReturn("node");
////        doReturn("BODY").when(el1).getTagName();
////
////        Element el2 = mock(Element.class);
////        when(el2.getBoundingCoords()).thenReturn(new int[] {0,0,100,100});
////        when(el2.getXpath()).thenReturn("parent");
////        doReturn("BODY").when(el2).getTagName();
////
////        double[] eq = new double[]{1.0,1.0,-20};
////
////        HashMap<String, Element> vmap = new HashMap<>();
////        vmap.put(el1.getXpath(), el1);
////
////        LayoutFactory agf = spy(new LayoutFactory(el1));
////        doReturn(el1).when(mockDoms).get(anyInt());
////        PowerMockito.doReturn(agf).when(rlg).getLayoutFactory(750);
////        PowerMockito.doReturn(vmap).when(agf).getElementMap();
////
////        try {
////            int result = rlg.findWidthBreakpoint(eq, 700, 800, "node", "parent");
//////            verify(rlg, times(1)).findWidthBreakpoint(eq, 700, 750, "node", "parent");
////            assertEquals(750, result);
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
////    }
//

}