package examples;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import rank.WordRankingParsingException;
import dictionary.DictionaryParsingException;
import lemma.Lemmatizer;

public class LemmatizerExample {

  public static void main(String[] args) {
    System.out.println("Starting...");
    Lemmatizer lemmatizer = null;
    String[] tokens = {"Era", "uma", "vez", "um", "gato", "maltês", ",",
        "tocava", "piano", "e", "falava", "francês", "."};
    String[] tags = {"v-fin", "art", "n", "art", "n", "adj", "punc", "v-inf",
        "n", "conj-c", "v-fin", "n", "punc"};

    try {
      lemmatizer = new Lemmatizer();
    }
    catch (NumberFormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (InvalidPropertiesFormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (ParserConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (DictionaryParsingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (WordRankingParsingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String[] lemmas = lemmatizer.lemmatize(tokens, tags);

    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < tokens.length; i++) {
      buffer.append(tokens[i] + "#" + tags[i] + ":" + lemmas[i] + " ");
    }

    System.out.println(buffer.toString().trim());

    System.out.println("Done!");
  }
}
