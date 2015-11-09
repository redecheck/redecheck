package edu.gatech.xpert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.crawljax.plugins.crawloverview.model.Edge;
import com.crawljax.plugins.crawloverview.model.OutPutModel;
import com.crawljax.plugins.crawloverview.model.Serializer;
import com.crawljax.plugins.crawloverview.model.State;

import edu.gatech.xpert.dom.DomNode;
import edu.gatech.xpert.dom.JsonDomParser;

public class BrowserModel {
	
	OutPutModel stg; // State transition graph
	Map<String, String> htmlMap;
	Map<String, DomNode> domMap;
	JsonDomParser parser;
	
	public BrowserModel(String outDir) {
		try {
			this.stg = Serializer.read(new File(outDir+File.separator+"result.json"));
			this.parser = new JsonDomParser();
			populateDoms(outDir);
		} catch (IOException e) {
			System.err.println("ERROR: reading browser capture data");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void populateDoms(String outDir) throws IOException {
		this.htmlMap = new HashMap<String, String>();
		this.domMap = new HashMap<String, DomNode>();
		String prefix = outDir + File.separator + "doms" + File.separator;
		for (String state : stg.getStates().keySet()) {
			
			String html = FileUtils.readFileToString(new File(prefix + state + ".html"));
			this.htmlMap.put(state, html);
			
			String json = FileUtils.readFileToString(new File(prefix + state + ".json"));
			DomNode root = parser.parseJsonDom(json);
			this.domMap.put(state, root);
		}
	}

	public OutPutModel stg() {
		return stg;
	}
	
	public Map<String, String> getHtmlMap() {
		return htmlMap;
	}
	
	public String getHtml(String state) {
		return htmlMap.get(state);
	}
	
	public Map<String, DomNode> getDomMap() {
		return domMap;
	}
	
	public DomNode getDom(String state) {
		return domMap.get(state);
	}

	public List<Edge> getOutgoingEdges(State s) {
		String sName = s.getName();
		List<Edge> out = new ArrayList<Edge>();
		for(Edge e: this.stg.getEdges()) {
			if(StringUtils.equals(sName, e.getFrom())){
				out.add(e);
			}
		}
		return out;
	}

}
