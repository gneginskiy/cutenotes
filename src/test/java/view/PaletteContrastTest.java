package view;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.List;

import org.junit.jupiter.api.Test;

import model.Theme;
import model.ThemePreset;
import model.ThemePresets;

/** Secondary text must stay readable (WCAG AA, 4.5:1) on every built-in preset. */
class PaletteContrastTest {

  @Test
  void mutedTextIsReadableOnThePageAndTheBars() {
    StringBuilder failures = new StringBuilder();
    for (ThemePreset preset : ThemePresets.ALL) {
      UiPalette p = UiPalette.of(preset.applyTo(Theme.DEFAULT, List.of()));
      check(failures, preset.name() + " muted/bg", p.muted(), p.bg());
      check(failures, preset.name() + " muted/chrome", p.muted(), p.chrome());
      check(failures, preset.name() + " fg/bg", p.fg(), p.bg());
    }
    assertTrue(failures.isEmpty(), failures.toString());
  }

  private static void check(StringBuilder failures, String what, Color text, Color back) {
    double ratio = Colors.contrastRatio(text, back);
    if (ratio < UiPalette.READABLE) {
      failures.append(String.format("%s = %.2f%n", what, ratio));
    }
  }
}
