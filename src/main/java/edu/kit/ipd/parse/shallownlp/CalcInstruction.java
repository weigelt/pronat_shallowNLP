package edu.kit.ipd.parse.shallownlp;

/**
 * This class contains the heuristic to calculate the instruction number for
 * each word
 * 
 * @author Markus Kocybik
 */
public class CalcInstruction {

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
				// looking for a verb
				if (verbCounter == 0) {
					//special case: two verbs in a row
					if (pos[i].startsWith("VB") && i < words.length - 1 && pos[i + 1].startsWith("VB")) {
						list[i] = instrNr;
						list[i + 1] = instrNr;
						i++;
						verbCounter++;
					}
					//verb found
					else if (pos[i].startsWith("VB")) {
						list[i] = instrNr;
						verbCounter++;
					}
					//no verb found
					else {
						list[i] = instrNr;
					}
				}
				//looking for an instruction boundary
				else {
					// these words represent instruction boundaries
					if (words[i].toLowerCase().equals("and") || words[i].toLowerCase().equals("if")
							|| words[i].toLowerCase().equals("when")) {
						verbCounter = 0;
						instrNr++;
						list[i] = instrNr;
					}
					//another verb also initiates a new instruction (imperative sentence)
					else if (pos[i].startsWith("VB")) {
						instrNr++;
						verbCounter = 0;
						i--; //repeat loop
					} else {
						list[i] = instrNr;
					}
				}
			}
			return list;
		} else {
			throw new IllegalArgumentException("word array and pos array have different lengths");
		}

	}
}