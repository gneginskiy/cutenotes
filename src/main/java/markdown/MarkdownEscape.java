package markdown;

/**
 * Backslash escaping for the inline Markdown dialect. Literal marker characters typed by the user
 * ({@code *}, {@code __}, {@code ~~}, {@code ```}) are written as {@code \*}, {@code \_} etc., so
 * they never turn into formatting on reload. A backslash is special only in front of a marker
 * character (or at the end of a run), so plain text such as {@code C:\Users} is stored as is.
 */
final class MarkdownEscape {

  private static final String SPECIAL = "*_~`";
  private static final char BACKSLASH = '\\';

  private MarkdownEscape() {}

  static String escape(String text) {
    StringBuilder out = new StringBuilder(text.length() + 8);
    int i = 0;
    while (i < text.length()) {
      if (text.charAt(i) == BACKSLASH) {
        i = escapeBackslashes(text, i, out);
      } else {
        if (needsEscape(text, i)) {
          out.append(BACKSLASH);
        }
        out.append(text.charAt(i));
        i++;
      }
    }
    return out.toString();
  }

  /**
   * Consumes the run of backslashes starting at {@code i}, appending the literal text it stands
   * for. Returns the index parsing continues from: a marker preceded by an even number of
   * backslashes is still a marker, an odd number turns it into a literal.
   */
  static int unescape(String md, int i, StringBuilder text) {
    int end = runEnd(md, i);
    int count = end - i;
    if (end < md.length() && !isSpecial(md.charAt(end))) {
      text.append(String.valueOf(BACKSLASH).repeat(count));
      return end;
    }
    text.append(String.valueOf(BACKSLASH).repeat(count / 2));
    if (count % 2 == 0) {
      return end;
    }
    if (end < md.length()) {
      text.append(md.charAt(end));
      return end + 1;
    }
    text.append(BACKSLASH);
    return end;
  }

  private static int escapeBackslashes(String text, int start, StringBuilder out) {
    int end = runEnd(text, start);
    int count = end - start;
    boolean beforeSpecial = end == text.length() || isSpecial(text.charAt(end));
    out.append(String.valueOf(BACKSLASH).repeat(beforeSpecial ? count * 2 : count));
    return end;
  }

  /**
   * {@code *} always toggles italic, so it is always escaped. The other markers only act when
   * doubled ({@code __}, {@code ~~}, {@code ```}); escaping a char followed by the same char, or
   * sitting at the end of the run (where a closing/next marker follows), is enough to neutralise
   * them while keeping the stored text readable.
   */
  private static boolean needsEscape(String text, int i) {
    char c = text.charAt(i);
    if (c == '*') {
      return true;
    }
    return isSpecial(c) && (i + 1 == text.length() || text.charAt(i + 1) == c);
  }

  private static int runEnd(String s, int start) {
    int end = start;
    while (end < s.length() && s.charAt(end) == BACKSLASH) {
      end++;
    }
    return end;
  }

  private static boolean isSpecial(char c) {
    return SPECIAL.indexOf(c) >= 0;
  }
}
