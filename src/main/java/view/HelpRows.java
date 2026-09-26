package view;

import static view.Messages.tr;

/**
 * Shortcut tables for the {@link HelpDialog}, grouped by section. Alternatives are separated by
 * {@code " / "}; see {@link ShortcutText} for how a cell is split into keys and phrases.
 */
final class HelpRows {

  private HelpRows() {}

  static String[][] tabs(boolean mac) {
    String cmd = PlatformKeys.menuKey(mac);
    return new String[][] {
      {tr("help.newTab"), cmd + "+T / " + tr("help.doubleClickBar")},
      {tr("help.closeTab"), cmd + "+W / " + tr("help.middleClick")},
      {tr("help.reopen"), cmd + "+Shift+T"},
      {tr("help.allNotes"), cmd + "+R"},
      {tr("help.findAll"), cmd + "+Shift+F"},
      {tr("help.nextTab"), PlatformKeys.nextTabHelp(mac)},
      {tr("help.previousTab"), PlatformKeys.previousTabHelp(mac)},
      {tr("help.tabNumber"), cmd + "+1…9"},
      {tr("help.renameTab"), tr("help.doubleClick") + " / " + tr("help.rightClick")},
      {tr("help.reorderTabs"), tr("help.drag")},
      {tr("help.pinTabs"), tr("help.pinButton")},
      {tr("help.summon"), tr("help.launchAgain")}
    };
  }

  static String[][] editing(boolean mac) {
    String cmd = PlatformKeys.menuKey(mac);
    return new String[][] {
      {tr("help.find"), cmd + "+F"},
      {tr("help.replace"), PlatformKeys.replaceHelp(mac)},
      {tr("help.nextMatch"), "Enter / Shift+Enter"},
      {tr("help.back"), PlatformKeys.navigationHelp(mac)},
      {tr("help.list"), "Enter"},
      {tr("help.indent"), "Tab / Shift+Tab"},
      {tr("help.checkbox"), cmd + "+Enter / " + tr("help.click")},
      {tr("help.openLink"), cmd + "+Click"},
      {tr("help.blankLines"), cmd + "+Enter"},
      {tr("help.cutCopyLine"), cmd + "+X / " + cmd + "+C"},
      {tr("help.duplicateLine"), cmd + "+D"},
      {tr("help.moveLine"), cmd + "+Shift+↑ / " + cmd + "+Shift+↓"},
      {tr("help.undo"), cmd + "+Z / " + cmd + "+Shift+Z"}
    };
  }

  static String[][] formatting(boolean mac) {
    String cmd = PlatformKeys.menuKey(mac);
    return new String[][] {
      {tr("help.bold"), cmd + "+B / " + cmd + "+I / " + cmd + "+U"},
      {tr("help.strike"), cmd + "+Shift+S"},
      {tr("help.code"), cmd + "+Shift+C"},
      {tr("help.heading"), tr("help.headingKeys")},
      {tr("help.pasteImage"), cmd + "+V"},
      {tr("help.pastePlain"), cmd + "+Shift+V"},
      {tr("help.resizeImage"), tr("help.rightClickIt")}
    };
  }

  static String[][] display(boolean mac) {
    String cmd = PlatformKeys.menuKey(mac);
    return new String[][] {
      {tr("help.zoom"), cmd + "+= / " + cmd + "+-"},
      {tr("help.showChrome"), "Esc"},
      {tr("help.export"), cmd + "+Shift+E"},
      {tr("help.print"), cmd + "+P"},
      {tr("help.lockAll"), cmd + "+Shift+L"},
      {tr("help.dropFiles"), tr("help.drag")},
      {tr("help.options"), cmd + "+O"},
      {tr("help.shortcuts"), "F1"}
    };
  }
}
