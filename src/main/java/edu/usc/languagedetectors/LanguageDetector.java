package edu.usc.languagedetectors;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public abstract class LanguageDetector {
	private Map<Language, String> languageToCode;
	
	public LanguageDetector(){
		languageToCode = new HashMap<LanguageDetector.Language, String>();
		languageToCode.put(Language.ENGLISH, "en");
		languageToCode.put(Language.SPANISH, "es");
		languageToCode.put(Language.RUSSIAN, "ru");
		languageToCode.put(Language.GERMAN, "de");
		languageToCode.put(Language.JAPANESE, "ja");
		languageToCode.put(Language.FRENCH, "fr");
		languageToCode.put(Language.PORTUGUESE, "pt");
		languageToCode.put(Language.ITALIAN, "it");
		languageToCode.put(Language.CHINESE, "zh");
		languageToCode.put(Language.POLISH, "pl");
		languageToCode.put(Language.TURKISH, "tr");
		languageToCode.put(Language.DUTCH, "nl");
	}
	
	public enum Language{
		ENGLISH,
		SPANISH,
		RUSSIAN,
		GERMAN,
		JAPANESE,
		FRENCH,
		PORTUGUESE,
		ITALIAN,
		CHINESE,
		POLISH,
		TURKISH,
		DUTCH,
		UNKNOWN;
	}
	
	public abstract String DetectLanguage(String html,String tld);

	public Language getLangfromNameOrCode(String langNameOrCode){
		Language foundLang = Language.UNKNOWN;
		for (Language language : languageToCode.keySet()) {
			String languageName = language.toString();
			String languageCode = languageToCode.get(language);
			if(langNameOrCode.equalsIgnoreCase(languageName) || langNameOrCode.equalsIgnoreCase(languageCode))
				foundLang = language;
		}
		return foundLang;
	}
	
	public String getLangCode(Language lang){
		return languageToCode.get(lang);
	}
	
	public static String formatLocale(Locale locale) {
	    return locale.getCountry().length() == 0
	      ? locale.getLanguage()
	      : locale.getLanguage() + "-" + locale.getCountry().toLowerCase();
	}
	
}
