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

  static void install(JTextComponent area) {
    int mask = ShortcutMask.menu();
    int shifted = mask | InputEvent.SHIFT_DOWN_MASK;
    bind(area, KeyEvent.VK_X, mask, EditorClipboard::cutLine);
    bind(area, KeyEvent.VK_C, mask, EditorClipboard::copyLine);
    bind(area, KeyEvent.VK_D, mask, LineOps::duplicateLine);
    bind(area, KeyEvent.VK_ENTER, mask, LineOps::insertBlankLines);
    bind(area, KeyEvent.VK_UP, shifted, a -> moveLine(a, -1));
    bind(area, KeyEvent.VK_DOWN, shifted, a -> moveLine(a, 1));
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
  private static void moveLine(JTextComponent area, int direction) {
    StyledDocument doc = (StyledDocument) area.getDocument();
    String text = docText(area);
    int caret = area.getCaretPosition();
    int current = Lines.index(text, caret);
    int other = current + direction;
    if (other < 0 || other >= Lines.count(text)) {
      return;
    }
    int top = Math.min(current, other);
    int topStart = Lines.startOffset(text, top);
    int bottomStart = Lines.startOffset(text, Math.max(current, other));
    int bottomEnd = Lines.endInclusive(text, bottomStart);
    boolean bottomHasNewline = text.substring(bottomStart, bottomEnd).endsWith("\n");
    List<StyledRuns.Run> topRuns = StyledRuns.capture(doc, topStart, bottomStart);
    List<StyledRuns.Run> bottomRuns = StyledRuns.capture(doc, bottomStart, bottomEnd);
    int col = caret - Lines.startOffset(text, current);
    doc.remove(topStart, bottomEnd - topStart);
    int pos = StyledRuns.insert(doc, topStart, bottomRuns);
    if (!bottomHasNewline) {
      doc.insertString(pos, "\n", null);
      pos++;
      topRuns = StyledRuns.trimTrailingNewline(topRuns);
    }
    StyledRuns.insert(doc, pos, topRuns);
    int firstBlock = (bottomEnd - bottomStart) + (bottomHasNewline ? 0 : 1);
    int newStart = direction == -1 ? topStart : topStart + firstBlock;
    area.setCaretPosition(Math.min(newStart + col, doc.getLength()));
  }

  @SneakyThrows
  static String docText(JTextComponent area) {
    Document doc = area.getDocument();
    return doc.getText(0, doc.getLength());
  }
}
