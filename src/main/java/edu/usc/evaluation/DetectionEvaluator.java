package edu.usc.evaluation;

import java.util.ArrayList;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.usc.config.Config;
import edu.usc.gwali.Gwali;
import edu.usc.webdriver.WebDriverPool;

public class DetectionEvaluator {
	final static Logger logger = LoggerFactory.getLogger(DetectionEvaluator.class);
	public static void main(String [] args){
		Config.applyConfig();

		WebDriverPool webdrivers = new WebDriverPool();
		FirefoxDriver baselineFDriver = (FirefoxDriver) webdrivers.createFirefoxDriver(null);
		FirefoxDriver putFDriver = (FirefoxDriver) webdrivers.createFirefoxDriver(null);

		String baselineURL = "https://www.facebook.com";
		String putURL = "https://pt-br.facebook.com";
		
		baselineFDriver.get(baselineURL);
		putFDriver.get(putURL);
		
		Gwali gwali = new Gwali(baselineFDriver, putFDriver);
		
		ArrayList<String> xpaths = gwali.runGwali();
		System.out.println("# of potentially faulty elements: " + xpaths.size());
		for (String xpath : xpaths) {
			System.out.println(xpath);
		}
		
		putFDriver.get("https://google.com");
		gwali.ChangePUT(putFDriver);
		xpaths = gwali.runGwali();
		System.out.println("# of potentially faulty elements for put2: " + xpaths.size());
		for (String xpath : xpaths) {
			System.out.println(xpath);
		}

		/**
		LayoutGraph putLG = gwali.getPutLG();
		
		List<DomNode> elementsForClustering = new ArrayList<DomNode>();
		
		for (LayoutNode node : putLG.getVertices()) {
			DomNode domNode = node.getDomNode();
			//if(domNode.isTag() && domNode.isVisible() && domNode.isLayout())
				elementsForClustering.add(domNode);
		}
		System.out.println("Elements to be clustered: "+elementsForClustering.size());
		ElementsClusterer clusterer = new ElementsClusterer(elementsForClustering);
		clusterer.perfomrClustering();		
		clusterer.printClusterResults();
		*/
		
		
		
			
		//WebElement element = fdriver2.findElement(By.xpath(xpath));
		//fdriver2.executeScript("arguments[0].style[arguments[1]] = arguments[2];", element, "font-size", "12px");
			
		
			
		
		
		baselineFDriver.quit();
		putFDriver.quit();

		
		
	}
}
