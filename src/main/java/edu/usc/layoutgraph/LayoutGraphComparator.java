package edu.usc.layoutgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.usc.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.layoutgraph.edge.EdgeComparator;
import edu.usc.layoutgraph.edge.NeighborEdge;
import edu.usc.layoutgraph.node.LayoutNode;
import edu.usc.layoutissue.Issue;
import edu.usc.layoutissue.IssueComparator;
import edu.usc.layoutissue.Issue.IssueType;
import gnu.trove.TIntProcedure;

public class LayoutGraphComparator {
	final static Logger logger = LoggerFactory.getLogger(LayoutGraphComparator.class);

	
	Map<DomNode, DomNode> matchedNodes;
	LayoutGraph layoutGraph1, layoutGraph2;
	
	private SpatialIndex spatialIndex;
	private Map<Integer, Rectangle> rects;
	private int rectId;
	private Map<Integer, LayoutNode> rectIdNodeMap;
	private Map<LayoutNode, Integer> rectNodeIdMap;
	
	// number of found rects for each point, used to avoid adding a rectangle as a neighbor of itself.
	private int numberOfRectsFound;

	//constructor assigning k value dynamically
	public LayoutGraphComparator(Map<DomNode, DomNode> matchedNodes, LayoutGraph lg1,
			LayoutGraph lg2) {
		int kValue = getAppropriateKValue(lg1.vertices.size());
		this.matchedNodes = matchedNodes;
		this.layoutGraph1 = lg1;
		this.layoutGraph2 = lg2;
		addLayoutNodesToSpatialIndex(this.layoutGraph1);
		assignNearestEdgeValues(this.layoutGraph1 , kValue);
	}

	public LayoutGraphComparator(Map<DomNode, DomNode> matchedNodes, LayoutGraph lg1,
			LayoutGraph lg2 , int nearestKValue) {
		this.matchedNodes = matchedNodes;
		this.layoutGraph1 = lg1;
		this.layoutGraph2 = lg2;
		addLayoutNodesToSpatialIndex(this.layoutGraph1);
		assignNearestEdgeValues(this.layoutGraph1 , nearestKValue);
	}

	
	private int getAppropriateKValue(int numberOfVertices) {
		int nearestKValue;
		if(numberOfVertices < 20)
			nearestKValue = 4;
		else if(numberOfVertices < 70)
			nearestKValue = 5;
		else if(numberOfVertices < 92)
			nearestKValue = 6;
		else
			nearestKValue = 6;
		return nearestKValue;
	}
	
	private void assignNearestEdgeValues(LayoutGraph layoutGraph, int nearestKValue) {
		for (NeighborEdge edge : layoutGraph.edges) {
			
			int rectangleId1 = rectNodeIdMap.get(edge.getNode1());
			int rectangleId2 = rectNodeIdMap.get(edge.getNode2());
			HashSet<Integer> results = getNearestRectangles(rectangleId1, nearestKValue);
			if(results.contains(rectangleId2)){
				edge.setNearestNeighbor(true);
			}
			//System.out.println(edge);
		}
	}
	
	private HashSet<Integer> getNearestRectangles(int rectid, int nearestKValue){
		
		HashSet<Integer> results = new HashSet<Integer>();
		
		//five points representing the four corners+centroid
		Point[] points = new Point[5];
		
		Rectangle r = rects.get(rectid);
		
		points[0] = r.centre();
		points[1] = new Point(r.minX, r.minY);
		points[2] = new Point(r.minX, r.maxY);
		points[3] = new Point(r.maxX, r.minY);
		points[4] = new Point(r.maxX, r.maxY);

		for (int i = 0; i < points.length; i++) {
			numberOfRectsFound = 0;
			spatialIndex.nearestN(
					  points[i],      // the point for which we want to find nearby rectangles
					  new TIntProcedure() {         // a procedure whose execute() method will be called with the results
					    public boolean execute(int foundid) {
					    	if(foundid == rectid)
						    	return true;            // return true here to continue receiving results
					    	else{
					    		results.add(foundid);
					    		numberOfRectsFound++;
					    		if(numberOfRectsFound < nearestKValue )
						    		return true;
					    		else 
					    			return false;
					    	}
					    }
					  },
					  nearestKValue + 1,            // the number of nearby rectangles to find (added +1 to not include the rectangle itself.
					  Config.SEARCH_RADIUS               // Don't bother searching further than this.
					);
		}
		return results;
	}
	
		
	

