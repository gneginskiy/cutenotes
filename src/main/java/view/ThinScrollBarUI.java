package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

final class ThinScrollBarUI extends BasicScrollBarUI {

  private static final Color THUMB = new Color(120, 120, 120, 110);

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
  protected void paintThumb(Graphics g, JComponent c, Rectangle bounds) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(THUMB);
    g2.fillRoundRect(bounds.x, bounds.y + 1, bounds.width, bounds.height - 2, 6, 6);
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
