package view;

import static view.Messages.tr;

import java.awt.Component;
import java.awt.Window;
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
  private final Toast toast;

  NoteGroupActions(
      Component parent,
      GroupTree tree,
      GroupStore store,
      NotesBrowserDialog.Callbacks callbacks,
      Toast toast) {
    this.parent = parent;
    this.tree = tree;
    this.store = store;
    this.callbacks = callbacks;
    this.toast = toast;
  }

  /** Opens a note; found by its text, it opens with the search term highlighted. */
  void open(String noteId) {
    NoteFilter filter = tree.query().filter();
    if (filter.searching() && !filter.tagQuery()) {
      callbacks.openFound().accept(noteId, filter.query());
    } else {
      callbacks.open().accept(noteId);
    }
  }

  void togglePin(String noteId) {
    NoteMetas metas = tree.query().metas();
    metas.put(metas.get(noteId).withPinned(!metas.get(noteId).pinned()));
    tree.refresh();
  }

  void setColor(String noteId, String color) {
    NoteMetas metas = tree.query().metas();
    metas.put(metas.get(noteId).withColor(color));
    callbacks.changed().accept(noteId);
    tree.refresh();
  }

  void newGroup(String parentId) {
    String id = UUID.randomUUID().toString();
    apply(tree.data().withGroup(id, tr("browser.newGroup"), parentId));
    tree.editGroup(id);
  }

  void rename(String groupId, String name) {
    apply(tree.data().renamed(groupId, sanitize(name)));
  }

  void deleteGroup(String groupId) {
    if (confirm(tr("browser.deleteGroup.confirm"), tr("browser.deleteGroup.title"))) {
      apply(tree.data().removed(groupId));
    }
  }

  void removeFromGroup(String noteId) {
    apply(tree.data().assign(noteId, null));
  }

  /** Moves a note to "Recently deleted": no question asked, since it can be restored. */
  void deleteNote(TabMeta note) {
    callbacks.delete().accept(note.id());
    apply(tree.data().assign(note.id(), null));
    tree.removeNote(note);
    toast.flash(tr("trash.moved", note.name()));
  }

  /** Closes the browser and shows "Recently deleted". */
  void showTrash() {
    if (parent instanceof Window window) {
      window.dispose();
    }
    callbacks.showTrash().run();
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
