package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;

import org.junit.jupiter.api.Test;

import model.Theme;

class NoteEditorTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  @Test
  void markdownIsCachedUntilTheDocumentChanges() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor editor = new NoteEditor("**hi** there");
          String first = editor.markdown();

          assertSame(first, editor.markdown(), "unchanged document must not be re-serialised");

          insert(editor, editor.getDocument().getLength(), "!");
          assertEquals("**hi** there!", editor.markdown());
        });
  }

  @Test
  void attributeOnlyChangeInvalidatesTheCache() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor editor = new NoteEditor("word");
          editor.markdown();
          javax.swing.text.SimpleAttributeSet bold = new javax.swing.text.SimpleAttributeSet();
          javax.swing.text.StyleConstants.setBold(bold, true);

          editor.getStyledDocument().setCharacterAttributes(0, 4, bold, false);

          assertEquals("**word**", editor.markdown());
        });
  }

  @Test
  void serialisesStyledRunsAndImagesInOrder() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor editor = new NoteEditor("a **b** *c*");
          StyledDocument doc = editor.getStyledDocument();
          BufferedImage img = new BufferedImage(4, 2, BufferedImage.TYPE_INT_ARGB);
          insert(editor, 1, " ", ImageAttr.of("images/x.png", img, 40, 20));

          assertEquals("a![|40x20](images/x.png) **b** *c*", DocumentMarkdown.toMarkdown(doc));
        });
  }

  @Test
  void zoomDoesNotAddUndoableEditsForCodeSections() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor editor = new NoteEditor("```code``` and text");
          AtomicInteger edits = new AtomicInteger();
          editor.getDocument().addUndoableEditListener(e -> edits.incrementAndGet());
          Theme t = ThemeHolder.current();

          editor.applyTheme(withSize(t, t.fontSize() + 10));

          assertEquals(0, edits.get());
        });
  }

  @Test
  void colourChangeRestylesCodeInOneEditPerRun() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor editor = new NoteEditor("```some code here```");
          AtomicInteger edits = new AtomicInteger();
          editor.getDocument().addUndoableEditListener(e -> edits.incrementAndGet());
          Theme t = ThemeHolder.current();

          editor.applyTheme(withCode(t, Color.MAGENTA));

          assertEquals(1, edits.get());
          Object fg =
              editor
                  .getStyledDocument()
                  .getCharacterElement(2)
                  .getAttributes()
                  .getAttribute(javax.swing.text.StyleConstants.Foreground);
          assertEquals(Color.MAGENTA, fg);
        });
  }

  @Test
  void groupedGestureIsUndoneInOneStepAndKeepsEarlierHistory() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor editor = new NoteEditor("");
          insert(editor, 0, "typed");
          editor.undoHistory().beginGroup();
          for (int i = 0; i < 300; i++) {
            insert(editor, editor.getDocument().getLength(), ".");
          }
          editor.undoHistory().endGroup();

          editor.undoHistory().undo();
          assertEquals("typed", editor.markdown());
          editor.undoHistory().undo();
          assertEquals("", editor.markdown());
        });
  }

  private static Theme withSize(Theme t, int size) {
    return new Theme(
        t.bg(), t.fg(), t.caret(), t.codeColor(), t.fontFamily(), size, t.title(), t.alwaysOnTop());
  }

  private static Theme withCode(Theme t, Color code) {
    return new Theme(
        t.bg(), t.fg(), t.caret(), code, t.fontFamily(), t.fontSize(), t.title(), t.alwaysOnTop());
  }

  private static void insert(NoteEditor editor, int at, String text) {
    insert(editor, at, text, null);
  }

  private static void insert(
      NoteEditor editor, int at, String text, javax.swing.text.AttributeSet attrs) {
    try {
      editor.getStyledDocument().insertString(at, text, attrs);
    } catch (javax.swing.text.BadLocationException e) {
      throw new IllegalStateException(e);
    }
  }
}
