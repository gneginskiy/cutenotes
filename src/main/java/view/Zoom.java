package view;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import model.Theme;

final class Zoom {

  private static final int[] LEVELS = {6, 8, 10, 12, 14, 18, 24, 32, 48};

  private Zoom() {}

  static void install(JFrame frame, AppOptions options) {
    int mod = ShortcutMask.menu() | InputEvent.SHIFT_DOWN_MASK;
    bind(frame, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, mod), "zoom-in", options, 1);
    bind(frame, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, mod), "zoom-out", options, -1);
  }

  private static void bind(JFrame frame, KeyStroke ks, String name, AppOptions options, int delta) {
    KeyBindings.bind(frame.getRootPane(), ks, name, () -> step(options, delta));
  }

  private static void step(AppOptions options, int delta) {
    Theme t = ThemeHolder.current();
    int idx = nearestIndex(t.fontSize());
    int next = Math.max(0, Math.min(LEVELS.length - 1, idx + delta));
    if (LEVELS[next] == t.fontSize()) {
      return;
    }
    options.applyTheme(
        new Theme(
            t.bg(), t.fg(), t.caret(), t.fontFamily(), LEVELS[next], t.title(), t.alwaysOnTop()));
  }

  private static int nearestIndex(int size) {
    int best = 0;
    int bestDiff = Math.abs(LEVELS[0] - size);
    for (int i = 1; i < LEVELS.length; i++) {
      int diff = Math.abs(LEVELS[i] - size);
      if (diff < bestDiff) {
        best = i;
        bestDiff = diff;
      }
    }
    return best;
  }
}
