package edu.kit.ipd.parse.shallownlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class supports the access to resources in this maven project 
 * @author Markus Kocybik
 */
public class ResourceReader {

	private static final Logger logger = LoggerFactory.getLogger(ResourceReader.class);	
	
	public static String getTextResource(Object obj, String path)
		{
			File output = ResourceReader.getFileResource(obj, path);
			try (BufferedReader br = new BufferedReader(new FileReader(output)))
			{
		        StringBuilder sb = new StringBuilder();
		        String line = br.readLine();
		        while (line != null) 
		        {
		            sb.append(line);
		            sb.append(System.lineSeparator());
		            line = br.readLine();
		        }
		        return sb.toString();
		    }
			catch(IOException e) {
				logger.warn("Resource file could not be found");
				return null;
			}
		}
		
		public static File getFileResource(Object obj, String path)
		{
			String src = obj.getClass().getClassLoader().getResource(path).getFile();
			return new File(src);
		}
		
		public static String getURL(Object obj, String path)
		{
			URL resourceUrl = obj.getClass().getClassLoader().getResource(path);
			Path resourcePath;
			try {
				resourcePath = Paths.get(resourceUrl.toURI());
			} catch (URISyntaxException e) {
				return null;
			}
			return resourcePath.toString();
		}
}