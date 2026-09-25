package model;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A curated look: colours plus a font preference list. Fonts are resolved against the installed
 * families, so one preset looks right on macOS, Windows and Linux alike.
 */
public record ThemePreset(String name, Color bg, Color fg, Color codeColor, List<String> fonts) {

  /**
   * The first of {@code candidates} that is installed, spelled as installed; the last candidate (a
   * logical Java font) when none is.
   */
  public static String firstInstalled(List<String> candidates, Collection<String> installed) {
    for (String candidate : candidates) {
      for (String family : installed) {
        if (family.equalsIgnoreCase(candidate)) {
          return family;
        }
      }
    }
    return candidates.get(candidates.size() - 1);
  }

  public String fontFamily(Collection<String> installed) {
    return firstInstalled(fonts, installed);
  }

  /** This preset's look on top of {@code base}: its size, title and window settings are kept. */
  public Theme applyTo(Theme base, Collection<String> installed) {
    return new Theme(
        bg,
        fg,
        null,
        codeColor,
        fontFamily(installed),
        base.fontSize(),
        base.title(),
        base.alwaysOnTop());
  }

  /** Whether {@code t} uses exactly this preset's colours. */
  public boolean matches(Theme t) {
    return bg.equals(t.bg())
        && fg.equals(t.fg())
        && t.caret() == null
        && Objects.equals(codeColor, t.codeColor());
  }
}
