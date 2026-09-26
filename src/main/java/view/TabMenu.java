package view;

import static view.Messages.tr;

import java.awt.Component;
import java.util.function.Consumer;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/** Right-click menu of a tab. */
final class TabMenu {

  /** {@code hasOthers} enables "Close other tabs"; {@code color} is the note's current colour. */
  record Actions(
      Runnable rename,
      Runnable close,
      Runnable closeOthers,
      boolean hasOthers,
      String color,
      Consumer<String> setColor) {}

  private TabMenu() {}

  static void show(Component chip, int x, int y, UiPalette palette, Actions actions) {
    JPopupMenu menu = new JPopupMenu();
    add(menu, tr("tab.rename"), actions.rename(), true);
    menu.add(ColorMenu.build(actions.color(), actions.setColor()));
    menu.addSeparator();
    add(menu, tr("tab.close"), actions.close(), true);
    add(menu, tr("tab.closeOthers"), actions.closeOthers(), actions.hasOthers());
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
