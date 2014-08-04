package lemma;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import lexicon.Lexicon;
import normalization.AdverbNormalizer;
import normalization.AugmentativeNormalizer;
import normalization.DiminutiveNormalizer;
import normalization.GenderNormalizer;
import normalization.NumberNormalizer;
import normalization.SuperlativeNormalizer;
import normalization.VerbNormalizer;

import org.xml.sax.SAXException;

import dictionary.Dictionary;
import dictionary.DictionaryParsingException;
import rank.WordRanking;
import rank.WordRankingParsingException;
import replacement.Replacement;

/**
 * This class ...
 *
 * @author   Ricardo Rodrigues
 * @version  0.9.1
 */
public class Lemmatizer {
  private static final String DEFAULT_PROP = "resources/properties/lemport.xml";

  /**
   * This field...
   */
  public static final int AUGMENTATIVE = 1;         // Binary 000000001

  /**
   * This field...
   */
  public static final int SUPERLATIVE = 2;          // Binary 000000010

  /**
   * This field...
   */
  public static final int DIMINUTIVE = 4;           // Binary 000000100

  /**
   * This field...
   */
  public static final int GENDER_ALL = 8;           // Binary 000001000

  /**
   * This field...
   */
  public static final int GENDER_DECLENSIONS = 16;  // Binary 000010000

  /**
   * This field...
   */
  public static final int GENDER_NOUNS = 32;       // Binary 000100000

  /**
   * This field...
   */
  public static final int NUMBER = 64;              // Binary 001000000

  /**
   * This field...
   */
  public static final int ADVERB = 128;             // Binary 010000000

  /**
   * This field...
   */
  public static final int VERB = 256;               // Binary 100000000

  /**
   * This field...
   */
  public static final int ALL = 511;                // Binary 111111111

  private AugmentativeNormalizer augmentativeNormalizer = null;
  private SuperlativeNormalizer superlativeNormalizer = null;
  private DiminutiveNormalizer diminutiveNormalizer = null;
  private GenderNormalizer genderNormalizer = null;
  private NumberNormalizer numberNormalizer = null;
  private AdverbNormalizer adverbNormalizer = null;
  private VerbNormalizer verbNormalizer = null;
  private String augmentativeTag = null;
  private String superlativeTag = null;
  private String diminutiveTag = null;
  private String genderTag = null;
  private String numberTag = null;
  private String adverbTag = null;
  private String verbTag = null;
  private int flags = 0;                            // Binary 000000000
  private int cacheSize = 0;
  private boolean breakOnHyphen = false;
  private boolean breakOnUnderscore = false;
  private LemmatizerCache cache = null;
  private Dictionary dictionary = null;
  private Lexicon lexicon = null;
  private WordRanking ranking = null;
  private String dictionaryExclusions = null;
  private HashMap<String, String> lexiconConversions = null;

  /**
   * Creates a new <code>Lemmatizer</code> object ...
   * 
   * @throws NumberFormatException
   * @throws InvalidPropertiesFormatException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws DictionaryParsingException
   * @throws WordRankingParsingException
   */
  public Lemmatizer()
      throws NumberFormatException, InvalidPropertiesFormatException,
      IOException, ParserConfigurationException, SAXException,
      DictionaryParsingException, WordRankingParsingException {
    this(Short.MAX_VALUE);
  }

  /**
   * Creates a new <code>Lemmatizer</code> object ...
   * 
   * @param  cacheSize ...
   * @throws NumberFormatException
   * @throws InvalidPropertiesFormatException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws DictionaryParsingException
   * @throws WordRankingParsingException
   */
  public Lemmatizer(int cacheSize)
      throws NumberFormatException, InvalidPropertiesFormatException,
      IOException, ParserConfigurationException, SAXException,
      DictionaryParsingException, WordRankingParsingException {
    this(cacheSize, true, true);
  }


