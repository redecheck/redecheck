package edu.gatech.xpert.dom;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gatech.xpert.dom.visitors.DomVisitor;

public class DomNode {

	// Constructors

	public DomNode(String tagName, String xPath) {
		this.setTagName(tagName);
		this.setNodeType(NodeType.TAG);
		this.setxPath(xPath);
		this.children = new ArrayList<>();
		this.setAttributes(new HashMap<String, String>());
	}

	public DomNode(String text) {
		try {
			this.setText(URLDecoder.decode(text, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			System.err.println("Node text cannot be decoded :" + text);
			e.printStackTrace();
		}
		this.setNodeType(NodeType.TEXT);
		this.children = new ArrayList<>();
	}

	// Accept Visitors

	public void accept(DomVisitor visitor, boolean tagNodesOnly) {
		visitor.visit(this);

		for (DomNode child : this.children) {
			if (child.nodeType == NodeType.TAG || !tagNodesOnly) {
				child.accept(visitor, tagNodesOnly);
			}
		}

		visitor.endVisit(this);
	}

	public void accept(DomVisitor visitor) {
		accept(visitor, false);
	}

	// Variable Declarations

	public enum NodeType {
		TAG, TEXT
	}

	private NodeType nodeType;
	private String tagName, xPath, text, id;
	private Map<String, String> attributes;
	private int zindex, level;
	private int[] coords;
	private long hash;
	private boolean clickable, visible;
	private List<DomNode> children;
	private DomNode parent;
	private boolean matched;

	// Setters & Getters

	public String attr(String key) {
		return this.attributes.get(key);
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getxPath() {
		return xPath;
	}

	public void setxPath(String xPath) {
		this.xPath = xPath;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public int getZindex() {
		return zindex;
	}

	public void setZindex(int zindex) {
		this.zindex = zindex;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int[] getCoords() {
		return coords;
	}

	public void setCoords(int[] coords) {
		this.coords = coords;
	}

	public long getHash() {
		return hash;
	}

	public void setHash(long hash) {
		this.hash = hash;
	}

	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public List<DomNode> getChildren() {
		return children;
	}

	public void setChildren(List<DomNode> children) {
		this.children = children;
	}

	public DomNode getParent() {
		return parent;
	}

	public void setParent(DomNode parent) {
		this.parent = parent;
	}

	public boolean isMatched() {
		return matched;
	}

	public void setMatched(boolean matched) {
		this.matched = matched;
	}

	// Utility Methods

	public void addChild(DomNode node) {
		node.setParent(this);
		this.children.add(0, node);
	}

	public boolean isTag() {
		return (nodeType == NodeType.TAG);
	}

	public boolean isText() {
		return (nodeType == NodeType.TEXT);
	}

	@Override
	public String toString() {
		return tagName + " > " + xPath + "{txt=" + text + "}" + attributes;
	}

	public Map<String, String> getDynDomData() {
		Map<String, String> domData = new HashMap<>();
		domData.put("clickable", isClickable() ? "t" : "f");
		domData.put("visible", isVisible() ? "t" : "f");
		if (zindex != Integer.MIN_VALUE) {
			domData.put("zindex", String.format("%d", zindex));
		}
		//domData.put("children", String.format("%d", children.size()));
		return domData;
	}

}
