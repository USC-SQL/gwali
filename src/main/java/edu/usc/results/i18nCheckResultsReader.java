package edu.usc.results;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3.i18n.Assertion;
import org.w3.i18n.Assertion.Level;

import edu.usc.config.Config;
import edu.usc.frameworkdetector.FrameworkDetector.Framework;

public class i18nCheckResultsReader {


	public static void main(String[] args) throws IOException {
		File inFile;
		FileInputStream fileIn;
		ObjectInputStream objectIn = null;
		String fileName = Config.I18NCHECKER_RESULTS_FILE; 
		
		Map<String, Integer> errorTypesFreq = new HashMap<String, Integer>();
		Map<String, Integer> warnsTypesFreq = new HashMap<String, Integer>();

		
		try {
			inFile = new File(fileName);

			fileIn = new FileInputStream(inFile);

			objectIn = new ObjectInputStream(fileIn);

			int i = 0;
	        while(true){
	        	i++;
	        	i18nCheckerResult i18nResult =  (i18nCheckerResult) objectIn.readObject();
	        	List<String> errors = new ArrayList<String>(4);
	        	List<String> warnings = new ArrayList<String>(4);

	        	for (Assertion assertion : i18nResult.getResults()) {

					if(assertion.getLevel() == Level.ERROR){
						String errorTitle = assertion.getHtmlTitle();
						errors.add(errorTitle);
						addValueToMap(errorTypesFreq, errorTitle);
					}
					else if(assertion.getLevel() == Level.WARNING){
						String warnTitle = assertion.getHtmlTitle();
						warnings.add(warnTitle);
						addValueToMap(warnsTypesFreq, warnTitle);
					}
				}
	        	printResult(i, i18nResult.getFramework(), errors, warnings, i18nResult.getUrl());

	        }
        } catch(EOFException e){
        	System.out.println("reached end of file "+fileName);
        	
        } catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		objectIn.close();
		System.out.println("**************************");
		System.out.println("Errors:");
		for (String errorType : errorTypesFreq.keySet()) {
			System.out.println(errorType + " | " + errorTypesFreq.get(errorType));
		}
		System.out.println("**************************");
		System.out.println("Warnings:");
		for (String warntype : warnsTypesFreq.keySet()) {
			System.out.println(warntype + " | " + warnsTypesFreq.get(warntype));
		}
	}
	
	public static void printResult(int id, Framework fw ,List<String> errors, List<String> warnings, String url){
    	StringBuilder resultString = new StringBuilder();
    	resultString.append(id + " | " + fw + " | ");
    	
    	resultString.append(errors.size() + " | " );
    	for (String err : errors) {
    		resultString.append(err+ " | " );
		}
    	for (int j = errors.size() ; j < 4; j++) {
    		resultString.append( "* | " );
		}
    	
    	resultString.append(warnings.size() + " | " );
    	for (String wrn : warnings) {
    		resultString.append(wrn+ " | " );
		}
    	for (int j = warnings.size() ; j < 4; j++) {
    		resultString.append( "* | " );
		}
    	
    	resultString.append(url);
		System.out.println(resultString.toString() );
	}
	
	public static void addValueToMap(Map<String,Integer> titleFreq, String title){
		Integer freq = titleFreq.get(title);
		if(freq == null)
			titleFreq.put(title, 1);
		else titleFreq.put(title, freq+1);
	}
	

}
