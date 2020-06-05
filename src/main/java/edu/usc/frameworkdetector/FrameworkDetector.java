package edu.usc.frameworkdetector;

public class FrameworkDetector {
	
	private final String KEYWORD_GOOGLETRANS1 = "google_translate_element";
	private final String KEYWORD_GOOGLETRANS2 = "google_translator_element";
	private final String KEYWORD_MSTRANS = "MicrosoftTranslatorWidget";
	private final String KEYWORD_MOTIONP1 = "mp_trans";
	private final String KEYWORD_MOTIONP2 = "MotionPoint's TransMotion";
	private final String KEYWORD_TRANSFIX = "//cdn.transifex.com/live.js";
	private final String KEYWORD_WP_MULTI = "sitepress-multilingual-cms";
	private final String KEYWORD_POLYLANG1 = "widget_polylang";
	private final String KEYWORD_POLYLANG2 = "wp-content/plugins/polylang/";
	private final String KEYWORD_POLYLANG3 = "wp-content/polylang/";
	private final String KEYWORD_DRUPAL1 = "language-switcher-locale-url";
	private final String KEYWORD_DRUPAL2 = "drupal";

	
	public enum Framework{
		GOOGLETRANSLATE, TRANSIFEX, WORDPRESS, POLYLANG, TRANSLATETHIS, MOTIONPOINT, MSTRANSLATOR, DRUPAL, UNKNOWN
	}
	
	
	
	public Framework DetectFramework(String htmlCode){
		if (htmlCode.contains(KEYWORD_GOOGLETRANS1) || htmlCode.contains(KEYWORD_GOOGLETRANS2))
			return Framework.GOOGLETRANSLATE;
		else if(htmlCode.contains(KEYWORD_MOTIONP1) || htmlCode.contains(KEYWORD_MOTIONP2))
			return Framework.MOTIONPOINT;
		else if(htmlCode.contains(KEYWORD_MSTRANS))
			return Framework.MSTRANSLATOR;
		else if(htmlCode.contains(KEYWORD_WP_MULTI))
			return Framework.WORDPRESS;
		else if(htmlCode.contains(KEYWORD_POLYLANG1) || htmlCode.contains(KEYWORD_POLYLANG2) || htmlCode.contains(KEYWORD_POLYLANG3))
			return Framework.POLYLANG;
		else if(htmlCode.contains(KEYWORD_TRANSFIX))
			return Framework.TRANSIFEX;
		else if(htmlCode.contains(KEYWORD_DRUPAL1) && htmlCode.contains(KEYWORD_DRUPAL2))
			return Framework.DRUPAL;
		else
			return Framework.UNKNOWN;
	}
	
}
