package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataFilesTest {

  @Test
  void copiesNotesImagesAndHistoryButKeepsWhatIsAlreadyThere(@TempDir Path tmp) throws Exception {
    Path from = tmp.resolve("from");
    Path to = tmp.resolve("to");
    Files.createDirectories(from.resolve("images"));
    Files.createDirectories(from.resolve("logs"));
    Files.writeString(from.resolve("note_a_A.txt"), "A");
    Files.writeString(from.resolve("note_b_B.txt"), "B");
    Files.writeString(from.resolve("images/x.png"), "png");
    Files.writeString(from.resolve("logs/l.log"), "log");
    Files.writeString(from.resolve(".lock"), "");
    Files.writeString(from.resolve("half.tmp"), "");
    Files.createDirectories(to);
    Files.writeString(to.resolve("note_b_B.txt"), "B there");

    assertEquals(2, DataFiles.copy(from, to));

    assertEquals("A", Files.readString(to.resolve("note_a_A.txt")));
    assertEquals("B there", Files.readString(to.resolve("note_b_B.txt")));
    assertTrue(Files.exists(to.resolve("images/x.png")));
    assertFalse(Files.exists(to.resolve("logs")));
    assertFalse(Files.exists(to.resolve(".lock")));
    assertEquals(0, DataFiles.copy(tmp.resolve("missing"), to));
  }
}
