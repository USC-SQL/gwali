package edu.usc.experiment;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.config.Config;
import edu.usc.frameworkdetector.FrameworkDetector;
import edu.usc.frameworkdetector.FrameworkDetector.Framework;
import edu.usc.issuesfilter.LayoutIssuesFilterProcessor;
import edu.usc.languagedetectors.LanguageDetector;
import edu.usc.languagedetectors.LanguageDetector.Language;
import edu.usc.languagedetectors.WebPageTextExtractor;
import edu.usc.layoutgraph.LayoutGraphBuilder;
import edu.usc.layoutissue.Issue;
import edu.usc.results.AnalysisResultSummary;
import edu.usc.results.TestCaseResult;
import edu.usc.util.Utils;
import edu.usc.webdriver.WebDriverPool;


public class ExperimentMain {

	final static Logger logger = LoggerFactory.getLogger(ExperimentMain.class);
	
	public static FrameworkDetector frameworkDetector = new FrameworkDetector();
	public static WebDriverPool webdrivers = new WebDriverPool();
	public static LanguageDetector languageDetector = Utils.getLanguageDetector();
	
	public static void startAnalysis() throws IOException{
		String[] urls = 
				Utils.getPkgFileContents(ExperimentMain.class, Config.FILTERED_URLS_FILE )
				.split("\n");

		FileOutputStream fos = new FileOutputStream(Config.ANALYSIS_RESULTS_FILE , false);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		final int startTC = 381;
		final int endTC = Config.NO_OF_SUBJECTS;
		
		logger.info("NO_OF_SUBJECTS: "+ (endTC - startTC));
		AnalysisResultSummary resultSummary = new AnalysisResultSummary();
		for (int i = startTC; i < endTC; i++) {
			
			if(i != 0 && i%20 == 0){
				resultSummary.printResultSummary();
			}
			String baseline = null;
			ArrayList<String> translated = new ArrayList<String>();

			ArrayList<File> testCaseFolders = Utils.getTestCasesFolders(i);

			for (File folder : testCaseFolders) {
				if(folder.getName().contains("baseline")){
					baseline = folder.getAbsolutePath();
				}
				else{
					translated.add(folder.getAbsolutePath());
				}
			}
			if(testCaseFolders.size() == 0 || baseline == null || translated.size() < 1){
				logger.info("skipping TC_"+i+" only one language avaliable...");
				continue;
			}
			
			String baselineUrl = "file://"+baseline+"/index.html";
			for (int j = 0; j < translated.size(); j++) {
				String putUrl = "file://"+translated.get(j)+"/index.html";
				FirefoxDriver fdriver1 = (FirefoxDriver) webdrivers.createFirefoxDriver();
				FirefoxDriver fdriver2 = (FirefoxDriver) webdrivers.createFirefoxDriver();
				
				fdriver1.get(baselineUrl);
				fdriver2.get(putUrl);
				String baselineSrc = fdriver1.getPageSource();
				String putSrc = fdriver2.getPageSource();
				
				Framework framework = frameworkDetector.DetectFramework(baselineSrc);
				if(framework == Framework.UNKNOWN)
					framework = frameworkDetector.DetectFramework(putSrc);
				
				int baselineTextSize = WebPageTextExtractor.extractText(baselineSrc).length();
				int putTextSize = WebPageTextExtractor.extractText(putSrc).length();

				
				int baselineSize = fdriver1.findElementsByXPath(Config.ALL_ELEMENTS_XPATH).size();
				int putSize      = fdriver2.findElementsByXPath(Config.ALL_ELEMENTS_XPATH).size();
				String website = urls[i];
				
				Language baselinelanguage = Utils.getLangFromDirName(baseline);
				Language putlanguage = Utils.getLangFromDirName(translated.get(j));
				
				TestCaseResult result = new TestCaseResult(baselineSize, putSize,
						baselineTextSize,putTextSize,
						framework, baselinelanguage, 
						putlanguage, baselineUrl, putUrl, 
						website);
				
				
				try {
					//compare graphs
					LayoutGraphBuilder lgb = new LayoutGraphBuilder(fdriver1, fdriver2);
					double matchRatio = lgb.getMatchRatio();

					
					result.setMatchRatio(matchRatio);
					ArrayList<Issue> potentialLayoutIssues = lgb.compareLayoutGraphs();
					
					result.setLayoutGraphs(lgb);
					
					//filter issues
					LayoutIssuesFilterProcessor filter = new LayoutIssuesFilterProcessor();
					ArrayList<Issue> filteredLayoutIssues = filter.filterissues(potentialLayoutIssues);
					
					ArrayList<String> filteredLayoutIssuesString = new ArrayList<String>();
					for(Issue issue : filteredLayoutIssues){
						filteredLayoutIssuesString.add(issue.toString());
					}
					
					result.setReportedIssues(filteredLayoutIssues);

				} catch (Exception e) {
					result.setExecFailed(true);
					logger.error("Error in testing TC_"+i+" language_"+j);
				}
				result.logResult();
				saveTCResult(result, oos);
				resultSummary.AddTCResult(result);
				fdriver1.quit();
				fdriver2.quit();
				System.gc();
			}
		}
		closeObjectOutputStream(oos);
		resultSummary.printResultSummary();
	}
	


	public static void main(String[] args) {
		try{
			startAnalysis();
		}catch(IOException e){
			e.printStackTrace();
		}
		//SubjectsFilter.filterURLSThreaded();
		//SubjectsSaver ss = new SubjectsSaver(webdrivers, languageDetector, frameworkDetector);
		//ss.SavePages("/URLS/filteredURLS.txt");		
	}
	
	
	public static void saveTCResults(ArrayList<TestCaseResult> tcResults){
		// write object to file
		boolean append = false;
		try {
			FileOutputStream fos = new FileOutputStream(Config.ANALYSIS_RESULTS_FILE , append);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(tcResults);
			oos.close();
		}catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public static void saveTCResult(TestCaseResult tcResult , ObjectOutputStream oos){
		// write object to file
		try {
			oos.writeObject(tcResult);
			oos.flush();
		}catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public static void closeObjectOutputStream(ObjectOutputStream oos){

		try {
			oos.close();
		}catch (IOException e) {
			logger.error(e.getMessage());
		}
	}


}
