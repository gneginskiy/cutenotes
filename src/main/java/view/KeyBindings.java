package view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

final class KeyBindings {

  private KeyBindings() {}

  static void bind(JRootPane root, KeyStroke ks, String name, Runnable action) {
    root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, name);
    root.getActionMap()
        .put(
            name,
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                action.run();
              }
            });
  }
}
