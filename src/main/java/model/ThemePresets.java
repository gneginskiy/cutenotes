package model;

import java.awt.Color;
import java.util.List;

/** The built-in looks offered in Options. {@link #PAPER} is the default for new installs. */
public final class ThemePresets {

  public static final List<String> SANS =
      List.of(
          "SF Pro Text",
          "Helvetica Neue",
          "Segoe UI",
          "Inter",
          "Noto Sans",
          "Ubuntu",
          "Cantarell",
          "DejaVu Sans",
          "SansSerif");

  public static final List<String> SERIF =
      List.of("Charter", "Iowan Old Style", "Georgia", "Cambria", "Noto Serif", "Serif");

  public static final List<String> MONO =
      List.of(
          "SF Mono",
          "JetBrains Mono",
          "Menlo",
          "Cascadia Mono",
          "Consolas",
          "DejaVu Sans Mono",
          "Monospaced");

  public static final List<String> HAND =
      List.of("Comic Sans MS", "Chalkboard SE", "Comic Neue", "SansSerif");

  public static final ThemePreset PAPER =
      new ThemePreset("Paper", rgb(0xFAF8F3), rgb(0x2B2A28), rgb(0x0F7B6C), SANS);

  public static final ThemePreset GRAPHITE =
      new ThemePreset("Graphite", rgb(0x1F2023), rgb(0xDEDDDA), rgb(0x5CC8A8), SANS);

  public static final ThemePreset MIDNIGHT =
      new ThemePreset("Midnight", rgb(0x111726), rgb(0xC8D3E6), rgb(0x82AAFF), SANS);

  public static final ThemePreset SEPIA =
      new ThemePreset("Sepia", rgb(0xF3EBDA), rgb(0x3E3226), rgb(0xA4502F), SERIF);

  public static final ThemePreset STICKY =
      new ThemePreset("Sticky", new Color(250, 250, 180), Color.BLACK, rgb(0x0A846E), HAND);

  public static final ThemePreset TERMINAL =
      new ThemePreset("Terminal", rgb(0x0B0F0C), rgb(0x3CE05C), rgb(0xF5B942), MONO);

  public static final List<ThemePreset> ALL =
      List.of(PAPER, GRAPHITE, MIDNIGHT, SEPIA, STICKY, TERMINAL);

  private ThemePresets() {}

  /** The preset called {@code name}; Paper for an unknown name. */
  public static ThemePreset named(String name) {
    return ALL.stream().filter(p -> p.name().equals(name)).findFirst().orElse(PAPER);
  }

  private static Color rgb(int hex) {
    return new Color(hex);
  }
}
