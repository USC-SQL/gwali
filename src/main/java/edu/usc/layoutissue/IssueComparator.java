package edu.usc.layoutissue;

import java.util.Comparator;


public class IssueComparator implements Comparator<Issue> {


	@Override
	public int compare(Issue issue1, Issue issue2) {
		return issueTypeScore(issue2) - issueTypeScore(issue1);
	}
	
	public int issueTypeScore(Issue issue){
		
		if (issue.isDirectionIssue()) 
			return 100;
		else if (issue.isContainmentIssue()) 
			return 90;
		else 
			return 80;
	}

}
