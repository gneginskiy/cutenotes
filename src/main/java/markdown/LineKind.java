package markdown;

/** How a line of a note is displayed: as a heading, as a done task, or as plain text. */
public enum LineKind {
  PLAIN(1f),
  H1(1.5f),
  H2(1.3f),
  H3(1.15f),
  DONE(1f);

  private final float scale;

  LineKind(float scale) {
    this.scale = scale;
  }

  /** Font size factor of the line. */
  public float scale() {
    return scale;
  }

  public boolean heading() {
    return this == H1 || this == H2 || this == H3;
  }

  /**
   * Headings start with {@code #}, {@code ##} or {@code ###} and a space; {@code #tag} does not.
   */
  public static LineKind of(String text) {
    int newline = text.indexOf('\n');
    String line = newline < 0 ? text : text.substring(0, newline);
    if (line.startsWith("# ")) {
      return H1;
    }
    if (line.startsWith("## ")) {
      return H2;
    }
    if (line.startsWith("### ")) {
      return H3;
    }
    ListLine item = ListLine.parse(line);
    return item != null && item.checked() ? DONE : PLAIN;
  }
}
