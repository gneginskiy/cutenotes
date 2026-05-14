package dao;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import lombok.SneakyThrows;
import model.Tab;
import model.TabMeta;

public class FileTabRepository implements TabRepository {

  private static final String PREFIX = "note_";
  private static final String SUFFIX = ".txt";

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
      for (Path p : files.filter(this::isNoteFile).toList()) {
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
    Path target = baseDir.resolve(PREFIX + id + "_" + sanitize(name) + SUFFIX);
    Path existing = findFileById(id);
    if (existing != null && !existing.equals(target)) {
      Files.move(existing, target, StandardCopyOption.REPLACE_EXISTING);
    }
    Files.writeString(target, name + "\n" + content, StandardCharsets.UTF_8);
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
  public String newId() {
    return UUID.randomUUID().toString();
  }

  @SneakyThrows
  private Path findFileById(String id) {
    if (!Files.isDirectory(baseDir)) {
      return null;
    }
    String prefix = PREFIX + id;
    try (Stream<Path> s = Files.list(baseDir)) {
      return s.filter(p -> matchesId(p, prefix)).findFirst().orElse(null);
    }
  }

  private static boolean matchesId(Path p, String prefix) {
    String n = p.getFileName().toString();
    if (!n.startsWith(prefix) || !n.endsWith(SUFFIX)) {
      return false;
    }
    String rest = n.substring(prefix.length());
    return rest.equals(SUFFIX) || rest.startsWith("_");
  }

  private boolean isNoteFile(Path p) {
    String n = p.getFileName().toString();
    return n.startsWith(PREFIX) && n.endsWith(SUFFIX);
  }

  @SneakyThrows
  private TabMeta metaOf(Path p) {
    String fn = p.getFileName().toString();
    String stripped = fn.substring(PREFIX.length(), fn.length() - SUFFIX.length());
    int sep = stripped.indexOf('_');
    String id = sep < 0 ? stripped : stripped.substring(0, sep);
    String name;
    try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
      String first = br.readLine();
      name = first == null ? "" : first;
    }
    return new TabMeta(id, name, Files.getLastModifiedTime(p).toInstant());
  }

  private static String sanitize(String name) {
    return name.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_");
  }
}
