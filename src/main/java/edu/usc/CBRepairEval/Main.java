package edu.usc.CBRepairEval;

import org.openqa.selenium.firefox.FirefoxDriver;

import edu.usc.config.Config;
import edu.usc.gwali.Gwali;
import edu.usc.util.Utils;

public class Main {

	public static void main(String[] args) {
		
		// dummy parameters
		String args_0 = "file:///home/abdulmajeed/data/gwali/ScrapBook/data/hightail-ref";
		String args_1 = "file:///home/abdulmajeed/data/gwali/ScrapBook/data/hightail-test";

        String baseline = args[0] + "/index.html";//args[0];
        String put = args[1] + "/index.html";//args[1];
		
        Config.applyConfig();

        FirefoxDriver refDriver = Utils.getNewFirefoxDriver();
        FirefoxDriver testDriver = Utils.getNewFirefoxDriver();

		refDriver.get(baseline);
		testDriver.get(put);

        Gwali gwali = new Gwali(refDriver, testDriver);
        gwali.runGwali();


        int num = gwali.getNoOfIncosistancy();
		
        System.out.println("NoOfIncosistancy: " + num);
		refDriver.close();
		testDriver.close();
		
	}

}
