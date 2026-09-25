package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * A borderless toolbar button: invisible at rest, a soft rounded plate on hover, accent-coloured
 * when switched on. Looks the same on every platform (no Aqua/Metal bevels).
 */
final class FlatButton extends JButton {

  private static final int ARC = 8;
  private static final Color CLEAR = new Color(0, 0, 0, 0);

  private final transient VectorIcon glyph;
  private transient UiPalette palette = UiPalette.current();
  private boolean on;
  private boolean ghost;

  FlatButton(VectorIcon.Kind kind, int iconSize, String tooltip, Runnable action) {
    this(new VectorIcon(kind, iconSize), null, tooltip, action);
  }

  FlatButton(String text, String tooltip, Runnable action) {
    this((VectorIcon) null, text, tooltip, action);
  }

  /** Icon and text side by side, for labelled toolbar buttons. */
  FlatButton(VectorIcon.Kind kind, String text, String tooltip, Runnable action) {
    this(new VectorIcon(kind, 14), text, tooltip, action);
    setIconTextGap(5);
  }

  private FlatButton(VectorIcon glyph, String text, String tooltip, Runnable action) {
    super(text, glyph);
    this.glyph = glyph;
    setContentAreaFilled(false);
    setBorderPainted(false);
    setFocusPainted(false);
    setFocusable(false);
    setRolloverEnabled(true);
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    setToolTipText(tooltip);
    addActionListener(e -> action.run());
  }

  @Override
  public void updateUI() {
    setUI(new BasicButtonUI());
  }

  void applyPalette(UiPalette p) {
    this.palette = p;
    repaint();
  }

  /** Toggled look: accent-coloured, with a faint plate even when not hovered. */
  void setOn(boolean value) {
    this.on = value;
    repaint();
  }

  boolean isOn() {
    return on;
  }

  void setGlyph(VectorIcon.Kind kind) {
    if (glyph != null) {
      glyph.setKind(kind);
      repaint();
    }
  }

  /** A ghost button hides its icon until hovered (the close button of an inactive tab). */
  void setGhost(boolean value) {
    this.ghost = value;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    boolean hot = getModel().isRollover() || getModel().isArmed();
    if (hot || on) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(hot ? palette.hover() : Colors.withAlpha(palette.accent(), 38));
      g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
      g2.dispose();
    }
    Color fg = on ? palette.accent() : hot ? palette.fg() : palette.muted();
    if (glyph != null) {
      glyph.setColor(ghost && !hot ? CLEAR : fg);
    }
    setForeground(fg);
    super.paintComponent(g);
  }
}
