package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.PatternSyntaxException;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultHighlighter;

import org.junit.jupiter.api.Test;

class SearchReplaceTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  private static final SearchOptions WORD = new SearchOptions(false, true, false);
  private static final SearchOptions REGEX = new SearchOptions(false, false, true);

  @Test
  void wholeWordsAndRegularExpressions() {
    assertEquals(1, SearchEngine.findAll("cat catalog cat.", "cat", WORD).size() - 1);
    assertEquals(2, SearchEngine.findAll("кот котик кот", "кот", WORD).size());
    assertEquals(2, SearchEngine.findAll("a1 b22 c", "\\d+", REGEX).size());
    assertEquals(0, SearchEngine.findAll("abc", "x*", REGEX).size(), "empty matches skipped");
    assertThrows(PatternSyntaxException.class, () -> SearchEngine.findAll("x", "(", REGEX));
  }

  @Test
  void regexReplacementsExpandGroups() {
    String text = "2026-09-26";
    int[] m = SearchEngine.findAll(text, "(\\d+)-(\\d+)-(\\d+)", REGEX).get(0);

    assertEquals(
        "26.09.2026",
        SearchEngine.replacementFor(text, m, "(\\d+)-(\\d+)-(\\d+)", REGEX, "$3.$2.$1"));
    assertEquals("$9", SearchEngine.replacementFor(text, m, "(\\d+)", REGEX, "$9"));
    assertEquals("$1", SearchEngine.replacementFor(text, m, "x", SearchOptions.PLAIN, "$1"));
  }

  @Test
  void replaceCurrentThenAllKeepingFormattingInOneUndoStepEach() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor e = new NoteEditor("**cat** and cat and cat");
          FindModel model = new FindModel(() -> e);
          model.setPainters(
              new DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.YELLOW),
              new DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.ORANGE));

          model.run("cat", SearchOptions.PLAIN, 0);
          assertEquals(3, model.count());
          assertTrue(model.replaceCurrent("dog"));
          assertEquals("**dog** and cat and cat", e.markdown());
          assertEquals(2, model.count());

          assertEquals(2, model.replaceAll("cow"));
          assertEquals("**dog** and cow and cow", e.markdown());
          e.undoHistory().undo();
          assertEquals("**dog** and cat and cat", e.markdown());
          model.clear();
          assertEquals(0, Highlights.count(e, SearchEngine.LAYER));
        });
  }

  @Test
  void invalidPatternIsReportedNotThrown() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor e = new NoteEditor("text");
          FindModel model = new FindModel(() -> e);
          model.run("(", REGEX, 0);
          assertTrue(model.invalid());
          assertEquals(0, model.count());
        });
  }

  @Test
  void clearingSearchKeepsLinkDecorations() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor e = new NoteEditor("see https://example.com and #tag");
          assertEquals(2, Highlights.count(e, EditorDecorations.LAYER));

          SearchEngine.highlight(
              e,
              List.of(new int[] {0, 3}),
              0,
              new DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.YELLOW),
              new DefaultHighlighter.DefaultHighlightPainter(java.awt.Color.ORANGE));
          SearchEngine.clear(e);

          assertEquals(2, Highlights.count(e, EditorDecorations.LAYER));
          assertEquals("https://example.com", EditorDecorations.linkAt(e, 6));
        });
  }
}
