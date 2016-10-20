package twalsh.layout;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by thomaswalsh on 16/05/2016.
 */
public class LayoutFactory {
    String dom;
    public Layout layout;
    private RTree rtree;
    HashMap<Integer, Rectangle> rectangles;
    HashMap<Integer, String> xpaths;
    HashMap<String, Element> elements;
    int bodyID;
    ArrayList<String> noSizeElements = new ArrayList<>();
    ArrayList<String> nonVisibleElements = new ArrayList<>();

    public static final String[] tagsIgnore = { "I", "G", "PATH", "AREA", "B", "BLOCKQUOTE",
            "BR", "CANVAS", "CENTER", "CSACTIONDICT", "CSSCRIPTDICT", "CUFON",
            "CUFONTEXT", "DD", "EM", "EMBED", "FIELDSET", "FONT",
            "HEAD", "HR", "IFRAME", "INS", "LEGEND", "LINK", "MAP", "MENUMACHINE",
            "META", "NOFRAMES", "NOSCRIPT", "OBJECT", "OPTGROUP", "OPTION",
            "PARAM", "S", "SCRIPT", "SMALL", "SPAN", "STRIKE", "STRONG",
            "STYLE", "TBODY", "TITLE", "TR", "TT", "U" };

    public LayoutFactory(String dom) {
        this.dom = dom;
        buildRTree(dom);
        resizeBodyElement();
        layout = new Layout(rtree, rectangles, xpaths, elements);
    }
    
