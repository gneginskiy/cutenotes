package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * One tab of the tabs bar. The active tab is a raised card in the editor's own colour, with an
 * accent line on top, so it visually flows into the note below; the close button appears on hover.
 * The title keeps one font weight in every state, so selecting a tab never shifts its neighbours.
 */
final class TabChip extends JPanel {

  private static final int MAX_TITLE_PX = 180;
  private static final int TOP_GAP = 6;

  private final JLabel title = new JLabel();
  private final FlatButton close;
  private transient UiPalette palette = UiPalette.current();
  private JTextField editor;
  private boolean active;
  private boolean hover;

  TabChip(String name, Runnable onClose) {
    super(new BorderLayout(4, 0));
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(TOP_GAP, 14, 0, 6));
    title.setFont(UiFonts.ui(Font.PLAIN, 13f));
    close = new FlatButton(VectorIcon.Kind.CLOSE, 12, "Close tab", onClose);
    close.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    close.setGhost(true);
    add(title, BorderLayout.CENTER);
    add(close, BorderLayout.EAST);
    setTitle(name);
    MouseAdapter hoverTracker =
        new MouseAdapter() {
          @Override
          public void mouseEntered(MouseEvent e) {
            setHover(true);
          }

          @Override
          public void mouseExited(MouseEvent e) {
            // Moving onto the close button "exits" the chip; leaving the button exits only it.
            setHover(
                contains(
                    SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), TabChip.this)));
          }
        };
    addMouseListener(hoverTracker);
    close.addMouseListener(hoverTracker);
  }

  String title() {
    return title.getText();
  }

  void setTitle(String name) {
    title.setText(name);
    setToolTipText(name);
    Dimension pref = title.getUI() == null ? null : title.getUI().getPreferredSize(title);
    int width = pref == null ? MAX_TITLE_PX : Math.min(pref.width, MAX_TITLE_PX);
    title.setPreferredSize(new Dimension(width, pref == null ? 16 : pref.height));
    revalidate();
  }

  boolean isActive() {
    return active;
  }

  void setActive(boolean value) {
    active = value;
    restyle();
  }

  boolean isEditing() {
    return editor != null;
  }

  void applyPalette(UiPalette p) {
    palette = p;
    close.applyPalette(p);
    restyle();
  }

  /** Swaps the title for an inline field; {@code done} gets the text, or null when cancelled. */
  void startEdit(Consumer<String> done) {
    if (editor != null) {
      return;
    }
    editor =
        InlineEditor.create(
            title.getText(),
            title.getFont(),
            palette,
            (f, text) -> finishEdit(done, text),
            f -> finishEdit(done, null));
    remove(title);
    add(editor, BorderLayout.CENTER);
    revalidate();
    repaint();
    editor.requestFocusInWindow();
  }

  private void finishEdit(Consumer<String> done, String text) {
    if (editor == null) {
      return;
    }
    remove(editor);
    editor = null;
    add(title, BorderLayout.CENTER);
    revalidate();
    repaint();
    done.accept(text);
  }

  private void setHover(boolean value) {
    hover = value;
    restyle();
  }

  private void restyle() {
    title.setForeground(active || hover ? palette.fg() : palette.muted());
    close.setGhost(!active && !hover);
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (active || hover) {
      TabCard.paint(g, getWidth(), getHeight(), TOP_GAP, active ? palette.bg() : palette.hover());
    }
    if (active) {
      TabCard.paintAccent(g, getWidth(), getHeight(), TOP_GAP, palette.accent());
    }
  }
}
