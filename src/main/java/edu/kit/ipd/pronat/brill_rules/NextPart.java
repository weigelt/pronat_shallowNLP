/**
 *
 */
package edu.kit.ipd.pronat.brill_rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a {@link IConditionPart} checking attributes of words succeeding
 * the current word
 *
 * @author Tobias Hey
 *
 */
public class NextPart implements IConditionPart {

	private enum Types {
		POS("POS"), CHUNK("CHUNK"), WORD("WORD");

		private final String op;

		private Types(String op) {
			this.op = op;
		}

		@Override
		public String toString() {

			return op;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(NextPart.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see IConditionPart#checkPart(java.lang.String, java.lang.String[],
	 * java.lang.String[], java.lang.String[], int)
	 */
	@Override
	public boolean checkPart(String part, String[] words, String[] posTags, String[] chunks, int currIndex) {
		String[] parts = part.trim().split("_");
		if (parts.length > 3) {
			int index;
			try {
				index = Integer.parseInt(parts[1]);
				String type = parts[2];
				String expected = parts[3];
				int i = currIndex + index;
				if (i < words.length) {

					//skip interjections
					if (posTags[i].equalsIgnoreCase("UH") && i + 1 < words.length) {
						i++;
					}
					if (type.equalsIgnoreCase(Types.POS.toString())) {
						if (posTags[i].equalsIgnoreCase(expected)) {
							return true;
						}
					} else if (type.equalsIgnoreCase(Types.CHUNK.toString())) {
						if (chunks[i].equalsIgnoreCase(expected)) {
							return true;
						}
					} else if (type.equalsIgnoreCase(Types.WORD.toString())) {
						if (words[i].equalsIgnoreCase(expected)) {
							return true;
						}
					} else {
						logger.warn("Next part of condition is not defined correctly (Type must be POS, CHUNK or WORD). Ignore Part");
					}
				}
			} catch (NumberFormatException e) {
				logger.warn("Next part of condition is not defined correctly (index must be second argument). Ignore Part");
				return false;
			}
		} else {
			logger.warn(
					"Next part of condition is not defined correctly (Next parts must consist of at least NEXT_N_TYPE_xx). Ignore Part");
		}
		return false;
	}

	@Override
	public boolean isPartOfType(String part) {
		if (part.startsWith("NEXT")) {
			return true;
		}
		return false;
	}

}
