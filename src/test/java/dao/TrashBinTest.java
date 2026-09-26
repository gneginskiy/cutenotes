package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.TabMeta;

class TrashBinTest {

  @Test
  void deletedNotesGoToTheBinAndComeBack(@TempDir Path data) {
    FileTabRepository repo = new FileTabRepository(data);
    repo.save("a", "Alpha", "text");

    repo.trash("a");

    assertTrue(repo.listMeta().isEmpty(), "gone from the notes");
    List<TabMeta> bin = repo.trashBin().list();
    assertEquals(1, bin.size());
    assertEquals("Alpha", bin.get(0).name());
    assertEquals("text", repo.trashBin().load("a").content());
    assertTrue(bin.get(0).lastModified().isAfter(Instant.now().minusSeconds(60)), "dated now");

    repo.trashBin().restore("a");

    assertEquals("text", repo.load("a").content());
    assertTrue(repo.trashBin().list().isEmpty());
  }

  @Test
  void purgingDeletesForGood(@TempDir Path data) throws Exception {
    FileTabRepository repo = new FileTabRepository(data);
    repo.save("old", "Old", "x");
    repo.save("new", "New", "y");
    repo.trash("old");
    repo.trash("new");
    Path oldFile = data.resolve(FileTrashBin.DIR).resolve(NoteFileNames.of("old", "Old"));
    Files.setLastModifiedTime(oldFile, FileTime.from(Instant.now().minus(Duration.ofDays(40))));

    assertEquals(1, repo.trashBin().purgeOlderThan(Instant.now().minus(Duration.ofDays(30))));
    repo.trashBin().purge("new");

    assertTrue(repo.trashBin().list().isEmpty());
  }

  @Test
  void missingNotesAreIgnoredAndInMemoryRepositoriesDelete(@TempDir Path data) {
    FileTabRepository repo = new FileTabRepository(data);
    repo.trash("none");
    repo.trashBin().restore("none");

    TrashBin none = TrashBin.NONE;
    assertTrue(none.list().isEmpty());
    assertEquals("", none.load("x").content());
    none.restore("x");
    none.purge("x");
    assertEquals(0, none.purgeOlderThan(Instant.now()));
  }
}
