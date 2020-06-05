package edu.usc.issuesfilter;

import java.util.ArrayList;

import edu.usc.layoutissue.Issue;
import edu.usc.layoutissue.Issue.IssueType;

public class CenteredIssueFilter implements LayoutIssuesFilter{

	@Override
	public ArrayList<Issue> filter(ArrayList<Issue> issues) {
		ArrayList<Issue> filteredLayoutIssues = new ArrayList<>();
		for (Issue issue : issues) {
			if(issue.getIssueType() != IssueType.CENTERED)
			{
				filteredLayoutIssues.add(issue);
			}
		}

		return filteredLayoutIssues;

	}

}
