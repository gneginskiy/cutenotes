package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NoteStorageTest {

  @Test
  void savesAreRecordedAndProtectedTextNeverReachesTheHistory(@TempDir Path data) throws Exception {
    NoteStorage storage = NoteStorage.open(data);

    storage.repo().save("a", "A", "plain");
    storage.repo().protect("b", "B", "secret", NoteCrypto.newKey("pw".toCharArray()));
    storage.housekeepInBackground().join();

    assertEquals(1, storage.history().versions("a").size());
    assertTrue(storage.history().versions("b").isEmpty());
    assertEquals("secret", storage.repo().load("b").content());
    assertTrue(Files.isDirectory(data.resolve("backups")));
  }
}
