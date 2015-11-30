package edu.kit.ipd.parse.shallownlp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents all valid pos tags
 * @author Markus Kocybik
 */
public enum POSTag {
  ADJECTIVE( "JJ" ),
  ADJECTIVE_COMPARATIVE( ADJECTIVE + "R" ),
  ADJECTIVE_SUPERLATIVE( ADJECTIVE + "S" ),
  ADVERB( "RB" ),
  ADVERB_COMPARATIVE( ADVERB + "R" ),
  ADVERB_SUPERLATIVE( ADVERB + "S" ),
  ADVERB_WH( "W" + ADVERB ),
  CONJUNCTION_COORDINATING( "CC" ),
  CONJUNCTION_SUBORDINATING( "IN" ),
  CARDINAL_NUMBER( "CD" ),
  DETERMINER( "DT" ),
  DETERMINER_WH( "W" + DETERMINER ),
  EXISTENTIAL_THERE( "EX" ),
  FOREIGN_WORD( "FW" ),
  LIST_ITEM_MARKER( "LS" ),
  NOUN( "NN" ),
  NOUN_PLURAL( NOUN + "S" ),
  NOUN_PROPER_SINGULAR( NOUN + "P" ),
  NOUN_PROPER_PLURAL( NOUN + "PS" ),
  PREDETERMINER( "PDT" ),
  POSSESSIVE_ENDING( "POS" ),
  PRONOUN_PERSONAL( "PRP" ),
  PRONOUN_POSSESSIVE( "PRP$" ),
  PRONOUN_POSSESSIVE_WH( "WP$" ),
  PRONOUN_WH( "WP" ),
  PARTICLE( "RP" ),
  SYMBOL( "SYM" ),
  TO( "TO" ),
  INTERJECTION( "UH" ),
  VERB( "VB" ),
  VERB_PAST_TENSE( VERB + "D" ),
  VERB_PARTICIPLE_PRESENT( VERB + "G" ),
  VERB_PARTICIPLE_PAST( VERB + "N" ),
  VERB_SINGULAR_PRESENT_NONTHIRD_PERSON( VERB + "P" ),
  VERB_SINGULAR_PRESENT_THIRD_PERSON( VERB + "Z" ),
  VERB_MODAL( "MD" ),


  CLOSER( "." ),
  COMMA( "," ),
  COLON( ":" ),
  LEFT_PAREN( "("),
  RIGHT_PAREN( ")" );
  
  private static final Logger logger = LoggerFactory.getLogger(POSTag.class);
  
  private final String tag;

  private POSTag( String tag ) {
    this.tag = tag;
  }

  /**
   * Returns the encoding for this part-of-speech.
   * 
   * @return A string representing a Penn Treebank encoding for an English
   * part-of-speech.
   */
  public String toString() {
    return getTag();
  }

  protected String getTag() {
    return this.tag;
  }

  public static POSTag get( String value ) {
    for( POSTag v : values() ) {
      if( value.equals( v.getTag() ) ) {
        return v;
      }
    }
    logger.error("Unknown part of speech: " + value );
    throw new IllegalArgumentException( "Unknown part of speech: " + value + "." );
  }
  
  public boolean isVerb() {
	  return this.getTag().startsWith("VB");
  }
}
