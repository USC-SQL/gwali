package edu.usc.issuesfilter;

import java.util.ArrayList;

import edu.usc.config.Config;
import edu.usc.layoutgraph.LayoutGraphComparator;
import edu.usc.layoutissue.Issue;

public class DirectionIssueFilter implements LayoutIssuesFilter {


	@Override
	public ArrayList<Issue> filter(ArrayList<Issue> issues) {
		ArrayList<Issue> filteredIssues = new ArrayList<>();
		
		for (Issue issue : issues) {
			if(issue.isDirectionIssue()){				
				double baselineAngle = issue.getBaselineEdge().getAngleDegree();
				double PUTAngle = issue.getPageUnderTestEdge().getAngleDegree();
				double angelDiff = LayoutGraphComparator.AngleDiff(baselineAngle, PUTAngle);
				if(angelDiff > Config.ANGEL_THRISHOLD){
					filteredIssues.add(issue);
					
				}
				else{
					continue;
				}
			}
			else{ // not direction issue
				filteredIssues.add(issue);
			}
		}
		
		return filteredIssues;

	}
	
	

}
