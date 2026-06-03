package view;

import java.awt.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import model.Group;
import model.TabMeta;

/** Renders group nodes as {@code Name (count)} and note leaves with their date and open marker. */
class NoteCellRenderer extends DefaultTreeCellRenderer {

  private static final DateTimeFormatter FMT =
      DateTimeFormatter.ofPattern("d MMM HH-mm", Locale.ENGLISH);

  private final transient Set<String> openIds;

  NoteCellRenderer(Set<String> openIds) {
    this.openIds = openIds;
  }

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean focus) {
    super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, focus);
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    Object userObject = node.getUserObject();
    if (userObject instanceof TabMeta meta) {
      String date =
          LocalDateTime.ofInstant(meta.lastModified(), ZoneId.systemDefault()).format(FMT);
      setText(meta.name() + "  —  " + date + (openIds.contains(meta.id()) ? "  • open" : ""));
    } else if (userObject instanceof Group group) {
      setText(group.name() + " (" + node.getChildCount() + ")");
    } else if (userObject != null) {
      setText(userObject + " (" + node.getChildCount() + ")");
    }
    return this;
  }
}
