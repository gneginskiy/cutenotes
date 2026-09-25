package view;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.function.BooleanSupplier;
import javax.swing.JComponent;
import javax.swing.Timer;

final class Revealer {

  private static final int FALLBACK_HEIGHT = 28;
  private static final int AUTO_HIDE_MS = 6000;
  private static final int FRAME_MS = 10;
  private static final int ANIM_MS = 180;

  private final JComponent target;
  private final JComponent layoutRoot;
  private final int fullHeight;
  private final Timer hideTimer;
  private final BooleanSupplier keepOpen;
  private Timer animTimer;
  private boolean shown;
  private boolean pinned;

  Revealer(JComponent target, JComponent layoutRoot) {
    this(target, layoutRoot, () -> false);
  }

  Revealer(JComponent target, JComponent layoutRoot, BooleanSupplier keepOpen) {
    this.target = target;
    this.layoutRoot = layoutRoot;
    this.keepOpen = keepOpen;
    this.fullHeight = Math.max(target.getPreferredSize().height, FALLBACK_HEIGHT);
    this.hideTimer = new Timer(AUTO_HIDE_MS, e -> autoHide());
    hideTimer.setRepeats(false);
    target.setPreferredSize(new Dimension(0, 0));
    target.setVisible(false);
    attachActivity();
  }

  private void autoHide() {
    if (keepOpen.getAsBoolean()) {
      hideTimer.restart();
    } else {
      hide();
    }
  }

  boolean isShown() {
    return shown;
  }

  boolean isPinned() {
    return pinned;
  }

  void setPinned(boolean pin) {
    this.pinned = pin;
    if (pin) {
      hideTimer.stop();
      show();
    } else {
      hideTimer.restart();
    }
  }

  void reveal() {
    show();
    hideTimer.restart();
  }

  void revealIf(boolean condition) {
    if (condition) {
      reveal();
    } else {
      hide();
    }
  }

  void show() {
    if (shown) {
      return;
    }
    shown = true;
    target.setVisible(true);
    animate(0, fullHeight, hideTimer::restart);
  }

  void hide() {
    if (!shown || pinned) {
      return;
    }
    shown = false;
    hideTimer.stop();
    animate(target.getHeight(), 0, () -> target.setVisible(false));
  }

  private void animate(int from, int to, Runnable onDone) {
    if (animTimer != null && animTimer.isRunning()) {
      animTimer.stop();
    }
    long start = System.nanoTime();
    animTimer = new Timer(FRAME_MS, e -> frame(from, to, start, onDone, (Timer) e.getSource()));
    animTimer.start();
  }

  private void frame(int from, int to, long start, Runnable onDone, Timer src) {
    double progress = Easing.progress(start, System.nanoTime(), ANIM_MS);
    int h = Easing.lerp(from, to, Easing.easeOutCubic(progress));
    target.setPreferredSize(new Dimension(target.getWidth(), h));
    layoutRoot.revalidate();
    layoutRoot.repaint();
    if (progress >= 1) {
      src.stop();
      onDone.run();
    }
  }

  private void attachActivity() {
    target.addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            hideTimer.restart();
          }
        });
    target.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            hideTimer.restart();
          }
        });
  }
}
