package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import javax.swing.Icon;

/**
 * A crisp, resolution-independent line icon in a single colour. Replaces emoji and text glyphs (📌,
 * ˄, ✕) whose look depended on the platform's fonts.
 */
final class VectorIcon implements Icon {

  enum Kind {
    CLOSE,
    PLUS,
    CHEVRON_UP,
    CHEVRON_DOWN,
    SEARCH,
    PIN,
    PIN_ON,
    NOTE,
    FOLDER
  }

  private static final float STROKE = 1.5f;

  private Kind kind;
  private final int size;
  private Color color = Color.GRAY;

  VectorIcon(Kind kind, int size) {
    this.kind = kind;
    this.size = size;
  }

  Kind kind() {
    return kind;
  }

  void setKind(Kind kind) {
    this.kind = kind;
  }

  Color color() {
    return color;
  }

  void setColor(Color color) {
    this.color = color;
  }

  @Override
  public int getIconWidth() {
    return size;
  }

  @Override
  public int getIconHeight() {
    return size;
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      g2.translate(x, y);
      g2.scale(size / IconShapes.GRID, size / IconShapes.GRID);
      g2.setColor(color);
      Shape fill = IconShapes.fill(kind);
      if (fill != null) {
        g2.fill(fill);
      }
      g2.setStroke(new BasicStroke(STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.draw(IconShapes.outline(kind));
    } finally {
      g2.dispose();
    }
  }
}
