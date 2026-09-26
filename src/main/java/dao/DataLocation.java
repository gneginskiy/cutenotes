package dao;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.SneakyThrows;

/**
 * Remembers a notes folder chosen by the user (e.g. inside iCloud Drive or Dropbox). The pointer
 * lives in the home folder, outside every notes folder, so it is found before any of them is known.
 */
public final class DataLocation {

  static final String POINTER = ".cutenotes-location";

  private DataLocation() {}

  /** The chosen folder, or {@code null} when none was chosen (or the pointer is unreadable). */
  public static Path read(Path home) {
    Path pointer = home.resolve(POINTER);
    try {
      if (!Files.isRegularFile(pointer)) {
        return null;
      }
      String value = Files.readString(pointer, StandardCharsets.UTF_8).trim();
      return value.isEmpty() ? null : Path.of(value);
    } catch (Exception e) {
      return null;
    }
  }

  public static void write(Path home, Path folder) {
    AtomicFiles.write(home.resolve(POINTER), folder.toAbsolutePath().normalize().toString());
  }

  @SneakyThrows
  public static void clear(Path home) {
    Files.deleteIfExists(home.resolve(POINTER));
  }

  public static Path home() {
    return Path.of(System.getProperty("user.home"));
  }
}
