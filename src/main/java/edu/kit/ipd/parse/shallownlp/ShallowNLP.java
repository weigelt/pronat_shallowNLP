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
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
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
	// TODO: WordPosType definitions in config
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
		opt = props.getProperty("MODE").equals("sennaandstanford");
		fillers = new ArrayList<String>();
		if (excludeFillers) {
			fillers.addAll(Arrays.asList(props.getProperty("FILLERS").split(",")));
		}
	}

	/**
	 * This method parses a text which contains multiple sentences. The text can
	 * either contain periods or not.
	 * 
	 * @param text
	 *            represents the sentences to parse. The text can consist of
	 *            multiple sentences.
	 * @param wordPosList
	 *            the defined Word-Pos-Alignments
	 * @return the created Tokens with POS, Chunks (and instruction number)
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public Token[] parse(String text, WordPosType wordPosList)
			throws IOException, URISyntaxException, InterruptedException {
		File tempFile;
		String[] input;
		if (!containPeriods) {
			input = new String[] { text };
		} else {

			input = new Stanford().splitSentence(text);
			// opt = false;
		}
		tempFile = writeToTempFile(input);
		return shallowNLP(wordPosList, tempFile);
	}

	/**
	 * This method parses a text which is tokenized in sentences
	 * 
	 * @param text
	 *            each element in the array represents one sentence to parse
	 * @param wordPosList
	 *            the defined Word-Pos-Alignments
	 * @return the created Tokens with POS, Chunks (and instruction number)
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public Token[] parse(String[] text, WordPosType wordPosList)
			throws IOException, URISyntaxException, InterruptedException {
		File tempFile = writeToTempFile(text);
		return shallowNLP(wordPosList, tempFile);
	}

	/**
	 * This method parses multiple Hypotheses as batch. A hypothesis is a list
	 * of Strings. The result is a list of tagged hypotheses (List<List
	 * <String>>)
	 * 
	 * @param hypotheses
	 *            the hypotheses to process
	 * @param wordPosList
	 *            the defined Word-Pos-Alignments
	 * @return tagged hypotheses (List<List<String>>)
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	private List<List<Token>> parse(List<List<String>> hypotheses, WordPosType wordPosList)
			throws IOException, URISyntaxException, InterruptedException {
		List<List<Token>> result = new ArrayList<List<Token>>();
		for (List<String> hypothesis : hypotheses) {
			result.add(Arrays.asList(parse(String.join(" ", hypothesis), wordPosList)));
		}
		return result;
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

	private Token[] shallowNLP(WordPosType list, File tempFile)
			throws IOException, URISyntaxException, InterruptedException {
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
	private Token[] sennaAndStanford(WordPosType list, File tempFile)
			throws IOException, URISyntaxException, InterruptedException {
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
	 * This method realizes the batched pos tagging with SENNA.
	 * 
	 * @param tempFile
	 * @return A List of Token-Arrays which is the result of batched parsing
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	private List<Token[]> onlySennaBatch(File tempFile) throws IOException, URISyntaxException, InterruptedException {
		logger.info("Starting BATCHED pos taggig with Senna");
		Facade f = new Facade();
		CalcInstruction ci = new CalcInstruction();
		List<Token[]> resultList = new ArrayList<Token[]>();
		WordPosType sennaParse = new Senna().parse(tempFile);
		List<WordPosType> debatchedList = generateWordPosList(sennaParse);
		for (WordPosType curWps : debatchedList) {
			String[] words = curWps.getWords();
			String[] posSenna = curWps.getPos();

			for (int i = 0; i < words.length; i++) {
				if (fillers.contains(words[i].toLowerCase()))
					posSenna[i] = POSTag.INTERJECTION.getTag();
			}

			String[] chunks = new Facade().parse(words, posSenna);

			int[] instr = new int[words.length];
			if (imp) {
				try {
					instr = ci.calculateInstructionNumber(words, posSenna);
				} catch (IllegalArgumentException e) {
					logger.error("Cannot calculate instruction number, instruction number is set to -1", e);
					Arrays.fill(instr, -1);
				}

			}

			resultList.add(createTokens(words, posSenna, instr, chunks));
		}
		return resultList;
	}

	// TODO only copy
	/**
	 * This method realizes the batched pos tagging with SENNA and Stanford.
	 * 
	 * @param tempFile
	 * @return A List of Token-Arrays which is the result of batched parsing
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	private List<Token[]> sennaAndStanfordBatch(File tempFile)
			throws IOException, URISyntaxException, InterruptedException {
		logger.info("Starting BATCHED pos taggig with Senna");
		Facade f = new Facade();
		CalcInstruction ci = new CalcInstruction();
		Stanford s = new Stanford();
		List<Token[]> resultList = new ArrayList<Token[]>();
		WordPosType sennaParse = new Senna().parse(tempFile);
		List<WordPosType> debatchedList = generateWordPosList(sennaParse);
		for (WordPosType curWps : debatchedList) {
			String[] words = curWps.getWords();
			String[] posSenna = curWps.getPos();
			String[] posStan = s.posTag(words);
			for (int i = 0; i < words.length; i++) {
				if (fillers.contains(words[i].toLowerCase()))
					posSenna[i] = POSTag.INTERJECTION.getTag();
				else if (!posSenna[i].startsWith("VB") && posStan[i].startsWith("VB"))
					posSenna[i] = posStan[i];
			}

			String[] chunks = new Facade().parse(words, posSenna);

			int[] instr = new int[words.length];
			if (imp) {
				try {
					instr = ci.calculateInstructionNumber(words, posSenna);
				} catch (IllegalArgumentException e) {
					logger.error("Cannot calculate instruction number, instruction number is set to -1", e);
					Arrays.fill(instr, -1);
				}

			}

			resultList.add(createTokens(words, posSenna, instr, chunks));
		}
		return resultList;
	}

	List<WordPosType> generateWordPosList(WordPosType sennaParse) {
		List<WordPosType> debatched = new ArrayList<WordPosType>();
		List<String> curWords = new ArrayList<String>();
		List<String> curPos = new ArrayList<String>();
		for (int i = 0; i < sennaParse.getWords().length; i++) {
			if (sennaParse.getPos()[i].equals(".")) {
				debatched.add(new WordPosType(curWords.toArray(new String[curWords.size()]),
						curPos.toArray(new String[curPos.size()])));
				curWords = new ArrayList<String>();
				curPos = new ArrayList<String>();
			} else {
				curWords.add(sennaParse.getWords()[i]);
				curPos.add(sennaParse.getPos()[i]);
			}
		}
		return debatched;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kit.ipd.parse.luna.pipeline.IPipelineStage#getID()
	 */
	@Override
	public String getID() {
		return ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.kit.ipd.parse.luna.pipeline.IPipelineStage#exec(edu.kit.ipd.parse
	 * .luna.data.AbstractPipelineData)
	 */
	@Override
	public void exec(AbstractPipelineData data) throws PipelineStageException {

		// TODO: clean up!

		// try to get data as pre pipeline data. If this fails, return
		try {
			prePipeData = data.asPrePipelineData();
		} catch (PipelineDataCastException e) {
			logger.error("Cannot process on data - PipelineData unreadable", e);
			throw new PipelineStageException(e);
		}

		// try to process on hypotheses. This is the default option
		try {
			List<List<String>> hypotheses = prePipeData.getHypotheses();
			List<List<Token>> taggedHypotheses = parse(hypotheses, null);
			prePipeData.setTaggedHypotheses(taggedHypotheses);
			return;
		} catch (MissingDataException e) {
			logger.info("No utterance array to process, trying single input instead...");
		} catch (IOException e) {
			logger.error("An IOException occured during run of SENNA", e);
			throw new PipelineStageException(e);
		} catch (URISyntaxException e) {
			logger.error("An URISyntaxException occured during initialization of SENNA", e);
			throw new PipelineStageException(e);
		} catch (InterruptedException e) {
			logger.error("The SENNA process interrupted unexpectedly", e);
			throw new PipelineStageException(e);
		}

		// try to process on utterance array
		try {
			String[] utterances = prePipeData.getTranscriptions();
			Token[] tokens = parse(utterances, null);
			prePipeData.setTokens(tokens);
			prePipeData.setGraph(createAGGGraph(tokens));
			return;
		} catch (MissingDataException e) {
			logger.info("No utterance array to process, trying single input instead...");
		} catch (IOException e) {
			logger.error("An IOException occured during run of SENNA", e);
			throw new PipelineStageException(e);
		} catch (URISyntaxException e) {
			logger.error("An URISyntaxException occured during initialization of SENNA", e);
			throw new PipelineStageException(e);
		} catch (InterruptedException e) {
			logger.error("The SENNA process interrupted unexpectedly", e);
			throw new PipelineStageException(e);
		}

		// try to process n single utterance. If this fails, return and show
		// error, as we have no other alternative
		try {
			String utterance = prePipeData.getTranscription();
			Token[] tokens = parse(utterance, null);
			prePipeData.setTokens(tokens);
			prePipeData.setGraph(createAGGGraph(tokens));
			return;
		} catch (MissingDataException e) {
			logger.error("No utterance to process, abborting...", e);
			throw new PipelineStageException(e);
		} catch (IOException e) {
			logger.error("An IOException occured during run of SENNA", e);
			throw new PipelineStageException(e);
		} catch (URISyntaxException e) {
			logger.error("An URISyntaxException occured during initialization of SENNA", e);
			throw new PipelineStageException(e);
		} catch (InterruptedException e) {
			logger.error("The SENNA process interrupted unexpectedly", e);
			throw new PipelineStageException(e);
		}

	}
}
