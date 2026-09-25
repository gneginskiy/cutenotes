package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EasingTest {

  @Test
  void progressIsClampedFractionOfTheDuration() {
    long start = 1_000_000_000L;

    assertEquals(0, Easing.progress(start, start - 5_000_000L, 100));
    assertEquals(0.5, Easing.progress(start, start + 50_000_000L, 100), 1e-9);
    assertEquals(1, Easing.progress(start, start + 500_000_000L, 100));
    assertEquals(1, Easing.progress(start, start, 0), "zero duration finishes at once");
  }

  @Test
  void easeOutStartsFastAndLandsSoftly() {
    assertEquals(0, Easing.easeOutCubic(0), 1e-9);
    assertEquals(1, Easing.easeOutCubic(1), 1e-9);
    assertEquals(1, Easing.easeOutCubic(3), 1e-9);
    assertTrue(Easing.easeOutCubic(0.25) > 0.25, "ahead of linear early on");
    double previous = 0;
    for (int i = 1; i <= 100; i++) {
      double v = Easing.easeOutCubic(i / 100.0);
      assertTrue(v >= previous, "monotonic");
      previous = v;
    }
  }

  @Test
  void lerpInterpolatesBothWays() {
    assertEquals(18, Easing.lerp(0, 36, 0.5));
    assertEquals(9, Easing.lerp(36, 0, 0.75));
    assertEquals(36, Easing.lerp(0, 36, 1));
  }
}
