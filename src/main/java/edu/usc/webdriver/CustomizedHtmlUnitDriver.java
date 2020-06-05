package edu.usc.webdriver;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

//class that uses HtmlUnitDriver but adds the ability to set the HTTP headers;
public class CustomizedHtmlUnitDriver extends HtmlUnitDriver {
	public CustomizedHtmlUnitDriver(boolean enableJavascript){
		super(enableJavascript);
	}
	public void setHeader(String name, String value){
		this.getWebClient().addRequestHeader(name, value);
	}
}
