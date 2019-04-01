package edu.kit.ipd.parse.shallownlp;

import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.data.token.Token;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AdvancedInstructionCalculationTest {
	static ShallowNLP snlp;
	String input;
	Token[] actual;
	IGraph graph;
	PrePipelineData ppd;
	private static Properties props;

	@BeforeClass
	public static void setUp() {
		props = ConfigManager.getConfiguration(Stanford.class);
		props.setProperty("TAGGER_MODEL", "/edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger");
		props.setProperty("LEGACY_CALC_INSTR_MODE", "false");
		snlp = new ShallowNLP();
		snlp.init();
	}

	@Test
	public void testVBG_VBZ_And_To_Inf() {
		input = "serving a drink for someone means you have to put water in a glass and bring it to me";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2);
		try {
			actual = snlp.parse(input, null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < actual.length; i++) {
			Assert.assertEquals(expectedInst.get(i).longValue(), actual[i].getInstructionNumber());
		}
	}

	@Test
	public void test2() {
		input = "if you are asked to bring a drink you have to pour water in a glass that is how you serve a drink";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2);
		try {
			actual = snlp.parse(input, null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < actual.length; i++) {
			Assert.assertEquals(expectedInst.get(i).longValue(), actual[i].getInstructionNumber());
		}
	}

	@Test
	public void test3() {
		input = "to bring coffee you must turn the machine on";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 1, 1, 1, 1, 1, 1);
		try {
			actual = snlp.parse(input, null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < actual.length; i++) {
			Assert.assertEquals(expectedInst.get(i).longValue(), actual[i].getInstructionNumber());
		}
	}

	@Test
	public void test4() {
		input = "to set the table you have to bring forks and put them carefully in the cupboard";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2);
		try {
			actual = snlp.parse(input, null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < actual.length; i++) {
			Assert.assertEquals(expectedInst.get(i).longValue(), actual[i].getInstructionNumber());
		}
	}
}
