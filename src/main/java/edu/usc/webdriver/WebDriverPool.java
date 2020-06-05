package edu.usc.webdriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.experiment.ExperimentMain;
import edu.usc.languagedetectors.LanguageDetector;
import edu.usc.util.Utils;


//Original Code written by Tim Büthe
//Obtained from: http://stackoverflow.com/questions/9822717/how-to-change-the-language-of-a-webdriver
//Edited and improved by Abdulmajeed Alameer alameer@usc.edu

public class WebDriverPool {
		final static Logger logger = LoggerFactory.getLogger(ExperimentMain.class);
	
	  private static final int DRIVER_RESTART = 10; //every 10 calls the driver need to be restarted
	  												// to avoid unreachable browser exception
	  
	  private static final long DRIVER_TIMEOUT = 10;
	  private Map<String, WebDriver> languagesDrivers = new HashMap<String, WebDriver>();
	  private WebDriver genericWebDriver = null;
	 
	  //how many times getWebDriver is requested before the last restart
	  private int count = 0; 
	  
	  
	  private List<WebDriver> driversInUse = new ArrayList<WebDriver>();

	  public WebDriverPool() {
		  Runtime.getRuntime().addShutdownHook(new Thread(){
			  @Override
		      public void run(){
		    	  if(genericWebDriver != null )
		    		  genericWebDriver.quit();
		    	  for (WebDriver driver : languagesDrivers.values())
		    		  driver.quit();
		        
		    	  if (!driversInUse.isEmpty())
		    		  throw new IllegalStateException("There are still drivers in use, did someone forget to return it? (size: " + driversInUse.size() + ")");
		      }
		  });
	  }

	 public FirefoxDriver createFirefoxDriver(Locale locale , boolean useScrapbook){
		FirefoxDriver fdriver = null;
		  
		if(useScrapbook){
			FirefoxProfile profile = new FirefoxProfile();

			try {
				profile.addExtension(Utils.getScrapbookExtentionFile(WebDriverPool.class));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			
			if(locale != null){
				profile.setPreference("intl.accept_languages", LanguageDetector.formatLocale(locale));
			}
			fdriver = new FirefoxDriver(profile);
		  }
		  else{
			  fdriver = new FirefoxDriver();
		  }
			fdriver.manage().timeouts().pageLoadTimeout(DRIVER_TIMEOUT, TimeUnit.SECONDS);
			fdriver.manage().timeouts().implicitlyWait(DRIVER_TIMEOUT, TimeUnit.SECONDS);
			fdriver.manage().window().maximize();
			return fdriver;
	  }
	 
	 public WebDriver createFirefoxDriver(){
		 return createFirefoxDriver(null,false);
	 }
	 public WebDriver createFirefoxDriver(Locale locale){
		 return createFirefoxDriver(locale,false);
	 }



	  /**
	   * @param clazz
	   * @param locale
	   * @return web driver which can be new or recycled
	   */
	  public synchronized WebDriver getLanguageWebDriver(Class<? extends WebDriver> clazz, Locale locale){
		  

		  
	    String key = clazz.getName() + "-" + locale;

	    if(!languagesDrivers.containsKey(key)){

	      if(clazz == FirefoxDriver.class){
	        languagesDrivers.put(key, createFirefoxDriver(locale));
	      }

	      // TODO create other drivers here ...

	      // else if(clazz == ChromeDriver.class){
	      //     drivers.put(key, createChromeDriver(locale));
	      // }

	      else{
	        throw new IllegalArgumentException(clazz.getName() + " not supported yet!");
	      }
	    }

	    WebDriver driver = languagesDrivers.get(key);
	    

	    
	    if(driversInUse.contains(driver))
	      throw new IllegalStateException("This driver is already in use. Did someone forgot to return it?");

		if(count >= DRIVER_RESTART){
			driver = restartDriver(driver, clazz, locale);
			languagesDrivers.put(key, driver);
			count = 0;
		}
	    
	    driversInUse.add(driver);
	    
	    count++;
	    return driver;
	  }
	  
	  /**
	   * @param clazz
	   * @param locale
	   * @return web driver which can be new or recycled
	   */
	  public synchronized WebDriver getGenericWebDriver(Class<? extends WebDriver> clazz){
	
		    if(genericWebDriver == null){
	
		    	if(clazz == FirefoxDriver.class){
		    		genericWebDriver = createFirefoxDriver(null);
			    }

				// TODO create other drivers here ...
				
				// else if(clazz == ChromeDriver.class){
				//     genericWebDriver = createChromeDriver(locale));
				// }
				
				else{
					throw new IllegalArgumentException(clazz.getName() + " not supported yet!");
				}
		    }
	
		    
		    
		    if(driversInUse.contains(genericWebDriver))
		    	throw new IllegalStateException("This driver is already in use. Did someone forgot to return it?");
	
			if(count >= DRIVER_RESTART){
				genericWebDriver = restartDriver(genericWebDriver, clazz, null);
				count = 0;
			}

		    driversInUse.add(genericWebDriver);
		    count++;
		    return genericWebDriver;
	  }

	  

	  private WebDriver restartDriver(WebDriver webDriver , Class<? extends WebDriver> clazz, Locale locale) {
		  webDriver.quit();
		  if(clazz == FirefoxDriver.class){
			  webDriver = createFirefoxDriver(locale);
		  }else{
			  throw new IllegalArgumentException(clazz.getName() + " not supported yet!");
		  }
		  
		  
		  return webDriver;
	  }

	public synchronized void returnWebDriver(WebDriver driver){
	    driversInUse.remove(driver);
	  }
	}