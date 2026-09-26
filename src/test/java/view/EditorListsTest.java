package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class EditorListsTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  @Test
  void enterContinuesBulletsNumbersAndTasks() throws Exception {
    onEdt(
        () -> {
          assertEquals("- milk\n- ", enterAtEnd("- milk"));
          assertEquals("  1. one\n  2. ", enterAtEnd("  1. one"));
          assertEquals("- [x] done\n- [ ] ", enterAtEnd("- [x] done"));
        });
  }

  @Test
  void enterOnAnEmptyItemEndsTheList() throws Exception {
    onEdt(() -> assertEquals("- a\n", enterAtEnd("- a\n- ")));
  }

  @Test
  void enterOutsideAListIsAnOrdinaryLineBreak() throws Exception {
    onEdt(() -> assertEquals("plain\n", enterAtEnd("plain")));
  }

  @Test
  void enterInsideTheMarkerIsAnOrdinaryLineBreak() throws Exception {
    onEdt(
        () -> {
          NoteEditor e = new NoteEditor("- item");
          e.setCaretPosition(1);
          fire(e, KeyEvent.VK_ENTER, 0);
          assertEquals("-\n item", e.getText());
        });
  }

  @Test
  void tabIndentsListItemsAndShiftTabOutdentsInOneUndoStep() throws Exception {
    onEdt(
        () -> {
          NoteEditor e = new NoteEditor("- a\n- b");
          e.select(0, e.getText().length());
          fire(e, KeyEvent.VK_TAB, 0);
          assertEquals("  - a\n  - b", e.getText());

          fire(e, KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK);
          assertEquals("- a\n- b", e.getText());

          e.undoHistory().undo();
          assertEquals("  - a\n  - b", e.getText());
        });
  }

  @Test
  void tabOnAPlainLineStillInsertsATab() throws Exception {
    onEdt(
        () -> {
          NoteEditor e = new NoteEditor("x");
          e.setCaretPosition(1);
          fire(e, KeyEvent.VK_TAB, 0);
          assertEquals("x\t", e.getText());
        });
  }

  @Test
  void checkboxToggleAndConversion() throws Exception {
    onEdt(
        () -> {
          NoteEditor e = new NoteEditor("- [ ] ship\n- plain");
          assertTrue(EditorCheckboxes.toggle(e, 0));
          assertEquals("- [x] ship\n- plain", e.getText());
          assertTrue(EditorCheckboxes.toggle(e, 12));
          assertEquals("- [x] ship\n- [ ] plain", e.getText());
          assertFalse(EditorCheckboxes.toggle(new NoteEditor("text"), 0));
          assertEquals("- [x] ship\n- [ ] plain", e.markdown().replace("\\\\", ""));
        });
  }

  @Test
  void cmdEnterOutsideAListStillInsertsBlankLines() throws Exception {
    onEdt(
        () -> {
          NoteEditor e = new NoteEditor("ab");
          e.setCaretPosition(1);
          fire(e, KeyEvent.VK_ENTER, ShortcutMask.menu());
          assertEquals("a" + "\n".repeat(10) + "b", e.getText());
        });
  }

  private static String enterAtEnd(String text) {
    NoteEditor e = new NoteEditor("");
    e.setText(text);
    e.setCaretPosition(text.length());
    fire(e, KeyEvent.VK_ENTER, 0);
    return e.getText();
  }

  static void fire(NoteEditor e, int key, int modifiers) {
    Object name = e.getInputMap().get(KeyStroke.getKeyStroke(key, modifiers));
    e.getActionMap().get(name).actionPerformed(new java.awt.event.ActionEvent(e, 0, ""));
  }

  private static void onEdt(Runnable r) throws Exception {
    SwingUtilities.invokeAndWait(r);
  }
}
