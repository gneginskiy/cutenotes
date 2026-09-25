package view;

/**
 * Static shortcut tables for the {@link HelpDialog}, grouped by section. Alternatives are separated
 * by {@code " / "}; see {@link ShortcutText} for how a cell is split into keys and phrases.
 */
final class HelpRows {

  private HelpRows() {}

  static String[][] tabs(boolean mac) {
    String cmd = PlatformKeys.menuKey(mac);
    return new String[][] {
      {"New tab", cmd + "+T / Double-click the bar"},
      {"Close tab", cmd + "+W / Middle-click"},
      {"Reopen closed tab", cmd + "+Shift+T"},
      {"All notes: search & open", cmd + "+R"},
      {"Next tab", PlatformKeys.nextTabHelp(mac)},
      {"Rename tab", "Double-click / Right-click"},
      {"Reorder tabs", "Drag"},
      {"Keep the tabs bar shown", "Pin button"}
    };
  }

  static String[][] editing(boolean mac) {
    String cmd = PlatformKeys.menuKey(mac);
    return new String[][] {
      {"Find in note", cmd + "+F"},
      {"Next / previous match", "Enter / Shift+Enter"},
      {"Back / forward to last spot", PlatformKeys.navigationHelp(mac)},
      {"Insert 10 blank lines", cmd + "+Enter"},
      {"Cut / copy line", cmd + "+X / " + cmd + "+C"},
      {"Duplicate line", cmd + "+D"},
      {"Move line up / down", cmd + "+Shift+↑ / " + cmd + "+Shift+↓"},
      {"Undo / redo", cmd + "+Z / " + cmd + "+Shift+Z"}
    };
  }

  static String[][] formatting(boolean mac) {
    String cmd = PlatformKeys.menuKey(mac);
    return new String[][] {
      {"Bold / italic / underline", cmd + "+B / " + cmd + "+I / " + cmd + "+U"},
      {"Strikethrough", cmd + "+Shift+S"},
      {"Code", cmd + "+Shift+C"},
      {"Paste image", cmd + "+V"},
      {"Resize / remove image", "Right-click it"}
    };
  }

  static String[][] display(boolean mac) {
    String cmd = PlatformKeys.menuKey(mac);
    return new String[][] {
      {"Zoom in / out", cmd + "+= / " + cmd + "+-"},
      {"Show menu & tabs bar", "Esc"},
      {"Options", cmd + "+O"},
      {"Keyboard shortcuts", "F1"}
    };
  }
}
