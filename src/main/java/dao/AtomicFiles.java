package dao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import lombok.SneakyThrows;

/**
 * Crash-safe text writes: the content goes to a temp file in the same directory, which then
 * atomically replaces the target. A crash or power loss mid-write leaves either the old or the new
 * file — never a truncated one.
 */
public final class AtomicFiles {

  static final String TMP_SUFFIX = ".tmp";

  private AtomicFiles() {}

  @SneakyThrows
  public static void write(Path target, String content) {
    Path dir = target.toAbsolutePath().getParent();
    Files.createDirectories(dir);
    Path tmp = Files.createTempFile(dir, "." + target.getFileName(), TMP_SUFFIX);
    try {
      Files.writeString(tmp, content, StandardCharsets.UTF_8);
      replace(tmp, target);
    } finally {
      Files.deleteIfExists(tmp);
    }
  }

  private static void replace(Path tmp, Path target) throws IOException {
    try {
      Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
