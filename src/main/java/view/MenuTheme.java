package view;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import model.Theme;

final class MenuTheme {

  private MenuTheme() {}

  static void apply(JMenuBar bar, Theme t) {
    if (bar == null) {
      return;
    }
    Color bg = Colors.darken(t.bg(), 18);
    Color fg = t.fg();
    bar.setBackground(bg);
    bar.setOpaque(true);
    for (Component c : bar.getComponents()) {
      style(c, bg, fg);
    }
  }

  private static void style(Component c, Color bg, Color fg) {
    c.setBackground(bg);
    c.setForeground(fg);
    if (c instanceof JComponent jc) {
      jc.setOpaque(true);
    }
    if (c instanceof JMenu m) {
      m.getPopupMenu().setBackground(bg);
      for (Component sub : m.getMenuComponents()) {
        style(sub, bg, fg);
      }
    }
  }
}
