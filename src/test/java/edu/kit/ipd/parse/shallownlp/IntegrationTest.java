package edu.kit.ipd.parse.shallownlp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.data.token.Token;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;

public class IntegrationTest {

	ShallowNLP snlp;
	String input;
	Token[] actual;
	IGraph graph;
	PrePipelineData ppd;

	@Before
	public void setUp() {
		snlp = new ShallowNLP();
		snlp.init();
	}

	@Test
	public void showSingleStringOutput() {
		input = "Armar go to the fridge.";
		try {
			actual = snlp.parse(input, null);
		} catch (IOException | URISyntaxException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Arrays.deepToString(actual));
		graph = snlp.createParseGraph(actual);
		graph.showGraph();
	}

	@Test
	public void pipelineStageTest() {
		ppd = new PrePipelineData();
		ppd.setTranscription("Armar go to the fridge");

		try {
			snlp.exec(ppd);
		} catch (PipelineStageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			graph = ppd.getGraph();
		} catch (MissingDataException e) {
			e.printStackTrace();
		}
		System.out.println(graph.showGraph());

	}

	@Test
	public void showUsrTokensOutput() {
		input = "Armar go to the fridge .";
		try {
			actual = snlp.parse(input, null);
		} catch (IOException | URISyntaxException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Arrays.deepToString(actual));
		graph = snlp.createParseGraph(actual);
		System.out.println(graph.showGraph());
	}

	@Test
	public void testBatch() {
		ppd = new PrePipelineData();
		List<List<String>> inputHypotheses = Arrays.asList(Arrays.asList("Armar", "go", "to", "the", "fridge"),
				Arrays.asList("Are", "man", "gone", "to", "the", "bridge"));
		ppd.setHypotheses(inputHypotheses);
		try {
			snlp.exec(ppd);
		} catch (PipelineStageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			for (List<Token> list : ppd.getTaggedHypotheses()) {
				System.out.println(Arrays.deepToString(list.toArray()));
			}
		} catch (MissingDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
