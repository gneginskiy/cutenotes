package view;

import java.awt.Font;
import java.util.Collection;
import java.util.Set;
import javax.swing.UIManager;

/** The platform's interface font at a given style and size, for chrome drawn by hand. */
final class UiFonts {

  private static final Set<String> LOGICAL =
      Set.of(Font.DIALOG, Font.DIALOG_INPUT, Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED);

  private UiFonts() {}

  static Font ui(int style, float size) {
    Font base = UIManager.getFont("Label.font");
    String lafFamily = base == null ? Font.DIALOG : base.getFamily();
    String family = family(lafFamily, FontFamilies.installed());
    if (base != null && family.equals(lafFamily)) {
      // Keep the LAF's own font object: the macOS system font cannot be recreated by name.
      return base.deriveFont(style, size);
    }
    return new Font(family, style, Math.round(size)).deriveFont(size);
  }

  /**
   * The look-and-feel font when it is a real desktop font (Aqua, Windows); Java's logical fonts,
   * which the Metal look reports on Linux, are replaced with the best installed UI font.
   */
  static String family(String lafFamily, Collection<String> installed) {
    return LOGICAL.contains(lafFamily) ? FontFamilies.resolve(null, installed) : lafFamily;
  }
}
