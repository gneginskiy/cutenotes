package markdown;

/** The Markdown form of an inline image: {@code ![|WxH](path)}, the size being optional. */
final class ImageSyntax {

  private ImageSyntax() {}

  /**
   * The image starting at {@code i} (at {@code ![}), with {@code advance[0]} set past it; {@code
   * null} when the text there is not a complete image reference.
   */
  static MdImage parse(String md, int i, int[] advance) {
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

  static void write(StringBuilder out, MdImage img) {
    out.append("![");
    if (img.width() > 0 && img.height() > 0) {
      out.append('|').append(img.width()).append('x').append(img.height());
    }
    out.append("](").append(img.path()).append(')');
  }
}
