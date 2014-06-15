package dictionary;

/**
 * This class ...
 *
 * @author   Ricardo Rodrigues
 * @version  0.9
 */
public class DictionaryParsingException extends Exception{
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new ...
   * 
   */
  public DictionaryParsingException() {
    super();
  }

  /**
   * Creates a new ...
   * 
   * @param  message ...
   */
  public DictionaryParsingException(String message) {
    super(message);
  }
}
