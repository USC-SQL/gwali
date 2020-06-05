package edu.usc.subjectsfilter;
import edu.usc.util.Utils;
import edu.usc.webdriver.WebDriverPool;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.firefox.FirefoxDriver;


public class PagesSaver {

	public static void main(String[] args) {
		//Config.applyConfig();
		WebDriverPool driverPool = new WebDriverPool();
		SubjectsSaver pagesSaver = new SubjectsSaver(driverPool, null, null);
		FirefoxDriver ffdriver = driverPool.createFirefoxDriver(null, true);
		String URLsFile = "/URLS/missingAlexa.txt";
		String[] urls = Utils.getPkgFileContents(PagesSaver.class, URLsFile ).split("\n");
		final String SUBJECTS_FOLDER = System.getProperty("user.home")+"/PAGES_FOR_IFIX_JOURNAL";
	
		for (int i = 0; i < urls.length; i++) {
			System.out.println(urls[i]);
			try{
				ffdriver.get("http://"+urls[i]);
			}catch(TimeoutException e){
				System.err.println("Timed out waiting for page load:"+urls[i]);
				pagesSaver.savePage(ffdriver,SUBJECTS_FOLDER+"/"+urls[i]);
				ffdriver.quit();
				ffdriver = driverPool.createFirefoxDriver(null, true);
				continue;
			}
			pagesSaver.savePage(ffdriver,SUBJECTS_FOLDER+"/"+urls[i]);
			
		}
		ffdriver.quit();
	}

}
