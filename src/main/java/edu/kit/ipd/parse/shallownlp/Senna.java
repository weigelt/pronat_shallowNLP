package edu.kit.ipd.parse.shallownlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a facade for SENNA
 * 
 * @author Markus Kocybik
 * @author Sven Scheu - revised code on 24-02-2016
 * @author Sebastian Weigelt - revised code on 26-02-2016
 */
public class Senna {

	private static final Logger logger = LoggerFactory.getLogger(Senna.class);

	/**
	 * This method excecutes SENNA as a seperate process. This method is only
	 * executable on a Windows machine.
	 */
	public WordPosType parse() {
		String input = ResourceReader.getURL(this, "input.txt");
		String output = ResourceReader.getURL(this, "output.txt");
		this.excecuteSenna(input, output);
		return readFile(output);
	}

	/**
	 * Creates the process to run Senna
	 * 
	 * @param resourcePath
	 *            the path where Senna is located
	 * @param input
	 *            the path of the file used as input file
	 * @param output
	 *            the path of the file used as output file
	 * @return the process Senna runs in
	 */
	private ProcessBuilder createSennaProcess(Path resourcePath, String input, String output) {
		String os = System.getProperty("os.name", "generic").toLowerCase();
		ProcessBuilder pb;
		if (os.contains("darwin") || os.contains("mac")) {
			pb = new ProcessBuilder(resourcePath.toString() + "/senna-osx");
		} else if (os.contains("nux")) {
			pb = new ProcessBuilder(resourcePath.toString() + "/senna-linux64");
		} else if (os.contains("win") && System.getenv("ProgramFiles(x86)") != null) {
			pb = new ProcessBuilder(resourcePath.toString() + "/senna.exe");
		} else {
			pb = new ProcessBuilder(resourcePath.toString() + "/senna-win32.exe");
		}
		pb.redirectInput(new File(input));
		pb.redirectOutput(new File(output));
		pb.directory(new File(resourcePath.toString()));
		return pb;
	}

	/**
	 * This method executes SENNA with the windows terminal
	 */
	private void excecuteSenna(String input, String output) {
		try {
			URL resourceUrl = getClass().getResource("/senna");
			Path resourcePath = Paths.get(resourceUrl.toURI());
			//Process p = Runtime.getRuntime().exec("cmd /c start /wait /d \"" 
			//				+ resourcePath.toString() + "\" test.bat " + input + " " + output);
			ProcessBuilder builder = createSennaProcess(resourcePath, input, output);
			Process p = builder.start();
			if (p.waitFor() != 0) {
				String error;
				BufferedReader br = null;
				StringBuilder sb = new StringBuilder();

				String line;
				try {
					br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				error = sb.toString();
				throw new RuntimeException("SENNA finished with status: " + p.exitValue() + "\nMessage:\n" + error);
			}
		} catch (Exception e) {
			logger.error("Error: Parsing with SENNA failed");
			logger.error(e.getMessage());
		}
	}

	/**
	 * This method reads the parse result of SENNA.
	 * 
	 * @param path
	 *            of the file
	 * @return the parse result of SENNA
	 */
	private WordPosType readFile(String path) {
		List<String> words = new ArrayList<String>();
		List<String> pos = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.trim().equals("")) {
					String[] tokens = line.trim().split("\\s+");
					words.add(tokens[0]);
					pos.add(tokens[1]);
				}
			}
		} catch (IOException e) {
			logger.error("Something went wrong");
			;
		}
		return new WordPosType(words.toArray(new String[words.size()]), pos.toArray(new String[pos.size()]));
	}

}
