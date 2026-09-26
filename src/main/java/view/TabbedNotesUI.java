package view;

import javax.swing.JFrame;

import dao.GroupStore;
import dao.NoteMetaStore;
import dao.SessionStore;
import model.Theme;
import util.EncryptingRepository;

public class TabbedNotesUI extends JFrame {

  private final String defaultTitle;
  private final TabsPane tabs = new TabsPane();
  private final SearchBar searchBar = new SearchBar(tabs::activeArea);
  private final AppOptions appOptions = new AppOptions(this, this::applyTheme);
  private final Toast toast = new Toast(getRootPane());
  private final WindowMemory windowMemory = new WindowMemory();
  private final Runnable smartReveal =
      () -> tabs.strip().revealer().revealIf(!tabs.openIds().isEmpty());
  private final SaveStatus saveStatus;
  private boolean titleBarFits = true;
  private final NoteSession session;

  public TabbedNotesUI(
      String defaultTitle,
      EncryptingRepository repo,
      SessionStore sessions,
      GroupStore groups,
      NoteMetaStore metas) {
    super(defaultTitle);
    this.defaultTitle = defaultTitle;
    this.saveStatus = new SaveStatus(this::setTitle, SaveStatus.dialogOver(this), defaultTitle);
    tabs.identity().useMetas(metas);
    this.session = new NoteSession(repo, sessions, tabs, saveStatus);
    AppCommands commands =
        new AppCommands(
            new AppContext(this, session, tabs, searchBar, appOptions, toast, repo, groups));
    windowMemory.restore(this);
    PlatformLook.installIcons(this);
    Chrome.install(this, AppMenu.build(getRootPane(), smartReveal, commands), tabs, searchBar);
    Zoom.install(this, appOptions, toast::flash);
    tabs.strip().setNotifier(toast::flash);
    tabs.addBelowHeader(searchBar);
    add(tabs.component());
    session.restore();
    applyTheme(ThemeHolder.current());
    AppExit.install(this, this::persist, SaveStatus.askQuitUnsaved(this));
    session.start();
    setVisible(true);
    Updates.greet(toast);
    Updates.checkAtStartup(toast);
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
    boolean fits = PlatformLook.titleBarFits(UiPalette.of(t));
    if (titleBarFits && !fits) {
      toast.flash(Messages.tr("toast.restartTitleBar"));
    }
    titleBarFits = fits;
  }
}
