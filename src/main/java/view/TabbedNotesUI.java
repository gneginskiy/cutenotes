package view;

import javax.swing.JFrame;

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
  private final Toast toast = new Toast(getRootPane());
  private final WindowMemory windowMemory = new WindowMemory();
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
    windowMemory.restore(this);
    new Chrome(this, AppMenu.build(getRootPane(), smartReveal, menuActions()), tabs, searchBar);
    Zoom.install(this, appOptions, toast::flash);
    tabs.strip().setNotifier(toast::flash);
    tabs.addBelowHeader(searchBar);
    add(tabs.component());
    session.restore();
    applyTheme(ThemeHolder.current());
    AppExit.install(this, this::persist, SaveStatus.askQuitUnsaved(this));
    session.start();
    setVisible(true);
  }

  private AppMenu.Actions menuActions() {
    return new AppMenu.Actions(
        session::newTab,
        this::reopenLastClosed,
        session::closeCurrent,
        () -> NotesBrowserDialog.open(this, repo, groups, tabs.openIds(), session),
        tabs::selectNext,
        searchBar::open,
        appOptions::openDialog,
        () -> new HelpDialog(this).setVisible(true));
  }

  private void reopenLastClosed() {
    if (!session.reopenLastClosed()) {
      toast.flash("No recently closed tabs");
    }
  }

  /** Runs on exit (possibly on the shutdown-hook thread); returns whether every note was saved. */
  private boolean persist() {
    windowMemory.save(this);
    return session.persist();
  }

  private void applyTheme(Theme t) {
    saveStatus.setTitle(t.title() != null && !t.title().isBlank() ? t.title() : defaultTitle);
    setAlwaysOnTop(t.alwaysOnTop());
    tabs.applyTheme(t);
    MenuTheme.apply(getJMenuBar(), t);
    searchBar.applyTheme(t);
    PlatformLook.tintTitleBar(this, UiPalette.of(t));
  }
}
