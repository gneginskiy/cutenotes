package dao;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.SneakyThrows;

/** Reads and writes the simple {@code key=value} files used for options and settings. */
public final class KeyValueFile {

  private KeyValueFile() {}

  /** The entries of {@code file}; empty when it does not exist. Keys keep their file order. */
  @SneakyThrows
  public static Map<String, String> read(Path file) {
    if (!Files.exists(file)) {
      return new LinkedHashMap<>();
    }
    return parse(Files.readString(file, StandardCharsets.UTF_8));
  }

  public static void write(Path file, Map<String, String> entries) {
    AtomicFiles.write(file, format(entries));
  }

  static Map<String, String> parse(String content) {
    Map<String, String> m = new LinkedHashMap<>();
    for (String line : content.split("\\R")) {
      int eq = line.indexOf('=');
      if (eq > 0) {
        m.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
      }
    }
    return m;
  }

  static String format(Map<String, String> entries) {
    StringBuilder text = new StringBuilder();
    entries.forEach(
        (key, value) -> {
          if (value != null) {
            text.append(key).append('=').append(value).append('\n');
          }
        });
    return text.toString();
  }

  static int intOr(String value, int fallback) {
    if (value == null) {
      return fallback;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  static long longOr(String value, long fallback) {
    if (value == null) {
      return fallback;
    }
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }
}
