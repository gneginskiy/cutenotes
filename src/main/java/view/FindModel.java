package view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.StyledDocument;

/** The matches of the find bar in the active note: search, step, highlight and replace. */
final class FindModel {

  private final Supplier<NoteEditor> editor;
  private final List<int[]> matches = new ArrayList<>();
  private Highlighter.HighlightPainter matchPainter;
  private Highlighter.HighlightPainter activePainter;
  private String query = "";
  private SearchOptions options = SearchOptions.PLAIN;
  private int active = -1;
  private boolean invalid;

  FindModel(Supplier<NoteEditor> editor) {
    this.editor = editor;
  }

  void setPainters(Highlighter.HighlightPainter match, Highlighter.HighlightPainter current) {
    this.matchPainter = match;
    this.activePainter = current;
  }

  /** Searches anew; the first match at or after {@code from} becomes the current one. */
  void run(String newQuery, SearchOptions newOptions, int from) {
    query = newQuery;
    options = newOptions;
    matches.clear();
    invalid = false;
    NoteEditor area = editor.get();
    try {
      matches.addAll(SearchEngine.findAll(area == null ? "" : area.getText(), query, options));
    } catch (PatternSyntaxException e) {
      invalid = true;
    }
    active = firstFrom(from);
    paint();
  }

  void step(int direction) {
    if (!matches.isEmpty()) {
      active = SearchEngine.step(active, matches.size(), direction);
      paint();
    }
  }

  /** Replaces the current match and moves on to the next one; returns whether one was replaced. */
  boolean replaceCurrent(String replacement) {
    NoteEditor area = editor.get();
    if (area == null || active < 0) {
      return false;
    }
    int[] m = matches.get(active);
    String text = area.getText();
    String value = SearchEngine.replacementFor(text, m, query, options, replacement);
    area.editAsOneStep(doc -> replace(doc, m, value));
    run(query, options, m[0] + value.length());
    return true;
  }

  /** Replaces every match as one undo step; returns how many. */
  int replaceAll(String replacement) {
    NoteEditor area = editor.get();
    if (area == null || matches.isEmpty()) {
      return 0;
    }
    String text = area.getText();
    List<int[]> all = new ArrayList<>(matches);
    area.editAsOneStep(
        doc -> {
          for (int i = all.size() - 1; i >= 0; i--) {
            int[] m = all.get(i);
            replace(doc, m, SearchEngine.replacementFor(text, m, query, options, replacement));
          }
        });
    run(query, options, 0);
    return all.size();
  }

  void clear() {
    SearchEngine.clear(editor.get());
    matches.clear();
    active = -1;
  }

  int count() {
    return matches.size();
  }

  int activeIndex() {
    return active;
  }

  boolean invalid() {
    return invalid;
  }

  private void replace(StyledDocument doc, int[] m, String value) throws BadLocationException {
    AttributeSet attrs = doc.getCharacterElement(m[0]).getAttributes().copyAttributes();
    doc.remove(m[0], m[1] - m[0]);
    doc.insertString(m[0], value, attrs);
  }

  private int firstFrom(int from) {
    for (int i = 0; i < matches.size(); i++) {
      if (matches.get(i)[0] >= from) {
        return i;
      }
    }
    return matches.isEmpty() ? -1 : 0;
  }

  private void paint() {
    NoteEditor area = editor.get();
    SearchEngine.highlight(area, matches, active, matchPainter, activePainter);
    if (active >= 0) {
      SearchEngine.scrollTo(area, matches.get(active)[0]);
    }
  }
}
