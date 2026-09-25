package view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.KeyStroke;

import org.junit.jupiter.api.Test;

class PlatformKeysTest {

  private static final int CMD = InputEvent.META_DOWN_MASK;
  private static final int CTRL = InputEvent.CTRL_DOWN_MASK;
  private static final int ALT = InputEvent.ALT_DOWN_MASK;

  @Test
  void altTabIsLeftToTheOsWindowSwitcherOnWindowsAndLinux() {
    assertArrayEquals(new int[] {CTRL}, PlatformKeys.nextTabModifiers(false));
    assertArrayEquals(new int[] {ALT, CTRL}, PlatformKeys.nextTabModifiers(true));
  }

  @Test
  void backForwardAvoidsCtrlAltArrowsOnWindowsAndLinux() {
    // Ctrl+Alt+arrows switch workspaces on GNOME and rotate the screen with some Windows drivers.
    int[] other = PlatformKeys.navigationModifiers(false, CTRL);
    assertArrayEquals(new int[] {ALT, CTRL | ALT}, other, "Alt+arrows first, like browsers");
    assertArrayEquals(new int[] {CMD | ALT}, PlatformKeys.navigationModifiers(true, CMD));
  }

  @Test
  void zoomWorksWithTheStandardKeysOnEveryKeyboardLayout() {
    List<KeyStroke> in = PlatformKeys.zoomIn(CTRL);
    List<KeyStroke> out = PlatformKeys.zoomOut(CTRL);

    assertTrue(in.contains(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, CTRL)));
    assertTrue(in.contains(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, CTRL)));
    assertTrue(in.contains(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, CTRL)), "numpad +");
    assertTrue(in.contains(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, CTRL | shift())));
    assertTrue(out.contains(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, CTRL)));
    assertTrue(out.contains(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, CTRL)), "numpad -");
    assertTrue(out.contains(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, CTRL | shift())));
  }

  @Test
  void helpShowsOnlyShortcutsThatWorkOnThePlatform() {
    String winTabs = String.join(" ", flatten(HelpRows.tabs(false)));
    String winEdit = String.join(" ", flatten(HelpRows.editing(false)));
    String macTabs = String.join(" ", flatten(HelpRows.tabs(true)));

    assertFalse(winTabs.contains("Alt+Tab"));
    assertFalse(winTabs.contains("Cmd") || winEdit.contains("Cmd") || winEdit.contains("Option"));
    assertTrue(winEdit.contains("Alt+← / Alt+→"));
    assertFalse(winEdit.contains("Ctrl+Alt+←"));
    assertTrue(macTabs.contains("Option+Tab"));
    assertTrue(String.join(" ", flatten(HelpRows.editing(true))).contains("Cmd+Option+←"));
  }

  private static int shift() {
    return InputEvent.SHIFT_DOWN_MASK;
  }

  private static List<String> flatten(String[][] rows) {
    return java.util.Arrays.stream(rows).map(r -> r[0] + " = " + r[1]).toList();
  }
}
