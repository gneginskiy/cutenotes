package view;

import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

final class MenuItems {

  private MenuItems() {}

  static JMenuItem make(
      JRootPane root, Runnable reveal, String label, KeyStroke acc, Runnable action) {
    Runnable wrapped =
        () -> {
          action.run();
          reveal.run();
        };
    KeyBindings.bind(root, acc, label, wrapped);
    JMenuItem mi = new JMenuItem(label);
    mi.setAccelerator(acc);
    mi.addActionListener(e -> wrapped.run());
    return mi;
  }

  /** A menu item whose shortcut (if any) is bound elsewhere; it is only shown here. */
  static JMenuItem display(String label, KeyStroke acc, Runnable action) {
    JMenuItem mi = new JMenuItem(label);
    mi.setAccelerator(acc);
    mi.addActionListener(e -> action.run());
    return mi;
  }
}
