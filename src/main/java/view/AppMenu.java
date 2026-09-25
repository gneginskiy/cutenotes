package view;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/** The window's menu; every item's accelerator also works while the menu bar is hidden. */
final class AppMenu {

  /** The commands behind the menu items. */
  record Actions(
      Runnable newTab,
      Runnable reopenLast,
      Runnable close,
      Runnable browse,
      Runnable next,
      Runnable find,
      Runnable options,
      Runnable help) {}

  private AppMenu() {}

  /** {@code reveal} runs after tab commands, so the tabs bar shows what just happened. */
  static JMenuBar build(JRootPane root, Runnable reveal, Actions a) {
    int mask = ShortcutMask.menu();
    JMenu menu = new ThemedMenu("Menu");
    menu.add(MenuItems.make(root, reveal, "New tab", ks(KeyEvent.VK_T, mask), a.newTab()));
    menu.add(
        MenuItems.make(
            root,
            reveal,
            "Reopen closed tab",
            ks(KeyEvent.VK_T, mask | InputEvent.SHIFT_DOWN_MASK),
            a.reopenLast()));
    menu.add(MenuItems.make(root, reveal, "Close tab", ks(KeyEvent.VK_W, mask), a.close()));
    menu.add(MenuItems.make(root, reveal, "All notes…", ks(KeyEvent.VK_R, mask), a.browse()));
    for (int modifier : PlatformKeys.nextTabModifiers(PlatformLook.MAC)) {
      menu.add(nextTab(root, reveal, modifier, a.next()));
    }
    menu.addSeparator();
    menu.add(MenuItems.make(root, reveal, "Find…", ks(KeyEvent.VK_F, mask), a.find()));
    menu.addSeparator();
    menu.add(MenuItems.make(root, () -> {}, "Options…", ks(KeyEvent.VK_O, mask), a.options()));
    menu.add(MenuItems.make(root, () -> {}, "Keyboard shortcuts", ks(KeyEvent.VK_F1, 0), a.help()));
    JMenuBar bar = new ThemedMenuBar();
    bar.add(menu);
    return bar;
  }

  private static JMenuItem nextTab(JRootPane root, Runnable reveal, int modifier, Runnable next) {
    return MenuItems.make(root, reveal, "Next tab", ks(KeyEvent.VK_TAB, modifier), next);
  }

  private static KeyStroke ks(int keyCode, int modifiers) {
    return KeyStroke.getKeyStroke(keyCode, modifiers);
  }
}
