package shef.layout;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;
import gnu.trove.TIntProcedure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomaswalsh on 16/05/2016.
 */
public class Layout {
    private SpatialIndex si;
    private RTree rtree;
    HashMap<Integer, Rectangle> rectangles;
    HashMap<Integer, String> xpaths;



    public HashMap<String, Element> getElements() {
        return elements;
    }

    HashMap<String, Element> elements;
    HashMap<String, Relationship> relationships;

    public Layout(RTree rtree, HashMap<Integer, Rectangle> rects, HashMap<Integer, String> xs, HashMap<String, Element> els) {
        this.rtree = rtree;
        this.rectangles = rects;
        this.xpaths = xs;
        elements = els;
        relationships = new HashMap<>();

        extract();
    }

    private void extract() {



        HashMap<Integer, Integer> parentMap = new HashMap<>();
        for (int c = 0; c < elements.size(); c++) {
            List<Integer> cIDs = getChildren(c);
            for (int i : cIDs) {
                // Check new parent is smaller than existing
                if (parentMap.containsKey(i)) {
                    int currentParent = parentMap.get(i);
                    List<Integer> tempIds = getChildren(currentParent);
                    if (tempIds.contains(c)) {
//                    if (rectangles.get(c).area() < rectangles.get(currentParent).area()) {
//                        if (xpaths.get(i).equals("/HTML/BODY/DIV[2]/DIV[2]/DIV[2]/FORM/DIV/INPUT")) {
//                            System.out.println("Changing parent map from " + xpaths.get(currentParent) + " to " + xpaths.get(i) + " and " + xpaths.get(c));
//                        }
                        parentMap.put(i, c);
                    } else {
//                        if (xpaths.get(i).equals("/HTML/BODY/DIV[2]/DIV[2]/DIV[2]/FORM/DIV/INPUT")) {
//                            System.out.println("Didn't change parent map with " + xpaths.get(i) + " and " + xpaths.get(c));
//                            System.out.println(xpaths.get(currentParent) + " " + rectangles.get(currentParent));
//                            System.out.println(xpaths.get(c) + " " + rectangles.get(c));
//                            System.out.println();
//                        }
                    }
                } else {
                    parentMap.put(i,c);
                }

            }
        }

//        for (int c : parentMap.keySet()) {
//            try {
//                if (parentMap.containsKey(parentMap.get(c))) {
//                    if (parentMap.get(parentMap.get(c)) == c) {
//                        System.out.println(c + " and " + parentMap.get(c));
//                        for (int x : parentMap.keySet()) {
//                            if (parentMap.get(x) == c) {
//                                System.out.println(xpaths.get(x) + " child of " + xpaths.get(parentMap.get(x)));
//                            }
//                        }
//                    }
//                }
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

//        System.out.println(rectangles.size() + " rectangles");
//        System.out.println(parentMap.size() + " in parent map");
//        for (Integer i : rectangles.keySet()) {
//            if (!parentMap.containsKey(i)) {
//                System.out.println(xpaths.get(i) + " " + rectangles.get(i));
//            }
//        }

        HashMap<Element, ArrayList<Element>> parents = new HashMap<>();

        for (Integer x : parentMap.keySet()) {
            Integer pId = parentMap.get(x);
            Element p = elements.get(xpaths.get(pId));
            Element c = elements.get(xpaths.get(x));
            if (p != null & c != null) {
//                if (p.getXpath().equals("/HTML/BODY/SECTION[5]/DIV/DIV/DIV")) {
//                    System.out.println("DIV contains " + c);
//                }
//                if (p.getXpath().equals("/HTML/BODY/DIV[2]")) {
//                    System.out.println("body/div[2] contains " + c);
//                }

                ParentChild pc = new ParentChild(p, c);
//                if (!flippedParent(pc)) {
                    c.setParent(p);
                    relationships.put(pc.getKey(), pc);

                    if (!parents.containsKey(p)) {
                        parents.put(p, new ArrayList<Element>());
                    }
                    parents.get(p).add(c);
//                }
            }
        }

        for(ArrayList<Element> sib : parents.values()) {
            List<Element> sibCopy = (ArrayList<Element>) sib.clone();
            while(sibCopy.size() > 0) {
                Element node = sibCopy.remove(0);
                for(Element n : sibCopy) {
                    Sibling sibling = new Sibling(node, n);
                    relationships.put(sibling.getKey(), sibling);
                }
            }
        }

//
    }

    private boolean flippedParent(ParentChild pc) {
        for (Relationship r : relationships.values()) {
            if (r instanceof ParentChild) {
                if (r.getNode1() == pc.getNode2() && r.getNode2() == pc.getNode1()) {
//                    System.out.println("FLIPPED" + pc);
                    return true;
                }
            }
        }
        return false;
    }

    /*
    Thanks to Sonal Mahajan (sonalmahajan), who wrote this method for her tool WebSee.
    I have modified it slightly for use in ReDeCheck.
    */
    public List<Integer> getChildren(final int elementId) {
        final ArrayList<Integer> children = new ArrayList<>();
        rtree.contains(rectangles.get(elementId), new TIntProcedure() {
            @Override
            public boolean execute(int i) {
                if (i != elementId) {
                    // Check for children same size as parents
                    if (!xpaths.get(elementId).contains(xpaths.get(i))) {
                        children.add(i);
                    }

                }
                return true;
            }
        });
        return children;
    }



    public HashMap<String,Relationship> getRelationships() {
        return relationships;
    }
}
