package edu.gatech.xpert.dom;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonParser {
	final static Logger logger = LoggerFactory.getLogger(JsonParser.class);

	public DomNode parseJson(String domStr,String url) {
		// Initialize DOM tree root
		Map<Integer, DomNode> domMap = new HashMap<Integer, DomNode>();
		DomNode rootNode = new DomNode("HTML", "/HTML");
		domMap.put(-1, rootNode);
		try {
			JSONArray arrDom = new JSONArray(domStr.trim());
			for (int i = 0; i < arrDom.length(); i++) {
				JSONObject nodeData = arrDom.getJSONObject(i);
				DomNode node = getDomNode(nodeData,url);
				// Rare case 
				if(node.isTag() && node.getTagName().startsWith("/")){
					logger.warn("node tag name starts with /");
					continue;
				}
				
				domMap.put(nodeData.getInt("nodeid"), node);
				
				int parentId = getInt(nodeData, "pid");
				if(domMap.containsKey(parentId)){
					DomNode parent = domMap.get(parentId);
					parent.addChild(node);
				}
			}
		} catch (JSONException e) {
			System.err.println("JSON Exception while parsing : \n" + domStr);
			e.printStackTrace();
			return null;
		}

		return rootNode;
	}

	private DomNode getDomNode(JSONObject nodeData, String url) throws JSONException {
		DomNode node = null;
		int type = getInt(nodeData, "type");
		boolean visible = getBoolean(nodeData, "visible");
		boolean layout = getBoolean(nodeData, "layout");
		
		String color = getString(nodeData, "color");
		String className = getString(nodeData, "className");
		String font = getString(nodeData, "font");
		double fontSize = getDouble(nodeData, "fontSize");
		
		
		String xPath = getString(nodeData, "xpath");
		int[] coords = getCoords(nodeData,false);
		int[] innerCoords = getCoords(nodeData,true);

		node = new DomNode(type , xPath , url, visible , layout, color, className, font, fontSize, parseTagName(xPath),innerCoords, coords);
		return node;
	}

	public static int[] getCoords(JSONObject ob, boolean inner) {
		try {
			JSONArray data;
			if(inner)
				data = ob.getJSONArray("innerCoord");			
			else 
				data = ob.getJSONArray("coord");

			int[] retval = { data.getInt(0), data.getInt(1), data.getInt(2),
					data.getInt(3) };
			return retval;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "unused" })
	private Map<String, String> getAttributes(JSONObject object) {
		Map<String, String> attributes = new HashMap<String, String>();
		try {
			JSONObject attr = object.getJSONObject("attributes");
			Iterator<String> it = attr.keys();
			while (it.hasNext()) {
				String key = it.next();
				String value = attr.getString(key);
				attributes.put(key.toLowerCase(),
						URLDecoder.decode(value, "UTF-8"));
			}
			return attributes;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}


	private String parseTagName(String xPath) {
		if(xPath == null) {
			return null;
		} else {
			String[] tags = xPath.split("/");
			if (tags.length > 0) {
				return tags[tags.length - 1].replaceAll("\\[[0-9]*\\]", "");
			}
			return null;
		}
	}

	private int getInt(JSONObject ob, String key) {
		try {
			return ob.getInt(key);
		} catch (Exception e) {
			// Value not present
			return -1;
		}
	}
	

	private double getDouble(JSONObject ob, String key) {
		try {
			return ob.getDouble(key);
		} catch (Exception e) {
			// Value not present
			return -1;
		}
	}
	
	private boolean getBoolean(JSONObject ob, String key) {
		try {
			return ob.getBoolean(key);
		} catch (Exception e) {
			// Value not present
			return true;
		}
	}

	private String getString(JSONObject ob, String key) {
		try {
			return ob.getString(key);
		} catch (Exception e) {
			// Value not present
			return null;
		}
		
	}
}
