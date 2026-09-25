package dao;

import java.awt.Rectangle;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Stores the window bounds as a single {@code x,y,width,height} line. */
public class FileBoundsStore implements BoundsStore {

  private static final String FILE_NAME = "cutenotes_window.txt";

  private final Path file;

  public FileBoundsStore() {
    this(DataDir.resolve().resolve(FILE_NAME));
  }

  public FileBoundsStore(Path file) {
    this.file = file;
  }

  @Override
  public Rectangle load() {
    try {
      return Files.exists(file) ? parse(Files.readString(file, StandardCharsets.UTF_8)) : null;
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public void save(Rectangle b) {
    try {
      AtomicFiles.write(file, b.x + "," + b.y + "," + b.width + "," + b.height);
    } catch (Exception e) {
      // AtomicFiles throws I/O errors sneakily. Window placement is a convenience:
      // failing to remember it must never block quitting.
    }
  }

  static Rectangle parse(String text) {
    String[] parts = text.trim().split(",");
    if (parts.length != 4) {
      return null;
    }
    try {
      int w = Integer.parseInt(parts[2].trim());
      int h = Integer.parseInt(parts[3].trim());
      if (w <= 0 || h <= 0) {
        return null;
      }
      return new Rectangle(
          Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), w, h);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
