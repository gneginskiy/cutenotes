package view;

import static view.Messages.tr;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import model.NoteMeta;
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
      NoteMeta meta = tree.query().metas().get(note.id());
      add(menu, tr("browser.open"), () -> actions.open(note.id()));
      add(
          menu,
          tr(meta.pinned() ? "browser.unpin" : "browser.pin"),
          () -> actions.togglePin(note.id()));
      menu.add(ColorMenu.build(meta.color(), color -> actions.setColor(note.id(), color)));
      add(menu, tr("browser.removeFromGroup"), () -> actions.removeFromGroup(note.id()));
      add(menu, tr("browser.delete"), () -> actions.deleteNote(note));
    } else if (groupId != null) {
      add(menu, tr("browser.newSubgroup"), () -> actions.newGroup(groupId));
      add(menu, tr("browser.renameGroup"), () -> tree.editGroup(groupId));
      add(menu, tr("browser.deleteGroup"), () -> actions.deleteGroup(groupId));
    } else {
      add(menu, tr("browser.newGroup"), () -> actions.newGroup(null));
    }
    menu.show(tree, x, y);
  }

  private static void add(JPopupMenu menu, String label, Runnable action) {
    JMenuItem item = new JMenuItem(label);
    item.addActionListener(e -> action.run());
    menu.add(item);
  }
}
