package xpert.ag;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import twalsh.rlg.WidthConstraint;
import xpert.dom.DomNode;

public class AlignmentGraph {

	ArrayList<AGNode> vertices;
	Map<String, AGNode> vMap;
	private List<Contains> contains;
	List<Sibling> siblings;
	HashMap<String, String> nodes;
	HashMap<Edge, String> edges;
    HashMap<String, Edge> newEdges;
	String outputFileName;
	

	public static final String[] tagsIgnore = { "A", "AREA", "B", "BLOCKQUOTE",
			"BR", "CANVAS", "CENTER", "CSACTIONDICT", "CSSCRIPTDICT", "CUFON",
			"CUFONTEXT", "DD", "EM", "EMBED", "FIELDSET", "FONT", "FORM",
			"HEAD", "HR", "I", "LABEL", "LEGEND", "LINK", "MAP", "MENUMACHINE",
			"META", "NOFRAMES", "NOSCRIPT", "OBJECT", "OPTGROUP", "OPTION",
			"PARAM", "S", "SCRIPT", "SMALL", "SPAN", "STRIKE", "STRONG",
			"STYLE", "TBODY", "TITLE", "TR", "TT", "U" };

	public static final String[] tagsContainer = { "DD", "DIV", "DT", "P",
			"TD", "TR" };

	public AlignmentGraph(DomNode root) {
		vertices = new ArrayList<>();
		vMap = new HashMap<>();
		setContains(new ArrayList<Contains>());
		siblings = new ArrayList<>();
		nodes = new HashMap<String, String>();
		edges = new HashMap<Edge, String>();
        newEdges = new HashMap<String, Edge>();
		init(root);
	}
	
	public void setOutputFileName(String o) {
		this.outputFileName = o;
	}

	public void init(DomNode root) {
		List<DomNode> worklist = new ArrayList<>();
		ArrayList<DomNode> mainlist = new ArrayList<DomNode>();
		
		worklist.add(root);
		DomNode tempBody = null;
		int maxY = 0;
		while (!worklist.isEmpty()) {
			DomNode n = worklist.remove(0);
			String tagName = n.getTagName();
			if (tagName != null) {
				if (tagName.equals("BODY")) {
						tempBody = n;
						mainlist.add(n);
				}
			}
			
			
			if (isLayoutNode(n)) {
				mainlist.add(n);
				
				int yVal = n.getCoords()[3];
				if (yVal > maxY) {
					maxY = yVal;
				}
			}
			worklist.addAll(n.getChildren());
		}
		
		
		// We assume the BODY tag contains EVERYTHING, so set its coords to 0,0 to bottom right corner
		tempBody.getCoords()[1] = 0;
		tempBody.getCoords()[3] = maxY;
		
		
		// Populate Nodes
		for (DomNode node : mainlist) {
			if (isLayoutNode(node)) {
				vertices.add(new AGNode(node));
				vMap.put(node.getxPath(), new AGNode(node));
			}
			
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
					if  ( (parent != null && (parent.getArea() <= n.getArea()))) {
						continue;
					}
					parent = n;
				}
			}
			if ((parent != null) && (!parent.domNode.getxPath().equals(node.domNode.getxPath()))) {
				getContains().add(new Contains(parent, node));
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

		traverseGraph();
	}

	private boolean ancestorOf(AGNode node, AGNode n) {
		DomNode current = n.getDomNode();
		while (!current.getTagName().equals("BODY")) {
			current = current.getParent();
			if (current.getxPath().equals(node.getDomNode().getxPath())) {
				return true;
			}
		}
		return false;
	}

	private ArrayList<Contains> getParents(AGNode child) {
		String childXPath = child.domNode.getxPath();
		ArrayList<Contains> contains = new ArrayList<Contains>();
		for (Contains c : getContains()) {
			if (c.getChild().getDomNode().getxPath().equals(childXPath)) {
				contains.add(c);
			}
		}
		return contains;
	}

