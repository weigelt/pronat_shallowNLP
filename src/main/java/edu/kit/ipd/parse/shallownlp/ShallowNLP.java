package edu.kit.ipd.parse.shallownlp;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.ipd.parse.luna.data.AbstractPipelineData;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PipelineDataCastException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.ParseArc;
import edu.kit.ipd.parse.luna.graph.ParseArcType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import edu.kit.ipd.parse.luna.graph.ParseNode;
import edu.kit.ipd.parse.luna.graph.ParseNodeType;
import edu.kit.ipd.parse.luna.pipeline.IPipelineStage;
import edu.kit.ipd.parse.parsebios.Facade;

/**
 * This class represents the API to use this project. The goal of this project
 * is to parse an input text to an AGG-Graph. To use this project create an
 * object of this class and excecute one of the parse methods.
 * 
 * @author Markus Kocybik
 *
 */
public class ShallowNLP implements IPipelineStage {

	private static final Logger logger = LoggerFactory.getLogger(ShallowNLP.class);

	private static final String ID = "snlp";

	private PrePipelineData prePipeData;

	/**
	 * These fillers will be cut out off from the input text
	 */
	private static final List<String> fillers = new ArrayList<String>() {
		{
			add("ah");
			add("eh");
			add("er");
			add("ehm");
			add("em");
			add("hm");
			add("hmm");
			add("uh");
			add("um");
			add("uhm");
		}
	};

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
	 */
	public Token[] parse(String text, boolean containPeriods, boolean opt, boolean imp, WordPosType list) {
		String[] input;
		if (!containPeriods) {
			input = new String[] { text };
		} else {

			input = new Stanford().splitSentence(text);
			opt = false;
		}
		writeToFile(input);
		return shallowNLP(opt, imp, list);
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
	 */
	public Token[] parse(String[] text, boolean opt, boolean imp, WordPosType list) {
		writeToFile(text);
		return shallowNLP(opt, imp, list);
	}

	/**
	 * This method writes the input text into a text file. The text file is the
	 * input file for SENNA.
	 * 
	 * @param text
	 *            the text to parse
	 */
	private void writeToFile(String[] text) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(ResourceReader.getURL(this, "input.txt"), "UTF-8");
			for (String line : text) {
				writer.println(line);
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
		}
	}

	private Token[] shallowNLP(boolean opt, boolean imp, WordPosType list) {
		if (!opt)
			return onlySenna(imp);
		else
			return sennaAndStanford(imp, list);
	}

	/**
	 * This method realizes the pos tagging with SENNA.
	 * 
	 * @param imp
	 *            is true if the text is composed of imperative sentences, then
	 *            the instruction number can be calculated
	 * @return a token array which is the result of parsing
	 */
	private Token[] onlySenna(boolean imp) {
		logger.info("using senna for pos tagging");
		WordPosType list = new Senna().parse();
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
		if (imp)
			instr = new CalcInstruction().CalculateInstructionNumber(words, pos);

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
	 * @return a token array which is the result of parsing
	 */
	private Token[] sennaAndStanford(boolean imp, WordPosType list) {
		logger.info("using senna and stanford core nlp for pos tagging");
		WordPosType result = new Senna().parse();
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
		if (imp)
			instr = new CalcInstruction().CalculateInstructionNumber(words, pos);

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
	public ParseGraph createAGGGraph(Token[] input) {
		ParseGraph graph = new ParseGraph();
		ParseNodeType wordType = new ParseNodeType("token");
		ParseArcType arcType = new ParseArcType("relation");

		wordType.addAttributeToType("String", "value");
		wordType.addAttributeToType("String", "pos");
		wordType.addAttributeToType("String", "chunkIOB");
		wordType.addAttributeToType("String", "chunkName");
		wordType.addAttributeToType("int", "predecessors");
		wordType.addAttributeToType("int", "successors");
		wordType.addAttributeToType("int", "instructionNumber");
		wordType.addAttributeToType("int", "position");
		arcType.addAttributeToType("String", "value");

		ParseNode[] nodes = new ParseNode[input.length];
		for (Token tok : input) {
			ParseNode node = graph.createNode(wordType);
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
				ParseArc arc = graph.createArc(nodes[i], nodes[i + 1], arcType);
				arc.setAttributeValue("value", "NEXT");
			}
		}
		return graph;
	}

	@Override
	public void init() {

	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public void exec(AbstractPipelineData data) {

		Token[] tokens;

		try {
			prePipeData = data.asPrePipelineData();
		} catch (PipelineDataCastException e) {
			logger.error(e.toString());
			logger.info("Cannot process on data");
			return;
		}

		try {
			String[] utterances = prePipeData.getTranscriptions();
			tokens = parse(utterances, true, false, null);
			prePipeData.setGraph(createAGGGraph(tokens));
			return;
		} catch (MissingDataException e) {
			logger.info("No utterance array to process, trying single input instead...");
		}

		try {
			String utterance = prePipeData.getTranscription();
			tokens = parse(utterance, false, true, false, null);
			prePipeData.setGraph(createAGGGraph(tokens));
			return;
		} catch (MissingDataException e) {
			logger.error("No utterance to process, abborting...");
			return;
		}

	}
}
