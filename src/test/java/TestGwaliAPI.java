import edu.usc.config.Config;
import edu.usc.gwali.Gwali;
import edu.usc.util.Utils;

import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.List;

/**
 * Created by alameer on 4/16/17.
 */
public class TestGwaliAPI {

    public static void main(String[]args){
        String refPath = "file:///Users/abdulmajeed/Library/Application%20Support/Firefox/Profiles/vyr24igw.default-1432923284677/ScrapBook/data/20170419182957/index.html";
        String testPath = "file:///Users/abdulmajeed/Library/Application%20Support/Firefox/Profiles/vyr24igw.default-1432923284677/ScrapBook/data/20170419182916/index.html";
        testPath = "https://www.think-pink.be/nl/raceforthecure/neem-deel";
        refPath = "https://www.think-pink.be/fr/raceforthecure/neem-deel";

        //Config.CHECK_RTL = true;
        FirefoxDriver refDriver = Utils.getNewFirefoxDriver();
        FirefoxDriver testDriver = Utils.getNewFirefoxDriver();

		refDriver.get(refPath);
		testDriver.get(testPath);

        Gwali gwali = new Gwali(refDriver, testDriver);
        List<String> potentiallyFaultyElements = gwali.runGwali();
        gwali.printIssues();
        for (String xpath: potentiallyFaultyElements) {
            System.out.println(xpath);
        }

		refDriver.quit();
		testDriver.quit();
    }

}
