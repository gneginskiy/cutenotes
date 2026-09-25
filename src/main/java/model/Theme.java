package model;

import java.awt.Color;

public record Theme(
    Color bg,
    Color fg,
    Color caret,
    Color codeColor,
    String fontFamily,
    int fontSize,
    String title,
    boolean alwaysOnTop) {

  /** The "Paper" preset; an uninstalled first-choice font falls back along the preset's list. */
  public static final Theme DEFAULT =
      new Theme(
          ThemePresets.PAPER.bg(),
          ThemePresets.PAPER.fg(),
          null,
          ThemePresets.PAPER.codeColor(),
          ThemePresets.PAPER.fonts().get(0),
          15,
          null,
          true);
}
