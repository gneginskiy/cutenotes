package view;

import static view.Messages.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.time.Instant;
import java.time.ZoneId;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import model.NoteMeta;
import model.TabMeta;

/**
 * Notes browser rows: an icon (pinned notes get a pin; the note colour or, for open notes, the
 * accent tints it), the name, and a muted detail — the text around a search match, or a relative
 * date ("Yesterday"); groups show their item count.
 */
class NoteCellRenderer extends DefaultTreeCellRenderer {

  private static final int GAP = 10;

  private final transient BrowserQuery query;
  private final transient VectorIcon noteIcon = new VectorIcon(VectorIcon.Kind.NOTE, 14);
  private final transient VectorIcon folderIcon = new VectorIcon(VectorIcon.Kind.FOLDER, 14);
  private transient UiPalette palette = UiPalette.current();
  private String detail = "";

  NoteCellRenderer(BrowserQuery query) {
    this.query = query;
    setIconTextGap(7);
    applyPalette(palette);
  }

  final void applyPalette(UiPalette p) {
    palette = p;
    folderIcon.setColor(p.muted());
    setTextNonSelectionColor(p.fg());
    setTextSelectionColor(p.fg());
    setBackgroundNonSelectionColor(p.bg());
    setBackgroundSelectionColor(p.selection());
    setBorderSelectionColor(p.selection());
  }

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean focus) {
    super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, focus);
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    Object userObject = node.getUserObject();
    if (userObject instanceof TabMeta meta) {
      describe(meta);
    } else {
      setIcon(folderIcon);
      detail = userObject != null ? String.valueOf(node.getChildCount()) : "";
    }
    return this;
  }

  private void describe(TabMeta note) {
    NoteMeta meta = query.metas().get(note.id());
    boolean open = query.filter().openIds().contains(note.id());
    Color tint = NoteColors.of(meta.color());
    noteIcon.setKind(meta.pinned() ? VectorIcon.Kind.PIN_ON : VectorIcon.Kind.NOTE);
    noteIcon.setColor(tint != null ? tint : open ? palette.accent() : palette.muted());
    setIcon(noteIcon);
    String snippet = query.filter().snippet(note);
    detail =
        snippet != null
            ? snippet
            : RelativeTime.format(note.lastModified(), Instant.now(), ZoneId.systemDefault());
    if (open) {
      detail += "  ·  " + tr("browser.status.open");
    }
    if (query.filter().index() != null && query.filter().index().locked(note.id())) {
      detail += "  ·  " + tr("browser.status.locked");
    }
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    if (d != null && !detail.isEmpty()) {
      d.width += GAP + getFontMetrics(getFont()).stringWidth(detail);
    }
    return d;
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    if (detail.isEmpty()) {
      return;
    }
    FontMetrics fm = g.getFontMetrics(getFont());
    Insets in = getInsets();
    int iconWidth = getIcon() == null ? 0 : getIcon().getIconWidth() + getIconTextGap();
    int x = in.left + iconWidth + fm.stringWidth(getText()) + GAP;
    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
    g.setColor(palette.muted());
    g.setFont(getFont());
    g.drawString(detail, x, y);
  }
}
