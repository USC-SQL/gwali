package edu.usc.config;

import java.io.File;

public class Config {

    public static float SEARCH_RADIUS = 200;
    //setting this to false increases the accuracy since normalized xpaths causes inaccuracy in the matching algorithm
	public static boolean normalizePath = true;

	public static boolean removeNonTagNodes = true;

	// strict mode will report extra failures, but might also report many false positives..
	// the extra failures that are reported include removing the filter that filters alignment
	// inconsistency for text node. Also, it creates extra edge labels in the layout graph, such as
	// elements are centered
	public static boolean strictMode = false;

	public static void applyConfig(){
		//System.setProperty("webdriver.gecko.driver",DRIVER_LOCATION);
		if(new File(FIREFOX_LOCATION).exists()){
			System.out.println("firefox location: "+FIREFOX_LOCATION);
			System.setProperty("webdriver.firefox.bin",FIREFOX_LOCATION);
		}

	}

	public static boolean CHECK_RTL = false;

	public static final String FIREFOX_LOCATION = "C:\\Program Files\\Mozilla Firefox 46\\firefox.exe";
	public static final String FIREFOX_LOCATION_MAC = "C:\\Program Files\\Mozilla Firefox 46\\firefox.exe";
	public static final String DRIVER_LOCATION = "C:\\Users\\fzhan\\code\\research\\geckodriver.exe";

	// Test subject information
	public static final String SUBJECTS_FOLDER = "C:\\Users\\fzhan\\code\\research\\ifix\\ifix-master\\subjects\\";
	public static final String ACCURACY_CONFIG_FILE = "C:\\Users\\fzhan\\code\\research\\gwali\\src\\test\\RQ1TestCases.txt";
	public static final String RANK_CONFIG_FILE = "C:\\Users\\fzhan\\code\\research\\gwali\\src\\test\\RQ2TestCases.txt";

	public static final int NO_OF_SUBJECTS = 453;

	public static int ANGEL_THRISHOLD = 60;
	public static final long FILTERING_TASK_TIMEOUT = 1000*60*5; //in millisecond so total is 5 minutes..
	public static final String ALL_CHILDREN_XPATH = "/descendant::text()";
	public static final String ALL_ELEMENTS_XPATH = "//*";

	
	public static final String TEMP_FOLDER = "./temp";
	
	public static final String SYSTEM_TEMP_FOLDER = System.getProperty("java.io.tmpdir");

	public static final String SUBJECT_FOLDER_PREFIX = "TC_";
	public static final String SCRAPBOOK_FILE_NAME = "/firefox-scrapbook.xpi";
	public static final String SAVED_WEBPAGES_FOLDER = System.getProperty("user.home")+"/IFIX_Journal_WebPages";
	public static final String FILTERED_URLS_FILE = "/URLS/filteredURLS.txt";
	public static final String ALL_URLS_FILE = "/URLS/URoulete.txt";

	public static final String ANALYSIS_RESULTS_FILE = "./results/TCResults.ser";
	public static final String I18NCHECKER_RESULTS_FILE = "./results/i18nCheckResults";




}
