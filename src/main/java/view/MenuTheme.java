package view;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import model.Theme;

/** Paints the in-window menu bar and popup menus in the chrome colours of the theme. */
final class MenuTheme {

  private MenuTheme() {}

  static void apply(JMenuBar bar, Theme t) {
    if (bar == null) {
      return;
    }
    UiPalette p = UiPalette.of(t);
    bar.setBackground(p.chrome());
    bar.setOpaque(true);
    bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, p.border()));
    for (Component c : bar.getComponents()) {
      style(c, p);
    }
  }

  static void stylePopup(JPopupMenu popup, UiPalette p) {
    popup.setBackground(p.chrome());
    popup.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(p.border()),
            BorderFactory.createEmptyBorder(4, 0, 4, 0)));
    for (Component c : popup.getComponents()) {
      style(c, p);
    }
  }

  private static void style(Component c, UiPalette p) {
    c.setBackground(p.chrome());
    c.setForeground(p.fg());
    if (c instanceof JComponent jc) {
      jc.setOpaque(true);
    }
    if (c instanceof JMenuItem item && !(c instanceof JMenu)) {
      item.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 14));
    }
    if (c instanceof JMenu m) {
      stylePopup(m.getPopupMenu(), p);
    }
  }
}
