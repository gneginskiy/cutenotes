package view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

final class SearchEngine {

  static final String LAYER = "search";

  private SearchEngine() {}

  static List<int[]> findAll(String text, String query) {
    return findAll(text, query, false);
  }

  static List<int[]> findAll(String text, String query, boolean caseSensitive) {
    return findAll(text, query, new SearchOptions(caseSensitive, false, false));
  }

  /**
   * Non-overlapping matches of {@code query} in {@code text}; empty matches are skipped. Throws
   * {@link java.util.regex.PatternSyntaxException} for an invalid regular expression.
   */
  static List<int[]> findAll(String text, String query, SearchOptions options) {
    List<int[]> result = new ArrayList<>();
    if (query.isEmpty() || text.isEmpty()) {
      return result;
    }
    Matcher m = pattern(query, options).matcher(text);
    while (m.find()) {
      if (m.end() > m.start()) {
        result.add(new int[] {m.start(), m.end()});
      }
    }
    return result;
  }

  static Pattern pattern(String query, SearchOptions options) {
    String core = options.regex() ? query : Pattern.quote(query);
    if (options.wholeWord()) {
      core = "(?<![\\p{L}\\p{N}_])(?:" + core + ")(?![\\p{L}\\p{N}_])";
    }
    int flags = options.caseSensitive() ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    return Pattern.compile(core, flags);
  }

  /**
   * The text that replaces {@code match}: {@code replacement} literally, or with {@code $1}-style
   * groups expanded in regular-expression mode (a malformed reference is taken literally).
   */
  static String replacementFor(
      String text, int[] match, String query, SearchOptions options, String replacement) {
    if (!options.regex()) {
      return replacement;
    }
    try {
      Matcher m = pattern(query, options).matcher(text);
      if (!m.find(match[0]) || m.start() != match[0]) {
        return replacement;
      }
      StringBuilder out = new StringBuilder();
      m.appendReplacement(out, replacement);
      return out.substring(match[0]);
    } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
      return replacement;
    }
  }

  static int step(int current, int total, int direction) {
    if (total == 0) {
      return -1;
    }
    return (current + direction + total) % total;
  }

  static void highlight(
      JTextComponent area,
      List<int[]> matches,
      int activeIdx,
      Highlighter.HighlightPainter normal,
      Highlighter.HighlightPainter active) {
    if (area == null) {
      return;
    }
    List<Highlights.Mark> marks = new ArrayList<>();
    for (int i = 0; i < matches.size(); i++) {
      int[] m = matches.get(i);
      marks.add(new Highlights.Mark(m[0], m[1], i == activeIdx ? active : normal));
    }
    Highlights.set(area, LAYER, marks);
  }

  static void clear(JTextComponent area) {
    if (area != null) {
      Highlights.clear(area, LAYER);
    }
  }

  static void scrollTo(JTextComponent area, int offset) {
    if (area == null) {
      return;
    }
    try {
      var rect = area.modelToView2D(offset);
      if (rect != null) {
        area.scrollRectToVisible(rect.getBounds());
      }
    } catch (BadLocationException ignored) {
      // skip
    }
  }
}
