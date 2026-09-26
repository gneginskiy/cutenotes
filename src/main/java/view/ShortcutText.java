package view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parses shortcut descriptions such as {@code "Cmd+Shift+T / Cmd+W"} into alternatives: key
 * combinations (drawn as key caps) or plain phrases ("Double-click"). On macOS modifier names turn
 * into the symbols printed on Mac keyboards.
 */
final class ShortcutText {

  /** One alternative: either the keys of a combination, or a phrase. */
  record Alt(List<String> keys, String phrase) {

    boolean isKeys() {
      return keys != null;
    }
  }

  private static final String ALTERNATIVES = "\\s+/\\s+";

  private static final Set<String> NAMED_KEYS =
      Set.of(
          "Cmd", "Ctrl", "Shift", "Option", "Alt", "Enter", "Tab", "Esc", "Space", "Delete", "PgUp",
          "PgDn", "Click");

  private static final Map<String, String> MAC_SYMBOLS =
      Map.of(
          "Cmd", "⌘", "Shift", "⇧", "Option", "⌥", "Ctrl", "⌃", "Enter", "↩", "Tab", "⇥", "Esc",
          "esc");

  private ShortcutText() {}

  static List<Alt> parse(String spec, boolean mac) {
    List<Alt> result = new ArrayList<>();
    for (String part : spec.trim().split(ALTERNATIVES)) {
      result.add(isCombo(part) ? new Alt(keys(part, mac), null) : new Alt(null, part));
    }
    return result;
  }

  static boolean isCombo(String part) {
    if (part.isEmpty() || part.contains(" ")) {
      return false;
    }
    for (String token : part.split("\\+")) {
      if (token.isEmpty() || (token.length() > 3 && !NAMED_KEYS.contains(token))) {
        return false;
      }
    }
    return true;
  }

  private static List<String> keys(String combo, boolean mac) {
    List<String> keys = new ArrayList<>();
    for (String token : combo.split("\\+")) {
      keys.add(mac ? MAC_SYMBOLS.getOrDefault(token, token) : token);
    }
    return keys;
  }
}
