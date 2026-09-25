package view;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;

import lombok.SneakyThrows;

final class LineOps {

  private static final int BLANK_LINES = 10;

  private LineOps() {}

  static void install(JTextComponent area, EditorUndo undo) {
    install(area, undo, ShortcutMask.menu());
  }

  /**
   * {@code mask} is Cmd on macOS and Ctrl on Windows / Linux ({@link ShortcutMask#menu()}), so
   * "move line" is Cmd+Shift+↑/↓ on a Mac and Ctrl+Shift+↑/↓ elsewhere.
   */
  static void install(JTextComponent area, EditorUndo undo, int mask) {
    int shifted = mask | InputEvent.SHIFT_DOWN_MASK;
    bind(area, KeyEvent.VK_X, mask, EditorClipboard::cutLine);
    bind(area, KeyEvent.VK_C, mask, EditorClipboard::copyLine);
    bind(area, KeyEvent.VK_D, mask, a -> grouped(undo, () -> duplicateLine(a)));
    bind(area, KeyEvent.VK_ENTER, mask, LineOps::insertBlankLines);
    bind(area, KeyEvent.VK_UP, shifted, a -> LineMover.move(a, undo, -1));
    bind(area, KeyEvent.VK_DOWN, shifted, a -> LineMover.move(a, undo, 1));
  }

  private static void grouped(EditorUndo undo, Runnable edit) {
    undo.beginGroup();
    try {
      edit.run();
    } finally {
      undo.endGroup();
    }
  }

  private static void bind(JTextComponent area, int key, int mod, Consumer<JTextComponent> action) {
    String name = "line-op-" + key;
    area.getInputMap().put(KeyStroke.getKeyStroke(key, mod), name);
    area.getActionMap()
        .put(
            name,
            new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                action.accept(area);
              }
            });
  }

  @SneakyThrows
  private static void insertBlankLines(JTextComponent area) {
    int caret = area.getCaretPosition();
    area.getDocument().insertString(caret, "\n".repeat(BLANK_LINES), null);
    area.setCaretPosition(caret + BLANK_LINES);
  }

  @SneakyThrows
  private static void duplicateLine(JTextComponent area) {
    StyledDocument doc = (StyledDocument) area.getDocument();
    String text = docText(area);
    int caret = area.getCaretPosition();
    int start = Lines.start(text, caret);
    int end = Lines.endInclusive(text, caret);
    int col = caret - start;
    boolean noNewline = !text.substring(start, end).endsWith("\n");
    List<StyledRuns.Run> runs = StyledRuns.capture(doc, start, end);
    int at = end;
    if (noNewline) {
      doc.insertString(end, "\n", null);
      at = end + 1;
    }
    StyledRuns.insert(doc, at, runs);
    area.setCaretPosition(at + col);
  }

  @SneakyThrows
  static String docText(JTextComponent area) {
    Document doc = area.getDocument();
    return doc.getText(0, doc.getLength());
  }
}
