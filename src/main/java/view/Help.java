package view;

import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

final class Help {

  private Help() {}

  static void install(JFrame frame) {
    KeyBindings.bind(
        frame.getRootPane(),
        KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
        "help",
        () -> new HelpDialog(frame).setVisible(true));
  }
}
