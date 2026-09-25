package view;

/** Time-based animation maths: motion that decelerates into place instead of a linear crawl. */
final class Easing {

  private static final double NANOS_PER_MS = 1_000_000d;

  private Easing() {}

  /** Fraction of {@code durationMs} elapsed between the two instants, clamped to 0..1. */
  static double progress(long startNanos, long nowNanos, int durationMs) {
    if (durationMs <= 0) {
      return 1;
    }
    return clamp((nowNanos - startNanos) / NANOS_PER_MS / durationMs);
  }

  /** Fast start, soft landing. */
  static double easeOutCubic(double t) {
    double u = 1 - clamp(t);
    return 1 - u * u * u;
  }

  static int lerp(int from, int to, double fraction) {
    return (int) Math.round(from + (to - from) * fraction);
  }

  private static double clamp(double t) {
    return Math.max(0, Math.min(1, t));
  }
}
