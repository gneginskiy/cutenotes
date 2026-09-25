package dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileBoundsStoreTest {

  @Test
  void savedBoundsAreLoadedBack(@TempDir Path tmp) {
    FileBoundsStore store = new FileBoundsStore(tmp.resolve("window.txt"));
    Rectangle bounds = new Rectangle(-1200, 40, 640, 480);

    store.save(bounds);

    assertEquals(bounds, store.load());
  }

  @Test
  void nothingSavedYieldsNull(@TempDir Path tmp) {
    assertNull(new FileBoundsStore(tmp.resolve("missing.txt")).load());
  }

  @Test
  void corruptContentYieldsNull(@TempDir Path tmp) throws Exception {
    Path file = tmp.resolve("window.txt");
    for (String bad : new String[] {"", "1,2,3", "a,b,c,d", "1,2,0,50", "1,2,50,-3", "1,2,3,4,5"}) {
      Files.writeString(file, bad);
      assertNull(new FileBoundsStore(file).load(), bad);
    }
  }

  @Test
  void toleratesSpacesAndTrailingNewline(@TempDir Path tmp) throws Exception {
    Path file = tmp.resolve("window.txt");
    Files.writeString(file, " 10, 20 ,300,200\n");

    assertEquals(new Rectangle(10, 20, 300, 200), new FileBoundsStore(file).load());
  }

  @Test
  void unreadableOrUnwritableLocationNeverThrows(@TempDir Path tmp) throws Exception {
    Path dir = tmp.resolve("window.txt");
    Files.createDirectories(dir);
    Files.writeString(dir.resolve("occupied"), "x");
    FileBoundsStore store = new FileBoundsStore(dir);

    assertNull(store.load());
    assertDoesNotThrow(() -> store.save(new Rectangle(0, 0, 400, 300)));
  }
}