  /**
   * Creates a new <code>Lemmatizer</code> object ...
   * 
   * @param  cacheSize ...
   * @param  breakOnHyphen ...
   * @param  breakOnUnderscore ...
   * @throws InvalidPropertiesFormatException
   * @throws IOException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws DictionaryParsingException
   * @throws NumberFormatException
   * @throws WordRankingParsingException
   */
  public Lemmatizer(int cacheSize, boolean breakOnHyphen,
      boolean breakOnUnderscore)
          throws InvalidPropertiesFormatException, IOException,
          ParserConfigurationException, SAXException,
          DictionaryParsingException, NumberFormatException,
          WordRankingParsingException {
    Properties properties = new Properties();
    properties.loadFromXML(
        this.getClass().getClassLoader().getResourceAsStream(DEFAULT_PROP));
    InputStream adverbDeclensionInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("adverbDeclensions"));
    InputStream augmentativeDeclensionInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("augmentativeDeclensions"));
    InputStream diminutiveDeclensionInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("diminutiveDeclensions"));
    InputStream genderDeclensionInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("genderDeclensions"));
    InputStream genderNounInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("genderNouns"));
    InputStream numberDeclensionInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("numberDeclensions"));
    InputStream superlativeDeclensionInput = 
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("superlativeDeclensions"));
    InputStream irregularVerbConjugationInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("irregularVerbConjugations"));
    InputStream regularVerbLexemeInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("regularVerbLexemes"));
    InputStream regularVerbDeclensionInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("regularVerbDeclensions"));
    InputStream dictionaryInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("dictionary"));
    InputStream customDictionaryInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("customDictionary"));
    InputStream wordRankingInput =
        this.getClass().getClassLoader().getResourceAsStream(
            properties.getProperty("wordRanking"));
    String dictionaryExclusions =
        properties.getProperty("dictionaryExclusions");
    HashMap<String, String> lexiconConversions = new HashMap<String, String>();
    String[] conversions = properties.getProperty("lexConversions").split(";");
    for (String conversion : conversions) {
      if (conversion.contains(":") && (conversion.indexOf(":") > 0)
          && (conversion.indexOf(":") < conversion.length() - 1)) {
        lexiconConversions.put(conversion.substring(0, conversion.indexOf(":")),
            conversion.substring(conversion.indexOf(":") + 1));
      }
    }
    this.initialize(adverbDeclensionInput, augmentativeDeclensionInput,
        diminutiveDeclensionInput, genderDeclensionInput, genderNounInput,
        numberDeclensionInput, superlativeDeclensionInput,
        irregularVerbConjugationInput, regularVerbLexemeInput,
        regularVerbDeclensionInput, dictionaryInput, customDictionaryInput,
        wordRankingInput, dictionaryExclusions, lexiconConversions, cacheSize,
        breakOnHyphen, breakOnUnderscore);
  }

  /**
   * Creates a new <code>Lemmatizer</code> object ...
   * 
   * @param  adverbDeclensionInput ...
   * @param  augmentativeDeclensionInput ...
   * @param  diminutiveDeclensionInput ...
   * @param  genderDeclensionInput ...
   * @param  genderNounInput ...
   * @param  numberDeclensionInput ...
   * @param  superlativeDeclensionInput ...
   * @param  irregularVerbConjugationInput ...
   * @param  regularVerbLexemeInput ...
   * @param  regularVerbDeclensionInput ...
   * @param  dictionaryInput ...
   * @param  customDictionaryInput ...
   * @param  wordRankingInput ...
   * @param  dictionaryExclusions ...
   * @param  lexiconConversions ...
   * @throws NumberFormatException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @throws DictionaryParsingException
   * @throws WordRankingParsingException
   */
  public Lemmatizer(InputStream adverbDeclensionInput,
      InputStream augmentativeDeclensionInput,
      InputStream diminutiveDeclensionInput,
      InputStream genderDeclensionInput,
      InputStream genderNounInput,
      InputStream numberDeclensionInput,
      InputStream superlativeDeclensionInput,
      InputStream irregularVerbConjugationInput,
      InputStream regularVerbLexemeInput,
      InputStream regularVerbDeclensionInput,
      InputStream dictionaryInput,
      InputStream customDictionaryInput,
      InputStream wordRankingInput,
      String dictionaryExclusions, HashMap<String, String> lexiconConversions)
          throws NumberFormatException, ParserConfigurationException,
          SAXException, IOException, DictionaryParsingException,
          WordRankingParsingException {
    this(adverbDeclensionInput, augmentativeDeclensionInput,
        diminutiveDeclensionInput, genderDeclensionInput, genderNounInput,
        numberDeclensionInput, superlativeDeclensionInput,
        irregularVerbConjugationInput, regularVerbLexemeInput,
        regularVerbDeclensionInput, dictionaryInput, customDictionaryInput,
        wordRankingInput, dictionaryExclusions, lexiconConversions,
        Short.MAX_VALUE);
  }

  /**
   * Creates a new <code>Lemmatizer</code> object ...
   * 
   * @param  adverbDeclensionInput ...
   * @param  augmentativeDeclensionInput ...
   * @param  diminutiveDeclensionInput ...
   * @param  genderDeclensionInput ...
   * @param  genderNounInput ...
   * @param  numberDeclensionInput ...
   * @param  superlativeDeclensionInput ...
   * @param  irregularVerbConjugationInput ...
   * @param  regularVerbLexemeInput ...
   * @param  regularVerbDeclensionInput ...
   * @param  dictionaryInput ...
   * @param  customDictionaryInput ...
   * @param  wordRankingInput ...
   * @param  dictionaryExclusions ...
   * @param  lexiconConversions ...
   * @param  cacheSize ...
   * @throws NumberFormatException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @throws DictionaryParsingException
   * @throws WordRankingParsingException
   */
  public Lemmatizer(InputStream adverbDeclensionInput,
      InputStream augmentativeDeclensionInput,
      InputStream diminutiveDeclensionInput,
      InputStream genderDeclensionInput,
      InputStream genderNounInput,
      InputStream numberDeclensionInput,
      InputStream superlativeDeclensionInput,
      InputStream irregularVerbConjugationInput,
      InputStream regularVerbLexemeInput,
      InputStream regularVerbDeclensionInput,
      InputStream dictionaryInput,
      InputStream customDictionaryInput,
      InputStream wordRankingInput,
      String dictionaryExclusions, HashMap<String, String> lexiconConversions,
      int cacheSize)
          throws NumberFormatException, ParserConfigurationException,
          SAXException, IOException, DictionaryParsingException,
          WordRankingParsingException {
    this(adverbDeclensionInput, augmentativeDeclensionInput,
        diminutiveDeclensionInput, genderDeclensionInput, genderNounInput,
        numberDeclensionInput, superlativeDeclensionInput,
        irregularVerbConjugationInput, regularVerbLexemeInput,
        regularVerbDeclensionInput, dictionaryInput, customDictionaryInput,
        wordRankingInput, dictionaryExclusions, lexiconConversions,
        Short.MAX_VALUE, true, true);
  }

  /**
   * Creates a new <code>Lemmatizer</code> object ...
   * 
   * @param  adverbDeclensionInput ...
   * @param  augmentativeDeclensionInput ...
   * @param  diminutiveDeclensionInput ...
   * @param  genderDeclensionInput ...
   * @param  genderNounInput ...
   * @param  numberDeclensionInput ...
   * @param  superlativeDeclensionInput ...
   * @param  irregularVerbConjugationInput ...
   * @param  regularVerbLexemeInput ...
   * @param  regularVerbDeclensionInput ...
   * @param  dictionaryInput ...
   * @param  customDictionaryInput ...
   * @param  wordRankingInput ...
   * @param  dictionaryExclusions ...
   * @param  lexiconConversions ...
   * @param  cacheSize ...
   * @param  breakOnHyphen ...
   * @param  breakOnUnderscore ...
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   * @throws DictionaryParsingException
   * @throws NumberFormatException
   * @throws WordRankingParsingException
   */
  public Lemmatizer(InputStream adverbDeclensionInput,
      InputStream augmentativeDeclensionInput,
      InputStream diminutiveDeclensionInput,
      InputStream genderDeclensionInput,
      InputStream genderNounInput,
      InputStream numberDeclensionInput,
      InputStream superlativeDeclensionInput,
      InputStream irregularVerbConjugationInput,
      InputStream regularVerbLexemeInput,
      InputStream regularVerbDeclensionInput,
      InputStream dictionaryInput,
      InputStream customDictionaryInput,
      InputStream wordRankingInput,
      String dictionaryExclusions, HashMap<String, String> lexiconConversions,
      int cacheSize, boolean breakOnHyphen, boolean breakOnUnderscore)
          throws ParserConfigurationException, SAXException, IOException,
          DictionaryParsingException, NumberFormatException,
          WordRankingParsingException {
    this.initialize(adverbDeclensionInput, augmentativeDeclensionInput,
        diminutiveDeclensionInput, genderDeclensionInput, genderNounInput,
        numberDeclensionInput, superlativeDeclensionInput,
        irregularVerbConjugationInput, regularVerbLexemeInput,
        regularVerbDeclensionInput, dictionaryInput, customDictionaryInput,
        wordRankingInput, dictionaryExclusions, lexiconConversions, cacheSize,
        breakOnHyphen, breakOnUnderscore);
  }

  private void initialize(InputStream adverbDeclensionInput,
      InputStream augmentativeDeclensionInput,
      InputStream diminutiveDeclensionInput,
      InputStream genderDeclensionInput,
      InputStream genderNounInput,
      InputStream numberDeclensionInput,
      InputStream superlativeDeclensionInput,
      InputStream irregularVerbConjugationInput,
      InputStream regularVerbLexemeInput,
      InputStream regularVerbDeclensionInput,
      InputStream dictionaryInput,
      InputStream customDictionaryInput,
      InputStream wordRankingInput,
      String dictionaryExclusions, HashMap<String, String> lexiconConversions,
      int cacheSize, boolean breakOnHyphen, boolean breakOnUnderscore)
          throws ParserConfigurationException, SAXException, IOException,
          DictionaryParsingException, NumberFormatException,
          WordRankingParsingException {
    Replacement[] augmentativeDeclensions = Replacement.readReplacements
        (augmentativeDeclensionInput);
    Replacement[] superlativeDeclensions = Replacement.readReplacements(
        superlativeDeclensionInput);
    Replacement[] diminutiveDeclensions = Replacement.readReplacements(
        diminutiveDeclensionInput);
    Replacement[] genderDeclensionInputs = Replacement.readReplacements(
        genderDeclensionInput);
    Replacement[] genderNouns = Replacement.readReplacements(genderNounInput);
    Replacement[] adverbDeclensions = Replacement.readReplacements(
        adverbDeclensionInput);
    Replacement[] numberDeclensions = Replacement.readReplacements(
        numberDeclensionInput);
    Replacement[] irregularVerbConjugations = Replacement.readReplacements(
        irregularVerbConjugationInput);
    Replacement[] regularVerbLexemes = Replacement.readReplacements(
        regularVerbLexemeInput);
    Replacement[] regularVerbDeclensions = Replacement.readReplacements(
        regularVerbDeclensionInput);

    this.augmentativeNormalizer = new AugmentativeNormalizer(
        augmentativeDeclensions);
    this.superlativeNormalizer = new SuperlativeNormalizer(
        superlativeDeclensions);
    this.diminutiveNormalizer = new DiminutiveNormalizer(diminutiveDeclensions);
    this.genderNormalizer = new GenderNormalizer(genderDeclensionInputs,
        genderNouns);
    this.adverbNormalizer = new AdverbNormalizer(adverbDeclensions);
    this.numberNormalizer = new NumberNormalizer(numberDeclensions);
    this.verbNormalizer = new VerbNormalizer(irregularVerbConjugations,
        regularVerbLexemes, regularVerbDeclensions);

    this.augmentativeTag = this.combineReplacementTags(augmentativeDeclensions);
    this.superlativeTag = this.combineReplacementTags(superlativeDeclensions);
    this.diminutiveTag = this.combineReplacementTags(diminutiveDeclensions);
    this.genderTag = this.combineReplacementTags(genderDeclensionInputs) + "|"
        + this.combineReplacementTags(genderNouns);
    this.numberTag = this.combineReplacementTags(numberDeclensions);
    this.adverbTag = this.combineReplacementTags(adverbDeclensions);
    this.verbTag = this.combineReplacementTags(irregularVerbConjugations) + "|"
        + this.combineReplacementTags(regularVerbLexemes) + "|"
        + this.combineReplacementTags(regularVerbDeclensions);

    this.cache = new LemmatizerCache(cacheSize);
    this.breakOnHyphen = breakOnHyphen;
    this.breakOnUnderscore = breakOnUnderscore;
    // dictionary, lexicon & ranking
    this.dictionary = new Dictionary(dictionaryInput);
    this.dictionary.load(customDictionaryInput);
    this.lexicon = dictionary.retrieveLexicon();
    this.ranking = new WordRanking(wordRankingInput);
    this.dictionaryExclusions = dictionaryExclusions;
    this.lexiconConversions = lexiconConversions;
  }

  /**
   * This method retrieves the lemma of a given token, when classified with
   * a given <em>PoS tag</em>.
   *
   * @param  token the token whose lemma is wanted
   * @param  tag the <em>PoS tag</em> of the token
   * @return the lemma of the token (when classified with the given tag)
   */
  public String lemmatize(String token, String tag) {
    return this.lemmatize(token, tag, ALL);
  }

  /**
   * This method retrieves the lemma of a given token, when classified with
   * a given <em>PoS tag</em>.
   *
   * @param  token the token whose lemma is wanted
   * @param  tag the <em>PoS tag</em> of the token
   * @param  flags the reductions that should be applied
   * @return the lemma of the token (when classified with the given tag)
   */
  public String lemmatize(String token, String tag, int flags) {
    this.setFlags(flags);
    // normalize token/lemma
    String lemma = token.toLowerCase();
    // check for token|tag in cache
    LemmatizerCacheKey key = new LemmatizerCacheKey(lemma, tag.toLowerCase());
    if (cache.containsKey(key)) {
      lemma = cache.get(key);
      return lemma;       
    }
    // check dictionary
    String lexPOSTag = tag.toUpperCase();
    if (lexPOSTag.contains("-")) {
      lexPOSTag = lexPOSTag.substring(0, lexPOSTag.indexOf("-"));
    }
    // address pos tag notation differences between open-nlp and label-lex-sw
    for (String conversionKey : lexiconConversions.keySet()) {
      if (lexPOSTag.equals(conversionKey)) {
        lexPOSTag = lexiconConversions.get(conversionKey);
      }
    }
    if (dictionary.contains(lemma, lexPOSTag)
        && !lexPOSTag.matches(dictionaryExclusions)) {
      String[] lemmas = dictionary.retrieveLemmas(lemma, lexPOSTag);
      return ranking.retrieveTopWord(lemmas);
    }
    // check lexicon
    if (lexicon.contains(lemma, lexPOSTag)) {
      cache.put(key, lemma);
      return lemma;          
    }
    // check for composed tokens
    if (breakOnHyphen && lemma.contains("-")) {
      return this.lemmatize(lemma.substring(0, lemma.indexOf("-")), tag, flags)
          + "-" + this.lemmatize(lemma.substring(lemma.indexOf("-") + 1), tag,
              flags);
    }
    if (breakOnUnderscore && lemma.contains("_")) {
      return this.lemmatize(lemma.substring(0, lemma.indexOf("_")), tag, flags)
          + "_" + this.lemmatize(lemma.substring(lemma.indexOf("_") + 1), tag,
              flags);
    }
    // use rules
    if (this.checkFlag(ADVERB)
        && tag.toLowerCase().matches(adverbTag)) {
      lemma = adverbNormalizer.normalize(lemma, tag);
      if (lexicon.contains(lemma, lexPOSTag)) {
        cache.put(key, lemma);
        return lemma;          
      }
    }
    if (this.checkFlag(NUMBER)
        && tag.toLowerCase().matches(numberTag)) {
      lemma = numberNormalizer.normalize(lemma, tag);
      if (lexicon.contains(lemma, lexPOSTag)) {
        cache.put(key, lemma);
        return lemma;          
      }
    }
    if (this.checkFlag(SUPERLATIVE)
        && tag.toLowerCase().matches(superlativeTag)) {
      lemma = superlativeNormalizer.normalize(lemma, tag);
      if (lexicon.contains(lemma, lexPOSTag)) {
        cache.put(key, lemma);
        return lemma;          
      }
    }
    if (this.checkFlag(AUGMENTATIVE) &&
        tag.toLowerCase().matches(augmentativeTag)) {
      lemma = augmentativeNormalizer.normalize(lemma, tag);
      if (lexicon.contains(lemma, lexPOSTag)) {
        cache.put(key, lemma);
        return lemma;          
      }
    }
    if (this.checkFlag(DIMINUTIVE)
        && tag.toLowerCase().matches(diminutiveTag)) {
      lemma = diminutiveNormalizer.normalize(lemma, tag);
      if (lexicon.contains(lemma, lexPOSTag)) {
        cache.put(key, lemma);
        return lemma;          
      }
    }
    if (this.checkFlag(GENDER_ALL)
        && tag.toLowerCase().matches(genderTag)) {
      lemma = genderNormalizer.normalize(lemma, tag);
      if (lexicon.contains(lemma, lexPOSTag)) {
        cache.put(key, lemma);
        return lemma;          
      }
    }
    else if (this.checkFlag(GENDER_DECLENSIONS)
        && tag.toLowerCase().matches(genderTag)) {
      lemma = genderNormalizer.normalize(lemma, tag,
          GenderNormalizer.DECLENSIONS);
      if (lexicon.contains(lemma, lexPOSTag)) {
        cache.put(key, lemma);
        return lemma;          
      }
    }
    else if (this.checkFlag(GENDER_NOUNS)
        && tag.toLowerCase().matches(genderTag)) {
      lemma = genderNormalizer.normalize(lemma, tag,
          GenderNormalizer.NOUNS);
      if (lexicon.contains(lemma, lexPOSTag)) {
        cache.put(key, lemma);
        return lemma;          
      }
    }
    if (this.checkFlag(VERB)
        && tag.toLowerCase().matches(verbTag)) {
      lemma = verbNormalizer.normalize(lemma, tag);
      if (lexicon.contains(lemma, lexPOSTag)) {
        cache.put(key, lemma);
        return lemma;          
      }
    }
    return lemma;
  }

  /**
   * This method ...
   *
   * @param  tokens ...
   * @param  tags ...
   * @return ...
   */
  public String[] lemmatize(String tokens[], String tags[]) {
    String[] lemmas = new String[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      lemmas[i] = this.lemmatize(tokens[i], tags[i]);
    }
    return lemmas;
  }

  /**
   * This method ...
   *
   * @param  tokens ...
   * @param  tags ...
   * @param  flags ...
   * @return ...
   */
  public String[] lemmatize(String tokens[], String tags[], int flags) {
    String[] lemmas = new String[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      lemmas[i] = this.lemmatize(tokens[i], tags[i], flags);
    }
    return lemmas;
  }

  /**
   * This method ...
   * 
   * @return ...
   */
  public int getCacheSize() {
    return cacheSize;
  }

  /**
   * This method ...
   * 
   * @param  cacheSize ...
   */
  public void setCacheSize(int cacheSize) {
    this.cacheSize = cacheSize;
  }

  private String combineReplacementTags(Replacement[] replacements) {
    String combinedTags = new String();
    for (Replacement replacement : replacements) {
      if (combinedTags.length() > 0) {
        if (!("|" + combinedTags + "|").contains(
            "|" + replacement.getTag() + "|")) {
          combinedTags += "|" + replacement.getTag();
        }
      }
      else {
        combinedTags = replacement.getTag();
      }
    }
    return combinedTags;
  }

  private void setFlags(int flags) {
    // bitmasks
    this.flags = this.flags | flags;
  }

  private boolean checkFlag(int flag) {
    // bitmasks
    return (flags & flag) == flag;
  }
}
