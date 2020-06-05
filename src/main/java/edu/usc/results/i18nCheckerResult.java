package edu.usc.results;

import java.io.Serializable;
import java.util.List;

import org.w3.i18n.Assertion;

import edu.usc.frameworkdetector.FrameworkDetector.Framework;

public class i18nCheckerResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String url;
	private Framework framework;
	private java.util.List<Assertion> results;
	
	public i18nCheckerResult(String url, Framework framework, List<Assertion> results) {
		super();
		this.results = results;
		this.framework = framework;
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}

	public Framework getFramework() {
		return framework;
	}

	public java.util.List<Assertion> getResults() {
		return results;
	}
}