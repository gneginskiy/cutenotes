package view;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

final class SearchEngine {

  private SearchEngine() {}

  static List<int[]> findAll(String text, String query) {
    return findAll(text, query, false);
  }

  static List<int[]> findAll(String text, String query, boolean caseSensitive) {
    List<int[]> result = new ArrayList<>();
    if (query.isEmpty() || text.isEmpty()) {
      return result;
    }
    String haystack = caseSensitive ? text : text.toLowerCase(Locale.ROOT);
    String needle = caseSensitive ? query : query.toLowerCase(Locale.ROOT);
    int from = 0;
    while (true) {
      int idx = haystack.indexOf(needle, from);
      if (idx < 0) {
        break;
      }
      result.add(new int[] {idx, idx + query.length()});
      from = idx + 1;
    }
    return result;
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
    Highlighter h = area.getHighlighter();
    h.removeAllHighlights();
    for (int i = 0; i < matches.size(); i++) {
      int[] m = matches.get(i);
      try {
        h.addHighlight(m[0], m[1], i == activeIdx ? active : normal);
      } catch (BadLocationException ignored) {
        // skip
      }
    }
  }

  static void clear(JTextComponent area) {
    if (area != null) {
      area.getHighlighter().removeAllHighlights();
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
