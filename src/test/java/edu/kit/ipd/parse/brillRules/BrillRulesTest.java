package edu.kit.ipd.parse.brillRules;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Tobias Hey
 *
 */
public class BrillRulesTest {

	String[] words = new String[] { "Go", "to", "the", "table", "and", "grab", "the", "green", "cup", "then", "go", "to", "the", "fridge",
			"and", "open", "it", "take", "the", "Apollinaris", "bottle", "and", "open", "it", "pour", "the", "bottle", "into", "the", "cup",
			"until", "it's", "full", "put", "down", "both", "objects", "onto", "the", "nearest", "table", "and", "close", "the", "fridge",
			"then", "go", "to", "the", "dishwasher", "open", "it", "and", "then", "take", "all", "the", "red", "cups", "and", "place",
			"them", "into", "the", "cupboard" };
	String[] pos = new String[] { "VB", "TO", "DT", "NN", "CC", "VB", "DT", "JJ", "NN", "RB", "VB", "TO", "DT", "NN", "CC", "VB", "PRP",
			"VB", "DT", "NNP", "NN", "CC", "VB", "PRP", "VB", "DT", "NN", "IN", "DT", "NN", "IN", "JJ", "JJ", "VBD", "RP", "DT", "NNS",
			"IN", "DT", "JJS", "NN", "CC", "VB", "DT", "NN", "RB", "VB", "TO", "DT", "NN", "JJ", "PRP", "CC", "RB", "VB", "PDT", "DT", "JJ",
			"NNS", "CC", "VB", "PRP", "IN", "DT", "NN" };
	String[] chunks = new String[] { "B-VP", "B-PP", "B-NP", "I-NP", "O", "B-VP", "B-NP", "I-NP", "I-NP", "B-ADVP", "B-VP", "B-PP", "B-NP",
			"I-NP", "O", "B-VP", "B-NP", "B-VP", "B-NP", "I-NP", "I-NP", "O", "B-VP", "B-NP", "B-VP", "B-NP", "I-NP", "B-PP", "B-NP",
			"I-NP", "B-SBAR", "B-NP", "I-NP", "B-VP", "B-PRT", "B-NP", "I-NP", "B-PP", "B-NP", "I-NP", "I-NP", "O", "B-VP", "B-NP", "I-NP",
			"B-ADVP", "B-VP", "B-PP", "B-NP", "I-NP", "B-ADJP", "B-NP", "O", "B-VP", "I-VP", "B-NP", "I-NP", "I-NP", "I-NP", "O", "B-VP",
			"B-NP", "B-PP", "B-NP", "I-NP" };

	static IRule rule;

	@BeforeClass
	public static void setUp() {
		rule = new POSRule("open", "JJ", "VB",
				"NOT NEXT_1_POS_NN AND NOT NEXT_1_POS_NNS AND NOT NEXT_1_POS_NNP AND NOT NEXT_1_POS_NNPS AND NOT PREV_1_POS_VB AND NOT PREV_1_POS_VBD AND NOT PREV_1_POS_VBG AND NOT PREV_1_POS_VBN AND NOT PREV_1_POS_VBP AND NOT PREV_1_POS_VBZ");
	}

	@Test
	public void testRule() {
		for (int i = 0; i < words.length; i++) {
			if (rule.applyRule(words, pos, chunks, i)) {
				System.out.println("Rule applied on word " + words[i] + " at position " + i);
			}
		}
		Assert.assertEquals("VB", pos[50]);
	}

}
