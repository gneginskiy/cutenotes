package view;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.JComponent;

import model.ThemePreset;

/** A clickable theme card: the preset's paper, an "Aa" in its font and its accent colour. */
final class PresetSwatch extends JComponent {

  private static final int W = 88;
  private static final int H = 58;
  private static final int ARC = 12;

  private final transient ThemePreset preset;
  private final Font sample;
  private boolean selected;
  private boolean hover;

  PresetSwatch(ThemePreset preset, Consumer<ThemePreset> onPick) {
    this.preset = preset;
    this.sample = new Font(preset.fontFamily(FontFamilies.installed()), Font.PLAIN, 20);
    setPreferredSize(new Dimension(W, H));
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    setToolTipText(preset.name());
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            onPick.accept(preset);
          }

          @Override
          public void mouseEntered(MouseEvent e) {
            hover = true;
            repaint();
          }

          @Override
          public void mouseExited(MouseEvent e) {
            hover = false;
            repaint();
          }
        });
  }

  ThemePreset preset() {
    return preset;
  }

  void setSelected(boolean value) {
    selected = value;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    int w = getWidth() - 4;
    int h = getHeight() - 4;
    g2.setColor(preset.bg());
    g2.fillRoundRect(2, 2, w, h, ARC, ARC);
    g2.setFont(sample);
    g2.setColor(preset.fg());
    g2.drawString("Aa", 12, 8 + g2.getFontMetrics().getAscent());
    g2.setColor(preset.codeColor());
    g2.fillRoundRect(w - 16, 12, 8, 8, 8, 8);
    g2.setFont(UiFonts.ui(Font.PLAIN, 10f));
    FontMetrics fm = g2.getFontMetrics();
    g2.setColor(Colors.blend(preset.fg(), preset.bg(), 0.35f));
    g2.drawString(preset.name(), 12, h - 4 - fm.getDescent());
    g2.setColor(selected ? preset.codeColor() : Colors.contrast(preset.bg(), hover ? 0.4f : 0.18f));
    g2.setStroke(new BasicStroke(selected ? 2.5f : 1f));
    g2.drawRoundRect(2, 2, w, h, ARC, ARC);
    g2.dispose();
  }
}
