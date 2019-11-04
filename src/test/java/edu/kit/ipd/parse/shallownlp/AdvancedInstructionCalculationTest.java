package edu.kit.ipd.parse.shallownlp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.luna.data.token.Token;
import edu.kit.ipd.parse.luna.tools.ConfigManager;

public class AdvancedInstructionCalculationTest {
	private static ShallowNLP snlp;
	private String input;
	private Token[] actual;
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

	@Test
	public void test5() {
		input = "if you are asked to bring a drink means you have to pour water in a glass that is how you serve a drink";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2);
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
	public void test6() {
		input = "when greeting someone you need to wave your hand and say hello all while looking the person in the eyes";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3);
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
	public void test7() {
		input = "if it is closed please open it";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 1, 1, 1);
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
	public void test8() {
		input = "go to the table grab the green cup turn and go to the dishwasher if it is closed please open it find a free place put the green cup into it and close the dishwasher";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 1, 1, 1, 1, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 7,
				7, 8, 8, 8, 8);
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
	public void test9() {
		input = "place the orange juice back on the table then go to the window";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1);

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
	public void test10() {
		input = "take the orange juice from the forth shelf between the water and other juice close the fridge";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1);

		try {
			actual = snlp.parse(input, null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		printActual(input, actual);
		for (int i = 0; i < actual.length; i++) {
			Assert.assertEquals(expectedInst.get(i).longValue(), actual[i].getInstructionNumber());
		}
	}

	@Test
	public void test11() {
		input = "start washing the dishes and then put them into the cupboard";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1);

		try {
			actual = snlp.parse(input, null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		printActual(input, actual);
		for (int i = 0; i < actual.length; i++) {
			Assert.assertEquals(expectedInst.get(i).longValue(), actual[i].getInstructionNumber());
		}
	}

	private void printActual(String input, Token[] actual) {
		List<String> inputArray = Arrays.asList(input.split(" "));
		for (int i = 0; i < actual.length; i++) {
			System.out.println(inputArray.get(i) + "; " + actual[i].getInstructionNumber() + "; " + actual[i].getPos() + "; "
					+ actual[i].getChunkIOB());
		}
	}
}
