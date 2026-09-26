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

  /** WCAG contrast ratio of two colours, from 1 (same) to 21 (black on white). */
  static double contrastRatio(Color a, Color b) {
    double la = luminance(a);
    double lb = luminance(b);
    return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
  }

  /**
   * {@code text}, moved towards {@code strong} just enough to reach {@code minRatio} against every
   * given background (or {@code strong} itself when even that is not enough).
   */
  static Color legible(Color text, Color strong, double minRatio, Color... backs) {
    for (float f = 0f; f < 1f; f += 0.05f) {
      Color c = blend(text, strong, f);
      boolean ok = true;
      for (Color back : backs) {
        ok &= contrastRatio(c, back) >= minRatio;
      }
      if (ok) {
        return c;
      }
    }
    return strong;
  }

  private static double luminance(Color c) {
    return 0.2126 * linear(c.getRed())
        + 0.7152 * linear(c.getGreen())
        + 0.0722 * linear(c.getBlue());
  }

  private static double linear(int channel) {
    double s = channel / 255.0;
    return s <= 0.03928 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4);
  }

  static Color withAlpha(Color c, int alpha) {
    return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
  }

  static String toHex(Color c) {
    return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
  }
}
