package edu.usc.results;

import java.util.HashMap;
import java.util.Map;

import edu.usc.frameworkdetector.FrameworkDetector.Framework;
import edu.usc.languagedetectors.LanguageDetector.Language;

public class AnalysisResultSummary {
	private Map<Language, Integer> langTestCount = new HashMap<Language, Integer>();
	private Map<Language, Integer> langFailureCount = new HashMap<Language, Integer>();
	
	private Map<Framework, Integer> frameworkTestCount = new HashMap<Framework, Integer>();
	private Map<Framework, Integer> frameworkFailureCount = new HashMap<Framework, Integer>();


	public AnalysisResultSummary(){
		//init all languages and frameworks with zero
		for (Language lang : Language.values()) {
			langTestCount.put(lang, 0);
			langFailureCount.put(lang, 0);
		}
		for(Framework fw : Framework.values()) {
			frameworkTestCount.put(fw, 0);
			frameworkFailureCount.put(fw, 0);
		}
	}
	
	public void AddTCResult(TestCaseResult TCResult){
		if(!TCResult.isExecFailed()){
			Language lang = TCResult.getPutLanguage();
			Framework fw = TCResult.getFramework();
			increaseFrameworkTestCount(fw);
			increaseLangTestCount(lang);
			if(TCResult.getNoOflayoutIssues() > 0){
				increaseFrameworkFailureCount(fw);
				increaseLangFailureCount(lang);
			}
		}
	}
	
	
	public int getLangTestCount(Language lang){
		return langTestCount.get(lang);
	}

	public int getLangFailureCount(Language lang){
		return langFailureCount.get(lang);
	}
	
	private void increaseLangTestCount(Language lang){
		int old = langTestCount.get(lang);
		langTestCount.put(lang, old+1);
	}

	
	private void increaseLangFailureCount(Language lang){
		int old = langFailureCount.get(lang);
		langFailureCount.put(lang, old+1);
	}
	
	public int getFrameworkTestCount(Framework fw){
		return frameworkTestCount.get(fw);
	}
	
	
	public int getFrameworkFailureCount(Framework fw){
		return frameworkFailureCount.get(fw);
	}
		
	private void increaseFrameworkTestCount(Framework fw){
		int old = frameworkTestCount.get(fw);
		frameworkTestCount.put(fw, old+1);
	}
	
	private void increaseFrameworkFailureCount(Framework fw){
		int old = frameworkFailureCount.get(fw);
		frameworkFailureCount.put(fw, old+1);
	}


	public void printResultSummary(){
		System.out.println("results summary:");
		System.out.println("language "+"|"+" test count "+"|"+"fail count");

		for (Language lang : Language.values()) {
			System.out.println(lang+"|"+langTestCount.get(lang)+"|"+langFailureCount.get(lang));

		}
		System.out.println("--------------------------------------");

		System.out.println("framework "+"|"+" test count "+"|"+"fail count");
		for(Framework fw : Framework.values()) {
			System.out.println(fw+"|"+frameworkTestCount.get(fw)+"|"+frameworkFailureCount.get(fw));
		}
	}
	
	
	
}
