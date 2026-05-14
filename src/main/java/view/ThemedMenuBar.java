package view;

import java.awt.Graphics;
import javax.swing.JMenuBar;

final class ThemedMenuBar extends JMenuBar {

  @Override
  protected void paintComponent(Graphics g) {
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
  }
}
