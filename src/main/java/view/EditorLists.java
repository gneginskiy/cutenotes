package view;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.SimpleAttributeSet;

import markdown.ListLine;

/**
 * Lists and checkboxes while typing: Enter continues a list (and ends it on an empty item), Tab /
 * Shift+Tab indent and outdent, Cmd/Ctrl+Enter or a click toggles a checkbox. Everything stays
 * plain Markdown text; each gesture is a single undo step.
 */
final class EditorLists {

  private EditorLists() {}

  static void install(NoteEditor pane) {
    Action insertBreak = pane.getActionMap().get(DefaultEditorKit.insertBreakAction);
    Action insertTab = pane.getActionMap().get(DefaultEditorKit.insertTabAction);
    bind(
        pane,
        KeyEvent.VK_ENTER,
        0,
        "list-enter",
        () -> fallback(pane, continueList(pane), insertBreak));
    bind(pane, KeyEvent.VK_TAB, 0, "list-indent", () -> fallback(pane, shift(pane, 1), insertTab));
    bind(pane, KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK, "list-outdent", () -> shift(pane, -1));
    EditorCheckboxes.install(pane);
  }

  /** Enter on a list item; returns false when the line is no list item (normal Enter). */
  static boolean continueList(NoteEditor pane) {
    if (pane.getSelectionStart() != pane.getSelectionEnd()) {
      return false;
    }
    String text = LineOps.docText(pane);
    int caret = pane.getCaretPosition();
    int start = Lines.start(text, caret);
    String line = line(text, start);
    ListLine item = ListLine.parse(line);
    if (item == null || (caret - start < item.prefixLength() && !item.isEmpty())) {
      return false;
    }
    pane.editAsOneStep(
        doc -> {
          if (item.isEmpty()) {
            doc.remove(start, line.length());
          } else {
            String next = "\n" + item.continuation();
            doc.insertString(caret, next, new SimpleAttributeSet());
            pane.setCaretPosition(caret + next.length());
          }
        });
    return true;
  }

  /**
   * Indents ({@code direction} 1) or outdents (-1) the selected lines, or the caret's line. With
   * nothing selected, Tab outside a list item is an ordinary tab: returns false then.
   */
  static boolean shift(NoteEditor pane, int direction) {
    String text = LineOps.docText(pane);
    int selStart = pane.getSelectionStart();
    int selEnd = pane.getSelectionEnd();
    boolean multi = Lines.start(text, selStart) != Lines.start(text, selEnd);
    if (direction > 0
        && !multi
        && ListLine.parse(line(text, Lines.start(text, selStart))) == null) {
      return false;
    }
    pane.editAsOneStep(doc -> LineIndent.apply(doc, text, selStart, selEnd, direction));
    return true;
  }

  static String line(String text, int start) {
    int end = text.indexOf('\n', start);
    return text.substring(start, end < 0 ? text.length() : end);
  }

  private static void fallback(NoteEditor pane, boolean handled, Action standard) {
    if (!handled && standard != null) {
      standard.actionPerformed(new ActionEvent(pane, ActionEvent.ACTION_PERFORMED, ""));
    }
  }

  private static void bind(NoteEditor pane, int key, int mod, String name, Runnable action) {
    KeyBindings.bindFocused(pane, KeyStroke.getKeyStroke(key, mod), name, action);
  }
}
