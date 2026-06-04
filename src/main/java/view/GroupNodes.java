package view;

import java.util.ArrayList;
import java.util.Comparator;
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
    List<TabMeta> inGroup = new ArrayList<>();
    for (TabMeta meta : notes) {
      if (!showAll && openIds.contains(meta.id())) {
        continue;
      }
      if (Objects.equals(data.groupOf(meta.id()), groupId)) {
        inGroup.add(meta);
      }
    }
    inGroup.sort(Comparator.comparingInt(m -> data.orderIndex(m.id())));
    for (TabMeta meta : inGroup) {
      node.add(new DefaultMutableTreeNode(meta, false));
    }
  }

  static List<String> noteOrder(DefaultMutableTreeNode root) {
    List<String> ids = new ArrayList<>();
    collectNotes(root, ids);
    return ids;
  }

  private static void collectNotes(DefaultMutableTreeNode node, List<String> ids) {
    for (int i = 0; i < node.getChildCount(); i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
      if (child.getUserObject() instanceof TabMeta meta) {
        ids.add(meta.id());
      }
      collectNotes(child, ids);
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
