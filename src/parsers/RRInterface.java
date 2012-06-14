package parsers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import model.SpecifiedRow;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Helpers.RowExporter;

public class RRInterface {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	final String MODIFIER = "123AXZKY";
	
	//returns the parsed rows as a collection of Elements, selected by the tag "instance"
	public Elements parseRows (Elements rows) throws IOException{
		
		List<String> rowUrls = RowExporter.saveRowsToUrls(rows);
		
		StringBuilder sb = new StringBuilder(); 
		sb.append("-NrowExtractor ");
		for (String row : rowUrls){
			sb.append(row+" ");
		}
		System.out.println(sb.toString());
		try {
			roadrunner.Shell.main(sb.toString().split(" "));
		}
		catch (Exception e){
			System.out.println(e.getMessage());
		}
		
		File results = new File("output/rowExtractor/rowExtractor0_DataSet.xml");
		Document doc = Jsoup.parse(results, "iso-8859-1");
		Elements parsedRows = doc.select("instance");
		return parsedRows;
	}
	
	public List<Elements> getParsedRowsGroups(Elements rows,List<List<String>> examples){
		
		//This exists to solve the problem of rows that have the same attribute value; if this was not done, 
		//RoadRunner would not find these attributes when the example had the same value of the new row.
		List<SpecifiedRow> exampleRows = getModifiedExampleRows(rows, examples);
		
		//Method that makes the parsing
		List<Elements> parsedGroups = getParsedRowsGroups_exampleAlg(rows, exampleRows);
		
		//In the end, we remove the strings we had put to make attributes unique
		removeModifierString(parsedGroups,examples);

		return parsedGroups;
	}
	
	//Receives rows that will be parsed in a way that the attributes that we want to find will be under attribute-nodes
	//Also receives a list of examples of rows. At least one of the rows must be equivalent to one of the examples
	private List<Elements> getParsedRowsGroups_exampleAlg (Elements rows,List<SpecifiedRow> exampleRows){
		
		List<Elements> parsedGroups = new ArrayList<Elements>();
		
		//We make a copy of rows because we're going to modify it
		Set<Element> elements = new HashSet<Element>(rows);
		Elements copyOfRows = new Elements(elements);
		
		//For each exRow, tries to parse every row. Every parsed row is removed from KASNOFIAF,
		//because we don't need to parse it again.
		for (SpecifiedRow exRow : exampleRows){
			List<Elements> newGroups = parseUsingExample(exRow,copyOfRows);
			if (!newGroups.isEmpty()){
				parsedGroups.addAll(newGroups);
			}
		}
		return parsedGroups;
	}


	//Besides getting the example rows, we modify their attributes to make it less probable of coinciding with others
	private List<SpecifiedRow> getModifiedExampleRows(Elements rows, List<List<String>> examples) {
		List<SpecifiedRow> exampleRows = new ArrayList<SpecifiedRow>();
		
		for (List<String> example : examples){
			Set<Element> elements = new HashSet<Element>(rows);
			Elements candidateRows = new Elements(elements);

			for (String attribute : example){
				candidateRows.retainAll(rows.select(":contains("+attribute+")"));
			}
			if (!candidateRows.isEmpty()){
				for (Element row : candidateRows){
					exampleRows.add(new SpecifiedRow(row, example));
				}
			}
		}
		for (int i=0; i<exampleRows.size();i++){
			SpecifiedRow sr = exampleRows.get(i);
			for (String attr : sr.attributes){
				sr.row.html(sr.row.html().replace(attr,attr+this.MODIFIER));
			}
			for (int j=0; j<sr.attributes.size();j++){
				sr.attributes.set(j, sr.attributes.get(j)+this.MODIFIER);
			}
		}
		
		return exampleRows;
	}
	
