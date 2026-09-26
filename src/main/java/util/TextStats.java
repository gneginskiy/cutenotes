package util;

/**
 * Word and character counts of a text.
 *
 * @param words runs of non-whitespace characters
 * @param characters characters without line breaks
 */
public record TextStats(int words, int characters) {

  public static TextStats of(String text) {
    int words = 0;
    int characters = 0;
    boolean inWord = false;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c != '\n' && c != '\r') {
        characters++;
      }
      boolean space = Character.isWhitespace(c) || Character.isSpaceChar(c) || c == '￼';
      if (!space && !inWord) {
        words++;
      }
      inWord = !space;
    }
    return new TextStats(words, characters);
  }
}
