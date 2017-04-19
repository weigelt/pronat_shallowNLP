package edu.kit.ipd.parse.brillRules;

/**
 *
 * @author Tobias Hey
 *
 */
public interface IConditionPart {

	public boolean isPartOfType(String part);

	public boolean checkPart(String part, String[] words, String[] posTags, String[] chunks, int currIndex);

}
