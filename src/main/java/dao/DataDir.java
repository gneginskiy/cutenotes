package dao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;

import lombok.SneakyThrows;

public final class DataDir {

  private static final String FOLDER = "cutenotes_data";

  private DataDir() {}

  @SneakyThrows
  public static Path resolve() {
    Path jarDir = Path.of("");
    CodeSource src = DataDir.class.getProtectionDomain().getCodeSource();
    if (src != null) {
      Path loc = Path.of(src.getLocation().toURI());
      jarDir = Files.isDirectory(loc) ? loc : loc.getParent();
    }
    Path data = jarDir.resolve(FOLDER);
    Files.createDirectories(data);
    return data;
  }
}
