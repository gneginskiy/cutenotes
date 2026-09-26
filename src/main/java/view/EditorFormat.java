package view;

import java.awt.Color;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import model.Theme;

/** Installs bold/italic/underline/strikethrough/code toggle shortcuts on a {@link JTextPane}. */
final class EditorFormat {

  static final String CODE = "cutenotes.code";

  private EditorFormat() {}

  static void install(JTextPane pane) {
    int mask = ShortcutMask.menu();
    for (InlineStyle style : InlineStyle.values()) {
      KeyBindings.bindFocused(
          pane, style.keyStroke(mask), "fmt-" + style.name(), () -> toggle(pane, style));
    }
  }

  static boolean isCode(AttributeSet attrs) {
    return attrs.getAttribute(CODE) == Boolean.TRUE;
  }

  static void styleCode(MutableAttributeSet attrs, boolean on) {
    styleCode(attrs, on, ThemeHolder.current());
  }

  /**
   * Applies (or clears) the monospace code-section look: font, code colour and shaded background,
   * using the colours of the given theme.
   */
  static void styleCode(MutableAttributeSet attrs, boolean on, Theme t) {
    attrs.addAttribute(CODE, on);
    StyleConstants.setFontFamily(
        attrs, on ? FontFamilies.mono() : FontFamilies.resolve(t.fontFamily()));
    StyleConstants.setForeground(attrs, on ? t.codeColor() : t.fg());
    StyleConstants.setBackground(attrs, on ? codeBackground(t) : t.bg());
  }

  static Color codeBackground(Theme t) {
    return Colors.contrast(t.bg(), 0.13f);
  }

  /**
   * Toggles {@code style}: on the selection (off only when all of it has the style), or on the
   * typing attributes when nothing is selected.
   */
  static void toggle(JTextPane pane, InlineStyle style) {
    int start = pane.getSelectionStart();
    int end = pane.getSelectionEnd();
    if (start == end) {
      MutableAttributeSet input = pane.getInputAttributes();
      style.set.accept(input, !style.isSet.test(input));
      return;
    }
    StyledDocument doc = pane.getStyledDocument();
    boolean allSet = true;
    for (int i = start; i < end; i++) {
      if (!style.isSet.test(doc.getCharacterElement(i).getAttributes())) {
        allSet = false;
        break;
      }
    }
    SimpleAttributeSet attr = new SimpleAttributeSet();
    style.set.accept(attr, !allSet);
    doc.setCharacterAttributes(start, end - start, attr, false);
  }
}
