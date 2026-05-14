package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;

import model.Theme;

final class TextAreaFactory {

  private TextAreaFactory() {}

  static JTextArea create(String text) {
    JTextArea area = new JTextArea(text);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    area.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
    applyTheme(area, ThemeHolder.current());
    area.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.emptySet());
    installUndoRedo(area);
    LineOps.install(area);
    return area;
  }

  static void applyTheme(JTextArea area, Theme t) {
    if (!area.getBackground().equals(t.bg())) {
      area.setBackground(t.bg());
    }
    if (!area.getForeground().equals(t.fg())) {
      area.setForeground(t.fg());
    }
    Color caret = t.caret() != null ? t.caret() : Colors.inverse(t.bg());
    if (!caret.equals(area.getCaretColor())) {
      area.setCaretColor(caret);
    }
    Font next = new Font(t.fontFamily(), Font.PLAIN, t.fontSize());
    if (!area.getFont().equals(next)) {
      area.setFont(next);
    }
  }

  private static void installUndoRedo(JTextArea area) {
    UndoManager undo = new UndoManager();
    area.getDocument().addUndoableEditListener(e -> undo.addEdit(e.getEdit()));
    int shortcut = ShortcutMask.menu();
    area.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcut), "Undo");
    area.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcut), "Redo");
    area.getInputMap()
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcut | InputEvent.SHIFT_DOWN_MASK), "Redo");
    area.getActionMap().put("Undo", undoAction(undo));
    area.getActionMap().put("Redo", redoAction(undo));
  }

  private static AbstractAction undoAction(UndoManager undo) {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (undo.canUndo()) {
          undo.undo();
        }
      }
    };
  }

  private static AbstractAction redoAction(UndoManager undo) {
    return new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (undo.canRedo()) {
          undo.redo();
        }
      }
    };
  }
}
