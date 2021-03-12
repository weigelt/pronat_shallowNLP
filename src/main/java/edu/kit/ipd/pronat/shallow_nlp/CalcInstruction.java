package edu.kit.ipd.pronat.shallow_nlp;

import edu.kit.ipd.parse.luna.tools.ConfigManager;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * This class contains the heuristic to calculate the instruction number for
 * each word
 * 
 * @author Markus Kocybik
 * @author Tobias Hey - extended Boundary Keywords for temporal Keywords
 *         (2016-07-28)
 * @author Sebastian Weigelt - advanced instruction number calculation
 */
public class CalcInstruction {

	private static Properties props = ConfigManager.getConfiguration(CalcInstruction.class);

	private static final String PROP_LEGACY_CALC_INSTR_MODE = "LEGACY_CALC_INSTR_MODE";
	private static final String PROP_LOOP_KEYWORDS = "LOOP_KEYWORDS";
	private static final String PROP_IF_KEYWORDS = "IF_KEYWORDS";
	private static final String PROP_THEN_KEYWORDS = "THEN_KEYWORDS";
	private static final String PROP_ELSE_KEYWORDS = "ELSE_KEYWORDS";
	private static final String PROP_TEMPORAL_KEYWORDS = "TEMPORAL_KEYWORDS";

	private static final List<String> ifKeywords = Arrays.asList(props.getProperty(PROP_IF_KEYWORDS).split(","));
	private static final List<String> thenKeywords = Arrays.asList(props.getProperty(PROP_THEN_KEYWORDS).split(","));
	private static final List<String> elseKeywords = Arrays.asList(props.getProperty(PROP_ELSE_KEYWORDS).split(","));
	private static final List<String> loopKeywords = Arrays.asList(props.getProperty(PROP_LOOP_KEYWORDS).split(","));
	private static final List<String> temporalKeywords = Arrays.asList(props.getProperty(PROP_TEMPORAL_KEYWORDS).split(","));

	private static boolean legacyCalcInstrMode = Boolean.parseBoolean(props.getProperty(PROP_LEGACY_CALC_INSTR_MODE));

	private static final List<String> haveOrBe = Arrays.asList("have", "has", "had", "is", "am", "are", "been", "was", "were");

	private static final List<String> demonstrativePronoun = Arrays.asList("that", "this", "those", "these");

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
	 *             throws an exception if word array and pos array have different
	 *             lengths
	 */
	int[] calculateInstructionNumber(String[] words, String[] pos) {
		if (words.length == pos.length) {
			if (legacyCalcInstrMode) {
				return calculateInstructionNumberLegacy(words, pos);
			} else {
				return calculateInstructionNumberAdvanced(words, pos);
			}
		} else {
			throw new IllegalArgumentException("word array and pos array have different lengths");
		}

	}

	private boolean isInstructionBoundary(String word) {
		return temporalKeywords.contains(word.toLowerCase()) || ifKeywords.contains(word.toLowerCase())
				|| thenKeywords.contains(word.toLowerCase()) || elseKeywords.contains(word.toLowerCase())
				|| loopKeywords.contains(word.toLowerCase());
		//OLD:
		//if (//word.toLowerCase().equals("and") || word.toLowerCase().equals("or") || word.toLowerCase().equals("but")
		//|| 
		//		temporal_keywords.contains(word.toLowerCase()) || if_keywords.contains(word.toLowerCase())
		//				|| then_keywords.contains(word.toLowerCase()) || else_keywords.contains(word.toLowerCase())) {
		//			return true;
		//		}
		//		return false;
	}

