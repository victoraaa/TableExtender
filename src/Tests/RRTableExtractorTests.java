package Tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import Extractors.RRTableExtractor;

// I SHOULD MAKE TESTS RELATED TO THE EXTRACT DESIRED ATTRIBUTES METHOD. 
public class RRTableExtractorTests {

	RRTableExtractor te = new RRTableExtractor();
	
	List<String> examples;
	List<List<String>> examplesList;
	Document doc;
	String htmlBody;
	List<String> row1;
	List<String> row2;
	List<String> keys;
	
	List<Elements> rowGroups;
	Elements rowGroup;
	
	@Before
	public void setUp(){
		examples = new ArrayList<String>();
		examples.add("example1");
		examples.add("example2");
		
		examplesList = new ArrayList<List<String>>();
		examplesList.add(examples);
		
		row1 = new ArrayList<String>();
		row1.add("key1");
		row1.add("attribute1");
		
		row2 = new ArrayList<String>();
		row2.add("key2");
		row2.add("attribute2");
		
		rowGroup = new Elements();
		rowGroups = new ArrayList<Elements>();
	}
	
	@Test //Test that checks whether we get the simplest table, with only the desired fields
	public void getsSimpleTable(){
		//old test
		htmlBody = "<table> <tbody> <tr> <td> example1 </td> </tr> <tr> <td> example2 </td> </tr> </tbody> </table>";
		doc = Jsoup.parseBodyFragment(htmlBody);
		Elements results = te.getDeepestTablesWithExamples(doc.getAllElements(),examplesList); 
		Elements expected = doc.select("tbody");
		assertTrue(results.equals(expected));
		assertTrue(results.size()==1);
		//new test
		htmlBody = "<div> <div> <div> example1 </div> </div> <div> <div> example2 </div> </div> </div>";
		doc = Jsoup.parseBodyFragment(htmlBody);
		results = te.getDeepestTablesWithExamples(doc.getAllElements(),examplesList);
		expected = new Elements();
		expected.add(doc.select("div").first());
		assertTrue(results.equals(expected));
		assertTrue(results.size()==1);
	}

	@Test //Test that checks whether we get more than one simple table if they satisfy the requirements
	public void getsTwoSimpleTables(){
		//old test
		htmlBody = "<table> <tbody> <tr> <td> example1 </td> </tr> <tr> <td> example2 </td> </tr> </tbody> </table> " +
				"<table> <tbody> <tr> <td> example1 </td> </tr> <tr> <td> example3 </td> </tr> <tr> <td> example2 </td> </tr> </tbody> </table>";
		doc = Jsoup.parseBodyFragment(htmlBody);
		Elements results = te.getDeepestTablesWithExamples(doc.getAllElements(), examplesList); 
		Elements expected = doc.select("tbody");
		assertTrue("Should return the two tables",results.equals(expected));
		assertTrue("Should return 2 tables",results.size()==2);
		//new test
		htmlBody = "<div> <div> <div> example1 </div> </div> <div> <div> example2 </div> </div> </div> " +
				"<div> <div> <div> example1 </div> </div> <div> <div> example3 </div> </div> <div> <div> example2 </div> </div> </div>";
		doc = Jsoup.parseBodyFragment(htmlBody);
		results = te.getDeepestTablesWithExamples(doc.getAllElements(), examplesList); 
		expected = new Elements();
		expected.add(doc.select("div").first());
		expected.add(doc.select("div").get(5));
		assertTrue("Should return the two tables",results.equals(expected));
		assertTrue("Should return 2 tables",results.size()==2);
	}
	
	@Test //Test that checks whether we don't get a table if has some of the examples, but not all of them
	public void getsOnlyTableWithAllExamples(){
		//old test
		htmlBody = "<table> <tbody> <tr> <td> example1 </td> </tr> <tr> <td> example2 </td> </tr> </tbody> </table> " +
				"<table> <tbody> <tr> <td> example1 </td> </tr> <tr> <td> exampleNOT2 </td> </tr> </tbody> </table>";
		doc = Jsoup.parseBodyFragment(htmlBody);
		Elements results = te.getDeepestTablesWithExamples(doc.getAllElements(), examplesList); 
		Elements expected = Jsoup.parseBodyFragment("<table> <tbody> " +
				"<tr> <td> example1 </td> </tr> <tr> <td> example2 </td> </tr> </tbody> </table>").select("tbody"); 
		assertTrue("Should return only first table",results.toString().equals(expected.toString()));
		assertTrue("Should return just 1 table",results.size()==1);
		//new test
		htmlBody = "<div> <div> <div> example1 </div> </div> <div> <div> example2 </div> </div> </div>" +
				"<div> <div> <div> example1 </div> </div> <div> <div> exampleNOT2 </div> </div> </div>";
		doc = Jsoup.parseBodyFragment(htmlBody);
		results = te.getDeepestTablesWithExamples(doc.getAllElements(), examplesList); 
		expected = new Elements(); 
		expected.add(Jsoup.parseBodyFragment("<div> <div> <div> example1 </div> </div> <div> <div> example2 </div> </div> </div>").select("div").first());	 
		assertTrue("Should return only first table",results.toString().equals(expected.toString()));
		assertTrue("Should return just 1 table",results.size()==1);
	}

