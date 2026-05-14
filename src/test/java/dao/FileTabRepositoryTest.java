package dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.Tab;
import model.TabMeta;

class FileTabRepositoryTest {

  @Test
  void emptyDirHasNoMeta(@TempDir Path tmp) {
    assertEquals(List.of(), new FileTabRepository(tmp).listMeta());
  }

  @Test
  void savedTabRoundTrips(@TempDir Path tmp) {
    FileTabRepository repo = new FileTabRepository(tmp);

    repo.save("abc", "shopping", "milk\nbread");

    Tab loaded = repo.load("abc");
    assertEquals("abc", loaded.id());
    assertEquals("shopping", loaded.name());
    assertEquals("milk\nbread", loaded.content());
  }

  @Test
  void listMetaReflectsSavedFiles(@TempDir Path tmp) {
    FileTabRepository repo = new FileTabRepository(tmp);

    repo.save("id1", "first", "a");
    repo.save("id2", "second", "b");

    List<TabMeta> metas = repo.listMeta();
    assertEquals(2, metas.size());
    assertTrue(metas.stream().anyMatch(m -> m.id().equals("id1") && m.name().equals("first")));
    assertTrue(metas.stream().anyMatch(m -> m.id().equals("id2") && m.name().equals("second")));
  }

  @Test
  void listMetaIgnoresUnrelatedFiles(@TempDir Path tmp) throws Exception {
    FileTabRepository repo = new FileTabRepository(tmp);
    repo.save("kept", "keeper", "x");
    Files.writeString(tmp.resolve("random.txt"), "ignored");
    Files.writeString(tmp.resolve("note_other.md"), "wrong ext");

    assertEquals(1, repo.listMeta().size());
    assertEquals("kept", repo.listMeta().get(0).id());
  }

  @Test
  void loadReturnsEmptyTabForMissingId(@TempDir Path tmp) {
    Tab tab = new FileTabRepository(tmp).load("nope");

    assertEquals("nope", tab.id());
    assertEquals("", tab.name());
    assertEquals("", tab.content());
  }

  @Test
  void saveAllowsSameNameAcrossIds(@TempDir Path tmp) {
    FileTabRepository repo = new FileTabRepository(tmp);

    repo.save("id1", "duplicate", "alpha");
    repo.save("id2", "duplicate", "beta");

    assertEquals("alpha", repo.load("id1").content());
    assertEquals("beta", repo.load("id2").content());
  }

  @Test
  void renameKeepsContentAndChangesNameInPlace(@TempDir Path tmp) {
    FileTabRepository repo = new FileTabRepository(tmp);
    repo.save("the-id", "old", "stuff");

    repo.save("the-id", "new", "stuff");

    assertEquals("new", repo.load("the-id").name());
    assertEquals("stuff", repo.load("the-id").content());
  }

  @Test
  void renameMovesFileOnDisk(@TempDir Path tmp) {
    FileTabRepository repo = new FileTabRepository(tmp);
    repo.save("xyz", "first", "data");

    repo.save("xyz", "second", "data");

    assertTrue(Files.exists(tmp.resolve("note_xyz_second.txt")));
    assertFalse(Files.exists(tmp.resolve("note_xyz_first.txt")));
  }

  @Test
  void sanitizesUnsafeCharsInFileName(@TempDir Path tmp) {
    FileTabRepository repo = new FileTabRepository(tmp);

    repo.save("id1", "with/slash", "data");

    assertTrue(Files.exists(tmp.resolve("note_id1_with_slash.txt")));
    assertEquals("with/slash", repo.load("id1").name());
  }

  @Test
  void newIdReturnsUniqueValues() {
    FileTabRepository repo = new FileTabRepository();

    assertNotEquals(repo.newId(), repo.newId());
  }

  @Test
  void deleteRemovesFile(@TempDir Path tmp) {
    FileTabRepository repo = new FileTabRepository(tmp);
    repo.save("doomed", "x", "x");

    repo.delete("doomed");

    assertEquals(List.of(), repo.listMeta());
  }

  @Test
  void deleteIsIdempotent(@TempDir Path tmp) {
    assertDoesNotThrow(() -> new FileTabRepository(tmp).delete("ghost"));
  }

  @Test
  void defaultConstructorResolvesBaseDir() {
    assertDoesNotThrow(() -> new FileTabRepository().listMeta());
  }
}
