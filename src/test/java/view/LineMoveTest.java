package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LineMoveTest {

  @Test
  void movingUpSwapsWithTheLineAboveAndKeepsTheCaretColumn() {
    String text = "one\ntwo\nthree";
    int caret = text.indexOf("wo");

    assertEquals("two\none\nthree", apply(text, caret, caret, -1));
    assertEquals(1, LineMove.plan(text, caret, caret, -1).moved(caret), "still after 't'");
  }

  @Test
  void movingDownSwapsWithTheLineBelow() {
    String text = "one\ntwo\nthree";
    int caret = 1;

    LineMove move = LineMove.plan(text, caret, caret, 1);
    assertEquals("two\none\nthree", apply(text, caret, caret, 1));
    assertEquals(5, move.moved(caret));
  }

  @Test
  void theLastLineWithoutANewlineMovesBothWays() {
    assertEquals("one\nthree\ntwo", apply("one\ntwo\nthree", 10, 10, -1));
    assertEquals("one\nthree\ntwo", apply("one\ntwo\nthree", 5, 5, 1));
  }

  @Test
  void nothingHappensAtTheDocumentEdges() {
    assertNull(LineMove.plan("one\ntwo", 1, 1, -1));
    assertNull(LineMove.plan("one\ntwo", 5, 5, 1));
    assertNull(LineMove.plan("single", 2, 2, 1));
  }

  @Test
  void aMultiLineSelectionMovesAsOneBlockAndStaysSelected() {
    String text = "a\nb\nc\nd";
    int from = text.indexOf('b');
    int to = text.indexOf('c') + 1;

    LineMove move = LineMove.plan(text, from, to, 1);
    assertEquals("a\nd\nb\nc", apply(text, from, to, 1));
    assertEquals("b\nc", apply(text, from, to, 1).substring(move.moved(from), move.moved(to)));
  }

  @Test
  void aSelectionEndingAtALineStartDoesNotDragThatLine() {
    String text = "a\nb\nc\nd";
    int from = text.indexOf('b');
    int to = text.indexOf('c');

    assertEquals("b\na\nc\nd", apply(text, from, to, -1), "like IntelliJ: 'c' stays");
  }

  @Test
  void theEmptyLineAfterATrailingNewlineCountsAsALine() {
    assertEquals("a\n\nb", apply("a\nb\n", 2, 2, 1));
    assertEquals("a\n\nb", apply("a\nb\n", 4, 4, -1));
  }

  @Test
  void emptyLinesMoveToo() {
    assertEquals("\nx\ny", apply("x\n\ny", 2, 2, -1));
  }

  /**
   * Applies the plan the way the editor does: insert the neighbour copy, then drop the original.
   */
  private static String apply(String text, int from, int to, int direction) {
    LineMove m = LineMove.plan(text, from, to, direction);
    StringBuilder doc = new StringBuilder(text);
    doc.insert(m.insertAt(), m.insertText(text));
    doc.delete(m.removeFrom(), m.removeTo());
    return doc.toString();
  }
}