	private void addToMap(Map<AGNode, ArrayList<AGNode>> cMap, AGNode parent, AGNode a) {
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
		if (ArrayUtils.contains(tagsIgnore, node.getTagName())) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public String toString() {
		StringBuffer out = new StringBuffer("Vertices:"+vertices.size());
		
		
//		for(AGNode v : vertices){
//			out.append("\n\t");
//			out.append(v.domNode.getxPath());
//		}
		
		out.append("\nEdges:(c="+getContains().size()+", s="+siblings.size()+")}");
		for(Contains c:getContains()) {
				out.append("\n\tC:");
				out.append(c.toString());
//				out.append(c.parent);
//				out.append("-->");
//				out.append(c.child);
//				out.append("  -->>  ");
//				out.append(c.getAttributes());
			
		
		}
	
		for(Sibling s:siblings) {
			out.append("\n\tS:");
			out.append(s);
			out.append(" ");
			out.append(s.getFancyAttributes());
		}
//		
		
		return out.toString();
	}
	
	public void writetoGraphViz(String graphName) {
		PrintWriter output = null;
		try {
			output = new PrintWriter(graphName + ".gv");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		traverseGraph();
		
		output.append("digraph G {");
		
		Iterator it = edges.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			Edge e = (Edge) pair.getKey();
			
			output.append("\n\t");
			output.append(e.getNode2().getxPath().replaceAll("\\[|\\]", "").replaceAll("/", ""));
			output.append(" -> ");
			output.append(e.getNode1().getxPath().replaceAll("\\[|\\]", "").replaceAll("/", ""));
			
			if (e instanceof Sibling) {
				output.append(" [ style=dotted, label= \"" + pair.getValue() + "\" ];");
			} else {
				output.append(" [ label= \"" + pair.getValue() + "\" ];");
			}
			
			output.append("\n\t");
			output.append(e.getNode2().getxPath().replaceAll("\\[|\\]", "").replaceAll("/", ""));
			output.append(" [ label = \"" + e.getNode2().getLabel() + " \" ];");
			
			output.append("\n\t");
			output.append(e.getNode1().getxPath().replaceAll("\\[|\\]", "").replaceAll("/", ""));
			output.append(" [ label = \"" + e.getNode1().getLabel() + " \" ];");
		}
		
		output.append("\n}");
		output.close();
		
	}
	
	public void traverseGraph() {
		int counter = 0;
		for (Contains c : getContains()) {
//			if ((!c.parent.domNode.getTagName().equals("A")) && (!c.child.domNode.getTagName().equals("A"))) {
				if (!nodes.containsKey(c.parent.toString())) {
					nodes.put(c.parent.toString(), "element-" + counter);
					counter++;
				}
				if (!nodes.containsKey(c.child.toString())) {
					nodes.put(c.child.toString(), "element-" + counter);
					counter++;
				}
				edges.put(c, c.getAttributes());
                newEdges.put(c.getNode1().getxPath()+c.getNode2().getxPath()+"contains"+c.generateLabelling(),c);
//			}
		}
		
		for (Sibling s : siblings) {
			edges.put(s, s.getLabelling());
            newEdges.put(s.getNode1().getxPath()+s.getNode2().getxPath()+"sibling",s);
		}
	}
	


	public HashMap<String, String> getNodes() {
		return nodes;
	}

	public HashMap<Edge, String> getEdges() { return edges; }

    public HashMap<String, Edge> getNewEdges() { return newEdges; }

	public List<Contains> getContains() {
		return contains;
	}

	public void setContains(List<Contains> contains) {
		this.contains = contains;
	}

	public List<Sibling> getSiblings() {
		return siblings;
	}

	public Object getVertices() {
		return vertices;
	}

	public ArrayList<AGNode> getChildren(AGNode agn) {
		ArrayList<AGNode> children = new ArrayList<AGNode>();
		for (Contains c : this.contains) {
			if (c.parent == agn) {
				children.add(c.child);
			}
		}
		
		HashSet<AGNode> horizChildren = new HashSet<AGNode>();
		
		for (Sibling s : siblings) {
			for (AGNode c : children) {
				if ((s.getNode1() == c.getDomNode()) || (s.getNode2() == c.getDomNode())) {
					if ((s.leftRight) || (s.rightLeft) || (s.topEdgeAligned)) {
						horizChildren.add(c);
					}
				}
			}
		}
		return new ArrayList<AGNode>(horizChildren);
	}

	public Map<String, AGNode> getVMap() {
		return vMap;
	}
	
	
}
