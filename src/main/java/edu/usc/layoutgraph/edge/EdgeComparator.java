package edu.usc.layoutgraph.edge;

import java.util.Comparator;

public class EdgeComparator implements Comparator<NeighborEdge> {

	@Override
	public int compare(NeighborEdge e1, NeighborEdge e2) {
		int area1 = (int)(e1.node1.getArea() + e1.node2.getArea());
		int area2 = (int)(e2.node1.getArea() + e2.node2.getArea()); 
		
		return area2 - area1;
	}

	

}
