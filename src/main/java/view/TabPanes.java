package view;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

final class TabPanes {

  private static final int HEADER_HEIGHT = 36;
  private static final int SCROLLBAR_HEIGHT = 6;
  private static final int CONTENT_BAR = 10;

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
    followBackground(header, pane);
    pane.setWheelScrollingEnabled(false);
    thin(pane.getHorizontalScrollBar(), new Dimension(0, barHeight));
    pane.getHorizontalScrollBar().setUnitIncrement(20);
    return pane;
  }

  static void focusLater(JTextComponent area) {
    SwingUtilities.invokeLater(() -> SwingUtilities.invokeLater(area::requestFocusInWindow));
  }

  /** A borderless scroller with thin overlay-style scrollbars that suit every theme. */
  static JScrollPane content(JComponent area) {
    JScrollPane pane = new JScrollPane(area);
    pane.setBorder(BorderFactory.createEmptyBorder());
    followBackground(area, pane);
    thin(pane.getVerticalScrollBar(), new Dimension(CONTENT_BAR, 0));
    thin(pane.getHorizontalScrollBar(), new Dimension(0, CONTENT_BAR));
    pane.getVerticalScrollBar().setUnitIncrement(16);
    if (area instanceof NoteEditor editor) {
      pane.getViewport()
          .addComponentListener(
              new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                  fit(editor, pane);
                }
              });
    }
    return pane;
  }

  /** Re-fits an editor inside its scroller (after a font or line-width change). */
  static void refit(NoteEditor editor) {
    if (SwingUtilities.getAncestorOfClass(JScrollPane.class, editor) instanceof JScrollPane pane) {
      fit(editor, pane);
    }
  }

  /** Re-centres the editor's text column for the current line-width setting. */
  static void fit(NoteEditor editor, JScrollPane pane) {
    editor.fitWidth(pane.getViewport().getWidth(), SettingsHolder.current().lineWidth());
  }

  private static void thin(JScrollBar bar, Dimension size) {
    bar.setUI(new ThinScrollBarUI());
    bar.setOpaque(false);
    bar.setPreferredSize(size);
  }

  /** The viewport and the corners always show the colour of the component they scroll. */
  private static void followBackground(JComponent view, JScrollPane pane) {
    Runnable sync =
        () -> {
          pane.getViewport().setBackground(view.getBackground());
          pane.setBackground(view.getBackground());
        };
    sync.run();
    view.addPropertyChangeListener("background", e -> sync.run());
  }
}
