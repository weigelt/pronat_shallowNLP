package edu.kit.ipd.parse.shallownlp;

import java.util.Arrays;

import org.junit.Test;

import edu.kit.ipd.parse.luna.graph.GraphInterface;

public class IntegrationTest {
	@Test
	public void showSingleStringOutput() {
		String input = "Armar go to the fridge";
		ShallowNLP snlp = new ShallowNLP();
		Token[] actual = snlp.parse(input, false, true, true, null);
		System.out.println(Arrays.deepToString(actual));
		GraphInterface graph = snlp.createAGGGraph(actual);
		graph.showGraph();
	}

}
