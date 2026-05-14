package view;

import java.awt.Color;

final class Colors {

  private Colors() {}

  static Color darken(Color c, int delta) {
    return new Color(
        Math.max(0, c.getRed() - delta),
        Math.max(0, c.getGreen() - delta),
        Math.max(0, c.getBlue() - delta));
  }

  static Color inverse(Color c) {
    return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
  }

  static String toHex(Color c) {
    return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
  }
}
