package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
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

  @Test
  void closingAnInactiveTabFromTheBarSavesItAndKeepsTheActiveOne() throws Exception {
    String[] ids = new String[2];
    onEdt(
        () -> {
          TabsPane tabs = new TabsPane();
          NoteSession session = new NoteSession(repo, sessions, tabs, f -> {});
          session.newTab();
          ids[0] = tabs.activeId();
          tabs.activeArea().setMarkdown("first note");
          session.newTab();
          ids[1] = tabs.activeId();

          tabs.strip().header().actions().close().accept(ids[0]);

          assertEquals(List.of(ids[1]), tabs.openIds());
          assertEquals(ids[1], tabs.activeId(), "closing another tab must not switch tabs");
          session.persist();
        });

    assertEquals("first note", repo.load(ids[0]).content());
  }

  @Test
  void closeOtherTabsKeepsOnlyTheChosenOne() throws Exception {
    String[] ids = new String[3];
    onEdt(
        () -> {
          TabsPane tabs = new TabsPane();
          NoteSession session = new NoteSession(repo, sessions, tabs, f -> {});
          for (int i = 0; i < 3; i++) {
            session.newTab();
            ids[i] = tabs.activeId();
            tabs.activeArea().setMarkdown(i == 2 ? "" : "note " + i);
          }

          tabs.strip().header().actions().closeOthers().accept(ids[1]);

          assertEquals(List.of(ids[1]), tabs.openIds());
          assertEquals(ids[1], tabs.activeId());
          session.persist();
        });

    assertEquals("note 0", repo.load(ids[0]).content());
    assertNull(repo.byId.get(ids[2]), "a blank tab closed this way is deleted, as with Cmd+W");
  }

  @Test
  void barButtonsAndRenameReachTheSession() throws Exception {
    onEdt(
        () -> {
          TabsPane tabs = new TabsPane();
          NoteSession session = new NoteSession(repo, sessions, tabs, f -> {});
          tabs.strip().header().actions().newTab().run();
          String id = tabs.activeId();
          tabs.activeArea().setMarkdown("text");

          TabHeader header = tabs.strip().header();
          header.startRename(id);
          assertTrue(header.isEditing());
          JTextField field = findField(header);
          field.setText("  Shopping list ");
          field.postActionEvent();

          assertFalse(header.isEditing());
          assertEquals("Shopping list", header.titleOf(id));
          assertEquals("Shopping list", tabs.snapshotOf(id).name());
        });
  }

  @Test
  void reopenReportsWhetherThereWasAnythingToReopen() throws Exception {
    onEdt(
        () -> {
          TabsPane tabs = new TabsPane();
          NoteSession session = new NoteSession(repo, sessions, tabs, f -> {});
          assertFalse(session.reopenLastClosed());

          session.newTab();
          tabs.activeArea().setMarkdown("kept");
          session.closeCurrent();

          assertTrue(session.reopenLastClosed());
          assertEquals("kept", tabs.activeArea().markdown());
        });
  }

  @Test
  void closingTheLastTabHidesTheTabsBar() throws Exception {
    onEdt(
        () -> {
          TabsPane tabs = new TabsPane();
          NoteSession session = new NoteSession(repo, sessions, tabs, f -> {});
          session.newTab();
          tabs.revealer().show();

          session.closeCurrent();

          assertFalse(tabs.revealer().isShown());
        });
  }

  private static JTextField findField(Container c) {
    for (Component child : c.getComponents()) {
      if (child instanceof JTextField f) {
        return f;
      }
      if (child instanceof Container nested && findField(nested) != null) {
        return findField(nested);
      }
    }
    return null;
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
