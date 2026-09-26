package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackupsTest {

  private static final LocalDate DAY = LocalDate.of(2026, 9, 26);

  @Test
  void zipsTheNotesOncePerDayWithoutBackupsLogsOrLocks(@TempDir Path data) throws Exception {
    Files.writeString(data.resolve("note_a_A.txt"), "A\ntext");
    Files.createDirectories(data.resolve("images"));
    Files.writeString(data.resolve("images/x.png"), "png");
    Files.createDirectories(data.resolve("logs"));
    Files.writeString(data.resolve("logs/cutenotes-0.log"), "log");
    Files.writeString(data.resolve(".lock"), "");

    Path zip = Backups.backupIfDue(data, DAY);

    assertNotNull(zip);
    assertEquals(List.of("images/x.png", "note_a_A.txt"), entries(zip));
    assertNull(Backups.backupIfDue(data, DAY), "one backup a day");
  }

  @Test
  void keepsTheNewestSeven(@TempDir Path data) throws Exception {
    Files.writeString(data.resolve("note_a_A.txt"), "A\ntext");
    for (int i = 0; i < Backups.KEEP + 3; i++) {
      Backups.backupIfDue(data, DAY.plusDays(i));
    }

    List<String> zips;
    try (var files = Files.list(data.resolve(Backups.DIR))) {
      zips = files.map(f -> f.getFileName().toString()).sorted().toList();
    }
    assertEquals(Backups.KEEP, zips.size());
    assertTrue(zips.get(0).contains(DAY.plusDays(3).toString()));
    assertNull(Backups.backupIfDue(data.resolve("missing"), DAY));
  }

  private static List<String> entries(Path zip) throws Exception {
    List<String> names = new ArrayList<>();
    try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
      for (ZipEntry e = in.getNextEntry(); e != null; e = in.getNextEntry()) {
        names.add(e.getName());
      }
    }
    names.sort(String::compareTo);
    return names;
  }
}
