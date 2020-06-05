package edu.gatech.xpert.dom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchResult implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Map<String, String> matched;
	int numberOfMatched = 0;
	List<String> unmatched1, unmatched2;
	private double matchRatio;


	public MatchResult(Map<DomNode, DomNode> matched, List<DomNode> unmatched1,
			List<DomNode> unmatched2) {
		setMatched(matched);
		setUnmatched1(unmatched1);
		setUnmatched2(unmatched2);
	}
	
	public MatchResult(Map<DomNode, DomNode> matched, List<DomNode> unmatched1,
			List<DomNode> unmatched2, int numberOfMatch) {
		this(matched,unmatched1,unmatched2);
		this.numberOfMatched = numberOfMatch;
		computeMatchRatio();

	}

	private void computeMatchRatio() {
		double noMatched = this.getNoOfMatched();
		
		double totalElements = this.getUnmatched1().size() + 
							   this.getMatched().size();
				
		this.setMatchRatio(noMatched/totalElements);
	}
	
	// Setters and Getters
	
	public Map<String, String> getMatched() {
		return matched;
	}

	public void setMatched(Map<DomNode, DomNode> matched) {
		Map<String, String> matchedXPaths = new HashMap<String, String>();
		
		for (DomNode node : matched.keySet()) {
			String baseXpath = node.getxPath();
			String putXpath = matched.get(node).getxPath();
			matchedXPaths.put(baseXpath, putXpath);
		}
		this.matched = matchedXPaths;
	}

	public List<String> getUnmatched1() {
		return unmatched1;
	}

	public void setUnmatched1(List<DomNode> unmatched1) {
		List<String> unmatchedXpaths1 = new ArrayList<String>();
		for (DomNode node : unmatched1) {
			String nodeXpath = node.getxPath();
			unmatchedXpaths1.add(nodeXpath);
		}
		this.unmatched1 = unmatchedXpaths1;
	}

	public List<String> getUnmatched2() {
		return unmatched2;
	}

	public void setUnmatched2(List<DomNode> unmatched2) {
		List<String> unmatchedXpaths2 = new ArrayList<String>();
		for (DomNode node : unmatched2) {
			String nodeXpath = node.getxPath();
			unmatchedXpaths2.add(nodeXpath);
		}
		this.unmatched2 = unmatchedXpaths2;
	}

	public int getNoOfMatched() {
		return numberOfMatched;
	}
	
	public Map<DomNode, DomNode> getMatchedNodes(Map<String,DomNode> baseElements, Map<String,DomNode> testElements){
		Map<DomNode, DomNode> matchedNodes = new HashMap<DomNode, DomNode>();
		for (String baseXpath : matched.keySet()) {
			String putXpath = matched.get(baseXpath);
			DomNode baseNode = baseElements.get(baseXpath);
			DomNode putNode = testElements.get(putXpath);
			matchedNodes.put(baseNode, putNode);
		}
		return matchedNodes;
	}

	/**
	 * @return the matchRatio
	 */
	public double getMatchRatio() {
		return matchRatio;
	}
	
	
	public void setMatchRatio(double matchRatio) {
		this.matchRatio = matchRatio;
	}


}
