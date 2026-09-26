package view;

import static view.Messages.tr;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;

/** The Edit and Format menus; their editor items run the editor's own key bindings. */
final class EditMenus {

  private static final int SHIFT = InputEvent.SHIFT_DOWN_MASK;

  private EditMenus() {}

  static JMenu edit(JRootPane root, AppCommands app, int mask, boolean mac) {
    TabsPane tabs = app.context().tabs();
    JMenu menu = new ThemedMenu(tr("menu.edit"));
    clipboard(menu, tabs, mask);
    menu.addSeparator();
    search(menu, root, app, mask, mac);
    menu.addSeparator();
    menu.add(editor("menu.edit.duplicateLine", KeyEvent.VK_D, mask, tabs));
    menu.add(editor("menu.edit.moveUp", KeyEvent.VK_UP, mask | SHIFT, tabs));
    menu.add(editor("menu.edit.moveDown", KeyEvent.VK_DOWN, mask | SHIFT, tabs));
    return menu;
  }

  static JMenu format(TabsPane tabs, int mask) {
    JMenu menu = new ThemedMenu(tr("menu.format"));
    for (InlineStyle style : InlineStyle.values()) {
      menu.add(EditorKeys.item(tr(style.labelKey), style.keyStroke(mask), tabs));
    }
    menu.addSeparator();
    menu.add(editor("menu.format.checkbox", KeyEvent.VK_ENTER, mask, tabs));
    return menu;
  }

  /** Undo / redo and the clipboard. */
  private static void clipboard(JMenu menu, TabsPane tabs, int mask) {
    menu.add(editor("menu.edit.undo", KeyEvent.VK_Z, mask, tabs));
    menu.add(editor("menu.edit.redo", KeyEvent.VK_Z, mask | SHIFT, tabs));
    menu.addSeparator();
    menu.add(editor("menu.edit.cut", KeyEvent.VK_X, mask, tabs));
    menu.add(editor("menu.edit.copy", KeyEvent.VK_C, mask, tabs));
    menu.add(editor("menu.edit.paste", KeyEvent.VK_V, mask, tabs));
    menu.add(editor("menu.edit.pastePlain", KeyEvent.VK_V, mask | SHIFT, tabs));
    menu.add(editor("menu.edit.selectAll", KeyEvent.VK_A, mask, tabs));
  }

  /** Find, replace and find in all notes: window-wide shortcuts. */
  private static void search(JMenu menu, JRootPane root, AppCommands app, int mask, boolean mac) {
    SearchBar bar = app.context().search();
    Runnable none = () -> {};
    menu.add(
        MenuItems.make(
            root, none, tr("menu.edit.find"), AppMenu.ks(KeyEvent.VK_F, mask), bar::open));
    menu.add(
        MenuItems.make(
            root,
            none,
            tr("menu.edit.replace"),
            PlatformKeys.replace(mac, mask),
            bar::openReplace));
    menu.add(
        MenuItems.make(
            root,
            none,
            tr("menu.edit.findAll"),
            AppMenu.ks(KeyEvent.VK_F, mask | SHIFT),
            app::browse));
  }

  private static JMenuItem editor(String key, int code, int modifiers, TabsPane tabs) {
    return EditorKeys.item(tr(key), AppMenu.ks(code, modifiers), tabs);
  }
}
