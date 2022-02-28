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
        String refPath = "file:///C:/Users/fzhan/code/research/ifix/ifix-master/subjects/westin-ref/index.html";
        String testPath = "file:///C:/Users/fzhan/code/research/ifix/ifix-master/subjects/westin-test/index.html";
        // testPath = "https://www.google.com";
        // refPath = "https://www.google.com";

        Config.CHECK_RTL = false;
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
