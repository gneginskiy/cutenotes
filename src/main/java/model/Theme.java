package model;

import java.awt.Color;

public record Theme(
    Color bg,
    Color fg,
    Color caret,
    String fontFamily,
    int fontSize,
    String title,
    boolean alwaysOnTop) {

  public static final Theme DEFAULT =
      new Theme(new Color(250, 250, 180), Color.BLACK, null, "comic sans ms", 14, null, true);
}
