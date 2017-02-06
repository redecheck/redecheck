package edu.gatech.xpert.dom.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import edu.gatech.xpert.dom.DomNode;

public class AlignmentGraph {

	ArrayList<AGNode> vertices;
	Map<String, AGNode> vMap;
	List<Contains> contains;
	List<Sibling> siblings;

	public static final String[] tagsIgnore = { "A", "AREA", "B", "BLOCKQUOTE",
			"BR", "CANVAS", "CENTER", "CSACTIONDICT", "CSSCRIPTDICT", "CUFON",
			"CUFONTEXT", "DD", "EM", "EMBED", "FIELDSET", "FONT", "FORM",
			"HEAD", "HR", "I", "LABEL", "LEGEND", "LINK", "MAP", "MENUMACHINE",
			"META", "NOFRAMES", "NOSCRIPT", "OBJECT", "OPTGROUP", "OPTION",
			"PARAM", "S", "SCRIPT", "SMALL", "SPAN", "STRIKE", "STRONG",
			"STYLE", "TBODY", "TITLE", "TR", "TT", "U", "UL" };

	public static final String[] tagsContainer = { "DD", "DIV", "DT", "P",
			"TD", "TR" };

	public AlignmentGraph(DomNode root) {
		vertices = new ArrayList<>();
		vMap = new HashMap<>();
		contains = new ArrayList<>();
		siblings = new ArrayList<>();
		init(root);
	}

	public void init(DomNode root) {
		List<DomNode> worklist = new ArrayList<>();
		worklist.add(root);

		// Populate Nodes
		while (!worklist.isEmpty()) {
			DomNode node = worklist.remove(0);
			if (isLayoutNode(node)) {
				AGNode n = new AGNode(node);
				vertices.add(n);
				vMap.put(node.getxPath(), n);
			}
			worklist.addAll(node.getChildren());
		}
		
		// Sort the vertices based on Area and DOM hierarchy
		Collections.sort(vertices, new AGNodeComparator());
		ArrayList<AGNode> v = (ArrayList<AGNode>) vertices.clone();
		
		// Populate parent edges
		Map<AGNode, ArrayList<AGNode>> cMap = new HashMap<>();
		
		while(v.size() > 0) {
			AGNode node = v.remove(0);
			AGNode parent = null;
			for(AGNode n : v) {
				if(n.contains(node)) {
					if(parent != null && (parent.getArea() <= n.getArea())) {
						continue;
					}
					parent = n;
				}
			}
			if(parent != null) {
				contains.add(new Contains(parent, node));
				addToMap(cMap, parent, node);
			}
		}
		
		// Populate sibling edges
		for(ArrayList<AGNode> sib : cMap.values()) {
			List<AGNode> s = (ArrayList<AGNode>) sib.clone();
			while(s.size() > 0) {
				AGNode node = s.remove(0);
				for(AGNode n : s) {
					siblings.add(new Sibling(node,  n));
				}
			}
//			for(int i=0; i<sib.size()-1; i++) {
//				for( int j=i+1; j< sib.size(); j++) {
//					siblings.add(new Sibling(sib.get(i), sib.get(j)));
//				}
//			}
		}
	}

	private void addToMap(Map<AGNode, ArrayList<AGNode>> cMap, AGNode parent,
			AGNode a) {
		if(!cMap.containsKey(parent)) {
			cMap.put(parent, new ArrayList<AGNode>());
		}
		cMap.get(parent).add(a);
	}

	private boolean isLayoutNode(DomNode node) {
		// is it ignored?
		if (isIgnored(node)) {
			return false;
		}

		// check size
		int[] c = node.getCoords();
		if (c == null) {
			return false;
		}
		if (c[0] < 0 || c[1] < 0 || c[2] <= 0 || c[3] <= 0) {
			return false;
		}
		int t = 5; // 5 (Negligible dimensions)
		if (c[2] - c[0] <= t || c[3] - c[1] <= t) {
			return false;
		}

		// empty container
		if (ArrayUtils.contains(tagsContainer, node.getTagName())) {
			if (node.getChildren().size() == 0) {
				return false;
			}
			boolean hasVisibleChild = false;
			for (DomNode child : node.getChildren()) {
				if (child.getHash() != Long.MIN_VALUE
						|| (child.isText() && !StringUtils.isBlank(child
								.getText())) || (!isIgnored(child))) {
					hasVisibleChild = true;
				}
			}
			if (!hasVisibleChild)
				return false;
		}

		return true;
	}

	private boolean isIgnored(DomNode node) {
		if (ArrayUtils.contains(tagsIgnore, node)) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer("Vertices:"+vertices.size());
		
//		for(AGNode v : vertices){
//			out.append("\n\t");
//			out.append(v.domNode.getxPath());
//		}
		
		out.append("\nEdges:(c="+contains.size()+", s="+siblings.size()+")}");
		for(Contains c:contains) {
			out.append("\n\tC:");
			out.append(c.parent);
			out.append("-->");
			out.append(c.child);
		}
		for(Sibling s:siblings) {
			out.append("\n\tS:");
			out.append(s);
			out.append(" ");
			out.append(s.getAttributes());
		}
		
		return out.toString();
	}

}
