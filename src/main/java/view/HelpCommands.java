package view;

import javax.swing.JFrame;

import dao.DataDir;
import util.AppLog;
import util.AppVersion;
import util.UpdateChecker;

/** The Help menu: shortcuts, about, what's new, updates, log files and the notes folder. */
final class HelpCommands {

  private final JFrame frame;
  private final Toast toast;

  HelpCommands(JFrame frame, Toast toast) {
    this.frame = frame;
    this.toast = toast;
  }

  void shortcuts() {
    new HelpDialog(frame).setVisible(true);
  }

  void about() {
    new AboutDialog(frame).setVisible(true);
  }

  void whatsNew() {
    Desktops.browse(UpdateChecker.pageOf(AppVersion.current()));
  }

  void checkUpdates() {
    Updates.check(toast, true);
  }

  void logs() {
    Desktops.open(AppLog.folder(DataDir.resolve()));
  }

  void dataFolder() {
    Desktops.open(DataDir.resolve());
  }
}
