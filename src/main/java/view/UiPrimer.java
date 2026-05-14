package view;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

final class UiPrimer {

  private UiPrimer() {}

  static void prime(Container container) {
    if (!(container instanceof JComponent jc)) {
      return;
    }
    int w = Math.max(1, jc.getWidth());
    int h = Math.max(1, jc.getHeight());
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics g = img.getGraphics();
    try {
      jc.printAll(g);
    } finally {
      g.dispose();
    }
  }
}
