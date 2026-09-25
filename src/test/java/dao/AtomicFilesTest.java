package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AtomicFilesTest {

  @Test
  void writesContentAndCreatesMissingParents(@TempDir Path tmp) throws Exception {
    Path target = tmp.resolve("a/b/file.txt");

    AtomicFiles.write(target, "привет");

    assertEquals("привет", Files.readString(target, StandardCharsets.UTF_8));
  }

  @Test
  void replacesExistingContentAndLeavesNoTempFiles(@TempDir Path tmp) throws Exception {
    Path target = tmp.resolve("file.txt");
    Files.writeString(target, "old old old old");

    AtomicFiles.write(target, "new");

    assertEquals("new", Files.readString(target));
    try (Stream<Path> files = Files.list(tmp)) {
      assertEquals(1, files.count(), "temp file must not be left behind");
    }
  }

  @Test
  void failedWriteKeepsThePreviousFileIntact(@TempDir Path tmp) throws Exception {
    Path target = tmp.resolve("file.txt");
    Files.writeString(target, "precious");
    Files.createDirectories(tmp.resolve("dir-in-the-way"));

    // the replacement cannot land on a non-empty directory: the write fails as a whole
    Files.writeString(tmp.resolve("dir-in-the-way/child"), "x");
    assertThrows(Exception.class, () -> AtomicFiles.write(tmp.resolve("dir-in-the-way"), "y"));

    assertEquals("precious", Files.readString(target));
    try (Stream<Path> files = Files.list(tmp)) {
      assertEquals(2, files.count(), "temp file must be cleaned up after a failure");
    }
  }
}