	@Test //Test that checks whether we get only the smaller tables, and not its supersets
	public void getsOnlySmallerTableWithAllExamples(){
		//old test
		htmlBody = "<table> <tbody>" +
				"<tr> <td> opa </td> </tr> " +
				"<tr> <td> <table> <tbody> <tr> <td> example1 </td> </tr> <tr> <td> example2 </td> </tr> </tbody> </table> </td> </tr> " +
				"</tbody> </table>";
		doc = Jsoup.parseBodyFragment(htmlBody);
		Elements results = te.getDeepestTablesWithExamples(doc.getAllElements(), examplesList); 
		Elements expected = Jsoup.parseBodyFragment("<table> <tbody> <tr> <td> example1 </td> </tr> <tr> " +
				"<td> example2 </td> </tr> </tbody> </table>").select("tbody"); 
		assertTrue("Should return only smaller table",results.toString().equals(expected.toString()));
		assertTrue("Should return just 1 table",results.size()==1);
		//new test
		htmlBody = "<div> " +
				"<div> <div> opa </div> </div> " +
				"<div> <div> " +
				"<div> <div> <div> <div> example1 </div> </div> <div> <div> example2 </div> </div> </div> </div> " +
				"</div> </div> " +
				"</div>";
		doc = Jsoup.parseBodyFragment(htmlBody);
		results = te.getDeepestTablesWithExamples(doc.getAllElements(), examplesList); 
		expected = new Elements(); 
		expected.add(Jsoup.parseBodyFragment("<div> <div> <div> <div> example1 </div> </div> <div> " +
				"<div> example2 </div> </div> </div> </div>").select("div").get(1));		 
		assertTrue("Should return only smaller table",results.toString().equals(expected.toString()));
		assertTrue("Should return just 1 table",results.size()==1);
	}
	
	@Test //Test with an incomplete table as a row
	public void getsOnlyTableWithAllExamplesWithInsiderTable(){
		//old test
		htmlBody = "<table> <tbody> " +
				"<tr> <td> example1 </td> </tr> " +
				"<tr> <td> <table> <tbody> <tr> <td> example2 </td> </tr> </tbody> </table> </td> </tr> " +
				"</tbody> </table>";
		doc = Jsoup.parseBodyFragment(htmlBody);
		Elements results = te.getDeepestTablesWithExamples(doc.getAllElements(), examplesList); 
		Elements expectedResult = Jsoup.parseBodyFragment(htmlBody).select("tbody:contains(example1)"); 
		assertTrue("Should return only table with both examples",results.toString().equals(expectedResult.toString()));
		assertTrue("Should return just 1 table",results.size()==1);
	}
	
	@Test //Test if we're extracting the information we want
	public void simpleExtraction() throws IOException{
		File file = new File("testFiles/parsedCitiesWithNon-AttributeNode.xml");
		doc = Jsoup.parse(file, "iso-8859-1");
		rowGroup = doc.select("instance");
		rowGroups.add(rowGroup);
		List<String> example = new ArrayList<String>();
		example.add("80.9");
		example.add("Baltimore");
		List<List<String>> exampleRow = new ArrayList<List<String>>();
		exampleRow.add(example);
		
		List<String> extractedRow = new ArrayList<String>();
		extractedRow.add("59.2");
		extractedRow.add("Spokane");
		List<List<String>> results = te.extractDesiredAttributes(rowGroups, exampleRow);
		assertTrue("Should get the example row", results.get(0).get(0).equals(exampleRow.get(0).get(0)) && 
				results.get(0).get(1).equals(exampleRow.get(0).get(1)) );
		assertTrue("Should get the other row", results.get(1).equals(extractedRow));
	}
	
	
	
}
