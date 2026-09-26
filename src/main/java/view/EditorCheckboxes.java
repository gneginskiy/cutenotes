package view;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.SimpleAttributeSet;

import markdown.ListLine;

/**
 * Task checkboxes ({@code - [ ] …}): a click on the brackets or Cmd/Ctrl+Enter toggles them.
 * Cmd/Ctrl+Enter on a plain list item turns it into a task; elsewhere it still inserts blank lines.
 */
final class EditorCheckboxes {

  private EditorCheckboxes() {}

  static void install(NoteEditor pane) {
    KeyBindings.bindFocused(
        pane,
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ShortcutMask.menu()),
        "list-check",
        () -> {
          if (!toggle(pane, pane.getCaretPosition())) {
            LineOps.insertBlankLines(pane);
          }
        });
    pane.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            int offset = at(pane, e.getPoint());
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1 && offset >= 0) {
              toggle(pane, offset);
            }
          }
        });
  }

  /** Offset of the checkbox character under {@code p} (the brackets count too), or -1. */
  static int at(NoteEditor pane, Point p) {
    int offset = pane.viewToModel2D(p);
    String text = LineOps.docText(pane);
    int start = Lines.start(text, Math.min(Math.max(offset, 0), text.length()));
    ListLine item = ListLine.parse(EditorLists.line(text, start));
    if (item == null || !item.checkbox()) {
      return -1;
    }
    int check = start + item.checkOffset();
    return offset >= check - 1 && offset <= check + 1 ? check : -1;
  }

  /** Toggles the checkbox of the line at {@code offset}, or adds one to a plain list item. */
  static boolean toggle(NoteEditor pane, int offset) {
    String text = LineOps.docText(pane);
    int start = Lines.start(text, Math.min(offset, text.length()));
    ListLine item = ListLine.parse(EditorLists.line(text, start));
    if (item == null) {
      return false;
    }
    pane.editAsOneStep(
        doc -> {
          if (item.checkbox()) {
            LineIndent.replaceChar(doc, start + item.checkOffset(), item.checked() ? " " : "x");
          } else {
            int at = start + item.indent().length() + item.marker().length() + 1;
            doc.insertString(at, "[ ] ", new SimpleAttributeSet());
          }
        });
    return true;
  }
}
