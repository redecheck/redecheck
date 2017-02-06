package edu.gatech.xpert.dom;

public class DomNodePair {
	String state1, state2;
	DomNode node1, node2;
	
	public DomNodePair(String state1, DomNode node1, String state2, DomNode node2) {
		this.state1 = state1;
		this.state2 = state2;
		this.node1 = node1;
		this.node2 = node2;
	}

	public String getState1() {
		return state1;
	}

	public void setState1(String state1) {
		this.state1 = state1;
	}

	public String getState2() {
		return state2;
	}

	public void setState2(String state2) {
		this.state2 = state2;
	}

	public DomNode getNode1() {
		return node1;
	}

	public void setNode1(DomNode node1) {
		this.node1 = node1;
	}

	public DomNode getNode2() {
		return node2;
	}

	public void setNode2(DomNode node2) {
		this.node2 = node2;
	}
	
	@Override
	public String toString() {
		return state1+"::"+node1 + "\n\t>>"+state2+"::"+node2;
	}
	
}
