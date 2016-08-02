package edu.kit.ipd.parse.shallownlp;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains the heuristic to calculate the instruction number for
 * each word
 * 
 * @author Markus Kocybik
 * @author Tobias Hey - extended Boundary Keywords for temporal Keywords
 *         (2016-07-28)
 */
public class CalcInstruction {
	private static final List<String> if_keywords = Arrays.asList(new String[] { "if", "when", "whenever", "unless" });
	private static final List<String> then_keywords = Arrays.asList(new String[] { "then" });
	private static final List<String> else_keywords = Arrays
			.asList(new String[] { "else", "otherwise", "elseways", "alternatively", "instead", "either", "rather", "oppositely" });

	private static final List<String> temporal_keywords = Arrays.asList(new String[] { "before", "after", "finally", "when", "afterwards",
			"then", "later", "thereupon", "whereupon", "hereupon", "as", "previously" });

	/**
	 * This method calculates the instruction number for each word of the input
	 * text.
	 * 
	 * @param words
	 *            each element represents one word of the input text
	 * @param pos
	 *            the pos tags for each word
	 * @return the instruction number for each word
	 * 
	 * @throws IllegalArgumentException
	 *             throws an exception if word aaray and pos array have
	 *             different lengths
	 */
	public int[] calculateInstructionNumber(String[] words, String[] pos) throws IllegalArgumentException {
		if (words.length == pos.length) {
			int[] list = new int[words.length];
			int instrNr = 0;
			int verbCounter = 0;
			for (int i = 0; i < words.length; i++) {

				if (isInstructionBoundary(words[i])) {
					// no verb in between boundaries resets instructionNumber
					// and extends previous instruction
					if (verbCounter == 0) {
						resetLastInstruction(list, instrNr);
						list[i] = instrNr;

					} else {
						verbCounter = 0;
						instrNr++;
						list[i] = instrNr;
					}
				} else {
					// search Verb
					if (verbCounter == 0) {
						// special case: two verbs in a row
						if (pos[i].startsWith("VB") && i < words.length - 1 && pos[i + 1].startsWith("VB")) {
							list[i] = instrNr;
							list[i + 1] = instrNr;
							i++;
							verbCounter++;
						}
						// verb found
						else if (pos[i].startsWith("VB")) {
							list[i] = instrNr;
							verbCounter++;
						}
						// no verb found
						else {
							list[i] = instrNr;
						}
					} else {
						// another verb also initiates a new instruction
						// (imperative sentence)
						if (pos[i].startsWith("VB")) {
							instrNr++;
							verbCounter = 0;
							i--; // repeat loop
						} else {
							list[i] = instrNr;
						}
					}
				}

			}
			return list;
		} else {
			throw new IllegalArgumentException("word array and pos array have different lengths");
		}

	}

	private boolean isInstructionBoundary(String word) {
		if (word.toLowerCase().equals("and") || word.toLowerCase().equals("or") || word.toLowerCase().equals("but")
				|| temporal_keywords.contains(word.toLowerCase()) || if_keywords.contains(word.toLowerCase())
				|| then_keywords.contains(word.toLowerCase()) || else_keywords.contains(word.toLowerCase())) {
			return true;
		}
		return false;
	}

	private void resetLastInstruction(int[] list, int instrNum) {
		int number = 0;
		for (int i = 0; i < list.length; i++) {
			if (list[i] > number && list[i] < instrNum) {
				number = list[i];
			}
			if (list[i] == instrNum) {
				list[i] = number;
			}
		}

	}
}