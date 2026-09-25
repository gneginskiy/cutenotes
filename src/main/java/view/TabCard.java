package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

/** Paints the rounded card behind a tab; its bottom runs off the edge to merge with the editor. */
final class TabCard {

  static final int ARC = 10;
  static final int ACCENT_PX = 2;
  private static final int SIDE_GAP = 2;

  private TabCard() {}

  static void paint(Graphics g, int width, int height, int top, Color fill) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(fill);
    g2.fill(card(width, height, top));
    g2.dispose();
  }

  /** The accent line along the top edge of the card, following its rounded corners. */
  static void paintAccent(Graphics g, int width, int height, int top, Color accent) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.clip(card(width, height, top));
    g2.setColor(accent);
    g2.fillRect(0, top, width, ACCENT_PX);
    g2.dispose();
  }

  private static Shape card(int width, int height, int top) {
    return new RoundRectangle2D.Float(
        SIDE_GAP, top, width - 2f * SIDE_GAP, height - top + (float) ARC, ARC, ARC);
  }
}
