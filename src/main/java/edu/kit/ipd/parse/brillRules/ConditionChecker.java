/**
 *
 */
package edu.kit.ipd.parse.brillRules;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to check a given condition
 *
 * @author Tobias Hey
 *
 */
public class ConditionChecker {

	private static List<IConditionPart> partChecker;

	private static final Logger logger = LoggerFactory.getLogger(ConditionChecker.class);

	static {
		partChecker = new ArrayList<>();
		partChecker.add(new NextPart());
		partChecker.add(new PrevPart());
	}

	private enum BooleanOperators {
		AND("AND"), OR("OR"), NOT("NOT"), XOR("XOR");
		private final String op;

		private BooleanOperators(String op) {
			this.op = op;
		};

		@Override
		public String toString() {
			return this.op;
		}
	}

	/**
	 * Checks if the given condition is fulfilled.
	 *
	 * @param condition
	 *            textual representation of the condition
	 * @param words
	 *            The word Array
	 * @param posTags
	 *            The pos tag Array
	 * @param chunks
	 *            the chunk tag Array (IOB-Format)
	 * @param currIndex
	 *            The index of the word the rule is applied on
	 * @return if the given condition is fulfilled
	 */
	public static boolean checkCondition(String condition, String[] words, String[] posTags, String[] chunks, int currIndex) {
		boolean result = false;
		String[] condParts = condition.trim().split(" ");
		if (condParts.length != 0 && !condition.equals("")) {
			int nextIndex = 1;
			// is condition starting with NOT?
			if (condParts.length > 1 && condParts[0].equalsIgnoreCase(BooleanOperators.NOT.toString())) {
				for (IConditionPart pChecker : partChecker) {
					if (pChecker.isPartOfType(condParts[1])) {
						result = !pChecker.checkPart(condParts[1], words, posTags, chunks, currIndex);
						nextIndex = 2;
						break;
					}
				}
			} else {
				for (IConditionPart pChecker : partChecker) {
					if (pChecker.isPartOfType(condParts[0])) {
						result = pChecker.checkPart(condParts[0], words, posTags, chunks, currIndex);
						nextIndex = 1;
						break;
					}
				}
			}
			if (nextIndex < condParts.length) {
				String part = condParts[nextIndex];
				if (part.equalsIgnoreCase(BooleanOperators.AND.toString())) {

					result = result && checkCondition(buildCondition(condParts, nextIndex), words, posTags, chunks, currIndex);

				} else if (part.equalsIgnoreCase(BooleanOperators.OR.toString())) {

					result = result || checkCondition(buildCondition(condParts, nextIndex), words, posTags, chunks, currIndex);

				} else if (part.equalsIgnoreCase(BooleanOperators.XOR.toString())) {

					result = result ^ checkCondition(buildCondition(condParts, nextIndex), words, posTags, chunks, currIndex);

				} else {
					logger.warn("Parts of the specified Condition are not combined by a boolean operator. Rule: " + condition);
				}

			}
		} else {
			return true;
		}
		return result;
	}

	private static String buildCondition(String[] condParts, int i) {
		String cond = "";
		for (int j = i + 1; j < condParts.length; j++) {
			cond += condParts[j] + " ";
		}
		return cond;
	}

}
