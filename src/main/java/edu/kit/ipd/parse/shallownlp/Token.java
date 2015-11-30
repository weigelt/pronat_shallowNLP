package edu.kit.ipd.parse.shallownlp;

/**
 * This class represents the internal representation of a token. 
 * Each token consists of a word, position, pos tag, chunk and an instruction number. 
 * @author Markus Kocybik
 */
public class Token {
	private String word;
	private int position;
	private POSTag pos;
	private ChunkIOB chunkIOB;
	private int instructionNumber;
	private Chunk chunk; 
	
	public Token(String word, POSTag pos, ChunkIOB chunkIOB, Chunk chunk, int position, int instructionNumber){
		this.word = word;
		this.pos = pos;
		this.position = position;
		this.chunkIOB = chunkIOB;
		this.chunk = chunk;
		this.instructionNumber = instructionNumber;
	}
	
	public ChunkIOB getChunkIOB() {
		return chunkIOB;
	}

	public void setChunkIOB(ChunkIOB chunkIOB) {
		this.chunkIOB = chunkIOB;
	}

	public void setChunk(Chunk chunk) {
		this.chunk = chunk;
	}
	
	public Chunk getChunk() {
		return chunk;
	}

	public int getInstructionNumber() {
		return instructionNumber;
	}

	public void setInstructionNumber(int instructionNumber) {
		this.instructionNumber = instructionNumber;
	}

	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public POSTag getPos() {
		return pos;
	}
	public void setPos(POSTag pos) {
		this.pos = pos;
	}

	
	public String toString(){
		return word + "(" + pos + "/" +  instructionNumber + "/" + chunkIOB  + "/"
				+ chunk.getName() + "/" +  chunk.getPredecessor() + "/" + chunk.getSuccessor() + ")";
	}
}
