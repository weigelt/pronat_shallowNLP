package edu.kit.ipd.parse.shallownlp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.data.token.MainHypothesisToken;
import edu.kit.ipd.parse.luna.data.token.Token;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;

public class IntegrationTest {

	static ShallowNLP snlp;
	String input;
	Token[] actual;
	IGraph graph;
	PrePipelineData ppd;

	@BeforeClass
	public static void setUp() {
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
		} catch (final PipelineStageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			graph = ppd.getGraph();
		} catch (final MissingDataException e) {
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
	public void testHypothesis() {
		ppd = new PrePipelineData();
		final MainHypothesisToken h0 = new MainHypothesisToken("Armar", 0);
		final MainHypothesisToken h1 = new MainHypothesisToken("go", 1);
		final MainHypothesisToken h2 = new MainHypothesisToken("to", 2);
		final MainHypothesisToken h3 = new MainHypothesisToken("the", 3);
		final MainHypothesisToken h4 = new MainHypothesisToken("fridge", 4);
		final List<MainHypothesisToken> hypothesis = new ArrayList<MainHypothesisToken>();
		hypothesis.add(h0);
		hypothesis.add(h1);
		hypothesis.add(h2);
		hypothesis.add(h3);
		hypothesis.add(h4);

		ppd.setMainHypotheses(hypothesis);

		try {
			snlp.exec(ppd);
		} catch (final PipelineStageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			for (final List<Token> list : ppd.getTaggedHypotheses()) {
				System.out.println(Arrays.deepToString(list.toArray()));
			}
		} catch (final MissingDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
