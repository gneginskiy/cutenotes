package view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import model.Group;
import model.GroupData;
import model.TabMeta;

/** A {@link JTree} showing notes nested under a forest of groups, with an "Ungrouped" bucket. */
class GroupTree extends JTree {

  static final String UNGROUPED = "Ungrouped";

  private final transient List<TabMeta> notes;
  private final transient Set<String> openIds;
  private transient GroupData data;
  private transient BiConsumer<String, String> onRename = (id, name) -> {};
  private boolean showAll = true;

  GroupTree(List<TabMeta> notes, Set<String> openIds, GroupData data) {
    this.notes =
        new ArrayList<>(
            notes.stream().sorted(Comparator.comparing(TabMeta::lastModified).reversed()).toList());
    this.openIds = openIds;
    this.data = data;
    setRootVisible(false);
    setShowsRootHandles(true);
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    NoteCellRenderer renderer = new NoteCellRenderer(openIds);
    setCellRenderer(renderer);
    setCellEditor(new GroupCellEditor(this, renderer));
    setEditable(true);
    setDragEnabled(true);
    setDropMode(DropMode.ON);
    rebuild();
  }

  GroupData data() {
    return data;
  }

  void setData(GroupData next) {
    this.data = next;
    rebuild();
  }

  void setShowAll(boolean all) {
    this.showAll = all;
    rebuild();
  }

  void setOnRename(BiConsumer<String, String> handler) {
    this.onRename = handler;
  }

  void removeNote(TabMeta meta) {
    notes.removeIf(n -> n.id().equals(meta.id()));
    rebuild();
  }

  TabMeta selectedNote() {
    return userObject() instanceof TabMeta meta ? meta : null;
  }

  String selectedGroupId() {
    return userObject() instanceof Group group ? group.id() : null;
  }

  String dropTargetGroupId(TreePath path) {
    if (path == null) {
      return null;
    }
    Object target = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
    if (target instanceof Group group) {
      return group.id();
    }
    return target instanceof TabMeta meta ? data.groupOf(meta.id()) : null;
  }

  void editGroup(String id) {
    TreePath path = GroupNodes.pathOfGroup((DefaultMutableTreeNode) getModel().getRoot(), id);
    if (path != null) {
      expandPath(path.getParentPath());
      setSelectionPath(path);
      startEditingAtPath(path);
    }
  }

  @Override
  public String convertValueToText(
      Object value, boolean sel, boolean exp, boolean leaf, int row, boolean focus) {
    Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
    if (userObject instanceof Group group) {
      return group.name();
    }
    if (userObject instanceof TabMeta meta) {
      return meta.name();
    }
    return userObject == null ? "" : userObject.toString();
  }

  private Object userObject() {
    TreePath path = getSelectionPath();
    return path == null
        ? null
        : ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
  }

  private void commitEdit(TreePath path, Object newValue) {
    Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
    if (userObject instanceof Group group) {
      SwingUtilities.invokeLater(() -> onRename.accept(group.id(), String.valueOf(newValue)));
    }
  }

  private void rebuild() {
    DefaultMutableTreeNode root = GroupNodes.root(data, notes, openIds, showAll, UNGROUPED);
    setModel(new GroupTreeModel(root, this::commitEdit));
    for (int i = 0; i < getRowCount(); i++) {
      expandRow(i);
    }
  }
}
