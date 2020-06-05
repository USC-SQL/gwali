package edu.usc.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.frameworkdetector.FrameworkDetector.Framework;
import edu.usc.languagedetectors.LanguageDetector.Language;
import edu.usc.layoutgraph.LayoutGraph;
import edu.usc.layoutgraph.LayoutGraphBuilder;
import edu.usc.layoutgraph.edge.NeighborEdge;
import edu.usc.layoutissue.FailureCategorizer;
import edu.usc.layoutissue.FailureCategorizer.FailureCateogry;
import edu.usc.layoutissue.Issue;

public class TestCaseResult implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static Logger logger = LoggerFactory.getLogger(TestCaseResult.class);
	
	private int baselineSize;
	private int putSize;

	private Framework framework;
	private Language baselineLanguage;
	private Language putLanguage;

	private String website;
	private String baselineUrl;
	private String putUrl;
	
	private ArrayList<Issue> reportedIssues;
	
	private boolean execFailed;
	
	private LayoutGraphBuilder layoutGraphs;

	private double structureMatchRatio;



	private long baselineTextSize;
	private long putTextSize;


	
	public TestCaseResult(int baselineSize, int putSize, long baselineTextSize, long putTextSize, Framework framework,
			Language baselineLanguage,Language putLanguage , String baselineUrl, String putUrl ,String website) {
		super();
		this.baselineSize = baselineSize;
		this.putSize = putSize;
		this.baselineTextSize = baselineTextSize;
		this.putTextSize = putTextSize;
		this.framework = framework;
		this.baselineLanguage = baselineLanguage;
		this.putLanguage = putLanguage;
		this.baselineUrl = baselineUrl;
		this.putUrl = putUrl;
		this.website = website;
		this.execFailed = false;
	}


	public int getBaselineSize() {
		return baselineSize;
	}


	public void setBaselineSize(int baselineSize) {
		this.baselineSize = baselineSize;
	}


	public int getPutSize() {
		return putSize;
	}


	public void setPutSize(int putSize) {
		this.putSize = putSize;
	}


	public Framework getFramework() {
		return framework;
	}


	public void setFramework(Framework framework) {
		this.framework = framework;
	}



	public String getBaselineUrl() {
		return baselineUrl;
	}




	public void setBaselineUrl(String baselineUrl) {
		this.baselineUrl = baselineUrl;
	}




	public String getPutUrl() {
		return putUrl;
	}




	public void setPutUrl(String putUrl) {
		this.putUrl = putUrl;
	}


	

	public float getNoOflayoutIssues() {
		return (1.0f*reportedIssues.size())/layoutGraphs.GetNoOfRelations();
	}
	
	public double getSeverityRatio(){
		double reportedIssues = getNoOflayoutIssues();
		LayoutGraph lg1 = layoutGraphs.getBaselineLG();
		LayoutGraph lg2 = layoutGraphs.getTestPageLG();
		
		ArrayList<NeighborEdge> baselineEdges = lg1.getEdges();
		ArrayList<NeighborEdge> putEdges = lg2.getEdges();
		
		int allRelations = 0;
		for (NeighborEdge neighborEdge : baselineEdges) {
			if(neighborEdge.isNearestNeighbor())
				allRelations += neighborEdge.getRelationshipsSize();
		}
		for (NeighborEdge neighborEdge : putEdges) {
			if(neighborEdge.isNearestNeighbor())
				allRelations += neighborEdge.getRelationshipsSize();
		}
		
		return reportedIssues / allRelations;
	}



	public boolean isExecFailed() {
		return execFailed;
	}


	public void setExecFailed(boolean hasFailed) {
		this.execFailed = hasFailed;
	}
	
	public double getMatchRatio() {
		return structureMatchRatio;
	}

	public void setMatchRatio(double matchRatio) {
		this.structureMatchRatio = matchRatio;
	}
	

	public ArrayList<Issue> getReportedIssues() {
		return reportedIssues;
	}


	public void setReportedIssues(ArrayList<Issue> reportedIssues) {
		this.reportedIssues = reportedIssues;
	}
	

	public LayoutGraphBuilder getLayoutGraphs() {
		return layoutGraphs;
	}


	public void setLayoutGraphs(LayoutGraphBuilder layoutGraphs) {
		this.layoutGraphs = layoutGraphs;
	}
	

	

	public Language getBaselineLanguage() {
		return baselineLanguage;
	}


	public void setBaselineLanguage(Language baselineLanguage) {
		this.baselineLanguage = baselineLanguage;
	}


	public Language getPutLanguage() {
		return putLanguage;
	}


	public void setPutLanguage(Language putLanguage) {
		this.putLanguage = putLanguage;
	}


	public String getWebsite() {
		return website;
	}


	public void setWebsite(String website) {
		this.website = website;
	}
	
	public long getBaselineTextSize() {
		return baselineTextSize;
	}


	public void setBaselineTextSize(long baselineTextSize) {
		this.baselineTextSize = baselineTextSize;
	}


	public long getPutTextSize() {
		return putTextSize;
	}


	public void setPutTextSize(long putTextSize) {
		this.putTextSize = putTextSize;
	}


	public void logResult(){
		logger.info("******************************************************");
		logger.info("Comparing: "+baselineUrl);
		logger.info("With     : "+putUrl);
		logger.info("Framework: "+framework);
		logger.info("Baseline Size: "+baselineSize);
		logger.info("PUT      Size: "+putSize);
		logger.info("TC Execution Failed: "+execFailed);
		if(!execFailed){
			logger.info("Match ratio: "+ structureMatchRatio);
			logger.info("Detected issues size: "+ getNoOflayoutIssues());
			logger.debug("Reported issues:");
			for (Issue issue : reportedIssues) {
				logger.debug(issue.toString());
			}
		}
		logger.info("******************************************************");

	}
	public void logResultForExcel(){
    	int overlapping = 0;
		int overflowing = 0;
		int alignment = 0;
		int movement = 0;
		FailureCategorizer categorizer = new FailureCategorizer();
		HashMap<NeighborEdge, NeighborEdge> inconsistentEdges = new HashMap<NeighborEdge, NeighborEdge>();
		for (Issue issue : reportedIssues) {
			inconsistentEdges.put(issue.getBaselineEdge(), issue.getPageUnderTestEdge());
		}
		for (NeighborEdge inconsistentEdge : inconsistentEdges.keySet()) {
			NeighborEdge baselineEdge = inconsistentEdge;
			NeighborEdge putEdge = inconsistentEdges.get(baselineEdge);
			Set<FailureCateogry> cateogries = categorizer.getCategories(baselineEdge, putEdge);
			for (FailureCateogry failureCateogry : cateogries) {
				if (failureCateogry == FailureCateogry.ALIGNMENT) {
					alignment++;
				}
				else if (failureCateogry == FailureCateogry.MOVEMENT) {
					movement++;
				}
				else if (failureCateogry == FailureCateogry.OVERLAPPING) {
					overlapping++;
				}
				else if (failureCateogry == FailureCateogry.OVERFLOWING) {
					overflowing++;
				}
			}
		}
		
		double sizeRatio = ((putTextSize - baselineTextSize) / baselineTextSize) * 100 ;
		layoutGraphs.getMatchRatio();
		
		
		
    	StringBuilder resultString = new StringBuilder();
    	resultString.append(website + " | " + baselineLanguage + " | ");
    	resultString.append(putLanguage + " | " );
    	resultString.append((getNoOflayoutIssues() > 0) + " | " );
    	resultString.append(getSeverityRatio() + " | " );
    	resultString.append(framework + " | " );
    	resultString.append(baselineSize + " | " );
    	resultString.append(putSize + " | " );
    	resultString.append(baselineTextSize + " | " );
    	resultString.append(putTextSize + " | " + sizeRatio + " | " );
		resultString.append(structureMatchRatio + " | " +overlapping+" | " +overflowing + " | " + alignment + " | " + movement);
		System.out.println(resultString.toString() );

	}
}
