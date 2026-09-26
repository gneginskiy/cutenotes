package dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import lombok.SneakyThrows;
import model.Tab;
import model.TabMeta;

public class FileTabRepository implements TabRepository {

  private final Path baseDir;

  public FileTabRepository() {
    this(DataDir.resolve());
  }

  public FileTabRepository(Path baseDir) {
    this.baseDir = baseDir;
  }

  @Override
  @SneakyThrows
  public List<TabMeta> listMeta() {
    if (!Files.isDirectory(baseDir)) {
      return List.of();
    }
    List<TabMeta> result = new ArrayList<>();
    try (Stream<Path> files = Files.list(baseDir)) {
      for (Path p : files.filter(FileTabRepository::isNoteFile).toList()) {
        result.add(metaOf(p));
      }
    }
    return result;
  }

  @Override
  @SneakyThrows
  public Tab load(String id) {
    Path p = findFileById(id);
    if (p == null) {
      return new Tab(id, "", "");
    }
    String all = Files.readString(p, StandardCharsets.UTF_8);
    int nl = all.indexOf('\n');
    if (nl < 0) {
      return new Tab(id, all, "");
    }
    String name = all.substring(0, nl);
    if (name.endsWith("\r")) {
      name = name.substring(0, name.length() - 1);
    }
    return new Tab(id, name, all.substring(nl + 1));
  }

  @Override
  @SneakyThrows
  public void save(String id, String name, String content) {
    Files.createDirectories(baseDir);
    Path target = baseDir.resolve(NoteFileNames.of(id, name));
    Path existing = findFileById(id);
    if (existing != null && !existing.equals(target)) {
      Files.move(existing, target, StandardCopyOption.REPLACE_EXISTING);
    }
    AtomicFiles.write(target, name + "\n" + content);
  }

  @Override
  @SneakyThrows
  public void delete(String id) {
    Path p = findFileById(id);
    if (p != null) {
      Files.deleteIfExists(p);
    }
  }

  @Override
  @SneakyThrows
  public void trash(String id) {
    Path p = findFileById(id);
    if (p != null) {
      moveInto(p, baseDir.resolve(FileTrashBin.DIR));
    }
  }

  @Override
  public TrashBin trashBin() {
    return new FileTrashBin(this, new FileTabRepository(baseDir.resolve(FileTrashBin.DIR)));
  }

  /** Moves a note file into {@code dir}, dated now (for the bin: the moment of deletion). */
  static void moveInto(Path file, Path dir) throws IOException {
    Files.createDirectories(dir);
    Path target = dir.resolve(file.getFileName());
    Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
    Files.setLastModifiedTime(target, FileTime.from(Instant.now()));
  }

  Path dir() {
    return baseDir;
  }

  Path fileOf(String id) {
    return findFileById(id);
  }

  @Override
  public String newId() {
    return UUID.randomUUID().toString();
  }

  @SneakyThrows
  private Path findFileById(String id) {
    if (!Files.isDirectory(baseDir)) {
      return null;
    }
    try (Stream<Path> s = Files.list(baseDir)) {
      return s.filter(p -> NoteFileNames.matchesId(p.getFileName().toString(), id))
          .findFirst()
          .orElse(null);
    }
  }

  private static boolean isNoteFile(Path p) {
    return NoteFileNames.isNote(p.getFileName().toString());
  }

  @SneakyThrows
  private static TabMeta metaOf(Path p) {
    String name;
    try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
      String first = br.readLine();
      name = first == null ? "" : first;
    }
    String id = NoteFileNames.idOf(p.getFileName().toString(), name);
    return new TabMeta(id, name, Files.getLastModifiedTime(p).toInstant());
  }
}
