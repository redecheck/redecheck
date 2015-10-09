package edu.gatech.xpert.dom.layout;

import edu.gatech.xpert.dom.DomNode;
import edu.gatech.xpert.dom.layout.Contains;
import edu.gatech.xpert.dom.layout.Sibling;
import edu.gatech.xpert.dom.layout.AGEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thomaswalsh on 02/10/2015.
 */
public class AlignmentGraphFactory {
    public DomNode dn;
    AlignmentGraph ag;
    public Map<String, AGNode> nodeMap;
    public HashMap<String, DomNode> domNodeMap;
    public HashMap<String, AGEdge> edgeMap;

    public AlignmentGraphFactory(DomNode dn) {
        this.dn = dn;
        updateBodyTag();
        this.ag = new AlignmentGraph(this.dn);
        this.nodeMap = ag.vMap;
        this.edgeMap = generateEdgeMap();
        this.domNodeMap = generateDomNodeMap();

    }

    private HashMap<String, DomNode> generateDomNodeMap() {
        HashMap<String, DomNode> map = new HashMap<>();
        for (String s : nodeMap.keySet()) {
            DomNode dn = nodeMap.get(s).domNode;
            map.put(s, dn);
        }
        return map;
    }

    private HashMap<String, AGEdge> generateEdgeMap() {
        HashMap<String, AGEdge> edgeMap = new HashMap<>();

        int counter = 0;
        for (Contains c : ag.contains) {
            edgeMap.put(c.getNode1().getxPath()+c.getNode2().getxPath()+"contains"+generateEdgeLabelling(c),c);
        }

        for (Sibling s : ag.siblings) {
            edgeMap.put(s.getNode1().getxPath()+s.getNode2().getxPath()+"sibling"+generateEdgeLabelling(s),s);
        }
        return edgeMap;
    }

    private void updateBodyTag() {
        ArrayList<DomNode> toCheck = new ArrayList<>();
        toCheck.add(dn);
        DomNode bodyNode = null;
        int maxY = 0;
        while (!toCheck.isEmpty()) {
            DomNode n = toCheck.remove(0);
            String tagName = n.getTagName();
            if (tagName != null) {
                if (tagName.equals("BODY")) {
                    bodyNode = n;
                }
            }
            try {
                int yVal = n.getCoords()[3];
                if (yVal > maxY) {
                    maxY = yVal;
                }
            } catch (NullPointerException e) {

            }
            toCheck.addAll(n.getChildren());
        }

        try {
            bodyNode.getCoords()[1] = 0;
            bodyNode.getCoords()[3] = maxY;
        } catch (Exception e) {

        }
    }

    public static String generateEdgeLabelling(AGEdge e) {
        String result = "";
        if (e instanceof Sibling) {
            Sibling s = (Sibling) e;
            if (s.isTopBottom()) {
                result = result + "below";
            }
            if (s.isBottomTop()) {
                result = result + "above";
            }
            if (s.isRightLeft()) {
                result = result + "leftOf";
            }
            if (s.isLeftRight()) {
                result = result + "rightOf";
            }
            if (s.isTopEdgeAligned()) {
                result = result + "topAlign";
            }
            if (s.isBottomEdgeAligned()) {
                result = result + "bottomAlign";
            }
            if (s.isLeftEdgeAligned()) {
                result = result + "leftAlign";
            }
            if (s.isRightEdgeAligned()) {
                result = result + "rightAlign";
            }
        } else {
            Contains c = (Contains) e;
            if (c.isCentered())
                result+="centered";
            if (c.isLeftJustified())
                result += "leftJust";
            if (c.isRightJustified())
                result += "rightJust";
            if (c.isMiddle())
                result+= "middle";
            if (c.isTopAligned())
                result+= "top";
            if (c.isBottomAligned())
                result+= "bottom";
        }
        return result;
    }

