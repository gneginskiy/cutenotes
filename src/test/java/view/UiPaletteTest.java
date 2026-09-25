package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.List;

import org.junit.jupiter.api.Test;

import model.Theme;
import model.ThemePresets;

class UiPaletteTest {

  @Test
  void lightThemesGetDarkerChromeAndDarkThemesLighterChrome() {
    UiPalette light = UiPalette.of(ThemePresets.PAPER.applyTo(Theme.DEFAULT, List.of()));
    UiPalette dark = UiPalette.of(ThemePresets.GRAPHITE.applyTo(Theme.DEFAULT, List.of()));

    assertFalse(light.dark());
    assertTrue(dark.dark());
    assertTrue(brightness(light.chrome()) < brightness(light.bg()));
    assertTrue(brightness(dark.chrome()) > brightness(dark.bg()));
    assertTrue(brightness(light.hover()) < brightness(light.chrome()), "hover stands out more");
  }

  @Test
  void mutedTextSitsBetweenTextAndBackground() {
    UiPalette p = UiPalette.of(Theme.DEFAULT);

    float muted = brightness(p.muted());
    assertTrue(muted > brightness(p.fg()) && muted < brightness(p.bg()));
  }

  @Test
  void accentIsTheCodeColourWithAFallback() {
    Theme t = Theme.DEFAULT;
    Theme noCode = new Theme(t.bg(), t.fg(), null, null, t.fontFamily(), 15, null, true);

    assertEquals(t.codeColor(), UiPalette.of(t).accent());
    assertEquals(UiPalette.FALLBACK_ACCENT, UiPalette.of(noCode).accent());
    assertNotEquals(t.bg(), UiPalette.of(t).selection());
  }

  @Test
  void searchMatchesStandOutEvenOnYellowPaper() {
    UiPalette sticky = UiPalette.of(ThemePresets.STICKY.applyTo(Theme.DEFAULT, List.of()));

    assertTrue(distance(sticky.match(), sticky.bg()) > 60);
    assertTrue(distance(sticky.activeMatch(), sticky.bg()) > distance(sticky.match(), sticky.bg()));
  }

  @Test
  void perceivedBrightnessDecidesDarkness() {
    assertTrue(Colors.isDark(new Color(0, 0, 255)), "pure blue reads dark");
    assertFalse(Colors.isDark(new Color(255, 255, 0)), "pure yellow reads light");
    assertEquals(new Color(1, 2, 3, 40), Colors.withAlpha(new Color(1, 2, 3), 40));
  }

  private static int distance(Color a, Color b) {
    return Math.abs(a.getRed() - b.getRed())
        + Math.abs(a.getGreen() - b.getGreen())
        + Math.abs(a.getBlue() - b.getBlue());
  }

  private static float brightness(Color c) {
    return (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000f;
  }
}
