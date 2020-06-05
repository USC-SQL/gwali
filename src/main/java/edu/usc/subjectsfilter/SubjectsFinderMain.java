package edu.usc.subjectsfilter;

import edu.usc.languagedetectors.CLD2DetectorJNI;
import edu.usc.languagedetectors.LanguageDetector;
import edu.usc.webdriver.WebDriverPool;
import edu.usc.subjectsfilter.SubjectsSaver;

public class SubjectsFinderMain {

	public static void main(String[] args) {

		WebDriverPool driverPool = new WebDriverPool();
		LanguageDetector langDetect = new CLD2DetectorJNI();

		SubjectsSaver subjectsSaver = new SubjectsSaver(driverPool, langDetect, null);
		
		subjectsSaver.SavePages("urlsfile.txt");
	}

}
