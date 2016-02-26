package edu.kit.ipd.parse.shallownlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.tools.ConfigManager;

/**
 * This class represents a facade for SENNA
 * 
 * @author Markus Kocybik
 * @author Sven Scheu - revised code on 24-02-2016
 * @author Sebastian Weigelt - revised code on 26-02-2016
 */
public class Senna {

	private static final Logger logger = LoggerFactory.getLogger(Senna.class);
	Properties props;

	/**
	 * Default Constructor that simply reads the Properties
	 */
	Senna() {
		props = ConfigManager.getConfiguration(getClass());
	}

	/**
	 * This method excecutes SENNA as a seperate process. This method is only
	 * executable on a Windows machine.
	 * 
	 * @param tempFile
	 *            the temporary input file to use
	 * @throws IOException
	 *             throws exception if something within file handling goes wrong
	 * @throws InterruptedException
	 *             throws exception if the process SENNA runs in is interrupted
	 * @throws URISyntaxException
	 *             throws exception if something in the URL creation of the
	 *             SENNA resource path goes wrong
	 */
	WordPosType parse(File tempInputFile) throws IOException, URISyntaxException, InterruptedException {
		File outputFile = excecuteSenna(tempInputFile);
		return readFile(outputFile);
	}

	/**
	 * Creates the process to run Senna
	 * 
	 * @param resourcePath
	 *            the path where Senna is located
	 * @param options
	 *            the options to pass, normally just ["-usrtokens","-pos"]
	 * @param tempInputFile
	 *            the file used as input file
	 * @param tempOutputFile
	 *            the file used as output file
	 * @return the process Senna runs in
	 */
	private ProcessBuilder createSennaProcess(Path resourcePath, String[] options, File tempInputFile, File tempOutputFile) {
		String os = System.getProperty("os.name", "generic").toLowerCase();
		ProcessBuilder pb;
		List<String> command = new ArrayList<String>();
		if (os.contains("darwin") || os.contains("mac")) {
			command.add(resourcePath.toString() + "/senna-osx");
		} else if (os.contains("nux")) {
			command.add(resourcePath.toString() + "/senna-linux64");
		} else if (os.contains("win") && System.getenv("ProgramFiles(x86)") != null) {
			command.add(resourcePath.toString() + "/senna.exe");
		} else {
			command.add(resourcePath.toString() + "/senna-win32.exe");
		}
		command.addAll(Arrays.asList(options));
		logger.trace("Calling the process builder with: {}", command.toString());
		pb = new ProcessBuilder(command);
		pb.redirectInput(tempInputFile);
		pb.redirectOutput(tempOutputFile);
		pb.directory(new File(resourcePath.toString()));
		return pb;
	}

	/**
	 * This method executes SENNA with the proper executional
	 * 
	 * @param tempInputFile
	 *            the file with the input
	 * @return the file with the output
	 * @throws IOException
	 *             throws exception if something within file handling goes wrong
	 * @throws URISyntaxException
	 *             throws exception if something in the URL creation of the
	 *             SENNA resource path goes wrong
	 * @throws InterruptedException
	 *             throws exception if the process SENNA runs in is interrupted
	 */
	private File excecuteSenna(File tempInputFile) throws IOException, URISyntaxException, InterruptedException {
		File tempOutputFile = File.createTempFile("output", "txt");
		Path resourcePath = Paths.get(getClass().getResource("/senna").toURI());
		ProcessBuilder builder = createSennaProcess(resourcePath, props.getProperty("SENNA_OPTIONS").split(","), tempInputFile,
				tempOutputFile);
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
				throw e;
			} finally {
				if (br != null) {
					br.close();
				}
			}
			error = sb.toString();
			logger.info("SENNA finished with status: " + p.exitValue() + "\nMessage:\n" + error);
		}
		return tempOutputFile;
	}

	/**
	 * This method reads the parse result of SENNA.
	 * 
	 * @param outputFile
	 *            of the file
	 * @return the parse result of SENNA
	 * @throws IOException
	 *             throws exception if something during creation or usage of the
	 *             buffered reader for the output file goes wrong
	 */
	private WordPosType readFile(File outputFile) throws IOException {
		List<String> words = new ArrayList<String>();
		List<String> pos = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(outputFile));
		String line;
		while ((line = br.readLine()) != null) {
			if (!line.trim().equals("")) {
				String[] tokens = line.trim().split("\\s+");
				words.add(tokens[0]);
				pos.add(tokens[1]);
			}
		}
		if (br != null) {
			br.close();
		}
		return new WordPosType(words.toArray(new String[words.size()]), pos.toArray(new String[pos.size()]));
	}

}
