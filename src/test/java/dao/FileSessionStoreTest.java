package dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSessionStoreTest {

  @Test
  void readsEmptyWhenMissing(@TempDir Path tmp) {
    assertEquals(List.of(), new FileSessionStore(tmp.resolve("s.txt")).read());
  }

  @Test
  void roundTrip(@TempDir Path tmp) {
    FileSessionStore store = new FileSessionStore(tmp.resolve("s.txt"));

    store.write(List.of("a", "b", "c"));

    assertEquals(List.of("a", "b", "c"), store.read());
  }

  @Test
  void skipsBlankLines(@TempDir Path tmp) throws Exception {
    Path file = tmp.resolve("s.txt");
    Files.writeString(file, "a\n\nb\n\n");

    assertEquals(List.of("a", "b"), new FileSessionStore(file).read());
  }

  @Test
  void defaultConstructorReadsWithoutError() {
    assertDoesNotThrow(() -> new FileSessionStore().read());
  }
}
