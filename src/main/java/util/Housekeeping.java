package util;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dao.Backups;
import dao.HistoryStore;
import dao.ImageStore;
import dao.TabRepository;
import dao.TrashBin;
import model.TabMeta;

/**
 * Background chores at startup: the daily backup, emptying notes deleted over a month ago and
 * removing images nothing refers to. Each step is independent; a failing one is logged and skipped.
 */
public final class Housekeeping {

  /** How long "Recently deleted" keeps a note. */
  public static final Duration TRASH_DAYS = Duration.ofDays(30);

  private static final Logger LOG = Logger.getLogger(Housekeeping.class.getName());

  private Housekeeping() {}

  public static void run(Path dataDir, TabRepository repo, HistoryStore history, Instant now) {
    step("backup", () -> Backups.backupIfDue(dataDir, LocalDate.ofInstant(now, ZoneId.of("UTC"))));
    step("trash", () -> repo.trashBin().purgeOlderThan(now.minus(TRASH_DAYS)));
    step("images", () -> ImageGc.run(new ImageStore(dataDir), allTexts(repo, history), now));
  }

  /** Every text an image may be referenced from: the notes, the trash and the history. */
  static List<String> allTexts(TabRepository repo, HistoryStore history) {
    List<String> texts = new ArrayList<>();
    for (TabMeta meta : repo.listMeta()) {
      texts.add(repo.load(meta.id()).content());
    }
    TrashBin bin = repo.trashBin();
    for (TabMeta meta : bin.list()) {
      texts.add(bin.load(meta.id()).content());
    }
    texts.addAll(history.allTexts());
    return texts;
  }

  private static void step(String name, Chore chore) {
    try {
      chore.run();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "housekeeping step failed: " + name, e);
    }
  }

  @FunctionalInterface
  private interface Chore {
    Object run() throws Exception;
  }
}
