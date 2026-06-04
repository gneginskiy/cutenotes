package view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.swing.AbstractAction;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import model.Theme;

/** Installs bold/italic/underline/strikethrough/code toggle shortcuts on a {@link JTextPane}. */
final class EditorFormat {

  static final String CODE = "cutenotes.code";
  static final String MONO = "Monospaced";

  private EditorFormat() {}

  static void install(JTextPane pane) {
    int mask = ShortcutMask.menu();
    int shifted = mask | InputEvent.SHIFT_DOWN_MASK;
    bind(pane, KeyEvent.VK_B, mask, StyleConstants::isBold, StyleConstants::setBold);
    bind(pane, KeyEvent.VK_I, mask, StyleConstants::isItalic, StyleConstants::setItalic);
    bind(pane, KeyEvent.VK_U, mask, StyleConstants::isUnderline, StyleConstants::setUnderline);
    bind(
        pane,
        KeyEvent.VK_S,
        shifted,
        StyleConstants::isStrikeThrough,
        StyleConstants::setStrikeThrough);
    bind(pane, KeyEvent.VK_C, shifted, EditorFormat::isCode, EditorFormat::styleCode);
  }

  private static boolean isCode(AttributeSet attrs) {
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
    StyleConstants.setFontFamily(attrs, on ? MONO : t.fontFamily());
    StyleConstants.setForeground(attrs, on ? t.codeColor() : t.fg());
    StyleConstants.setBackground(attrs, on ? codeBackground(t) : t.bg());
  }

  static Color codeBackground(Theme t) {
    return Colors.contrast(t.bg(), 0.13f);
  }

  private static void bind(
      JTextPane pane,
      int key,
      int mod,
      Predicate<AttributeSet> getter,
      BiConsumer<MutableAttributeSet, Boolean> setter) {
    String name = "fmt-" + key + "-" + mod;
    pane.getInputMap().put(KeyStroke.getKeyStroke(key, mod), name);
    pane.getActionMap()
        .put(
            name,
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                toggle(pane, getter, setter);
              }
            });
  }

  private static void toggle(
      JTextPane pane,
      Predicate<AttributeSet> getter,
      BiConsumer<MutableAttributeSet, Boolean> set) {
    int start = pane.getSelectionStart();
    int end = pane.getSelectionEnd();
    if (start == end) {
      MutableAttributeSet input = pane.getInputAttributes();
      set.accept(input, !getter.test(input));
      return;
    }
    StyledDocument doc = pane.getStyledDocument();
    boolean allSet = true;
    for (int i = start; i < end; i++) {
      if (!getter.test(doc.getCharacterElement(i).getAttributes())) {
        allSet = false;
        break;
      }
    }
    SimpleAttributeSet attr = new SimpleAttributeSet();
    set.accept(attr, !allSet);
    doc.setCharacterAttributes(start, end - start, attr, false);
  }
}
