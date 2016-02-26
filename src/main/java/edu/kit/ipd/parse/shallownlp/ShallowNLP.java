package edu.kit.ipd.parse.shallownlp;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.data.AbstractPipelineData;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PipelineDataCastException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.data.token.Chunk;
import edu.kit.ipd.parse.luna.data.token.ChunkIOB;
import edu.kit.ipd.parse.luna.data.token.POSTag;
import edu.kit.ipd.parse.luna.data.token.Token;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseArcType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import edu.kit.ipd.parse.luna.graph.ParseNode;
import edu.kit.ipd.parse.luna.graph.ParseNodeType;
import edu.kit.ipd.parse.luna.pipeline.IPipelineStage;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.kit.ipd.parse.parsebios.Facade;

/**
 * This class represents the API to use this project. The goal of this project
 * is to parse an input text to an AGG-Graph. To use this project create an
 * object of this class and excecute one of the parse methods.
 * 
 * @author Markus Kocybik
 *
 */
@MetaInfServices(IPipelineStage.class)
public class ShallowNLP implements IPipelineStage {

	private static final Logger logger = LoggerFactory.getLogger(ShallowNLP.class);

	private static final String ID = "snlp";

	private PrePipelineData prePipeData;

	private Properties props;

	private boolean imp, opt, containPeriods, excludeFillers;

	private static List<String> fillers;

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.ipd.parse.luna.pipeline.IPipelineStage#init()
	 */
	@Override
	public void init() {
		props = ConfigManager.getConfiguration(getClass());
		imp = Boolean.parseBoolean(props.getProperty("IMPERATIVE"));
		containPeriods = Boolean.parseBoolean(props.getProperty("PERIODS"));
		excludeFillers = Boolean.parseBoolean(props.getProperty("EXCLUDE_FILLERS"));
		opt = props.getProperty("EXCLUDE_FILLERS").equals("sennaandstanford");
		fillers = new ArrayList<String>();
		if (excludeFillers) {
			fillers.addAll(Arrays.asList(props.getProperty("EXCLUDE_FILLERS").split(",")));
		}
	}

	/**
	 * This method parses a text which contains multiple sentences. The text can
	 * either contain periods or not.
	 * 
	 * @param text
	 *            represents the sentences to parse. The text can consist of
	 *            multiple sentences.
	 * @param containPeriods
	 *            true if the text contains periods and should be tokenized in
	 *            sentences false if the text does not contain periods
	 * @param opt
	 *            enables domain specific optimization, uses SENNA and the
	 *            stanford pos tagger for pos tagging
	 * @param imp
	 *            true if the text is composed of imperative sentences, then the
	 *            instruction number can be calculated
	 * @param verbList
	 *            fix list of words which should be tagged as verbs is only used
	 *            if the parameter opt is true
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public Token[] parse(String text, WordPosType list) throws IOException, URISyntaxException, InterruptedException {
		File tempFile;
		String[] input;
		if (!containPeriods) {
			input = new String[] { text };
		} else {

			input = new Stanford().splitSentence(text);
			opt = false;
		}
		tempFile = writeToTempFile(input);
		return shallowNLP(list, tempFile);
	}

	/**
	 * This method parses a text which is tokenized in sentences
	 * 
	 * @param text
	 *            each element in the array represents one sentence to parse
	 * @param opt
	 *            enables domain specific optimization, uses senna and the
	 *            stanford pos tagger for pos tagging
	 * @param imp
	 *            is true if the text is composed of imperative sentences, then
	 *            the instruction number can be calculated
	 * @param verbList
	 *            fix list of words which should be tagged as verb, is only used
	 *            if the parameter opt is true
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public Token[] parse(String[] text, WordPosType list) throws IOException, URISyntaxException, InterruptedException {
		File tempFile = writeToTempFile(text);
		return shallowNLP(list, tempFile);
	}

	/**
	 * This method writes the input text into a text file. The text file is the
	 * input file for SENNA.
	 * 
	 * @param text
	 *            the text to parse
	 * @throws IOException
	 */
	private File writeToTempFile(String[] text) throws IOException {
		PrintWriter writer;
		File tempFile = File.createTempFile("input", "txt");
		writer = new PrintWriter(tempFile);
		for (String line : text) {
			writer.println(line);
		}
		writer.close();
		return tempFile;

	}

	private Token[] shallowNLP(WordPosType list, File tempFile) throws IOException, URISyntaxException, InterruptedException {
		if (!opt)
			return onlySenna(tempFile);
		else
			return sennaAndStanford(list, tempFile);
	}

