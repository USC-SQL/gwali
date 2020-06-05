package edu.usc.languagedetectors;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebPageTextExtractor {
	
	public static String extractText(String html){

	    Document doc = Jsoup.parse(html);
	    return doc.text();
	}
}