    public void buildRTree(String dom) {
        rtree = new RTree();
        rtree.init(null);
        rectangles = new HashMap<>();
        xpaths = new HashMap<>();
        elements = new HashMap<>();
        Rectangle r1 = null, r2 = null, r3 = null;
        try {
            JSONArray arrDom = new JSONArray(dom.trim());
            int numElements = 0;
            for (int i = 0; i < arrDom.length(); i++) {
                JSONObject nodeData = arrDom.getJSONObject(i);
                Element e = getElementFromDomData(nodeData);
                if (e != null) {
                    try {
                        int[] coords = e.getBoundingCoords();
                        if (coords != null && coords[2] >=0) {
                            Rectangle r = new Rectangle(coords[0], coords[1], coords[2], coords[3]);
                            int[] contentCoords = getCoords(nodeData, false);
                            HashMap<String, String> styles = getStyles(nodeData);
//                            System.out.println(contentCoords[0]);
                            e.setContentCoords(contentCoords);
                            e.setStyles(styles);
                            rectangles.put(numElements, r);
                            rtree.add(r, numElements);
                            xpaths.put(numElements, e.getXpath());
                            elements.put(e.getXpath(), e);
                            if (e.getXpath().equals("/HTML/BODY")) {
                                bodyID = numElements;
                            }
                            numElements++;
                        }
                    } catch (Exception ex ) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (JSONException ex2) {
            System.err.println("JSON Exception while layout : \n" + dom);
            ex2.printStackTrace();
        }


    }

    private void resizeBodyElement() {
        Element bodyNode = null;
        int maxY = 0;
        for (Element e : this.elements.values()) {

            try {
                String xp = e.getXpath();
                if (xp != null) {
                    if (xp.trim().equals("/HTML/BODY")) {
                        bodyNode = e;
                    }
                }

                int yVal = e.y2;
                if (yVal > maxY) {
                    maxY = yVal;
                }
            } catch (NullPointerException ex) {

            }

        }

        try {
            if (bodyNode != null) {
                bodyNode.setY1(0);
                bodyNode.setY2(maxY);
                Rectangle r = rectangles.get(bodyID);
                r.maxY = maxY;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Element getElementFromDomData(JSONObject obj) throws JSONException {
        int[] coords = getCoords(obj, true);
        if (!Arrays.equals(new int[]{0,0,0,0}, coords)) {
            try {
                String xpath = obj.getString("xpath");
                int width = coords[2] - coords[0];
                int height = coords[3] - coords[1];
                if (!(((width < 1) || (height < 1)) && (obj.getInt("overflow") == 0))) {
                    if (obj.getInt("visible") == 1) {
                        if (height >=5 && width >= 5) {
                            if (!ArrayUtils.contains(tagsIgnore, parseTagName(xpath).toUpperCase())) {

                                if (!childOfNoSizeElement(xpath)) {
                                    try {
                                        Element e = new Element(obj.getString("xpath"), coords[0], coords[1], coords[2], coords[3]);
                                        return e;
                                    } catch (JSONException e) {
                                        System.out.println("Error while layout the XPath of " + obj.toString());
                                        e.printStackTrace();
                                    }
                                } else {
//                                System.out.println(xpath + " was a child of a no-size or non-visible element");
                                }
                            }
                        } else {
//                            System.out.println(xpath + " was too small with size " + width + " x " + height);
                        }

                    } else {
//                        System.out.println(xpath + " was not visible");
                        nonVisibleElements.add(xpath);
                    }
                } else {
//                    System.out.println(obj);
                    nonVisibleElements.add(obj.getString("xpath"));
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    private boolean childOfNoSizeElement(String xpath) {
        for (String ns : nonVisibleElements) {
            if (xpath.contains(ns)) {
                return true;
            }
        }

        return false;
    }

    public static int[] getCoords(JSONObject ob, boolean b) {
        try {
            if (b) {
                JSONArray data = ob.getJSONArray("coord");
                Double height = data.getDouble(3) - data.getDouble(1);
                Double width = data.getDouble(2) - data.getDouble(0);
                int[] coords = {data.getInt(0), data.getInt(1), data.getInt(2), data.getInt(3)};
                return coords;
            } else {
                JSONArray data = ob.getJSONArray("contentCoords");
                int[] coords = {data.getInt(0), data.getInt(1), data.getInt(2), data.getInt(3)};
                return coords;
            }
        } catch (Exception e) {
            System.out.println("Error while layout coordinates");
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<String, String> getStyles(JSONObject object) throws JSONException {
        HashMap<String, String> styles = new HashMap<>();
        JSONObject attr = object.getJSONObject("styles");
        Iterator<String> it = attr.keys();
        while (it.hasNext()) {
            String key = it.next();
            String value = attr.getString(key);
            styles.put(key.toLowerCase(), value.toLowerCase());
        }
        return styles;
    }



    public HashMap<String, Element> getElementMap() {
        return layout.getElements();
    }

    public HashMap<String, Relationship> getRelationships() {
        return layout.getRelationships();
    }

    public static String generateEdgeLabelling(Relationship r) {
        String result = " {";
        if (r instanceof Sibling) {
            Sibling s = (Sibling) r;
            if (s.isAbove()) {
                result = result + "above";
            }
            if (s.isBelow()) {
                result = result + "below";
            }
            if (s.isRightOf()) {
                result = result + "rightOf";
            }
            if (s.isLeftOf()) {
                result = result + "leftOf";
            }
            if (s.isTopEdge()) {
                result = result + "topAlign";
            }
            if (s.isBottomEdge()) {
                result = result + "bottomAlign";
            }
            if (s.isyMid()) {
                result = result + "yMidAlign";
            }
            if (s.isLeftEdge()) {
                result = result + "leftAlign";
            }
            if (s.isRightEdge()) {
                result = result + "rightAlign";
            }
            if (s.isxMid()) {
                result = result + "xMidAlign";
            }
            if (s.isOverlapping()) {
                result = result + "overlapping";
            }
        } else {
            ParentChild pc = (ParentChild) r;
            if (pc.isCentreJust())
                result+="centered";
            if (pc.isLeftJust())
                result += "leftJust";
            if (pc.isRightJust())
                result += "rightJust";
            if (pc.isMiddleJust())
                result+= "middle";
            if (pc.isTopJust())
                result+= "top";
            if (pc.isBottomJust())
                result+= "bottom";
        }
        return result + "}";
    }

    public static String generateFlippedLabelling(Sibling s) {
        String result = " {";
        if (s.isAbove()) {
            result = result + "below";
        }
        if (s.isBelow()) {
            result = result + "above";
        }
        if (s.isRightOf()) {
            result = result + "leftOf";
        }
        if (s.isLeftOf()) {
            result = result + "rightOf";
        }
        if (s.isTopEdge()) {
            result = result + "topAlign";
        }
        if (s.isBottomEdge()) {
            result = result + "bottomAlign";
        }
        if (s.isyMid()) {
            result = result + "yMidAlign";
        }
        if (s.isLeftEdge()) {
            result = result + "leftAlign";
        }
        if (s.isRightEdge()) {
            result = result + "rightAlign";
        }
        if (s.isxMid()) {
            result = result + "xMidAlign";
        }
        if (s.isOverlapping()) {
            result = result + "overlapping";
        }
        return result + "}";
    }

    public static String generateKey(Relationship r) {

        if (r instanceof ParentChild)
            return r.getNode1().getXpath()+" contains "+r.getNode2().getXpath() + generateEdgeLabelling(r);
        else
            return r.getNode1().getXpath()+" sibling of "+r.getNode2().getXpath() + generateEdgeLabelling(r);
    }

    private String parseTagName(String xPath) {
        if(xPath == null) {
            return null;
        } else {
            String[] tags = xPath.split("/");
            if (tags.length > 0) {
                return tags[tags.length - 1].replaceAll("\\[[0-9]*\\]", "");
            }
            return null;
        }
    }
}
