package view;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JOptionPane;

import dao.GroupStore;
import model.GroupData;
import model.TabMeta;

/** All mutating operations of the notes browser: groups, membership, deletion and reopening. */
class NoteGroupActions {

  private final Component parent;
  private final GroupTree tree;
  private final transient GroupStore store;
  private final transient NotesBrowserDialog.Callbacks callbacks;

  NoteGroupActions(
      Component parent, GroupTree tree, GroupStore store, NotesBrowserDialog.Callbacks callbacks) {
    this.parent = parent;
    this.tree = tree;
    this.store = store;
    this.callbacks = callbacks;
  }

  void open(String noteId) {
    callbacks.open().accept(noteId);
  }

  void newGroup(String parentId) {
    String id = UUID.randomUUID().toString();
    apply(tree.data().withGroup(id, "New group", parentId));
    tree.editGroup(id);
  }

  void rename(String groupId, String name) {
    apply(tree.data().renamed(groupId, sanitize(name)));
  }

  void deleteGroup(String groupId) {
    if (confirm("Delete this group? Its notes become ungrouped.", "Delete group")) {
      apply(tree.data().removed(groupId));
    }
  }

  void removeFromGroup(String noteId) {
    apply(tree.data().assign(noteId, null));
  }

  void deleteNote(TabMeta note) {
    if (confirm("Delete '" + note.name() + "' permanently?", "Delete note")) {
      callbacks.delete().accept(note.id());
      apply(tree.data().assign(note.id(), null));
      tree.removeNote(note);
    }
  }

  void placeNote(String noteId, String targetGroupId, String beforeNoteId) {
    GroupData base = tree.data().assign(noteId, targetGroupId);
    List<String> order = new ArrayList<>(tree.currentNoteOrder());
    order.remove(noteId);
    int index = beforeNoteId == null ? -1 : order.indexOf(beforeNoteId);
    order.add(index < 0 ? order.size() : index, noteId);
    apply(base.withOrder(order));
  }

  void moveGroup(String groupId, String targetParentId) {
    apply(tree.data().reparented(groupId, targetParentId));
  }

  private void apply(GroupData next) {
    tree.setData(next);
    store.write(next);
  }

  private boolean confirm(String message, String title) {
    return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION)
        == JOptionPane.YES_OPTION;
  }

  private static String sanitize(String name) {
    return name == null ? "" : name.replace('\t', ' ').replace('\n', ' ').trim();
  }
}
