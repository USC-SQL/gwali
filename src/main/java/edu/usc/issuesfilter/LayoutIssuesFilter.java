package edu.usc.issuesfilter;

import java.util.ArrayList;

import edu.usc.layoutissue.Issue;

public interface LayoutIssuesFilter {
	public abstract ArrayList<Issue> filter(ArrayList<Issue> issues);
	
}
