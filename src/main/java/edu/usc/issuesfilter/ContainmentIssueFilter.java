package edu.usc.issuesfilter;

import java.util.ArrayList;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.layoutissue.Issue;

public class ContainmentIssueFilter implements LayoutIssuesFilter {



	@Override
	public ArrayList<Issue> filter(ArrayList<Issue> issues) {
		ArrayList<Issue> filteredIssues = new ArrayList<>();
		
		for (Issue issue : issues) {
			if(issue.isContainmentIssue()){
				DomNode domNode1 = issue.getBaselineEdge().getNode1().getDomNode();
				DomNode domNode2 = issue.getBaselineEdge().getNode2().getDomNode();
				
				//filter: only tag nodes are checked alignment
				if(    domNode1.isTag() && (domNode2.isText() || domNode2.isInputText())
					|| domNode2.isTag() && (domNode1.isText() || domNode1.isInputText())
						)
					filteredIssues.add(issue);
			}
			else{ //non containment issue
				filteredIssues.add(issue);
			}
		}
		
		return filteredIssues;

	}

}
