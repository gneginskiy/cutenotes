package view;

import static view.Messages.tr;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/** The per-note items of the File menu: version history, the bin, export, print, password. */
final class NoteMenuItems {

  private NoteMenuItems() {}

  static void add(JMenu menu, JRootPane root, AppCommands app, int mask) {
    Runnable none = () -> {};
    int shift = InputEvent.SHIFT_DOWN_MASK;
    menu.add(MenuItems.display(tr("menu.file.history"), null, app::showHistory));
    menu.add(MenuItems.display(tr("menu.file.trash"), null, app::showTrash));
    menu.addSeparator();
    menu.add(
        MenuItems.make(
            root,
            none,
            tr("menu.file.export"),
            AppMenu.ks(KeyEvent.VK_E, mask | shift),
            app::export));
    menu.add(
        MenuItems.make(
            root, none, tr("menu.file.print"), AppMenu.ks(KeyEvent.VK_P, mask), app::print));
    menu.addSeparator();
    JMenuItem password = MenuItems.display(tr("menu.file.lock"), null, app::togglePassword);
    menu.add(password);
    JMenuItem lockAll =
        MenuItems.make(
            root,
            none,
            tr("menu.file.lockAll"),
            AppMenu.ks(KeyEvent.VK_L, mask | shift),
            app::lockAll);
    menu.add(lockAll);
    menu.addMenuListener(new PasswordItems(app.locks(), password, lockAll));
  }

  /** Names the password item after what it will do and enables "lock" only when it can. */
  private record PasswordItems(NoteLocks locks, JMenuItem password, JMenuItem lockAll)
      implements MenuListener {

    @Override
    public void menuSelected(MenuEvent e) {
      password.setText(tr(locks.activeProtected() ? "menu.file.unlock" : "menu.file.lock"));
      lockAll.setEnabled(locks.anyUnlocked());
    }

    @Override
    public void menuDeselected(MenuEvent e) {
      // nothing to reset
    }

    @Override
    public void menuCanceled(MenuEvent e) {
      // nothing to reset
    }
  }
}