    public static String generateFlippedLabelling(Sibling s) {
        String result = "";
            if (s.isTopBottom()) {
                result = result + "above";
            }
            if (s.isBottomTop()) {
                result = result + "below";
            }
            if (s.isRightLeft()) {
                result = result + "rightOf";
            }
            if (s.isLeftRight()) {
                result = result + "leftOf";
            }
            if (s.isTopEdgeAligned()) {
                result = result + "topAlign";
            }
            if (s.isBottomEdgeAligned()) {
                result = result + "bottomAlign";
            }
            if (s.isLeftEdgeAligned()) {
                result = result + "leftAlign";
            }
            if (s.isRightEdgeAligned()) {
                result = result + "rightAlign";
            }
        return result;
    }

    public static boolean isAlignmentTheSame(AGEdge a, AGEdge b) {
        if (a instanceof Contains) {
            Contains c1 = (Contains) a;
            Contains c2 = (Contains) b;
            if (c1.isTopAligned() != c2.isTopAligned()) {
                return false;
            } else if (c1.isBottomAligned() != c2.isBottomAligned()) {
                return false;
            } else if (c1.isMiddle() != c2.isMiddle()) {
                return false;
            } else if (c1.isRightJustified() != c2.isRightJustified()) {
                return false;
            } else if (c1.isLeftJustified() != c2.isLeftJustified()) {
                return false;
            } else if (c1.isCentered() != c2.isCentered()) {
                return false;
            } else {
                return true;
            }
        } else {
            Sibling s1 = (Sibling) a;
            Sibling s2 = (Sibling) b;
            if (a.getNode1().getxPath().equals(b.getNode1().getxPath())) {
                if (s1.isBottomTop() != s2.isBottomTop()) {
                    return false;
                } else if (s1.isTopBottom() != s2.isTopBottom()) {
                    return false;
                } else if (s1.isLeftRight() != s2.isLeftRight()) {
                    return false;
                } else if (s1.isRightLeft() != s2.isRightLeft()) {
                    return false;
                } else if (s1.isLeftEdgeAligned() != s2.isLeftEdgeAligned()) {
                    return false;
                } else if (s1.isRightEdgeAligned() != s2.isRightEdgeAligned()) {
                    return false;
                } else if (s1.isTopEdgeAligned() != s2.isTopEdgeAligned()) {
                    return false;
                } else if (s1.isBottomEdgeAligned() != s2.isBottomEdgeAligned()) {
                    return false;
                } else {
                    return true;
                }
            } else {
                if (s1.isBottomTop() != s2.isTopBottom()) {
                    return false;
                } else if (s1.isTopBottom() != s2.isBottomTop()) {
                    return false;
                } else if (s1.isLeftRight() != s2.isRightLeft()) {
                    return false;
                } else if (s1.isRightLeft() != s2.isLeftRight()) {
                    return false;
                } else if (s1.isLeftEdgeAligned() != s2.isLeftEdgeAligned()) {
                    return false;
                } else if (s1.isRightEdgeAligned() != s2.isRightEdgeAligned()) {
                    return false;
                } else if (s1.isTopEdgeAligned() != s2.isTopEdgeAligned()) {
                    return false;
                } else if (s1.isBottomEdgeAligned() != s2.isBottomEdgeAligned()) {
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    public AlignmentGraph getAg() {
        return this.ag;
    }

    public Map<String, AGNode> getNodeMap() {
        return nodeMap;
    }

    public HashMap<String,AGEdge> getEdgeMap() {
        return edgeMap;
    }

    public HashMap<String, DomNode> getDomNodeMap() {
        return domNodeMap;
    }

    public static String generateKey(AGEdge e) {

        if (e instanceof Contains)
            return e.getNode1().getxPath()+e.getNode2().getxPath()+"contains" + generateEdgeLabelling(e);
        else
            return e.getNode1().getxPath()+e.getNode2().getxPath()+"sibling" + generateEdgeLabelling(e);
    }
}
