package dao;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import lombok.SneakyThrows;

/**
 * Earlier versions of notes, as files {@code <data>/history/<note id>/<epoch millis>.md}. A
 * snapshot is taken at most every ten minutes and only when the text changed; the newest fifty are
 * kept per note.
 */
public final class HistoryStore implements NoteHistory {

  static final String DIR = "history";
  static final Duration EVERY = Duration.ofMinutes(10);
  static final int KEEP = 50;
  private static final String EXT = ".md";

  private final Path root;

  public HistoryStore(Path dataDir) {
    this.root = dataDir.resolve(DIR);
  }

  /**
   * Records {@code content} unless a snapshot was taken recently ({@code force} skips the wait).
   */
  @Override
  @SneakyThrows
  public void snapshot(String id, String content, Instant now, boolean force) {
    List<Instant> versions = versions(id);
    if (!versions.isEmpty()) {
      boolean recent = versions.get(0).isAfter(now.minus(EVERY));
      if ((recent && !force) || content.equals(load(id, versions.get(0)))) {
        return;
      }
    }
    Path dir = root.resolve(id);
    AtomicFiles.write(dir.resolve(now.toEpochMilli() + EXT), content);
    List<Instant> all = versions(id);
    for (Instant old : all.subList(Math.min(KEEP, all.size()), all.size())) {
      Files.deleteIfExists(dir.resolve(old.toEpochMilli() + EXT));
    }
  }

  /** When each snapshot of the note was taken, newest first. */
  @Override
  @SneakyThrows
  public List<Instant> versions(String id) {
    Path dir = root.resolve(id);
    List<Instant> versions = new ArrayList<>();
    if (!Files.isDirectory(dir)) {
      return versions;
    }
    try (Stream<Path> files = Files.list(dir)) {
      for (Path f : files.toList()) {
        String name = f.getFileName().toString();
        String stamp = name.endsWith(EXT) ? name.substring(0, name.length() - EXT.length()) : "";
        if (stamp.matches("\\d+")) {
          versions.add(Instant.ofEpochMilli(Long.parseLong(stamp)));
        }
      }
    }
    versions.sort(Comparator.reverseOrder());
    return versions;
  }

  @Override
  @SneakyThrows
  public String load(String id, Instant version) {
    Path file = root.resolve(id).resolve(version.toEpochMilli() + EXT);
    return Files.exists(file) ? Files.readString(file, StandardCharsets.UTF_8) : "";
  }

  /** Forgets every version of a note (deleted for good, or now password-protected). */
  @Override
  @SneakyThrows
  public void deleteAll(String id) {
    Path dir = root.resolve(id);
    if (Files.isDirectory(dir)) {
      try (Stream<Path> files = Files.list(dir)) {
        for (Path f : files.toList()) {
          Files.deleteIfExists(f);
        }
      }
      Files.deleteIfExists(dir);
    }
  }

  /** The text of every snapshot (for finding images that are still referenced). */
  @SneakyThrows
  public List<String> allTexts() {
    List<String> texts = new ArrayList<>();
    if (!Files.isDirectory(root)) {
      return texts;
    }
    try (Stream<Path> files = Files.walk(root)) {
      for (Path f : files.filter(p -> p.toString().endsWith(EXT)).toList()) {
        texts.add(Files.readString(f, StandardCharsets.UTF_8));
      }
    }
    return texts;
  }
}
