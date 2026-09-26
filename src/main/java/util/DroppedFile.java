package util;

import java.util.Locale;
import java.util.Set;

/** What a file dropped on the window becomes: a new note, an inline image, or nothing. */
public enum DroppedFile {
  NOTE,
  IMAGE,
  UNSUPPORTED;

  private static final Set<String> TEXT = Set.of("txt", "md", "markdown", "text");
  private static final Set<String> PICTURES = Set.of("png", "jpg", "jpeg", "gif", "bmp");

  public static DroppedFile of(String fileName) {
    String ext = extension(fileName);
    if (TEXT.contains(ext)) {
      return NOTE;
    }
    return PICTURES.contains(ext) ? IMAGE : UNSUPPORTED;
  }

  /** The note name for a dropped file: its name without the extension. */
  public static String title(String fileName) {
    int dot = fileName.lastIndexOf('.');
    return dot > 0 ? fileName.substring(0, dot) : fileName;
  }

  private static String extension(String fileName) {
    int dot = fileName.lastIndexOf('.');
    return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
  }
}
