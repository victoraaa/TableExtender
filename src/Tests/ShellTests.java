package Tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import tableExtender.Shell;

public class ShellTests {

	Shell shell;
	List<String> examples;
	List<List<String>> examplesList;
	Document doc;
	String htmlBody;
	List<String> row1;
	List<String> row2;
	List<String> keys;
	
	@Before
	public void setUp(){
		shell = new Shell();
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
		
	}
	
	@Test
	public void getsOnlyTablesWithAllExampleRows() {
		Element correctTable = Jsoup.parseBodyFragment("<table> <tr> <td> key1 </td> <td> attribute1 </td> </tr> " +
				"<tr> <td> key2 </td> <td> attribute2 </td> </tr> " +
				"</table>").select("table").first();
		Element tableWithDifferentAttribute = Jsoup.parseBodyFragment("<table> <tr> <td> key1 </td> <td> attribute2 </td> </tr> " +
				"<tr> <td> key2 </td> <td> attribute2 </td> </tr> " +
				"</table>").select("table").first();
		Element tableWithDifferentKey = Jsoup.parseBodyFragment("<table> <tr> <td> key2 </td> <td> attribute1 </td> </tr> " +
				"<tr> <td> key1 </td> <td> attribute2 </td> </tr> " +
				"</table>").select("table").first();
		doc = Jsoup.parseBodyFragment(correctTable.toString()+" "+tableWithDifferentAttribute.toString()+" "+tableWithDifferentKey);
		
		Elements tables = new Elements();
		tables.add(correctTable);
		tables.add(tableWithDifferentAttribute);
		tables.add(tableWithDifferentKey);
		List<List<String>> exampleRows = new ArrayList<List<String>>();
		exampleRows.add(row1);
		exampleRows.add(row2);
		assertTrue("Should only get two rows as results", shell.tableExtractorShell(doc, exampleRows).size()==2);
		assertTrue("Should get row1", shell.tableExtractorShell(doc, exampleRows).get(0).equals(row1));
		assertTrue("Should get row2", shell.tableExtractorShell(doc, exampleRows).get(1).equals(row2));
	}

}
