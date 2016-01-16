package dictionary;

/**
 * This class ...
 *
 * @author   Ricardo Rodrigues
 * @version  0.9.4
 */
public class DictionaryLoadException extends Exception{
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new ...
   * 
   */
  public DictionaryLoadException() {
    super();
  }

  /**
   * Creates a new ...
   * 
   * @param  message ...
   */
  public DictionaryLoadException(String message) {
    super(message);
  }
}
