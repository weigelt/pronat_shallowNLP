package edu.kit.ipd.parse.shallownlp;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ShallowNLPTest {

	@Test
	public void testWordPosDebatch() {
		String[] inputWords = { "This", "is", "a", "test", ".", "And", "it", "is", "over", "." };
		String[] inputPos = { "DT", "VBZ", "DT", "NN", ".", "CC", "PRP", "VBZ", "RP", "." };
		WordPosType input = new WordPosType(inputWords, inputPos);
		String[] expWords_0 = { "This", "is", "a", "test" };
		String[] expPos_0 = { "DT", "VBZ", "DT", "NN" };
		String[] expWords_1 = { "And", "it", "is", "over" };
		String[] expPos_1 = { "CC", "PRP", "VBZ", "RP" };
		WordPosType exp_0 = new WordPosType(expWords_0, expPos_0);
		WordPosType exp_1 = new WordPosType(expWords_1, expPos_1);
		List<WordPosType> actualList = new ShallowNLP().generateWordPosList(input);
		Assert.assertEquals(2, actualList.size());
		Assert.assertArrayEquals(exp_0.getWords(), actualList.get(0).getWords());
		Assert.assertArrayEquals(exp_0.getPos(), actualList.get(0).getPos());
		Assert.assertArrayEquals(exp_1.getWords(), actualList.get(1).getWords());
		Assert.assertArrayEquals(exp_1.getPos(), actualList.get(1).getPos());
	}

}
