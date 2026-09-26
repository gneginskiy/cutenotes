package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import javax.swing.text.View;

import org.junit.jupiter.api.Test;

import markdown.LineKind;

class EditorRenderingTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  @Test
  void headingsAndDoneTasksAreStyledByTheViewNotTheDocument() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor e = new NoteEditor("# Title\n- [x] done\nplain");
          AtomicInteger edits = new AtomicInteger();
          e.getDocument().addUndoableEditListener(ev -> edits.incrementAndGet());
          e.setSize(400, 400);

          NoteLabelView heading = labelAt(e, 2);
          NoteLabelView done = labelAt(e, 10);
          NoteLabelView plain = labelAt(e, 20);

          assertEquals(LineKind.H1, heading.kind());
          assertTrue(heading.getFont().isBold());
          assertTrue(heading.getFont().getSize2D() > plain.getFont().getSize2D());
          assertTrue(done.isStrikeThrough());
          assertTrue(!plain.isStrikeThrough() && plain.getFont().getStyle() == Font.PLAIN);
          assertEquals("# Title\n- \\[x\\] done\nplain".replace("\\", ""), e.getText());
          assertEquals(0, edits.get());
        });
  }

  @Test
  void typingAHeadingMarkerGrowsTheLine() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor e = new NoteEditor("Title");
          e.setSize(400, 400);
          double before = height(e);
          try {
            e.getDocument().insertString(0, "# ", null);
          } catch (javax.swing.text.BadLocationException ex) {
            throw new IllegalStateException(ex);
          }
          e.setSize(400, 401);
          assertTrue(height(e) > before, "heading line is taller");
        });
  }

  private static double height(NoteEditor e) {
    try {
      return e.modelToView2D(e.getDocument().getLength() - 1).getHeight();
    } catch (javax.swing.text.BadLocationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static NoteLabelView labelAt(NoteEditor e, int offset) {
    View root = e.getUI().getRootView(e);
    root.setSize(400, 1000);
    NoteLabelView found = find(root, offset);
    if (found == null) {
      throw new AssertionError("no text view at " + offset);
    }
    return found;
  }

  private static NoteLabelView find(View v, int offset) {
    if (v instanceof NoteLabelView label
        && offset >= v.getStartOffset()
        && offset < v.getEndOffset()) {
      return label;
    }
    for (int i = 0; i < v.getViewCount(); i++) {
      NoteLabelView found = find(v.getView(i), offset);
      if (found != null) {
        return found;
      }
    }
    return null;
  }
}
