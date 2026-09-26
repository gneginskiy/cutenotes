package view;

import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

final class Chrome {

  private Chrome() {}

  /** Installs the (hidden until Esc) menu bar and the Esc toggle for menu, tabs and search. */
  static void install(JFrame frame, JMenuBar bar, TabsPane tabs, SearchBar search) {
    frame.setJMenuBar(bar);
    MenuTheme.apply(bar, ThemeHolder.current());
    Revealer menu = new Revealer(bar, frame.getRootPane());
    KeyBindings.bind(
        frame.getRootPane(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        "esc",
        () -> onEscape(menu, tabs, search));
  }

  private static void onEscape(Revealer menu, TabsPane tabs, SearchBar search) {
    Revealer tabsRev = tabs.strip().revealer();
    boolean anyOpen =
        menu.isShown() || search.isVisible() || (tabsRev.isShown() && !tabs.strip().pinned());
    if (anyOpen) {
      menu.hide();
      tabsRev.hide();
      search.close();
    } else {
      menu.show();
      if (!tabs.openIds().isEmpty()) {
        tabsRev.show();
      }
    }
  }
}
