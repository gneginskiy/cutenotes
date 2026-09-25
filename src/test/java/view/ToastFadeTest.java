package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ToastFadeTest {

  @Test
  void fadesInHoldsAndFadesOut() {
    assertEquals(0f, ToastFade.alpha(0));
    assertEquals(0.5f, ToastFade.alpha(ToastFade.FADE_IN_MS / 2), 0.01f);
    assertEquals(1f, ToastFade.alpha(ToastFade.FADE_IN_MS));
    assertEquals(1f, ToastFade.alpha(ToastFade.FADE_IN_MS + ToastFade.HOLD_MS - 1));
    float fading = ToastFade.alpha(ToastFade.FADE_IN_MS + ToastFade.HOLD_MS + 100);
    assertTrue(fading > 0f && fading < 1f);
    assertEquals(0f, ToastFade.alpha(ToastFade.TOTAL_MS));
  }

  @Test
  void isDoneOnlyAfterTheWholeCurve() {
    assertFalse(ToastFade.done(ToastFade.TOTAL_MS - 1));
    assertTrue(ToastFade.done(ToastFade.TOTAL_MS));
  }
}
