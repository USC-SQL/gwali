package edu.usc.gwali;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.xpert.dom.DomNode;
import edu.gatech.xpert.dom.DomNode.NodeType;
import edu.usc.config.Config;
import edu.usc.issuesfilter.AlignmentIssueFilter;
import edu.usc.issuesfilter.CenteredIssueFilter;
import edu.usc.issuesfilter.ContainmentIssueFilter;
import edu.usc.issuesfilter.DirectionIssueFilter;
import edu.usc.issuesfilter.LayoutIssuesFilterProcessor;
import edu.usc.issuesfilter.OnePixelIssueFilter;
import edu.usc.issuesfilter.OptionElementFilter;
import edu.usc.layoutgraph.LayoutGraph;
import edu.usc.layoutgraph.LayoutGraphBuilder;
import edu.usc.layoutgraph.edge.NeighborEdge;
import edu.usc.layoutissue.Issue;
import edu.usc.layoutissue.NodeSuspiciousness;
import edu.usc.layoutissue.SuspiciousnessScoreEvaluator;
import edu.usc.performance.TimeMeasurement;

public class Gwali {
	final static Logger logger = LoggerFactory.getLogger(Gwali.class);

	private LayoutGraphBuilder lgb;
	private int noOfInconsistancy = -1;
	private int amountOfInconsistancy = -1;
	
	private ArrayList<Issue> layoutIssues;
	private ArrayList<Issue> unfilteredIssues;
	
	private ArrayList<String> potentiallyFaultyElements;
	private ArrayList<String[]> potentiallyFaultyElementPairs;
	
	public Gwali(FirefoxDriver baselineWebDriver, FirefoxDriver putWebDriver){

		TimeMeasurement.reset();
		TimeMeasurement.tic("GWALI");
		//extract MBRs..
		lgb = new LayoutGraphBuilder(baselineWebDriver, putWebDriver);

	}
	
	public void ChangePUT(FirefoxDriver putWebDriver){
			TimeMeasurement.reset();
			TimeMeasurement.tic("GWALI");
			lgb.changePUT(putWebDriver);
	}
	
	
	public ArrayList<String> runGwali(){
		
		potentiallyFaultyElements = new ArrayList<>();
		potentiallyFaultyElementPairs = new ArrayList<String[]>();
		
			
		//build and compare graphs
		ArrayList<Issue> potentialLayoutIssues = lgb.compareLayoutGraphs();
		
		unfilteredIssues = potentialLayoutIssues;
		
		//filter issues
		LayoutIssuesFilterProcessor filters = new LayoutIssuesFilterProcessor();
		
		filters.addFilter(new ContainmentIssueFilter());
		filters.addFilter(new DirectionIssueFilter());
		filters.addFilter(new OptionElementFilter());
		filters.addFilter(new OnePixelIssueFilter());
		// in case GWALI is running in strict mode these issues should not be filtered
		if(!Config.strictMode){
			filters.addFilter(new CenteredIssueFilter());
			filters.addFilter(new AlignmentIssueFilter());
		}
		ArrayList<Issue> filteredLayoutIssues = filters.filterissues(potentialLayoutIssues);
		
		layoutIssues = filteredLayoutIssues;
		
		//set the value for number of inconsistencies
		noOfInconsistancy = filteredLayoutIssues.size();
		
		amountOfInconsistancy = computeAmountOfInconsistancy(filteredLayoutIssues);
		//compute suspiciousness scores
		SuspiciousnessScoreEvaluator suspiciousnessComputer = new SuspiciousnessScoreEvaluator(filteredLayoutIssues);
		ArrayList<NodeSuspiciousness> nodesSuspiciousness = suspiciousnessComputer.computeSuspiciousnesses();
		//normalize xpaths for all nodes
		
		
		ArrayList<DomNode> resultDomNodes = new ArrayList<DomNode>();
		
		for (NodeSuspiciousness susNode : nodesSuspiciousness) {
			resultDomNodes.add(susNode.getDomNode());
		}
		
		if(Config.removeNonTagNodes){
			resultDomNodes = removeNonTagNodes(resultDomNodes);
		}
		
		for (DomNode domNode : resultDomNodes) {
			String xpath = domNode.getxPath();
			potentiallyFaultyElements.add(xpath);
		}
		
		// retain rank order according to suspiciousness but remove extra elements (lowest common ancestor)
		Set<String> visitedXpaths = new HashSet<>();
		
		for (NodeSuspiciousness susNode : nodesSuspiciousness) 
		{
			for (Issue filteredLayoutIssue : filteredLayoutIssues)
			{
				DomNode n1 = filteredLayoutIssue.getPageUnderTestEdge().getNode1().getDomNode();
				DomNode n2 = filteredLayoutIssue.getPageUnderTestEdge().getNode2().getDomNode();
				if(susNode.getDomNode().equals(n1))
				{
					String n1Xpath = getCleanedDomNode(n1).getxPath();
					String n2Xpath = getCleanedDomNode(n2).getxPath();
					if(!visitedXpaths.contains(n1Xpath) || !visitedXpaths.contains(n2Xpath))
					{
						potentiallyFaultyElementPairs.add(new String[]{n1Xpath, n2Xpath});
						visitedXpaths.add(n1Xpath);
						visitedXpaths.add(n2Xpath);
					}
					break;
				}
			}
		}

		
		TimeMeasurement.toc();

		return potentiallyFaultyElements;
	}
	
	
	public int computeAmountOfInconsistancy(ArrayList<Issue> filteredLayoutIssues) {
		int amount = 0;
		
		for (Issue issue : filteredLayoutIssues) {
			amount += issue.getIssueAmount();
		}
		
		return amount;
	}

