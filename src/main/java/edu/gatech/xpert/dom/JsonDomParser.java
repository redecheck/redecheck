package edu.gatech.xpert.dom;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonDomParser {
	public DomNode parseJsonDom(String domStr) {
		// Initialize DOM tree root
		Map<Integer, DomNode> domMap = new HashMap<>();
		DomNode rootNode = new DomNode("HTML", "/HTML");
		domMap.put(-1, rootNode);

		try {
			JSONArray arrDom = new JSONArray(domStr.trim());
			for (int i = 0; i < arrDom.length(); i++) {
				JSONObject nodeData = arrDom.getJSONObject(i);
				DomNode node = getDomNode(nodeData);
				
				// Rare case 
				if(node.isTag() && node.getTagName().startsWith("/")) continue;
				
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

	private DomNode getDomNode(JSONObject nodeData) throws JSONException {
		DomNode node = null;
		int type = getInt(nodeData, "type");
		switch (type) {
		case 0:
			node = new DomNode(getString(nodeData, "text"));
			break;
		case 1:
			String xPath = getString(nodeData, "xpath");
			node = new DomNode(parseTagName(xPath), xPath);
			node.setId(getString(nodeData, "id"));
			node.setAttributes(getAttributes(nodeData));
			node.setCoords(getCoords(nodeData));
			try {
				node.setZindex(nodeData.getInt("zindex"));
			} catch (Exception e) { /* Missing zindex */ }
			break;
		default:
			System.err.println("Unknown node type:" + type);
		}
		
		return node;
	}

	public static int[] getCoords(JSONObject ob) {
		try {
			JSONArray data = ob.getJSONArray("coord");
			int[] retval = { data.getInt(0), data.getInt(1), data.getInt(2),
					data.getInt(3) };
			return retval;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

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

	private String getString(JSONObject ob, String key) {
		try {
			return ob.getString(key);
		} catch (Exception e) {
			// Value not present
			return null;
		}
		
	}

}