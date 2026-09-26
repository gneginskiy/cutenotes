package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ZoomTest {

  static {
    Messages.useLanguage("en");
  }

  @Test
  void stepsMoveToTheNeighbouringLevel() {
    assertEquals(16, Zoom.stepFrom(15, 1));
    assertEquals(14, Zoom.stepFrom(15, -1));
  }

  @Test
  void sizesBetweenLevelsSnapInTheZoomDirection() {
    assertEquals(18, Zoom.stepFrom(17, 1));
    assertEquals(16, Zoom.stepFrom(17, -1));
  }

  @Test
  void theEndsStayPut() {
    assertEquals(48, Zoom.stepFrom(48, 1));
    assertEquals(8, Zoom.stepFrom(8, -1));
    assertEquals(72, Zoom.stepFrom(72, 1), "a size above the scale never zooms back down");
    assertEquals(48, Zoom.stepFrom(72, -1));
  }

  @Test
  void labelNamesTheSize() {
    assertEquals("Text size 18 pt", Zoom.label(18));
  }
}
