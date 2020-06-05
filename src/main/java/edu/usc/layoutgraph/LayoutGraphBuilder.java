package edu.usc.layoutgraph;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.xpert.dom.DomNode;
import edu.gatech.xpert.dom.JsonParser;
import edu.gatech.xpert.dom.MatchResult;
import edu.gatech.xpert.dom.Matcher;
import edu.usc.config.Config;
import edu.usc.layoutgraph.edge.NeighborEdge;
import edu.usc.layoutissue.Issue;
import edu.usc.util.Utils;


public class LayoutGraphBuilder implements Serializable{

	/*
	 *  
	 */
	private static final long serialVersionUID = 1L;
	
		private Map<String, DomNode> doms;
		private MatchResult matchResult; // stores only xpath to xpath mapping
		private Map<DomNode, DomNode> matchedNodes; //stores DomNode to DomNode mapping
		private LayoutGraph baseLG;
		private LayoutGraph putLG;
		
		//contains all elements for the baseline and put including non layout Elements;
		private Map<String,DomNode> baselineElements;
		private Map<String,DomNode> testpageElements;
		

		final static Logger logger = LoggerFactory.getLogger(LayoutGraphBuilder.class);

		
		private final String[] keys = {"url1","url2"};

		private String url1;
		private String url2;
		
		
		public LayoutGraphBuilder(FirefoxDriver driver1, FirefoxDriver driver2){
			init(driver1, driver2);
		}
		
		
		
		public LayoutGraphBuilder(String url1, String url2) {
			FirefoxDriver driver1 =  (FirefoxDriver) Utils.getNewFirefoxDriver();
			FirefoxDriver driver2 =  (FirefoxDriver) Utils.getNewFirefoxDriver();			
			// URL1
			driver1.manage().window().maximize();
			driver1.get(url1);
			
			// URL2
			driver2.manage().window().maximize();
			driver2.get(url2);

			init(driver1,driver2);
			driver1.close();
			driver2.close();
		}
		
		public void init(FirefoxDriver driver1, FirefoxDriver driver2) {

			url1 = driver1.getCurrentUrl();
			url2 = driver2.getCurrentUrl();

			
			String domStr1 = extractPageMBRs(driver1);
			String domStr2 = extractPageMBRs(driver2);
			
			this.doms = new HashMap<String,DomNode>();
			
			// maps each key to the root node of the page
			DomNode baselineRoot = loadDom(domStr1,url1);
			DomNode putRoot = loadDom(domStr2,url2);
			
			this.doms.put(keys[0], baselineRoot);
			this.doms.put(keys[1], putRoot);
			
			//initialize lists of baseline and put elements
			this.baselineElements = getElementSetFromDomTree(baselineRoot);
			this.testpageElements = getElementSetFromDomTree(putRoot);




			//creates xpath to xpath matching and saves it in MatchResult object
			this.matchResult = matchDoms(doms);
			this.matchedNodes = matchResult.getMatchedNodes(baselineElements, testpageElements);
			
			//create layoutgraph objects
			this.baseLG = new LayoutGraph(baselineRoot);
			this.putLG = new LayoutGraph(putRoot);
			
		}
		
		public void changePUT(FirefoxDriver driver2) {
			this.url2 = driver2.getCurrentUrl();
			String mbrsStr = extractPageMBRs(driver2);
			DomNode putRoot = loadDom(mbrsStr,url2);
			this.doms.put(keys[1], putRoot);
			this.testpageElements = getElementSetFromDomTree(putRoot);
			this.putLG = new LayoutGraph(putRoot);
			this.matchedNodes = matchResult.getMatchedNodes(baselineElements, testpageElements);
			
		}




		public MatchResult matchDoms(Map<String, DomNode> doms) {
						
			Matcher matcher = new Matcher();
			return matcher.doMatch(doms, keys);			
		}
		
		private Map<String,DomNode> getElementSetFromDomTree(DomNode root) {
			Map<String,DomNode> pageElements = new HashMap<String,DomNode>();
			List<DomNode> worklist = new ArrayList<>();
			worklist.add(root);

			// Populate Nodes
			while (!worklist.isEmpty()) {
				DomNode node = worklist.remove(0);
				
				// This code is to normalize the xpath, so they are all lower case and indexed
				// TODO: remove this code once we update getMBR to report normalized xpaths from the beginning
				if(Config.normalizePath){
					String xpath = Utils.normalizeXPATH(node.getxPath());
					node.setxPath(xpath);
				}
				pageElements.put(node.getxPath(),node);
				worklist.addAll(node.getChildren());
			}
			return pageElements;
		}

		

		public double getMatchRatio(){
			return matchResult.getMatchRatio();
		}
		
