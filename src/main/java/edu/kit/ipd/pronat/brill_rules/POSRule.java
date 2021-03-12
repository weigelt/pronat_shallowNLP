/**
 *
 */
package edu.kit.ipd.pronat.brill_rules;

/**
 * Represents a brill rule checking pos tags
 * 
 * @author Tobias Hey
 *
 */
public class POSRule implements IRule {

	private String word;
	private String from, to;
	private String condition;

	public POSRule(String word, String from, String to, String condition) {
		super();
		this.word = word;
		this.from = from;
		this.to = to;
		this.condition = condition;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IRule#applyRule(java.lang.String[], java.lang.String[],
	 * java.lang.String[])
	 */
	@Override
	public boolean applyRule(String[] words, String[] posTags, String[] chunks, int currIndex) {
		if (!word.equals("*") && !words[currIndex].equalsIgnoreCase(word)) {
			return false;
		}
		if (!from.equals("*") && !posTags[currIndex].equalsIgnoreCase(from)) {
			return false;
		}
		if (posTags[currIndex].equalsIgnoreCase(to)) {
			return false;
		}
		if (ConditionChecker.checkCondition(condition, words, posTags, chunks, currIndex)) {
			posTags[currIndex] = to;
			return true;
		}
		return false;
	}

}
