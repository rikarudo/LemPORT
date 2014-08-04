package normalization;

import java.util.Arrays;
import java.util.regex.Pattern;

import replacement.Replacement;

/**
 * This class ...
 *
 * @author   Ricardo Rodrigues
 * @version  0.9.1
 */
public class GenderNormalizer extends Normalizer {
  /**
   * This field...
   */
  public static final int DECLENSIONS = 1;  // Binary 01

  /**
   * This field...
   */
  public static final int NOUNS = 2;        // Binary 10

  /**
   * This field...
   */
  public static final int ALL = 3;          // Binary 11

  private Pattern[] declensionExceptions = null;
  private Pattern[] declensionTargets = null;
  private Pattern[] declensionTags = null;
  private Pattern[] nounTargets = null;
  private Pattern[] nounTags = null;
  private Replacement[] declensions = null;
  private Replacement[] nouns = null;
  private int flags = 0;                    // Binary 00

  /**
   * Creates a new <code>GenderNormalizer</code> object ...
   * 
   * @param  declensions ...
   * @param  nouns ...
   */
  public GenderNormalizer(Replacement[] declensions, Replacement[] nouns) {
    this.declensions = declensions;
    this.nouns = nouns;
    Arrays.sort(this.declensions);
    Arrays.sort(this.nouns);
    declensionExceptions = new Pattern[this.declensions.length];
    declensionTargets = new Pattern[this.declensions.length];
    declensionTags = new Pattern[this.declensions.length];
    for (int i = 0; i < declensions.length; i++) {
      declensionExceptions[i] = Pattern.compile(declensions[i].getExceptions());
      declensionTargets[i] = Pattern.compile(declensions[i].getPrefix()
          + declensions[i].getTarget() + declensions[i].getSuffix());
      declensionTags[i] = Pattern.compile(declensions[i].getTag());
    }
    nounTargets = new Pattern[this.nouns.length];
    nounTags = new Pattern[this.nouns.length];
    for (int i = 0; i < nouns.length; i++) {
      nounTargets[i] = Pattern.compile(nouns[i].getPrefix() +
          nouns[i].getTarget() + nouns[i].getSuffix());
      nounTags[i] = Pattern.compile(nouns[i].getTag());
    }
  }

  /**
   * This method retrieves the masculine form of a given token, if it exists,
   * when classified with a given <em>PoS tag</em>. Otherwise, it retrieves
   * the same token (in lower case).
   *
   * @param  token the token whose lemma is wanted
   * @param  tag the <em>PoS tag</em> of the token
   * @return the masculine form of the token (when with the given tag)
   */
  public String normalize(String token, String tag) {
    return this.normalize(token, tag, ALL);
  }

  /**
   * This method retrieves the masculine form of a given token, if it exists,
   * when classified with a given <em>PoS tag</em>. Otherwise, it retrieves
   * the same token (in lower case).
   *
   * @param  token the token whose lemma is wanted
   * @param  tag the <em>PoS tag</em> of the token
   * @param  flags the reductions that should be applied
   * @return the masculine form of the token (when with the given tag)
   */
  public String normalize(String token, String tag, int flags) {
    this.setFlags(flags);
    String normalization = token.toLowerCase();
    boolean matchFound = false;
    // using gender specific nouns
    if (this.checkFlag(NOUNS) && !matchFound) {
      for (int i = 0; i < nouns.length; i++) {
        if (nounTargets[i].matcher(normalization).matches()
            && nounTags[i].matcher(tag.toLowerCase()).matches()) {
          normalization = nouns[i].getReplacement();
          matchFound = true;
          break;
        }
      }
    }
    // using gender declensions
    if (this.checkFlag(DECLENSIONS) && !matchFound) {
      for (int i = 0; i < declensions.length; i++) {
        if (declensionTargets[i].matcher(normalization).matches()
            && declensionTags[i].matcher(tag.toLowerCase()).matches()
            && !declensionExceptions[i].matcher(normalization).matches()) {
          normalization = normalization.substring(0,
              normalization.length() - declensions[i].getTarget().length())
              + declensions[i].getReplacement();
          matchFound = true;
          break;
        }
      }
    }
    return normalization;
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
