package view;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.InputEvent;

final class ShortcutMask {

  private ShortcutMask() {}

  static int menu() {
    try {
      return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    } catch (HeadlessException e) {
      return InputEvent.CTRL_DOWN_MASK;
    }
  }
}
