package twalsh.layout;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thomaswalsh on 13/05/2016.
 */
public class Element {
    String xpath;
    int x1, x2, y1, y2;
    Element parent;
    ArrayList<Element> children;
    HashMap<String, String> styles;
    Rectangle rectangle;

    public HashMap<String, String> getStyles() {
        return styles;
    }

    public String getXpath() {
        return xpath;
    }

    public int[] getCoordsArray() {
        return new int[] {x1, y1, x2, y2};
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public Element(String x, int x1, int y1, int x2, int y2) {
        this.xpath = x;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.rectangle = new Rectangle(x1, y1, x2-x1, y2 - y1);
    }

    public void setParent(Element p) {
        parent = p;
    }

    public Element getParent() {
        return parent;
    }

    public void addChild(Element c) {
        this.children.add(c);
    }

    public String toString() {
        return xpath + " [" + x1 + "," + y1 + "," + x2 + "," + y2 + "]";
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }
}
