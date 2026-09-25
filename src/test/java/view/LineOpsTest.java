package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

class LineOpsTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  @Test
  void insertsTenNewlinesAtCaret() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor area = new NoteEditor("ab");
          area.setCaretPosition(1);

          fireEnter(area);

          assertEquals("a\n\n\n\n\n\n\n\n\n\nb", area.getText());
          assertEquals(11, area.getCaretPosition());
        });
  }

  @Test
  void insertedBlockIsSingleUndoStep() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor area = new NoteEditor("ab");
          area.setCaretPosition(1);

          fireEnter(area);
          assertEquals("a\n\n\n\n\n\n\n\n\n\nb", area.getText());

          KeyStroke z = KeyStroke.getKeyStroke(KeyEvent.VK_Z, ShortcutMask.menu());
          Action undo = area.getActionMap().get(z.toString());
          undo.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, "Undo"));

          assertEquals("ab", area.getText());
        });
  }

  @Test
  void moveLineIsCmdShiftOnMacAndCtrlShiftOnWindowsAndLinux() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          for (int mask : new int[] {InputEvent.META_DOWN_MASK, InputEvent.CTRL_DOWN_MASK}) {
            JTextPane pane = new JTextPane();
            LineOps.install(pane, new EditorUndo(pane), mask);
            int shifted = mask | InputEvent.SHIFT_DOWN_MASK;

            assertNotNull(pane.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, shifted)));
            assertNotNull(
                pane.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, shifted)));
          }
        });
  }

  @Test
  void movedLinesKeepSelectionFormattingAndUndoInOneStep() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor area = new NoteEditor("top\n**bold** line\nplain\nbottom");
          area.select(area.getText().indexOf("old"), area.getText().indexOf("lain"));

          fire(area, KeyEvent.VK_DOWN);

          assertEquals("top\nbottom\n**bold** line\nplain", area.markdown());
          assertEquals("old line\np", area.getSelectedText());

          area.undoHistory().undo();
          assertEquals("top\n**bold** line\nplain\nbottom", area.markdown());
        });
  }

  @Test
  void movingALineDoesNotScrollWhileItStaysInView() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor area = new NoteEditor("line\n".repeat(200) + "last");
          JScrollPane pane = new JScrollPane(area);
          pane.setSize(300, 200);
          pane.doLayout();
          pane.getViewport().doLayout();
          int bottom = area.getPreferredSize().height - pane.getViewport().getHeight();
          pane.getViewport().setViewPosition(new Point(0, bottom));
          area.setCaretPosition(area.getDocument().getLength() - 12);
          Point before = pane.getViewport().getViewPosition();

          fire(area, KeyEvent.VK_UP);

          assertEquals(before, pane.getViewport().getViewPosition());
        });
  }

  @Test
  void movingALongLineIsNotANavigationJump() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          NoteEditor area = new NoteEditor("x".repeat(100) + "\nshort");
          area.setCaretPosition(3);

          fire(area, KeyEvent.VK_DOWN);
          int moved = area.getCaretPosition();
          area.getActionMap().get("nav-back").actionPerformed(null);

          assertEquals(moved, area.getCaretPosition(), "back must not undo the move's caret shift");
        });
  }

  private static void fire(NoteEditor area, int key) {
    Object name =
        area.getInputMap()
            .get(KeyStroke.getKeyStroke(key, ShortcutMask.menu() | InputEvent.SHIFT_DOWN_MASK));
    area.getActionMap().get(name).actionPerformed(null);
  }

  private static void fireEnter(NoteEditor area) {
    Action action = area.getActionMap().get("line-op-10");
    action.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, "enter"));
  }
}
