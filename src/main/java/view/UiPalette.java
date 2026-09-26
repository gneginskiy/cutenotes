package view;

import java.awt.Color;

import model.Theme;

/**
 * Design tokens of the window chrome, all derived from the user's {@link Theme}: one place decides
 * how bars, hover states, hairlines, secondary text and selections relate to the editor colours.
 *
 * @param chrome background of bars around the editor (tabs, search, menu)
 * @param hover background of a hovered control
 * @param border hairlines and outlines
 * @param muted secondary text and idle icons
 * @param accent highlights: active tab, focus ring, toggled buttons
 * @param selection text selection background
 * @param danger errors, such as a search without matches
 */
record UiPalette(
    Color bg,
    Color fg,
    Color chrome,
    Color hover,
    Color border,
    Color muted,
    Color accent,
    Color selection,
    Color danger) {

  /** WCAG AA for normal text: secondary text never drops below it. */
  static final double READABLE = 4.5;

  static final Color FALLBACK_ACCENT = new Color(15, 123, 108);
  private static final Color AMBER = new Color(255, 196, 0);
  private static final Color ORANGE = new Color(255, 140, 0);

  static UiPalette of(Theme t) {
    Color bg = t.bg();
    boolean dark = Colors.isDark(bg);
    Color accent = t.codeColor() != null ? t.codeColor() : FALLBACK_ACCENT;
    Color chrome = Colors.contrast(bg, dark ? 0.07f : 0.045f);
    Color muted = Colors.legible(Colors.blend(t.fg(), bg, 0.45f), t.fg(), READABLE, bg, chrome);
    return new UiPalette(
        bg,
        t.fg(),
        chrome,
        Colors.contrast(bg, dark ? 0.15f : 0.09f),
        Colors.contrast(bg, dark ? 0.2f : 0.13f),
        muted,
        accent,
        Colors.blend(bg, accent, dark ? 0.42f : 0.24f),
        dark ? new Color(255, 112, 102) : new Color(200, 55, 45));
  }

  static UiPalette current() {
    return of(ThemeHolder.current());
  }

  boolean dark() {
    return Colors.isDark(bg);
  }

  /** Background of search matches: amber mixed into the page, visible even on yellow paper. */
  Color match() {
    return Colors.blend(bg, AMBER, dark() ? 0.38f : 0.5f);
  }

  /** Background of the current search match. */
  Color activeMatch() {
    return Colors.blend(bg, ORANGE, dark() ? 0.7f : 0.8f);
  }
}
