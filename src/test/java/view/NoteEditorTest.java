package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @Test
  void editorTypographyFollowsTheThemeWithoutTouchingContentOrHistory() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor editor = new NoteEditor("line one\nline **two**");
          AtomicInteger edits = new AtomicInteger();
          editor.getDocument().addUndoableEditListener(e -> edits.incrementAndGet());
          Theme t = ThemeHolder.current();

          editor.applyTheme(t);

          javax.swing.text.Style base =
              editor.getStyledDocument().getStyle(javax.swing.text.StyleContext.DEFAULT_STYLE);
          assertEquals(
              NoteEditorKit.LINE_SPACING, javax.swing.text.StyleConstants.getLineSpacing(base));
          assertEquals(UiPalette.of(t).selection(), editor.getSelectionColor());
          assertEquals(EditorTheme.CARET_WIDTH, editor.getClientProperty("caretWidth"));
          assertEquals(0, edits.get(), "typography is not an undoable edit");
          assertEquals("line one\nline **two**", editor.markdown(), "nor part of the note");
        });
  }

  @Test
  void themeFontThatIsNotInstalledFallsBackToAnInstalledUiFont() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor editor = new NoteEditor("x");
          Theme t = ThemeHolder.current();

          editor.applyTheme(
              new Theme(t.bg(), t.fg(), null, t.codeColor(), "No Such Font", 15, null, true));

          assertTrue(FontFamilies.installed().contains(editor.getFont().getFamily()));
        });
  }

  @Test
  void codeSectionsUseAnInstalledMonospaceAndPlainTextAnInstalledFont() {
    Theme t = ThemeHolder.current();
    Theme missingFont =
        new Theme(t.bg(), t.fg(), null, t.codeColor(), "No Such Font", 15, null, true);
    javax.swing.text.SimpleAttributeSet on = new javax.swing.text.SimpleAttributeSet();
    javax.swing.text.SimpleAttributeSet off = new javax.swing.text.SimpleAttributeSet();

    EditorFormat.styleCode(on, true, missingFont);
    EditorFormat.styleCode(off, false, missingFont);

    java.util.List<String> installed = FontFamilies.installed();
    assertEquals(
        model.ThemePreset.firstInstalled(model.ThemePresets.MONO, installed),
        javax.swing.text.StyleConstants.getFontFamily(on),
        "not the logical Monospaced (Courier New on Windows)");
    assertEquals(
        FontFamilies.resolve("No Such Font"), javax.swing.text.StyleConstants.getFontFamily(off));
  }

  @Test
  void aLongWordWithoutSpacesWrapsInsteadOfScrollingSideways() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          String key = "ZG9udC1ldmVyLXN0b3JlLWFueXRoaW5nLWluLXBsYWluLXRleHQtaXQncy1zdHVwaWQ=";
          NoteEditor editor = new NoteEditor("key:\n" + key.repeat(3) + "\nend");
          javax.swing.JScrollPane pane = new javax.swing.JScrollPane(editor);
          pane.setSize(240, 400);
          pane.doLayout();
          pane.getViewport().doLayout();

          assertTrue(editor.getScrollableTracksViewportWidth(), "no horizontal scrolling");
          assertTrue(editor.getWidth() <= pane.getViewport().getWidth());
          try {
            double firstLine = editor.modelToView2D(5).getY();
            double lastChar = editor.modelToView2D(5 + key.length() * 3 - 1).getY();
            assertTrue(lastChar > firstLine, "the key continues on the next visual line");
          } catch (javax.swing.text.BadLocationException e) {
            throw new IllegalStateException(e);
          }
        });
  }

  @Test
  void theFirstLineGetsTheSameLineSpacingAsTheRest() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          for (javax.swing.JTextPane pane :
              new javax.swing.JTextPane[] {new NoteEditor(""), preview()}) {
            insert(pane, 0, "first\nsecond\nthird");
            pane.setSize(400, 400);
            try {
              double first = pane.modelToView2D(0).getHeight();
              double second = pane.modelToView2D(6).getHeight();
              assertEquals(second, first, 0.5, pane.getClass().getSimpleName() + ": even rhythm");
            } catch (javax.swing.text.BadLocationException e) {
              throw new IllegalStateException(e);
            }
          }
        });
  }

  private static javax.swing.JTextPane preview() {
    ThemePreview preview = new ThemePreview();
    preview.render(ThemeHolder.current());
    try {
      preview.getDocument().remove(0, preview.getDocument().getLength());
    } catch (javax.swing.text.BadLocationException e) {
      throw new IllegalStateException(e);
    }
    return preview;
  }

  @Test
  void aParagraphWithAnImageGetsNoExtraLineSpacing() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor editor = new NoteEditor("");
          BufferedImage img = new BufferedImage(100, 200, BufferedImage.TYPE_INT_ARGB);
          insert(editor, 0, " ", ImageAttr.of("images/x.png", img, 100, 200));
          insert(editor, 1, "\nnext line");
          editor.setSize(400, 800);
          try {
            double imageTop = editor.modelToView2D(0).getY();
            double nextTop = editor.modelToView2D(2).getY();
            assertTrue(nextTop - imageTop < 200 * 1.05, "no 15 % gap under the image");
            assertTrue(nextTop - imageTop >= 200, "the next line starts below the image");
          } catch (javax.swing.text.BadLocationException e) {
            throw new IllegalStateException(e);
          }
        });
  }

  @Test
  void lineWidthCentresTheTextInAWideWindow() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor editor = new NoteEditor("x");
          editor.fitWidth(2000, 60);
          int pad = editor.getInsets().left;
          assertTrue(pad > 300, "wide window, narrow column");
          assertEquals(pad, editor.getInsets().right);

          editor.fitWidth(2000, 0);
          assertEquals(18, editor.getInsets().left, "full width keeps the normal padding");
          editor.fitWidth(300, 60);
          assertEquals(18, editor.getInsets().left, "never narrower than the normal padding");
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

  private static void insert(javax.swing.JTextPane editor, int at, String text) {
    insert(editor, at, text, null);
  }

  private static void insert(
      javax.swing.JTextPane editor, int at, String text, javax.swing.text.AttributeSet attrs) {
    try {
      editor.getStyledDocument().insertString(at, text, attrs);
    } catch (javax.swing.text.BadLocationException e) {
      throw new IllegalStateException(e);
    }
  }
}
