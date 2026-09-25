package view;

import java.awt.Color;
import java.awt.Font;
import javax.swing.text.JTextComponent;

import model.Theme;

/**
 * Applies a {@link Theme} to a text component: colours, font, a 2px caret and a selection tinted
 * with the accent colour (line spacing comes from {@link NoteEditorKit}). Every property is written
 * only when it differs, so re-applying an unchanged theme fires no events.
 */
final class EditorTheme {

  static final int CARET_WIDTH = 2;

  private EditorTheme() {}

  static void apply(JTextComponent area, Theme t) {
    applyColors(area, t);
    area.putClientProperty("caretWidth", CARET_WIDTH);
    Font next = new Font(FontFamilies.resolve(t.fontFamily()), Font.PLAIN, t.fontSize());
    if (!area.getFont().equals(next)) {
      area.setFont(next);
    }
  }

  private static void applyColors(JTextComponent area, Theme t) {
    UiPalette p = UiPalette.of(t);
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
    if (!p.selection().equals(area.getSelectionColor())) {
      area.setSelectionColor(p.selection());
    }
    if (!t.fg().equals(area.getSelectedTextColor())) {
      area.setSelectedTextColor(t.fg());
    }
  }
}
