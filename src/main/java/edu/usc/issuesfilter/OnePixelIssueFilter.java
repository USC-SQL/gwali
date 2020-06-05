package edu.usc.issuesfilter;

import java.util.ArrayList;

import edu.usc.layoutissue.Issue;


//this filter is used to remove issues that are introduced because of rounding, 
//rounding could cause elements to have 1 pixel difference in their locations 
public class OnePixelIssueFilter implements LayoutIssuesFilter {

	@Override
	public ArrayList<Issue> filter(ArrayList<Issue> issues) {
		ArrayList<Issue> filteredLayoutIssuesUpdated = new ArrayList<>();
		for (Issue issue : issues) {
			if(issue.getIssueAmount() > 1)
			{
				filteredLayoutIssuesUpdated.add(issue);
			}
		}

		return filteredLayoutIssuesUpdated;
	}

}
