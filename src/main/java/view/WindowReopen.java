package view;

import java.awt.Desktop;
import java.awt.desktop.AppReopenedListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import dao.DataDir;
import util.InstanceChannel;

/**
 * Shows or hides the window when the app is started again (a hotkey bound to the app, the launcher)
 * and brings it back on macOS when its Dock icon is clicked while it is hidden.
 */
final class WindowReopen {

  private static final Logger LOG = Logger.getLogger(WindowReopen.class.getName());

  private WindowReopen() {}

  static void install(JFrame frame) {
    Runnable show = () -> SwingUtilities.invokeLater(() -> AppTray.WindowShow.bringToFront(frame));
    try {
      // starting again works like a show / hide hotkey
      InstanceChannel.listen(
          DataDir.resolve(),
          () -> SwingUtilities.invokeLater(() -> AppTray.WindowShow.toggle(frame)));
    } catch (IOException e) {
      LOG.log(Level.INFO, "a second start cannot reach this one", e);
    }
    if (Desktop.isDesktopSupported()
        && Desktop.getDesktop().isSupported(Desktop.Action.APP_EVENT_REOPENED)) {
      Desktop.getDesktop().addAppEventListener((AppReopenedListener) e -> show.run());
    }
  }
}