	/**
	 * This method realizes the pos tagging with SENNA.
	 * 
	 * @param imp
	 *            is true if the text is composed of imperative sentences, then
	 *            the instruction number can be calculated
	 * @param tempFile
	 * @return a token array which is the result of parsing
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	private Token[] onlySenna(File tempFile) throws IOException, URISyntaxException, InterruptedException {
		logger.info("using senna for pos tagging");
		WordPosType list = new Senna().parse(tempFile);
		String[] words = list.getWords();
		String[] posSenna = list.getPos();

		String[] pos = new String[words.length];
		for (int i = 0; i < words.length; i++) {
			if (fillers.contains(words[i].toLowerCase()))
				pos[i] = POSTag.INTERJECTION.getTag();
			else
				pos[i] = posSenna[i];
		}

		String[] chunks = new Facade().parse(words, pos);

		int[] instr = new int[words.length];
		if (imp) {
			try {
				instr = new CalcInstruction().calculateInstructionNumber(words, pos);
			} catch (IllegalArgumentException e) {
				logger.error("Cannot calculate instruction number, instruction number is set to -1", e);
				Arrays.fill(instr, -1);
			}

		}

		return createTokens(words, pos, instr, chunks);
	}

	/**
	 * This method realizes the pos tagging with SENNA.
	 * 
	 * @param imp
	 *            is true if the text is composed of imperative sentences, then
	 *            the instruction number can be calculated
	 * @param list
	 *            fix list of words which should be tagged as verbs is only used
	 *            if the parameter opt is true
	 * @param tempFile
	 * @return a token array which is the result of parsing
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	private Token[] sennaAndStanford(WordPosType list, File tempFile) throws IOException, URISyntaxException, InterruptedException {
		logger.info("using senna and stanford core nlp for pos tagging");
		WordPosType result = new Senna().parse(tempFile);
		String[] words = result.getWords();
		String[] posSenna = result.getPos();
		String[] posStan = new Stanford().posTag(words);

		String[] pos = new String[words.length];
		for (int i = 0; i < words.length; i++) {
			if (list != null && Arrays.asList(list.getWords()).contains(words[i]))
				pos[i] = list.getPos()[Arrays.asList(list.getWords()).indexOf(words[i])];
			else if (fillers.contains(words[i].toLowerCase()))
				pos[i] = POSTag.INTERJECTION.getTag();
			else if (!posSenna[i].startsWith("VB") && posStan[i].startsWith("VB"))
				pos[i] = posStan[i];
			else
				pos[i] = posSenna[i];
		}

		String[] chunks = new Facade().parse(words, pos);

		int[] instr = new int[words.length];
		if (imp) {
			try {
				instr = new CalcInstruction().calculateInstructionNumber(words, pos);
			} catch (IllegalArgumentException e) {
				logger.error("Cannot calculate instruction number, instruction number is set to -1", e);
				Arrays.fill(instr, -1);
			}
		}

		return createTokens(words, pos, instr, chunks);
	}

	/**
	 * This method summarizes the results of the pos tagging, chunking and the
	 * calculation of the instruction number in a token object.
	 * 
	 * @param words
	 *            of the input text
	 * @param pos
	 *            the pos tags for each word
	 * @param instr
	 *            the calculation number of each word
	 * @param chunks
	 *            the IOB-chunk of each word
	 * @return the result of parsing as token array
	 */
	private Token[] createTokens(String[] words, String[] pos, int[] instr, String[] chunksIOB) {
		Token[] result = new Token[words.length];
		Chunk[] chunks = new Chunk().convertIOB(chunksIOB);
		for (int i = 0; i < words.length; i++) {
			Token tmp = new Token(words[i], POSTag.get(pos[i]), ChunkIOB.get(chunksIOB[i]), chunks[i], i, instr[i]);
			result[i] = tmp;
		}
		return result;
	}

	/**
	 * This method creates the AGG-Graph from the token array.
	 * 
	 * @param input
	 *            the token array
	 * @param saveAGGto
	 *            location to save the AGG-Graph.
	 * @return the AGG-Graph
	 */
	public IGraph createAGGGraph(Token[] input) {
		IGraph graph = new ParseGraph();
		INodeType wordType = new ParseNodeType("token");
		IArcType arcType = new ParseArcType("relation");

		wordType.addAttributeToType("String", "value");
		wordType.addAttributeToType("String", "pos");
		wordType.addAttributeToType("String", "chunkIOB");
		wordType.addAttributeToType("String", "chunkName");
		wordType.addAttributeToType("int", "predecessors");
		wordType.addAttributeToType("int", "successors");
		wordType.addAttributeToType("int", "instructionNumber");
		wordType.addAttributeToType("int", "position");
		arcType.addAttributeToType("String", "value");

		INode[] nodes = new ParseNode[input.length];
		for (Token tok : input) {
			INode node = graph.createNode(wordType);
			node.setAttributeValue("value", tok.getWord());
			node.setAttributeValue("pos", tok.getPos().toString());
			node.setAttributeValue("chunkIOB", tok.getChunkIOB().toString());
			node.setAttributeValue("chunkName", tok.getChunk().getName());
			node.setAttributeValue("predecessors", tok.getChunk().getPredecessor());
			node.setAttributeValue("successors", tok.getChunk().getSuccessor());
			node.setAttributeValue("instructionNumber", tok.getInstructionNumber());
			node.setAttributeValue("position", tok.getPosition());
			nodes[tok.getPosition()] = node;
		}

		for (int i = 0; i < nodes.length; i++) {
			if (i < nodes.length - 1) {
				IArc arc = graph.createArc(nodes[i], nodes[i + 1], arcType);
				arc.setAttributeValue("value", "NEXT");
			}
		}
		return graph;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public void exec(AbstractPipelineData data) {

		Token[] tokens;

		//try to get data as pre pipeline data. If this fails, return
		try {
			prePipeData = data.asPrePipelineData();
		} catch (PipelineDataCastException e) {
			logger.error(e.toString());
			logger.info("Cannot process on data");
			return;
		}

		// try to process on utterance array
		try {
			String[] utterances = prePipeData.getTranscriptions();
			tokens = parse(utterances, null);
			prePipeData.setTokens(tokens);
			prePipeData.setGraph(createAGGGraph(tokens));
			return;
		} catch (MissingDataException e) {
			logger.info("No utterance array to process, trying single input instead...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//try to process n single utterance. If this fails, return and show error, as we have no other alternative
		try {
			String utterance = prePipeData.getTranscription();
			tokens = parse(utterance, null);
			prePipeData.setTokens(tokens);
			prePipeData.setGraph(createAGGGraph(tokens));
			return;
		} catch (MissingDataException e) {
			logger.error("No utterance to process, abborting...");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
