package edu.kit.ipd.parse.brillRules;

/**
 * This interface represents possible parts that can be defined in the condition
 * of a brill rule
 *
 * @author Tobias Hey
 *
 */
public interface IConditionPart {

	/**
	 * Checks if the given textual representation of a part of a condition
	 * belongs to this type of part
	 *
	 * @param part
	 * @return
	 */
	public boolean isPartOfType(String part);

	/**
	 * Check if this part of the condition is fulfilled
	 *
	 * @param part
	 *            textual representation of the part to check
	 * @param words
	 *            The word Array
	 * @param posTags
	 *            The pos tag Array
	 * @param chunks
	 *            the chunk tag Array (IOB-Format)
	 * @param currIndex
	 *            The index of the word the rule is applied on
	 * @return if this part is fulfilled
	 */
	public boolean checkPart(String part, String[] words, String[] posTags, String[] chunks, int currIndex);

}
