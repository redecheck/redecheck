package edu.gatech.xpert.dom.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.gatech.xpert.dom.DomNode;

public class AGDiff {
	Map<DomNode, DomNode> matchedNodes;
	AlignmentGraph ag1, ag2;

	public AGDiff(Map<DomNode, DomNode> matchedNodes, AlignmentGraph ag1,
			AlignmentGraph ag2) {
		this.matchedNodes = matchedNodes;
		this.ag1 = ag1;
		this.ag2 = ag2;
	}

	public List<String> diff(String xPath1, String xPath2) {
		AGNode a = ag1.vMap.get(xPath1);
		AGNode b = ag2.vMap.get(xPath2);

		List<String> issues = new ArrayList<>();
		if (a != null && b != null) {
			compareParents(a, b, issues);
			compareSiblings(a, b, issues);
		}
		return issues;
	}

	private void compareParents(AGNode a, AGNode b, List<String> issues) {
		Contains c1 = null, c2 = null;
		for (Contains c : ag1.contains) {
			if (c.child == a) {
				c1 = c;
				break;
			}
		}
		for (Contains c : ag2.contains) {
			if (c.child == b) {
				c2 = c;
				break;
			}
		}

		if (c1 != null && c2 != null) {
			DomNode p1 = c1.parent.domNode;
			DomNode p2 = c2.parent.domNode;

			DomNode expected_p2 = matchedNodes.get(p1);
			if (expected_p2 != null
					&& !StringUtils.equals(expected_p2.getxPath(),
							p2.getxPath())) {
				issues.add(errMsg("PARENTS DIFFER", c1, c2));
				return;
			}
			
			if(c1.isSizeDiffX() && c2.isSizeDiffY()) {
				if (testSizeDiff(c1.topAligned, c2.topAligned, c1.yError,c2.yError)) {
					issues.add(errMsg("TOP-ALIGNMENT", c1, c2));
				}
	
				if (testSizeDiff(c1.bottomAligned, c2.bottomAligned, c1.yError, c2.yError)) {
					issues.add(errMsg("BOTTOM-ALIGNMENT", c1, c2));
				}
				
				if(testSizeDiff(c1.middle, c2.middle, c1.yError, c2.yError)) {
					issues.add(errMsg("VMID-ALIGNMENT", c1, c2));
				}
				
				if(c1.vFill ^ c2.vFill) {
					issues.add(errMsg("VFILL", c1, c2));
				}
			}
			
			if(c1.isSizeDiffX() && c2.isSizeDiffX()) {
				if(c1.hFill ^ c2.hFill) {
					issues.add(errMsg("HFILL", c1, c2));
				}
				
				if (c1.centered ^ c2.centered) {
					issues.add(errMsg("CENTER-ALIGNMENT", c1, c2));
				}
	
				if (c1.leftJustified ^ c2.leftJustified) {
					issues.add(errMsg("LEFT-JUSTIFICATION", c1, c2));
				}
	
				if (c1.rightJustified ^ c2.rightJustified) {
					issues.add(errMsg("RIGHT-JUSTIFICATION", c1, c2));
				}
			}
		} else if (c1 == null && c2 != null) {
			issues.add(errMsg("MISSING-PARENT-1", c1, c2));
		} else if (c1 != null && c2 == null) {
			issues.add(errMsg("MISSING-PARENT-2", c1, c2));
		}
		
	}

