package view;

/** Opacity curve of a {@link Toast}: quick fade in, a short hold, then a gentle fade out. */
final class ToastFade {

  static final int FADE_IN_MS = 120;
  static final int HOLD_MS = 1100;
  static final int FADE_OUT_MS = 280;
  static final int TOTAL_MS = FADE_IN_MS + HOLD_MS + FADE_OUT_MS;

  private ToastFade() {}

  static float alpha(long elapsedMs) {
    if (elapsedMs <= 0 || elapsedMs >= TOTAL_MS) {
      return 0f;
    }
    if (elapsedMs < FADE_IN_MS) {
      return (float) elapsedMs / FADE_IN_MS;
    }
    long fadeOutStart = FADE_IN_MS + HOLD_MS;
    if (elapsedMs < fadeOutStart) {
      return 1f;
    }
    return 1f - (float) Easing.easeOutCubic((double) (elapsedMs - fadeOutStart) / FADE_OUT_MS);
  }

  static boolean done(long elapsedMs) {
    return elapsedMs >= TOTAL_MS;
  }
}
