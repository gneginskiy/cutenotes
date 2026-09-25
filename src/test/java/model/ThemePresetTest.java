package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

class ThemePresetTest {

  @Test
  void firstInstalledFollowsThePreferenceOrderAndKeepsTheInstalledSpelling() {
    List<String> installed = List.of("Arial", "helvetica neue", "Segoe UI");

    assertEquals("helvetica neue", ThemePreset.firstInstalled(ThemePresets.SANS, installed));
  }

  @Test
  void firstInstalledFallsBackToTheLogicalFont() {
    assertEquals("SansSerif", ThemePreset.firstInstalled(ThemePresets.SANS, List.of("Arial")));
    assertEquals("Monospaced", ThemePreset.firstInstalled(ThemePresets.MONO, List.of()));
  }

  @Test
  void applyingAPresetKeepsSizeTitleAndWindowSettings() {
    Theme base = new Theme(Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, "X", 22, "Mine", false);

    Theme t = ThemePresets.GRAPHITE.applyTo(base, List.of("Segoe UI"));

    assertEquals(ThemePresets.GRAPHITE.bg(), t.bg());
    assertEquals(ThemePresets.GRAPHITE.fg(), t.fg());
    assertEquals(ThemePresets.GRAPHITE.codeColor(), t.codeColor());
    assertNull(t.caret(), "presets use the automatic caret colour");
    assertEquals("Segoe UI", t.fontFamily());
    assertEquals(22, t.fontSize());
    assertEquals("Mine", t.title());
    assertFalse(t.alwaysOnTop());
    assertTrue(ThemePresets.GRAPHITE.matches(t));
    assertFalse(ThemePresets.PAPER.matches(t));
  }

  @Test
  void aCustomCaretIsNotAPresetLook() {
    Theme t = ThemePresets.SEPIA.applyTo(Theme.DEFAULT, List.of());
    Theme withCaret =
        new Theme(t.bg(), t.fg(), Color.RED, t.codeColor(), t.fontFamily(), 15, null, true);

    assertFalse(ThemePresets.SEPIA.matches(withCaret));
  }

  @Test
  void theDefaultThemeIsThePaperPreset() {
    assertTrue(ThemePresets.PAPER.matches(Theme.DEFAULT));
    assertEquals(ThemePresets.SANS.get(0), Theme.DEFAULT.fontFamily());
  }

  @Test
  void presetsHaveDistinctNamesAndLooks() {
    assertEquals(
        ThemePresets.ALL.size(),
        new HashSet<>(ThemePresets.ALL.stream().map(ThemePreset::name).toList()).size());
    assertEquals(
        ThemePresets.ALL.size(),
        new HashSet<>(ThemePresets.ALL.stream().map(ThemePreset::bg).toList()).size());
  }
}