	public LayoutGraph getBaseLG(){
		return lgb.getBaselineLG();
	}
	
	public LayoutGraph getPutLG(){
		return lgb.getTestPageLG();
	}
	
	public ArrayList<DomNode> getBaseElements(){
		return lgb.getBaselineElements();
	}
	
	public ArrayList<DomNode> getPutElements(){
		return lgb.getTestpageElements();
	}
	
	public int getNoOfIncosistancy(){
		return noOfInconsistancy;
	}
	
	public int getAmountOfInconsistancy(){
		return amountOfInconsistancy;
	}


	public Map<DomNode, DomNode> getMatchedNodes() {
		return lgb.getMatchedNodes();
	}


	public ArrayList<String> getPotentiallyFaultyElements()
	{
		return potentiallyFaultyElements;
	}

	public ArrayList<String[]> getPotentiallyFaultyElementPairs()
	{
		return potentiallyFaultyElementPairs;
	}


	public double ComputeDistanceDifference(){
		double totalDistance = 0;
		LayoutGraph layoutGraph1 = getBaseLG();
		LayoutGraph layoutGraph2 = getPutLG();
		Map<DomNode,DomNode> matchedNodes = getMatchedNodes();

		for (NeighborEdge baselineEdge:layoutGraph1.getEdges()) {
			NeighborEdge putEdge = baselineEdge.getMatchedEdge(matchedNodes,layoutGraph2);
			if( putEdge != null){
				totalDistance += Math.abs(putEdge.getDistance() - baselineEdge.getDistance());
			}
		}
		return totalDistance;
	}
	
	
	//print the discover issues by gwali..
	public void printIssues(){
		for (Issue layoutIssue : layoutIssues) {
				System.out.println(layoutIssue.toString());
		}
		System.out.println(TimeMeasurement.getMeasurementResults());
	}
	
	//print the discover issues by gwali..
	public void printUnfilteredIssues(){
		for (Issue layoutIssue : unfilteredIssues) {
				System.out.println(layoutIssue.toString());
		}
		System.out.println(TimeMeasurement.getMeasurementResults());
	}

	//This method takes a list of DomNode including nodes that contain attributes
	//and text, then cleans this list by removing the attribute and the text nodes
	//and keeping only the tag nodes.
	//For any attribute or text node that is removed, the parent tag node is added
	public static ArrayList<DomNode> removeNonTagNodes(ArrayList<DomNode> uncleaned){
		ArrayList<DomNode> cleaned = new ArrayList<DomNode>();
		for (DomNode domNode : uncleaned) {
			domNode = getCleanedDomNode(domNode);
			//add nodes if they are not there already (avoid duplicates)
			if(!cleaned.contains(domNode))
					cleaned.add(domNode);
		}
		
		return cleaned;
	}
	
	private static DomNode getCleanedDomNode(DomNode node)
	{
		//for non TAG nodes, like text or attribute, add their parent node
		if(node.getNodeType() != NodeType.TAG)
			node = node.getParent();
		return node;
	}



}
