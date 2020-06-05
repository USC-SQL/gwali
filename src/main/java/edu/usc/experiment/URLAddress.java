package edu.usc.experiment;

import java.net.MalformedURLException;
import java.net.URL;

public class URLAddress {
	private String urlAddressString;
	public URLAddress(String url) {
		setUrlAddress(url);
	}
	/**
	 * @return the urlAddress
	 */
	public String getUrlAddress() {
		return urlAddressString;
	}
	/**
	 * @param urlAddress the urlAddress to set
	 */
	public void setUrlAddress(String urlAddress) {
		this.urlAddressString = urlAddress;
	}
	
	//parameter urlString: a String
	//returns: a String representing the TLD of urlString, or null iff urlString is malformed
	public String getTldString() {

	    String tldString = "com";
	    try {
	    	URL url = new URL(urlAddressString);
	        String[] domainNameParts = url.getHost().split("\\.");
	        tldString = domainNameParts[domainNameParts.length-1];
	    }
	    catch (MalformedURLException e) {
	    	System.err.println("Malformed url:"+urlAddressString);
	    	return "com";
	    }

	    return tldString;
	}

}
