package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;

/** Painters of the link underline and the tag pill; they draw behind the text, per line piece. */
final class DecorationPainters {

  private DecorationPainters() {}

  static LayeredHighlighter.LayerPainter underline(Color color) {
    return new Piece() {
      @Override
      void draw(Graphics2D g, Rectangle r) {
        g.setColor(color);
        g.fillRect(r.x, r.y + r.height - 2, r.width, 1);
      }
    };
  }

  static LayeredHighlighter.LayerPainter pill(Color color) {
    return new Piece() {
      @Override
      void draw(Graphics2D g, Rectangle r) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        g.fillRoundRect(r.x - 2, r.y + 1, r.width + 4, r.height - 2, 8, 8);
      }
    };
  }

  private abstract static class Piece extends LayeredHighlighter.LayerPainter {

    abstract void draw(Graphics2D g, Rectangle r);

    @Override
    public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
      // only used through paintLayer
    }

    @Override
    public Shape paintLayer(Graphics g, int p0, int p1, Shape bounds, JTextComponent c, View view) {
      try {
        Shape s = view.modelToView(p0, Position.Bias.Forward, p1, Position.Bias.Backward, bounds);
        Rectangle r = s.getBounds();
        Graphics2D g2 = (Graphics2D) g.create();
        draw(g2, r);
        g2.dispose();
        return r;
      } catch (BadLocationException e) {
        return null;
      }
    }
  }
}
