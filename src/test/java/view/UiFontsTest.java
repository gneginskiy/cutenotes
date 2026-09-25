package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class UiFontsTest {

  private static final List<String> LINUX = List.of("DejaVu Sans", "Noto Sans", "Dialog");

  @Test
  void aNativeLookAndFeelFontIsKept() {
    assertEquals("Segoe UI", UiFonts.family("Segoe UI", LINUX));
    assertEquals(".AppleSystemUIFont", UiFonts.family(".AppleSystemUIFont", LINUX));
  }

  @Test
  void javaLogicalFontsAreReplacedWithTheDesktopUiFont() {
    // Metal (Windows/Linux fallback) reports "Dialog", which maps to whatever Java bundles.
    assertEquals("Noto Sans", UiFonts.family("Dialog", LINUX));
    assertEquals("Noto Sans", UiFonts.family("SansSerif", LINUX));
  }
}
