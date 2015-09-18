package twalsh.rlg;

import org.junit.Before;
import org.junit.Test;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

/**
 * Created by thomaswalsh on 18/09/15.
 */
public class NodeTest {

    Node n;

    @Before
    public void setup() {
        n = new Node("testXPath");
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetXpath() throws Exception {

        assertEquals(n.getXpath(), "testXPath");
    }

    @Test
    public void testGetWidthConstraints() throws Exception {

    }

    @Test
    public void testGetVisibilityConstraints() throws Exception {

    }

    @Test
    public void testGetParentConstraints() throws Exception {

    }

    @Test
    public void testGenerateGraphVizLabel() throws Exception {

    }

    @Test
    public void testAddWidthConstraint() throws Exception {
        WidthConstraint wc = mock(WidthConstraint.class);
        n.addWidthConstraint(wc);
        assertEquals(n.getWidthConstraints().size(), 1);
    }

    @Test
    public void testAddVisibilityConstraint() throws Exception {
        VisibilityConstraint vc = mock(VisibilityConstraint.class);
        n.addVisibilityConstraint(vc);
        assertEquals(n.getVisibilityConstraints().size(),1);
    }

    @Test
    public void testAddParentConstraint() throws Exception {
        AlignmentConstraint ac = mock(AlignmentConstraint.class);
        n.addParentConstraint(ac);
        assertEquals(n.getParentConstraints().size(), 1);
    }

    @Test
    public void testToString() {
        VisibilityConstraint vc = new VisibilityConstraint(300,600);
        n.addVisibilityConstraint(vc);

        Node n2 = new Node("testParent");
        WidthConstraint wc = new WidthConstraint(300,800, 1, n2, -50);
        n.addWidthConstraint(wc);

        String expected = "\n\t300 -> 600testXPath\n\t300 --> 800 : 100.0% of testParent + -50.0";
        assertEquals(n.toString(), expected);
    }

    @Test
    public void testGraphVizLabel() {
        VisibilityConstraint vc = new VisibilityConstraint(300,600);
        n.addVisibilityConstraint(vc);

        Node n2 = new Node("testParent");
        WidthConstraint wc = new WidthConstraint(300,800, 1, n2, -50);
        n.addWidthConstraint(wc);

        String expected = "\n\t300 -> 600\ntestXPath\n300 -> 800 : 1.0% of parent + -50.0";
        assertEquals(n.generateGraphVizLabel(), expected);
    }
}