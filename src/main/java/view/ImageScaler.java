package view;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;

/**
 * Scales pasted images for display. Halves step by step with bilinear filtering (fast and smooth,
 * where {@code getScaledInstance(SCALE_SMOOTH)} took ~19 ms per resize step for a Retina
 * screenshot), and adds a 2× variant so images stay sharp on high-density screens.
 */
final class ImageScaler {

  private ImageScaler() {}

  /** An image shown at {@code width × height}, with a double-resolution variant when possible. */
  static Image forDisplay(Image source, int width, int height) {
    BufferedImage src = toBuffered(source);
    BufferedImage base = scale(src, width, height);
    if (src.getWidth() < width * 2 || src.getHeight() < height * 2) {
      return base;
    }
    return new BaseMultiResolutionImage(base, scale(src, width * 2, height * 2));
  }

  static BufferedImage scale(BufferedImage src, int width, int height) {
    int w = Math.max(1, width);
    int h = Math.max(1, height);
    BufferedImage current = src;
    int cw = src.getWidth();
    int ch = src.getHeight();
    while (cw / 2 >= w && ch / 2 >= h) {
      cw /= 2;
      ch /= 2;
      current = draw(current, cw, ch);
    }
    return current.getWidth() == w && current.getHeight() == h ? current : draw(current, w, h);
  }

  static BufferedImage toBuffered(Image image) {
    if (image instanceof BufferedImage buffered) {
      return buffered;
    }
    return draw(image, Math.max(1, image.getWidth(null)), Math.max(1, image.getHeight(null)));
  }

  private static BufferedImage draw(Image src, int w, int h) {
    BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = out.createGraphics();
    g.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.drawImage(src, 0, 0, w, h, null);
    g.dispose();
    return out;
  }
}
