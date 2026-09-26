package view;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;

/**
 * Undo/redo of a note editor. Edits of one gesture (an image drag-resize fires two document edits
 * per mouse move) are grouped into a single step; with Swing's default limit of 100 edits a single
 * resize used to push the whole text history out.
 */
final class EditorUndo implements UndoableEditListener {

  static final int LIMIT = 2000;

  private final UndoManager manager = new UndoManager();
  private CompoundEdit group;

  private static void bind(JTextComponent area, KeyStroke ks, Runnable action) {
    KeyBindings.bindFocused(area, ks, ks.toString(), action);
  }

  EditorUndo(JTextComponent area) {
    manager.setLimit(LIMIT);
    area.getDocument().addUndoableEditListener(this);
    int mask = ShortcutMask.menu();
    bind(area, KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask), this::undo);
    bind(area, KeyStroke.getKeyStroke(KeyEvent.VK_Y, mask), this::redo);
    bind(
        area, KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask | InputEvent.SHIFT_DOWN_MASK), this::redo);
  }

  @Override
  public void undoableEditHappened(UndoableEditEvent e) {
    if (group != null) {
      group.addEdit(e.getEdit());
    } else {
      manager.addEdit(e.getEdit());
    }
  }

  void beginGroup() {
    if (group == null) {
      group = new CompoundEdit();
    }
  }

  void endGroup() {
    if (group != null) {
      CompoundEdit done = group;
      group = null;
      done.end();
      manager.addEdit(done);
    }
  }

  void discardAll() {
    manager.discardAllEdits();
  }

  void undo() {
    endGroup();
    if (manager.canUndo()) {
      manager.undo();
    }
  }

  void redo() {
    if (manager.canRedo()) {
      manager.redo();
    }
  }
}
