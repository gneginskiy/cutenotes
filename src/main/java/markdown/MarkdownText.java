package markdown;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts between inline Markdown and a flat list of {@link MdNode}s. Styles are toggle markers:
 * {@code **bold**}, {@code *italic*}, {@code __underline__}, {@code ~~strike~~}, {@code
 * ```code```}; images are {@code ![|WxH](path)} (the size suffix is optional).
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
    int[] advance = new int[1];
    MdImage image = starts(md, i, "![") ? image(md, i, advance) : null;
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

  public static String write(List<MdNode> nodes) {
    StringBuilder out = new StringBuilder();
    for (MdNode node : nodes) {
      if (node instanceof MdText t) {
        writeText(out, t);
      } else if (node instanceof MdImage img) {
        writeImage(out, img);
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
    out.append(t.text());
    out.append(strike).append(underline).append(italic).append(bold).append(code);
  }

  private static void writeImage(StringBuilder out, MdImage img) {
    out.append("![");
    if (img.width() > 0 && img.height() > 0) {
      out.append('|').append(img.width()).append('x').append(img.height());
    }
    out.append("](").append(img.path()).append(')');
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

  private static MdImage image(String md, int i, int[] advance) {
    int altEnd = md.indexOf("](", i + 2);
    if (altEnd < 0) {
      return null;
    }
    int pathEnd = md.indexOf(')', altEnd + 2);
    if (pathEnd < 0) {
      return null;
    }
    String alt = md.substring(i + 2, altEnd);
    String path = md.substring(altEnd + 2, pathEnd);
    advance[0] = pathEnd + 1;
    int width = 0;
    int height = 0;
    int x = alt.startsWith("|") ? alt.indexOf('x', 1) : -1;
    if (x > 0) {
      width = parseInt(alt.substring(1, x));
      height = parseInt(alt.substring(x + 1));
    }
    return new MdImage(path, width, height);
  }

  private static int parseInt(String s) {
    try {
      return Integer.parseInt(s.trim());
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
