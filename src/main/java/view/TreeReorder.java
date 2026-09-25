package view;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.TreePath;

import model.Group;
import model.TabMeta;

/**
 * Manual drag-to-reorder for the notes tree. Avoids Swing's built-in tree DnD, whose macOS LAF
 * crashes when rendering insert lines.
 */
class TreeReorder extends MouseInputAdapter {

  private static final int THRESHOLD = 4;

  private final GroupTree tree;
  private final transient NoteGroupActions actions;
  private transient TreePath source;
  private int pressY;
  private boolean dragging;

  TreeReorder(GroupTree tree, NoteGroupActions actions) {
    this.tree = tree;
    this.actions = actions;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    source = tree.searching() ? null : tree.getPathForLocation(e.getX(), e.getY());
    pressY = e.getY();
    dragging = false;
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (source != null && Math.abs(e.getY() - pressY) > THRESHOLD) {
      dragging = true;
      tree.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    tree.setCursor(Cursor.getDefaultCursor());
    if (dragging && source != null) {
      drop(source, tree.getClosestPathForLocation(e.getX(), e.getY()));
    }
    source = null;
    dragging = false;
  }

  private void drop(TreePath from, TreePath to) {
    Object dragged = GroupNodes.userObject(from);
    Object target = GroupNodes.userObject(to);
    String targetGroup = groupOf(target);
    if (dragged instanceof TabMeta note) {
      String before = target instanceof TabMeta meta ? meta.id() : null;
      if (!note.id().equals(before)) {
        actions.placeNote(note.id(), targetGroup, before);
      }
    } else if (dragged instanceof Group group && !group.id().equals(targetGroup)) {
      actions.moveGroup(group.id(), targetGroup);
    }
  }

  /** The group a drop onto {@code target} lands in: the group itself, or the note's group. */
  private String groupOf(Object target) {
    if (target instanceof Group group) {
      return group.id();
    }
    return target instanceof TabMeta meta ? tree.data().groupOf(meta.id()) : null;
  }
}
