package view;

import static view.Messages.tr;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Point;
import java.io.File;
import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyledDocument;

import lombok.SneakyThrows;

/** Resize, remove, open and the right-click menu for an inline image at a document offset. */
final class ImageActions {

  private ImageActions() {}

  static void showMenu(JTextPane pane, int x, int y, int offset) {
    JPopupMenu menu = new JPopupMenu();
    menu.add(tr("image.resize")).addActionListener(a -> promptResize(pane, offset));
    menu.add(tr("image.open")).addActionListener(a -> open(pane, offset));
    menu.add(tr("image.remove")).addActionListener(a -> remove(pane, offset));
    menu.show(pane, x, y);
  }

  static void promptResize(JTextPane pane, int offset) {
    String input =
        JOptionPane.showInputDialog(pane, tr("image.width"), ImageAttr.width(attrs(pane, offset)));
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
    Path path = NoteImages.store().resolve(ImageAttr.path(attrs(pane, offset)));
    File file = path == null ? null : path.toFile();
    if (file != null
        && file.isFile()
        && Desktop.isDesktopSupported()
        && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
      Desktop.getDesktop().open(file);
    }
  }

  @SneakyThrows
  static void remove(JTextPane pane, int offset) {
    pane.getStyledDocument().remove(offset, 1);
  }

  /** Offset of the image under {@code p} (or just left of it), or -1. */
  static int imageAt(JTextPane pane, Point p) {
    int offset = pane.viewToModel2D(p);
    if (isImage(pane, offset)) {
      return offset;
    }
    return isImage(pane, offset - 1) ? offset - 1 : -1;
  }

  private static boolean isImage(JTextPane pane, int offset) {
    return offset >= 0
        && offset < pane.getStyledDocument().getLength()
        && ImageAttr.isImage(attrs(pane, offset));
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
