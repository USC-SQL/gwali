package edu.usc.util;



import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HTTP;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.jsoup.nodes.Element;
import edu.usc.config.Config;
import edu.usc.experiment.URLAddress;
import edu.usc.languagedetectors.CLD2DetectorJNI;
import edu.usc.languagedetectors.LanguageDetector;
import edu.usc.languagedetectors.LanguageDetector.Language;
import edu.usc.layoutgraph.LayoutGraphBuilder;
import edu.usc.languagedetectors.OptimaizeTextLanguageDetector;





public class Utils {


	public static FirefoxDriver recycableDriver;
	
	private static String mbrsExtractionsScript;
	
	
	public static FirefoxDriver getNewFirefoxDriver() {
		Config.applyConfig();
		File pathBinary;
		if(System.getProperty("os.name").toLowerCase().contains("mac"))
			pathBinary = new File(Config.FIREFOX_LOCATION_MAC);
		else{
			pathBinary = new File(Config.FIREFOX_LOCATION);
		}
        FirefoxBinary ffBinary = new FirefoxBinary(pathBinary);
        FirefoxProfile ffProfile = new FirefoxProfile();
		FirefoxDriver driver = new FirefoxDriver(ffBinary,ffProfile);
		driver.manage().window().maximize();
		return driver;

	}
	
	public static WebDriver getRecycledWebDriver() {
		if(recycableDriver == null){
			recycableDriver = (FirefoxDriver) getNewFirefoxDriver();
			return recycableDriver;
		}
		return recycableDriver;
	}
	
	public static String getMBRsExtractionScript(){
		if(mbrsExtractionsScript == null) {
			mbrsExtractionsScript = getPkgFileContents(LayoutGraphBuilder.class, "/getMBRs.js");
			mbrsExtractionsScript = compressJS(mbrsExtractionsScript);
		}
		return mbrsExtractionsScript+"\n"+"return data;";	
	}
	
	
	
	//returns OptimaizeText for Max OS X, or CLD2 Detector for Linux (Windows is not supported)
	public static LanguageDetector getLanguageDetector(){
		String osName = System.getProperty("os.name");
		if(osName.equals("Mac OS X")){
			return new OptimaizeTextLanguageDetector();
		}else{
			return new CLD2DetectorJNI();
		}
		
	}
	
	/**
	 * Read a file that is in the package structure
	 * 
	 * @param pkgFileName
	 * @return file contents
	 */
	@SuppressWarnings("rawtypes") 
	public static String getPkgFileContents(Class cls, String pkgFileName) {
		InputStream inputStream = cls.getResourceAsStream(pkgFileName);
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		String resultString = null;
		try {
			while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
			resultString = result.toString("UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultString;
	}

	
	public static void recursiveDelete(File file) {
        if (!file.exists())
            return;
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                recursiveDelete(f);
            }
        }
        file.delete();
    }
	
	public static String getTLD(String urlString){
		URLAddress urlAddress = new URLAddress(urlString);
		return urlAddress.getTldString();
	}
	
	@SuppressWarnings("rawtypes")
	public static File getScrapbookExtentionFile(Class cls){
		URL scrapBook = cls.getResource(Config.SCRAPBOOK_FILE_NAME);
		File extFile = null;
		try {
			extFile = new File(scrapBook.toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return extFile;
	}
	
	public static File getNewestChildFile(File folder , FileFilter filter) throws FileNotFoundException{
		File[] files;
		if (filter != null){
			files = folder.listFiles(filter);
		}else{
			files = folder.listFiles();
		}
	    File newestFile = null;
	    if (files.length > 0) {
	        Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
	        newestFile = files[0];
	    }else throw new FileNotFoundException("could not any file enclosed in the folder with filter "+filter.toString());
	    
	    return newestFile;
	}
	
	public static ArrayList<File> getTestCasesFolders(int i){
		File dir = new File(Config.SAVED_WEBPAGES_FOLDER);
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.startsWith(Config.SUBJECT_FOLDER_PREFIX+i+'_');
		    }
		};
		File[] foundFiles = dir.listFiles(filter);
		ArrayList<File> filesHavingIndexPage = new ArrayList<File>();
		for (File file : foundFiles) {
			//remove comment to add only folders having index.html page
			//if(Arrays.asList(file.list()).contains("index.html"));
				filesHavingIndexPage.add(file);
		}
		return filesHavingIndexPage;
	}
	
	public static String getPageSource(WebDriver driver){
		String javascript = "return arguments[0].innerHTML";
		String pageSource=(String)((JavascriptExecutor)driver)
		    .executeScript(javascript, driver.findElement(By.tagName("html")));
		pageSource = "<html>"+pageSource +"</html>";
		return pageSource;
	}
	
