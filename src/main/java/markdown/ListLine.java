package markdown;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A list item line: {@code - item}, {@code * item}, {@code 1. item}, {@code - [ ] task} or {@code -
 * [x] done}, optionally indented.
 *
 * @param indent leading spaces / tabs
 * @param marker {@code -}, {@code *}, {@code +} or a number with {@code .} / {@code )}
 * @param check {@code null} without a checkbox, otherwise the character between the brackets
 * @param rest the item's text
 */
public record ListLine(String indent, String marker, String check, String rest) {

  private static final Pattern ITEM =
      Pattern.compile("^([ \\t]*)([-*+]|\\d{1,9}[.)])(?: \\[([ xX])\\])?(?: (.*))?$");

  /** The list item on {@code line}, or {@code null} for an ordinary line. */
  public static ListLine parse(String line) {
    Matcher m = ITEM.matcher(line);
    if (!m.matches()) {
      return null;
    }
    boolean bareMarker = m.group(3) == null && m.group(4) == null;
    if (bareMarker) {
      return null;
    }
    return new ListLine(m.group(1), m.group(2), m.group(3), m.group(4) == null ? "" : m.group(4));
  }

  public boolean checkbox() {
    return check != null;
  }

  public boolean checked() {
    return "x".equalsIgnoreCase(check);
  }

  public boolean isEmpty() {
    return rest.isBlank();
  }

  /** Where the item's own text starts within the line. */
  public int prefixLength() {
    return indent.length() + marker.length() + (checkbox() ? 4 : 0) + 1;
  }

  /** Offset of the checkbox character within the line; -1 without a checkbox. */
  public int checkOffset() {
    return checkbox() ? indent.length() + marker.length() + 2 : -1;
  }

  /** The prefix of the next item: same indent and bullet, the next number, a fresh checkbox. */
  public String continuation() {
    return indent + nextMarker() + (checkbox() ? " [ ]" : "") + " ";
  }

  private String nextMarker() {
    char last = marker.charAt(marker.length() - 1);
    if (last == '.' || last == ')') {
      long number = Long.parseLong(marker.substring(0, marker.length() - 1));
      return (number + 1) + String.valueOf(last);
    }
    return marker;
  }
}
