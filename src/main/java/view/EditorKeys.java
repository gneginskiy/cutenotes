package view;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Menu items for editor commands. They run exactly what the editor's own key binding runs, so a
 * menu item and its shortcut can never behave differently.
 */
final class EditorKeys {

  private EditorKeys() {}

  /** Runs the action {@code ks} is bound to in {@code editor}; returns whether there is one. */
  static boolean fire(NoteEditor editor, KeyStroke ks) {
    Object name = editor.getInputMap().get(ks);
    Action action = name == null ? null : editor.getActionMap().get(name);
    if (action == null) {
      return false;
    }
    editor.requestFocusInWindow();
    action.actionPerformed(new ActionEvent(editor, ActionEvent.ACTION_PERFORMED, ""));
    return true;
  }

  /** A menu item showing {@code ks} that performs it on the active note. */
  static JMenuItem item(String label, KeyStroke ks, TabsPane tabs) {
    JMenuItem item = new JMenuItem(label);
    item.setAccelerator(ks);
    item.addActionListener(e -> fire(tabs.activeArea(), ks));
    return item;
  }
}
