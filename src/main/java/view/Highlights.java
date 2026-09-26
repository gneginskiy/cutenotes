package view;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * Named layers of highlights on a text component. Clearing one layer (search matches) leaves the
 * others (links, tags, the selection) alone; {@code removeAllHighlights} used to wipe them all.
 */
final class Highlights {

  /** A highlighted range and how to paint it. */
  record Mark(int start, int end, Highlighter.HighlightPainter painter) {}

  private Highlights() {}

  /** Replaces the highlights of {@code layer} with {@code marks}. */
  static void set(JTextComponent area, String layer, List<Mark> marks) {
    clear(area, layer);
    List<Object> tags = new ArrayList<>();
    Highlighter h = area.getHighlighter();
    int length = area.getDocument().getLength();
    for (Mark m : marks) {
      if (m.start() < m.end() && m.end() <= length) {
        try {
          tags.add(h.addHighlight(m.start(), m.end(), m.painter()));
        } catch (BadLocationException e) {
          // the text changed meanwhile: skip this mark
        }
      }
    }
    area.putClientProperty(key(layer), tags);
  }

  static void clear(JTextComponent area, String layer) {
    if (area.getClientProperty(key(layer)) instanceof List<?> tags) {
      for (Object tag : tags) {
        area.getHighlighter().removeHighlight(tag);
      }
    }
    area.putClientProperty(key(layer), null);
  }

  static int count(JTextComponent area, String layer) {
    return area.getClientProperty(key(layer)) instanceof List<?> tags ? tags.size() : 0;
  }

  private static String key(String layer) {
    return "cutenotes.highlights." + layer;
  }
}
