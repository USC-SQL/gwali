package edu.usc.layoutissue;

import java.util.ArrayList;
import java.util.Collections;

import edu.gatech.xpert.dom.DomNode;
import edu.gatech.xpert.dom.DomNode.NodeType;
import edu.usc.config.Config;

public class SuspiciousnessScoreEvaluator {
	ArrayList<Issue> issues;
	public SuspiciousnessScoreEvaluator(ArrayList<Issue> filteredIssues) {
		issues = filteredIssues;
	}
	
	public ArrayList<NodeSuspiciousness> computeSuspiciousnesses(){
		ArrayList<NodeSuspiciousness> nodesSuspiciousness = new ArrayList<NodeSuspiciousness>();
		
		//iterate over all the issues and compute the score for each node.
		for (Issue issue : issues) {
			DomNode node1 = issue.getPageUnderTestEdge().getNode1().getDomNode();
			DomNode node2 = issue.getPageUnderTestEdge().getNode2().getDomNode();
			DomNode lowestCommonAncestor = issue.getPageUnderTestEdge().lowestCommonAncestor();
			DomNode allTxtChildren = new DomNode(lowestCommonAncestor);
			allTxtChildren.setParent(lowestCommonAncestor);
			String AllChildrenXpath = allTxtChildren.getxPath();
			allTxtChildren.setxPath(AllChildrenXpath + Config.ALL_CHILDREN_XPATH );
			allTxtChildren.setNodeType(NodeType.TEXT);
			boolean node1Exist = false;
			boolean node2Exist = false;
			boolean lcaExist = false;
			
			for (NodeSuspiciousness ns : nodesSuspiciousness) {
				if (issue.isDirectionIssue() && ns.getDomNode().getxPath().equals(allTxtChildren.getxPath()) ) {
					ns.increaseSuspiciousness(issue.getIssueWeight());
					lcaExist = true;
				}
				if ( ns.getDomNode().getxPath().equals(node1.getxPath()) ) {
					ns.increaseSuspiciousness(issue.getIssueWeight());
					node1Exist = true;
				}
				if ( ns.getDomNode().getxPath().equals(node2.getxPath()) ) {
					ns.increaseSuspiciousness(issue.getIssueWeight());
					node2Exist = true;
				}
			}
			if(issue.isDirectionIssue() && !lcaExist) 
				nodesSuspiciousness.add(new NodeSuspiciousness(allTxtChildren)); 
			if(!node1Exist) nodesSuspiciousness.add(new NodeSuspiciousness(node1));
			if(!node2Exist) nodesSuspiciousness.add(new NodeSuspiciousness(node2));
		}
		//sort the nodes so the nodes with higher suspicious score so they appear first in the list
		Collections.sort(nodesSuspiciousness);
		return nodesSuspiciousness;
	}

}
