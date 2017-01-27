package edu.kit.ipd.parse.shallownlp;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.kit.ipd.parse.luna.data.token.Token;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * This class represents a facade for Stanford CoreNLP
 *
 * @author Markus Kocybik
 * @author Tobias Hey - MaxentTagger only loaded once (2016-07-28)
 */
public class Stanford {

	Properties props;
	MaxentTagger tagger;
	Morphology morph;

	Stanford() {
		props = ConfigManager.getConfiguration(getClass());
		tagger = new MaxentTagger(props.getProperty("TAGGER_MODEL"));
		morph = new Morphology();
	}

	/**
	 * This method realizes pos tagging with the Stanford POS Tagger
	 *
	 * @param text
	 *            the input text. Each element in the array represents one word.
	 * @return the pos tags
	 */
	public String[] posTag(String[] text) {

		List<HasWord> sent = Sentence.toWordList(text);
		List<TaggedWord> taggedSent = tagger.tagSentence(sent);
		String[] result = new String[taggedSent.size()];
		for (int i = 0; i < taggedSent.size(); i++) {
			result[i] = taggedSent.get(i).tag();
		}
		return result;
	}

	/**
	 * This method splits a text which contains periods
	 *
	 * @param text
	 *            an input text with periods to split
	 * @return each element in the array represents one sentence
	 */
	public String[] splitSentence(String text) {
		DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(text));
		List<String> sentenceList = new ArrayList<String>();

		for (List<HasWord> sentence : dp) {
			String sentenceString = Sentence.listToString(sentence);
			sentenceList.add(sentenceString.toString());
		}
		return sentenceList.toArray(new String[sentenceList.size()]);
	}

	/**
	 * adds stem and lemma to the specified {@link Token}s
	 *
	 * @param text
	 *            the {@link Token} to stem and lemmatize
	 */
	public void stemAndLemmatize(List<Token> text) {
		for (Token token : text) {
			String stem = morph.stem(token.getWord());
			String lemma = morph.lemma(token.getWord(), token.getPos().getTag(), true);
			token.setLemma(lemma);
			token.setStem(stem);
		}
	}

}
