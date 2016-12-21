package shef.layout;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by thomaswalsh on 18/11/2016.
 */
public class ElementTest {
    Element e;
    Element p, q;
    @Before
    public void setUp() throws Exception {
        e = new Element("testXpath", 10, 10, 20, 20);
        p = new Element("parent", 0, 0, 30, 30);
        q = new Element("another", 50, 50, 100, 100);
    }

    @Test
    public void getContentCoords() throws Exception {

    }

    @Test
    public void getStyles() throws Exception {

    }

    @Test
    public void getXpath() throws Exception {

    }

    @Test
    public void getBoundingCoordinates() throws Exception {

    }

    @Test
    public void getRectangle() throws Exception {

    }

    @Test
    public void setParent() throws Exception {
        e.setParent(p);
        assertEquals(p, e.getParent());
        e.setParent(q);
        assertEquals(q, e.getParent());
    }

    @Test
    public void addChild() throws Exception {

    }

    @Test
    public void setY1() throws Exception {
        e.setY1(20);
        assertEquals(20, e.getY1());
    }

    @Test
    public void setY2() throws Exception {
        e.setY2(60);
        assertEquals(60, e.getY2());
    }

    @Test
    public void getBoundingCoords() throws Exception {
        int[] expected = new int[] {10, 10, 20, 20};
        int[] actual = e.getBoundingCoords();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void setContentCoords() throws Exception {

    }

    @Test
    public void getContentRectangle() throws Exception {

    }

    @Test
    public void setStyles() throws Exception {

    }

}