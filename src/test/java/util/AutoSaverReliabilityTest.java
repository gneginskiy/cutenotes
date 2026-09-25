package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import dao.SessionStore;
import model.Tab;

class AutoSaverReliabilityTest {

  private final RecordingRepo repo = new RecordingRepo();
  private final List<Exception> statuses = new ArrayList<>();
  private final RecordingSessions sessions = new RecordingSessions();
  private final AtomicReference<List<Tab>> source = new AtomicReference<>(List.of());
  private final AutoSaver saver = new AutoSaver(source::get, repo, sessions, statuses::add);

  @Test
  void oneUnsavableTabDoesNotBlockTheOthers() {
    repo.failing = Set.of("bad");
    source.set(List.of(new Tab("bad", "n", "x"), new Tab("good", "n", "y")));

    saver.tick();

    assertTrue(saver.isFailing());
    assertEquals("y", repo.byId.get("good").content());
  }

  @Test
  void failureIsReportedOnceAndRecoveryIsReported() {
    repo.failing = Set.of("a");
    source.set(List.of(new Tab("a", "n", "x")));

    saver.tick();
    saver.tick();
    repo.failing = Set.of();
    saver.tick();

    assertEquals(2, statuses.size());
    assertFalse(saver.isFailing());
    assertNotNull(statuses.get(0));
    assertNull(statuses.get(1));
    assertEquals("x", repo.byId.get("a").content(), "failed save is retried");
  }

  @Test
  void tabLoadedFromDiskIsNotRewrittenOnStartup() {
    Tab loaded = new Tab("a", "n", "from disk");
    saver.markSaved(loaded);
    source.set(List.of(loaded));

    saver.tick();

    assertNull(repo.writeCount.get("a"));
  }

  @Test
  void clearingAPreviouslySavedNoteIsPersisted() {
    saver.markSaved(new Tab(Tab.DEFAULT_ID, "default", "secret"));
    source.set(List.of(new Tab(Tab.DEFAULT_ID, "default", "")));

    saver.tick();

    assertEquals("", repo.byId.get(Tab.DEFAULT_ID).content());
  }

  @Test
  void brandNewEmptyTabIsNotWritten() {
    source.set(List.of(new Tab("fresh", "untitled", "")));

    saver.tick();

    assertFalse(repo.byId.containsKey("fresh"));
  }

  @Test
  void discardedNoteIsNotResurrectedByAStaleSnapshot() {
    source.set(List.of(new Tab("gone", "n", "edited just before delete")));

    saver.discard("gone");
    saver.tick();

    assertFalse(repo.byId.containsKey("gone"));
    assertEquals(List.of(), sessions.last);
  }

  @Test
  void sessionIsWrittenWhenOpenTabsChangeAndOnlyThen() {
    source.set(
        List.of(
            new Tab("a", "n", "x"), new Tab("empty", "n", " "), new Tab(Tab.DEFAULT_ID, "d", "z")));

    saver.tick();
    saver.tick();

    assertEquals(List.of("a"), sessions.last);
    assertEquals(1, sessions.writes);
  }

  @Test
  void sessionWriteFailureIsReported() {
    sessions.fail = true;
    source.set(List.of(new Tab("a", "n", "x")));

    saver.tick();

    assertEquals(1, statuses.size());
    assertTrue(statuses.get(0).getMessage().contains("disk"));
  }

  @Test
  void explicitSaveWritesOnlyWhenChangedAndReportsFailure() {
    Tab tab = new Tab("a", "n", "x");
    saver.save(tab);
    saver.save(tab);
    assertEquals(1, repo.writeCount.get("a"));

    repo.failing = Set.of("b");
    assertThrows(IllegalStateException.class, () -> saver.save(new Tab("b", "n", "y")));
    assertEquals(1, statuses.size());
  }

  private static final class RecordingSessions implements SessionStore {
    List<String> last;
    int writes;
    boolean fail;

    @Override
    public List<String> read() {
      return last == null ? List.of() : last;
    }

    @Override
    public void write(List<String> ids) {
      if (fail) {
        throw new IllegalStateException("disk full");
      }
      last = ids;
      writes++;
    }
  }
}
