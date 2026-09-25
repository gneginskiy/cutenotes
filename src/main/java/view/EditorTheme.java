package view;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import model.Theme;

/**
 * Applies a {@link Theme} to a text component: colours, font, a 2px caret, a selection tinted with
 * the accent colour and airy line spacing. Every property is written only when it differs, so
 * re-applying an unchanged theme fires no events.
 */
final class EditorTheme {

  /** Extra space under each line, as a fraction of the line height. */
  static final float LINE_SPACING = 0.15f;

  static final int CARET_WIDTH = 2;

  private EditorTheme() {}

  static void apply(JTextComponent area, Theme t) {
    applyColors(area, t);
    area.putClientProperty("caretWidth", CARET_WIDTH);
    Font next = new Font(FontFamilies.resolve(t.fontFamily()), Font.PLAIN, t.fontSize());
    if (!area.getFont().equals(next)) {
      area.setFont(next);
    }
    if (area instanceof JTextPane pane) {
      applyLineSpacing(pane.getStyledDocument());
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

  /**
   * Sets the spacing on the document's default style: every paragraph inherits it, and unlike
   * per-paragraph attributes it is neither an undoable edit nor part of the saved Markdown.
   */
  static void applyLineSpacing(StyledDocument doc) {
    Style base = doc.getStyle(StyleContext.DEFAULT_STYLE);
    if (base != null && Float.compare(StyleConstants.getLineSpacing(base), LINE_SPACING) != 0) {
      StyleConstants.setLineSpacing(base, LINE_SPACING);
    }
  }
}
