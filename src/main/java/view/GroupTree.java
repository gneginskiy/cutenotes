package view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
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
  private transient GroupData data;
  private transient BiConsumer<String, String> onRename = (id, name) -> {};
  private transient NoteFilter filter;

  GroupTree(List<TabMeta> notes, Set<String> openIds, GroupData data) {
    this.notes =
        new ArrayList<>(
            notes.stream().sorted(Comparator.comparing(TabMeta::lastModified).reversed()).toList());
    this.filter = NoteFilter.all(openIds);
    this.data = data;
    setRootVisible(false);
    setShowsRootHandles(true);
    setRowHeight(26);
    putClientProperty("JTree.lineStyle", "None");
    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    NoteCellRenderer renderer = new NoteCellRenderer(openIds);
    setCellRenderer(renderer);
    setCellEditor(new GroupCellEditor(this, renderer));
    setEditable(true);
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
    this.filter = filter.withShowOpen(all);
    rebuild();
  }

  void setQuery(String query) {
    this.filter = filter.withQuery(query);
    rebuild();
  }

  /** Selects the first note in tree order; returns it, or null when nothing is listed. */
  TabMeta selectFirstNote() {
    for (int row = 0; row < getRowCount(); row++) {
      TreePath path = getPathForRow(row);
      if (GroupNodes.userObject(path) instanceof TabMeta meta) {
        setSelectionPath(path);
        scrollPathToVisible(path);
        return meta;
      }
    }
    return null;
  }

  /** While a search narrows the list, dragging would reorder against notes that are hidden. */
  boolean searching() {
    return filter.searching();
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

  List<String> currentNoteOrder() {
    return GroupNodes.noteOrder((DefaultMutableTreeNode) getModel().getRoot());
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
    return GroupNodes.userObject(getSelectionPath());
  }

  private void commitEdit(TreePath path, Object newValue) {
    if (GroupNodes.userObject(path) instanceof Group group) {
      SwingUtilities.invokeLater(() -> onRename.accept(group.id(), String.valueOf(newValue)));
    }
  }

  private void rebuild() {
    DefaultMutableTreeNode root = GroupNodes.root(data, notes, filter, UNGROUPED);
    setModel(new GroupTreeModel(root, this::commitEdit));
    for (int i = 0; i < getRowCount(); i++) {
      expandRow(i);
    }
  }
}
