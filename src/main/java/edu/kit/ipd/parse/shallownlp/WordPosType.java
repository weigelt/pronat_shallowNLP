package edu.kit.ipd.parse.shallownlp;

public class WordPosType {
	private String[] words;
	private String[] pos;

	public WordPosType(String[] words, String[] pos) {
		this.words = words;
		this.pos = pos;
	}

	public String[] getWords() {
		return words;
	}

	public String[] getPos() {
		return pos;
	}
}
