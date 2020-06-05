package edu.usc.languagedetectors;

public class CLD2DetectorJNI extends LanguageDetector{
	static{
		System.loadLibrary("cld2jni");
	}
	public native String detectLanguage(String html,String tld);
	@Override
	public String DetectLanguage(String html, String tld) {
		// call the native method
		return detectLanguage(html, tld);
	}
	
}
