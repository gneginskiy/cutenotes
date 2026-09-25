package view;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

final class WindowPlacement {

  /** How much of the title bar must stay on a screen for saved bounds to be reused. */
  static final int GRAB_WIDTH = 80;

  static final int GRAB_HEIGHT = 20;
  static final int MIN_WIDTH = 240;
  static final int MIN_HEIGHT = 160;

  private WindowPlacement() {}

  static void centerOnOwner(Window window) {
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

  /** Reopens the window where it was; centred at {@code fallback} size when that is unusable. */
  static void restore(Window window, Rectangle saved, Dimension fallback) {
    if (fits(saved, screens())) {
      window.setBounds(saved);
    } else {
      window.setSize(fallback);
      centerOnScreen(window);
    }
  }

  /**
   * Whether {@code saved} is a sensible size and its title bar can still be grabbed on one of the
   * screens: a monitor that was unplugged since must not leave the window off-screen.
   */
  static boolean fits(Rectangle saved, List<Rectangle> screens) {
    if (saved == null || saved.width < MIN_WIDTH || saved.height < MIN_HEIGHT) {
      return false;
    }
    Rectangle titleBar = new Rectangle(saved.x, saved.y, saved.width, GRAB_HEIGHT);
    for (Rectangle screen : screens) {
      Rectangle visible = screen.intersection(titleBar);
      if (visible.width >= GRAB_WIDTH && visible.height >= GRAB_HEIGHT) {
        return true;
      }
    }
    return false;
  }

  private static List<Rectangle> screens() {
    List<Rectangle> result = new ArrayList<>();
    for (GraphicsDevice d : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
      result.add(d.getDefaultConfiguration().getBounds());
    }
    return result;
  }
}