	private void compareSiblings(AGNode a, AGNode b, List<String> issues) {
		List<Sibling> sa = new ArrayList<>(), sb = new ArrayList<>();
		for (Sibling e : ag1.siblings) {
			if (e.getNode1() == a.domNode) {
				sa.add(e);
			}
		}
		for (Sibling e : ag2.siblings) {
			if (e.getNode1() == b.domNode) {
				sb.add(e);
			}
		}

		Map<Sibling, Sibling> matched = new HashMap<>();
		List<Sibling> unmatch1 = new ArrayList<>(), unmatch2 = new ArrayList<>();

		while (sa.size() > 0) {
			Sibling s = sa.remove(0), match = null;
			for (Sibling tmp : sb) {
				if (tmp.getNode2() == matchedNodes.get(s.getNode2())) {
					match = tmp;
					break;
				}
			}
			if (match != null) {
				matched.put(s, match);
				sb.remove(match);
			} else {
				unmatch1.add(s);
			}
		}

		for (Sibling s : sb) {
			unmatch2.add(s);
		}

		for (Sibling s : unmatch1) {
			issues.add(errMsg("MISSING-SIBLING-2", s, null));
		}
		for (Sibling s : unmatch1) {
			issues.add(errMsg("MISSING-SIBLING-1", null, s));
		}

		for (Sibling x : matched.keySet()) {
			Sibling y = matched.get(x);

			if (x.topEdgeAligned ^ y.topEdgeAligned) {
				issues.add(errMsg("TOP-EDGE-ALIGNMENT", x, y));
			}
			if (x.rightEdgeAligned ^ y.rightEdgeAligned) {
				issues.add(errMsg("RIGHT-EDGE-ALIGNMENT", x, y));
			}
			if (x.bottomEdgeAligned ^ y.bottomEdgeAligned) {
				issues.add(errMsg("BOTTOM-EDGE-ALIGNMENT", x, y));
			}
			if (x.leftEdgeAligned ^ y.leftEdgeAligned) {
				issues.add(errMsg("LEFT-EDGE-ALIGNMENT", x, y));
			}

			if (x.topBottom ^ y.topBottom && TBDiff(x, y)) {
				issues.add(errMsg("TOP-BOTTOM", x, y));
			}
			if (x.bottomTop ^ y.bottomTop && BTDiff(x, y)) {
				issues.add(errMsg("BOTTOM-TOP", x, y));
			}
			if (x.leftRight ^ y.leftRight && LRDiff(x, y)) {
				issues.add(errMsg("LEFT-RIGHT", x, y));
			}
			if (x.rightLeft ^ y.rightLeft && RLDiff(x, y)) {
				issues.add(errMsg("RIGHT-LEFT", x, y));
			}
		}

	}

	private String errMsg(String msg, AGEdge x, AGEdge y) {

		String xp1a = "*", xp1b = "*", xp2a = "*", xp2b = "*";
		if (x != null) {
			xp1a = x.getNode1().getxPath();
			xp1b = x.getNode2().getxPath();
		}

		if (y != null) {
			xp2a = y.getNode1().getxPath();
			xp2b = y.getNode2().getxPath();
		}

		return String.format("\"%s\",\"(%s-%s)\",\"(%s-%s)\"", msg, xp1a, xp1b,
				xp2a, xp2b);
	}

	private boolean TBDiff(Sibling x, Sibling y) {
		return isSignificantDiff(x.node1.y1, x.node2.y2, y.node1.y1, y.node2.y2);
	}

	private boolean BTDiff(Sibling x, Sibling y) {
		return isSignificantDiff(x.node1.y2, x.node2.y1, y.node1.y2, y.node2.y1);
	}

	private boolean LRDiff(Sibling x, Sibling y) {
		return isSignificantDiff(x.node1.x2, x.node2.x1, y.node1.x2, y.node2.x1);
	}

	private boolean RLDiff(Sibling x, Sibling y) {
		return isSignificantDiff(x.node1.x1, x.node2.x2, y.node1.x1, y.node2.x2);
	}

	int diffThreshold = 5;

	private boolean isSignificantDiff(int a, int b, int c, int d) {
		int x = Math.abs(a - b);
		int y = Math.abs(c - d);

		if (Math.abs(x - y) > diffThreshold) {
			return true;
		}

		return false;
	}

	private boolean testSizeDiff(boolean p1, boolean p2, double e1, double e2) {
		if (p1 ^ p2) {
			if (p1 && e1 < 0.8) {
				return true;
			}
			if (p2 && e2 < 0.8) {
				return true;
			}
		}
		return false;
	}
}
