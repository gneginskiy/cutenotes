package view;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import dao.GroupStore;
import dao.SessionStore;
import dao.TabRepository;
import model.Theme;

public class TabbedNotesUI extends JFrame {

  private final TabRepository repo;
  private final GroupStore groups;
  private final String defaultTitle;
  private final TabsPane tabs = new TabsPane();
  private final SearchBar searchBar = new SearchBar(tabs::activeArea);
  private final AppOptions appOptions = new AppOptions(this, this::applyTheme);
  private final Runnable smartReveal = () -> tabs.revealer().revealIf(!tabs.openIds().isEmpty());
  private final SaveStatus saveStatus;
  private final NoteSession session;

  public TabbedNotesUI(
      String defaultTitle, TabRepository repo, SessionStore sessions, GroupStore groups) {
    super(defaultTitle);
    this.defaultTitle = defaultTitle;
    this.repo = repo;
    this.groups = groups;
    this.saveStatus = new SaveStatus(this::setTitle, SaveStatus.dialogOver(this), defaultTitle);
    this.session = new NoteSession(repo, sessions, tabs, saveStatus);
    setSize(500, 400);
    WindowPlacement.centerOnScreen(this);
    new Chrome(this, buildMenu(), tabs, searchBar);
    Zoom.install(this, appOptions);
    Help.install(this);
    tabs.addBelowHeader(searchBar);
    add(tabs.component());
    session.restore();
    applyTheme(ThemeHolder.current());
    AppExit.install(this, session::persist, SaveStatus.askQuitUnsaved(this));
    session.start();
    setVisible(true);
  }

  private JMenuBar buildMenu() {
    int mask = ShortcutMask.menu();
    JMenuBar bar = new ThemedMenuBar();
    JMenu menu = new ThemedMenu("Menu");
    menu.add(item("New tab", ks(KeyEvent.VK_T, mask), session::newTab));
    menu.add(
        item(
            "Reopen last",
            ks(KeyEvent.VK_T, mask | InputEvent.SHIFT_DOWN_MASK),
            session::reopenLastClosed));
    menu.add(item("Close tab", ks(KeyEvent.VK_W, mask), session::closeCurrent));
    menu.add(item("Reopen tab...", ks(KeyEvent.VK_R, mask), this::reopenTab));
    menu.add(item("Next tab", ks(KeyEvent.VK_TAB, InputEvent.ALT_DOWN_MASK), tabs::selectNext));
    menu.add(item("Next tab", ks(KeyEvent.VK_TAB, InputEvent.CTRL_DOWN_MASK), tabs::selectNext));
    menu.add(item("Find", ks(KeyEvent.VK_F, mask), searchBar::open));
    menu.add(itemPlain("Options", ks(KeyEvent.VK_O, mask), appOptions::openDialog));
    bar.add(menu);
    return bar;
  }

  private static KeyStroke ks(int keyCode, int modifiers) {
    return KeyStroke.getKeyStroke(keyCode, modifiers);
  }

  private JMenuItem item(String label, KeyStroke acc, Runnable action) {
    return MenuItems.make(getRootPane(), smartReveal, label, acc, action);
  }

  private JMenuItem itemPlain(String label, KeyStroke acc, Runnable action) {
    return MenuItems.make(getRootPane(), () -> {}, label, acc, action);
  }

  private void applyTheme(Theme t) {
    saveStatus.setTitle(t.title() != null && !t.title().isBlank() ? t.title() : defaultTitle);
    setAlwaysOnTop(t.alwaysOnTop());
    tabs.applyTheme(t);
    MenuTheme.apply(getJMenuBar(), t);
    searchBar.applyTheme(t);
  }

  private void reopenTab() {
    new NotesBrowserDialog(
            this,
            repo,
            groups,
            new HashSet<>(tabs.openIds()),
            new NotesBrowserDialog.Callbacks(session::openExisting, session::delete))
        .setVisible(true);
  }
}
