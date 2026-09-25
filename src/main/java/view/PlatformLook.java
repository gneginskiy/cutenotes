package view;

import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import model.Theme;

/** Platform integration of the look: text antialiasing and a macOS title bar tinted to match. */
public final class PlatformLook {

  private static final String OS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

  static final boolean MAC = OS.contains("mac");
  static final boolean WINDOWS = OS.contains("win");

  private static final String TRANSPARENT_TITLE = "apple.awt.transparentTitleBar";

  /** The macOS appearance chosen at start-up; it cannot change while the app runs. */
  private static boolean darkAppearance;

  private PlatformLook() {}

  /**
   * Must run before the first window or dialog: these settings are read once, when AWT starts.
   * macOS: native Aqua plus an appearance matching the theme. Windows: the native look (Java would
   * otherwise use Metal, and its own ClearType settings are already right). Linux: Metal without
   * its bold labels, since the GTK look ignores the theme colours of menus, trees and fields, and
   * grayscale antialiasing, which suits every panel and scale factor.
   */
  public static void prepare(Theme t) {
    darkAppearance = Colors.isDark(UiPalette.of(t).chrome());
    if (MAC) {
      System.setProperty(
          "apple.awt.application.appearance",
          darkAppearance ? "NSAppearanceNameDarkAqua" : "NSAppearanceNameAqua");
    } else if (WINDOWS) {
      useSystemLookAndFeel();
    } else {
      setIfAbsent("awt.useSystemAAFontSettings", "on");
      UIManager.put("swing.boldMetal", Boolean.FALSE);
    }
  }

  private static void useSystemLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
      UIManager.put("swing.boldMetal", Boolean.FALSE);
    }
  }

  /**
   * Paints the macOS title bar in the chrome colour, so bar, tabs and editor read as one surface.
   * After a switch between a light and a dark theme the title text colour no longer fits, so the
   * native title bar comes back until the next start.
   */
  static void tintTitleBar(JFrame frame, UiPalette p) {
    if (!MAC) {
      return;
    }
    boolean fits = Colors.isDark(p.chrome()) == darkAppearance;
    frame.getRootPane().putClientProperty(TRANSPARENT_TITLE, fits);
    frame.setBackground(p.chrome());
  }

  private static void setIfAbsent(String key, String value) {
    if (System.getProperty(key) == null) {
      System.setProperty(key, value);
    }
  }
}
