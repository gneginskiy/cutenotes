package view;

import java.awt.Desktop;
import java.awt.Image;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

import dao.DataDir;
import lombok.SneakyThrows;

/** Resize, remove, open and the right-click menu for an inline image at a document offset. */
final class ImageActions {

  private ImageActions() {}

  static void showMenu(JTextPane pane, int x, int y, int offset) {
    JPopupMenu menu = new JPopupMenu();
    menu.add("Resize…").addActionListener(a -> promptResize(pane, offset));
    menu.add("Open full image").addActionListener(a -> open(pane, offset));
    menu.add("Remove image").addActionListener(a -> remove(pane, offset));
    menu.show(pane, x, y);
  }

  static void promptResize(JTextPane pane, int offset) {
    String input =
        JOptionPane.showInputDialog(
            pane, "Image width (px):", ImageAttr.width(attrs(pane, offset)));
    int width = parse(input);
    if (width > 0) {
      applyWidth(pane, offset, width);
    }
  }

  @SneakyThrows
  static void applyWidth(JTextPane pane, int offset, int width) {
    AttributeSet a = attrs(pane, offset);
    Image source = ImageAttr.source(a);
    if (source == null) {
      return;
    }
    int height = NoteImages.scaledHeight(source, width);
    StyledDocument doc = pane.getStyledDocument();
    doc.remove(offset, 1);
    doc.insertString(offset, " ", ImageAttr.of(ImageAttr.path(a), source, width, height));
  }

  @SneakyThrows
  static void open(JTextPane pane, int offset) {
    File file = DataDir.resolve().resolve(ImageAttr.path(attrs(pane, offset))).toFile();
    if (file.isFile()
        && Desktop.isDesktopSupported()
        && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
      Desktop.getDesktop().open(file);
    }
  }

  @SneakyThrows
  static void remove(JTextPane pane, int offset) {
    pane.getStyledDocument().remove(offset, 1);
  }

  static AttributeSet attrs(JTextPane pane, int offset) {
    return pane.getStyledDocument().getCharacterElement(offset).getAttributes();
  }

  private static int parse(String value) {
    try {
      return value == null ? 0 : Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
