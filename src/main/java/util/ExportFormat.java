package util;

import java.util.Locale;
import java.util.function.UnaryOperator;

import markdown.HtmlExport;
import markdown.MarkdownText;

/** The file types a note can be exported to. */
public enum ExportFormat {
  MARKDOWN("md"),
  TEXT("txt"),
  HTML("html");

  private static final int MAX_NAME = 80;

  private final String extension;

  ExportFormat(String extension) {
    this.extension = extension;
  }

  public String extension() {
    return extension;
  }

  /** The file content; {@code imageUrl} maps an image path to a link (HTML only). */
  public String render(String title, String md, UnaryOperator<String> imageUrl) {
    return switch (this) {
      case MARKDOWN -> md;
      case TEXT -> MarkdownText.plain(md);
      case HTML -> HtmlExport.page(title, md, imageUrl);
    };
  }

  /** The format a file name asks for by its extension; {@code fallback} without a known one. */
  public static ExportFormat ofFile(String fileName, ExportFormat fallback) {
    String lower = fileName.toLowerCase(Locale.ROOT);
    for (ExportFormat f : values()) {
      if (lower.endsWith("." + f.extension)) {
        return f;
      }
    }
    return fallback;
  }

  /** {@code fileName} with this format's extension, unless it already has it. */
  public String withExtension(String fileName) {
    return ofFile(fileName, null) == this ? fileName : fileName + "." + extension;
  }

  /** A file name for a note: characters file systems reject are replaced, the length capped. */
  public static String safeName(String title) {
    String name = title.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").strip();
    name = name.length() > MAX_NAME ? name.substring(0, MAX_NAME).strip() : name;
    return name.isEmpty() || name.startsWith(".") ? "note" + name : name;
  }
}