	public static Language getLangFromDirName(String dirName){
		int index = dirName.lastIndexOf(File.separator);
		String folder = dirName.substring(index+1);
		index = folder.lastIndexOf("_");
		String langCode = folder.substring(index+1);
		Language lang = getLanguageDetector().getLangfromNameOrCode(langCode);
		return lang;
	}
	
	public static String getPageSource(String url, Locale locale) throws ClientProtocolException, IOException{
		
		String html = "";
		String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:44.0) Gecko/20100101 Firefox/44.0";
		String acceptHeader = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
		
		HttpGet getRequest = new HttpGet(url);
		getRequest.setHeader(HTTP.USER_AGENT, userAgent);
		getRequest.setHeader("Accept",acceptHeader);

		HttpClient httpClient = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy()).build();		
		
		if(locale != null){
			String langCode = LanguageDetector.formatLocale(locale);
			getRequest.setHeader("Accept-Language",langCode);

		}
		
		HttpResponse response = httpClient.execute(getRequest);
		ResponseHandler<String> handler = new BasicResponseHandler();
		
		html = handler.handleResponse(response); 

		return html;
	}

	 
	/* TODO: remove this constant string and use a JavaScript function in a JavaScript File
	 *  this function will return the xpath as form "/html[1]/body[1]/div[1]/"
	 *  so an index [1] is attached to the first child tags even if they don't have siblings
	 *  this will provide better results for the leventine distance algorithm.
	 */
	 public static String getElementXPath(WebDriver driver, WebElement element) {
	    String javaScript = "function getElementXPath(elt){" +
	                            "var path = \"\";" +
	                            "for (; elt && elt.nodeType == 1; elt = elt.parentNode){" +
	                                "idx = getElementIdx(elt);" +
	                                "xname = elt.tagName;" +
	                                "xname += \"[\" + idx + \"]\";" +
	                                "path = \"/\" + xname + path;" +
	                            "}" + 
	                            "return path;" +
	                        "}" +
	                        "function getElementIdx(elt){" +
	                            "var count = 1;" +
	                            "for (var sib = elt.previousSibling; sib ; sib = sib.previousSibling){" +
	                                "if(sib.nodeType == 1 && sib.tagName == elt.tagName){" +
	                                    "count++;" +
	                                "}" +
	                            "}" +
	                            "return count;" + 
	                        "}" +
	                        "return getElementXPath(arguments[0]).toLowerCase();";      

	    return (String)((JavascriptExecutor)driver).executeScript(javaScript, element);     
	}
	 
	 public static String normalizeXPATH(String xpath){
		 //remove /text() , /@placeholder , /@value , /descendant::text() from the xpath
		 //currently not used in the normalization process
		 //xpath = removeNonTagsFromXPATH(xpath);
		 // add [1] before any '/'
		 xpath = xpath.replaceAll("\\/", "[1]/");
		 // remove [1] from the first '/' if it exist
		 xpath = xpath.replaceAll("^\\[1\\]", "");
		 //remove the added if there was an indexing used before   
		 xpath = xpath.replaceAll("\\]\\[1\\]", "]");
		 //remove '/' from the end of the path
		 if(xpath.endsWith("/"))
			 xpath = xpath.substring(0, xpath.length()-1);
		 //if the xpath does not end with index add [1] to the end
		 if(!xpath.endsWith("]"))
			 xpath = xpath + "[1]";
		 //if the xpath does not begin with '/' add it to the beginning
		 if(!xpath.startsWith("/"))
			 xpath = "/"+xpath;
		 //change the xpath to lower case character
		 return xpath.toLowerCase();
		 
	 }

	public static double getNumbersFromString(String string)
	{
		Pattern p = Pattern.compile("(\\d+(?:\\.\\d+)?)");
		Matcher m = p.matcher(string);
		if (m.find())
		{
			return Double.valueOf(m.group(1));
		}
		return 0;
	}


	//This method removes /text() , /@placeholder , /@value , /descendant::text() from the xpath
	 public static String removeNonTagsFromXPATH(String xpath){
		 xpath = xpath.replaceAll("(\\/text\\(\\)|\\/@placeholder|\\/@value|\\/descendant::text\\(\\)).*", "");
		 return xpath;
	 }


	 private static String compressJS(String jsString) {
	 	StringReader in = new StringReader(jsString);
	 	StringWriter out = new StringWriter();
	 	JavaScriptCompressor compressor;

		 try {
			 compressor = new JavaScriptCompressor(in, new ErrorReporter() {
                 public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
                     System.err.println("\n[WARNING] in " + in);
                     if(line < 0) {
                         System.err.println("  " + message);
                     } else {
                         System.err.println("  " + line + ':' + lineOffset + ':' + message);
                     }

                 }

                 public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
                     System.err.println("[ERROR] in " + in);
                     if(line < 0) {
                         System.err.println("  " + message);
                     } else {
                         System.err.println("  " + line + ':' + lineOffset + ':' + message);
                     }

                 }

                 public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
                     this.error(message, sourceName, line, lineSource, lineOffset);
                     return new EvaluatorException(message);
                 }
             });
			 compressor.compress(out, -1, true, false, false, false);
		 } catch (IOException e) {
			 e.printStackTrace();
		 }

		 if(out.toString().length() > 0)
		 	return out.toString();
		 else
		 	return jsString;

	 }

	public static String getXPathOfElementJava(org.jsoup.nodes.Element element) {
		ArrayList<String> paths = new ArrayList<String>();
		for (; element != null && !element.tagName().equals("#root"); element = element.parent())
		{
			int index = 0;
			/*
			 * if(!element.id().isEmpty()) { paths.add("/*[@id=\"" +
			 * element.id() + "\"]"); break; }
			 */

			for (Element sibling = element.previousElementSibling(); sibling != null && !sibling.tagName().equals("#root"); sibling = sibling.previousElementSibling())
			{
				if (sibling.tagName().equals(element.tagName()))
				{
					++index;
				}
			}
			String tagName = element.tagName().toLowerCase();
			String pathIndex = "[" + (index + 1) + "]";
			paths.add(tagName + pathIndex);
		}

		String result = null;
		if (paths.size() > 0)
		{
			result = "/";
			for (int i = paths.size() - 1; i > 0; i--)
			{
				result = result + paths.get(i) + "/";
			}
			result = result + paths.get(0);
		}

		return result;
	}

	public static org.w3c.dom.Element getW3CElementFromXPathJava(String xPath, org.w3c.dom.Document doc) throws IOException
	{
		String xPathArray[] = xPath.split("/");
		ArrayList<String> xPathList = new ArrayList<String>();

		for (int i = 0; i < xPathArray.length; i++)
		{
			if (!xPathArray[i].isEmpty())
			{
				xPathList.add(xPathArray[i]);
			}
		}

		org.w3c.dom.Element foundElement = null;
		org.w3c.dom.NodeList elements;
		int startIndex = 0;

		String id = getElementId(xPathList.get(0));
		if (id != null && !id.isEmpty())
		{
			foundElement = doc.getElementById(id);
			if (foundElement == null)
				return null;
			elements = foundElement.getChildNodes();
			startIndex = 1;
		}
		else
		{
			elements = doc.getElementsByTagName(xPathList.get(0).replaceFirst("\\[(.+)\\]", ""));
		}
		for (int i = startIndex; i < xPathList.size(); i++)
		{
			String xPathFragment = xPathList.get(i);
			int index = getSiblingIndex(xPathFragment);
			boolean found = false;

			// strip off sibling index in square brackets
			xPathFragment = xPathFragment.replaceFirst("\\[(.+)\\]", "");

			for (int j = 0; j < elements.getLength(); j++)
			{
				if (elements.item(j).getNodeType() != Node.ELEMENT_NODE)
				{
					continue;
				}

				org.w3c.dom.Element element = (org.w3c.dom.Element) elements.item(j);

				if (found == false && xPathFragment.equalsIgnoreCase(element.getTagName()))
				{
					// check if sibling index present
					if (index > 1)
					{
						int siblingCount = 0;

						for (org.w3c.dom.Node siblingNode = element.getParentNode().getFirstChild(); siblingNode != null; siblingNode = siblingNode.getNextSibling())
						{
							if (siblingNode.getNodeType() != Node.ELEMENT_NODE)
							{
								continue;
							}

							org.w3c.dom.Element siblingElement = (org.w3c.dom.Element) siblingNode;
							if ((siblingElement.getTagName().equalsIgnoreCase(xPathFragment)))
							{
								siblingCount++;
								if (index == siblingCount)
								{
									foundElement = siblingElement;
									found = true;
									break;
								}
							}
						}
						// invalid element (sibling index does not exist)
						if (found == false)
							return null;
					}
					else
					{
						foundElement = element;
						found = true;
					}
					break;
				}
			}

			// element not found
			if (found == false)
			{
				return null;
			}

			elements = foundElement.getChildNodes();
		}
		return foundElement;
	}
	
	private static int getSiblingIndex(String xPathElement)
	{
		String value = getValueFromRegex("\\[(.+)\\]", xPathElement);
		if (value == null)
			return -1;
		return Integer.parseInt(value);
	}

	
	public static String getValueFromRegex(String regex, String str)
	{
		Pattern p = Pattern.compile(regex, Pattern.DOTALL);
		Matcher m = p.matcher(str);
		if (m.find())
		{
			return m.group(1);
		}
		return null;
	}
	
	private static String getElementId(String xPathElement)
	{
		return getValueFromRegex("\\*\\[@id=['|\"]?(.+[^'\"])['|\"]?\\]", xPathElement);
	}



}
