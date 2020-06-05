package edu.gatech.xpert.dom;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import edu.gatech.xpert.dom.visitors.DomVisitor;
import edu.usc.util.CSSParser;


public class DomNode implements Serializable{

	// Constructors

	private NodeType nodeType;
	private String tagName, xPath, pageURL, text, id;
	private Map<String, String> attributes;
	private int zindex, level;
	private int[] coords;
	private int[] innerCoords;

	private long hash;
	private boolean clickable, visible;
	private List<DomNode> children;
	private DomNode parent;
	private boolean matched;
	private boolean layout;
	
	private String color;
	private String className;
	private String font;
	private double fontSize;
	private Map<String, String> explicitCSS;



	public DomNode(int Type, String xPath,String url, boolean visible, 
			boolean layout,	String color, String className, 
			String font, double fontSize, String tagName,
			int[] innerCoords, int[] coords) {
		
		this.setNodeType(getNodeType(Type));
		this.setxPath(xPath);
		this.visible = visible;
		this.pageURL = url;
		this.setCoords(coords);
		this.setInnerCoords(innerCoords);
		this.setTagName(tagName);
		this.setAttributes(new HashMap<String, String>());
		this.layout = layout;
		this.color = color;
		this.className = className;
		this.font = font;
		this.fontSize = fontSize;
		
		this.children = new ArrayList<DomNode>();
	}


	public DomNode(DomNode node) {
		this.nodeType = node.nodeType;

		this.xPath = node.xPath;
		this.visible = node.visible;
		this.coords = node.coords;
		this.innerCoords = node.innerCoords;
		this.tagName = node.tagName;
		this.attributes = node.attributes;
		this.layout = node.layout;
		this.color = node.color;
		this.className = node.className;
		this.font = node.font;
		this.fontSize = node.fontSize;
		this.children = node.children;
		this.explicitCSS = node.explicitCSS;
		this.pageURL = node.pageURL;
	}

	public DomNode(String tagName, String xPath) {
		this.setTagName(tagName);
		this.setNodeType(NodeType.TAG);
		this.setxPath(xPath);
		this.children = new ArrayList<DomNode>();
		this.setAttributes(new HashMap<String, String>());
	}
	
	

	// Accept Visitors

	public void accept(DomVisitor visitor, boolean tagNodesOnly) {
		visitor.visit(this);

		for (DomNode child : this.children) {
			if (child.nodeType == NodeType.TAG || !tagNodesOnly) {
				child.accept(visitor, tagNodesOnly);
			}
		}

		visitor.endVisit(this);
	}

	public void accept(DomVisitor visitor) {
		accept(visitor, false);
	}

	public Map<String, String> getExplicitCSS() {
		if(explicitCSS == null){
			computeCSSValues();
		}
		return explicitCSS;
	}

	public void setExplicitCSS(Map<String, String> explicitCSS) {
		this.explicitCSS = explicitCSS;
	}
	
	private void computeCSSValues(){
		CSSParser cp = CSSParser.getInstance(pageURL);
		
		Map<String, String> cssMap = new HashMap<>();
		try
		{
			cssMap = cp.getCSSPropertiesForElement(getxPath());
		}
		catch (XPathExpressionException | IOException e)
		{
			e.printStackTrace();
		}
		setExplicitCSS(cssMap);
	}


	// Variable Declarations

	public enum NodeType {
		TAG, TEXT, INPUTTEXT
	}
	
	public NodeType getNodeType( int typeidx){
		switch (typeidx) {
		case 0:
			return NodeType.TAG;
		case 1:
			return NodeType.TEXT;
		case 2:
			return NodeType.INPUTTEXT;
		default:
			return NodeType.TAG;
		}
	}


	// Setters & Getters

	public String attr(String key) {
		return this.attributes.get(key);
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	public String getTagName() {
		return tagName == null ? "" : tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getxPath() {
		return xPath;
	}

	public void setxPath(String xPath) {
		this.xPath = xPath;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public int getZindex() {
		return zindex;
	}

	public void setZindex(int zindex) {
		this.zindex = zindex;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int[] getCoords() {
		return coords;
	}

	public int[] getInnerCoords(){
		return innerCoords;
	}

	public void setCoords(int[] coords) {
		this.coords = coords;
	}

	public long getHash() {
		return hash;
	}

	public void setHash(long hash) {
		this.hash = hash;
	}

	public boolean isClickable() {
		return clickable;
	}

	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public String getColor() {
		return color;
	}

	public String getClassName() {
		return className;
	}

	public String getFont() {
		return font;
	}

	public double getFontSize() {
		return fontSize;
	}


	public List<DomNode> getChildren() {
		return children;
	}

	public void setChildren(List<DomNode> children) {
		this.children = children;
	}

	public DomNode getParent() {
		return parent;
	}

	public void setParent(DomNode parent) {
		this.parent = parent;
	}

	public boolean isMatched() {
		return matched;
	}

	public void setMatched(boolean matched) {
		this.matched = matched;
	}

	// Utility Methods

	public void addChild(DomNode node) {
		node.setParent(this);
		this.children.add(0, node);
	}

	
	public boolean isTag() {
		return (nodeType == NodeType.TAG);
	}

	public boolean isText() {
		return (nodeType == NodeType.TEXT);
	}

	public boolean isInputText() {
		return (nodeType == NodeType.INPUTTEXT);
	}
	
	@Override
	public String toString() {
		return tagName + " > " + xPath + "{txt=" + text + "}" + attributes;
	}

	public Map<String, String> getDynDomData() {
		Map<String, String> domData = new HashMap<String, String>();
		domData.put("clickable", isClickable() ? "t" : "f");
		domData.put("visible", isVisible() ? "t" : "f");
		if (zindex != Integer.MIN_VALUE) {
			domData.put("zindex", String.format("%d", zindex));
		}
		//domData.put("children", String.format("%d", children.size()));
		return domData;
	}

	public boolean isLayout() {
		return layout;
	}

	@Override
	public boolean equals(Object obj) {
		
		return (obj instanceof DomNode && ((DomNode)obj).xPath.equals(this.xPath));
	}


	/**
	 * @param innerCoords the innerCoords to set
	 */
	public void setInnerCoords(int[] innerCoords) {
		this.innerCoords = innerCoords;
	}

	
}
