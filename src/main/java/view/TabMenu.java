package view;

import java.awt.Component;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/** Right-click menu of a tab. */
final class TabMenu {

  /** {@code hasOthers} enables "Close other tabs". */
  record Actions(Runnable rename, Runnable close, Runnable closeOthers, boolean hasOthers) {}

  private TabMenu() {}

  static void show(Component chip, int x, int y, UiPalette palette, Actions actions) {
    JPopupMenu menu = new JPopupMenu();
    add(menu, "Rename…", actions.rename(), true);
    menu.addSeparator();
    add(menu, "Close tab", actions.close(), true);
    add(menu, "Close other tabs", actions.closeOthers(), actions.hasOthers());
    MenuTheme.stylePopup(menu, palette);
    menu.show(chip, x, y);
  }

  private static void add(JPopupMenu menu, String label, Runnable action, boolean enabled) {
    JMenuItem item = new JMenuItem(label);
    item.setEnabled(enabled);
    item.addActionListener(e -> action.run());
    menu.add(item);
  }
}
