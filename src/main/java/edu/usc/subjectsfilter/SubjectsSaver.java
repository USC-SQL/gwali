package edu.usc.subjectsfilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.config.Config;
import edu.usc.experiment.ExperimentMain;
import edu.usc.frameworkdetector.FrameworkDetector;
import edu.usc.frameworkdetector.FrameworkDetector.Framework;
import edu.usc.languagedetectors.LanguageDetector;
import edu.usc.languagedetectors.LanguageDetector.Language;
import edu.usc.util.Utils;
import edu.usc.webdriver.WebDriverPool;

public class SubjectsSaver {

	final static Logger logger = LoggerFactory.getLogger(SubjectsSaver.class);
	
	public LanguageDetector languageDetector;
	public FrameworkDetector frameworkDetector;
	public WebDriverPool webdrivers;

	public SubjectsSaver(WebDriverPool webdrivers, LanguageDetector languageDetector, FrameworkDetector frameworkDetector){
		this.webdrivers = webdrivers;
		this.languageDetector = languageDetector;
		this.frameworkDetector = frameworkDetector;
	}
	
	
	public void savePage(FirefoxDriver fdriver1, String saveLocation){
		performSaveAction(fdriver1);
		moveSavedFileToDrive(saveLocation);
	}
	
	
	private void moveSavedFileToDrive(String folderName) {
		File temp = new File(Config.SYSTEM_TEMP_FOLDER);
		
	    FileFilter profileFilter = new SuffixFileFilter("webdriver-profile");
	    FileFilter ScrapbookFolderFilter = new NameFileFilter("ScrapBook");
	    FileFilter ScrapbookDataFolderFilter = new NameFileFilter("data");
	    File newestProfile = null;
	    File scrapBookFolder = null;
	    File scrapBookDataFolder = null;
	    File ToBeSavedFolder = null;
	    File distFolder = new File(folderName);
	    try {
			newestProfile = Utils.getNewestChildFile(temp, profileFilter);
		    scrapBookFolder = Utils.getNewestChildFile(newestProfile,ScrapbookFolderFilter);
		    scrapBookDataFolder = Utils.getNewestChildFile(scrapBookFolder, ScrapbookDataFolderFilter);
		    ToBeSavedFolder = Utils.getNewestChildFile(scrapBookDataFolder,null);
			FileUtils.moveDirectory(ToBeSavedFolder, distFolder);
			logger.info("Saved folder to disk "+folderName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void performSaveAction(FirefoxDriver fdriver) {
		Actions action = new Actions(fdriver);
		Keys ctrlkey = System.getProperty("os.name").equals("Mac OS X") ? Keys.COMMAND : Keys.CONTROL;
		action.keyDown(ctrlkey).keyDown(Keys.SHIFT).sendKeys("q").keyUp(Keys.SHIFT).keyUp(Keys.CONTROL);
		action.perform();
	}
	
	public void SavePages(String URLsFile){

		String[] urls = Utils.getPkgFileContents(ExperimentMain.class, URLsFile ).split("\n");
		for (int i = 0; i < urls.length; i++) {
			try{
				FirefoxDriver genericDriver = (FirefoxDriver) webdrivers.createFirefoxDriver(null,true);
				genericDriver.get(urls[i]);
				Language baselineLang = getPageLanguage(genericDriver);
				
				String baselineLanguageCode = languageDetector.getLangCode(baselineLang);
				
				String subjectFolderName = Config.SUBJECT_FOLDER_PREFIX+i+"_"+"baseline"+"_"+baselineLanguageCode;
				
				savePage(genericDriver,Config.SAVED_WEBPAGES_FOLDER+"/"+subjectFolderName);
				genericDriver.quit();

				for (Language requestingLang : Language.values()) {
					//skip default language and UNKNOWN language
					if(requestingLang == Language.UNKNOWN || requestingLang == baselineLang)
						continue;
					
				
					String languageCode = languageDetector.getLangCode(requestingLang);
					Locale locale = new Locale(languageCode);
					FirefoxDriver fdriver = (FirefoxDriver) webdrivers.createFirefoxDriver(locale,true);
					fdriver.get(urls[i]);
					Framework fw = getPageFramework(fdriver, frameworkDetector);
					if(fw == Framework.GOOGLETRANSLATE ){
						setCookiesForDriver(fdriver, fw, requestingLang);
						fdriver.get(urls[i]);
						waitForGTransToFinsih(fdriver);
					}
					if(fw == Framework.MOTIONPOINT || fw == Framework.WORDPRESS){
						setCookiesForDriver(fdriver, fw, requestingLang);
						fdriver.get(urls[i]);
					}
					

					Language detectedLang = getPageLanguage(fdriver);
					if(detectedLang == requestingLang){
						String detectdLanguageCode = languageDetector.getLangCode(detectedLang);
						subjectFolderName = Config.SUBJECT_FOLDER_PREFIX+i+"_"+detectdLanguageCode;
						savePage(fdriver,Config.SAVED_WEBPAGES_FOLDER+"/"+subjectFolderName);
					}
					else{
						logger.info("skipping... requested language "+requestingLang+" for TC_"+i+".. detected: "+detectedLang);
					}
					fdriver.quit();
				}
			}catch(Exception e){
				logger.error("error on saving test case "+i+"... message:"+e.getMessage());
			}

		}

	}
	
	public Language getPageLanguage(WebDriver webDriver) {
		String tld = Utils.getTLD(webDriver.getCurrentUrl());
		String pageSource = Utils.getPageSource(webDriver);		
		String lang = languageDetector.DetectLanguage(pageSource, tld);
		return languageDetector.getLangfromNameOrCode(lang);
	}

	private void waitForGTransToFinsih(WebDriver webDriver){
		try{
			//switch to Google's translate iframe and wait until
			WebElement googleTranslateFrame = webDriver.findElement(By.xpath("//*[@class=\"goog-te-banner-frame skiptranslate\"]"));
			webDriver.switchTo().frame(googleTranslateFrame);
			WebDriverWait wait = new WebDriverWait(webDriver, 10);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=':1.finishSection' or @id=':2.finishSection' or @id=':3.finishSection']")));
			//ConfirmLangforGoogleTranslate(fdriver);
			//go back to default content..
			webDriver.switchTo().defaultContent();
		}catch(Exception e){
			logger.info("Error in waiting for google to translate the webpage");
		}
	}

	
	//this method uses the Webdriver to press the confirm button when page is loaded and google translate API
	//is asking for confirmation to translate the page to the requested language
	@SuppressWarnings("unused")
	private void ConfirmLangforGoogleTranslate(WebDriver webDriver){
		try{
			//switch to Google's translate iframe and click the confirm
			WebElement googleTranslateFrame = webDriver.findElement(By.xpath("//*[@class=\"goog-te-banner-frame skiptranslate\"]"));
			
			webDriver.switchTo().frame(googleTranslateFrame);
			
			List<WebElement> buttons = webDriver.findElements(By.tagName("button"));
			WebElement confirmButton = null;
			for (WebElement btn : buttons) {
				String buttonID = btn.getAttribute("id");
				if(buttonID.contains("confirm"))
					confirmButton = btn;
			}
			if(confirmButton != null){
				confirmButton.click(); 
			}
			else{
				logger.info("Could not find google translate switch language confirmation button");
			}
			//go back to default content..
			webDriver.switchTo().defaultContent();
		}catch(Exception e){
			logger.info("Error in google translate switch language");
		}
	}
	
	
	private Framework getPageFramework(WebDriver webDriver, FrameworkDetector frameworkDetect){
		String pageSource = Utils.getPageSource(webDriver);
		return frameworkDetect.DetectFramework(pageSource);	
	}
	
	public void setCookiesForDriver(WebDriver webDriver, Framework fw, Language lang){
		String langCode = languageDetector.getLangCode(lang);
		if(fw == Framework.GOOGLETRANSLATE){
			//google translate also adds country code for Chinese language .. 
			//because there is simplified and traditional Chinese
			if(langCode.equals("zh"))
				langCode = langCode+"-CN";
			webDriver.manage().addCookie(new Cookie("googtrans", "/auto/"+langCode,"/",null));
		}
		else if(fw == Framework.MOTIONPOINT){
			webDriver.manage().addCookie(new Cookie("MP_LANG", langCode,"/",null));
		}
		else if(fw == Framework.WORDPRESS){
			webDriver.manage().addCookie(new Cookie("_icl_current_language", langCode,"/",null));
		}
	}


}
