package view;

import dao.SessionStore;
import dao.TabRepository;
import model.Tab;
import util.AutoSaver;
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

  NoteSession(TabRepository repo, SessionStore sessions, TabsPane tabs, SaveListener listener) {
    this.repo = repo;
    this.sessions = sessions;
    this.tabs = tabs;
    this.autoSaver = new AutoSaver(() -> EdtRead.onEdt(tabs::snapshot), repo, sessions, listener);
  }

  void restore() {
    tabs.setDefaultContent(repo.load(Tab.DEFAULT_ID).content());
    autoSaver.markSaved(tabs.snapshotOf(Tab.DEFAULT_ID));
    for (String id : sessions.read()) {
      Tab tab = repo.load(id);
      if (tab.content().isBlank()) {
        repo.delete(id);
      } else {
        open(tab);
      }
    }
  }

  void start() {
    autoSaver.start();
  }

  void newTab() {
    tabs.openTab(repo.newId(), "untitled", "");
  }

  void openExisting(String id) {
    if (tabs.openIds().contains(id)) {
      tabs.select(id);
    } else {
      open(repo.load(id));
    }
  }

  void closeCurrent() {
    Tab closed = tabs.closeCurrent();
    if (closed == null) {
      return;
    }
    if (closed.content().isBlank()) {
      deleteFile(closed.id());
    } else {
      autoSaver.save(closed);
    }
  }

  void reopenLastClosed() {
    String candidate;
    while ((candidate = tabs.popLastClosed()) != null) {
      String id = candidate;
      if (repo.listMeta().stream().anyMatch(m -> m.id().equals(id))) {
        openExisting(id);
        return;
      }
    }
  }

  /** Deletes a note for good; an open tab showing it is closed instead of lingering unsaved. */
  void delete(String id) {
    tabs.close(id);
    deleteFile(id);
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

  private void open(Tab tab) {
    tabs.openTab(tab.id(), tab.name(), tab.content());
    autoSaver.markSaved(tabs.snapshotOf(tab.id()));
  }
}
