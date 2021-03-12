package edu.kit.ipd.pronat.shallow_nlp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import edu.kit.ipd.pronat.prepipedatamodel.PrePipelineData;
import edu.kit.ipd.pronat.prepipedatamodel.token.HypothesisTokenType;
import edu.kit.ipd.pronat.prepipedatamodel.token.MainHypothesisToken;
import edu.kit.ipd.pronat.prepipedatamodel.token.Token;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.luna.tools.ConfigManager;

/**
 * @author Sebastian Weigelt
 */
public class IntegrationTest {

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
		snlp = new ShallowNLP();
		snlp.init();
	}

	@Test
	public void showSingleStringOutput() {
		input = "robo go to the dishwasher and open it until the dishwasher is empty take an item from the dishwasher and put it into the cupboard";
		try {
			actual = snlp.parse(input, null);
		} catch (IOException | URISyntaxException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String words = "";
		String pos = "";
		String chks = "";
		for (int i = 0; i < actual.length; i++) {
			Token t = actual[i];
			words += "\"" + t.getWord() + "\", ";
			pos += "\"" + t.getPos().toString() + "\", ";
			chks += "\"" + t.getChunkIOB() + "\", ";
		}
		System.out.println(words);
		System.out.println(pos);
		System.out.println(chks);
		System.out.println(Arrays.deepToString(actual));
		//graph = snlp.createParseGraph(actual);
		//graph.showGraph();
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
		final MainHypothesisToken h0 = new MainHypothesisToken("Armar", 0, 1.0d, HypothesisTokenType.WORD);
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

		ppd.setMainHypothesis(hypothesis);

		try {
			snlp.exec(ppd);
		} catch (final PipelineStageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			for (final List<Token> list : ppd.getTaggedHypotheses()) {
				System.out.println(Arrays.deepToString(list.toArray()));
				assertEquals(h0.getConfidence(), list.get(0).getConfidence(), 0.0);
				assertEquals(h0.getType(), list.get(0).getType());
			}
		} catch (final MissingDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
