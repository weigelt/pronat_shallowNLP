/**
 *
 */
package edu.kit.ipd.parse.brillRules;

/**
 * This interface represents all possible brill rules
 *
 * @author Tobias Hey
 *
 */
public interface IRule {

	/**
	 * Applies this rule on the word with the specified index according to the
	 * given words, postags and chunks .
	 *
	 * @param words
	 *            The word Array
	 * @param posTags
	 *            The pos tag Array
	 * @param chunks
	 *            the chunk tag Array (IOB-Format)
	 * @param currIndex
	 *            The index of the word the rule is applied on
	 * @return if the rule provoked a change
	 */
	public boolean applyRule(String[] words, String[] posTags, String[] chunks, int currIndex);
}
