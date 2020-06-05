package edu.usc.util;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.domassign.StyleMap;
import org.apache.xerces.parsers.DOMParser;
import org.cyberneko.html.HTMLConfiguration;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * jStyleParser used for CSS parsing
 * @author sonal
 *
 */

public class CSSParser
{
	private String fileFullPath;
	private InputStream is;
    private Document doc;
    private String charset;
    private StyleMap styleMap;
    
    private static CSSParser instance = null;

    private CSSParser(String fileFullPath)
    {
    	this.fileFullPath = fileFullPath;
    	try
		{
			parseCSS();
		}
		catch (SAXException | IOException e)
		{
			e.printStackTrace();
		}
    }
    
    public static CSSParser getInstance(String fileFullPath)
    {
    	if(instance == null)
    	{
    		instance = new CSSParser(fileFullPath);
    	}
    	return instance;
    }

    public static void resetInstance()
    {
    	instance = null;
    }
    
    public InputStream getInputStream()
    {
        return is;
    }

    private void parse() throws SAXException, IOException
    {
        this.is = new FileInputStream(fileFullPath);
        DOMParser parser = new DOMParser(new HTMLConfiguration());
        parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        if (charset != null)
            parser.setProperty("http://cyberneko.org/html/properties/default-encoding", charset);
        parser.parse(new org.xml.sax.InputSource(is));
        doc = parser.getDocument();
    }

    private void parseCSS() throws SAXException, IOException
    {
    	parse();
    	
    	// don't include inherited properties
    	//styleMap = CSSFactory.assignDOM(doc, createBaseFromFilename(fileFullPath), "screen", false);
    	styleMap = CSSFactory.assignDOM(doc, "utf-8", createBaseFromFilename(fileFullPath), "screen", false);
    }
    
    private static URL createBaseFromFilename(String filename) {
		try {
			File f = new File(filename);
			return f.toURI().toURL();
		} catch (MalformedURLException e) {
			return null;
		}
	}
    
    public void parseCSSWithInheritance() throws SAXException, IOException
    {
    	parse();
    	
    	// include inherited properties
    	//styleMap = CSSFactory.assignDOM(doc, createBaseFromFilename(fileFullPath), "screen", true);
    	styleMap = CSSFactory.assignDOM(doc, "utf-8", createBaseFromFilename(fileFullPath), "screen", true);
    }
    
    public Map<String, String> getCSSPropertiesForElement(String xpathExpression) throws XPathExpressionException, IOException
    {
    	Map<String, String> cssProperties = new HashMap<String, String>();
    	
//    	XPath xPath = XPathFactory.newInstance().newXPath();
//    	NodeList nodes = (NodeList)xPath.evaluate(xpathExpression, doc.getDocumentElement(), XPathConstants.NODESET);

    	Element e = Utils.getW3CElementFromXPathJava(xpathExpression, doc);
    	
    	if(e != null)
    	{
    		NodeData data = styleMap.get(e);
    		
    		// process data
    		if(data != null)
    		{
	    		String[] rules = data.toString().split(";");
	    		
		    	for (int i = 0; i < rules.length-1; i++)
				{
					String[] rule = rules[i].split(":\\s");
					if(rule.length == 2)
					{
						String prop = rule[0].trim();
						String val = rule[1].trim();
						if(!val.isEmpty())
						{
							cssProperties.put(prop, val);
						}
					}
				}
    		}
    	}
    	return cssProperties;
    }
    
    public int getTotalNumberOfCSSProperties() throws IOException, XPathExpressionException
    {
    	int totalCSSProps = 0;
    	
    	org.jsoup.nodes.Document document = Jsoup.parse(new File(fileFullPath), null);
		for(org.jsoup.nodes.Element e : document.getAllElements())
		{
			getCSSPropertiesForElement(Utils.getXPathOfElementJava(e));
		}
		return totalCSSProps;
    }
    
    public static void main(String[] args) throws SAXException, IOException, XPathExpressionException
	{
    	//CSSParser cp = new CSSParser("/Users/sonal/USC/rca_search/rca-fix-search/xbi/XPERT/grantabooks/CHROME/output/index.html");
    	CSSParser cp = CSSParser.getInstance("/home/ifix/ifix/TestCases/ScrapBook/data/hightail-test/index.html");
    	String xpath = "/html[1]";
    	Map<String, String> cssMap = cp.getCSSPropertiesForElement(xpath);
		System.out.println("map = " + cssMap);
		System.out.println("searching for width property = " + cssMap.get("width"));
		
		//System.out.println(cp.getTotalNumberOfCSSProperties());
	}
}