package view;

import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.StyledDocument;

/** Selection grip drag-resize, double-click-to-open and the context menu for inline images. */
class ImageMouse extends MouseInputAdapter {

  private static final int MIN_WIDTH = 24;

  private final transient NoteEditor pane;
  private int resizeOffset = -1;
  private int startX;
  private int startWidth;

  ImageMouse(NoteEditor pane) {
    this.pane = pane;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger()) {
      menu(e);
      return;
    }
    int selected = pane.selectedImage();
    if (selected >= 0 && onGrip(selected, e.getPoint())) {
      resizeOffset = selected;
      startX = e.getX();
      startWidth = ImageAttr.width(ImageActions.attrs(pane, selected));
      setDrag(false);
      return;
    }
    pane.setSelectedImage(imageAt(e.getPoint()));
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger()) {
      menu(e);
    }
    if (resizeOffset >= 0) {
      resizeOffset = -1;
      setDrag(true);
    }
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    int offset = imageAt(e.getPoint());
    if (e.getClickCount() == 2 && offset >= 0) {
      ImageActions.open(pane, offset);
    }
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (resizeOffset >= 0) {
      ImageActions.applyWidth(
          pane, resizeOffset, Math.max(MIN_WIDTH, startWidth + e.getX() - startX));
      pane.setSelectedImage(resizeOffset);
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    int selected = pane.selectedImage();
    boolean grip = selected >= 0 && onGrip(selected, e.getPoint());
    pane.setCursor(Cursor.getPredefinedCursor(grip ? Cursor.SE_RESIZE_CURSOR : Cursor.TEXT_CURSOR));
  }

  private void menu(MouseEvent e) {
    int offset = imageAt(e.getPoint());
    if (offset >= 0) {
      pane.setSelectedImage(offset);
      ImageActions.showMenu(pane, e.getX(), e.getY(), offset);
    }
  }

  private boolean onGrip(int offset, Point p) {
    Rectangle grip = ImageHandle.bounds(pane, offset);
    return grip != null && grip.contains(p);
  }

  private int imageAt(Point p) {
    int offset = pane.viewToModel2D(p);
    if (isImage(offset)) {
      return offset;
    }
    return isImage(offset - 1) ? offset - 1 : -1;
  }

  private boolean isImage(int offset) {
    StyledDocument doc = pane.getStyledDocument();
    return offset >= 0
        && offset < doc.getLength()
        && ImageAttr.isImage(ImageActions.attrs(pane, offset));
  }

  private void setDrag(boolean enabled) {
    if (!GraphicsEnvironment.isHeadless()) {
      pane.setDragEnabled(enabled);
    }
  }
}
