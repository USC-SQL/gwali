package edu.usc.issuesfilter;

import java.util.ArrayList;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.layoutissue.Issue;

public class AlignmentIssueFilter implements LayoutIssuesFilter {

	
	@Override
	public ArrayList<Issue> filter(ArrayList<Issue> issues) {
		ArrayList<Issue> filteredIssues = new ArrayList<>();
		
		for (Issue issue : issues) {
			if(issue.isAlignmentIssue()){
				DomNode domNode1 = issue.getBaselineEdge().getNode1().getDomNode();
				DomNode domNode2 = issue.getBaselineEdge().getNode2().getDomNode();
				
					
				//filter: only tag nodes are checked alignment
				if(isAlignmentNeeded(domNode1) && isAlignmentNeeded(domNode2))
					filteredIssues.add(issue);
			}
			else{ //non Alignment issue
				filteredIssues.add(issue);
			}
		}
		
		return filteredIssues;
	}
	
	private boolean isAlignmentNeeded(DomNode domNode1){
		if(domNode1.isTag())
			return true;
		else
			return false;

	}

}
