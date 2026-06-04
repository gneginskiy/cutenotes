package view;

import java.awt.Color;
import java.awt.Font;
import javax.swing.text.JTextComponent;

import model.Theme;

/** Applies background, foreground, caret colour and font of a {@link Theme} to a text component. */
final class EditorTheme {

  private EditorTheme() {}

  static void apply(JTextComponent area, Theme t) {
    if (!area.getBackground().equals(t.bg())) {
      area.setBackground(t.bg());
    }
    if (!area.getForeground().equals(t.fg())) {
      area.setForeground(t.fg());
    }
    Color caret = t.caret() != null ? t.caret() : Colors.inverse(t.bg());
    if (!caret.equals(area.getCaretColor())) {
      area.setCaretColor(caret);
    }
    Font next = new Font(t.fontFamily(), Font.PLAIN, t.fontSize());
    if (!area.getFont().equals(next)) {
      area.setFont(next);
    }
  }
}
