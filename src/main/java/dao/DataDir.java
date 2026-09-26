package dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;

import lombok.SneakyThrows;

public final class DataDir {

  private static final String FOLDER = "cutenotes_data";

  /** Set by the launchers of the packaged apps (macOS .app, Windows / Linux bundles). */
  static final String INSTALLED_PROPERTY = "cutenotes.installed";

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
      Path home = DataLocation.home();
      dir =
          pick(
              DataLocation.read(home),
              Boolean.getBoolean(INSTALLED_PROPERTY),
              jarDir().resolve(FOLDER),
              home.resolve(FOLDER));
      cached = dir;
    }
    return dir;
  }

  /**
   * A folder chosen by the user wins when it can be used; otherwise the default applies (see {@link
   * #pick(boolean, Path, Path)}).
   */
  static Path pick(Path chosen, boolean installed, Path nextToJar, Path home) {
    if (chosen != null && usable(chosen)) {
      return chosen;
    }
    return pick(installed, nextToJar, home);
  }

  /**
   * A packaged app always uses the home folder: its jar lives inside the app, which is replaced as
   * a whole on upgrade. The bare jar stays portable and keeps its notes next to itself.
   */
  @SneakyThrows
  static Path pick(boolean installed, Path nextToJar, Path home) {
    if (installed) {
      Files.createDirectories(home);
      return home;
    }
    return choose(nextToJar, home);
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
