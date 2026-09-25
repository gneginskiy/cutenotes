package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import dao.SessionStore;
import model.Tab;
import util.RecordingRepo;

class NoteSessionTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  private final RecordingRepo repo = new RecordingRepo();
  private final MemorySessions sessions = new MemorySessions();

  @Test
  void restoringTheSessionDoesNotRewriteNotes() throws Exception {
    repo.save("a", "Alpha", "2\\*3 **bold**");
    repo.save(Tab.DEFAULT_ID, "default", "scratch");
    sessions.ids = List.of("a");
    repo.writeCount.clear();

    onEdt(
        () -> {
          NoteSession session = new NoteSession(repo, sessions, new TabsPane(), f -> {});
          session.restore();
          session.persist();
        });

    assertTrue(repo.writeCount.isEmpty(), "unchanged notes must keep their files/timestamps");
  }

  @Test
  void clearedScratchAreaStaysClearedAfterRestart() throws Exception {
    repo.save(Tab.DEFAULT_ID, "default", "sensitive text");

    onEdt(
        () -> {
          TabsPane tabs = new TabsPane();
          NoteSession session = new NoteSession(repo, sessions, tabs, f -> {});
          session.restore();
          tabs.setDefaultContent("");
          session.persist();
        });

    assertEquals("", repo.load(Tab.DEFAULT_ID).content());
  }

  @Test
  void deletingAnOpenNoteClosesItsTabAndItStaysDeleted() throws Exception {
    repo.save("a", "Alpha", "text");
    sessions.ids = List.of("a");

    onEdt(
        () -> {
          TabsPane tabs = new TabsPane();
          NoteSession session = new NoteSession(repo, sessions, tabs, f -> {});
          session.restore();
          tabs.activeArea().setMarkdown("edited right before delete");

          session.delete("a");
          session.persist();

          assertTrue(tabs.openIds().isEmpty());
        });

    assertFalse(repo.byId.containsKey("a"));
    assertEquals(List.of(), sessions.ids);
  }

  @Test
  void closingABlankTabDeletesItAndItCannotBeReopened() throws Exception {
    onEdt(
        () -> {
          TabsPane tabs = new TabsPane();
          NoteSession session = new NoteSession(repo, sessions, tabs, f -> {});
          session.newTab();
          String id = tabs.openIds().get(0);
          session.closeCurrent();
          session.reopenLastClosed();

          assertTrue(tabs.openIds().isEmpty());
          assertNull(repo.byId.get(id));
        });
  }

  @Test
  void closingATabSavesItsLastEditsAndReopenBringsThemBack() throws Exception {
    onEdt(
        () -> {
          TabsPane tabs = new TabsPane();
          NoteSession session = new NoteSession(repo, sessions, tabs, f -> {});
          session.newTab();
          tabs.activeArea().setMarkdown("typed");
          session.closeCurrent();
          session.reopenLastClosed();

          assertEquals("typed", tabs.activeArea().markdown());
        });
  }

  @Test
  void openingAnAlreadyOpenNoteKeepsItsUnsavedEdits() throws Exception {
    repo.save("a", "Alpha", "on disk");
    sessions.ids = List.of("a");

    onEdt(
        () -> {
          TabsPane tabs = new TabsPane();
          NoteSession session = new NoteSession(repo, sessions, tabs, f -> {});
          session.restore();
          tabs.activeArea().setMarkdown("unsaved edit");
          session.openExisting("a");
          session.persist();
        });

    assertEquals("unsaved edit", repo.load("a").content());
  }

  private static void onEdt(Runnable r) throws Exception {
    SwingUtilities.invokeAndWait(r);
  }

  private static final class MemorySessions implements SessionStore {
    List<String> ids = new ArrayList<>();

    @Override
    public List<String> read() {
      return ids;
    }

    @Override
    public void write(List<String> tabIds) {
      ids = tabIds;
    }
  }
}
