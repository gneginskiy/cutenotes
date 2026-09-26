package view;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;

/** The inline text styles, shared by the shortcuts, the Format menu and the text context menu. */
enum InlineStyle {
  BOLD("menu.format.bold", KeyEvent.VK_B, false, StyleConstants::isBold, StyleConstants::setBold),
  ITALIC(
      "menu.format.italic",
      KeyEvent.VK_I,
      false,
      StyleConstants::isItalic,
      StyleConstants::setItalic),
  UNDERLINE(
      "menu.format.underline",
      KeyEvent.VK_U,
      false,
      StyleConstants::isUnderline,
      StyleConstants::setUnderline),
  STRIKE(
      "menu.format.strike",
      KeyEvent.VK_S,
      true,
      StyleConstants::isStrikeThrough,
      StyleConstants::setStrikeThrough),
  CODE("menu.format.code", KeyEvent.VK_C, true, EditorFormat::isCode, EditorFormat::styleCode);

  final String labelKey;
  final Predicate<AttributeSet> isSet;
  final BiConsumer<MutableAttributeSet, Boolean> set;
  private final int key;
  private final boolean shift;

  InlineStyle(
      String labelKey,
      int key,
      boolean shift,
      Predicate<AttributeSet> isSet,
      BiConsumer<MutableAttributeSet, Boolean> set) {
    this.labelKey = labelKey;
    this.key = key;
    this.shift = shift;
    this.isSet = isSet;
    this.set = set;
  }

  /** Cmd (macOS) or Ctrl, plus Shift for strikethrough and code. */
  KeyStroke keyStroke(int menuMask) {
    return KeyStroke.getKeyStroke(key, shift ? menuMask | InputEvent.SHIFT_DOWN_MASK : menuMask);
  }
}
