package markdown;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts between inline Markdown and a flat list of {@link MdNode}s. Styles are toggle markers:
 * {@code **bold**}, {@code *italic*}, {@code __underline__}, {@code ~~strike~~}, {@code
 * ```code```}; images are {@code ![|WxH](path)} (the size suffix is optional). Literal marker
 * characters in text are backslash-escaped, see {@link MarkdownEscape}.
 */
public final class MarkdownText {

  private MarkdownText() {}

  public static List<MdNode> parse(String md) {
    List<MdNode> nodes = new ArrayList<>();
    StringBuilder text = new StringBuilder();
    boolean[] style = new boolean[5];
    int i = 0;
    while (i < md.length()) {
      i = step(md, i, nodes, text, style);
    }
    flush(nodes, text, style);
    return nodes;
  }

  private static int step(
      String md, int i, List<MdNode> nodes, StringBuilder text, boolean[] style) {
    if (md.charAt(i) == '\\') {
      return MarkdownEscape.unescape(md, i, text);
    }
    int[] advance = new int[1];
    MdImage image = starts(md, i, "![") ? ImageSyntax.parse(md, i, advance) : null;
    if (image != null) {
      flush(nodes, text, style);
      nodes.add(image);
      return advance[0];
    }
    int marker = handleMarker(md, i, nodes, text, style);
    if (marker > 0) {
      return i + marker;
    }
    text.append(md.charAt(i));
    return i + 1;
  }

  /** Flushes pending text, toggles the matching style at {@code i}, returns the marker length. */
  private static int handleMarker(
      String md, int i, List<MdNode> nodes, StringBuilder text, boolean[] style) {
    int index;
    int length;
    if (starts(md, i, "```")) {
      index = 4;
      length = 3;
    } else if (starts(md, i, "**")) {
      index = 0;
      length = 2;
    } else if (starts(md, i, "__")) {
      index = 2;
      length = 2;
    } else if (starts(md, i, "~~")) {
      index = 3;
      length = 2;
    } else if (md.charAt(i) == '*') {
      index = 1;
      length = 1;
    } else {
      return 0;
    }
    flush(nodes, text, style);
    style[index] = !style[index];
    return length;
  }

  /** The text of a note without Markdown markers; images are left out. */
  public static String plain(String md) {
    StringBuilder out = new StringBuilder();
    for (MdNode node : parse(md)) {
      if (node instanceof MdText t) {
        out.append(t.text());
      }
    }
    return out.toString();
  }

  public static String write(List<MdNode> nodes) {
    StringBuilder out = new StringBuilder();
    for (MdNode node : nodes) {
      if (node instanceof MdText t) {
        writeText(out, t);
      } else if (node instanceof MdImage img) {
        ImageSyntax.write(out, img);
      }
    }
    return out.toString();
  }

  private static void writeText(StringBuilder out, MdText t) {
    String code = t.code() ? "```" : "";
    String bold = t.bold() ? "**" : "";
    String italic = t.italic() ? "*" : "";
    String underline = t.underline() ? "__" : "";
    String strike = t.strike() ? "~~" : "";
    out.append(code).append(bold).append(italic).append(underline).append(strike);
    out.append(MarkdownEscape.escape(t.text()));
    out.append(strike).append(underline).append(italic).append(bold).append(code);
  }

  private static void flush(List<MdNode> nodes, StringBuilder text, boolean[] style) {
    if (text.length() > 0) {
      nodes.add(new MdText(text.toString(), style[0], style[1], style[2], style[3], style[4]));
      text.setLength(0);
    }
  }

  private static boolean starts(String md, int i, String token) {
    return md.regionMatches(i, token, 0, token.length());
  }
}
