package view;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.KeyStroke;

/**
 * Shortcuts that must differ by platform because Windows or Linux reserve the macOS choice, plus
 * the key variants that make a shortcut work on every keyboard layout.
 */
final class PlatformKeys {

  private static final int ALT = InputEvent.ALT_DOWN_MASK;
  private static final int CTRL = InputEvent.CTRL_DOWN_MASK;
  private static final int SHIFT = InputEvent.SHIFT_DOWN_MASK;

  private PlatformKeys() {}

  /** Alt+Tab is the window switcher of Windows and Linux; only macOS also gets Option+Tab. */
  static int[] nextTabModifiers(boolean mac) {
    return mac ? new int[] {ALT, CTRL} : new int[] {CTRL};
  }

  /**
   * Modifiers of back / forward caret navigation with ← / →. Ctrl+Alt+arrows switch workspaces on
   * GNOME and rotate the screen with some Windows graphics drivers, so Windows and Linux use
   * Alt+arrows like browsers; Ctrl+Alt stays as a second binding where the OS lets it through.
   */
  static int[] navigationModifiers(boolean mac, int menuMask) {
    return mac ? new int[] {menuMask | ALT} : new int[] {ALT, CTRL | ALT};
  }

  /**
   * Zoom in: Cmd/Ctrl with {@code =} or {@code +} (main row or keypad). Layouts without an {@code
   * =} key (German, French…) still reach one of them. The original Shift variant is kept.
   */
  static List<KeyStroke> zoomIn(int menuMask) {
    return List.of(
        KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, menuMask),
        KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, menuMask | SHIFT),
        KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, menuMask),
        KeyStroke.getKeyStroke(KeyEvent.VK_ADD, menuMask));
  }

  static List<KeyStroke> zoomOut(int menuMask) {
    return List.of(
        KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, menuMask),
        KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, menuMask | SHIFT),
        KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, menuMask));
  }

  /** Previous tab: Ctrl+Shift+Tab everywhere, plus Cmd+Shift+[ (macOS) or Ctrl+PgUp. */
  static List<KeyStroke> previousTab(boolean mac, int menuMask) {
    KeyStroke platform =
        mac
            ? KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, menuMask | SHIFT)
            : KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, CTRL);
    return List.of(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, CTRL | SHIFT), platform);
  }

  /** Extra next-tab keys besides {@link #nextTabModifiers}: Cmd+Shift+] (macOS) or Ctrl+PgDn. */
  static KeyStroke nextTabExtra(boolean mac, int menuMask) {
    return mac
        ? KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, menuMask | SHIFT)
        : KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, CTRL);
  }

  /** Find and replace: Cmd+Option+F on macOS, Ctrl+H elsewhere (the platform conventions). */
  static KeyStroke replace(boolean mac, int menuMask) {
    return mac
        ? KeyStroke.getKeyStroke(KeyEvent.VK_F, menuMask | ALT)
        : KeyStroke.getKeyStroke(KeyEvent.VK_H, CTRL);
  }

  static String previousTabHelp(boolean mac) {
    return mac ? "Ctrl+Shift+Tab / Cmd+Shift+[" : "Ctrl+Shift+Tab / Ctrl+PgUp";
  }

  static String replaceHelp(boolean mac) {
    return mac ? "Cmd+Option+F" : "Ctrl+H";
  }

  /** Help text of the next-tab shortcut. */
  static String nextTabHelp(boolean mac) {
    return mac ? "Option+Tab / Ctrl+Tab / Cmd+Shift+]" : "Ctrl+Tab / Ctrl+PgDn";
  }

  /** Help text of back / forward navigation. */
  static String navigationHelp(boolean mac) {
    return mac ? "Cmd+Option+← / Cmd+Option+→" : "Alt+← / Alt+→";
  }

  /** "Cmd" on macOS, "Ctrl" elsewhere, as printed in help texts. */
  static String menuKey(boolean mac) {
    return mac ? "Cmd" : "Ctrl";
  }
}
