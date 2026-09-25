package util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import model.Tab;

class AutoSaverTest {

  @Test
  void persistsEachTabOnFirstTick() {
    RecordingRepo repo = new RecordingRepo();
    AutoSaver saver =
        new AutoSaver(
            () -> List.of(new Tab("a", "Alpha", "AA"), new Tab("b", "Bravo", "BB")), repo);

    saver.tick();

    assertEquals("AA", repo.byId.get("a").content());
    assertEquals("BB", repo.byId.get("b").content());
  }

  @Test
  void skipsUnchangedTabs() {
    RecordingRepo repo = new RecordingRepo();
    AutoSaver saver = new AutoSaver(() -> List.of(new Tab("a", "n", "x")), repo);

    saver.tick();
    saver.tick();
    saver.tick();

    assertEquals(1, repo.writeCount.get("a"));
  }

  @Test
  void persistsAgainOnContentChange() {
    RecordingRepo repo = new RecordingRepo();
    AtomicReference<List<Tab>> source = new AtomicReference<>(List.of(new Tab("a", "n", "1")));
    AutoSaver saver = new AutoSaver(source::get, repo);

    saver.tick();
    source.set(List.of(new Tab("a", "n", "2")));
    saver.tick();

    assertEquals(2, repo.writeCount.get("a"));
    assertEquals("2", repo.byId.get("a").content());
  }

  @Test
  void persistsAgainOnNameChange() {
    RecordingRepo repo = new RecordingRepo();
    AtomicReference<List<Tab>> source = new AtomicReference<>(List.of(new Tab("a", "old", "x")));
    AutoSaver saver = new AutoSaver(source::get, repo);

    saver.tick();
    source.set(List.of(new Tab("a", "new", "x")));
    saver.tick();

    assertEquals("new", repo.byId.get("a").name());
  }

  @Test
  void tickSurvivesSupplierFailure() {
    RecordingRepo repo = new RecordingRepo();
    AtomicReference<List<Tab>> source = new AtomicReference<>(null);
    AutoSaver saver =
        new AutoSaver(
            () -> {
              List<Tab> tabs = source.get();
              if (tabs == null) {
                throw new IllegalStateException("document read race");
              }
              return tabs;
            },
            repo);

    assertDoesNotThrow(saver::tick);

    source.set(List.of(new Tab("a", "n", "recovered")));
    saver.tick();
    assertEquals("recovered", repo.byId.get("a").content());
  }

  @Test
  void flushDelegatesToTick() {
    RecordingRepo repo = new RecordingRepo();
    AutoSaver saver = new AutoSaver(() -> List.of(new Tab("a", "n", "z")), repo);

    saver.flush();

    assertEquals("z", repo.byId.get("a").content());
  }
}
