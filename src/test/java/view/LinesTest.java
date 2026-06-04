package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LinesTest {

  private static final String TEXT = "alpha\nbeta\ngamma";

  @Test
  void startFindsBeginningOfCurrentLine() {
    assertEquals(6, Lines.start(TEXT, 8));
    assertEquals(0, Lines.start(TEXT, 3));
  }

  @Test
  void endInclusiveCoversTrailingNewlineOrTextEnd() {
    assertEquals(6, Lines.endInclusive(TEXT, 2));
    assertEquals(TEXT.length(), Lines.endInclusive(TEXT, 13));
  }

  @Test
  void indexAndCount() {
    assertEquals(0, Lines.index(TEXT, 2));
    assertEquals(1, Lines.index(TEXT, 8));
    assertEquals(3, Lines.count(TEXT));
  }

  @Test
  void startOffsetOfNthLine() {
    assertEquals(0, Lines.startOffset(TEXT, 0));
    assertEquals(6, Lines.startOffset(TEXT, 1));
    assertEquals(11, Lines.startOffset(TEXT, 2));
  }
}
