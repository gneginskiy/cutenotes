package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.Tab;

class HistoryStoreTest {

  private static final Instant T0 = Instant.parse("2026-09-26T10:00:00Z");

  @Test
  void snapshotsAtMostEveryTenMinutesAndOnlyOnChange(@TempDir Path data) {
    HistoryStore history = new HistoryStore(data);

    history.snapshot("a", "one", T0, false);
    history.snapshot("a", "two", T0.plus(Duration.ofMinutes(5)), false);
    history.snapshot("a", "one", T0.plus(Duration.ofMinutes(15)), false);
    history.snapshot("a", "three", T0.plus(Duration.ofMinutes(20)), false);
    history.snapshot("a", "forced", T0.plus(Duration.ofMinutes(21)), true);

    List<Instant> versions = history.versions("a");
    assertEquals(3, versions.size());
    assertEquals("forced", history.load("a", versions.get(0)));
    assertEquals("one", history.load("a", versions.get(2)));
    assertEquals("", history.load("a", T0.minusSeconds(1)));
  }

  @Test
  void keepsTheNewestFiftyAndForgetsOnRequest(@TempDir Path data) throws Exception {
    HistoryStore history = new HistoryStore(data);
    for (int i = 0; i < HistoryStore.KEEP + 5; i++) {
      history.snapshot("a", "v" + i, T0.plus(Duration.ofMinutes(11L * i)), false);
    }
    Files.writeString(data.resolve("history/a/junk.txt"), "not a version");

    assertEquals(HistoryStore.KEEP, history.versions("a").size());
    assertEquals(HistoryStore.KEEP, history.allTexts().size());

    history.deleteAll("a");
    assertTrue(history.versions("a").isEmpty());
    history.deleteAll("never");
    assertTrue(new HistoryStore(data.resolve("none")).allTexts().isEmpty());
  }

  @Test
  void theHistoryRepositoryRecordsSavesButNotTheScratchArea(@TempDir Path data) {
    HistoryStore history = new HistoryStore(data);
    Clock clock = Clock.fixed(T0, ZoneOffset.UTC);
    HistoryRepository repo = new HistoryRepository(new FileTabRepository(data), history, clock);

    repo.save("a", "A", "text");
    repo.save(Tab.DEFAULT_ID, "default", "scratch");
    repo.save("b", "B", "  ");
    repo.save("c", "C", util.NoteCrypto.PREFIX + "secret");

    assertEquals(1, history.versions("a").size());
    assertTrue(history.versions(Tab.DEFAULT_ID).isEmpty());
    assertTrue(history.versions("b").isEmpty());
    assertTrue(history.versions("c").isEmpty(), "protected text is never recorded");
    assertEquals("text", repo.load("a").content());
    assertEquals(4, repo.listMeta().size());
    assertTrue(repo.newId().length() > 10);
    assertEquals(history, repo.history());

    repo.trash("a");
    assertEquals(1, repo.trashBin().list().size());
    repo.delete("b");
    assertEquals(1, history.versions("a").size(), "a deleted-to-bin note keeps its history");
  }

  @Test
  void repositoriesWithoutHistoryKeepNothing(@TempDir Path data) {
    NoteHistory none = new FileTabRepository(data).history();

    none.snapshot("a", "text", T0, true);
    none.deleteAll("a");

    assertTrue(none.versions("a").isEmpty());
    assertEquals("", none.load("a", T0));
  }
}
