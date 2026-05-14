package view;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;

final class WindowPlacement {

  private WindowPlacement() {}

  static void centerOnOwner(Window window) {
    //      if (true) return;
    Window owner = window.getOwner();
    if (owner == null) {
      centerOnScreen(window);
      return;
    }
    Rectangle ob = owner.getBounds();
    Dimension ds = window.getSize();
    window.setLocation(ob.x + (ob.width - ds.width) / 2, ob.y + (ob.height - ds.height) / 2);
  }

  static void centerOnScreen(Window window) {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension size = window.getSize();
    window.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
  }
}
