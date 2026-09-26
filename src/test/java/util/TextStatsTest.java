package util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TextStatsTest {

  @Test
  void countsWordsAndCharactersWithoutLineBreaks() {
    assertEquals(new TextStats(5, 26), TextStats.of("Hello, world!\r\nодин  два три"));
    assertEquals(new TextStats(0, 0), TextStats.of(""));
    assertEquals(new TextStats(0, 3), TextStats.of("   \n"));
  }
}
