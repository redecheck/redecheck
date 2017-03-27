package shef.analysis;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.google.common.collect.HashBasedTable;
import com.rits.cloning.Cloner;

import shef.layout.LayoutFactory;
import shef.reporting.inconsistencies.CollisionFailure;
import shef.reporting.inconsistencies.ElementProtrusionFailure;
import shef.reporting.inconsistencies.ViewportProtrusionFailure;
import shef.rlg.AlignmentConstraint;
import shef.rlg.Node;
import shef.rlg.ResponsiveLayoutGraph;
import shef.rlg.Type;
import shef.rlg.VisibilityConstraint;

public class RLGAnalyserTest {
	ResponsiveLayoutGraph rlg;
	RLGAnalyser analyser;
	HashMap<String, Node> nodeMap;
	Node n1, n2, body;
	
	@Before
    public void setup() {
        rlg = mock(ResponsiveLayoutGraph.class);
        analyser = new RLGAnalyser(rlg, null, "", new ArrayList<>(), new HashMap<>(), 320, 1400);
        nodeMap = new HashMap<>();
        VisibilityConstraint vc = new VisibilityConstraint(320, 1400);
		body = new Node("BODY");
		body.addVisibilityConstraint(vc);
		n1 = new Node("first");
		n1.addVisibilityConstraint(vc);
        n2 = new Node("second");
        n2.addVisibilityConstraint(vc);
        nodeMap.put(body.getXpath(), body);
        nodeMap.put(n1.getXpath(), n1);
        nodeMap.put(n2.getXpath(), n2);
	}
	
	@Test
	public void testViewportProtrusionNoIssue() {
		Node body = new Node("/HTML/BODY");
		Node n1 = new Node("first");
        Node n2 = new Node("second");
        nodeMap.put(body.getXpath(), body);
        nodeMap.put(n1.getXpath(), n1);
        nodeMap.put(n2.getXpath(), n2);
        
        AlignmentConstraint bodyN1 = new AlignmentConstraint(body, n1, Type.PARENT_CHILD, 320, 1400, new boolean[] {true, false, false, false, true, false}, null);
        AlignmentConstraint n1cN2 = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 320, 1400, new boolean[] {true, false, false, false, true, false}, null);
        n1.addParentConstraint(bodyN1);
        n2.addParentConstraint(n1cN2);
        HashBasedTable<String, int[], AlignmentConstraint> acs = HashBasedTable.create();
        acs.put(bodyN1.generateKey(), new int[]{bodyN1.getMin(), bodyN1.getMax()}, bodyN1);
        acs.put(n1cN2.generateKey(), new int[]{n1cN2.getMin(), n1cN2.getMax()},  n1cN2);      
        rlg.setAlignmentConstraints(acs);
        
