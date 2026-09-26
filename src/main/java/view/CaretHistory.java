package view;

import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

import lombok.SneakyThrows;

/**
 * IntelliJ-style "back / forward to last edit location": records significant caret jumps and lets
 * the caret hop between them with {@code Cmd/Ctrl+Alt+Left/Right}.
 */
final class CaretHistory {

  private static final int JUMP = 40;
  private static final String QUIET = "cutenotes.quietCaret";

  private final transient JTextComponent area;
  private final transient Deque<Position> back = new ArrayDeque<>();
  private final transient Deque<Position> forward = new ArrayDeque<>();
  private int last;
  private boolean navigating;

  private CaretHistory(JTextComponent area) {
    this.area = area;
    area.addCaretListener(e -> onCaret(e.getDot()));
  }

  static void install(JTextComponent area) {
    CaretHistory history = new CaretHistory(area);
    for (int mod : PlatformKeys.navigationModifiers(PlatformLook.MAC, ShortcutMask.menu())) {
      bind(area, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, mod), "nav-back", history::back);
      bind(area, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, mod), "nav-forward", history::forward);
    }
  }

  /**
   * Runs caret moves that follow an edit (a moved line) rather than the user's navigation, so they
   * do not become "back" targets.
   */
  static void quietly(JTextComponent area, Runnable caretMoves) {
    area.putClientProperty(QUIET, Boolean.TRUE);
    try {
      caretMoves.run();
    } finally {
      area.putClientProperty(QUIET, null);
    }
  }

  private void onCaret(int dot) {
    boolean quiet = Boolean.TRUE.equals(area.getClientProperty(QUIET));
    if (!navigating && !quiet && Math.abs(dot - last) > JUMP) {
      back.push(mark(last));
      forward.clear();
    }
    last = dot;
  }

  private void back() {
    if (!back.isEmpty()) {
      forward.push(mark(area.getCaretPosition()));
      go(back.pop());
    }
  }

  private void forward() {
    if (!forward.isEmpty()) {
      back.push(mark(area.getCaretPosition()));
      go(forward.pop());
    }
  }

  @SneakyThrows
  private void go(Position target) {
    navigating = true;
    int pos = clamp(target.getOffset());
    area.setCaretPosition(pos);
    var rect = area.modelToView2D(pos);
    if (rect != null) {
      area.scrollRectToVisible(rect.getBounds());
    }
    last = pos;
    navigating = false;
  }

  @SneakyThrows
  private Position mark(int offset) {
    return area.getDocument().createPosition(clamp(offset));
  }

  private int clamp(int offset) {
    return Math.max(0, Math.min(offset, area.getDocument().getLength()));
  }

  private static void bind(JTextComponent area, KeyStroke ks, String name, Runnable action) {
    KeyBindings.bindFocused(area, ks, name, action);
  }
}
