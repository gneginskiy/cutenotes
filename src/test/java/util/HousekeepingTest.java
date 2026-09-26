package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dao.FileTabRepository;
import dao.HistoryStore;
import dao.ImageStore;

class HousekeepingTest {

  @Test
  void backsUpEmptiesOldTrashAndKeepsImagesReferencedAnywhere(@TempDir Path data) throws Exception {
    ImageStore images = new ImageStore(data);
    String inNote = images.save(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));
    String inTrash = images.save(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));
    String inHistory = images.save(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));
    String orphan = images.save(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));
    for (String p : List.of(inNote, inTrash, inHistory, orphan)) {
      Files.setLastModifiedTime(
          images.resolve(p), FileTime.from(Instant.now().minus(Duration.ofDays(3))));
    }
    FileTabRepository repo = new FileTabRepository(data);
    HistoryStore history = new HistoryStore(data);
    repo.save("a", "A", "![](" + inNote + ")");
    repo.save("t", "T", "![](" + inTrash + ")");
    repo.trash("t");
    history.snapshot("h", "![](" + inHistory + ")", Instant.now(), true);

    Housekeeping.run(data, repo, history, Instant.now());

    assertTrue(Files.exists(images.resolve(inNote)));
    assertTrue(Files.exists(images.resolve(inTrash)));
    assertTrue(Files.exists(images.resolve(inHistory)));
    assertFalse(Files.exists(images.resolve(orphan)));
    try (Stream<Path> zips = Files.list(data.resolve("backups"))) {
      assertEquals(1, zips.count());
    }

    Housekeeping.run(data, repo, history, Instant.now().plus(Duration.ofDays(31)));
    assertTrue(repo.trashBin().list().isEmpty(), "a month later the bin is emptied");
  }

  @Test
  void aFailingStepDoesNotStopTheOthers(@TempDir Path data) throws Exception {
    Path file = data.resolve("not-a-folder");
    Files.writeString(file, "x");

    Housekeeping.run(file, new FileTabRepository(data), new HistoryStore(file), Instant.now());

    assertEquals("x", Files.readString(file));
  }
}
