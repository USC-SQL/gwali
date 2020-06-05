package edu.usc.layoutgraph.node;

import java.util.Comparator;


public class LayoutNodeComparator implements Comparator<LayoutNode> {

	@Override
	public int compare(LayoutNode a, LayoutNode b) {
		int diff = (int)(a.getArea() - b.getArea()); //small area 
		return (diff == 0) ? (b.getDomNode().getxPath().length() - a.getDomNode().getxPath().length()) : diff; // big xPath
	}

}
