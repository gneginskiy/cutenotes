package view;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

import dao.DataDir;
import dao.ImageStore;
import lombok.SneakyThrows;

/** Saves pasted images next to the notes and inserts them inline as resizable icons. */
final class NoteImages {

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
      insert(pane, ImageScaler.toBuffered((Image) clip.getTransferData(DataFlavor.imageFlavor)));
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

  /** The image behind a note's path; {@code null} if missing or outside the images folder. */
  static BufferedImage load(String path) {
    Path file = store().resolve(path);
    try {
      return file == null ? null : ImageIO.read(file.toFile());
    } catch (IOException e) {
      return null;
    }
  }

  static ImageStore store() {
    return new ImageStore(DataDir.resolve());
  }

  private static String save(BufferedImage image) {
    return store().save(image);
  }
}
