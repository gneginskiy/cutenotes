package view;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import model.TabMeta;

/** Keyboard of the notes tree: Enter opens, F2 renames a group, Delete removes, letters search. */
final class BrowserTreeKeys extends KeyAdapter {

  private final GroupTree tree;
  private final NoteGroupActions actions;
  private final BrowserToolbar toolbar;

  BrowserTreeKeys(GroupTree tree, NoteGroupActions actions, BrowserToolbar toolbar) {
    this.tree = tree;
    this.actions = actions;
    this.toolbar = toolbar;
  }

  @Override
  public void keyPressed(KeyEvent e) {
    int code = e.getKeyCode();
    if (code == KeyEvent.VK_ENTER && tree.selectedNote() != null) {
      actions.open(tree.selectedNote().id());
    } else if (code == KeyEvent.VK_F2 && tree.selectedGroupId() != null) {
      tree.editGroup(tree.selectedGroupId());
    } else if (code == KeyEvent.VK_DELETE || code == KeyEvent.VK_BACK_SPACE) {
      deleteSelected();
    }
  }

  /** Typing a letter while the list has focus continues the search. */
  @Override
  public void keyTyped(KeyEvent e) {
    char c = e.getKeyChar();
    boolean plain = (e.getModifiersEx() & ShortcutMask.menu()) == 0;
    if (plain && !Character.isISOControl(c) && !tree.isEditing()) {
      toolbar.typeIntoSearch(c);
      e.consume();
    }
  }

  private void deleteSelected() {
    TabMeta note = tree.selectedNote();
    if (note != null) {
      actions.deleteNote(note);
    } else if (tree.selectedGroupId() != null) {
      actions.deleteGroup(tree.selectedGroupId());
    }
  }
}
