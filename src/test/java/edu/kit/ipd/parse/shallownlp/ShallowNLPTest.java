package edu.kit.ipd.parse.shallownlp;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import edu.kit.ipd.parse.senna_wrapper.WordSennaResult;

public class ShallowNLPTest {

	@Test
	public void testWordPosDebatch() {
		String[] inputWords = { "This", "is", "a", "test", ".", "And", "it", "is", "over", "." };
		String[] inputPos = { "DT", "VBZ", "DT", "NN", ".", "CC", "PRP", "VBZ", "RP", "." };

		List<WordSennaResult> input = new ArrayList<WordSennaResult>();
		for (int i = 0; i < inputWords.length; i++) {
			input.add(new WordSennaResult(inputWords[i], new String[] { inputPos[i] }));
		}

		String[] expWords_0 = { "This", "is", "a", "test" };
		String[] expPos_0 = { "DT", "VBZ", "DT", "NN" };
		String[] expWords_1 = { "And", "it", "is", "over" };
		String[] expPos_1 = { "CC", "PRP", "VBZ", "RP" };

		List<WordSennaResult> exp_0 = new ArrayList<WordSennaResult>();
		for (int i = 0; i < expWords_0.length; i++) {
			exp_0.add(new WordSennaResult(expWords_0[i], new String[] { expPos_0[i] }));
		}

		List<WordSennaResult> exp_1 = new ArrayList<WordSennaResult>();
		for (int i = 0; i < expWords_1.length; i++) {
			exp_1.add(new WordSennaResult(expWords_1[i], new String[] { expPos_1[i] }));
		}

		List<List<WordSennaResult>> actualList = new ShallowNLP().generateDebatchedWordSennaResultList(input);

		Assert.assertEquals(2, actualList.size());
		for (int i = 0; i < expWords_0.length; i++) {
			Assert.assertEquals(exp_0.get(i).getWord(), actualList.get(0).get(i).getWord());
			Assert.assertArrayEquals(exp_0.get(i).getAnalysisResults(), actualList.get(0).get(i).getAnalysisResults());
		}
		for (int i = 0; i < expWords_1.length; i++) {
			Assert.assertEquals(exp_1.get(i).getWord(), actualList.get(1).get(i).getWord());
			Assert.assertArrayEquals(exp_1.get(i).getAnalysisResults(), actualList.get(1).get(i).getAnalysisResults());
		}

	}

}
