package edu.usc.layoutgraph;

import edu.usc.layoutgraph.edge.NeighborEdge;

import java.util.ArrayList;


//This class is used to convert a LayoutGraph for Right to Left Languages.
// So every Right To Left relationship in the graph becomes Left To Right 
// relationship, and left alignment becomes right alignment and vice versa.
// 
public class LayoutGraphConverter {
	
	/*
	 * This method will convert the layout graph lg by reversing it's edges
	 * So every Right-Left relationship in the graph becomes Left-Right 
	 * relationship, and Left-align becomes Right-align and vice versa..
	 * The method makes the changes to the graph itself (i.e. the graph
	 * is not copied)
	 */
	public LayoutGraph convertLayoutGraph(LayoutGraph lg){
		for (NeighborEdge edge : lg.edges) {
			convertEdgeforRTL(edge);
		}
		return lg;
	}

	public LayoutGraph convertLayoutGraph(LayoutGraph lg, ArrayList<NeighborEdge> excludedEdges){
		for (NeighborEdge edge : excludedEdges)
			if(edge.getNode1().getDomNode().getxPath().equalsIgnoreCase("/html[1]/body[1]/div[3]/div[1]/div[1]/div[2]")
					&& edge.getNode2().getDomNode().getxPath().equalsIgnoreCase("/html[1]/body[1]/footer[1]/div[1]"))
				System.out.println("The edge is excluded!!");

		for (NeighborEdge edge : lg.edges) {
			if (!excludedEdges.contains(edge)) {
				convertEdgeforRTL(edge);
			}
		}
		return lg;
	}

	private void convertEdgeforRTL(NeighborEdge edge) {
		if(edge.isLeftRight()){
			edge.setLeftRight(false);
			edge.setRightLeft(true);
		}
		else if(edge.isRightLeft()){
			edge.setRightLeft(false);
			edge.setLeftRight(true);
		}
		if(edge.isRightEdgeAligned() && !edge.isLeftEdgeAligned()){
			edge.setRightEdgeAligned(false);
			edge.setLeftEdgeAligned(true);
		}
		else if(edge.isLeftEdgeAligned() && !edge.isRightEdgeAligned()){
			edge.setLeftEdgeAligned(false);
			edge.setRightEdgeAligned(true);
		}
		double angle = edge.getAngleDegree();
		edge.setAngleDegree(360 - angle);

	}
}
