package view;

import java.awt.GraphicsEnvironment;
import java.util.Collection;
import java.util.List;

import model.ThemePreset;
import model.ThemePresets;

final class FontFamilies {

  private static final String[] CACHED =
      GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

  private static final List<String> INSTALLED = List.of(CACHED);

  /** Menlo / Consolas / DejaVu Sans Mono…: the logical "Monospaced" is Courier New on Windows. */
  private static final String MONO = ThemePreset.firstInstalled(ThemePresets.MONO, INSTALLED);

  private FontFamilies() {}

  static String[] all() {
    return CACHED;
  }

  static List<String> installed() {
    return INSTALLED;
  }

  static String mono() {
    return MONO;
  }

  static String resolve(String preferred) {
    return resolve(preferred, INSTALLED);
  }

  /**
   * The installed spelling of {@code preferred}; a missing font falls back to the best installed UI
   * font instead of the alphabetically first family (which used to turn a missing font into a
   * random one, and saving Options then made that choice permanent).
   */
  static String resolve(String preferred, Collection<String> installed) {
    if (preferred != null) {
      for (String family : installed) {
        if (family.equalsIgnoreCase(preferred)) {
          return family;
        }
      }
    }
    return ThemePreset.firstInstalled(ThemePresets.SANS, installed);
  }
}
