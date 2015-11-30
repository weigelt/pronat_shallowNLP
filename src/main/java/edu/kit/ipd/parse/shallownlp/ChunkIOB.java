package edu.kit.ipd.parse.shallownlp;

/**
 * This class represents all valid chunks (IOB-format)
 * @author Markus Kocybik
 */
public enum ChunkIOB {
	NOUN_PHRASE_BEGIN( "B-NP" ),
	NOUN_PHRASE_INSIDE( "I-NP" ),
	PREPOSITIONAL_PHRASE_BEGIN( "B-PP" ),
	PREPOSITIONAL_PHRASE_INSIDE( "I-PP" ),
	VERB_PHRASE_BEGIN( "B-VP" ),
	VERB_PHRASE_INSIDE( "I-VP" ),
	ADVERB_PHRASE_BEGIN( "B-ADVP" ),
	ADVERB_PHRASE_INSIDE( "I-ADVP" ),
	ADJECTIVE_PHRASE_BEGIN( "B-ADJP" ),
	ADJECTIVE_PHRASE_INSIDE( "I-ADJP" ),
	SUBORDINATING_CONJUNCTION_BEGIN( "B-SBAR" ),
	SUBORDINATING_CONJUNCTION_INSIDE( "I-SBAR" ),
	PARTICLE_BEGIN( "B-PRT" ),
	PARTICLE_INSIDE( "I-PRT" ),
	INTERJECTION_BEGIN( "B-INTJ" ),
	INTERJECTION_INSIDE( "I-INTJ" ),
	OUTSIDE( "O" );
	
	 private final String tag;
	 private ChunkIOB( String tag ) {
		    this.tag = tag;
	 }
	 
	 public String toString() {
		 return getTag();
	 }

	 protected String getTag() {
		 return this.tag;
	 }

	 public static ChunkIOB get( String value ) {
		 for( ChunkIOB v : values() ) {
			 if( value.equals( v.getTag() ) ) {
		        return v;
		      }
		 }

		 throw new IllegalArgumentException( "Unknown part of speech: '" + value + "'." );
	 }
}
