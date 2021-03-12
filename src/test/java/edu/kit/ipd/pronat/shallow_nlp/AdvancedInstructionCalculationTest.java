package edu.kit.ipd.pronat.shallow_nlp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import edu.kit.ipd.pronat.prepipedatamodel.token.Token;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.luna.tools.ConfigManager;

/**
 * @author Sebastian Weigelt
 */
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
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test2() {
		input = "if you are asked to bring a drink you have to pour water in a glass that is how you serve a drink";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test3() {
		input = "to bring coffee you must turn the machine on";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 1, 1, 1, 1, 1, 1);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test4() {
		input = "to set the table you have to bring forks and put them carefully in the cupboard";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test5() {
		input = "if you are asked to bring a drink means you have to pour water in a glass that is how you serve a drink";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test6() {
		input = "when greeting someone you need to wave your hand and say hello all while looking the person in the eyes";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test7() {
		input = "if it is closed please open it";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 1, 1, 1);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test8() {
		input = "go to the table grab the green cup turn and go to the dishwasher if it is closed please open it find a free place put the green cup into it and close the dishwasher";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 1, 1, 1, 1, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7, 7,
				7, 8, 8, 8, 8);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test9() {
		input = "place the orange juice back on the table then go to the window";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test10() {
		input = "take the orange juice from the forth shelf between the water and other juice close the fridge";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test11() {
		input = "start washing the dishes and then put them into the cupboard";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test12() {
		input = "setting a table for two requires you to locate two plates two glasses two knifes and two forks from the cupboard put the plates separately on the table then place a fork on the left of each plate a knife on the right of each plate and a glass behind each knife";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 3, 3,
				3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test13() {
		input = "can you bring some beverage from the fridge in kitchen counter pour thr beverage into a glass then hang it over me";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test14() {
		input = "okay Armar when someone enters the room I want you to make eye contact with them wave your hand side to side and clearly say hello";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test15() {
		input = "Preparing coffee means you to take a cup from besides the machine place this cup underneath the dispenser and start the machine by pressing the red button";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test16() {
		input = "robo go to the dishwasher and open it until the dishwasher is empty take an item from the dishwasher and put it into the cupboard";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test17() {
		input = "put it in the cupboard repeat this until the dishwasher is empty";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 0, 1, 1, 2, 2, 2, 2, 2);
		computeActual(input);
		check(expectedInst);
	}

	@Test
	public void test18() {
		input = "if it is closed please open the window";
		List<Integer> expectedInst = Arrays.asList(0, 0, 0, 0, 1, 1, 1, 1);
		computeActual(input);
		check(expectedInst);
	}

	private void computeActual(String input) {
		try {
			actual = snlp.parse(input, null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void check(List<Integer> expectedInst) {
		for (int i = 0; i < actual.length; i++) {
			Assert.assertEquals(expectedInst.get(i).longValue(), actual[i].getInstructionNumber());
		}
	}

	private void printActual() {
		for (int i = 0; i < actual.length; i++) {
			System.out.println(actual[i].getWord() + "; " + actual[i].getInstructionNumber() + "; " + actual[i].getPos() + "; "
					+ actual[i].getChunkIOB());
		}
	}

	private void printActualInstructions() {
		Arrays.stream(actual).forEach(e -> System.out.print(e.getInstructionNumber() + ","));
	}
}
