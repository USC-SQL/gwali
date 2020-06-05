package edu.usc.issuesfilter;

import java.util.ArrayList;

import edu.gatech.xpert.dom.DomNode;
import edu.usc.layoutissue.Issue;

public class OptionElementFilter implements LayoutIssuesFilter
{

	@Override
	public ArrayList<Issue> filter(ArrayList<Issue> issues)
	{
		ArrayList<Issue> filteredLayoutIssuesUpdated = new ArrayList<>();
		for (Issue issue : issues) {
			DomNode n1 = issue.getPageUnderTestEdge().getNode1().getDomNode();
			DomNode n2 = issue.getPageUnderTestEdge().getNode2().getDomNode();
			if(!n1.getxPath().toLowerCase().contains("option") && !n2.getxPath().toLowerCase().contains("option"))
			{
				filteredLayoutIssuesUpdated.add(issue);
			}
		}

		return filteredLayoutIssuesUpdated;
	}

}
