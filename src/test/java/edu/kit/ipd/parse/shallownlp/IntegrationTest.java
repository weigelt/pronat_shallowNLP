package edu.kit.ipd.parse.shallownlp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.data.token.Token;
import edu.kit.ipd.parse.luna.graph.IGraph;

public class IntegrationTest {

	ShallowNLP snlp;
	String input;
	Token[] actual;
	IGraph graph;

	@Before
	public void setUp() {
		snlp = new ShallowNLP();
	}

	@Test
	public void showSingleStringOutput() {
		input = "Armar go to the fridge.";
		try {
			actual = snlp.parse(input, false, true, true, null);
		} catch (IOException | URISyntaxException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Arrays.deepToString(actual));
		graph = snlp.createAGGGraph(actual);
		graph.showGraph();
	}

	@Test
	public void pipelineStageTest() {
		PrePipelineData ppd = new PrePipelineData();
		ppd.setTranscription("Armar go to the fridge");
		snlp.exec(ppd);
		try {
			graph = ppd.getGraph();
		} catch (MissingDataException e) {
			e.printStackTrace();
		}
		graph.showGraph();

	}

	@Test
	public void showUsrTokensOutput() {
		input = "Armar go to the fridge .";
		try {
			actual = snlp.parse(input, false, true, true, null);
		} catch (IOException | URISyntaxException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Arrays.deepToString(actual));
		graph = snlp.createAGGGraph(actual);
		System.out.println(graph.showGraph());
	}

}
