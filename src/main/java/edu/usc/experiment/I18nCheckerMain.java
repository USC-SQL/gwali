package edu.usc.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3.i18n.Assertion;

import edu.usc.config.Config;
import edu.usc.frameworkdetector.FrameworkDetector;
import edu.usc.frameworkdetector.FrameworkDetector.Framework;
import edu.usc.results.i18nCheckerResult;
import edu.usc.util.Utils;

public class I18nCheckerMain {


	
    public static void main( String[] args ) {
    	
    	String URLsFile = Config.FILTERED_URLS_FILE;
		String[] urlStrings = Utils.getPkgFileContents(I18nCheckerMain.class, URLsFile ).split("\n");
		File outFile = new File("./results/i18nCheckResults");
		FileOutputStream fos;
		ObjectOutputStream oos = null;
		FrameworkDetector frameworkDetector = new FrameworkDetector();
		try {
			
			
			fos = new FileOutputStream(outFile);
			oos = new ObjectOutputStream(fos);
		
		
			for (int i = 0; i < urlStrings.length; i++) {
		    	try {
		    		URL url = new URL(urlStrings[i]);
		    		
		    		java.util.List<Assertion> results = org.w3.i18n.I18nChecker.check(url);

		    		String pageSrc = Utils.getPageSource(urlStrings[i], null);
		    		Framework framework = frameworkDetector.DetectFramework(pageSrc);
		    		
		    		i18nCheckerResult i18nCheckerResultObj = new i18nCheckerResult(urlStrings[i], framework, results);
		    		
		    		oos.writeObject(i18nCheckerResultObj);
		    		System.out.println("Written results of "+i+" - "+urlStrings[i]);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (java.lang.RuntimeException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
		    	
			}
			
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    

    /* Runs the i18n-checker on the given URL and prints the results to the

     * standard output stream. */

    public static void printI18nCheck(java.net.URL url) throws java.io.IOException {
        // Run the checker on the remote Web page.

        java.util.List<Assertion> results =
                org.w3.i18n.I18nChecker.check(url);

        for (Assertion assertion : results) {
            System.out.printf(
                    "(%s) %s\n    Context: %s\n",
                    assertion.getLevel(),
                    assertion.getHtmlTitle(),
                    assertion.getContexts()
                    // Remove unwanted whitespace from verbatim contexts.
                    .toString().replaceAll("\\s+", " "));
        }

    }
    


}