	//After making the parsing, we remove the MODIFIER string from our rows and examples
	private void removeModifierString(List<Elements> parsedGroups, List<List<String>> examples) {
		
		for (Elements groups : parsedGroups){
			for (Element row : groups){
				row.html(row.html().replace(this.MODIFIER, ""));
			}
		}
		
	}

	
	//THIS MODIFIES THE 'rows' ARGUMENT.
	//THIS SHOULD NOT BE USED BY ANY OTHER THAN THE EXAMPLE_ALG
	//Tries to parse every row from 'rows' comparing to a SpecifiedRow
	private List<Elements> parseUsingExample(SpecifiedRow exRow, Elements rows) {
		List<Elements> parsedGroups = new ArrayList<Elements>();
		
		Iterator<Element> iterator = rows.iterator();
		while(iterator.hasNext()){
			Element row = iterator.next();
			Elements groupOfRows = new Elements();
			groupOfRows.add(row);

			try {
				Elements parsedPair = tryParsingGroupUsingExample(exRow,groupOfRows);
				if (!parsedPair.isEmpty()){
					parsedGroups.add(parsedPair);
					iterator.remove();
				}	
			}
			catch (Exception e){
				System.err.println(e.getMessage());
			}
		}
		
		return parsedGroups;
	}

	
	
	//Gets a group of rows and tries to parse it, using an example row to make it possible to check if all attributes 
	//may be found under attribute nodes.
	//Returns, in the end, the parsed rows, or an empty Elements if we can't parse.
	private Elements tryParsingGroupUsingExample(SpecifiedRow exRow, Elements group) throws Exception {
		
		if (exRow==null || group.isEmpty()) throw new Exception("Os argumentos não devem ser nulos ou vazios");
		
		group.add(0,exRow.row);
		
		//Now we try to parse them
		Elements parsedRows = new Elements();
		boolean isParsed;
		try {
			parsedRows = parseRows(group);
		}
		catch (Exception e){
			isParsed=false;
		}
		
		isParsed = hasAllAttributesUnderAttributeNodes(parsedRows, exRow.attributes);
		
		if (isParsed) {
			return parsedRows;
		}
		else {
			return new Elements();
		}
		
	}
	
	//Verify if we can find all example attributes under attribute nodes
	private boolean hasAllAttributesUnderAttributeNodes(Elements rows,List<String> attributes){
		
		Set<Element> set = new HashSet<Element>(rows);
		Elements candidateRows = new Elements(set);
		
		for (String attr : attributes){
			candidateRows.retainAll(candidateRows.select(":has(attribute:contains("+attr+"))"));
		}
		if (candidateRows.isEmpty()) return false;
		else return true;
		
	}
	
	//Right now, not used.
	//receives a pair of rows and the attributes of the first one. 
	//Returns the equivalent attributes for the second one
	private List<String> getAllAttributes(Elements rows,List<String> attributes){
		
		Element specifiedRow = rows.first();
		
		List<String> attrs = new ArrayList<String>();
		List<String> labels = new ArrayList<String>();
		
		for (String attr : attributes){
			labels.add(specifiedRow.select("attribute:contains("+attr+")").first().attr("label"));	
		}
		for (String label : labels){
			attrs.add(rows.get(1).select("attribute[label="+label+"]").first().text());
		}
		
		return attrs;
	}
	
	//not used anymore due the differentiation feature
	private List<SpecifiedRow> getExampleRows(Elements rows, List<List<String>> examples) {
		List<SpecifiedRow> exampleRows = new ArrayList<SpecifiedRow>();
		
		for (List<String> example : examples){
			Set<Element> elements = new HashSet<Element>(rows);
			Elements candidateRows = new Elements(elements);

			for (String attribute : example){
				candidateRows.retainAll(rows.select(":contains("+attribute+")"));
			}
			if (!candidateRows.isEmpty()){
				for (Element row : candidateRows){
					exampleRows.add(new SpecifiedRow(row, example));
				}
			}
		}
		
		return exampleRows;
	}

}
