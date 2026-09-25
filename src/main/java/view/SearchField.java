package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A rounded search box: magnifier, input with a placeholder and a trailing counter. The outline
 * turns accent-coloured while focused and red when nothing matches.
 */
final class SearchField extends JPanel {

  private static final int ARC = 10;

  private final VectorIcon glass = new VectorIcon(VectorIcon.Kind.SEARCH, 14);
  private final HintField input;
  private final JLabel counter = new JLabel();
  private transient UiPalette palette = UiPalette.current();
  private boolean failing;

  SearchField(String hint) {
    super(new BorderLayout(6, 0));
    input = new HintField(hint);
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 10));
    input.setFont(UiFonts.ui(Font.PLAIN, 13f));
    counter.setFont(UiFonts.ui(Font.PLAIN, 12f));
    add(new JLabel(glass), BorderLayout.WEST);
    add(input, BorderLayout.CENTER);
    add(counter, BorderLayout.EAST);
    input.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusGained(FocusEvent e) {
            repaint();
          }

          @Override
          public void focusLost(FocusEvent e) {
            repaint();
          }
        });
  }

  HintField input() {
    return input;
  }

  /** Shows {@code text} after the input; {@code error} paints it and the outline red. */
  void setCounter(String text, boolean error) {
    counter.setText(text);
    failing = error;
    counter.setForeground(error ? palette.danger() : palette.muted());
    repaint();
  }

  String counterText() {
    return counter.getText();
  }

  boolean isFailing() {
    return failing;
  }

  void applyPalette(UiPalette p) {
    palette = p;
    glass.setColor(p.muted());
    input.applyPalette(p);
    counter.setForeground(failing ? p.danger() : p.muted());
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(palette.bg());
    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
    g2.setColor(outline());
    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
    g2.dispose();
  }

  private Color outline() {
    if (failing) {
      return palette.danger();
    }
    return input.isFocusOwner() ? palette.accent() : palette.border();
  }
}
