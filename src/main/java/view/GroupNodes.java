package view;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import model.Group;
import model.GroupData;
import model.TabMeta;

/** Builds the {@link javax.swing.JTree} node structure for {@link GroupData} plus its notes. */
final class GroupNodes {

  private GroupNodes() {}

  static DefaultMutableTreeNode root(
      GroupData data, List<TabMeta> notes, Set<String> openIds, boolean showAll, String ungrouped) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    for (Group g : data.childrenOf(null)) {
      root.add(group(g, data, notes, openIds, showAll));
    }
    DefaultMutableTreeNode bucket = new DefaultMutableTreeNode(ungrouped);
    addNotes(bucket, null, data, notes, openIds, showAll);
    if (bucket.getChildCount() > 0 || data.groups().isEmpty()) {
      root.add(bucket);
    }
    return root;
  }

  private static DefaultMutableTreeNode group(
      Group g, GroupData data, List<TabMeta> notes, Set<String> openIds, boolean showAll) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(g);
    for (Group child : data.childrenOf(g.id())) {
      node.add(group(child, data, notes, openIds, showAll));
    }
    addNotes(node, g.id(), data, notes, openIds, showAll);
    return node;
  }

  private static void addNotes(
      DefaultMutableTreeNode node,
      String groupId,
      GroupData data,
      List<TabMeta> notes,
      Set<String> openIds,
      boolean showAll) {
    for (TabMeta meta : notes) {
      if (!showAll && openIds.contains(meta.id())) {
        continue;
      }
      if (Objects.equals(data.groupOf(meta.id()), groupId)) {
        node.add(new DefaultMutableTreeNode(meta, false));
      }
    }
  }

  static TreePath pathOfGroup(DefaultMutableTreeNode root, String id) {
    var nodes = root.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
      if (node.getUserObject() instanceof Group g && g.id().equals(id)) {
        return new TreePath(node.getPath());
      }
    }
    return null;
  }
}
