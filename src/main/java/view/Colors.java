package view;

import java.awt.Color;

final class Colors {

  private Colors() {}

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

  /** Perceived brightness below the middle grey: light text belongs on this colour. */
  static boolean isDark(Color c) {
    return (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000 < 128;
  }

  /** Moves {@code bg} towards white on dark colours and towards black on light ones. */
  static Color contrast(Color bg, float ratio) {
    return blend(bg, isDark(bg) ? Color.WHITE : Color.BLACK, ratio);
  }

  static Color withAlpha(Color c, int alpha) {
    return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
  }

  static String toHex(Color c) {
    return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
  }
}
