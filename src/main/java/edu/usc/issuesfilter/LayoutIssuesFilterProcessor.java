package edu.usc.issuesfilter;

import java.util.ArrayList;

import edu.usc.layoutissue.Issue;

public class LayoutIssuesFilterProcessor {

	private ArrayList<LayoutIssuesFilter> filters;
	private AlignmentIssueFilter alignmentFilter;
	private ContainmentIssueFilter containmentFilter;
	private DirectionIssueFilter directionFilter;
	
	public LayoutIssuesFilterProcessor(){
		//adding Filters issues
		filters = new ArrayList<LayoutIssuesFilter>();
	}
	
	public void addFilter(LayoutIssuesFilter filter){
		filters.add(filter);
	}
	
	public ArrayList<Issue> filterissues(ArrayList<Issue> potentialLayoutIssues){
		ArrayList<Issue> filteredLayoutIssues = new ArrayList<>();
		
		for (LayoutIssuesFilter filter : filters) {
			potentialLayoutIssues = filter.filter(potentialLayoutIssues);
		}
		filteredLayoutIssues = potentialLayoutIssues;
		return filteredLayoutIssues;
	}
	
}
