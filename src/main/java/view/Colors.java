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

  static Color blend(Color a, Color b, float ratio) {
    float r = Math.max(0, Math.min(1, ratio));
    return new Color(
        (int) (a.getRed() * (1 - r) + b.getRed() * r),
        (int) (a.getGreen() * (1 - r) + b.getGreen() * r),
        (int) (a.getBlue() * (1 - r) + b.getBlue() * r));
  }

  static Color contrast(Color bg, float ratio) {
    int avg = (bg.getRed() + bg.getGreen() + bg.getBlue()) / 3;
    return blend(bg, avg < 128 ? Color.WHITE : Color.BLACK, ratio);
  }

  static Color divider(Color bg) {
    return contrast(bg, 0.25f);
  }

  static String toHex(Color c) {
    return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
  }
}
