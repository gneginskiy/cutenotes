package view;

import static view.Messages.tr;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/**
 * The window's menu bar: File, Edit, Format, View, Help. Window-wide shortcuts also work while the
 * bar is hidden; editor shortcuts belong to the editor and are only shown here.
 */
final class AppMenu {

  private AppMenu() {}

  /** {@code reveal} runs after tab commands, so the tabs bar shows what just happened. */
  static JMenuBar build(JRootPane root, Runnable reveal, AppCommands app) {
    int mask = ShortcutMask.menu();
    boolean mac = PlatformLook.MAC;
    HelpCommands help = new HelpCommands(app.context().frame(), app.context().toast());
    JMenuBar bar = new ThemedMenuBar();
    bar.add(file(root, reveal, app, mask));
    bar.add(EditMenus.edit(root, app, mask, mac));
    bar.add(EditMenus.format(app.context().tabs(), mask));
    bar.add(ViewHelpMenus.view(root, reveal, app, mask, mac));
    bar.add(ViewHelpMenus.help(help));
    KeyBindings.bind(root, ks(KeyEvent.VK_F1, 0), "help", help::shortcuts);
    return bar;
  }

  private static JMenu file(JRootPane root, Runnable reveal, AppCommands app, int mask) {
    JMenu menu = new ThemedMenu(tr("menu.file"));
    menu.add(
        MenuItems.make(root, reveal, tr("menu.file.newTab"), ks(KeyEvent.VK_T, mask), app::newTab));
    menu.add(
        MenuItems.make(
            root, reveal, tr("menu.file.allNotes"), ks(KeyEvent.VK_R, mask), app::browse));
    menu.add(RecentMenu.build(app));
    menu.add(
        MenuItems.make(
            root,
            reveal,
            tr("menu.file.reopen"),
            ks(KeyEvent.VK_T, mask | InputEvent.SHIFT_DOWN_MASK),
            app::reopenLast));
    menu.add(
        MenuItems.make(
            root, reveal, tr("menu.file.closeTab"), ks(KeyEvent.VK_W, mask), app::closeTab));
    menu.addSeparator();
    NoteMenuItems.add(menu, root, app, mask);
    menu.addSeparator();
    menu.add(
        MenuItems.make(
            root,
            () -> {},
            tr("menu.file.options"),
            ks(KeyEvent.VK_O, mask),
            app.context().options()::openDialog));
    return menu;
  }

  static KeyStroke ks(int keyCode, int modifiers) {
    return KeyStroke.getKeyStroke(keyCode, modifiers);
  }
}
