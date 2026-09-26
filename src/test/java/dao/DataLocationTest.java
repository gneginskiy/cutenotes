package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataLocationTest {

  @Test
  void rememberedFolderIsReadBackUntilCleared(@TempDir Path home) {
    Path folder = home.resolve("Dropbox/notes");

    assertNull(DataLocation.read(home));
    DataLocation.write(home, folder);
    assertEquals(folder.toAbsolutePath().normalize(), DataLocation.read(home));

    DataLocation.clear(home);
    assertNull(DataLocation.read(home));
  }

  @Test
  void blankOrUnreadablePointerMeansNoChoice(@TempDir Path home) throws Exception {
    Files.writeString(home.resolve(DataLocation.POINTER), "  \n");
    assertNull(DataLocation.read(home));

    Files.delete(home.resolve(DataLocation.POINTER));
    Files.createDirectory(home.resolve(DataLocation.POINTER));
    assertNull(DataLocation.read(home));
  }

  @Test
  void aChosenFolderWinsOverTheDefaultsWhenUsable(@TempDir Path tmp) throws Exception {
    Path chosen = tmp.resolve("sync/notes");
    Path home = tmp.resolve("home/cutenotes_data");

    assertEquals(chosen, DataDir.pick(chosen, true, tmp.resolve("jar/data"), home));

    Path blocked = tmp.resolve("file");
    Files.writeString(blocked, "not a folder");
    assertEquals(home, DataDir.pick(blocked.resolve("x"), true, tmp.resolve("jar/data"), home));
    assertEquals(home, DataDir.pick(null, true, tmp.resolve("jar/data"), home));
  }
}
