package view;

import static view.Messages.tr;

import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/** The View menu (tabs, zoom, word count) and the Help menu. */
final class ViewHelpMenus {

  private ViewHelpMenus() {}

  static JMenu view(JRootPane root, Runnable reveal, AppCommands app, int mask, boolean mac) {
    JMenu menu = new ThemedMenu(tr("menu.view"));
    tabNavigation(menu, root, reveal, app, mask, mac);
    menu.addSeparator();
    menu.add(
        MenuItems.display(
            tr("menu.view.zoomIn"), AppMenu.ks(KeyEvent.VK_EQUALS, mask), () -> app.zoom(1)));
    menu.add(
        MenuItems.display(
            tr("menu.view.zoomOut"), AppMenu.ks(KeyEvent.VK_MINUS, mask), () -> app.zoom(-1)));
    menu.add(MenuItems.display(tr("menu.view.wordCount"), null, app::wordCount));
    menu.add(
        MenuItems.display(
            tr("menu.view.pinTabs"), null, () -> app.context().tabs().strip().togglePin()));
    return menu;
  }

  static JMenu help(HelpCommands help) {
    JMenu menu = new ThemedMenu(tr("menu.help"));
    menu.add(
        MenuItems.display(
            tr("menu.help.shortcuts"), AppMenu.ks(KeyEvent.VK_F1, 0), help::shortcuts));
    menu.add(MenuItems.display(tr("menu.help.whatsNew"), null, help::whatsNew));
    menu.add(MenuItems.display(tr("menu.help.checkUpdates"), null, help::checkUpdates));
    menu.addSeparator();
    menu.add(MenuItems.display(tr("menu.help.logs"), null, help::logs));
    menu.add(MenuItems.display(tr("menu.help.dataFolder"), null, help::dataFolder));
    menu.addSeparator();
    menu.add(MenuItems.display(tr("menu.help.about"), null, help::about));
    return menu;
  }

  /** Next / previous tab (with every platform variant) and Cmd/Ctrl+1…9. */
  private static void tabNavigation(
      JMenu menu, JRootPane root, Runnable reveal, AppCommands app, int mask, boolean mac) {
    for (int modifier : PlatformKeys.nextTabModifiers(mac)) {
      menu.add(
          MenuItems.make(
              root,
              reveal,
              tr("menu.view.nextTab"),
              AppMenu.ks(KeyEvent.VK_TAB, modifier),
              app::selectNext));
    }
    KeyBindings.bind(root, PlatformKeys.nextTabExtra(mac, mask), "next-tab-extra", app::selectNext);
    List<KeyStroke> previous = PlatformKeys.previousTab(mac, mask);
    menu.add(
        MenuItems.make(
            root, reveal, tr("menu.view.previousTab"), previous.get(0), app::selectPrevious));
    KeyBindings.bind(root, previous.get(1), "previous-tab-extra", app::selectPrevious);
    for (int n = 1; n <= 9; n++) {
      int number = n;
      KeyBindings.bind(
          root, AppMenu.ks(KeyEvent.VK_0 + n, mask), "tab-" + n, () -> app.selectTab(number));
    }
  }
}
