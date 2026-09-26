package markdown;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds web links in plain text ({@code https://…}, {@code http://…}, {@code www.…}). */
public final class Links {

  private static final Pattern URL =
      Pattern.compile("(?<![\\w@/])(?:https?://|www\\.)[^\\s<>\"'`]+", Pattern.CASE_INSENSITIVE);
  private static final String TRAILING = ".,;:!?'\"";

  private Links() {}

  /** {@code [start, end)} of every link, without trailing punctuation or an unmatched bracket. */
  public static List<int[]> find(String text) {
    List<int[]> links = new ArrayList<>();
    Matcher m = URL.matcher(text);
    while (m.find()) {
      int end = trim(text, m.start(), m.end());
      if (end > m.start()) {
        links.add(new int[] {m.start(), end});
      }
    }
    return links;
  }

  /** The link covering {@code offset}, ready to open; {@code null} when there is none. */
  public static String at(String text, int offset) {
    for (int[] link : find(text)) {
      if (offset >= link[0] && offset < link[1]) {
        return normalize(text.substring(link[0], link[1]));
      }
    }
    return null;
  }

  /** {@code www.example.com} opens as {@code https://www.example.com}. */
  public static String normalize(String link) {
    return link.regionMatches(true, 0, "www.", 0, 4) ? "https://" + link : link;
  }

  private static int trim(String text, int start, int end) {
    int e = end;
    while (e > start) {
      char c = text.charAt(e - 1);
      boolean unmatchedClose =
          (c == ')' && count(text, start, e, '(') < count(text, start, e, ')'))
              || (c == ']' && count(text, start, e, '[') < count(text, start, e, ']'));
      if (TRAILING.indexOf(c) >= 0 || unmatchedClose) {
        e--;
      } else {
        break;
      }
    }
    return e;
  }

  private static int count(String text, int start, int end, char c) {
    int n = 0;
    for (int i = start; i < end; i++) {
      if (text.charAt(i) == c) {
        n++;
      }
    }
    return n;
  }
}
