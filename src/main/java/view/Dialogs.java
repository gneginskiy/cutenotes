package view;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

final class Dialogs {

  private Dialogs() {}

  static void bindEscape(JDialog dialog) {
    bindEscape(dialog, dialog::dispose);
  }

  static void bindEscape(JDialog dialog, Runnable onEscape) {
    dialog
        .getRootPane()
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "dlg-esc");
    dialog
        .getRootPane()
        .getActionMap()
        .put(
            "dlg-esc",
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                onEscape.run();
              }
            });
  }
}
