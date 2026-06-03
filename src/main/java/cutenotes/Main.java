package cutenotes;

import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

import dao.FileGroupStore;
import dao.FileOptionsStore;
import dao.FileSessionStore;
import dao.FileTabRepository;
import dao.GroupStore;
import dao.SessionStore;
import dao.TabRepository;
import view.TabbedNotesUI;
import view.ThemeHolder;

public final class Main {

  private static final String TITLE = "Notes :3";

  public static void main(String[] args) {
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
