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

  @Test
  void veryLongNameIsStillSaved(@TempDir Path tmp) {
    FileTabRepository repo = new FileTabRepository(tmp);
    String longName = "Очень длинное название заметки ".repeat(12);

    repo.save("id1", longName, "body");

    assertEquals(longName, repo.load("id1").name());
    assertEquals("body", repo.load("id1").content());
    assertEquals("id1", repo.listMeta().get(0).id());
  }

  @Test
  void fileNamePartIsCappedByBytesWithoutSplittingCharacters() {
    String part = NoteFileNames.namePart("😀".repeat(100));

    assertTrue(
        part.getBytes(java.nio.charset.StandardCharsets.UTF_8).length
            <= NoteFileNames.MAX_NAME_BYTES);
    assertEquals(0, part.length() % 2, "surrogate pairs must stay whole");
  }

  @Test
  void controlCharactersAreSanitized(@TempDir Path tmp) {
    FileTabRepository repo = new FileTabRepository(tmp);

    repo.save("id1", "a\tb", "x");

    assertTrue(Files.exists(tmp.resolve("note_id1_a_b.txt")));
  }

  @Test
  void idContainingUnderscoresIsListedCorrectly(@TempDir Path tmp) {
    FileTabRepository repo = new FileTabRepository(tmp);

    repo.save("__default__", "default", "scratch");

    assertEquals("__default__", repo.listMeta().get(0).id());
    assertEquals("scratch", repo.load("__default__").content());
  }

  @Test
  void legacyFileNameStillYieldsItsId(@TempDir Path tmp) throws Exception {
    Files.writeString(tmp.resolve("note_abc_old name.txt"), "renamed since\nbody");

    assertEquals("abc", new FileTabRepository(tmp).listMeta().get(0).id());
  }

  @Test
  void saveIsAtomicAndLeavesNoTempFiles(@TempDir Path tmp) throws Exception {
    FileTabRepository repo = new FileTabRepository(tmp);
    repo.save("id1", "n", "one");
    repo.save("id1", "n", "two");

    try (var files = Files.list(tmp)) {
      assertEquals(1, files.count());
    }
    assertEquals("two", repo.load("id1").content());
  }
}
