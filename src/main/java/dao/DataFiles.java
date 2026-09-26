package dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The files that make up a notes folder: notes, images, history, bin and settings; not the backups,
 * logs, locks or half-written temporary files.
 */
public final class DataFiles {

  private static final Set<String> SKIP =
      Set.of(Backups.DIR, "logs", ".lock", FolderLease.FILE, ".cutenotes-port");

  private DataFiles() {}

  public static List<Path> of(Path dataDir) throws IOException {
    List<Path> files = new ArrayList<>();
    if (!Files.isDirectory(dataDir)) {
      return files;
    }
    try (Stream<Path> all = Files.walk(dataDir)) {
      for (Path f : all.filter(Files::isRegularFile).toList()) {
        Path top = dataDir.relativize(f).getName(0);
        if (!SKIP.contains(top.toString()) && !f.toString().endsWith(".tmp")) {
          files.add(f);
        }
      }
    }
    return files;
  }

  /** Copies a notes folder into another; files already there are kept. Returns how many copied. */
  public static int copy(Path from, Path to) throws IOException {
    int copied = 0;
    for (Path file : of(from)) {
      Path target = to.resolve(from.relativize(file).toString());
      if (!Files.exists(target)) {
        Files.createDirectories(target.getParent());
        Files.copy(file, target);
        copied++;
      }
    }
    return copied;
  }
}
