package Extractors;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class RRTableExtractor {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) {

	}
	
	//Receives WELL-FORMATTED groups of rows and a table (List<List<String>>) of examples, 
	//and calls the method that extracts the desired attributes for each group.
	//Each group must be parsed in the same way by RoadRunner. Also, each group must have at least one row that 
	//is equivalent to one of the exampleRows.
	//Returns: Table (List<List<String>>) with the information we got from the rows.
	public List<List<String>> extractDesiredAttributes (List<Elements> rowGroups, List<List<String>> exampleRows) {
		List<List<String>> extractedTable = new ArrayList<List<String>>();
		
		for (Elements rows : rowGroups){
			try {
				extractedTable.addAll(extractDesiredAttributes(rows, exampleRows));
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return extractedTable;
	}
	
	//If necessary, let's make a better way of checking if something is indeed a row.
	private boolean hasRowFormat(){
		return true;
	}
	
	
	//Receives WELL-FORMATTED rows that have been parsed in the same way and a table (List<List<String>>) with examples.  
	//Well-formatted row is the one that has been parsed by RoadRunner and that has all of the attributes that we're looking
	//for in attribute nodes.
	//At least one of the rows should be equivalent to one of the exampleRows, so we can find from where to get the attributes.
	//Returns: Table (List<List<String>>) with the information we got from the pair of rows.
	private List<List<String>> extractDesiredAttributes (Elements rows, List<List<String>> exampleRows) throws Exception{
		List<List<String>> extractedTable = new ArrayList<List<String>>();
		
		List<String> attrLabels = getRelevantLabels(rows, exampleRows);
		for (Element row : rows){
			List<String> newRow = new ArrayList<String>();
			for (String label : attrLabels){
				Elements query = row.select("attribute[label="+label+"]");
				if (!query.isEmpty()){
					//Gets the first because it should have only one
					newRow.add(query.first().text());
				}
				else {
					newRow.add("|no info found|");
				}
			}
			extractedTable.add(newRow);
		}
		
		return extractedTable;
	}
	
	
	
	//First Version only gets the children of the table. The "hasRowFormat()" method has yet to be implemented if necessary.
	public Elements getRows(Element table, List<List<String>> exampleRows) throws Exception{

		Elements rows = new Elements();
		for (Element possibleRow : table.children()){
			if (hasRowFormat()){
				rows.add(possibleRow);
			}
		}
		if (rows.isEmpty()) throw new Exception ("It was not possible to identify rows like the ones from the example in this table");
		return rows;
	}
	
	
	//Returns the List of labels from which we can extract the attributes equivalent to the ones in exampleRows. 
	//The list of labels is in the order correspondent to the order of the attributes in the exampleRows.
	//The exampleRows must be all of the same size.
	private List<String> getRelevantLabels(Elements rows, List<List<String>> exampleRows) throws Exception {
		
		List<String> labels = new ArrayList<String>();
		int numberOfAttributes = exampleRows.get(0).size();
		//Checks if all the exampleRows are of the same size
		for (List<String> exampleRow : exampleRows){
			if (exampleRow.size()!=numberOfAttributes) throw new Exception ("The exampleRows have different sizes");
		}
		
		//Finds the most probable label for each attribute
		for (int i=0; i<numberOfAttributes; i++){
			//First we make the list of examples of the attribute...
			List<String> attributes = new ArrayList<String>();
			for (List<String> exampleRow : exampleRows){
				attributes.add(exampleRow.get(i));
			}
			//...then we use it to get the label
			labels.add(getAttributeLabel(rows, attributes));
		}
		
		return labels;
	}
	

	
	//Given rows found and parsed by the algorithm (at least one must be equivalent to one of the example rows), 
	//and a list of 'examples of one of the attributes' extracted from the example rows, 
	//returns the most probable label for that attribute. With this label we can get the information from rows that were 
	//parsed in the same way.
	private String getAttributeLabel(Elements rows, List<String> attrExamples) throws Exception{
		if (rows.isEmpty() || attrExamples.isEmpty()){
			throw new Exception("It is not possible to find the label: there are no rows or attribute examples");
		}
		
		Map<String,Integer> labelCounter = new HashMap<String, Integer>();
		for (int i=0; i<rows.size(); i++){
			Element row = rows.get(i);
			if (row!=null){
				for (String attrExample : attrExamples){
					//For each row, if the attrExample appears is more than one label, we select the smaller one
					//This is an heuristics made to select the most probable attribute label: the larger the content,
					//the higher the chance it has some word that we're looking for. Our "match" is better when it is of the same
					//size of what we're looking for, and since we're never getting anything smaller than it, then the smaller 
					//the match, the better.
					int smallerSize = 99999;
					String bestLabelForRowAndAttr = null;
					for (Element attrElement : row.getAllElements().select(":containsOwn("+attrExample+")")){	
						if (attrElement.text().length()<smallerSize && attrElement.hasAttr("label")){
							smallerSize = attrElement.text().length();
							bestLabelForRowAndAttr = attrElement.attr("label");
						}
					}
					if (bestLabelForRowAndAttr!=null){
						if (labelCounter.containsKey(bestLabelForRowAndAttr)){
							labelCounter.put(bestLabelForRowAndAttr, labelCounter.get(bestLabelForRowAndAttr)+1);
						}
						else{
							labelCounter.put(bestLabelForRowAndAttr, 1);
						}
					}
				}
				
			}
			
		}
		String bestLabel = null;
		int bestCounter = 0;
		for (String currentLabel: labelCounter.keySet()){
			int currentCounter = labelCounter.get(currentLabel); 
			if (currentCounter>bestCounter) {
				bestLabel = currentLabel;
				bestCounter = currentCounter;
			}
		}
		if (bestLabel==null){
			throw new Exception("It is not possible to find the label: the rows do not have any of the example attributes");
		}
		return bestLabel;
	}

	//gets an element that has, inside of it, text matching with every attribute from every exampleRow
	public Elements getDeepestTablesWithExamples(Elements candidateTables, List<List<String>> exampleRows) {
		
		//First, we get the tables that have both (at least two? all? I have to decide this) of the example keys
		for (List<String> exampleRow : exampleRows){
			for (String attribute : exampleRow){
				candidateTables.retainAll(candidateTables.select(":contains("+attribute+")"));
			}
		}
		//If we find no elements with all the key examples, we return an empty list.
		if(candidateTables.isEmpty()) return candidateTables;
		
		//Now, we keep only the elements that not only have all the examples, but that are also not a superset of others that have.
		Elements desiredTables = new Elements();
		for (Element table : candidateTables){
			boolean isNotSuperset = true;
			for (Element otherTable : candidateTables){
				if (!table.equals(otherTable) && table.getAllElements().contains(otherTable)) {
					isNotSuperset=false;
					break;
				}
			}
			if (isNotSuperset){
				desiredTables.add(table);
			}
		}
		
		return desiredTables;
	}
	
	public void writeFormattedTable (Element table, String outputPath) throws IOException{
		FileWriter fw = new FileWriter(outputPath);
		BufferedWriter bw = new BufferedWriter(fw);
		
		bw.write(Jsoup.parseBodyFragment(table.toString()).toString());
		bw.close();
	}


	public void printTables(Elements tables) {
		for (Element table : tables){
			System.out.println(table.toString());
		}
	}

	public void printTable(List<List<String>> table) {
		for (List<String> row : table){
			for (String s : row){
				System.out.print(s + " - ");
			}
			System.out.println("");
		}
	}
	
}
