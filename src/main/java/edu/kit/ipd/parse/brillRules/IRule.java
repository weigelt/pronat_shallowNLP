/**
 *
 */
package edu.kit.ipd.parse.brillRules;

/**
 * @author Tobias Hey
 *
 */
public interface IRule {

	public boolean applyRule(String[] words, String[] posTags, String[] chunks, int currIndex);
}
