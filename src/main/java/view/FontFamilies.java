package view;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

final class FontFamilies {

  private static final String[] CACHED =
      GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

  private FontFamilies() {}

  static String[] all() {
    return CACHED;
  }

  static String resolve(String preferred) {
    for (String f : CACHED) {
      if (f.equalsIgnoreCase(preferred)) {
        return f;
      }
    }
    return CACHED.length > 0 ? CACHED[0] : Font.SANS_SERIF;
  }
}
