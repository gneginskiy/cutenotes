package dao;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A daily zip of the whole notes folder in {@code <data>/backups} (the newest seven are kept), so
 * even an accident outside the app — a sync gone wrong, a bad edit elsewhere — is recoverable.
 */
public final class Backups {

  static final String DIR = "backups";
  static final int KEEP = 7;

  private Backups() {}

  /** Writes today's backup unless it exists; returns the file written, or null. */
  public static Path backupIfDue(Path dataDir, LocalDate today) throws IOException {
    Path dir = dataDir.resolve(DIR);
    Path target = dir.resolve("cutenotes-" + today + ".zip");
    if (Files.exists(target) || !Files.isDirectory(dataDir)) {
      return null;
    }
    Files.createDirectories(dir);
    Path tmp = dir.resolve(target.getFileName() + ".tmp");
    try (OutputStream out = Files.newOutputStream(tmp);
        ZipOutputStream zip = new ZipOutputStream(out)) {
      for (Path file : DataFiles.of(dataDir)) {
        zip.putNextEntry(new ZipEntry(dataDir.relativize(file).toString().replace('\\', '/')));
        Files.copy(file, zip);
        zip.closeEntry();
      }
    }
    Files.move(tmp, target);
    prune(dir);
    return target;
  }

  private static void prune(Path dir) throws IOException {
    List<Path> zips;
    try (Stream<Path> all = Files.list(dir)) {
      zips = all.filter(f -> f.getFileName().toString().endsWith(".zip")).sorted().toList();
    }
    for (int i = 0; i < zips.size() - KEEP; i++) {
      Files.deleteIfExists(zips.get(i));
    }
  }
}
