package edu.usc.subjectsfilter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.config.Config;
import edu.usc.experiment.ExperimentMain;
import edu.usc.frameworkdetector.FrameworkDetector;
import edu.usc.frameworkdetector.FrameworkDetector.Framework;
import edu.usc.languagedetectors.LanguageDetector;
import edu.usc.languagedetectors.LanguageDetector.Language;
import edu.usc.util.Utils;

public class SubjectsFilter {
	
	final static Logger logger = LoggerFactory.getLogger(SubjectsFilter.class);

	public static String[] allUrls = Utils.getPkgFileContents(ExperimentMain.class, Config.ALL_URLS_FILE ).split("\n");
	
	public static void filterURLSThreaded() {
		int  corePoolSize  =    50;
		int  maxPoolSize   =   50;
		long keepAliveTime = Long.MAX_VALUE;

		LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
		
		ExecutorService threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,maxPoolSize,keepAliveTime,TimeUnit.NANOSECONDS,tasks);
		for (int i = 0; i < allUrls.length; i++) {
			threadPoolExecutor.execute(new I18nFilterTask(allUrls[i],i));
		}
		
	}
	
	public static void filterURLSUsingHTTPClient() {
		FrameworkDetector frameworkDetector = new FrameworkDetector();
		LanguageDetector languageDetector = Utils.getLanguageDetector();
		
		for (int i = 0; i < allUrls.length; i++) {
			try{
			    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Config.FILTERED_URLS_FILE, true)));
				logger.info(i+" - testing url:"+allUrls[i]);
				Language baselineLang = null;
				Framework fw = null;
				String html = Utils.getPageSource(allUrls[i], null);

				fw = frameworkDetector.DetectFramework(html);
				
				String tld = Utils.getTLD(allUrls[i]);
				String language = languageDetector.DetectLanguage(html, tld);
				baselineLang = languageDetector.getLangfromNameOrCode(language);
				
				
				if(fw != Framework.UNKNOWN){
					out.println(allUrls[i]);
					out.flush();
					logger.info("saving url... detected framework "+fw+" for URL: "+allUrls[i]);
					continue;
				}
				for (Language lang : Language.values()) {
					//skip the default language
					if(lang == Language.UNKNOWN || lang == baselineLang)
						continue;
					
					String languageCode = languageDetector.getLangCode(lang);
					Locale locale = new Locale(languageCode);
					html = Utils.getPageSource(allUrls[i], locale);
					
					language = languageDetector.DetectLanguage(html, tld);
					
					Language detectedLang = languageDetector.getLangfromNameOrCode(language);
					if(detectedLang != baselineLang){
						out.println(allUrls[i]);
						out.flush();
						logger.info("saving url... detected different language "+detectedLang+" for URL: "+allUrls[i]);
						break;
					}

				}
			out.close();
			}catch(Exception e){
				e.printStackTrace();
				logger.error("error on testing test case "+i);
			}
		}

		
	}
	



}
