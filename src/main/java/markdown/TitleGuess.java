package markdown;

/**
 * A name for a note derived from its text: the first non-empty line, without list or heading marks.
 */
public final class TitleGuess {

  static final int MAX_LENGTH = 48;

  private TitleGuess() {}

  /** The derived name, or an empty string when the note has no text yet. */
  public static String from(String text) {
    for (String raw : text.split("\\R", 64)) {
      String line = clean(raw);
      if (!line.isEmpty()) {
        return shorten(line);
      }
    }
    return "";
  }

  private static String clean(String raw) {
    ListLine item = ListLine.parse(raw);
    String line = (item != null ? item.rest() : raw).strip();
    if (line.matches("[-*+]")) {
      return "";
    }
    while (line.startsWith("#") && line.replaceFirst("^#+", "").startsWith(" ")) {
      line = line.replaceFirst("^#+", "").strip();
    }
    return line.replaceAll("[*_~`]{2,}|(?<!\\w)[*_](?=\\S)|(?<=\\S)[*_](?!\\w)", "").strip();
  }

  private static String shorten(String line) {
    if (line.length() <= MAX_LENGTH) {
      return line;
    }
    int cut = line.lastIndexOf(' ', MAX_LENGTH);
    return (cut > MAX_LENGTH / 2 ? line.substring(0, cut) : line.substring(0, MAX_LENGTH)).strip();
  }
}
