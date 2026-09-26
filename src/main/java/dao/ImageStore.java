package dao;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

import lombok.SneakyThrows;

/**
 * The pasted images of the notes, kept as PNG files in {@code <data>/images}. Notes reference them
 * by a relative path; {@link #resolve} refuses anything that points outside that folder.
 */
public final class ImageStore {

  public static final String DIR = "images";

  private final Path dataDir;
  private final Path root;

  public ImageStore(Path dataDir) {
    this.dataDir = dataDir;
    this.root = dataDir.resolve(DIR).toAbsolutePath().normalize();
  }

  /** Stores {@code image}; returns the relative path to put into the note. */
  @SneakyThrows
  public String save(BufferedImage image) {
    Files.createDirectories(root);
    String name = "img_" + UUID.randomUUID() + ".png";
    ImageIO.write(image, "png", root.resolve(name).toFile());
    return DIR + "/" + name;
  }

  /**
   * The file behind a path from a note, or {@code null} when the path leaves the images folder: a
   * note (e.g. pasted or synced from elsewhere) must not make the app open arbitrary files.
   */
  public Path resolve(String relative) {
    if (relative == null || relative.isBlank()) {
      return null;
    }
    try {
      Path file = dataDir.resolve(relative).toAbsolutePath().normalize();
      return file.startsWith(root) && !file.equals(root) ? file : null;
    } catch (InvalidPathException e) {
      return null;
    }
  }

  /**
   * Deletes images that no note references any more and that are older than {@code olderThan} (an
   * image pasted a moment ago may not be saved in its note yet). Returns how many were deleted.
   */
  public int collectGarbage(Set<String> referenced, Instant olderThan) throws IOException {
    if (!Files.isDirectory(root)) {
      return 0;
    }
    Set<Path> keep =
        referenced.stream().map(this::resolve).filter(Objects::nonNull).collect(Collectors.toSet());
    int deleted = 0;
    try (Stream<Path> files = Files.list(root)) {
      for (Path file : files.filter(Files::isRegularFile).toList()) {
        if (!keep.contains(file)
            && Files.getLastModifiedTime(file).toInstant().isBefore(olderThan)) {
          Files.delete(file);
          deleted++;
        }
      }
    }
    return deleted;
  }
}
