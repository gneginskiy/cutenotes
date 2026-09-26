package markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ListLineTest {

  @Test
  void bulletsContinueWithTheSameMarkerAndIndent() {
    ListLine item = ListLine.parse("  * milk");

    assertEquals("  * ", item.continuation());
    assertEquals("milk", item.rest());
    assertEquals(4, item.prefixLength());
    assertFalse(item.checkbox());
  }

  @Test
  void numbersCountUpKeepingTheirDelimiter() {
    assertEquals("3. ", ListLine.parse("2. second").continuation());
    assertEquals("10) ", ListLine.parse("9) ninth").continuation());
  }

  @Test
  void checkboxesContinueUnchecked() {
    ListLine done = ListLine.parse("- [x] ship it");

    assertTrue(done.checkbox());
    assertTrue(done.checked());
    assertEquals("- [ ] ", done.continuation());
    assertEquals(3, done.checkOffset());
    assertEquals(6, done.prefixLength());
    assertFalse(ListLine.parse("- [ ] todo").checked());
    assertTrue(ListLine.parse("- [X] caps").checked());
  }

  @Test
  void emptyItemsAreRecognised() {
    assertTrue(ListLine.parse("- ").isEmpty());
    assertTrue(ListLine.parse("- [ ]").isEmpty());
    assertTrue(ListLine.parse("1. ").isEmpty());
    assertEquals(-1, ListLine.parse("- x").checkOffset());
  }

  @Test
  void ordinaryLinesAreNotLists() {
    assertNull(ListLine.parse("plain text"));
    assertNull(ListLine.parse("-"));
    assertNull(ListLine.parse("---"));
    assertNull(ListLine.parse("-5 degrees"));
    assertNull(ListLine.parse("**bold**"));
    assertNull(ListLine.parse(""));
  }
}
