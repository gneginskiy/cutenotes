package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ShortcutTextTest {

  @Test
  void combinationsBecomeKeysAndMacModifiersBecomeSymbols() {
    List<ShortcutText.Alt> alts = ShortcutText.parse("Cmd+Shift+T / Option+Tab", true);

    assertEquals(List.of("⌘", "⇧", "T"), alts.get(0).keys());
    assertEquals(List.of("⌥", "⇥"), alts.get(1).keys());
  }

  @Test
  void otherPlatformsKeepModifierNames() {
    assertEquals(
        List.of("Ctrl", "Shift", "="), ShortcutText.parse("Ctrl+Shift+=", false).get(0).keys());
  }

  @Test
  void phrasesStayPhrases() {
    List<ShortcutText.Alt> alts = ShortcutText.parse("Cmd+W / Middle-click", true);

    assertTrue(alts.get(0).isKeys());
    assertFalse(alts.get(1).isKeys());
    assertEquals("Middle-click", alts.get(1).phrase());
    assertNull(alts.get(1).keys());
    assertFalse(ShortcutText.parse("Right-click it", false).get(0).isKeys());
    assertFalse(ShortcutText.parse("Drag", false).get(0).isKeys());
  }

  @Test
  void symbolKeysAndFunctionKeysAreKeys() {
    assertEquals(List.of("⌘", "⇧", "-"), ShortcutText.parse("Cmd+Shift+-", true).get(0).keys());
    assertEquals(List.of("F1"), ShortcutText.parse("F1", true).get(0).keys());
    assertEquals(List.of("esc"), ShortcutText.parse("Esc", true).get(0).keys());
  }

  @Test
  void everyHelpRowParsesIntoKeysOrPhrases() {
    for (boolean mac : new boolean[] {true, false}) {
      checkHelpRows(mac);
    }
  }

  private static void checkHelpRows(boolean mac) {
    String[][][] tables = {
      HelpRows.tabs(mac), HelpRows.editing(mac), HelpRows.formatting(mac), HelpRows.display(mac)
    };
    for (String[][] table : tables) {
      for (String[] row : table) {
        for (ShortcutText.Alt alt : ShortcutText.parse(row[1], mac)) {
          if (alt.isKeys()) {
            assertFalse(mac && alt.keys().contains("Cmd"), row[0] + ": modifier not symbolised");
          } else {
            assertFalse(alt.phrase().contains("+"), row[0] + ": combo parsed as a phrase");
          }
        }
      }
    }
  }
}
