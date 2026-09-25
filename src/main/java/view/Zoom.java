package view;

import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import model.Theme;

final class Zoom {

  static final int[] LEVELS = {8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24, 28, 32, 40, 48};

  private Zoom() {}

  /** {@code notify} gets the resulting text size, so zooming gives visible feedback. */
  static void install(JFrame frame, AppOptions options, Consumer<String> notify) {
    int mask = ShortcutMask.menu();
    Consumer<Integer> zoom = delta -> notify.accept(step(options, delta));
    for (KeyStroke ks : PlatformKeys.zoomIn(mask)) {
      KeyBindings.bind(frame.getRootPane(), ks, "zoom-in", () -> zoom.accept(1));
    }
    for (KeyStroke ks : PlatformKeys.zoomOut(mask)) {
      KeyBindings.bind(frame.getRootPane(), ks, "zoom-out", () -> zoom.accept(-1));
    }
  }

  /**
   * The next level above ({@code delta > 0}) or below {@code size}; {@code size} itself at either
   * end. Sizes typed in Options between two levels snap to the neighbour in the zoom direction.
   */
  static int stepFrom(int size, int delta) {
    if (delta > 0) {
      for (int level : LEVELS) {
        if (level > size) {
          return level;
        }
      }
      return size;
    }
    for (int i = LEVELS.length - 1; i >= 0; i--) {
      if (LEVELS[i] < size) {
        return LEVELS[i];
      }
    }
    return size;
  }

  static String label(int size) {
    return "Text size " + size + " pt";
  }

  private static String step(AppOptions options, int delta) {
    Theme t = ThemeHolder.current();
    int size = stepFrom(t.fontSize(), delta);
    if (size == t.fontSize()) {
      return label(size) + (delta > 0 ? " · largest" : " · smallest");
    }
    options.applyTheme(
        new Theme(
            t.bg(),
            t.fg(),
            t.caret(),
            t.codeColor(),
            t.fontFamily(),
            size,
            t.title(),
            t.alwaysOnTop()));
    return label(size);
  }
}
