package view;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

final class KeyBindings {

  private KeyBindings() {}

  /** Binds {@code ks} anywhere in the window. */
  static void bind(JRootPane root, KeyStroke ks, String name, Runnable action) {
    put(root, root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), ks, name, action);
  }

  /** Binds {@code ks} while {@code c} has the focus; overrides the look-and-feel's binding. */
  static void bindFocused(JComponent c, KeyStroke ks, Object name, Runnable action) {
    put(c, c.getInputMap(), ks, name, action);
  }

  private static void put(
      JComponent c, InputMap inputs, KeyStroke ks, Object name, Runnable action) {
    inputs.put(ks, name);
    c.getActionMap()
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
