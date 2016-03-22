package edu.kit.ipd.parse.shallownlp.senna;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SennaErrorReader implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(SennaErrorReader.class);

	final InputStream errorStream;

	public SennaErrorReader(Process p) {
		errorStream = p.getErrorStream();
	}

	@Override
	public void run() {
		InputStreamReader reader = new InputStreamReader(errorStream);
		Scanner scan = new Scanner(reader);
		while (scan.hasNextLine()) {
			logger.error("Error while running SENNA: {}", scan.nextLine());
		}
		scan.close();
	}
}
