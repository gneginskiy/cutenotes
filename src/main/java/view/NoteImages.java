package view;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

import dao.DataDir;
import lombok.SneakyThrows;

/** Saves pasted images next to the notes and inserts them inline as resizable icons. */
final class NoteImages {

  private static final String DIR = "images";
  private static final int MAX_WIDTH = 420;

  private NoteImages() {}

  @SneakyThrows
  static void paste(JTextComponent pane) {
    Transferable clip = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    if (clip == null) {
      pane.paste();
      return;
    }
    if (clip.isDataFlavorSupported(DataFlavor.imageFlavor)) {
      insert(pane, toBuffered((Image) clip.getTransferData(DataFlavor.imageFlavor)));
      return;
    }
    String text =
        clip.isDataFlavorSupported(DataFlavor.stringFlavor)
            ? (String) clip.getTransferData(DataFlavor.stringFlavor)
            : null;
    if (text != null && text.contains("![") && text.contains("](")) {
      int end =
          DocumentMarkdown.insertAt(
              (StyledDocument) pane.getDocument(), pane.getCaretPosition(), text);
      pane.setCaretPosition(end);
    } else {
      pane.paste();
    }
  }

  @SneakyThrows
  static void insert(JTextComponent pane, BufferedImage image) {
    String path = save(image);
    int width = Math.min(image.getWidth(), MAX_WIDTH);
    int height = scaledHeight(image, width);
    SimpleAttributeSet attrs = ImageAttr.of(path, image, width, height);
    StyledDocument doc = (StyledDocument) pane.getDocument();
    doc.insertString(pane.getCaretPosition(), " ", attrs);
  }

  static int scaledHeight(Image image, int width) {
    int natural = ((BufferedImage) image).getWidth();
    return Math.max(1, ((BufferedImage) image).getHeight() * width / Math.max(1, natural));
  }

  static BufferedImage load(String path) {
    try {
      return ImageIO.read(DataDir.resolve().resolve(path).toFile());
    } catch (IOException e) {
      return null;
    }
  }

  @SneakyThrows
  private static String save(BufferedImage image) {
    Path dir = DataDir.resolve().resolve(DIR);
    Files.createDirectories(dir);
    String name = "img_" + UUID.randomUUID() + ".png";
    ImageIO.write(image, "png", dir.resolve(name).toFile());
    return DIR + "/" + name;
  }

  private static BufferedImage toBuffered(Image image) {
    if (image instanceof BufferedImage buffered) {
      return buffered;
    }
    BufferedImage copy =
        new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    var g = copy.createGraphics();
    g.drawImage(image, 0, 0, null);
    g.dispose();
    return copy;
  }
}
