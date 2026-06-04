package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;

import lombok.SneakyThrows;

/** Geometry and painting of the bottom-right resize grip shown on a selected inline image. */
final class ImageHandle {

  static final int SIZE = 10;

  private ImageHandle() {}

  @SneakyThrows
  static Rectangle bounds(JTextPane pane, int offset) {
    Rectangle2D r = pane.modelToView2D(offset);
    if (r == null) {
      return null;
    }
    AttributeSet a = pane.getStyledDocument().getCharacterElement(offset).getAttributes();
    int width = ImageAttr.width(a);
    int height = ImageAttr.height(a);
    return new Rectangle((int) r.getX() + width - SIZE, (int) r.getY() + height - SIZE, SIZE, SIZE);
  }

  @SneakyThrows
  static void paint(Graphics g, JTextPane pane, int offset) {
    Rectangle2D r = pane.modelToView2D(offset);
    if (r == null) {
      return;
    }
    AttributeSet a = pane.getStyledDocument().getCharacterElement(offset).getAttributes();
    int width = ImageAttr.width(a);
    int height = ImageAttr.height(a);
    int x = (int) r.getX();
    int y = (int) r.getY();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setColor(new Color(0, 0, 0, 110));
    g2.drawRect(x, y, width, height);
    g2.setColor(Color.BLACK);
    g2.fillRect(x + width - SIZE, y + height - SIZE, SIZE, SIZE);
    g2.dispose();
  }
}
