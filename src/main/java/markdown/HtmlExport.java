package markdown;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A note as a standalone web page: headings, lists, checkboxes, inline styles, links and images.
 */
public final class HtmlExport {

  private static final String STYLE =
      "body{max-width:720px;margin:48px auto;padding:0 20px;"
          + "font:16px/1.6 -apple-system,'Segoe UI',system-ui,sans-serif;color:#222}"
          + "p{margin:0;min-height:1.6em}h1,h2,h3{margin:.6em 0 .2em}"
          + "code{background:#f2f2f2;border-radius:4px;padding:0 4px}"
          + ".done{color:#888;text-decoration:line-through}img{max-width:100%}";

  private HtmlExport() {}

  /** {@code imageUrl} turns a note's image path into a URL the page can load. */
  public static String page(String title, String md, UnaryOperator<String> imageUrl) {
    StringBuilder out = new StringBuilder("<!DOCTYPE html>\n<html><head><meta charset=\"utf-8\">");
    out.append("<title>").append(escape(title)).append("</title><style>");
    out.append(STYLE).append("</style></head><body>\n");
    for (List<MdNode> line : MdLines.split(md)) {
      out.append(line(line, imageUrl)).append('\n');
    }
    return out.append("</body></html>\n").toString();
  }

  static String line(List<MdNode> line, UnaryOperator<String> imageUrl) {
    String text = MdLines.text(line);
    LineKind kind = LineKind.of(text);
    if (kind.heading()) {
      int level = kind.ordinal();
      String inner = inline(MdLines.dropPrefix(line, level + 1), imageUrl);
      return "<h" + level + ">" + inner + "</h" + level + ">";
    }
    ListLine item = ListLine.parse(text);
    if (item == null) {
      return "<p>" + inline(line, imageUrl) + "</p>";
    }
    String inner = inline(MdLines.dropPrefix(line, item.prefixLength()), imageUrl);
    String bullet = item.checkbox() ? (item.checked() ? "☑" : "☐") : bullet(item.marker());
    String indent = "margin-left:" + (1 + item.indent().replace("\t", "  ").length() / 2) + "em";
    String cls = item.checked() ? " class=\"done\"" : "";
    return "<p style=\"" + indent + "\">" + bullet + " <span" + cls + ">" + inner + "</span></p>";
  }

  static String inline(List<MdNode> nodes, UnaryOperator<String> imageUrl) {
    StringBuilder out = new StringBuilder();
    for (MdNode node : nodes) {
      if (node instanceof MdImage img) {
        String width = img.width() > 0 ? " width=\"" + img.width() + "\"" : "";
        out.append("<img src=\"").append(escape(imageUrl.apply(img.path()))).append('"');
        out.append(width).append(" alt=\"\">");
      } else if (node instanceof MdText t) {
        out.append(styled(t));
      }
    }
    return out.toString();
  }

  private static String styled(MdText t) {
    String html = t.code() ? escape(t.text()) : linked(t.text());
    html = wrap(html, "code", t.code());
    html = wrap(html, "s", t.strike());
    html = wrap(html, "u", t.underline());
    html = wrap(html, "em", t.italic());
    return wrap(html, "strong", t.bold());
  }

  private static String linked(String text) {
    StringBuilder out = new StringBuilder();
    int from = 0;
    for (int[] link : Links.find(text)) {
      String url = text.substring(link[0], link[1]);
      out.append(escape(text.substring(from, link[0])));
      out.append("<a href=\"").append(escape(Links.normalize(url))).append("\">");
      out.append(escape(url)).append("</a>");
      from = link[1];
    }
    return out.append(escape(text.substring(from))).toString();
  }

  private static String bullet(String marker) {
    return Character.isDigit(marker.charAt(0)) ? escape(marker) : "•";
  }

  private static String wrap(String html, String tag, boolean on) {
    return on ? "<" + tag + ">" + html + "</" + tag + ">" : html;
  }

  static String escape(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
