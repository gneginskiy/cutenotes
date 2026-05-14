package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.Theme;

class FileOptionsStoreTest {

  @Test
  void loadReturnsDefaultWhenMissing(@TempDir Path tmp) {
    FileOptionsStore store = new FileOptionsStore(tmp.resolve("opts.txt"));

    assertEquals(Theme.DEFAULT, store.load());
  }

  @Test
  void saveAndLoadRoundTrip(@TempDir Path tmp) {
    FileOptionsStore store = new FileOptionsStore(tmp.resolve("opts.txt"));
    Theme custom =
        new Theme(
            new Color(10, 20, 30),
            new Color(200, 210, 220),
            new Color(50, 60, 70),
            "Serif",
            18,
            "My Notes",
            false);

    store.save(custom);

    assertEquals(custom, store.load());
  }

  @Test
  void loadFallsBackToDefaultOnBadValues(@TempDir Path tmp) throws Exception {
    Path file = tmp.resolve("opts.txt");
    java.nio.file.Files.writeString(file, "backgroundColor=garbage\nfontSize=notNumber\n");
    FileOptionsStore store = new FileOptionsStore(file);

    Theme loaded = store.load();
    assertEquals(Theme.DEFAULT.bg(), loaded.bg());
    assertEquals(Theme.DEFAULT.fontSize(), loaded.fontSize());
  }
}
