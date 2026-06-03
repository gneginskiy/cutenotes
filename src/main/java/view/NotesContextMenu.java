package view;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import model.TabMeta;

/** Right-click menu whose items depend on whether a note, a group or empty space was clicked. */
final class NotesContextMenu {

  private NotesContextMenu() {}

  static void show(GroupTree tree, NoteGroupActions actions, int x, int y) {
    TreePath path = tree.getClosestPathForLocation(x, y);
    if (path != null && tree.getPathBounds(path) != null && tree.getPathBounds(path).y <= y) {
      tree.setSelectionPath(path);
    } else {
      tree.clearSelection();
    }
    JPopupMenu menu = new JPopupMenu();
    TabMeta note = tree.selectedNote();
    String groupId = tree.selectedGroupId();
    if (note != null) {
      add(menu, "Open", () -> actions.open(note.id()));
      add(menu, "Remove from group", () -> actions.removeFromGroup(note.id()));
      add(menu, "Delete permanently", () -> actions.deleteNote(note));
    } else if (groupId != null) {
      add(menu, "New subgroup", () -> actions.newGroup(groupId));
      add(menu, "Rename", () -> tree.editGroup(groupId));
      add(menu, "Delete group", () -> actions.deleteGroup(groupId));
    } else {
      add(menu, "New group", () -> actions.newGroup(null));
    }
    menu.show(tree, x, y);
  }

  private static void add(JPopupMenu menu, String label, Runnable action) {
    JMenuItem item = new JMenuItem(label);
    item.addActionListener(e -> action.run());
    menu.add(item);
  }
}