	private int[] calculateInstructionNumberAdvanced(String[] words, String[] pos) {
		int[] interInstTags = new int[words.length];
		int[] resultInstTags = new int[words.length];
		int currInst = 0;
		boolean verbSeen = false;
		boolean inVP = false;
		boolean lastVerbVBGorVBN = false;
		boolean ifSeen = false;

		for (int i = 0; i < words.length; i++) {
			if (ifKeywords.contains(words[i].toLowerCase())) {
				ifSeen = true;
			}
			interInstTags[i] = currInst;
			if (isInstructionBoundary(words[i])
					|| ((words[i].equalsIgnoreCase("and") || words[i].equalsIgnoreCase("or") || words[i].equalsIgnoreCase("but"))
							&& !(pos[i + 1].equals("DT") || pos[i + 1].startsWith("NN") || pos[i + 1].startsWith("JJ")
									|| pos[i + 1].startsWith("CD")))) {
				if (verbSeen) {
					currInst++;
					interInstTags[i] = currInst;
					if (i > 0 && pos[i - 1].startsWith("DT") && !demonstrativePronoun.contains(words[i - 1].toLowerCase())) {
						interInstTags[i - 1] = currInst;
					}
					verbSeen = false;
				}
			} else {
				if (i < words.length - 1 && i > 0 && pos[i].startsWith("PRP") && pos[i + 1].startsWith("TO") && pos[i - 1].startsWith("VBZ")
						&& currInst > 0) {
					interInstTags[i] = currInst - 1;
				}
				if (pos[i].startsWith("VB")) {
					verbSeen = true;
					if (i != 0 && (haveOrBe.contains(words[i - 1])
							|| (i > 1 && (pos[i - 1].startsWith("RB") && haveOrBe.contains(words[i - 1].toLowerCase())))
							|| (i > 1 && (pos[i - 1].startsWith("TO") && pos[i - 2].startsWith("VB")))
							|| (i > 0 && (pos[i].equals("VBG") && pos[i - 1].equals("VB")))
							|| (i > 2 && (pos[i - 1].startsWith("RB") && pos[i - 2].startsWith("TO") && pos[i - 3].startsWith("VB")))
							|| (i > 2 && (pos[i - 1].startsWith("TO") && pos[i - 2].startsWith("PRP") && pos[i - 3].startsWith("VB"))))) {
						inVP = true;
					} else {
						inVP = false;
					}
					if (!inVP) {
						//if (!(i > 0 && interInstTags[i - 1] != currInst)) {
						if (lastVerbVBGorVBN && pos[i].startsWith("VBZ")) {
							if (!((i < words.length - 1 && pos[i + 1].startsWith("TO"))
									//										|| (i < words.length - 2 && pos[i + 2].startsWith("TO") && pos[i + 1].startsWith("PRP"))
									|| (i < words.length - 2 && pos[i + 2].startsWith("PRP") && !pos[i + 1].startsWith("VB"))
									|| (i < words.length - 3 && pos[i + 3].startsWith("PRP") && !pos[i + 1].startsWith("VB")
											&& !pos[i + 2].startsWith("VB")))) {
								interInstTags[i] = currInst;
								currInst++;
							}
						} else if (ifSeen && pos[i].startsWith("VBZ") && !haveOrBe.contains(words[i].toLowerCase())) {
							if (!((i < words.length - 2 && pos[i + 2].startsWith("PRP") && !pos[i + 1].startsWith("VB"))
									|| (i < words.length - 3 && pos[i + 3].startsWith("PRP") && !pos[i + 1].startsWith("VB")
											&& !pos[i + 2].startsWith("VB")))) {
								interInstTags[i] = currInst;
								currInst++;
								ifSeen = false;
							}
						} else if (i > 0 && haveOrBe.contains(words[i]) && demonstrativePronoun.contains(words[i - 1])) {
							if (i < words.length - 1 && pos[i + 1].startsWith("WRB")) {
								currInst++;
								interInstTags[i] = currInst;
								interInstTags[i - 1] = currInst;
							}
						} else {
							currInst++;
							interInstTags[i] = currInst;
							if (i > 0 && (pos[i - 1].startsWith("RB") || pos[i - 1].startsWith("MD"))
									&& !haveOrBe.contains(words[i - 1].toLowerCase())
									|| (i > 1 && pos[i - 1].startsWith("RB") && pos[i - 2].startsWith("MD")
											&& !haveOrBe.contains(words[i - 1].toLowerCase()))) {
								interInstTags[i - 1] = currInst;
							}
						}
						//}
					}

					if (pos[i].startsWith("VBG") || pos[i].startsWith("VBN")) {
						lastVerbVBGorVBN = true;
					} else {
						lastVerbVBGorVBN = false;
					}
				}
				if (i > 0 && pos[i].startsWith("PRP") && pos[i - 1].startsWith("NN")) {
					currInst++;
					interInstTags[i] = currInst;
				}
			}
		}

		currInst = 0;
		verbSeen = false;
		//boolean seenVBonly = true;
		boolean verbAfterWRB = true;
		boolean seenWRB = false;

		for (int i = 0; i < words.length; i++) {

			if (pos[i].startsWith("WRB")) {
				seenWRB = true;
				verbAfterWRB = false;
			}

			if (i != 0 && verbSeen && verbAfterWRB && interInstTags[i] != interInstTags[i - 1]) {
				currInst++;
				verbSeen = false;
				//TODO: Was that ever helpful? I doubt it!
				//				if (seenVBonly && pos[i - 1].startsWith("PRP")) {
				//					resultInstTags[i - 1] = currInst;
				//				}
				//seenVBonly = true;
			}
			resultInstTags[i] = currInst;
			if (pos[i].startsWith("VB")) {
				verbSeen = true;
				if (seenWRB) {
					verbAfterWRB = true;
					seenWRB = false;
				}
				//				if (!pos[i].equals("VB")) {
				//					seenVBonly = false;
				//				}
			}
		}
		return resultInstTags;
	}

	private int[] calculateInstructionNumberLegacy(String[] words, String[] pos) {
		int[] list = new int[words.length];
		int instrNr = 0;
		int verbCounter = 0;
		for (int i = 0; i < words.length; i++) {

			if (isInstructionBoundary(words[i].toLowerCase())) {
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