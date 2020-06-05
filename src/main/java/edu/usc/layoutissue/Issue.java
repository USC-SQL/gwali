package edu.usc.layoutissue;

import java.io.Serializable;

import edu.usc.layoutgraph.LayoutGraphComparator;
import edu.usc.layoutgraph.edge.NeighborEdge;
import edu.usc.layoutgraph.node.LayoutNode;

public class Issue implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum IssueType {
		LEFTRIGHT, RIGHTLEFT, TOPBOTTOM, BOTTOMTOP, INTERSECTION, CONTAINMENT,
		TOPEDGEALIGNMENT, RIGHTEDGEALIGNMENT, BOTTOMEDGEALIGNMENT, LEFTEDGEALIGNMENT, CENTERED
	}
	
	private IssueType issueType;
	private NeighborEdge baselineEdge;
	private NeighborEdge put;
	
	private int issueWeight;
	
	private int issueAmount;
	
	public Issue(IssueType issueType, NeighborEdge baselineEdge,
			NeighborEdge pageUnderTestEdge) {
		this.issueType = issueType;
		this.baselineEdge = baselineEdge;
		this.put = pageUnderTestEdge;
		this.issueWeight = 1;
		this.issueAmount = computeAmount();
	}

	public IssueType getIssueType() {
		return issueType;
	}

	public void setIssueType(IssueType issueType) {
		this.issueType = issueType;
	}

	public NeighborEdge getBaselineEdge() {
		return baselineEdge;
	}

	public void setBaselineEdge(NeighborEdge baselineEdge) {
		this.baselineEdge = baselineEdge;
	}

	public NeighborEdge getPageUnderTestEdge() {
		return put;
	}

	public void setPageUnderTestEdge(NeighborEdge pageUnderTestEdge) {
		this.put = pageUnderTestEdge;
	}
	
	
	public boolean similar(Issue issue){
		//same reference
		if(this == issue)
			return true;
		//different reference but same values;
		if(this.issueType == issue.issueType && this.getBaselineEdge() == issue.getBaselineEdge()
			&& this.getPageUnderTestEdge() == issue.getPageUnderTestEdge()	&&
			this.getBaselineEdge().getValueForIssueType(this.issueType)
			== issue.getBaselineEdge().getValueForIssueType(issue.issueType))
				return true;		
		
		return false;
	}
	
	public IssueType getOppositeType(IssueType issueType){
		if(issueType == IssueType.BOTTOMTOP)
			return IssueType.TOPBOTTOM;
		if(issueType == IssueType.TOPBOTTOM)
			return IssueType.BOTTOMTOP;
		if(issueType == IssueType.RIGHTLEFT)
			return IssueType.LEFTRIGHT;
		if(issueType == IssueType.LEFTRIGHT)
			return IssueType.RIGHTLEFT;
		return null;
	}
	
	public boolean isContainmentIssue(){
		if(issueType == IssueType.CONTAINMENT)
			return true;
		return false;
	}
	
	public boolean isIntersectionIssue(){
		if(issueType == IssueType.INTERSECTION)
			return true;
		return false;
	}

	
	public boolean isDirectionIssue(){
		if(issueType == IssueType.BOTTOMTOP || issueType == IssueType.TOPBOTTOM ||
				issueType == IssueType.RIGHTLEFT || issueType == IssueType.LEFTRIGHT )
			return true;
		return false;
	}
	
	public boolean isAlignmentIssue(){
		if(issueType == IssueType.BOTTOMEDGEALIGNMENT || issueType == IssueType.TOPEDGEALIGNMENT ||
				issueType == IssueType.RIGHTEDGEALIGNMENT || issueType == IssueType.LEFTEDGEALIGNMENT )
			return true;
		return false;
	}
	
	public int getIssueWeight() {
		return issueWeight;
	}

	public void setIssueWeight(int issueWeight) {
		this.issueWeight = issueWeight;
	}

	public String toString(){

		String xpath1a = "*", xpath1b = "*", xpath2a = "*", xpath2b = "*";
		boolean baselineEdgeValue = false;
		boolean putEdgeValue = false;;
		if (baselineEdge != null) {
			xpath1a = baselineEdge.getNode1().toString();
			xpath1b = baselineEdge.getNode2().toString();
			baselineEdgeValue = baselineEdge.getValueForIssueType(issueType);
		}

		if (put != null) {
			xpath2a = put.getNode1().toString();
			xpath2b = put.getNode2().toString();
			putEdgeValue = put.getValueForIssueType(issueType);
		}

		String issueString = String.format("\"%s\",%s,%s,\"(%s-%s)\",\"(%s-%s)\" angleDiff: %f , DistanceDiff: %f issueAmount: %d", issueType.toString(),baselineEdgeValue,putEdgeValue, xpath1a, xpath1b,
				xpath2a, xpath2b, 
				LayoutGraphComparator.AngleDiff(baselineEdge.getAngleDegree(), put.getAngleDegree()),
				Math.abs(baselineEdge.getDistance() - put.getDistance()) , issueAmount );
		
		
		
		return issueString;
	}

	private int computeAmount() {
		int amount = 0;		
		if(issueType == IssueType.TOPEDGEALIGNMENT){
			amount = computeTopAlignmentIssueAmount();
		}
		else if(issueType == IssueType.BOTTOMEDGEALIGNMENT){
			amount = computeBtmAlignmentIssueAmount();
		}
		else if(issueType == IssueType.RIGHTEDGEALIGNMENT){
			amount = computeRightAlignmentIssueAmount();
		}
		else if(issueType == IssueType.LEFTEDGEALIGNMENT){
			amount = computeLeftAlignmentIssueAmount();
		}
		else if(issueType == IssueType.CONTAINMENT){
			amount = computeContainmentIssueAmount();
		}
		else if(issueType == IssueType.TOPBOTTOM){
			amount = computeTopBtmIssueAmount();
		}
		else if(issueType == IssueType.BOTTOMTOP){
			amount = computeBtmTopIssueAmount();
		}
		else if(issueType == IssueType.RIGHTLEFT){
			amount = computeRightLeftIssueAmount();
		}
		else if(issueType == IssueType.LEFTRIGHT){
			amount = computeLeftRightIssueAmount();
		}
		else if(issueType == IssueType.INTERSECTION){
			//intersection issue amount sometime is replicated with other containment or direction issue amount
			//this is because any intersection issue has to be accompanied with direction or containment issue. However, some of the directional
			//issues are filtered, so we need to compute the amount of intersection here even if it is a replication
			//also,  when there is intersection issue. it always appears twice, once for edge (n1,n2) and other for edge (n2,n1)
			//if the relationship in the baseline is BOUNDEDBY and in the put it is INTERSECT, then this method will return zero
			//because we already computed the amount for the reverse edge (which has a relationship CONTAINS in the baseline)
			 amount = computeIntersectionIssueAmount();
		}
		else if(issueType == IssueType.CENTERED){
			amount = computeCenteredIssueAmount();
		}
		
		return amount * issueWeight;
	}
	
	private int computeCenteredIssueAmount() {
		LayoutNode n1 = this.put.getNode1();
		LayoutNode n2 = this.put.getNode2();
		int centerDiff = n1.getCenter()[0] - n2.getCenter()[0];
		return Math.abs(centerDiff);
	}

	private int computeTopAlignmentIssueAmount(){
		LayoutNode n1 = this.put.getNode1();
		LayoutNode n2 = this.put.getNode2();
		int y1Diff = n1.getY1() - n2.getY1();
		return Math.abs(y1Diff);
	}
	private int computeBtmAlignmentIssueAmount(){
		LayoutNode n1 = this.put.getNode1();
		LayoutNode n2 = this.put.getNode2();
		int y2Diff = n1.getY2() - n2.getY2();
		return Math.abs(y2Diff);

	}
	private int computeRightAlignmentIssueAmount(){
		LayoutNode n1 = this.put.getNode1();
		LayoutNode n2 = this.put.getNode2();
		int x2Diff = n1.getX2() - n2.getX2();
		return Math.abs(x2Diff);
	}
	private int computeLeftAlignmentIssueAmount(){
		LayoutNode n1 = this.put.getNode1();
		LayoutNode n2 = this.put.getNode2();
		int x1Diff = n1.getX1() - n2.getX1();
		return Math.abs(x1Diff);
	}
	private int computeTopBtmIssueAmount(){
		LayoutNode n1 = this.put.getNode1();
		LayoutNode n2 = this.put.getNode2();
		int delta = n1.getY2() - n2.getY1();
		if(delta < 0){
			System.err.println("THERE IS A BUG HERE.. DELTA SHOULD ALWAYS BE POSITIVE FOR THIS ISSUE TYPE!!! ");
		}
		return delta;
	}
	private int computeBtmTopIssueAmount(){
		LayoutNode n1 = this.put.getNode1();
		LayoutNode n2 = this.put.getNode2();
		int delta = n2.getY2() - n1.getY1();
		if(delta < 0){
			System.err.println("THERE IS A BUG HERE.. DELTA SHOULD ALWAYS BE POSITIVE FOR THIS ISSUE TYPE!!! ");
		}
		return delta;
	}
	private int computeRightLeftIssueAmount(){
		LayoutNode n1 = this.put.getNode1();
		LayoutNode n2 = this.put.getNode2();
		int delta = n2.getX2() - n1.getX1();
		if(delta < 0){
			System.err.println("THERE IS A BUG HERE.. DELTA SHOULD ALWAYS BE POSITIVE FOR THIS ISSUE TYPE!!! ");
		}
		return delta;
	}
	private int computeLeftRightIssueAmount(){
		LayoutNode n1 = this.put.getNode1();
		LayoutNode n2 = this.put.getNode2();
		int delta = n1.getX2() - n2.getX1();
		if(delta < 0){
			System.err.println("THERE IS A BUG HERE.. DELTA SHOULD ALWAYS BE POSITIVE FOR THIS ISSUE TYPE!!! ");
		}
		return delta;

	}


	private int computeContainmentIssueAmount(){
		LayoutNode n1 = this.put.getNode1();
		LayoutNode n2 = this.put.getNode2();

		int n1Right = n1.getX2();
		int n1Left = n1.getX1();
		int n1Top = n1.getY1();
		int n1Btm = n1.getY2();

		int n2Right = n2.getX2();
		int n2Left = n2.getX1();
		int n2Top = n2.getY1();
		int n2Btm = n2.getY2();

		//compute paddings in case of input field
		if(n1.getDomNode().getTagName().toUpperCase().equals("INPUT") &&
				n2.getDomNode().isInputText()){

			n1Left = n1.getDomNode().getInnerCoords()[0];
			n1Top = n1.getDomNode().getInnerCoords()[1];
			n1Right = n1.getDomNode().getInnerCoords()[2];
			n1Btm = n1.getDomNode().getInnerCoords()[3];
		}

		int totalDiff = 0;
		int leftDiff = n1Left - n2Left;
		int rigtDiff = n2Right - n1Right;
		int topDiff = n1Top - n2Top;
		int botmDiff = n2Btm - n1Btm;

		if(leftDiff > 0) totalDiff += leftDiff;
		if(rigtDiff > 0) totalDiff += rigtDiff;
		if(topDiff > 0) totalDiff += topDiff;
		if(botmDiff > 0) totalDiff += botmDiff;
		return totalDiff;

	}
	private int computeIntersectionIssueAmount(){
		int totalDiff = 0;
		if (baselineEdge.isContains() && !put.isContains()) {
			totalDiff += computeContainmentIssueAmount();
		}
		if (baselineEdge.isTopBottom() && !put.isTopBottom() ) { 
			totalDiff += computeTopBtmIssueAmount();
		}
		if (baselineEdge.isBottomTop() && !put.isBottomTop() ) { 
			totalDiff += computeBtmTopIssueAmount();
		}
		if (baselineEdge.isLeftRight() && !put.isLeftRight() ) {
			totalDiff += computeLeftRightIssueAmount();
		}
		if (baselineEdge.isRightLeft() && !put.isRightLeft() ) {
			totalDiff += computeRightLeftIssueAmount();
		}
		return totalDiff;
	}
	
	

	public int getIssueAmount() {
		return issueAmount;
	}

}
