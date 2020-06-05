package edu.usc.languagedetectors;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;



public class OptimaizeTextLanguageDetector extends edu.usc.languagedetectors.LanguageDetector{
	final static Logger logger = LoggerFactory.getLogger(OptimaizeTextLanguageDetector.class);
	
	public String DetectLanguage(String html, String tld){
		//tld hint is ignored in OptimaizeTextLanguageDetector
		
		// extract text from the html (by removing tags ...etc)
		String txt  = WebPageTextExtractor.extractText(html);		
		//load all languages:
		List<LanguageProfile> languageProfiles;
		try {
			
			languageProfiles = new LanguageProfileReader().readAllBuiltIn();

			//build language detector:
			LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
			        .withProfiles(languageProfiles)
			        .build();
	
			//create a text object factory
			TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
	
			//query:
			TextObject textObject = textObjectFactory.forText(txt);
			
			List<DetectedLanguage> lang = languageDetector.getProbabilities(textObject);
			for (Iterator<DetectedLanguage> iterator = lang.iterator(); iterator.hasNext();) {
				DetectedLanguage detectedLanguage = (DetectedLanguage) iterator
						.next();
				logger.debug("detected language= "+detectedLanguage.getLocale().getLanguage() + "  with probability = "+detectedLanguage.getProbability());
				
			}
			
			
			return lang.size() > 0 ? lang.get(0).getLocale().getLanguage() : "Language Undetected";
		} catch (Exception e) {
			System.err.println("************* IO exception");
			e.printStackTrace();
			return "Language Undetected";
		}
		
	}
}
