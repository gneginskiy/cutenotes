package view;

import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

final class TabPanes {

  private static final int HEADER_HEIGHT = 40;
  private static final int SCROLLBAR_HEIGHT = 6;

  private TabPanes() {}

  static JScrollPane header(TabHeader header) {
    return header(header, HEADER_HEIGHT, SCROLLBAR_HEIGHT);
  }

  static JScrollPane header(TabHeader header, int height, int barHeight) {
    JScrollPane pane =
        new JScrollPane(
            header,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pane.setBorder(BorderFactory.createEmptyBorder());
    pane.setPreferredSize(new Dimension(0, height));
    pane.setMinimumSize(new Dimension(0, height));
    pane.getViewport().setBackground(header.getBackground());
    header.addPropertyChangeListener(
        "background", e -> pane.getViewport().setBackground(header.getBackground()));
    pane.setWheelScrollingEnabled(false);
    JScrollBar bar = pane.getHorizontalScrollBar();
    bar.setUI(new ThinScrollBarUI());
    bar.setOpaque(false);
    bar.setPreferredSize(new Dimension(0, barHeight));
    bar.setUnitIncrement(20);
    return pane;
  }

  static JScrollPane content(JTextArea area) {
    JScrollPane pane = new JScrollPane(area);
    pane.setBorder(BorderFactory.createEmptyBorder());
    pane.getViewport().setBackground(area.getBackground());
    area.addPropertyChangeListener(
        "background", e -> pane.getViewport().setBackground(area.getBackground()));
    return pane;
  }
}
