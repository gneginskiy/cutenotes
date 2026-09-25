package view;

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.Timer;

/**
 * A heads-up message that floats over the bottom of the window and fades away by itself, such as
 * "Text size 18 pt". Feedback for actions that otherwise change nothing visible (or nothing at
 * all). It never takes focus and lets every click through.
 */
final class Toast extends JComponent {

  private static final int FRAME_MS = 16;
  private static final int PAD_X = 16;
  private static final int PAD_Y = 8;
  private static final int BOTTOM_GAP = 28;

  private final JLayeredPane layer;
  private final Timer timer = new Timer(FRAME_MS, e -> tick());
  private String text = "";
  private long shownAt;
  private float alpha;

  Toast(JRootPane root) {
    this.layer = root.getLayeredPane();
    layer.add(this, JLayeredPane.POPUP_LAYER);
    setFont(UiFonts.ui(Font.BOLD, 13f));
    setVisible(false);
  }

  void flash(String message) {
    text = message;
    shownAt = System.nanoTime();
    alpha = 0f;
    place();
    setVisible(true);
    timer.restart();
  }

  String text() {
    return isVisible() ? text : "";
  }

  @Override
  public boolean contains(int x, int y) {
    return false;
  }

  private void place() {
    FontMetrics fm = getFontMetrics(getFont());
    int w = fm.stringWidth(text) + PAD_X * 2;
    int h = fm.getHeight() + PAD_Y * 2;
    setBounds((layer.getWidth() - w) / 2, layer.getHeight() - h - BOTTOM_GAP, w, h);
  }

  private void tick() {
    long elapsed = (System.nanoTime() - shownAt) / 1_000_000;
    alpha = ToastFade.alpha(elapsed);
    if (ToastFade.done(elapsed)) {
      timer.stop();
      setVisible(false);
    }
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    UiPalette p = UiPalette.current();
    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g2.setColor(Colors.withAlpha(p.fg(), 232));
      g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
      g2.setColor(p.bg());
      FontMetrics fm = g2.getFontMetrics(getFont());
      g2.setFont(getFont());
      g2.drawString(text, PAD_X, PAD_Y + fm.getAscent());
    } finally {
      g2.dispose();
    }
  }
}
