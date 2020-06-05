package edu.usc.subjectsfilter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.config.Config;
import edu.usc.frameworkdetector.FrameworkDetector;
import edu.usc.frameworkdetector.FrameworkDetector.Framework;
import edu.usc.languagedetectors.LanguageDetector;
import edu.usc.languagedetectors.LanguageDetector.Language;
import edu.usc.util.Utils;
import edu.usc.webdriver.CustomizedHtmlUnitDriver;

public class I18nFilterTask implements Runnable{
	
	
		final static Logger logger = LoggerFactory.getLogger(I18nFilterTask.class);
	   	private Thread t;
	    //set as true to be a daemon thread and therefore exit on interrupt
	   	Timer timer = new Timer(true);
	   	
		private static LanguageDetector languageDetector = Utils.getLanguageDetector();
		private static FrameworkDetector frameworkDetector = new FrameworkDetector();
		private CustomizedHtmlUnitDriver driver = new CustomizedHtmlUnitDriver(false);
		private String url;
		
		private int taskNo;
					
		public static List<String> filteredrurls = Collections.synchronizedList(new ArrayList<String>());
		
		public I18nFilterTask( String url, int tNo){
	       this.url = url;
	       this.taskNo = tNo;
	   	}
	   
		public void run() {
			taskNo ++;
			boolean i18nDetected = false;
			TimeOutTask timeout = new TimeOutTask(Thread.currentThread());
			timer.schedule(timeout, Config.FILTERING_TASK_TIMEOUT);
			try {
				logger.info(taskNo+"- testing url: "+url);
				Language baselineLang = null;
				Framework fw = null;
				
				String	html = getPageSource(url, null);
				
				fw = frameworkDetector.DetectFramework(html);
				
				String tld = Utils.getTLD(url);
				String language = languageDetector.DetectLanguage(html, tld);
				baselineLang = languageDetector.getLangfromNameOrCode(language);
				
				
				if(fw != Framework.UNKNOWN){
					i18nDetected = true;
					logger.info(taskNo+"- saving url... detected framework "+fw+" for URL: "+url);
				}
				else{
					for (Language lang : Language.values()) {
						
						boolean hasQuit = driver.toString().contains("(null)");
						if(hasQuit)
							break;
						
						//skip the default language
						if(lang == Language.UNKNOWN || lang == baselineLang)
							continue;

						
						String languageCode = languageDetector.getLangCode(lang);
						Locale locale = new Locale(languageCode);
						html = getPageSource(url, locale);
						
						language = languageDetector.DetectLanguage(html, tld);
						
						Language detectedLang = languageDetector.getLangfromNameOrCode(language);
						if(detectedLang != baselineLang){
							i18nDetected = true;
							logger.info(taskNo+"- saving url... detected different language "+detectedLang+" for URL: "+url);
							break;
						}
	
					}
				}
			} catch (IOException e) {
				timeout.cancel();
				timer.cancel();
				logger.error(taskNo+"- "+e.getMessage());
				logger.error(taskNo+"- error in testing url: "+url);
			}

			
			if(i18nDetected){
				filteredrurls.add(url);
				writeURLToFile(url);
			}
			timeout.cancel();
			timer.cancel();
			logger.info(taskNo+"- done from url: "+url);
			logger.info("size of filtered list:" + filteredrurls.size());
	   }
	
	      
	   
	   public void start ()
	   {
	      if (t == null)
	      {
	         t = new Thread (this);
	         t.start ();
	      }
	   }
	   
	   private synchronized void writeURLToFile(String url){
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Config.FILTERED_URLS_FILE, true)));
				out.println(url);
				out.flush();
				out.close();
			} catch (IOException e) {
				logger.error("Error in synchronized method: could not write url "+url+" to the file:"+e.getMessage());
			}

	   }
	   
	   
	private String getPageSource(String url, Locale locale) throws IOException{
			String html = "";
			
			driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
			if(locale != null){
				driver.setHeader("Accept-Language", LanguageDetector.formatLocale(locale));
			}
			driver.get(url);
			html = driver.getPageSource();
			return html;
	}
	
	class TimeOutTask extends TimerTask{
		Thread t;
		TimeOutTask(Thread t){
			this.t = t;
	    }
	 
	    public void run(){
	    	if(t!= null && t.isAlive() && driver != null && !driver.toString().contains("(null)")){
	    		logger.error("**********Getting source of URL taking too long will close the driver: "+driver.getCurrentUrl());
	    		driver.close();
	    	}
	    }
	}

	
}
