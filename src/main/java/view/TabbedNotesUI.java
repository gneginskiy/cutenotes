package view;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import dao.SessionStore;
import dao.TabRepository;
import model.Tab;
import model.TabMeta;
import model.Theme;
import util.AutoSaver;

public class TabbedNotesUI extends JFrame {

  private final TabRepository repo;
  private final SessionStore sessions;
  private final String defaultTitle;
  private final TabsPane tabs = new TabsPane();
  private final SearchBar searchBar = new SearchBar(tabs::activeArea);
  private final AppOptions appOptions = new AppOptions(this, this::applyTheme);
  private final Runnable smartReveal = () -> tabs.revealer().revealIf(!tabs.openIds().isEmpty());
  private final AutoSaver autoSaver;

  public TabbedNotesUI(String defaultTitle, TabRepository repo, SessionStore sessions) {
    super(defaultTitle);
    this.defaultTitle = defaultTitle;
    this.repo = repo;
    this.sessions = sessions;
    this.autoSaver = new AutoSaver(tabs::snapshot, repo);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(500, 400);
    WindowPlacement.centerOnScreen(this);
    new Chrome(this, buildMenu(), tabs, searchBar);
    Zoom.install(this, appOptions);
    Help.install(this);
    tabs.addBelowHeader(searchBar);
    add(tabs.component());
    restoreSession();
    applyTheme(ThemeHolder.current());
    Runtime.getRuntime().addShutdownHook(new Thread(this::persist));
    autoSaver.start();
    setVisible(true);
  }

  private void persist() {
    autoSaver.flush();
    sessions.write(TabCleanup.pruneEmpty(tabs.snapshot(), repo));
  }

  private JMenuBar buildMenu() {
    int mask = ShortcutMask.menu();
    JMenuBar bar = new ThemedMenuBar();
    JMenu menu = new ThemedMenu("Menu");
    menu.add(
        item("New tab", ks(KeyEvent.VK_T, mask), () -> tabs.openTab(repo.newId(), "untitled", "")));
    menu.add(
        item(
            "Reopen last",
            ks(KeyEvent.VK_T, mask | InputEvent.SHIFT_DOWN_MASK),
            this::reopenLastClosed));
    menu.add(item("Close tab", ks(KeyEvent.VK_W, mask), this::closeCurrentTab));
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

  private void restoreSession() {
    tabs.setDefaultContent(repo.load(TabsPane.DEFAULT_ID).content());
    for (String id : sessions.read()) {
      Tab tab = repo.load(id);
      if (tab.content().isBlank()) {
        repo.delete(id);
      } else {
        tabs.openTab(id, tab.name(), tab.content());
      }
    }
  }

  private void openExisting(String id) {
    Tab tab = repo.load(id);
    tabs.openTab(id, tab.name(), tab.content());
  }

  private void applyTheme(Theme t) {
    setTitle(t.title() != null && !t.title().isBlank() ? t.title() : defaultTitle);
    setAlwaysOnTop(t.alwaysOnTop());
    tabs.applyTheme(t);
    MenuTheme.apply(getJMenuBar(), t);
    searchBar.applyTheme(t);
  }

  private void closeCurrentTab() {
    Tab closed = tabs.closeCurrent();
    if (closed != null && closed.content().isBlank()) {
      repo.delete(closed.id());
    }
  }

  private void reopenLastClosed() {
    String candidate;
    while ((candidate = tabs.popLastClosed()) != null) {
      String id = candidate;
      if (repo.listMeta().stream().anyMatch(m -> m.id().equals(id))) {
        openExisting(id);
        return;
      }
    }
  }

  private void reopenTab() {
    Set<String> openNow = new HashSet<>(tabs.openIds());
    List<TabMeta> closed =
        repo.listMeta().stream()
            .filter(m -> !openNow.contains(m.id()) && !TabsPane.DEFAULT_ID.equals(m.id()))
            .toList();
    if (closed.isEmpty()) {
      return;
    }
    new ClosedTabsDialog(
            this, closed, meta -> openExisting(meta.id()), meta -> repo.delete(meta.id()))
        .setVisible(true);
  }
}
