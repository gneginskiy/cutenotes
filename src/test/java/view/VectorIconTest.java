package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class VectorIconTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  @Test
  void everyGlyphDrawsInItsColourWithinItsSize() {
    for (VectorIcon.Kind kind : VectorIcon.Kind.values()) {
      VectorIcon icon = new VectorIcon(kind, 16);
      icon.setColor(Color.RED);
      BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = img.createGraphics();
      icon.paintIcon(null, g, 2, 2);
      g.dispose();

      assertEquals(16, icon.getIconWidth());
      assertTrue(inkInside(img, 2, 18) > 6, kind + " drew nothing");
      assertEquals(0, inkInside(img, 0, 1), kind + " bled outside its box");
    }
  }

  @Test
  void pinnedGlyphIsFilled() {
    VectorIcon outline = new VectorIcon(VectorIcon.Kind.PIN, 16);
    VectorIcon filled = new VectorIcon(VectorIcon.Kind.PIN, 16);
    filled.setKind(VectorIcon.Kind.PIN_ON);

    assertTrue(ink(filled) > ink(outline));
  }

  private static int ink(VectorIcon icon) {
    BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();
    icon.paintIcon(null, g, 0, 0);
    g.dispose();
    return inkInside(img, 0, 16);
  }

  /** Opaque pixels within rows/columns [from, to). */
  private static int inkInside(BufferedImage img, int from, int to) {
    int count = 0;
    for (int y = from; y < to; y++) {
      for (int x = from; x < to; x++) {
        if ((img.getRGB(x, y) >>> 24) > 40) {
          count++;
        }
      }
    }
    return count;
  }
}
