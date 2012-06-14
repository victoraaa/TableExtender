package tableExtender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import parsers.RRInterface;

import Extractors.RRTableExtractor;

public class Shell {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Shell shell = new Shell();
		Document doc;
		List<List<String>> exampleRows = new ArrayList<List<String>>();
		List<String> row1 = new ArrayList<String>();
		List<String> row2 = new ArrayList<String>();
		List<String> row3 = new ArrayList<String>();
		exampleRows.add(row1);
		exampleRows.add(row2);
		exampleRows.add(row3);
		/*
		//this one is wikipedia
		File wikipedia = new File("SiteExamples/wikiCities.html");
		doc = Jsoup.parse(wikipedia, "iso-8859-1");
		//This one also has cities
		//doc = Jsoup.connect("http://www.factmonster.com/ipka/A0763765.html").get();
		//POPULOUS CITY EXAMPLES
		row1.add("Chicago");
		row1.add("Illinois");
		row2.add("Atlanta");
		row2.add("Georgia");
		row3.add("Baltimore");
		row3.add("Maryland");
		//*/
		//*
		//This one is GAMESPOT
		File gamespot = new File("SiteExamples/gamespot.html");
		doc = Jsoup.parse(gamespot, "iso-8859-1");
		row1.add("Diablo III");
		//row1.add("May 15, 2012");
		row1.add("8.5");
		row1.add("PC");
		row2.add("The Sims 3");
		//row2.add("Jun 2, 2009");
		row2.add("9.0");
		row2.add("PC");
		row3.add("Medieval II: Total War");
		//row3.add("Nov 13, 2006");
		row3.add("8.8");
		row3.add("PC");
		//*/
		/*
		//this one is related to basketball
		doc = Jsoup.connect("http://www.basketball-reference.com/leaders/pts_per_g_career.html").get();
		row1.add("Michael Jordan");
		row1.add("30.12");
		row2.add("Lebron James");
		row2.add("27.64");
		row3.add("Kobe Bryant");
		row3.add("25.40");
		//*/
		/*
		//this one has used cars in providence!
		File file = new File("SiteExamples/usedCars.html");
		doc = Jsoup.parse(file, "iso-8859-1");
		row1.add("2010 Dodge Grand Caravan");
		row1.add("Silver");
		row1.add("$18,299");
		row2.add("2010 Chrysler Town & Country Touring ");
		row2.add("Black");
		row2.add("$21,850");
		row3.add("Used 2000 Chevrolet Silverado ");
		row3.add("Gray");
		row3.add("$5,500");
		//*/
		/*
		//Ebay selling toy story stuff
		File file = new File("SiteExamples/ebayTS.html");
		doc = Jsoup.parse(file, "iso-8859-1");
		row1.add("NWT Toy Story 3 Buzz Lightyear");
		row1.add("$19.99");
		row1.add("Accepted within 14 days");
		row2.add("TOY STORY 2 STINKY PETE THE PROSPECTOR");
		row2.add("$19.99");
		row2.add("Not accepted");
		row3.add("DISNEY Pixar TOY STORY Plush STUFFED Animal PEAS in a POD DOLL Green 8 in PLAY");
		row3.add("$7.99");
		row3.add("Not accepted");
		//*/
		
		//This one is a simple html table, the simplest possible.
		//
		/*
		doc = Jsoup.connect("http://www.w3schools.com/html/html_tables.asp").get();
		//Examples of the table
		row1.add("Apples");
		row1.add("44%");
		row2.add("Bananas");
		row2.add("23%");
		row3.add("Oranges");
		row3.add("13%");
		//*/
		
		//System.out.println(doc.toString());
		//Elements results = gte.getDeepestTablesWithExamples(doc.getAllElements(), exampleRows);
		
		shell.tableExtractorShell(doc, exampleRows);
	}

	RRTableExtractor gte;
	
	public Shell(){
		gte = new RRTableExtractor();
	}
	
	
	//Method used just to make calls easier for me while in development
	//It may be changed to be the public method that is going to be available for other classes.
	public List<List<String>> tableExtractorShell (Document doc, List<List<String>> exampleRows) {
		RRInterface rr = new RRInterface();
		//Table that will be printed as result
		List<List<String>> extractedTable = new ArrayList<List<String>>();
		//Given a Document, we get every element that may be a table and has the information we're looking for.
		Elements tablesWithContent = gte.getDeepestTablesWithExamples(doc.getAllElements(), exampleRows);
		for (Element table : tablesWithContent){
			try {
				//RoadRunner evaluates the rows and parses them. RRInterface returns each row as an Element.
				Elements rows = gte.getRows(table, exampleRows);
				List<Elements> parsedRows = rr.getParsedRowsGroups(rows,exampleRows);
				List<List<String>> parsedInformation = gte.extractDesiredAttributes(parsedRows, exampleRows);
				//We try to not add repeated rows. If we're using a key, this may be the place to implement the code that checks if
				//our key will continue being unique.
				for (List<String> newRow : parsedInformation){
					if (!extractedTable.contains(newRow)){
						extractedTable.add(newRow);
					}
				}
				
			} catch (Exception e) {
				//if we can't get rows, we just won't add rows...
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		gte.printTable(extractedTable);
		return extractedTable;
	}
}
