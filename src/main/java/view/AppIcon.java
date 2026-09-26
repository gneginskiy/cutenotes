package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * The cuteNotes icon, drawn in code so it is sharp at every size: a teal rounded square holding a
 * note sheet with a folded corner, three lines of text and a small ":3".
 */
public final class AppIcon {

  static final int[] SIZES = {16, 20, 24, 32, 40, 48, 64, 128, 256, 512, 1024};

  private static final Color TOP = new Color(0x19B7A1);
  private static final Color BOTTOM = new Color(0x0A6A5E);
  private static final Color SHEET = new Color(0xFBFAF6);
  private static final Color FOLD = new Color(0xDCD6C8);
  private static final Color LINE = new Color(0x9CB6B0);

  private AppIcon() {}

  /** Window and taskbar icons in the usual sizes. */
  public static List<BufferedImage> images() {
    List<BufferedImage> images = new ArrayList<>();
    for (int size : new int[] {16, 32, 48, 64, 128, 256}) {
      images.add(image(size));
    }
    return images;
  }

  public static BufferedImage image(int size) {
    BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    paint(g, size);
    g.dispose();
    return img;
  }

  static void paint(Graphics2D g, int size) {
    float s = size;
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    float inset = s * 0.06f;
    g.setPaint(new GradientPaint(0, 0, TOP, s, s, BOTTOM));
    g.fill(
        new RoundRectangle2D.Float(inset, inset, s - 2 * inset, s - 2 * inset, s * 0.4f, s * 0.4f));
    sheet(g, s);
    lines(g, s);
    face(g, s);
  }

  private static void sheet(Graphics2D g, float s) {
    float x = s * 0.25f;
    float y = s * 0.2f;
    float w = s * 0.5f;
    float h = s * 0.6f;
    float fold = s * 0.14f;
    Path2D.Float page = new Path2D.Float();
    page.moveTo(x, y);
    page.lineTo(x + w - fold, y);
    page.lineTo(x + w, y + fold);
    page.lineTo(x + w, y + h);
    page.lineTo(x, y + h);
    page.closePath();
    g.setColor(new Color(0, 0, 0, 40));
    g.translate(0, s * 0.015f);
    g.fill(page);
    g.translate(0, -s * 0.015f);
    g.setColor(SHEET);
    g.fill(page);
    Path2D.Float corner = new Path2D.Float();
    corner.moveTo(x + w - fold, y);
    corner.lineTo(x + w - fold, y + fold);
    corner.lineTo(x + w, y + fold);
    corner.closePath();
    g.setColor(FOLD);
    g.fill(corner);
  }

  private static void lines(Graphics2D g, float s) {
    if (s < 24) {
      return;
    }
    g.setColor(LINE);
    g.setStroke(new BasicStroke(s * 0.035f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    float x = s * 0.33f;
    float[] lengths = {0.26f, 0.34f, 0.22f};
    for (int i = 0; i < lengths.length; i++) {
      float y = s * (0.4f + i * 0.09f);
      g.draw(new Line2D.Float(x, y, x + s * lengths[i], y));
    }
  }

  private static void face(Graphics2D g, float s) {
    if (s < 32) {
      return;
    }
    g.setColor(BOTTOM);
    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.round(s * 0.13f)));
    g.drawString(":3", s * 0.53f, s * 0.74f);
  }
}
