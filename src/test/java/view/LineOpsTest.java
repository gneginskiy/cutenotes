package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
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

  private static void fireEnter(NoteEditor area) {
    Action action = area.getActionMap().get("line-op-10");
    action.actionPerformed(new ActionEvent(area, ActionEvent.ACTION_PERFORMED, "enter"));
  }
}
