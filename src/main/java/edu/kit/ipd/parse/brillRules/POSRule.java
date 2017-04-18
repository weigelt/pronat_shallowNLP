/**
 *
 */
package edu.kit.ipd.parse.brillRules;

/**
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
	 * @see edu.kit.ipd.parse.brillRules.IRule#applyRule(java.lang.String[],
	 * java.lang.String[], java.lang.String[])
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
		return false;
	}

}
