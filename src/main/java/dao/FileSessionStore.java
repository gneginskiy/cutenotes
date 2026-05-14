package dao;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import lombok.SneakyThrows;

public class FileSessionStore implements SessionStore {

  private static final String FILE_NAME = "cutenotes_session.txt";

  private final Path file;

  public FileSessionStore() {
    this(DataDir.resolve().resolve(FILE_NAME));
  }

  public FileSessionStore(Path file) {
    this.file = file;
  }

  @Override
  @SneakyThrows
  public List<String> read() {
    if (!Files.exists(file)) {
      return List.of();
    }
    String content = Files.readString(file, StandardCharsets.UTF_8);
    return Arrays.stream(content.split("\\R")).filter(s -> !s.isBlank()).toList();
  }

  @Override
  @SneakyThrows
  public void write(List<String> tabNames) {
    Files.createDirectories(file.getParent());
    Files.writeString(file, String.join("\n", tabNames), StandardCharsets.UTF_8);
  }
}
