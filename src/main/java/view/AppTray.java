package view;

import static view.Messages.tr;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 * The icon in the system tray (menu bar on macOS): click to show or hide the window; its menu also
 * makes a new note or quits. Shown while Options › Application › tray icon is on.
 */
final class AppTray {

  private static final Logger LOG = Logger.getLogger(AppTray.class.getName());

  private final JFrame frame;
  private final Runnable newNote;
  private TrayIcon icon;

  AppTray(JFrame frame, Runnable newNote) {
    this.frame = frame;
    this.newNote = newNote;
  }

  /** Adds or removes the icon to match the setting. */
  void update() {
    boolean wanted = SettingsHolder.current().trayIcon() && SystemTray.isSupported();
    if (wanted && icon == null) {
      add();
    } else if (!wanted && icon != null) {
      SystemTray.getSystemTray().remove(icon);
      icon = null;
    }
  }

  void toggle() {
    WindowShow.toggle(frame);
  }

  private void add() {
    PopupMenu menu = new PopupMenu();
    menu.add(item(tr("tray.toggle"), this::toggle));
    menu.add(
        item(
            tr("tray.newNote"),
            () -> {
              WindowShow.bringToFront(frame);
              newNote.run();
            }));
    menu.addSeparator();
    menu.add(
        item(
            tr("tray.quit"),
            () -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING))));
    TrayIcon tray = new TrayIcon(AppIcon.image(64), "cuteNotes", menu);
    tray.setImageAutoSize(true);
    tray.addActionListener(e -> toggle());
    try {
      SystemTray.getSystemTray().add(tray);
      icon = tray;
    } catch (AWTException e) {
      LOG.log(Level.INFO, "no system tray", e);
    }
  }

  private static MenuItem item(String label, Runnable action) {
    MenuItem item = new MenuItem(label);
    item.addActionListener(e -> action.run());
    return item;
  }

  /** Brings a window back: shown, not minimised, in front and focused. */
  static final class WindowShow {

    private WindowShow() {}

    /** Hides a window in front, otherwise brings it to the front. */
    static void toggle(JFrame frame) {
      if (frame.isVisible() && frame.isFocused()) {
        frame.setVisible(false);
      } else {
        bringToFront(frame);
      }
    }

    static void bringToFront(JFrame frame) {
      frame.setVisible(true);
      if ((frame.getExtendedState() & Frame.ICONIFIED) != 0) {
        frame.setExtendedState(frame.getExtendedState() & ~Frame.ICONIFIED);
      }
      frame.toFront();
      frame.requestFocus();
    }
  }
}
