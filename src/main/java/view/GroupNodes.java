package view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import model.Group;
import model.GroupData;
import model.TabMeta;

/** Builds the {@link javax.swing.JTree} node structure for {@link GroupData} plus its notes. */
final class GroupNodes {

  private GroupNodes() {}

  /**
   * The tree for {@code data}. While searching, a group is listed only when it holds a match (at
   * any depth) or its own name matches, in which case all its notes are shown.
   */
  static DefaultMutableTreeNode root(
      GroupData data, List<TabMeta> notes, NoteFilter filter, String ungrouped) {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    for (Group g : data.childrenOf(null)) {
      addGroup(root, g, data, notes, filter);
    }
    DefaultMutableTreeNode bucket = new DefaultMutableTreeNode(ungrouped);
    addNotes(bucket, null, data, notes, filter);
    if (bucket.getChildCount() > 0 || (data.groups().isEmpty() && !filter.searching())) {
      root.add(bucket);
    }
    return root;
  }

  private static void addGroup(
      DefaultMutableTreeNode parent, Group g, GroupData data, List<TabMeta> notes, NoteFilter f) {
    boolean nameHit = f.searching() && f.nameMatches(g.name());
    NoteFilter inside = nameHit ? f.withQuery("") : f;
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(g);
    for (Group child : data.childrenOf(g.id())) {
      addGroup(node, child, data, notes, inside);
    }
    addNotes(node, g.id(), data, notes, inside);
    if (!f.searching() || nameHit || node.getChildCount() > 0) {
      parent.add(node);
    }
  }

  private static void addNotes(
      DefaultMutableTreeNode node,
      String groupId,
      GroupData data,
      List<TabMeta> notes,
      NoteFilter filter) {
    List<TabMeta> inGroup = new ArrayList<>();
    for (TabMeta meta : notes) {
      if (filter.accepts(meta) && Objects.equals(data.groupOf(meta.id()), groupId)) {
        inGroup.add(meta);
      }
    }
    inGroup.sort(Comparator.comparingInt(m -> data.orderIndex(m.id())));
    for (TabMeta meta : inGroup) {
      node.add(new DefaultMutableTreeNode(meta, false));
    }
  }

  /** The user object behind {@code path}; null for no path. */
  static Object userObject(TreePath path) {
    return path == null
        ? null
        : ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
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
