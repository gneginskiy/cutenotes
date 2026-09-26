package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;

import org.junit.jupiter.api.Test;

class ImageScalerTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  @Test
  void scalesToTheRequestedSizeKeepingColours() {
    BufferedImage src = new BufferedImage(1600, 900, BufferedImage.TYPE_INT_ARGB);
    java.awt.Graphics2D g = src.createGraphics();
    g.setColor(Color.RED);
    g.fillRect(0, 0, 1600, 900);
    g.dispose();

    BufferedImage out = ImageScaler.scale(src, 160, 90);

    assertEquals(160, out.getWidth());
    assertEquals(90, out.getHeight());
    assertEquals(Color.RED.getRGB(), out.getRGB(80, 45));
  }

  @Test
  void largeSourcesGetARetinaVariantSmallOnesDoNot() {
    Image big =
        ImageScaler.forDisplay(new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB), 200, 150);
    Image small =
        ImageScaler.forDisplay(new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB), 200, 133);

    assertTrue(big instanceof MultiResolutionImage);
    assertEquals(2, ((MultiResolutionImage) big).getResolutionVariants().size());
    assertEquals(200, big.getWidth(null));
    assertTrue(small instanceof BufferedImage);
    assertEquals(200, small.getWidth(null));
  }

  @Test
  void upscalingAndDegenerateSizesAreSafe() {
    BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

    assertEquals(40, ImageScaler.scale(src, 40, 40).getWidth());
    assertEquals(1, ImageScaler.scale(src, 0, -3).getHeight());
    assertEquals(src, ImageScaler.scale(src, 10, 10));
  }
}
