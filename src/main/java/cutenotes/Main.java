package cutenotes;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import dao.DataDir;
import dao.FileGroupStore;
import dao.FileNoteMetaStore;
import dao.FileOptionsStore;
import dao.FileSessionStore;
import dao.FileSettingsStore;
import dao.FolderLease;
import dao.GroupStore;
import dao.InstanceLock;
import dao.NoteMetaStore;
import dao.SessionStore;
import model.Theme;
import util.AppLog;
import util.EncryptingRepository;
import util.InstanceChannel;
import util.NoteStorage;
import view.Messages;
import view.PlatformLook;
import view.SettingsHolder;
import view.TabbedNotesUI;
import view.ThemeHolder;

public final class Main {

  private static final String TITLE = "Notes :3";

  /** Held for the whole process lifetime; the OS releases it when the process ends. */
  private static InstanceLock lock;

  public static void main(String[] args) {
    Theme theme = new FileOptionsStore().load();
    SettingsHolder.init(new FileSettingsStore());
    Messages.useLanguage(SettingsHolder.current().language());
    PlatformLook.prepare(theme);
    ThemeHolder.set(theme);
    Path data = DataDir.resolve();
    acquireLock(data);
    AppLog.install(data);
    claimFolder(data);
    Thread.ofVirtual()
        .start(
            () -> GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    NoteStorage storage = NoteStorage.open(data);
    storage.housekeepInBackground();
    EncryptingRepository repo = storage.repo();
    SessionStore sessions = new FileSessionStore();
    GroupStore groups = new FileGroupStore();
    NoteMetaStore metas = new FileNoteMetaStore();
    SwingUtilities.invokeLater(() -> new TabbedNotesUI(TITLE, repo, sessions, groups, metas));
  }

  /** One app per notes folder on this computer: a second start just says so and quits. */
  private static void acquireLock(Path data) {
    lock = InstanceLock.tryAcquire(data);
    if (lock == null && InstanceChannel.signal(data)) {
      System.exit(0);
    }
    if (lock == null) {
      JOptionPane.showMessageDialog(
          null, Messages.tr("app.alreadyRunning", data), TITLE, JOptionPane.INFORMATION_MESSAGE);
      System.exit(0);
    }
  }

  /**
   * Warns when another computer used this (synced) notes folder minutes ago, then keeps announcing
   * this one.
   */
  private static void claimFolder(Path data) {
    FolderLease lease = new FolderLease(data, FolderLease.localHost());
    FolderLease.Holder other = lease.otherHolder(Instant.now());
    if (other != null) {
      long minutes = Duration.between(other.since(), Instant.now()).toMinutes();
      int answer =
          JOptionPane.showConfirmDialog(
              null,
              Messages.tr("lease.inUse", other.host(), minutes),
              Messages.tr("lease.title"),
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);
      if (answer != JOptionPane.YES_OPTION) {
        System.exit(0);
      }
    }
    lease.keepRenewing();
  }
}
