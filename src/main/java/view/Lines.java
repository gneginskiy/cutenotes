package view;

/** Line-boundary math over a plain string, replacing {@code JTextArea}'s line API for JTextPane. */
final class Lines {

  private Lines() {}

  static int start(String text, int caret) {
    return text.lastIndexOf('\n', caret - 1) + 1;
  }

  /** End of the line containing {@code caret}, including its trailing newline (or text end). */
  static int endInclusive(String text, int caret) {
    int nl = text.indexOf('\n', caret);
    return nl < 0 ? text.length() : nl + 1;
  }

  static int index(String text, int caret) {
    int count = 0;
    for (int i = 0; i < caret && i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        count++;
      }
    }
    return count;
  }

  static int count(String text) {
    int lines = 1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        lines++;
      }
    }
    return lines;
  }

  static int startOffset(String text, int lineIndex) {
    int pos = 0;
    for (int k = 0; k < lineIndex; k++) {
      pos = text.indexOf('\n', pos) + 1;
    }
    return pos;
  }
}
