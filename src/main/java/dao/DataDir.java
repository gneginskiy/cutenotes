package dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;

import lombok.SneakyThrows;

public final class DataDir {

  private static final String FOLDER = "cutenotes_data";

  private static volatile Path cached;

  private DataDir() {}

  /**
   * The data folder: next to the jar, or in the user's home when the jar sits in a read-only place
   * (e.g. {@code /Applications}, {@code Program Files}) — otherwise the app could not start at all.
   * Resolved once; every image load used to repeat the lookup and {@code createDirectories}.
   */
  public static Path resolve() {
    Path dir = cached;
    if (dir == null) {
      dir = choose(jarDir().resolve(FOLDER), Path.of(System.getProperty("user.home"), FOLDER));
      cached = dir;
    }
    return dir;
  }

  @SneakyThrows
  static Path choose(Path preferred, Path fallback) {
    if (usable(preferred)) {
      return preferred;
    }
    Files.createDirectories(fallback);
    return fallback;
  }

  private static boolean usable(Path dir) {
    try {
      Files.createDirectories(dir);
      return Files.isWritable(dir);
    } catch (IOException e) {
      return false;
    }
  }

  @SneakyThrows
  private static Path jarDir() {
    CodeSource src = DataDir.class.getProtectionDomain().getCodeSource();
    if (src == null) {
      return Path.of("");
    }
    Path loc = Path.of(src.getLocation().toURI());
    return Files.isDirectory(loc) ? loc : loc.getParent();
  }
}
