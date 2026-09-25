package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JTextField;

/** A borderless text field that shows a muted placeholder while it is empty. */
final class HintField extends JTextField {

  private final String hint;
  private Color hintColor = Color.GRAY;

  HintField(String hint) {
    this.hint = hint;
    setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    setOpaque(false);
  }

  String hint() {
    return hint;
  }

  void applyPalette(UiPalette p) {
    hintColor = p.muted();
    setForeground(p.fg());
    setCaretColor(p.accent());
    setSelectionColor(p.selection());
    setSelectedTextColor(p.fg());
    putClientProperty("caretWidth", EditorTheme.CARET_WIDTH);
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (!getText().isEmpty()) {
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setColor(hintColor);
    g2.setFont(getFont());
    Insets in = getInsets();
    int baseline =
        (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
    g2.drawString(hint, in.left, baseline);
    g2.dispose();
  }
}
