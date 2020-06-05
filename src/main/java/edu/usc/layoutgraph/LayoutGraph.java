package edu.usc.layoutgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.config.Config;
import edu.usc.layoutgraph.edge.NeighborEdge;
import edu.usc.layoutgraph.node.LayoutNode;
import edu.usc.layoutgraph.node.LayoutNodeComparator;
import edu.usc.util.Utils;

public class LayoutGraph {

	ArrayList<LayoutNode> vertices;
	ArrayList<NeighborEdge> edges;
	Map<String, LayoutNode> vMap;



	public LayoutGraph(DomNode root) {
		vertices = new ArrayList<>();
		edges = new ArrayList<>();
		vMap = new HashMap<>();
		
		init(root);
	}

	public void init(DomNode root) {
		List<DomNode> worklist = new ArrayList<>();
		worklist.add(root);

		// Populate Nodes
		while (!worklist.isEmpty()) {
			DomNode node = worklist.remove(0);
			
			// This code is to normalize the xpath, so they are all lower case and indexed
			// TODO: remove this code once we update getMBR to report normalized xpath
			if(Config.normalizePath){
				String xpath = Utils.normalizeXPATH(node.getxPath());
				node.setxPath(xpath);
			}
			if (node.isLayout() && node.isVisible()) { 
				LayoutNode n = new LayoutNode(node);
				vertices.add(n);
				vMap.put(node.getxPath(), n);
			}
			worklist.addAll(node.getChildren());
		}
		
		// Sort the vertices based on Area and DOM hierarchy
		Collections.sort(vertices, new LayoutNodeComparator());
				
		
		// complete graph.. all vertices connected to each other..
			for(LayoutNode v : vertices) {
				for (LayoutNode w : vertices) {
					if(!(v == w))
						edges.add( new NeighborEdge(v,w));
				}
			}
	}

	
	@SuppressWarnings("unused")
	private void addToMap(Map<LayoutNode, ArrayList<LayoutNode>> cMap, LayoutNode parent, LayoutNode a) {
		if(!cMap.containsKey(parent)) {
			cMap.put(parent, new ArrayList<LayoutNode>());
		}
		cMap.get(parent).add(a);
	}


	
	public ArrayList<LayoutNode> getVertices() {
		return vertices;
	}

	public void setVertices(ArrayList<LayoutNode> vertices) {
		this.vertices = vertices;
	}

	public ArrayList<NeighborEdge> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<NeighborEdge> edges) {
		this.edges = edges;
	}

	@Override
	public String toString() {
		StringBuffer out = new StringBuffer("Vertices:"+vertices.size());
		out.append("\nEdges   :"+edges.size());
		
		return out.toString();
	}

	public NeighborEdge findEdge(DomNode v, DomNode w) {
		for (NeighborEdge neighborEdge : edges) {
			if( neighborEdge.getNode1().getDomNode() == v && neighborEdge.getNode2().getDomNode() == w)
				return neighborEdge;
		}
		return null;
	}

	public NeighborEdge findEdge(String xpath1, String xpath2) {
		for (NeighborEdge neighborEdge : edges) {
			if( neighborEdge.getNode1().getDomNode().getxPath().equals(xpath1)
					&& neighborEdge.getNode2().getDomNode().getxPath().equals(xpath2))
				return neighborEdge;
		}
		return null;
	}
}
