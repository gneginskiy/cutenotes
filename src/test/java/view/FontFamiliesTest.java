package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class FontFamiliesTest {

  @Test
  void installedFontIsFoundCaseInsensitively() {
    assertEquals("Georgia", FontFamilies.resolve("georgia", List.of("Arial", "Georgia")));
  }

  @Test
  void missingFontFallsBackToTheBestUiFontNotTheFirstInTheList() {
    List<String> installed = List.of("Abadi", "Arial", "Segoe UI", "SansSerif");

    assertEquals("Segoe UI", FontFamilies.resolve("comic sans ms", installed));
    assertEquals("Segoe UI", FontFamilies.resolve(null, installed));
  }

  @Test
  void theRealFontListAlwaysResolvesToAnInstalledFamily() {
    String resolved = FontFamilies.resolve("No Such Font 123");

    assertTrue(FontFamilies.installed().contains(resolved));
  }
}
