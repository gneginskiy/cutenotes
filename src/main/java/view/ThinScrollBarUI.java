package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;

final class ThinScrollBarUI extends BasicScrollBarUI {

  private static final Color THUMB = new Color(128, 128, 128, 100);
  private static final Color THUMB_HOT = new Color(128, 128, 128, 170);
  private static final int INSET = 2;
  private static final int MIN_THUMB = 28;

  @Override
  protected void configureScrollBarColors() {
    thumbColor = THUMB;
    trackColor = new Color(0, 0, 0, 0);
  }

  @Override
  protected JButton createDecreaseButton(int orientation) {
    return zeroButton();
  }

  @Override
  protected JButton createIncreaseButton(int orientation) {
    return zeroButton();
  }

  @Override
  protected void paintTrack(Graphics g, JComponent c, Rectangle bounds) {
    // transparent track
  }

  @Override
  protected Dimension getMinimumThumbSize() {
    return new Dimension(MIN_THUMB, MIN_THUMB);
  }

  /** A pill floating inside the track; bars wider than 6px keep a small gap around it. */
  @Override
  protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
    if (bounds.isEmpty()) {
      return;
    }
    boolean vertical = scrollbar.getOrientation() == JScrollBar.VERTICAL;
    int thickness = vertical ? bounds.width : bounds.height;
    int side = thickness > 6 ? INSET : 0;
    Rectangle r =
        vertical
            ? new Rectangle(
                bounds.x + side, bounds.y + 1, bounds.width - 2 * side, bounds.height - 2)
            : new Rectangle(
                bounds.x + 1, bounds.y + side, bounds.width - 2, bounds.height - 2 * side);
    int arc = Math.min(r.width, r.height);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(isThumbRollover() ? THUMB_HOT : THUMB);
    g2.fillRoundRect(r.x, r.y, r.width, r.height, arc, arc);
    g2.dispose();
  }

  private JButton zeroButton() {
    JButton b = new JButton();
    Dimension zero = new Dimension(0, 0);
    b.setPreferredSize(zero);
    b.setMinimumSize(zero);
    b.setMaximumSize(zero);
    return b;
  }
}
