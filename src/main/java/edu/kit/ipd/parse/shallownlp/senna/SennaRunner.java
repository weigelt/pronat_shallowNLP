//package edu.kit.ipd.parse.shallownlp.senna;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Scanner;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//class SennaRunner implements Runnable {
//
//	private static final Logger logger = LoggerFactory.getLogger(SennaRunner.class);
//
//	ProcessBuilder pb;
//	Process p;
//	InputStream outStream;
//
//	SennaRunner(ProcessBuilder pb) throws IOException {
//		this.pb = pb;
//		p = pb.start();
//		outStream = p.getInputStream();
//	}
//
//	@Override
//	public void run() {
//		InputStreamReader reader = new InputStreamReader(outStream);
//		Scanner scan = new Scanner(reader);
//		int lineNumber = 0;
//		ArrayList<ArrayList<TagContainer>> tagLines = new ArrayList<>();
//		tagLines.add(new ArrayList<>());
//		int wordCount = 0;
//		while (scan.hasNextLine()) {
//			// TODO fetch results and put into document..
//			for (String tag : Arrays.asList(scan.nextLine().trim().split("\r"))) {
//				if (tag.isEmpty()) { // FIXME SENNA DOES AUTOMATIC LINEBREAK IF SENTENCE HAS MORE THAN 1024 Chars
//					tagLines.add(new ArrayList<>());
//					lineNumber++;
//					wordCount = 0;
//					logger.trace("---");
//
//				} else {
//					logger.trace("{}/{}", doc.getSentences().get(lineNumber).getWords().get(wordCount).getWord(), tag);
//					wordCount++;
//
//					tagLines.get(lineNumber).add(new TagContainer(shortName, new Tag(tag)));//PROBLEM: Senna tagt Zeielnweise 
//				}
//
//			}
//		}
//		lineNumber = 0;
//		for (Sentence s : doc.getSentences()) {
//			s.tagSentence(tagLines.get(lineNumber));
//			lineNumber++;
//		}
//		scan.close();
//
//	}
//}
