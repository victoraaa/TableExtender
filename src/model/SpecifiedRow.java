package model;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;

//A specified row is a row with its HTML and the attributes that we are looking for.
public class SpecifiedRow {
	public Element row;
	public List<String> attributes;
	
	public SpecifiedRow (Element row, List<String> attributes){
		this.row = new Element(row.tag(),row.baseUri(),row.attributes());
		this.row.html(row.html());
		this.attributes = new ArrayList<String>(attributes);
	}
}
