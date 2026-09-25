package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import model.Theme;

class SearchFieldTest {

  static {
    System.setProperty("java.awt.headless", "true");
  }

  @Test
  void noResultsTurnsTheFieldRedUntilSomethingMatches() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          SearchField field = new SearchField("Find in note");
          UiPalette p = UiPalette.of(Theme.DEFAULT);
          field.applyPalette(p);

          field.setCounter("No results", true);
          assertTrue(field.isFailing());
          assertEquals("No results", field.counterText());

          field.setCounter("1 of 3", false);
          assertFalse(field.isFailing());
          assertEquals("Find in note", field.input().hint());
        });
  }

  @Test
  void paintsWithAndWithoutText() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          SearchField field = new SearchField("Search notes");
          field.setSize(300, 30);
          field.doLayout();
          BufferedImage img = new BufferedImage(300, 30, BufferedImage.TYPE_INT_ARGB);
          field.paint(img.getGraphics());
          field.input().setText("abc");
          field.paint(img.getGraphics());

          assertTrue((img.getRGB(150, 1) >>> 24) > 0, "rounded box is painted");
        });
  }
}