	private void addLayoutNodesToSpatialIndex(LayoutGraph layoutGraph) {
		spatialIndex = new RTree();
		spatialIndex.init(null);
		rects = new HashMap<Integer, Rectangle>();
		rectIdNodeMap = new HashMap<Integer, LayoutNode>();
		rectNodeIdMap = new HashMap<LayoutNode, Integer>();

		for (LayoutNode node : layoutGraph1.vertices) {
			Rectangle r = new Rectangle(node.getX1(), node.getY1(), node.getX2(), node.getY1());
			rects.put(rectId, r);
			rectIdNodeMap.put(rectId, node);
			rectNodeIdMap.put(node, rectId);
			spatialIndex.add(r, rectId++);
		}
	}



	public ArrayList<Issue> compareGraphs() {
		ArrayList<Issue> allIssues = new ArrayList<>();
		
		
		
		Collections.sort(layoutGraph1.getEdges(), new EdgeComparator());
		
		//ArrayList<NeighborEdge> edges = (ArrayList<NeighborEdge>) layoutGraph1.getEdges().clone();
		
		
		
		for (NeighborEdge neighborEdge : layoutGraph1.getEdges()) {

			//Only compare nearestNeighborEdges
			if(!neighborEdge.isNearestNeighbor())
				continue;
			else{
				DomNode v1 = neighborEdge.getNode1().getDomNode();
				DomNode w1 = neighborEdge.getNode2().getDomNode();
				
				DomNode v2 = matchedNodes.get(v1);
				DomNode w2 = matchedNodes.get(w1);
								
				if( v2 == null || w2 == null ){
					//"no matched edge found for: (" + v1 + " , " + w1 + ")" );
					continue;
				}
				else{
					NeighborEdge matchedEdge = layoutGraph2.findEdge(v2,w2);
					if(matchedEdge == null){
						//"Found matched nodes but no matched edge found for: (" + v1 + " , " + w1 + ")"
						continue;
					}
					ArrayList<Issue> edgeIssues = CompareEdges(neighborEdge,matchedEdge);
					for (Issue newIssue : edgeIssues) {
						addIssue(allIssues, newIssue);
					}
				}
			}
		}
		
		Collections.sort(allIssues, new IssueComparator());
		
		return allIssues;
	}
	
	public ArrayList<Issue> addIssue(ArrayList<Issue> currentIssues, Issue newIssue){
		boolean exist = false;
		for (Issue existingIssue : currentIssues) {
			if(existingIssue.similar(newIssue)){
				existingIssue.setIssueWeight(existingIssue.getIssueWeight()+1);
				exist = true;
				break;
			}
		}
		if(!exist)
			currentIssues.add(newIssue);
		return currentIssues;
	}


