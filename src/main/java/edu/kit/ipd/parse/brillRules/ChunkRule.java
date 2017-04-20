/**
 *
 */
package edu.kit.ipd.parse.brillRules;

/**
 * Represents a brill rule checking chunk tags
 * 
 * @author Tobias Hey
 *
 */
public class ChunkRule implements IRule {

	private String word;
	private String from, to;
	private String condition;

	public ChunkRule(String word, String from, String to, String condition) {
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
		if (!from.equals("*") && !chunks[currIndex].equalsIgnoreCase(from)) {
			return false;
		}
		if (chunks[currIndex].equalsIgnoreCase(to)) {
			return false;
		}
		if (ConditionChecker.checkCondition(condition, words, posTags, chunks, currIndex)) {
			chunks[currIndex] = to;
			return true;
		}
		return false;
	}

}
