/**
 *
 */
package edu.kit.ipd.parse.brillRules;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tobias Hey
 *
 */
public class ConditionChecker {

	private static List<IConditionPart> partChecker;

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
	}

	public static boolean checkCondition(String condition, String[] words, String[] posTags, String[] chunks, int currIndex) {
		boolean result = false;
		String[] condParts = condition.trim().split(" ");
		if (condParts.length != 0) {
			String part = condParts[0];
			if (part.equals(BooleanOperators.AND)) {
				if (condParts.length > 1 && condParts[1].equals(BooleanOperators.NOT)) {
					result = result && !checkCondition(buildCondition(condParts, 1), words, posTags, chunks, currIndex);
				} else {
					result = result && checkCondition(buildCondition(condParts, 0), words, posTags, chunks, currIndex);
				}
			} else if (part.equals(BooleanOperators.OR)) {
				if (condParts.length > 1 && condParts[1].equals(BooleanOperators.NOT)) {
					result = result || !checkCondition(buildCondition(condParts, 1), words, posTags, chunks, currIndex);
				} else {
					result = result || checkCondition(buildCondition(condParts, 0), words, posTags, chunks, currIndex);
				}
			} else if (part.equals(BooleanOperators.XOR)) {
				if (condParts.length > 1 && condParts[1].equals(BooleanOperators.NOT)) {
					result = result ^ !checkCondition(buildCondition(condParts, 1), words, posTags, chunks, currIndex);
				} else {
					result = result ^ checkCondition(buildCondition(condParts, 0), words, posTags, chunks, currIndex);
				}
			} else {
				for (IConditionPart pChecker : partChecker) {
					if (pChecker.isPartOfType(part)) {
						result = pChecker.checkPart(part, words, posTags, chunks, currIndex);
					}
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
