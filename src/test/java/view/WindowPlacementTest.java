package view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import java.util.List;

import org.junit.jupiter.api.Test;

class WindowPlacementTest {

  private static final Rectangle LAPTOP = new Rectangle(0, 0, 1440, 900);
  private static final Rectangle LEFT_MONITOR = new Rectangle(-1920, 0, 1920, 1080);

  @Test
  void boundsOnAScreenAreReused() {
    assertTrue(WindowPlacement.fits(new Rectangle(100, 100, 560, 440), List.of(LAPTOP)));
    assertTrue(
        WindowPlacement.fits(new Rectangle(-1500, 50, 560, 440), List.of(LAPTOP, LEFT_MONITOR)));
  }

  @Test
  void boundsOnAnUnpluggedMonitorAreDropped() {
    assertFalse(WindowPlacement.fits(new Rectangle(-1500, 50, 560, 440), List.of(LAPTOP)));
  }

  @Test
  void aWindowWhoseTitleBarIsOffScreenIsDropped() {
    assertFalse(WindowPlacement.fits(new Rectangle(1400, 100, 560, 440), List.of(LAPTOP)));
    assertFalse(WindowPlacement.fits(new Rectangle(100, 890, 560, 440), List.of(LAPTOP)));
  }

  @Test
  void missingOrTinyBoundsAreDropped() {
    assertFalse(WindowPlacement.fits(null, List.of(LAPTOP)));
    assertFalse(WindowPlacement.fits(new Rectangle(10, 10, 50, 40), List.of(LAPTOP)));
  }
}
