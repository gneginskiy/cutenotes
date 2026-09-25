package cutenotes;

import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import dao.DataDir;
import dao.FileGroupStore;
import dao.FileOptionsStore;
import dao.FileSessionStore;
import dao.FileTabRepository;
import dao.GroupStore;
import dao.InstanceLock;
import dao.SessionStore;
import dao.TabRepository;
import view.TabbedNotesUI;
import view.ThemeHolder;

public final class Main {

  private static final String TITLE = "Notes :3";

  /** Held for the whole process lifetime; the OS releases it when the process ends. */
  private static InstanceLock lock;

  public static void main(String[] args) {
    lock = InstanceLock.tryAcquire(DataDir.resolve());
    if (lock == null) {
      JOptionPane.showMessageDialog(
          null,
          "cuteNotes is already running with the notes in\n" + DataDir.resolve(),
          TITLE,
          JOptionPane.INFORMATION_MESSAGE);
      System.exit(0);
    }
    Thread.ofVirtual()
        .start(
            () -> GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    TabRepository repo = new FileTabRepository();
    SessionStore sessions = new FileSessionStore();
    GroupStore groups = new FileGroupStore();
    ThemeHolder.set(new FileOptionsStore().load());
    SwingUtilities.invokeLater(() -> new TabbedNotesUI(TITLE, repo, sessions, groups));
  }
}
