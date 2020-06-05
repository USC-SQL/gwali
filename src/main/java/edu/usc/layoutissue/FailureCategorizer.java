package edu.usc.layoutissue;

import java.util.HashSet;
import java.util.Set;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.config.Config;
import edu.usc.layoutgraph.LayoutGraphComparator;
import edu.usc.layoutgraph.edge.NeighborEdge;

public class FailureCategorizer {

	public enum FailureCateogry{
		OVERLAPPING, ALIGNMENT, OVERFLOWING, MOVEMENT
	}
	
	//most likely will return one category only.. but using a set just in case..
	public Set<FailureCateogry> getCategories(NeighborEdge baselineEdge, NeighborEdge putEdge){
		Set<FailureCateogry> cateogries = new HashSet<FailureCateogry>();
		if(checkOverlapping(baselineEdge, putEdge))
			cateogries.add(FailureCateogry.OVERLAPPING);
		if(checkAlignment(baselineEdge, putEdge))
			cateogries.add(FailureCateogry.ALIGNMENT);
		if(checkOverflowing(baselineEdge, putEdge))
			cateogries.add(FailureCateogry.OVERFLOWING);
		if(checkMovement(baselineEdge,putEdge))
			cateogries.add(FailureCateogry.MOVEMENT);
		return cateogries;
	}
	
	public boolean checkOverlapping(NeighborEdge baselineEdge, NeighborEdge putEdge){
		if(!baselineEdge.isIntersect() && !baselineEdge.isContains() && !baselineEdge.isBoundedBy()
			&& putEdge.isIntersect())
			return true;
		else
			return false;
	}
	public boolean checkAlignment(NeighborEdge baselineEdge, NeighborEdge putEdge){
		DomNode domNode1 = baselineEdge.getNode1().getDomNode();
		DomNode domNode2 = baselineEdge.getNode2().getDomNode();
		//if there is movement failure, then we don't categorize it as alignment
		if(checkMovement(baselineEdge, putEdge))
			return false;
		//only tag nodes are checked for alignment
		if(domNode1.isTag() && domNode2.isTag()){
			if(baselineEdge.isLeftEdgeAligned() && !putEdge.isLeftEdgeAligned())
				return true;
			if(baselineEdge.isTopEdgeAligned() && !putEdge.isTopEdgeAligned())
				return true;
			if(baselineEdge.isRightEdgeAligned() && !putEdge.isRightEdgeAligned())
				return true;
			if(baselineEdge.isBottomEdgeAligned() && !putEdge.isBottomEdgeAligned())
				return true;
		}
		
		//no problem detected for alignment
		return false;
	}
	
	public boolean checkOverflowing(NeighborEdge baselineEdge, NeighborEdge putEdge){
		if(baselineEdge.isContains() && putEdge.isIntersect())
			return true;
		else
			return false;
	}
	
	public boolean checkMovement(NeighborEdge baselineEdge, NeighborEdge putEdge){
		//check angle too..
		double angelDiff = LayoutGraphComparator.AngleDiff(baselineEdge.getAngleDegree(), putEdge.getAngleDegree());
		if(angelDiff > Config.ANGEL_THRISHOLD){
			if(baselineEdge.isBottomTop() && 
				!putEdge.isBottomTop() && !putEdge.isContains() && 
				!putEdge.isBoundedBy() && !putEdge.isIntersect() )
					return true;
			
			if(baselineEdge.isTopBottom() && 
				!putEdge.isTopBottom() && !putEdge.isContains() && 
				!putEdge.isBoundedBy() && !putEdge.isIntersect() )
					return true;
			
			if(baselineEdge.isLeftRight() && 
				!putEdge.isLeftRight() && !putEdge.isContains() && 
				!putEdge.isBoundedBy() && !putEdge.isIntersect() )
					return true;
	
			if(baselineEdge.isRightLeft() && 
				!putEdge.isRightLeft() && !putEdge.isContains() && 
				!putEdge.isBoundedBy() && !putEdge.isIntersect() )
					return true;
		}
		return false;
	}
	
}
