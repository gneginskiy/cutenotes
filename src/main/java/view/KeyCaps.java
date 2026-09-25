package view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.JComponent;

/** Draws a shortcut as small key caps ("⌘" "⇧" "T"), alternatives separated by a muted slash. */
final class KeyCaps extends JComponent {

  private static final int PAD_X = 6;
  private static final int PAD_Y = 3;
  private static final int KEY_GAP = 3;
  private static final int ALT_GAP = 8;
  private static final int ARC = 6;
  private static final String OR = "/";

  private final transient List<ShortcutText.Alt> alts;
  private final transient UiPalette palette;

  KeyCaps(List<ShortcutText.Alt> alts, UiPalette palette) {
    this.alts = alts;
    this.palette = palette;
    setFont(UiFonts.ui(Font.PLAIN, 12f));
  }

  @Override
  public Dimension getPreferredSize() {
    FontMetrics fm = getFontMetrics(getFont());
    return new Dimension(layout(null, fm), fm.getHeight() + 2 * PAD_Y + 2);
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setFont(getFont());
    layout(g2, g2.getFontMetrics());
    g2.dispose();
  }

  /** Measures (g == null) or draws the caps; returns the total width. */
  private int layout(Graphics2D g, FontMetrics fm) {
    int x = 0;
    for (int i = 0; i < alts.size(); i++) {
      if (i > 0) {
        x = text(g, fm, OR, x + ALT_GAP) + ALT_GAP;
      }
      ShortcutText.Alt alt = alts.get(i);
      if (alt.isKeys()) {
        for (String key : alt.keys()) {
          x = cap(g, fm, key, x) + KEY_GAP;
        }
        x -= KEY_GAP;
      } else {
        x = text(g, fm, alt.phrase(), x);
      }
    }
    return x;
  }

  private int cap(Graphics2D g, FontMetrics fm, String key, int x) {
    int w = Math.max(fm.stringWidth(key) + 2 * PAD_X, fm.getHeight() + PAD_Y);
    int h = fm.getHeight() + 2 * PAD_Y;
    if (g != null) {
      g.setColor(palette.chrome());
      g.fillRoundRect(x, 0, w, h, ARC, ARC);
      g.setColor(palette.border());
      g.drawRoundRect(x, 0, w - 1, h - 1, ARC, ARC);
      g.fillRect(x + 2, h, w - 4, 1);
      g.setColor(palette.fg());
      g.drawString(key, x + (w - fm.stringWidth(key)) / 2, PAD_Y + fm.getAscent());
    }
    return x + w;
  }

  private int text(Graphics2D g, FontMetrics fm, String s, int x) {
    if (g != null) {
      g.setColor(palette.muted());
      g.drawString(s, x, PAD_Y + fm.getAscent());
    }
    return x + fm.stringWidth(s);
  }
}
