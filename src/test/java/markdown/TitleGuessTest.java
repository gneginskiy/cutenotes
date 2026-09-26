package markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TitleGuessTest {

  @Test
  void firstNonEmptyLineWithoutMarks() {
    assertEquals("Groceries", TitleGuess.from("\n\n  Groceries\nmilk"));
    assertEquals("Plan", TitleGuess.from("## Plan\n- a"));
    assertEquals("buy milk", TitleGuess.from("- [ ] buy milk"));
    assertEquals("bold idea", TitleGuess.from("**bold** *idea*"));
    assertEquals("snake_case_name", TitleGuess.from("snake_case_name"));
  }

  @Test
  void emptyTextHasNoTitle() {
    assertEquals("", TitleGuess.from(""));
    assertEquals("", TitleGuess.from(" \n \n- "));
  }

  @Test
  void longLinesAreCutAtAWord() {
    String title =
        TitleGuess.from("A very long first line that goes on and on about many different things");

    assertTrue(title.length() <= TitleGuess.MAX_LENGTH);
    assertTrue("A very long first line that goes on and on about".startsWith(title));
    assertEquals(TitleGuess.MAX_LENGTH, TitleGuess.from("x".repeat(80)).length());
  }
}
