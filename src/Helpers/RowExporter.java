package Helpers;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class RowExporter {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws IOException {
		//RowExporter.reformatFile("C:/Users/Victor/workspace/RoadRunner/examples/gameRows/diablo3.html",
		//		"C:/Users/Victor/workspace/RoadRunner/examples/gameRows/diablo3ok.html");
		//RowExporter.reformatFile("C:/Users/Victor/workspace/RoadRunner/examples/gameRows/minecraft.html",
		//		"C:/Users/Victor/workspace/RoadRunner/examples/gameRows/minecraftok.html");
		RowExporter.reformatFile("C:/Users/Victor/workspace/RoadRunner/examples/gameRows/wow.html",
				"C:/Users/Victor/workspace/RoadRunner/examples/gameRows/wowok.html");
	}
	
	//Receives the location of a file and the new location to save a new file with corrected HTML tags.
	//won't work if inputName==outputName
	public static void reformatFile (String inputName, String outputName) throws IOException{
		FileReader fr = new FileReader(inputName);
		BufferedReader br = new BufferedReader(fr);
		
		FileWriter fw = new FileWriter(outputName);
		BufferedWriter bw = new BufferedWriter(fw);
		
		StringBuilder sb = new StringBuilder();
		String line = "";
		while ((line=br.readLine())!=null){
			sb.append(line);
		}
		br.close();
		
		String output = Jsoup.parseBodyFragment(sb.toString()).toString();
		bw.write(output);
		bw.close();
		
	}
	
	//Receives one element and the location where to save it, and writes the file.
	public static void saveRowAsRRInput (Element row, String outputName) throws IOException{
		
		FileWriter fw = new FileWriter(outputName);
		BufferedWriter bw = new BufferedWriter(fw);
		
		//String output = Jsoup.parseBodyFragment(row.toString()).toString();
		String output = row.toString();
		bw.write(output);
		bw.close();
		
	}
	
	//Writes a list of elements to files and returns the string urls of these files.
	public static List<String> saveRowsToUrls (Elements rows) throws IOException{
		List<String> rowUrls = new ArrayList<String>();
		//Each row is saved in a file with the adequate format for RoadRunner
		int i=1;
		for(Element row: rows){
			String rowUrl = "C:/Users/Victor/workspace/TableExtender/rows/row"+i+".html";
			RowExporter.saveRowAsRRInput(row,rowUrl);
			rowUrls.add(rowUrl);
			i++;
		}
		
		return rowUrls;
	}
	
	
}
