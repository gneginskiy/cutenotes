package view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import model.TabMeta;

/**
 * Notes browser rows: a folder or note icon, the name, and a muted trailing detail — a relative
 * date ("Yesterday") for notes, the item count for groups. Open notes get an accent icon.
 */
class NoteCellRenderer extends DefaultTreeCellRenderer {

  private static final int GAP = 10;

  private final transient Set<String> openIds;
  private final transient VectorIcon noteIcon = new VectorIcon(VectorIcon.Kind.NOTE, 14);
  private final transient VectorIcon openIcon = new VectorIcon(VectorIcon.Kind.NOTE, 14);
  private final transient VectorIcon folderIcon = new VectorIcon(VectorIcon.Kind.FOLDER, 14);
  private transient UiPalette palette = UiPalette.current();
  private String detail = "";

  NoteCellRenderer(Set<String> openIds) {
    this.openIds = openIds;
    setIconTextGap(7);
    applyPalette(palette);
  }

  final void applyPalette(UiPalette p) {
    palette = p;
    noteIcon.setColor(p.muted());
    openIcon.setColor(p.accent());
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
      boolean open = openIds.contains(meta.id());
      setIcon(open ? openIcon : noteIcon);
      detail = RelativeTime.format(meta.lastModified(), Instant.now(), ZoneId.systemDefault());
      detail = open ? detail + "  ·  open" : detail;
    } else {
      setIcon(folderIcon);
      detail = userObject != null ? String.valueOf(node.getChildCount()) : "";
    }
    return this;
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