		public ArrayList<Issue> compareLayoutGraphs()  {
			

			Map<String, String> matchedXpaths = matchResult.getMatched();
			
			for (String baseXPath : matchedXpaths.keySet()) {
				logger.debug(baseXPath + " <is matched with> " + matchedXpaths.get(baseXPath));
			}
			//TimeMeasurement.toc();
			//TimeMeasurement.tic("BuildingGraphs");
			
			

			LayoutGraph lg1 = baseLG;
			LayoutGraph lg2 = putLG;



			// handle RTL languages by converting putLG;
			if(Config.CHECK_RTL){
				if(isMirrored(baseLG, putLG)){
					convertRTLPutLayoutGraph(baseLG,putLG);
				}
			}
			printLayoutGraph(lg1);
			printLayoutGraph(lg2);
			
			//TimeMeasurement.toc();
			//TimeMeasurement.tic("ComparingGraphs");

			
			LayoutGraphComparator lgsComparator = new LayoutGraphComparator(matchedNodes, lg1, lg2);
			
			ArrayList<Issue> layoutIssues = new ArrayList<Issue>();

			layoutIssues = lgsComparator.compareGraphs();
		
			return layoutIssues;
		}
		
		
		private void printLayoutGraph(LayoutGraph lg){
			logger.debug("***** LAYOUT GRAPH *****");
			logger.debug(lg.toString());
		}


		public void convertRTLPutLayoutGraph(LayoutGraph baseLG, LayoutGraph putLG){
			LayoutGraphComparator modelComparator = new LayoutGraphComparator(matchedNodes, baseLG, putLG);
			ArrayList<NeighborEdge> excludedEdges = modelComparator.getNonMirroredEdgesinPUT();
			System.out.println("Page under test is RTL, now converting the layoutgraph..");
			LayoutGraphConverter lgConverter = new LayoutGraphConverter();
			lgConverter.convertLayoutGraph(putLG,excludedEdges);
		}



		public DomNode loadDom(String domStr,String url) {
			JsonParser parser = new JsonParser();
			DomNode domRootNode = parser.parseJson(domStr,url);
			return domRootNode;
		}
		
		

	public String extractPageMBRs(JavascriptExecutor driver1){

		String script = Utils.getMBRsExtractionScript();
		String MBRsStr = (String) driver1.executeScript(script);
		//System.out.println(MBRsStr);
		return MBRsStr;
	}
	
	public int GetNoOfRelations(){
		int allRelations = 0;
		LayoutGraph lg1 = new LayoutGraph(doms.get(keys[0])), 
				lg2 = new LayoutGraph(doms.get(keys[1]));

		ArrayList<NeighborEdge> baselineEdges = lg1.edges;
		ArrayList<NeighborEdge> putEdges = lg2.edges;
		for (int i = 0; i < 2; i++) {
			ArrayList<NeighborEdge> lgEdges;
			if(i == 0)
				lgEdges = baselineEdges;
			else
				lgEdges = putEdges;
			
			for (NeighborEdge neighborEdge : lgEdges) {
				if(neighborEdge.isNearestNeighbor()){
					if(neighborEdge.isContains())
						allRelations++;
					if(neighborEdge.isIntersect())
						allRelations++;
					if(neighborEdge.isLeftEdgeAligned())
						allRelations++;
					if(neighborEdge.isRightEdgeAligned())
						allRelations++;
					if(neighborEdge.isTopEdgeAligned())
						allRelations++;
					if(neighborEdge.isBottomEdgeAligned())
						allRelations++;
					if(neighborEdge.isLeftRight())
						allRelations++;
					if(neighborEdge.isRightLeft())
						allRelations++;
					if(neighborEdge.isTopBottom())
						allRelations++;
					if(neighborEdge.isBottomTop())
						allRelations++;
				}
					
			}

		}
		return allRelations;
	}
	
	public boolean isMirrored(LayoutGraph baseLine, LayoutGraph put){
		int matchingRelations = 0;
		int mirroredRelations = 0;
		boolean isMirrored = false;
		Map<DomNode, DomNode> matchMap = matchedNodes;
		
		for (NeighborEdge baseEdge : baseLine.getEdges()) {
			NeighborEdge putEdge = baseEdge.getMatchedEdge(matchMap, put);
			if(putEdge == null)
				continue;
			if( baseEdge.isRightLeft() && putEdge.isRightLeft())
				matchingRelations++;
			if(baseEdge.isLeftRight() && putEdge.isLeftRight())
				matchingRelations++;
			if( baseEdge.isRightLeft() && putEdge.isLeftRight())
				mirroredRelations++;
			if(baseEdge.isLeftRight() && putEdge.isRightLeft())
				mirroredRelations++;
		}
		
		isMirrored = mirroredRelations > matchingRelations;
		
		return isMirrored;
		
	}
	
	
	public LayoutGraph getBaselineLG(){
		return baseLG;
	}
	
	public LayoutGraph getTestPageLG(){
		return putLG;
	}
	
	public ArrayList<DomNode> getBaselineElements() {
		ArrayList<DomNode> baseElements = new ArrayList<DomNode>(baselineElements.values());
		return baseElements;
	}

	public ArrayList<DomNode> getTestpageElements() {
		ArrayList<DomNode> testElements = new ArrayList<DomNode>(testpageElements.values());
		return testElements;
	}
	
	public Map<DomNode, DomNode> getMatchedNodes(){
		return this.matchedNodes;
	}





}


