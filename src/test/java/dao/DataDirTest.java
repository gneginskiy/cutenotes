package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataDirTest {

  @Test
  void prefersTheFolderNextToTheJar(@TempDir Path tmp) {
    Path preferred = tmp.resolve("jar/cutenotes_data");

    assertEquals(preferred, DataDir.choose(preferred, tmp.resolve("home/cutenotes_data")));
    assertTrue(Files.isDirectory(preferred));
  }

  @Test
  void fallsBackWhenTheJarFolderIsNotUsable(@TempDir Path tmp) throws Exception {
    Path blocker = tmp.resolve("jar");
    Files.writeString(blocker, "a file where the jar folder should be");
    Path fallback = tmp.resolve("home/cutenotes_data");

    assertEquals(fallback, DataDir.choose(blocker.resolve("cutenotes_data"), fallback));
    assertTrue(Files.isDirectory(fallback));
  }

  @Test
  void anInstalledAppKeepsNotesInTheHomeFolderNotInsideItself(@TempDir Path tmp) {
    // Replacing cuteNotes.app (or the app folder) on upgrade must not delete the notes.
    Path insideApp = tmp.resolve("cuteNotes.app/Contents/app/cutenotes_data");
    Path home = tmp.resolve("home/cutenotes_data");

    assertEquals(home, DataDir.pick(true, insideApp, home));
    assertTrue(Files.isDirectory(home));
    assertFalse(Files.exists(insideApp));
  }

  @Test
  void theBareJarStaysPortable(@TempDir Path tmp) {
    Path nextToJar = tmp.resolve("jar/cutenotes_data");

    assertEquals(nextToJar, DataDir.pick(false, nextToJar, tmp.resolve("home/cutenotes_data")));
  }

  @Test
  void resolveIsCached() {
    assertSame(DataDir.resolve(), DataDir.resolve());
  }
}
