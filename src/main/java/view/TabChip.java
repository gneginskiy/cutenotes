package view;

import static view.Messages.tr;

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
  private final transient ChipEditing editing = new ChipEditing(this);
  private boolean active;
  private boolean hover;

  TabChip(String name, Runnable onClose) {
    super(new BorderLayout(4, 0));
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(TOP_GAP, 14, 0, 6));
    title.setFont(UiFonts.ui(Font.PLAIN, 13f));
    close = new FlatButton(VectorIcon.Kind.CLOSE, 12, tr("tab.close"), onClose);
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

  /** Marks the tab with a note colour (a dot before the title); empty for none. */
  void setColor(String color) {
    title.setIcon(NoteColors.dot(color, 7));
    title.setIconTextGap(6);
    setTitle(title.getText());
  }

  String title() {
    return title.getText();
  }

  void setTitle(String name) {
    title.setText(name);
    setToolTipText(name);
    getAccessibleContext().setAccessibleName(name);
    close.getAccessibleContext().setAccessibleName(tr("tab.close") + ": " + name);
    Dimension pref = title.getUI() == null ? null : title.getUI().getPreferredSize(title);
    int width = pref == null ? MAX_TITLE_PX : Math.min(pref.width, MAX_TITLE_PX);
    title.setPreferredSize(new Dimension(width, pref == null ? 16 : pref.height));
    revalidate();
  }

  void setActive(boolean value) {
    active = value;
    restyle();
  }

  boolean isEditing() {
    return editing.active();
  }

  void applyPalette(UiPalette p) {
    palette = p;
    close.applyPalette(p);
    restyle();
  }

  /** Swaps the title for an inline field; {@code done} gets the text, or null when cancelled. */
  void startEdit(Consumer<String> done) {
    editing.start(title, palette, done);
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
