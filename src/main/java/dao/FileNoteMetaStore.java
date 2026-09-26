package dao;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.SneakyThrows;
import model.NoteMeta;

/**
 * Stores {@link NoteMeta} as tab-separated lines {@code id<TAB>flags<TAB>color}, where flags hold
 * {@code p} for pinned and {@code a} for an automatic title. Default entries are not written.
 */
public class FileNoteMetaStore implements NoteMetaStore {

  private static final String SEP = "\t";

  private final Path file;

  public FileNoteMetaStore() {
    this(DataDir.resolve().resolve("cutenotes_meta.txt"));
  }

  public FileNoteMetaStore(Path file) {
    this.file = file;
  }

  @Override
  @SneakyThrows
  public Map<String, NoteMeta> read() {
    Map<String, NoteMeta> all = new LinkedHashMap<>();
    if (!Files.exists(file)) {
      return all;
    }
    for (String line : Files.readString(file, StandardCharsets.UTF_8).split("\\R")) {
      String[] parts = line.split(SEP, -1);
      if (parts.length >= 3 && !parts[0].isBlank()) {
        String flags = parts[1];
        all.put(
            parts[0],
            new NoteMeta(parts[0], flags.contains("p"), parts[2].trim(), flags.contains("a")));
      }
    }
    return all;
  }

  @Override
  public void write(Map<String, NoteMeta> all) {
    StringBuilder text = new StringBuilder();
    for (NoteMeta m : all.values()) {
      if (m.isDefault()) {
        continue;
      }
      String flags = (m.pinned() ? "p" : "") + (m.autoTitle() ? "a" : "");
      text.append(m.id()).append(SEP).append(flags).append(SEP).append(m.color()).append('\n');
    }
    AtomicFiles.write(file, text.toString());
  }
}
