package view;

import java.util.function.UnaryOperator;

import dao.SessionStore;
import dao.TabRepository;
import model.Tab;
import util.AutoSaver;
import util.NoteCrypto;
import util.SaveListener;

/**
 * Note lifecycle behind the tabs: restoring the session, opening, closing, deleting and persisting.
 * Every write goes through the {@link AutoSaver}, so the EDT and the background saver never write
 * the same file concurrently and a deleted note cannot be resurrected by an in-flight snapshot.
 */
final class NoteSession {

  private final TabRepository repo;
  private final SessionStore sessions;
  private final TabsPane tabs;
  private final AutoSaver autoSaver;
  private UnaryOperator<Tab> unlocker = UnaryOperator.identity();

  NoteSession(TabRepository repo, SessionStore sessions, TabsPane tabs, SaveListener listener) {
    this.repo = repo;
    this.sessions = sessions;
    this.tabs = tabs;
    this.autoSaver = new AutoSaver(() -> EdtRead.onEdt(tabs::snapshot), repo, sessions, listener);
    tabs.strip().setRequests(new TabRequests(this::newTab, this::close, this::closeOthers));
  }

  /**
   * Decides how a stored note opens: a password-protected one asks for its password and returns the
   * readable note, or {@code null} to leave it closed.
   */
  void setUnlocker(UnaryOperator<Tab> unlocker) {
    this.unlocker = unlocker;
  }

  void restore() {
    tabs.setDefaultContent(repo.load(Tab.DEFAULT_ID).content());
    autoSaver.markSaved(tabs.snapshotOf(Tab.DEFAULT_ID));
    for (String id : sessions.read()) {
      Tab tab = repo.load(id);
      if (tab.content().isBlank()) {
        repo.delete(id);
      } else if (!NoteCrypto.isEncrypted(tab.content())) {
        open(tab);
      }
    }
  }

  void start() {
    autoSaver.start();
    new ExternalEdits(repo, tabs, autoSaver, tabs.strip()::announce).start();
  }

  void newTab() {
    newTab(Messages.tr("tab.untitled"), "");
  }

  void newTab(String name, String text) {
    tabs.openTab(repo.newId(), name, text);
  }

  void openExisting(String id) {
    if (tabs.openIds().contains(id)) {
      tabs.select(id);
    } else {
      open(repo.load(id));
    }
  }

  void closeCurrent() {
    close(tabs.activeId());
  }

  /** Closes a tab, active or not: its last edits are saved, a blank note is deleted. */
  void close(String id) {
    Tab closed = tabs.close(id);
    if (closed == null) {
      return;
    }
    if (closed.content().isBlank()) {
      deleteFile(closed.id());
    } else {
      autoSaver.save(closed);
    }
  }

  void closeOthers(String keepId) {
    for (String id : tabs.openIds()) {
      if (!id.equals(keepId)) {
        close(id);
      }
    }
    tabs.select(keepId);
  }

  /** Reopens the most recently closed note that still exists; false when there is none. */
  boolean reopenLastClosed() {
    String candidate;
    while ((candidate = tabs.popLastClosed()) != null) {
      String id = candidate;
      if (repo.listMeta().stream().anyMatch(m -> m.id().equals(id))) {
        openExisting(id);
        return true;
      }
    }
    return false;
  }

  /** Moves a note to "Recently deleted"; an open tab showing it is closed first. */
  void delete(String id) {
    Tab closed = tabs.close(id);
    autoSaver.discard(id);
    if (closed != null && !closed.content().isBlank()) {
      repo.save(id, closed.name(), closed.content());
    }
    repo.trash(id);
  }

  /**
   * Final save on exit; may run on the shutdown-hook thread, so it never touches components.
   * Returns whether every note reached the disk.
   */
  boolean persist() {
    autoSaver.flush();
    for (Tab tab : EdtRead.onEdt(tabs::snapshot)) {
      if (!tab.inSession() && !Tab.DEFAULT_ID.equals(tab.id())) {
        deleteFile(tab.id());
      }
    }
    return !autoSaver.isFailing();
  }

  private void deleteFile(String id) {
    autoSaver.discard(id);
    repo.delete(id);
  }

  private void open(Tab stored) {
    Tab tab = unlocker.apply(stored);
    if (tab != null) {
      tabs.openTab(tab.id(), tab.name(), tab.content());
      autoSaver.markSaved(tabs.snapshotOf(tab.id()));
    }
  }
}
