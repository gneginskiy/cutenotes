package view;

/** Static shortcut tables for the {@link HelpDialog}, grouped by section. */
final class HelpRows {

  private HelpRows() {}

  static String[][] tabs(String cmd, String alt) {
    return new String[][] {
      {"New tab", cmd + "+T"},
      {"Close tab", cmd + "+W"},
      {"Reopen last closed", cmd + "+Shift+T"},
      {"Browse notes & groups…", cmd + "+R"},
      {"Reorder / regroup notes", "Drag inside " + cmd + "+R"},
      {"Pin tabs bar", "📌 button"},
      {"Next tab", alt + "+Tab"},
      {"Rename tab", "Double-click or right-click"}
    };
  }

  static String[][] editing(String cmd) {
    return new String[][] {
      {"Find in current tab (case-insensitive)", cmd + "+F"},
      {"Next / previous match", "Enter / Shift+Enter  (or ↓ / ↑)"},
      {"Insert 10 blank lines", cmd + "+Enter"},
      {"Cut line", cmd + "+X"},
      {"Copy line", cmd + "+C"},
      {"Duplicate line", cmd + "+D"},
      {"Move line up / down", cmd + "+Shift+↑  /  " + cmd + "+Shift+↓"},
      {"Undo / Redo", cmd + "+Z  /  " + cmd + "+Shift+Z"}
    };
  }

  static String[][] formatting(String cmd) {
    return new String[][] {
      {"Bold / Italic / Underline", cmd + "+B  /  " + cmd + "+I  /  " + cmd + "+U"},
      {"Strikethrough", cmd + "+Shift+S"},
      {"Code (monospace)", cmd + "+Shift+C"},
      {"Paste image from clipboard", cmd + "+V"},
      {"Resize / remove image", "Right-click the image"}
    };
  }

  static String[][] display(String cmd) {
    return new String[][] {
      {"Zoom in / out", cmd + "+Shift+=  /  " + cmd + "+Shift+-"},
      {"Toggle menu & tabs strip", "Esc"},
      {"Options…", cmd + "+O"}
    };
  }
}