	private ArrayList<Issue> CompareEdges( NeighborEdge neighborEdge, NeighborEdge matchedEdge) {
		ArrayList<Issue> issues = new ArrayList<>();
		
		// ^ is bitwise exclusive OR operator can be used for two-way check
		// e.g. if (neighborEdge.topEdgeAligned ^ matchedEdge.topEdgeAligned) 
		

		if (neighborEdge.isContains() && !matchedEdge.isContains()) {
			addIssue(issues, new Issue(IssueType.CONTAINMENT, neighborEdge, matchedEdge));
		}
		if (neighborEdge.isTopEdgeAligned() && !matchedEdge.isTopEdgeAligned()) {
			addIssue(issues, new Issue(IssueType.TOPEDGEALIGNMENT, neighborEdge, matchedEdge));
		}
		if (neighborEdge.isBottomEdgeAligned() && !matchedEdge.isBottomEdgeAligned()) {
			addIssue(issues, new Issue(IssueType.BOTTOMEDGEALIGNMENT, neighborEdge, matchedEdge));
		}
		if(!neighborEdge.isIntersect() && matchedEdge.isIntersect()){
			addIssue(issues, new Issue(IssueType.INTERSECTION, neighborEdge, matchedEdge));
		}
		if (neighborEdge.isTopBottom() && !matchedEdge.isTopBottom() ) { 
			addIssue(issues, new Issue(IssueType.TOPBOTTOM, neighborEdge, matchedEdge));
		}
		if (neighborEdge.isBottomTop() && !matchedEdge.isBottomTop() ) { 
			addIssue(issues, new Issue(IssueType.BOTTOMTOP, neighborEdge, matchedEdge));
		}
		if (neighborEdge.isRightEdgeAligned() && !matchedEdge.isRightEdgeAligned()) {
			addIssue(issues, new Issue(IssueType.RIGHTEDGEALIGNMENT, neighborEdge, matchedEdge));
		}
		if (neighborEdge.isLeftEdgeAligned() && !matchedEdge.isLeftEdgeAligned()) {
			addIssue(issues, new Issue(IssueType.LEFTEDGEALIGNMENT, neighborEdge, matchedEdge));
		}
		if (neighborEdge.isLeftRight() && !matchedEdge.isLeftRight() ) {
			addIssue(issues, new Issue(IssueType.LEFTRIGHT, neighborEdge, matchedEdge));
		}
		if (neighborEdge.isRightLeft() && !matchedEdge.isRightLeft() ) {
			addIssue(issues, new Issue(IssueType.RIGHTLEFT, neighborEdge, matchedEdge));
		}
		if(neighborEdge.isCentered() && !matchedEdge.isCentered()) {
			addIssue(issues, new Issue(IssueType.CENTERED, neighborEdge, matchedEdge));
		}
		
		return issues;
	}


	public boolean isMirroredEdge(NeighborEdge neighborEdge, NeighborEdge matchedEdge){
		if (neighborEdge.isRightLeft() && matchedEdge.isLeftRight())
			return true;
		if(neighborEdge.isLeftRight() && matchedEdge.isRightLeft())
			return true;
		if((neighborEdge.isLeftEdgeAligned() && !neighborEdge.isRightEdgeAligned()) && (!matchedEdge.isLeftEdgeAligned() && matchedEdge.isRightEdgeAligned()))
			return true;
		if((neighborEdge.isRightEdgeAligned() && !neighborEdge.isLeftEdgeAligned()) && (!matchedEdge.isRightEdgeAligned() && matchedEdge.isLeftEdgeAligned()))
			return true;
		else
			return false;
	}

	public ArrayList<NeighborEdge> getNonMirroredEdgesinPUT(){
		ArrayList<NeighborEdge> nonMirroedEdges = new ArrayList<>();
		for (NeighborEdge edge:layoutGraph1.getEdges()) {
			NeighborEdge matched = getMatchedEdge(edge);
			if(matched == null) {
				continue;
			}
			else{
				if(!isMirroredEdge(edge,matched))
					nonMirroedEdges.add(matched);
			}
		}
		return nonMirroedEdges;
	}

	public ArrayList<NeighborEdge> getMirroredEdgesinPUT(){
		ArrayList<NeighborEdge> mirroedEdges = new ArrayList<>();
		for (NeighborEdge edge:layoutGraph1.getEdges()) {
			NeighborEdge matched = getMatchedEdge(edge);
			if(matched == null) {
				continue;
			}
			else{
				if(isMirroredEdge(edge,matched))
					mirroedEdges.add(matched);
			}
		}
		return mirroedEdges;
	}

	// return null of no matched edge found
	public NeighborEdge getMatchedEdge(NeighborEdge edge){

		NeighborEdge matchedEdge = null;
		DomNode v1 = edge.getNode1().getDomNode();
		DomNode w1 = edge.getNode2().getDomNode();

		DomNode v2 = matchedNodes.get(v1);
		DomNode w2 = matchedNodes.get(w1);

		if( v2 != null && w2 != null ) {
			matchedEdge = layoutGraph2.findEdge(v2, w2);
		}
		return matchedEdge;

	}



	public static double AngleDiff(double angleDegree, double angleDegree2){
		double d = Math.abs(angleDegree - angleDegree2) % 360;
		double r = d > 180 ? 360 - d : d;
		return r;
	}
}
