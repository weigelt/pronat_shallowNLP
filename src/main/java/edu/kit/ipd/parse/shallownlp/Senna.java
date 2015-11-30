package edu.kit.ipd.parse.shallownlp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class represents a facade for SENNA
 * @author Markus Kocybik
 */
public class Senna {

	private static final Logger logger = LoggerFactory.getLogger(Senna.class);

	
	/**
	 * This method excecutes SENNA as a seperate process.  
	 * This method is only executable on a Windows machine. 
	 */
	public WordPosType parse()
	{
		String input = ResourceReader.getURL(this, "input.txt"); 
		String output = ResourceReader.getURL(this, "output.txt");
		this.excecuteSenna(input, output);
		return readFile(output);
	}

	/**
	 * This method executes SENNA with the windows terminal
	 */
	private void excecuteSenna(String input, String output)
	{
		try {
			URL resourceUrl = getClass().getResource("/senna");
			Path resourcePath = Paths.get(resourceUrl.toURI());
			Process p = Runtime.getRuntime().exec("cmd /c start /wait /d \"" 
							+ resourcePath.toString() + "\" test.bat " + input + " " + output);
			p.waitFor();
		} catch (Exception e) {
			logger.error("Error: Parsing with SENNA failed");
		}
	}
	
	/**
	 * This method reads the parse result of SENNA.
	 * @param path of the file
	 * @return the parse result of SENNA
	 */
	private WordPosType readFile(String path)
	{
		List<String> words = new ArrayList<String>();
		List<String> pos = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(path)))
		{
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.trim().equals(""))	{
					String [] tokens = line.trim().split("\\s+");
					words.add(tokens[0]);
					pos.add(tokens[1]); 
				}
			}
		} catch (IOException e) {
			logger.error("Something went wrong");;
		}
		return new WordPosType(words.toArray(new String[words.size()]), pos.toArray(new String[pos.size()]));
	}
	
	
}
