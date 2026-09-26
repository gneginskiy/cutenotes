package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import model.AppSettings;

class FileSettingsStoreTest {

  @Test
  void defaultsWhenNothingIsSaved(@TempDir Path tmp) {
    assertEquals(AppSettings.DEFAULT, new FileSettingsStore(tmp.resolve("s.txt")).load());
  }

  @Test
  void roundTrip(@TempDir Path tmp) {
    FileSettingsStore store = new FileSettingsStore(tmp.resolve("s.txt"));
    AppSettings s =
        new AppSettings("ru", true, "Sepia", "Midnight", 72, false, false, 5, "v2026-09-26", 42L);

    store.save(s);

    assertEquals(s, store.load());
    assertEquals(s.withLastSeenVersion("v1").lastSeenVersion(), "v1");
    assertEquals(7L, s.withLastUpdateCheck(7L).lastUpdateCheck());
  }

  @Test
  void brokenValuesFallBackToDefaults(@TempDir Path tmp) throws Exception {
    Path file = tmp.resolve("s.txt");
    Files.writeString(file, "lineWidth=-5\nautoLockMinutes=abc\nlastUpdateCheck=?\n");

    AppSettings s = new FileSettingsStore(file).load();

    assertEquals(0, s.lineWidth());
    assertEquals(AppSettings.DEFAULT.autoLockMinutes(), s.autoLockMinutes());
    assertEquals(0L, s.lastUpdateCheck());
  }
}