        analyser.checkForViewportOverflows(nodeMap);
        assertEquals("No failure should have been reported", 0, analyser.errors.size());
	}
	
	@Test
	public void testViewportProtrusionWithIssue() {
		VisibilityConstraint vc = new VisibilityConstraint(320, 1400);
		Node body = new Node("BODY");
		body.addVisibilityConstraint(vc);
		Node n1 = new Node("first");
		n1.addVisibilityConstraint(vc);
        Node n2 = new Node("second");
        n2.addVisibilityConstraint(vc);
        nodeMap.put(body.getXpath(), body);
        nodeMap.put(n1.getXpath(), n1);
        nodeMap.put(n2.getXpath(), n2);
        
        AlignmentConstraint bodyN1 = new AlignmentConstraint(body, n1, Type.PARENT_CHILD, 320, 1400, new boolean[] {true, false, false, false, true, false}, null);
        AlignmentConstraint n1cN2 = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 400, 1400, new boolean[] {true, false, false, false, true, false}, null);
        n1.addParentConstraint(bodyN1);
        n2.addParentConstraint(n1cN2);
        HashBasedTable<String, int[], AlignmentConstraint> acs = HashBasedTable.create();
        acs.put(bodyN1.generateKey(), new int[]{bodyN1.getMin(), bodyN1.getMax()}, bodyN1);
        acs.put(n1cN2.generateKey(), new int[]{n1cN2.getMin(), n1cN2.getMax()},  n1cN2);      
        rlg.setAlignmentConstraints(acs);
        
        analyser.checkForViewportOverflows(nodeMap);
        assertEquals("One failure should have been reported", 1, analyser.errors.size());
        ViewportProtrusionFailure vpe = (ViewportProtrusionFailure) analyser.errors.get(0);
        assertEquals("Failure min bound should be 320", 320, vpe.getBounds()[0]);
        assertEquals("Failure max bound should be 399", 399, vpe.getBounds()[1]);
	}
	
	@Test
	public void testViewportProtrusionWithTwoIssues() {
		VisibilityConstraint vc = new VisibilityConstraint(320, 1400);
		Node body = new Node("BODY");
		body.addVisibilityConstraint(vc);
		Node n1 = new Node("first");
		n1.addVisibilityConstraint(vc);
        Node n2 = new Node("second");
        n2.addVisibilityConstraint(vc);
        nodeMap.put(body.getXpath(), body);
        nodeMap.put(n1.getXpath(), n1);
        nodeMap.put(n2.getXpath(), n2);
        
        AlignmentConstraint bodyN1 = new AlignmentConstraint(body, n1, Type.PARENT_CHILD, 320, 1400, new boolean[] {true, false, false, false, true, false}, null);
        AlignmentConstraint ac1 = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 400, 700, new boolean[] {true, false, false, false, true, false}, null);
        AlignmentConstraint ac2 = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 800, 1400, new boolean[] {true, false, false, false, true, false}, null);
        n1.addParentConstraint(bodyN1);
        n2.addParentConstraint(ac1);
        n2.addParentConstraint(ac2);
        HashBasedTable<String, int[], AlignmentConstraint> acs = HashBasedTable.create();
        acs.put(bodyN1.generateKey(), new int[]{bodyN1.getMin(), bodyN1.getMax()}, bodyN1);
        acs.put(ac1.generateKey(), new int[]{ac1.getMin(), ac1.getMax()},  ac1);    
        acs.put(ac2.generateKey(), new int[]{ac2.getMin(), ac2.getMax()},  ac2);
        rlg.setAlignmentConstraints(acs);
        
        analyser.checkForViewportOverflows(nodeMap);
        assertEquals("Two failures should have been reported", 2, analyser.errors.size());
	}
	
	@Test
	public void testViewportProtrusionAtVMax() {
		VisibilityConstraint vc = new VisibilityConstraint(320, 1400);
		Node body = new Node("BODY");
		body.addVisibilityConstraint(vc);
		Node n1 = new Node("first");
		n1.addVisibilityConstraint(vc);
        Node n2 = new Node("second");
        n2.addVisibilityConstraint(vc);
        nodeMap.put(body.getXpath(), body);
        nodeMap.put(n1.getXpath(), n1);
        nodeMap.put(n2.getXpath(), n2);
        
        AlignmentConstraint bodyN1 = new AlignmentConstraint(body, n1, Type.PARENT_CHILD, 320, 1400, new boolean[] {true, false, false, false, true, false}, null);
        AlignmentConstraint ac1 = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 320, 1380, new boolean[] {true, false, false, false, true, false}, null);
        n1.addParentConstraint(bodyN1);
        n2.addParentConstraint(ac1);
        HashBasedTable<String, int[], AlignmentConstraint> acs = HashBasedTable.create();
        acs.put(bodyN1.generateKey(), new int[]{bodyN1.getMin(), bodyN1.getMax()}, bodyN1);
        acs.put(ac1.generateKey(), new int[]{ac1.getMin(), ac1.getMax()},  ac1);
        rlg.setAlignmentConstraints(acs);
        
        analyser.checkForViewportOverflows(nodeMap);
        assertEquals("One failure should have been reported", 1, analyser.errors.size());
        ViewportProtrusionFailure vpe = (ViewportProtrusionFailure) analyser.errors.get(0);
        assertEquals("Failure min bound should be 1381", 1381, vpe.getBounds()[0]);
        assertEquals("Failure max bound should be 1400", 1400, vpe.getBounds()[1]);
	}
	
	@Test
	public void testOverlapWithIssue() {
		AlignmentConstraint overlapping = new AlignmentConstraint(n1, n2, Type.SIBLING, 400,700, new boolean[] {true, false, false, false, false, false, true, false, false, false, true}, null);
		AlignmentConstraint notOverlapping = new AlignmentConstraint(n1, n2, Type.SIBLING, 701,1400, new boolean[] {true, false, false, false, false, false, true, false, false, false, false}, null);
		HashBasedTable<String, int[], AlignmentConstraint> acs = HashBasedTable.create();
		acs.put(overlapping.generateKey(), new int[]{400, 700}, overlapping);
		acs.put(notOverlapping.generateKey(), new int[]{701, 1400}, notOverlapping);
		doReturn(acs).when(analyser.rlg).getAlignmentConstraints();
		analyser.rlg.setAlignmentConstraints(acs);
		analyser.detectOverflowOrOverlap(acs);
        assertEquals("One failure should have been reported", 1, analyser.errors.size());
        CollisionFailure ece = (CollisionFailure) analyser.errors.get(0);
        assertEquals("Failure min bound should be 400", 400, ece.getBounds()[0]);
        assertEquals("Failure max bound should be 700", 700, ece.getBounds()[1]);
	}
	
	@Test
	public void testOverlapWithFollowingOverlap() {
		AlignmentConstraint overlapping = new AlignmentConstraint(n1, n2, Type.SIBLING, 400,700, new boolean[] {true, false, false, false, false, false, true, false, false, false, true}, null);
		AlignmentConstraint notOverlapping = new AlignmentConstraint(n1, n2, Type.SIBLING, 701,1400, new boolean[] {true, false, false, false, false, false, true, false, false, false, true}, null);
		HashBasedTable<String, int[], AlignmentConstraint> acs = HashBasedTable.create();
		acs.put(overlapping.generateKey(), new int[]{400, 700}, overlapping);
		acs.put(notOverlapping.generateKey(), new int[]{701, 1400}, notOverlapping);
		doReturn(acs).when(analyser.rlg).getAlignmentConstraints();
		analyser.rlg.setAlignmentConstraints(acs);
		analyser.detectOverflowOrOverlap(acs);
        assertEquals("No failure should have been reported", 0, analyser.errors.size());
	}
	
	@Test
	public void testOverflowWithIssue() {
		AlignmentConstraint bodyN1 = new AlignmentConstraint(body, n1, Type.PARENT_CHILD, 400,1400, new boolean[] {true, false, false, false, false, false}, null);
		AlignmentConstraint bodyN2 = new AlignmentConstraint(body, n2, Type.PARENT_CHILD, 400,700, new boolean[] {true, false, false, false, false, false}, null);
		AlignmentConstraint overlapping = new AlignmentConstraint(n1, n2, Type.SIBLING, 400,700, new boolean[] {true, false, false, false, false, false, true, false, false, false, true}, null);
		AlignmentConstraint notOverlapping = new AlignmentConstraint(n1, n2, Type.PARENT_CHILD, 701,1400, new boolean[] {true, false, false, false, false, false}, null);
		n1.addParentConstraint(bodyN1);
		n2.addParentConstraint(bodyN2);
		n2.addParentConstraint(notOverlapping);
		HashBasedTable<String, int[], AlignmentConstraint> acs = HashBasedTable.create();
		acs.put(bodyN1.generateKey(), new int[]{400, 1400}, bodyN1);
		acs.put(bodyN2.generateKey(), new int[]{400, 700}, bodyN2);
		acs.put(overlapping.generateKey(), new int[]{400, 700}, overlapping);
		acs.put(notOverlapping.generateKey(), new int[]{701, 1400}, notOverlapping);
		doReturn(acs).when(analyser.rlg).getAlignmentConstraints();
		analyser.rlg.setAlignmentConstraints(acs);
		analyser.detectOverflowOrOverlap(acs);
        assertEquals("One failure should have been reported", 1, analyser.errors.size());
        ElementProtrusionFailure epe = (ElementProtrusionFailure) analyser.errors.get(0);
        assertEquals("Failure min bound should be 400", 400, epe.getBounds()[0]);
        assertEquals("Failure max bound should be 700", 700, epe.getBounds()[1]);
	}
	
	@Test
	public void testSetOfNodesToStringEmpty() {
		ArrayList nodes = new ArrayList();
		String result = analyser.setOfNodesToString(nodes);
		assertEquals("EMPTY", result);
	}
	
	@Test
	public void testSetOfNodesToStringNonEmpty() {
		ArrayList nodes = new ArrayList();
		nodes.add(body);
		nodes.add(n1);
		nodes.add(n2);
		String result = analyser.setOfNodesToString(nodes);
		assertEquals("BODY first second ", result);
	}
}
