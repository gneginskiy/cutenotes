package markdown;

import java.util.ArrayList;
import java.util.List;

/** A note's inline nodes cut into lines, for output that works line by line (HTML export). */
public final class MdLines {

  private MdLines() {}

  public static List<List<MdNode>> split(String md) {
    List<List<MdNode>> lines = new ArrayList<>();
    List<MdNode> current = new ArrayList<>();
    for (MdNode node : MarkdownText.parse(md)) {
      if (!(node instanceof MdText t)) {
        current.add(node);
        continue;
      }
      String[] parts = t.text().split("\n", -1);
      for (int i = 0; i < parts.length; i++) {
        if (i > 0) {
          lines.add(current);
          current = new ArrayList<>();
        }
        if (!parts[i].isEmpty()) {
          current.add(withText(t, parts[i]));
        }
      }
    }
    lines.add(current);
    return lines;
  }

  /** The line's text without images. */
  public static String text(List<MdNode> line) {
    StringBuilder out = new StringBuilder();
    for (MdNode node : line) {
      if (node instanceof MdText t) {
        out.append(t.text());
      }
    }
    return out.toString();
  }

  /** The line without its first {@code count} characters of text (a list or heading prefix). */
  public static List<MdNode> dropPrefix(List<MdNode> line, int count) {
    List<MdNode> rest = new ArrayList<>();
    int left = count;
    for (MdNode node : line) {
      if (left > 0 && node instanceof MdText t) {
        int cut = Math.min(left, t.text().length());
        left -= cut;
        if (cut < t.text().length()) {
          rest.add(withText(t, t.text().substring(cut)));
        }
      } else {
        rest.add(node);
      }
    }
    return rest;
  }

  static MdText withText(MdText t, String text) {
    return new MdText(text, t.bold(), t.italic(), t.underline(), t.strike(), t.code());
  }
}
