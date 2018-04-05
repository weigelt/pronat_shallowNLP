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

import edu.kit.ipd.parse.brillRules.ChunkRule;
import edu.kit.ipd.parse.brillRules.IRule;
import edu.kit.ipd.parse.brillRules.POSRule;
import edu.kit.ipd.parse.luna.data.AbstractPipelineData;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PipelineDataCastException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.data.token.Chunk;
import edu.kit.ipd.parse.luna.data.token.ChunkIOB;
import edu.kit.ipd.parse.luna.data.token.MainHypothesisToken;
import edu.kit.ipd.parse.luna.data.token.POSTag;
import edu.kit.ipd.parse.luna.data.token.Token;
import edu.kit.ipd.parse.luna.data.token.WordPosType;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import edu.kit.ipd.parse.luna.graph.ParseNode;
import edu.kit.ipd.parse.luna.pipeline.IPipelineStage;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.kit.ipd.parse.parsebios.Facade;
import edu.kit.ipd.parse.senna_wrapper.Senna;
import edu.kit.ipd.parse.senna_wrapper.WordSennaResult;

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

	// TODO: move this to config
	private static final String RELATION_ARC_TYPE = "relation";
	private static final String TOKEN_NODE_TYPE = "token";

	private static final String ID = "snlp";

	private PrePipelineData prePipeData;

	private Properties props;
	private Stanford stanford;
	private CalcInstruction ci;

	private boolean imp, opt, stanfordOnly, containPeriods, excludeFillers, parseAlternatives;

	private static List<String> fillers;

	private WordPosType wordPosList;

	private List<IRule> brillRules;

	/*
	 * (non-Javadoc)
	 *
	 * @see edu.kit.ipd.parse.luna.pipeline.IPipelineStage#init()
	 */
	@Override
	public void init() {
		props = ConfigManager.getConfiguration(getClass());
		stanford = new Stanford();
		ci = new CalcInstruction();
		imp = Boolean.parseBoolean(props.getProperty("IMPERATIVE"));
		containPeriods = Boolean.parseBoolean(props.getProperty("PERIODS"));
		excludeFillers = Boolean.parseBoolean(props.getProperty("EXCLUDE_FILLERS"));
		opt = props.getProperty("MODE").equals("sennaandstanford");
		stanfordOnly = props.getProperty("MODE").equals("stanford");
		parseAlternatives = Boolean.parseBoolean(props.getProperty("ALTERNATIVES"));
		fillers = new ArrayList<String>();
		if (excludeFillers) {
			fillers.addAll(Arrays.asList(props.getProperty("FILLERS").split(",")));
		}
		brillRules = parseBrillRules(props.getProperty("BRILL_RULES"));
	}

	private List<IRule> parseBrillRules(String input) {
		List<IRule> result = new ArrayList<>();
		String[] splitted = input.trim().split(";");
		for (int i = 0; i < splitted.length; i++) {
			String rule = splitted[i];
			String[] ruleParts = rule.trim().split(" ", 5);
			if (ruleParts.length < 4) {
				logger.warn("Brill rule wasn't specified correctly: " + rule);
				continue;
			}
			String type = ruleParts[0];
			String word = ruleParts[1];
			String from = ruleParts[2];
			String to = ruleParts[3];
			String condition = ruleParts[4];
			if (condition.contains("[")) {
				condition = condition.replaceAll("\\[", "").trim().replaceAll("\\]", "").trim();
			}
			if (type.equals("POS")) {
				result.add(new POSRule(word, from, to, condition));
			} else if (type.equals("CHK")) {
				result.add(new ChunkRule(word, from, to, condition));
			} else {
				logger.warn("Brill rule has no correct type: " + type);
				continue;
			}
		}
		return result;
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
	public Token[] parse(String text, WordPosType wordPosList) throws IOException, URISyntaxException, InterruptedException {
		File tempFile;
		String[] input;
		if (!containPeriods) {
			input = new String[] { text };
		} else {

			input = stanford.splitSentence(text);
			// opt = false;
		}
		tempFile = writeToTempFile(input);
		return shallowNLP(input, wordPosList, tempFile);
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
	public Token[] parse(String[] text, WordPosType wordPosList) throws IOException, URISyntaxException, InterruptedException {
		final File tempFile = writeToTempFile(text);
		return shallowNLP(text, wordPosList, tempFile);
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
	private List<List<Token>> parseBatch(List<List<MainHypothesisToken>> hypotheses, WordPosType wordPosList)
			throws IOException, URISyntaxException, InterruptedException {
		List<List<Token>> tokens;
		if (stanfordOnly) {
			tokens = onlyStanfordBatch(hypotheses, wordPosList);
		} else {
			File tempFile = writeBatchToTempFile(hypotheses);
			if (!opt) {

				tokens = onlySennaBatch(tempFile);

			} else {
				tokens = sennaAndStanfordBatch(wordPosList, tempFile);
			}
		}
		return tokens;
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
		final File tempFile = File.createTempFile("input", "txt");
		writer = new PrintWriter(tempFile);
		for (final String line : text) {
			writer.println(line);
		}
		writer.close();
		return tempFile;

	}

	private File writeBatchToTempFile(List<List<MainHypothesisToken>> hypotheses) throws IOException {
		PrintWriter writer;
		final File tempFile = File.createTempFile("input", "txt");
		writer = new PrintWriter(tempFile);
		for (final List<MainHypothesisToken> hypothesis : hypotheses) {
			for (final MainHypothesisToken hyp : hypothesis) {
				writer.print(hyp.getWord() + " ");
			}
			writer.println(". ");
		}
		writer.close();
		return tempFile;
	}

	private Token[] shallowNLP(String[] sentenceArray, WordPosType list, File tempFile)
			throws IOException, URISyntaxException, InterruptedException {
		if (stanfordOnly) {
			return onlyStanford(sentenceArray, list);
		}
		if (!opt) {
			return onlySenna(tempFile);
		} else {
			return sennaAndStanford(list, tempFile);
		}

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
		final List<WordSennaResult> list = new Senna(new String[] { "-usrtokens", "-pos" }).parse(tempFile);

		// get words and pos tags from Senna Result
		final String[] words = new String[list.size()];
		final String[] posSenna = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			final WordSennaResult word = list.get(i);
			words[i] = word.getWord();
			posSenna[i] = word.getAnalysisResults()[0];
		}

		final String[] pos = new String[words.length];
		for (int i = 0; i < words.length; i++) {
			if (fillers.contains(words[i].toLowerCase())) {
				pos[i] = POSTag.INTERJECTION.getTag();
			} else {
				pos[i] = posSenna[i];
			}
		}

		//apply brill_Pos Rules
		for (int i = 0; i < words.length; i++) {
			for (IRule brRule : brillRules) {
				if (brRule instanceof POSRule) {
					brRule.applyRule(words, pos, new String[] {}, i);
				}
			}
		}

		final String[] chunks = new Facade().parse(words, pos);

		//apply brill_chk Rules
		for (int i = 0; i < words.length; i++) {
			for (IRule brRule : brillRules) {
				if (brRule instanceof ChunkRule) {
					brRule.applyRule(words, pos, chunks, i);
				}
			}
		}

		int[] instr = new int[words.length];
		if (imp) {
			try {
				instr = ci.calculateInstructionNumber(words, pos);
			} catch (final IllegalArgumentException e) {
				logger.error("Cannot calculate instruction number, instruction number is set to -1", e);
				Arrays.fill(instr, -1);
			}

		}

		return createTokens(words, pos, instr, chunks);
	}

	/**
	 * This method realizes the pos tagging with Stanford.
	 *
	 * @param list
	 *            fix list of words which should be tagged as verbs is only used
	 *            if the parameter opt is true
	 * @param tempFile
	 * @return a token array which is the result of parsing
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	private Token[] onlyStanford(String[] sentenceArray, WordPosType list) throws IOException, URISyntaxException, InterruptedException {
		logger.info("using stanford core nlp for pos tagging");
		List<String> wordList = new ArrayList<>();
		for (final String line : sentenceArray) {
			for (String string : line.trim().split(" ")) {
				wordList.add(string.trim());
			}

		}
		final String[] words = wordList.toArray(new String[wordList.size()]);

		final String[] pos = stanford.posTag(words);

		//apply brill_Pos Rules
		for (int i = 0; i < words.length; i++) {
			for (IRule brRule : brillRules) {
				if (brRule instanceof POSRule) {
					brRule.applyRule(words, pos, new String[] {}, i);
				}
			}
		}

		final String[] chunks = new Facade().parse(words, pos);

		//apply brill_chk Rules
		for (int i = 0; i < words.length; i++) {
			for (IRule brRule : brillRules) {
				if (brRule instanceof ChunkRule) {
					brRule.applyRule(words, pos, chunks, i);
				}
			}
		}

		int[] instr = new int[words.length];
		if (imp) {
			try {
				instr = ci.calculateInstructionNumber(words, pos);
			} catch (final IllegalArgumentException e) {
				logger.error("Cannot calculate instruction number, instruction number is set to -1", e);
				Arrays.fill(instr, -1);
			}
		}
		List<Token> tokenList = Arrays.asList(createTokens(words, pos, instr, chunks));
		stanford.stemAndLemmatize(tokenList);

		return tokenList.toArray(new Token[tokenList.size()]);
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
		final List<WordSennaResult> results = new Senna(new String[] { "-usrtokens", "-pos" }).parse(tempFile);

		// get words and pos tags from Senna Result
		final String[] words = new String[results.size()];
		final String[] posSenna = new String[results.size()];
		for (int i = 0; i < results.size(); i++) {
			final WordSennaResult word = results.get(i);
			words[i] = word.getWord();
			posSenna[i] = word.getAnalysisResults()[0];
		}

		final String[] posStan = stanford.posTag(words);

		final String[] pos = new String[words.length];
		for (int i = 0; i < words.length; i++) {
			if (list != null && Arrays.asList(list.getWords()).contains(words[i])) {
				pos[i] = list.getPos()[Arrays.asList(list.getWords()).indexOf(words[i])];
			} else if (fillers.contains(words[i].toLowerCase())) {
				pos[i] = POSTag.INTERJECTION.getTag();
			} else if (posStan[i].startsWith("VB")) {
				pos[i] = posStan[i];
			} else {
				pos[i] = posSenna[i];
			}

		}
		//apply brill_Pos Rules
		for (int i = 0; i < words.length; i++) {
			for (IRule brRule : brillRules) {
				if (brRule instanceof POSRule) {
					brRule.applyRule(words, pos, new String[] {}, i);
				}
			}
		}

		final String[] chunks = new Facade().parse(words, pos);

		//apply brill_chk Rules
		for (int i = 0; i < words.length; i++) {
			for (IRule brRule : brillRules) {
				if (brRule instanceof ChunkRule) {
					brRule.applyRule(words, pos, chunks, i);
				}
			}
		}

		int[] instr = new int[words.length];
		if (imp) {
			try {
				instr = ci.calculateInstructionNumber(words, pos);
			} catch (final IllegalArgumentException e) {
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
	private List<List<Token>> onlySennaBatch(File tempFile) throws IOException, URISyntaxException, InterruptedException {
		logger.info("Starting BATCHED pos tagging with Senna");
		//		Facade f = new Facade();
		final List<List<Token>> resultList = new ArrayList<List<Token>>();
		final List<WordSennaResult> sennaParse = new Senna(new String[] { "-usrtokens", "-pos" }).parse(tempFile);
		final List<List<WordSennaResult>> debatchedList = generateDebatchedWordSennaResultList(sennaParse);
		for (final List<WordSennaResult> curWsrs : debatchedList) {

			// get words and pos tags from Senna Result
			final String[] words = new String[curWsrs.size()];
			final String[] posSenna = new String[curWsrs.size()];
			for (int i = 0; i < curWsrs.size(); i++) {
				final WordSennaResult word = curWsrs.get(i);
				words[i] = word.getWord();
				posSenna[i] = word.getAnalysisResults()[0];
			}

			for (int i = 0; i < words.length; i++) {
				if (fillers.contains(words[i].toLowerCase())) {
					posSenna[i] = POSTag.INTERJECTION.getTag();
				}
			}

			//apply brill_Pos Rules
			for (int i = 0; i < words.length; i++) {
				for (IRule brRule : brillRules) {
					if (brRule instanceof POSRule) {
						brRule.applyRule(words, posSenna, new String[] {}, i);
					}
				}
			}

			final String[] chunks = new Facade().parse(words, posSenna);

			//apply brill_chk Rules
			for (int i = 0; i < words.length; i++) {
				for (IRule brRule : brillRules) {
					if (brRule instanceof ChunkRule) {
						brRule.applyRule(words, posSenna, chunks, i);
					}
				}
			}

			int[] instr = new int[words.length];
			if (imp) {
				try {
					instr = ci.calculateInstructionNumber(words, posSenna);
				} catch (final IllegalArgumentException e) {
					logger.error("Cannot calculate instruction number, instruction number is set to -1", e);
					Arrays.fill(instr, -1);
				}

			}

			resultList.add(Arrays.asList(createTokens(words, posSenna, instr, chunks)));
		}
		for (List<Token> tokenList : resultList) {
			stanford.stemAndLemmatize(tokenList);
		}
		return resultList;
	}

	/**
	 * This method realizes the batched pos tagging with Stanford.
	 *
	 * @param tempFile
	 * @return A List of Token-Arrays which is the result of batched parsing
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	private List<List<Token>> onlyStanfordBatch(List<List<MainHypothesisToken>> hypotheses, WordPosType list)
			throws IOException, URISyntaxException, InterruptedException {
		logger.info("Starting BATCHED pos tagging with Stanford");
		//		Facade f = new Facade();
		final Stanford s = stanford;
		final List<List<Token>> resultList = new ArrayList<List<Token>>();
		//final List<WordSennaResult> sennaParse = new Senna(new String[] { "-usrtokens", "-pos" }).parse(tempFile);
		//final List<List<WordSennaResult>> debatchedList = generateDebatchedWordSennaResultList(sennaParse);
		List<String> wordList = new ArrayList<>();
		for (List<MainHypothesisToken> tokenList : hypotheses) {
			for (MainHypothesisToken token : tokenList) {
				wordList.add(token.getWord());
			}
		}
		String[] words = wordList.toArray(new String[wordList.size()]);

		String[] pos = s.posTag(words);
		List<WordPosType> debatchedList = generateDebatchedList(words, pos);
		for (final WordPosType curWpt : debatchedList) {
			words = curWpt.getWords();
			pos = curWpt.getPos();
			//apply brill_Pos Rules
			for (int i = 0; i < words.length; i++) {
				for (IRule brRule : brillRules) {
					if (brRule instanceof POSRule) {
						brRule.applyRule(words, pos, new String[] {}, i);
					}
				}
			}

			final String[] chunks = new Facade().parse(words, pos);

			//apply brill_chk Rules
			for (int i = 0; i < words.length; i++) {
				for (IRule brRule : brillRules) {
					if (brRule instanceof ChunkRule) {
						brRule.applyRule(words, pos, chunks, i);
					}
				}
			}

			int[] instr = new int[words.length];
			if (imp) {
				try {
					instr = ci.calculateInstructionNumber(words, pos);
				} catch (final IllegalArgumentException e) {
					logger.error("Cannot calculate instruction number, instruction number is set to -1", e);
					Arrays.fill(instr, -1);
				}

			}

			resultList.add(Arrays.asList(createTokens(words, pos, instr, chunks)));
		}
		for (List<Token> tokenList : resultList) {
			stanford.stemAndLemmatize(tokenList);
		}
		return resultList;
	}

	/**
	 * This method realizes the batched pos tagging with SENNA and Stanford.
	 *
	 * @param tempFile
	 * @return A List of Token-Arrays which is the result of batched parsing
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	private List<List<Token>> sennaAndStanfordBatch(WordPosType list, File tempFile)
			throws IOException, URISyntaxException, InterruptedException {
		logger.info("Starting BATCHED pos tagging with Senna and Stanford");
		//		Facade f = new Facade();
		final Stanford s = stanford;
		final List<List<Token>> resultList = new ArrayList<List<Token>>();
		final List<WordSennaResult> sennaParse = new Senna(new String[] { "-usrtokens", "-pos" }).parse(tempFile);
		final List<List<WordSennaResult>> debatchedList = generateDebatchedWordSennaResultList(sennaParse);
		for (final List<WordSennaResult> curWsrs : debatchedList) {
			final String[] words = new String[curWsrs.size()];
			final String[] posSenna = new String[curWsrs.size()];
			for (int i = 0; i < curWsrs.size(); i++) {
				final WordSennaResult word = curWsrs.get(i);
				words[i] = word.getWord();
				posSenna[i] = word.getAnalysisResults()[0];
			}
			final String[] posStan = s.posTag(words);
			for (int i = 0; i < words.length; i++) {
				if (list != null && Arrays.asList(list.getWords()).contains(words[i])) {
					posSenna[i] = list.getPos()[Arrays.asList(list.getWords()).indexOf(words[i])];
				} else if (fillers.contains(words[i].toLowerCase())) {
					posSenna[i] = POSTag.INTERJECTION.getTag();
				} else if (posStan[i].startsWith("VB")) {
					posSenna[i] = posStan[i];
				}
			}

			//apply brill_Pos Rules
			for (int i = 0; i < words.length; i++) {
				for (IRule brRule : brillRules) {
					if (brRule instanceof POSRule) {
						brRule.applyRule(words, posSenna, new String[] {}, i);
					}
				}
			}

			final String[] chunks = new Facade().parse(words, posSenna);

			//apply brill_chk Rules
			for (int i = 0; i < words.length; i++) {
				for (IRule brRule : brillRules) {
					if (brRule instanceof ChunkRule) {
						brRule.applyRule(words, posSenna, chunks, i);
					}
				}
			}

			int[] instr = new int[words.length];
			if (imp) {
				try {
					instr = ci.calculateInstructionNumber(words, posSenna);
				} catch (final IllegalArgumentException e) {
					logger.error("Cannot calculate instruction number, instruction number is set to -1", e);
					Arrays.fill(instr, -1);
				}

			}

			resultList.add(Arrays.asList(createTokens(words, posSenna, instr, chunks)));
		}
		for (List<Token> tokenList : resultList) {
			stanford.stemAndLemmatize(tokenList);
		}
		return resultList;
	}

	private List<WordPosType> generateDebatchedList(String[] words, String[] pos) {
		final List<WordPosType> debatched = new ArrayList<WordPosType>();
		List<String> currentWords = new ArrayList<>();
		List<String> currentPos = new ArrayList<>();
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			String posTag = pos[i];
			if (!word.equals(".")) {
				currentWords.add(word);
				currentPos.add(posTag);
			} else {
				String[] wordArray = currentWords.toArray(new String[currentWords.size()]);
				String[] posArray = currentPos.toArray(new String[currentPos.size()]);
				debatched.add(new WordPosType(wordArray, posArray));
				currentWords = new ArrayList<String>();
				currentPos = new ArrayList<String>();
			}
		}
		if (debatched.isEmpty() && !currentWords.isEmpty()) {
			debatched.add(new WordPosType(words, pos));
		}
		return debatched;
	}

	List<List<WordSennaResult>> generateDebatchedWordSennaResultList(List<WordSennaResult> sennaParse) {
		final List<List<WordSennaResult>> debatched = new ArrayList<List<WordSennaResult>>();
		List<WordSennaResult> current = new ArrayList<WordSennaResult>();
		for (int i = 0; i < sennaParse.size(); i++) {
			if (sennaParse.get(i).getAnalysisResults()[0].equals(".")) {
				debatched.add(current);
				current = new ArrayList<WordSennaResult>();
			} else {
				current.add(sennaParse.get(i));
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
		final Token[] result = new Token[words.length];
		final Chunk[] chunks = new Chunk().convertIOB(chunksIOB);
		for (int i = 0; i < words.length; i++) {
			final Token tmp = new Token(words[i], POSTag.get(pos[i]), ChunkIOB.get(chunksIOB[i]), chunks[i], i, instr[i]);
			result[i] = tmp;
		}
		return result;
	}

	/**
	 * This method creates the ParseGraph from the token array.
	 *
	 * @param input
	 *            the token array
	 * @return the ParseGraph
	 */
	@Deprecated
	public IGraph createParseGraph(Token[] input) {
		final IGraph graph = new ParseGraph();
		INodeType wordType;
		IArcType arcType;
		if (graph.hasNodeType(TOKEN_NODE_TYPE)) {
			wordType = graph.getNodeType(TOKEN_NODE_TYPE);
		} else {
			wordType = graph.createNodeType(TOKEN_NODE_TYPE);
		}

		if (graph.hasArcType(RELATION_ARC_TYPE)) {
			arcType = graph.getArcType(RELATION_ARC_TYPE);
		} else {
			arcType = graph.createArcType(RELATION_ARC_TYPE);
		}

		wordType.addAttributeToType("String", "value");
		wordType.addAttributeToType("String", "pos");
		wordType.addAttributeToType("String", "chunkIOB");
		wordType.addAttributeToType("String", "chunkName");
		wordType.addAttributeToType("int", "predecessors");
		wordType.addAttributeToType("int", "successors");
		wordType.addAttributeToType("int", "instructionNumber");
		wordType.addAttributeToType("int", "position");
		arcType.addAttributeToType("String", "value");

		final INode[] nodes = new ParseNode[input.length];
		for (final Token tok : input) {
			final INode node = graph.createNode(wordType);
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
				final IArc arc = graph.createArc(nodes[i], nodes[i + 1], arcType);
				arc.setAttributeValue("value", "NEXT");
			}
		}
		return graph;
	}

	@Deprecated
	private List<IGraph> createBatchGraphs(List<List<Token>> hypotheses) {
		final List<IGraph> graphs = new ArrayList<IGraph>();
		for (final List<Token> hyp : hypotheses) {
			graphs.add(createParseGraph(hyp.toArray(new Token[hyp.size()])));
		}
		return graphs;
	}

	private void transferTokenInformation(List<List<MainHypothesisToken>> source, List<List<Token>> sink) throws PipelineStageException {
		if (sink.size() != source.size()) {
			logger.error("Hypotheses and tagged Hypotheses size differs");
			throw new PipelineStageException();
		}
		for (int i = 0; i < source.size(); i++) {
			if (sink.get(i).size() != source.get(i).size()) {
				logger.error("A Hypothesis and a tagged Hypothesis differ in size");
				throw new PipelineStageException();
			}
			for (int j = 0; j < sink.get(i).size(); j++) {
				sink.get(i).get(j).consumeHypothesisToken(source.get(i).get(j));
			}
		}
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
		} catch (final PipelineDataCastException e) {
			logger.error("Cannot process on data - PipelineData unreadable", e);
			throw new PipelineStageException(e);
		}

		// try to process on hypotheses. This is the default option
		if (parseAlternatives) {
			// MODE: main and alternative hypotheses
			try {
				final List<List<MainHypothesisToken>> hypotheses = prePipeData.getAltHypotheses();
				hypotheses.add(0, prePipeData.getMainHypothesis());
				final List<List<Token>> taggedHypotheses = parseBatch(hypotheses, wordPosList);
				transferTokenInformation(hypotheses, taggedHypotheses);
				prePipeData.setTaggedHypotheses(taggedHypotheses);
				//				final List<IGraph> graphs = createBatchGraphs(taggedHypotheses);
				//				prePipeData.setGraph(graphs.get(0));
				//				if (graphs.size() > 1) {
				//					for (int i = 1; i < graphs.size(); i++) {
				//						//TODO: add alternatives
				//					}
				//				}
				return;
			} catch (final MissingDataException e) {
				logger.info("No main hypothesis to process, trying single input instead...");
			} catch (final IOException e) {
				logger.error("An IOException occured during run of SENNA", e);
				throw new PipelineStageException(e);
			} catch (final URISyntaxException e) {
				logger.error("An URISyntaxException occured during initialization of SENNA", e);
				throw new PipelineStageException(e);
			} catch (final InterruptedException e) {
				logger.error("The SENNA process interrupted unexpectedly", e);
				throw new PipelineStageException(e);
			}
		} else {
			// MODE: only main hypothesis
			try {
				final List<List<MainHypothesisToken>> hypotheses = new ArrayList<List<MainHypothesisToken>>();
				hypotheses.add(prePipeData.getMainHypothesis());
				final List<List<Token>> taggedHypotheses = parseBatch(hypotheses, wordPosList);
				transferTokenInformation(hypotheses, taggedHypotheses);
				prePipeData.setTaggedHypotheses(taggedHypotheses);
				//				final List<IGraph> graphs = createBatchGraphs(taggedHypotheses);
				//				prePipeData.setGraph(graphs.get(0));
				return;
			} catch (final MissingDataException e) {
				logger.info("No main hypothesis to process, trying single input instead...");
			} catch (final IOException e) {
				logger.error("An IOException occured during run of SENNA", e);
				throw new PipelineStageException(e);
			} catch (final URISyntaxException e) {
				logger.error("An URISyntaxException occured during initialization of SENNA", e);
				throw new PipelineStageException(e);
			} catch (final InterruptedException e) {
				logger.error("The SENNA process interrupted unexpectedly", e);
				throw new PipelineStageException(e);
			}
		}

		// try to process on utterance array
		try {
			final String[] utterances = prePipeData.getTranscriptions();
			final Token[] tokens = parse(utterances, null);
			prePipeData.setTokens(tokens);
			//			prePipeData.setGraph(createParseGraph(tokens));
			return;
		} catch (final MissingDataException e) {
			logger.info("No utterance array to process, trying single input instead...");
		} catch (final IOException e) {
			logger.error("An IOException occured during run of SENNA", e);
			throw new PipelineStageException(e);
		} catch (final URISyntaxException e) {
			logger.error("An URISyntaxException occured during initialization of SENNA", e);
			throw new PipelineStageException(e);
		} catch (final InterruptedException e) {
			logger.error("The SENNA process interrupted unexpectedly", e);
			throw new PipelineStageException(e);
		}

		// try to process n single utterance. If this fails, return and show
		// error, as we have no other alternative
		try {
			final String utterance = prePipeData.getTranscription();
			final Token[] tokens = parse(utterance, null);
			prePipeData.setTokens(tokens);
			//			prePipeData.setGraph(createParseGraph(tokens));
			return;
		} catch (final MissingDataException e) {
			logger.error("No utterance to process, abborting...", e);
			throw new PipelineStageException(e);
		} catch (final IOException e) {
			logger.error("An IOException occured during run of SENNA", e);
			throw new PipelineStageException(e);
		} catch (final URISyntaxException e) {
			logger.error("An URISyntaxException occured during initialization of SENNA", e);
			throw new PipelineStageException(e);
		} catch (final InterruptedException e) {
			logger.error("The SENNA process interrupted unexpectedly", e);
			throw new PipelineStageException(e);
		}

	}
}
